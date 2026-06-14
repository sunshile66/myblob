package com.myblob.module.news.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblob.module.news.entity.NewsItem;
import com.myblob.module.news.entity.NewsSource;
import com.myblob.module.news.repository.NewsItemRepository;
import com.myblob.module.news.repository.NewsSourceRepository;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsFetchService {

    private final NewsSourceRepository newsSourceRepository;
    private final NewsItemRepository newsItemRepository;
    private final NewsFilterService newsFilterService;
    private final NewsProxyConfig newsProxyConfig;
    private final NewsTranslationService translationService;
    private final NewsTopicService topicService;
    private final NewsSentimentService sentimentService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String HN_TOP_STORIES = "https://hacker-news.firebaseio.com/v0/topstories.json";
    private static final String HN_ITEM = "https://hacker-news.firebaseio.com/v0/item/%d.json";

    /** 文章最大保留天数，超过的旧文章直接丢弃 */
    private static final int MAX_ARTICLE_AGE_DAYS = 30;
    
    /** 不做日期过滤的类别（如官方媒体RSS常返回较旧内容） */
    private static final Set<String> SKIP_AGE_FILTER_CATEGORIES = Set.of("官方媒体");

    // === 翻译熔断器 ===
    private static final int TRANSLATE_FAIL_THRESHOLD = 5;
    private static final long TRANSLATE_COOLDOWN_MS = 30 * 60 * 1000L; // 30 min
    private volatile int translateFailCount = 0;
    private volatile long translateCooldownUntil = 0;
    private static final String REDDIT_PROGRAMMING = "https://www.reddit.com/r/programming/hot.json?limit=30";
    private static final String REDDIT_TECH = "https://www.reddit.com/r/technology/hot.json?limit=30";
    private static final String GITHUB_TRENDING = "https://github.com/trending";

    public int fetchAllSources() {
        List<NewsSource> sources = newsSourceRepository.findByEnabledTrueOrderByPriorityAsc();
        if (sources.isEmpty()) {
            log.info("No enabled news sources found");
            return 0;
        }
        log.info("Starting fetch for {} enabled sources", sources.size());

        List<CompletableFuture<Integer>> futures = sources.stream()
                .map(source -> CompletableFuture.supplyAsync(() -> fetchSource(source)))
                .toList();

        int totalFetched = futures.stream()
                .map(CompletableFuture::join)
                .mapToInt(Integer::intValue)
                .sum();

        log.info("Fetch complete: {} new items from {} sources", totalFetched, sources.size());
        return totalFetched;
    }

    @Async
    public CompletableFuture<Integer> fetchSourceAsync(NewsSource source) {
        return CompletableFuture.completedFuture(fetchSource(source));
    }

    public int fetchSource(NewsSource source) {
        try {
            String method = source.getFetchMethod() != null ? source.getFetchMethod().toUpperCase() : "RSS";
            // TWITTER / BLUESKY 已移除（国内 GFW 无法访问）
            if ("TWITTER".equals(method) || "BLUESKY".equals(method)) {
                log.debug("Skipping source [{}]: method {} is no longer supported", source.getName(), method);
                return 0;
            }
            List<NewsItem> items = switch (method) {
                case "RSS" -> fetchRss(source);
                case "API" -> fetchApi(source);
                case "JSOUP" -> fetchJsoup(source);
                // TWITTER / BLUESKY 已移除（国内无法访问）
                default -> fetchRss(source);
            };

            if (items.isEmpty()) {
                source.setConsecutiveErrors(0);
                newsSourceRepository.save(source);
                return 0;
            }

            // 过滤过期文章（官方媒体类别跳过日期过滤）
            String category = source.getCategory();
            boolean skipAgeFilter = SKIP_AGE_FILTER_CATEGORIES.contains(category);
            if (!skipAgeFilter) {
                LocalDateTime cutoff = LocalDateTime.now().minusDays(MAX_ARTICLE_AGE_DAYS);
                int beforeStale = items.size();
                items = items.stream()
                        .filter(i -> i.getPublishedAt() == null || i.getPublishedAt().isAfter(cutoff))
                        .toList();
                if (items.size() < beforeStale) {
                    log.debug("Source [{}]: dropped {} stale articles (>{})", source.getName(), beforeStale - items.size(), MAX_ARTICLE_AGE_DAYS + "d");
                }
            } else {
                log.debug("Source [{}]: category='{}', skipping age filter", source.getName(), category);
            }

            // Apply filtering
            List<NewsItem> filtered = newsFilterService.filterAndScore(items, source);

            // 话题提取 + 情感分析（在翻译前执行，同时分析原始中文和英文内容）
            topicService.extractTopics(filtered);
            sentimentService.analyzeSentiment(filtered);

            // Translate EN items（带熔断保护）
            boolean isAirlineCategory = "国际航司".equals(source.getCategory());
            if (isAirlineCategory || newsProxyConfig.getFetch().isTranslateEnabled()) {
                if (System.currentTimeMillis() < translateCooldownUntil) {
                    log.debug("Translation skipped: cooldown active ({} failures)", translateFailCount);
                } else {
                    translateEnglishItems(filtered);
                }
            }

            // Save new items
            int saved = saveNewItems(filtered, source);

            source.setLastFetchedAt(LocalDateTime.now());
            source.setConsecutiveErrors(0);
            newsSourceRepository.save(source);

            log.debug("Source [{}]: fetched {}, after filter {}, saved {}",
                    source.getName(), items.size(), filtered.size(), saved);
            return saved;

        } catch (Exception e) {
            log.error("Failed to fetch source [{}]: {}", source.getName(), e.getMessage());
            source.setErrorCount(source.getErrorCount() + 1);
            source.setConsecutiveErrors(source.getConsecutiveErrors() + 1);
            source.setLastFetchedAt(LocalDateTime.now());
            newsSourceRepository.save(source);
            return 0;
        }
    }

    private List<NewsItem> fetchRss(NewsSource source) {
        List<NewsItem> items = new ArrayList<>();
        String feedUrl = source.getFeedUrl();
        boolean needsProxy = needsProxy(feedUrl, source);
        try {
            URL url = URI.create(feedUrl).toURL();
            HttpURLConnection conn = openConnection(url, needsProxy);
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            conn.setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml, */*");
            conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            conn.setConnectTimeout(newsProxyConfig.getProxy().getConnectTimeout());
            conn.setReadTimeout(newsProxyConfig.getProxy().getReadTimeout());

            try (InputStream is = conn.getInputStream()) {
                // Read raw bytes
                byte[] rawBytes = is.readAllBytes();
                // Sanitize: strip invalid XML chars (control chars except tab, LF, CR)
                byte[] cleaned = sanitizeXmlBytes(rawBytes);
                // Build XML reader that allows DOCTYPE
                XmlReader reader = new XmlReader(new java.io.ByteArrayInputStream(cleaned));
                SyndFeed feed = new SyndFeedInput().build(reader);
                int maxItems = newsProxyConfig.getFetch().getMaxItemsPerSource();
                int count = 0;

                for (SyndEntry entry : feed.getEntries()) {
                    if (count >= maxItems)
                        break;
                    NewsItem item = mapRssEntry(entry, source);
                    if (item != null) {
                        items.add(item);
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("RSS fetch failed for {}: {}. Attempting Jsoup fallback.", source.getName(), e.getMessage());
            // Fallback: try to extract feeds using Jsoup HTML parsing
            try {
                return fetchJsoup(source);
            } catch (Exception ex2) {
                log.debug("Jsoup fallback also failed for {}: {}", source.getName(), ex2.getMessage());
            }
        }
        return items;
    }

    /**
     * Determine if a feed URL needs HTTP proxy.
     * - Only use proxy if explicitly configured and enabled
     * - If no proxy configured, try direct connection (works on overseas servers)
     */
    private boolean needsProxy(String feedUrl, NewsSource source) {
        // Only use proxy if explicitly configured and enabled
        if (newsProxyConfig.getProxy().getHttp().isEnabled()) {
            String host = newsProxyConfig.getProxy().getHttp().getHost();
            return host != null && !host.isEmpty();
        }
        return false;
    }

    /**
     * Open HttpURLConnection with optional HTTP proxy.
     * Retries once on failure if proxy is used (fallback to direct).
     */
    private HttpURLConnection openConnection(URL url, boolean useProxy) throws Exception {
        if (useProxy) {
            String host = newsProxyConfig.getProxy().getHttp().getHost();
            int port = newsProxyConfig.getProxy().getHttp().getPort();
            if (host != null && !host.isEmpty() && port > 0) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
                log.debug("Using proxy {}:{} for {}", host, port, url);
                return (HttpURLConnection) url.openConnection(proxy);
            }
        }
        return (HttpURLConnection) url.openConnection();
    }

    /**
     * Strip control characters that break XML parsing, and remove DOCTYPE
     * declarations
     */
    /**
     * Sanitize RSS/XML bytes to handle common issues from Chinese and non-standard feeds:
     * - BOM (UTF-8 byte order mark)
     * - DOCTYPE declarations that Rome's parser rejects
     * - HTML entities (&nbsp;, &copy;) not valid in XML
     * - -- inside XML comments
     * - Content before XML declaration (ads, HTML headers)
     * - HTML tags mixed into RSS (script, style blocks)
     * - Non-XML attributes like "defer" without value
     * Falls back to extracting feeds via Jsoup if XML parsing fails.
     */
    private byte[] sanitizeXmlBytes(byte[] bytes) {
        String s = new String(bytes, StandardCharsets.UTF_8);

        // 1. Strip BOM
        if (s.startsWith("﻿")) s = s.substring(1);

        // 2. Remove DOCTYPE
        s = s.replaceAll("(?i)<!DOCTYPE[^>]*>", "");

        // 3. Remove script/style blocks with their content
        s = s.replaceAll("(?is)<script[^>]*>.*?</script>", "");
        s = s.replaceAll("(?is)<style[^>]*>.*?</style>", "");

        // 4. Fix "defer" and similar HTML boolean attributes without values
        s = s.replaceAll("(?i)\\sdefer(\\s|>)", " defer=\"defer\"$1");
        s = s.replaceAll("(?i)\\sasync(\\s|>)", " async=\"async\"$1");
        s = s.replaceAll("(?i)\\schecked(\\s|>)", " checked=\"checked\"$1");
        s = s.replaceAll("(?i)\\sdisabled(\\s|>)", " disabled=\"disabled\"$1");
        s = s.replaceAll("(?i)\\sselected(\\s|>)", " selected=\"selected\"$1");

        // 5. Remove -- from inside XML comments (replace with ==)
        StringBuilder result = new StringBuilder();
        java.util.regex.Pattern commentPattern = java.util.regex.Pattern.compile("<!--(.*?)-->", java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher cm = commentPattern.matcher(s);
        while (cm.find()) {
            String comment = cm.group(1).replace("--", "==").replace("$", "\\$");
            cm.appendReplacement(result, "<!--" + comment + "-->");
        }
        cm.appendTail(result);
        s = result.toString();

        // 6. Find XML start: trim content before <?xml or <rss or <feed or <channel
        int xmlStart = -1;
        for (String marker : new String[]{"<?xml", "<rss", "<feed", "<RDF", "<channel", "<atom"}) {
            int idx = s.indexOf(marker);
            if (idx >= 0 && (xmlStart < 0 || idx < xmlStart)) xmlStart = idx;
        }
        if (xmlStart > 0) s = s.substring(xmlStart);

        // 7. Replace non-breaking space with regular space
        s = s.replace(" ", " ");

        // 8. Encode bare & not followed by entity reference
        s = s.replaceAll("&(?!amp;|lt;|gt;|quot;|apos;|#\\d+;|#x[0-9a-fA-F]+;)", "&amp;");

        // 9. Strip control characters except tab, LF, CR
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == 0x9 || c == 0xA || c == 0xD || c >= 0x20) {
                sb.append(c);
            }
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private NewsItem mapRssEntry(SyndEntry entry, NewsSource source) {
        try {
            String title = entry.getTitle() != null ? entry.getTitle().trim() : "";
            if (title.isEmpty())
                return null;

            String summary = entry.getDescription() != null
                    ? entry.getDescription().getValue().replaceAll("<[^>]*>", "").trim()
                    : "";

            String link = entry.getLink() != null ? entry.getLink() : "";

            LocalDateTime publishedAt = null;
            if (entry.getPublishedDate() != null) {
                publishedAt = entry.getPublishedDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
            }

            // Extract thumbnail and media type
            String thumbnailUrl = extractThumbnail(entry, link);
            String mediaType = "image";
            String videoUrl = null;

            // Detect video content (YouTube, Vimeo, etc.)
            if (link.contains("youtube.com/watch") || link.contains("youtu.be/") || link.contains("vimeo.com/")) {
                mediaType = "video";
                videoUrl = link;
                // Extract YouTube thumbnail
                String ytId = extractYouTubeId(link);
                if (ytId != null) {
                    thumbnailUrl = "https://img.youtube.com/vi/" + ytId + "/hqdefault.jpg";
                }
            } else if (thumbnailUrl != null && (thumbnailUrl.endsWith(".mp4") || thumbnailUrl.endsWith(".webm"))) {
                mediaType = "video";
                videoUrl = thumbnailUrl;
            } else if (thumbnailUrl != null) {
                mediaType = "image";
            } else {
                mediaType = "text";
            }

            return NewsItem.builder()
                    .title(title.length() > 500 ? title.substring(0, 497) + "..." : title)
                    .summary(summary.length() > 1000 ? summary.substring(0, 997) + "..." : summary)
                    .sourceUrl(link)
                    .sourcePlatform(source.getPlatformName())
                    .sourceName(source.getName())
                    .category(source.getCategory())
                    .language(source.getLanguage())
                    .thumbnailUrl(thumbnailUrl)
                    .mediaType(mediaType)
                    .videoUrl(videoUrl)
                    .publishedAt(publishedAt != null ? publishedAt : LocalDateTime.now())
                    .fetchedAt(LocalDateTime.now())
                    .qualityScore(50)
                    .isFiltered(false)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extract thumbnail URL from RSS entry:
     * 1. media:content / media:thumbnail namespace
     * 2. enclosure links
     * 3. First img in HTML description
     */
    private String extractThumbnail(SyndEntry entry, String link) {
        // 1. Check enclosures (most common for media RSS)
        List<SyndEnclosure> enclosures = entry.getEnclosures();
        if (enclosures != null && !enclosures.isEmpty()) {
            for (SyndEnclosure enc : enclosures) {
                String url = enc.getUrl();
                String type = enc.getType();
                if (url != null && type != null && type.startsWith("image/")) {
                    return url;
                }
                if (url != null && (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png")
                        || url.endsWith(".webp"))) {
                    return url;
                }
            }
        }

        // 2. Check foreign markup (media:content, media:thumbnail)
        try {
            List<org.jdom2.Element> foreignMarkup = entry.getForeignMarkup();
            if (foreignMarkup != null) {
                for (org.jdom2.Element el : foreignMarkup) {
                    String name = el.getName();
                    if ("content".equals(name) || "thumbnail".equals(name)) {
                        String url = el.getAttributeValue("url");
                        if (url != null && !url.isEmpty())
                            return url;
                    }
                    // Check group > content
                    if ("group".equals(name)) {
                        for (org.jdom2.Element child : el.getChildren()) {
                            if ("content".equals(child.getName()) || "thumbnail".equals(child.getName())) {
                                String url = child.getAttributeValue("url");
                                if (url != null && !url.isEmpty())
                                    return url;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore foreign markup parsing errors
        }

        // 3. Extract from HTML description
        if (entry.getDescription() != null) {
            String html = entry.getDescription().getValue();
            if (html != null) {
                String imgUrl = extractImgFromHtml(html);
                if (imgUrl != null)
                    return imgUrl;
            }
        }

        // 4. Check contents (Atom feeds)
        List<SyndContent> contents = entry.getContents();
        if (contents != null && !contents.isEmpty()) {
            for (SyndContent content : contents) {
                String val = content.getValue();
                if (val != null) {
                    String imgUrl = extractImgFromHtml(val);
                    if (imgUrl != null)
                        return imgUrl;
                }
            }
        }

        return null;
    }

    /** Extract first img src from HTML string */
    private String extractImgFromHtml(String html) {
        if (html == null || html.isEmpty())
            return null;
        try {
            Document doc = Jsoup.parse(html);
            Element img = doc.selectFirst("img[src]");
            if (img != null) {
                String src = img.attr("src");
                if (src != null && !src.isEmpty() && src.startsWith("http"))
                    return src;
            }
        } catch (Exception e) {
            // Fallback to regex
            int idx = html.indexOf("<img");
            if (idx >= 0) {
                int srcIdx = html.indexOf("src=", idx);
                if (srcIdx >= 0) {
                    char quote = html.charAt(srcIdx + 4);
                    int endIdx = html.indexOf(quote, srcIdx + 5);
                    if (endIdx > srcIdx + 5) {
                        String url = html.substring(srcIdx + 5, endIdx);
                        if (url.startsWith("http"))
                            return url;
                    }
                }
            }
        }
        return null;
    }

    /** Extract YouTube video ID from URL */
    private String extractYouTubeId(String url) {
        if (url == null)
            return null;
        if (url.contains("youtube.com/watch")) {
            int idx = url.indexOf("v=");
            if (idx >= 0) {
                String id = url.substring(idx + 2);
                int amp = id.indexOf('&');
                return amp > 0 ? id.substring(0, amp) : id;
            }
        } else if (url.contains("youtu.be/")) {
            String id = url.substring(url.indexOf("youtu.be/") + 9);
            int q = id.indexOf('?');
            return q > 0 ? id.substring(0, q) : id;
        }
        return null;
    }

    private List<NewsItem> fetchApi(NewsSource source) {
        String platform = source.getPlatformName().toLowerCase();
        return switch (platform) {
            case "hackernews" -> fetchHackerNews(source);
            case "reddit" -> fetchReddit(source);
            default -> fetchRss(source);
        };
    }

    @SuppressWarnings("unchecked")
    private List<NewsItem> fetchHackerNews(NewsSource source) {
        List<NewsItem> items = new ArrayList<>();
        try {
            List<Integer> storyIds = restTemplate.getForObject(HN_TOP_STORIES, List.class);
            if (storyIds == null)
                return items;

            int maxItems = Math.min(newsProxyConfig.getFetch().getMaxItemsPerSource(), storyIds.size());
            for (int i = 0; i < maxItems; i++) {
                try {
                    Map<String, Object> story = restTemplate.getForObject(
                            String.format(HN_ITEM, storyIds.get(i)), Map.class);
                    if (story == null || "job".equals(story.get("type")))
                        continue;

                    String title = (String) story.getOrDefault("title", "");
                    if (title.isEmpty())
                        continue;

                    String url = (String) story.getOrDefault("url",
                            "https://news.ycombinator.com/item?id=" + story.get("id"));

                    Integer score = (Integer) story.getOrDefault("score", 0);
                    Integer time = (Integer) story.get("time");
                    LocalDateTime publishedAt = time != null
                            ? LocalDateTime.ofEpochSecond(time, 0, java.time.ZoneOffset.UTC)
                            : LocalDateTime.now();

                    NewsItem item = NewsItem.builder()
                            .title(title)
                            .summary("Score: " + score + " | Comments: " + story.getOrDefault("descendants", 0))
                            .sourceUrl(url)
                            .sourcePlatform(source.getPlatformName())
                            .sourceName(source.getName())
                            .category(source.getCategory())
                            .language("EN")
                            .publishedAt(publishedAt)
                            .fetchedAt(LocalDateTime.now())
                            .qualityScore(Math.min(100, 40 + score / 10))
                            .isFiltered(false)
                            .build();
                    items.add(item);
                } catch (Exception e) {
                    log.debug("Failed to fetch HN item: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("HackerNews fetch failed: {}", e.getMessage());
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    private List<NewsItem> fetchReddit(NewsSource source) {
        List<NewsItem> items = new ArrayList<>();
        String[] urls = { REDDIT_PROGRAMMING, REDDIT_TECH };
        for (String apiUrl : urls) {
            try {
                // Reddit requires a descriptive User-Agent
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.set("User-Agent", "MyBlob:news-aggregator:v1.0 (by /u/myblob_bot)");
                org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);
                org.springframework.http.ResponseEntity<Map> respEntity = restTemplate.exchange(
                        apiUrl, org.springframework.http.HttpMethod.GET, entity, Map.class);
                Map<String, Object> response = respEntity.getBody();
                if (response == null)
                    continue;

                Map<String, Object> data = (Map<String, Object>) response.get("data");
                if (data == null)
                    continue;

                List<Map<String, Object>> children = (List<Map<String, Object>>) data.get("children");
                if (children == null)
                    continue;

                for (Map<String, Object> child : children) {
                    Map<String, Object> postData = (Map<String, Object>) child.get("data");
                    if (postData == null)
                        continue;

                    String title = (String) postData.getOrDefault("title", "");
                    if (title.isEmpty() || (Boolean) postData.getOrDefault("stickied", false))
                        continue;

                    String selftext = (String) postData.getOrDefault("selftext", "");
                    String url = "https://www.reddit.com" + postData.getOrDefault("permalink", "");
                    Integer score = (Integer) postData.getOrDefault("score", 0);
                    Double created = (Double) postData.get("created_utc");
                    LocalDateTime publishedAt = created != null
                            ? LocalDateTime.ofEpochSecond(created.longValue(), 0, java.time.ZoneOffset.UTC)
                            : LocalDateTime.now();

                    NewsItem item = NewsItem.builder()
                            .title(title)
                            .summary(selftext.length() > 500 ? selftext.substring(0, 497) + "..." : selftext)
                            .sourceUrl(url)
                            .sourcePlatform(source.getPlatformName())
                            .sourceName(source.getName())
                            .category(source.getCategory())
                            .language("EN")
                            .publishedAt(publishedAt)
                            .fetchedAt(LocalDateTime.now())
                            .qualityScore(Math.min(100, 35 + score / 5))
                            .isFiltered(false)
                            .build();
                    items.add(item);
                }
            } catch (Exception e) {
                log.warn("Reddit fetch failed for {}: {}", apiUrl, e.getMessage());
            }
        }
        return items;
    }

    private List<NewsItem> fetchJsoup(NewsSource source) {
        String platform = source.getPlatformName().toLowerCase();
        if ("github trending".equals(platform)) {
            return fetchGitHubTrending(source);
        }
        if ("微博热搜".equals(source.getPlatformName())) {
            return fetchWeiboHotSearch(source);
        }
        if ("知乎热榜".equals(source.getPlatformName())) {
            return fetchZhihuHotList(source);
        }
        if ("今日头条".equals(source.getPlatformName())) {
            return fetchToutiaoHot(source);
        }
        if ("百度热搜".equals(source.getPlatformName())) {
            return fetchBaiduHot(source);
        }
        if ("抖音热榜".equals(source.getPlatformName())) {
            return fetchDouyinHot(source);
        }
        // Generic fallback: try to extract feeds using Jsoup's forgiving HTML parser
        return fetchJsoupGeneric(source);
    }

    /**
     * Use Jsoup to extract feed items from RSS/Atom/HTML pages.
     * More forgiving than Rome's strict XML parser — handles broken XML from Chinese sites.
     */
    private List<NewsItem> fetchJsoupGeneric(NewsSource source) {
        List<NewsItem> items = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(source.getFeedUrl())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(newsProxyConfig.getProxy().getReadTimeout())
                    .ignoreContentType(true)
                    .get();

            // Try RSS <item> elements first
            Elements rssItems = doc.select("item, entry");
            int maxItems = newsProxyConfig.getFetch().getMaxItemsPerSource();
            int count = 0;

            for (Element item : rssItems) {
                if (count >= maxItems) break;
                String title = item.selectFirst("title") != null
                        ? item.selectFirst("title").text().trim() : "";
                if (title.isEmpty() && item.selectFirst("entry > title") != null)
                    title = item.selectFirst("entry > title").text().trim();
                if (title.isEmpty()) continue;

                String link = "";
                Element linkEl = item.selectFirst("link");
                if (linkEl != null) {
                    link = linkEl.attr("href");
                    if (link.isEmpty()) link = linkEl.text();
                }

                String summary = "";
                Element descEl = item.selectFirst("description, summary, content");
                if (descEl != null) {
                    summary = Jsoup.parse(descEl.text()).text();
                    if (summary.length() > 500) summary = summary.substring(0, 497) + "...";
                }

                NewsItem newsItem = NewsItem.builder()
                        .title(title).summary(summary).sourceUrl(link)
                        .sourcePlatform(source.getPlatformName())
                        .sourceName(source.getName())
                        .category(source.getCategory())
                        .language(source.getLanguage())
                        .publishedAt(LocalDateTime.now())
                        .mediaType("text").sentiment("neutral")
                        .qualityScore(50).isFiltered(false).deleted(false)
                        .build();
                items.add(newsItem);
                count++;
            }

            // If no RSS items found, try to extract links from the page
            if (items.isEmpty()) {
                Elements links = doc.select("a[href]");
                for (Element a : links) {
                    if (count >= maxItems) break;
                    String title = a.text().trim();
                    if (title.length() < 10) continue;
                    String href = a.absUrl("href");
                    if (href.isEmpty()) continue;

                    NewsItem newsItem = NewsItem.builder()
                            .title(title).sourceUrl(href)
                            .sourcePlatform(source.getPlatformName())
                            .sourceName(source.getName()).category(source.getCategory())
                            .language(source.getLanguage()).publishedAt(LocalDateTime.now())
                            .mediaType("text").sentiment("neutral")
                            .qualityScore(40).isFiltered(false).deleted(false)
                            .build();
                    items.add(newsItem);
                    count++;
                }
            }

            log.debug("Jsoup generic for {}: extracted {} items", source.getName(), items.size());
        } catch (Exception e) {
            log.debug("Jsoup generic fallback failed for {}: {}", source.getName(), e.getMessage());
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    private List<NewsItem> fetchWeiboHotSearch(NewsSource source) {
        List<NewsItem> items = new ArrayList<>();
        try {
            String apiUrl = "https://weibo.com/ajax/statuses/hot_band";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Referer", "https://weibo.com/");
            headers.set("Accept", "application/json, text/plain, */*");
            headers.set("Accept-Language", "zh-CN,zh;q=0.9");
            org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<Map> respEntity = restTemplate.exchange(
                    apiUrl, org.springframework.http.HttpMethod.GET, entity, Map.class);
            Map<String, Object> resp = respEntity.getBody();
            if (resp == null || resp.get("data") == null)
                return items;
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            List<Map<String, Object>> bandList = (List<Map<String, Object>>) data.get("band_list");
            if (bandList == null)
                return items;

            int maxItems = Math.min(newsProxyConfig.getFetch().getMaxItemsPerSource(), bandList.size());
            for (int i = 0; i < maxItems; i++) {
                Map<String, Object> band = bandList.get(i);
                String word = (String) band.getOrDefault("word", "");
                if (word.isEmpty())
                    continue;
                String rawUrl = (String) band.getOrDefault("word_scheme", "");
                if (rawUrl.isEmpty())
                    rawUrl = "https://s.weibo.com/weibo?q=" + word;
                Object rawHot = band.get("raw_hot");
                int hotVal = rawHot instanceof Number ? ((Number) rawHot).intValue() : 0;

                NewsItem newsItem = NewsItem.builder()
                        .title(word)
                        .summary("热度: " + hotVal)
                        .sourceUrl(rawUrl)
                        .sourcePlatform(source.getPlatformName())
                        .sourceName(source.getName())
                        .category(source.getCategory())
                        .language("CN")
                        .publishedAt(LocalDateTime.now())
                        .fetchedAt(LocalDateTime.now())
                        .hotScore(hotVal)
                        .qualityScore(Math.min(100, 30 + (int)(Math.log10(Math.max(hotVal, 1)) * 12)))
                        .isFiltered(false)
                        .build();
                items.add(newsItem);
            }
        } catch (Exception e) {
            log.warn("Weibo fetch failed: {}", e.getMessage());
        }
        return items;
    }

    private List<NewsItem> fetchZhihuHotList(NewsSource source) {
        List<NewsItem> items = new ArrayList<>();
        try {
            // Zhihu API needs auth, fall back to page scraping
            Document doc = Jsoup.connect("https://www.zhihu.com/hot")
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(newsProxyConfig.getProxy().getReadTimeout())
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .get();

            Elements cards = doc.select(".HotList-item, .HotItem");
            int maxItems = Math.min(newsProxyConfig.getFetch().getMaxItemsPerSource(), cards.size());
            for (int i = 0; i < maxItems; i++) {
                Element card = cards.get(i);
                Element titleEl = card.selectFirst(".HotList-itemTitle, h2, .HotItem-title");
                if (titleEl == null)
                    continue;
                String title = titleEl.text().trim();
                if (title.isEmpty())
                    continue;

                Element linkEl = card.selectFirst("a");
                String link = linkEl != null ? linkEl.attr("href") : "";
                if (!link.startsWith("http"))
                    link = "https://www.zhihu.com" + link;

                Element excerptEl = card.selectFirst(".HotList-itemExcerpt, .HotItem-excerpt, p");
                String summary = excerptEl != null ? excerptEl.text().trim() : "";

                NewsItem item = NewsItem.builder()
                        .title(title)
                        .summary(summary)
                        .sourceUrl(link.isEmpty() ? "https://www.zhihu.com/hot" : link)
                        .sourcePlatform(source.getPlatformName())
                        .sourceName(source.getName())
                        .category(source.getCategory())
                        .language("CN")
                        .publishedAt(LocalDateTime.now())
                        .fetchedAt(LocalDateTime.now())
                        .qualityScore(55)
                        .isFiltered(false)
                        .build();
                items.add(item);
            }
        } catch (Exception e) {
            log.warn("Zhihu fetch failed: {}", e.getMessage());
        }
        return items;
    }

    /**
     * 今日头条热榜抓取
     */
    @SuppressWarnings("unchecked")
    private List<NewsItem> fetchToutiaoHot(NewsSource source) {
        List<NewsItem> items = new ArrayList<>();
        try {
            // 头条热榜 API
            String apiUrl = "https://www.toutiao.com/hot-event/hot-board/?origin=toutiao_pc";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Referer", "https://www.toutiao.com/");
            headers.set("Accept", "application/json, text/plain, */*");
            org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<Map> respEntity = restTemplate.exchange(
                    apiUrl, org.springframework.http.HttpMethod.GET, entity, Map.class);
            Map<String, Object> resp = respEntity.getBody();
            if (resp == null || !resp.containsKey("data"))
                return items;
            
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) resp.get("data");
            if (dataList == null)
                return items;

            int maxItems = Math.min(newsProxyConfig.getFetch().getMaxItemsPerSource(), dataList.size());
            for (int i = 0; i < maxItems; i++) {
                Map<String, Object> item = dataList.get(i);
                String title = (String) item.getOrDefault("Title", "");
                if (title.isEmpty())
                    continue;
                
                String hotValue = String.valueOf(item.getOrDefault("HotValue", 0));
                String url = (String) item.getOrDefault("Url", "");
                if (url.isEmpty()) {
                    String clusterId = (String) item.getOrDefault("ClusterId", "");
                    url = "https://www.toutiao.com/trending/" + clusterId + "/";
                }

                NewsItem newsItem = NewsItem.builder()
                        .title(title)
                        .summary("热度: " + hotValue)
                        .sourceUrl(url)
                        .sourcePlatform(source.getPlatformName())
                        .sourceName(source.getName())
                        .category(source.getCategory())
                        .language("CN")
                        .publishedAt(LocalDateTime.now())
                        .fetchedAt(LocalDateTime.now())
                        .qualityScore(60)
                        .isFiltered(false)
                        .build();
                items.add(newsItem);
            }
        } catch (Exception e) {
            log.warn("Toutiao fetch failed: {}", e.getMessage());
        }
        return items;
    }

    /**
     * 百度热搜抓取
     */
    private List<NewsItem> fetchBaiduHot(NewsSource source) {
        List<NewsItem> items = new ArrayList<>();
        try {
            Document doc = Jsoup.connect("https://top.baidu.com/board?tab=realtime")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(newsProxyConfig.getProxy().getReadTimeout())
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .get();

            // 百度热搜的热榜列表
            Elements items_ = doc.select(".category-wrap_iQLoo .content_1YWBm");
            int maxItems = Math.min(newsProxyConfig.getFetch().getMaxItemsPerSource(), items_.size());
            for (int i = 0; i < maxItems; i++) {
                Element el = items_.get(i);
                Element titleEl = el.selectFirst(".c-single-text-ellipsis");
                if (titleEl == null)
                    continue;
                String title = titleEl.text().trim();
                if (title.isEmpty())
                    continue;

                Element linkEl = el.selectFirst("a");
                String link = linkEl != null ? linkEl.attr("href") : "";
                if (!link.startsWith("http"))
                    link = "https://top.baidu.com" + link;

                Element hotEl = el.selectFirst(".hot-index_1Bl1a");
                String hot = hotEl != null ? hotEl.text().trim() : "0";

                NewsItem newsItem = NewsItem.builder()
                        .title(title)
                        .summary("热度: " + hot)
                        .sourceUrl(link)
                        .sourcePlatform(source.getPlatformName())
                        .sourceName(source.getName())
                        .category(source.getCategory())
                        .language("CN")
                        .publishedAt(LocalDateTime.now())
                        .fetchedAt(LocalDateTime.now())
                        .qualityScore(58)
                        .isFiltered(false)
                        .build();
                items.add(newsItem);
            }
        } catch (Exception e) {
            log.warn("Baidu fetch failed: {}", e.getMessage());
        }
        return items;
    }

    /**
     * 抖音热榜抓取
     */
    private List<NewsItem> fetchDouyinHot(NewsSource source) {
        List<NewsItem> items = new ArrayList<>();
        try {
            // 抖音热搜 API
            String apiUrl = "https://www.douyin.com/aweme/v1/web/hot/search/list/?device_platform=webapp&aid=6383&channel=channel_pc_web&detail_list=1";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Referer", "https://www.douyin.com/");
            headers.set("Accept", "application/json, text/plain, */*");
            org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<Map> respEntity = restTemplate.exchange(
                    apiUrl, org.springframework.http.HttpMethod.GET, entity, Map.class);
            Map<String, Object> resp = respEntity.getBody();
            if (resp == null || !resp.containsKey("data"))
                return items;
            
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            List<Map<String, Object>> wordList = (List<Map<String, Object>>) data.getOrDefault("word_list", new ArrayList<>());
            if (wordList == null)
                return items;

            int maxItems = Math.min(newsProxyConfig.getFetch().getMaxItemsPerSource(), wordList.size());
            for (int i = 0; i < maxItems; i++) {
                Map<String, Object> word = wordList.get(i);
                String title = (String) word.getOrDefault("word", "");
                if (title.isEmpty())
                    continue;
                
                Object hotVal = word.getOrDefault("hot_value", 0);
                String hot = String.valueOf(hotVal);
                String wordId = (String) word.getOrDefault("word_id", "");
                String url = "https://www.douyin.com/search/" + title;

                NewsItem newsItem = NewsItem.builder()
                        .title(title)
                        .summary("热度: " + hot)
                        .sourceUrl(url)
                        .sourcePlatform(source.getPlatformName())
                        .sourceName(source.getName())
                        .category(source.getCategory())
                        .language("CN")
                        .publishedAt(LocalDateTime.now())
                        .fetchedAt(LocalDateTime.now())
                        .qualityScore(58)
                        .isFiltered(false)
                        .build();
                items.add(newsItem);
            }
        } catch (Exception e) {
            log.warn("Douyin fetch failed: {}", e.getMessage());
        }
        return items;
    }

    private List<NewsItem> fetchGitHubTrending(NewsSource source) {
        List<NewsItem> items = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(GITHUB_TRENDING)
                    .userAgent(newsProxyConfig.getProxy().getUserAgent())
                    .timeout(newsProxyConfig.getProxy().getReadTimeout())
                    .get();

            Elements repos = doc.select("article.Box-row");
            int maxItems = Math.min(newsProxyConfig.getFetch().getMaxItemsPerSource(), repos.size());

            for (int i = 0; i < maxItems; i++) {
                Element repo = repos.get(i);
                Element linkEl = repo.selectFirst("h2 a");
                if (linkEl == null)
                    continue;

                String[] parts = linkEl.text().replaceAll("\\s+", " ").trim().split(" / ");
                String repoName = parts.length > 1 ? parts[1].trim() : parts[0].trim();
                String fullName = linkEl.attr("href").replace("/", "");

                Element descEl = repo.selectFirst("p");
                String description = descEl != null ? descEl.text().trim() : "";

                Element langEl = repo.selectFirst("[itemprop=programmingLanguage]");
                String language = langEl != null ? langEl.text().trim() : "";

                NewsItem item = NewsItem.builder()
                        .title("GitHub Trending: " + repoName)
                        .summary(description + (language.isEmpty() ? "" : " [" + language + "]"))
                        .sourceUrl("https://github.com" + linkEl.attr("href"))
                        .sourcePlatform(source.getPlatformName())
                        .sourceName(source.getName())
                        .category(source.getCategory())
                        .language("EN")
                        .publishedAt(LocalDateTime.now())
                        .fetchedAt(LocalDateTime.now())
                        .qualityScore(55)
                        .isFiltered(false)
                        .build();
                items.add(item);
            }
        } catch (Exception e) {
            log.warn("GitHub Trending fetch failed: {}", e.getMessage());
        }
        return items;
    }

    // fetchTwitter / fetchBluesky 已移除：国内GFW无法访问

    /** Translate title and summary for English-language items (with circuit breaker) */
    private void translateEnglishItems(List<NewsItem> items) {
        for (NewsItem item : items) {
            if (!"EN".equals(item.getLanguage()))
                continue;
            // 全局熔断器已触发则立即跳过
            if (translateFailCount >= TRANSLATE_FAIL_THRESHOLD || System.currentTimeMillis() < translateCooldownUntil) {
                return;
            }
            try {
                String tTitle = translationService.translate(item.getTitle());
                if (tTitle == null) {
                    onTranslateFail();
                    continue;
                }
                if (!tTitle.equals(item.getTitle())) {
                    item.setTranslatedTitle(tTitle);
                }
                String tSummary = translationService.translate(item.getSummary());
                if (tSummary == null) {
                    onTranslateFail();
                    continue;
                }
                if (!tSummary.equals(item.getSummary())) {
                    item.setTranslatedSummary(tSummary);
                }
                // 翻译成功，重置失败计数
                translateFailCount = 0;
            } catch (Exception e) {
                onTranslateFail();
                log.debug("Translate exception for [{}]: {}", item.getTitle(), e.getMessage());
            }
        }
    }

    private void onTranslateFail() {
        translateFailCount++;
        if (translateFailCount >= TRANSLATE_FAIL_THRESHOLD) {
            translateCooldownUntil = System.currentTimeMillis() + TRANSLATE_COOLDOWN_MS;
            log.warn("Translation circuit breaker OPEN ({} fails), cooldown 30min", translateFailCount);
        }
    }

    @Transactional
    private int saveNewItems(List<NewsItem> items, NewsSource source) {
        if (items.isEmpty()) {
            return 0;
        }
        
        // 批量查询已存在的 URL
        List<String> urls = items.stream().map(NewsItem::getSourceUrl).toList();
        Set<String> existingUrls = new HashSet<>(newsItemRepository.findExistingSourceUrls(urls));
        
        // 过滤出新条目
        List<NewsItem> newItems = items.stream()
                .filter(item -> !existingUrls.contains(item.getSourceUrl()))
                .toList();
        
        // 批量保存（利用 Hibernate batch_size=25 配置）
        if (!newItems.isEmpty()) {
            newsItemRepository.saveAll(newItems);
        }
        
        return newItems.size();
    }
}
