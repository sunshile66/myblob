package com.myblob.module.news.service;

import com.myblob.module.news.entity.NewsItem;
import com.myblob.module.news.entity.NewsSource;
import com.myblob.module.news.repository.NewsItemRepository;
import com.myblob.module.news.repository.NewsSourceRepository;
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
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsFetchService {

    private final NewsSourceRepository newsSourceRepository;
    private final NewsItemRepository newsItemRepository;
    private final NewsFilterService newsFilterService;
    private final NewsProxyConfig newsProxyConfig;
    private final NewsTranslationService translationService;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String HN_TOP_STORIES = "https://hacker-news.firebaseio.com/v0/topstories.json";
    private static final String HN_ITEM = "https://hacker-news.firebaseio.com/v0/item/%d.json";
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
            List<NewsItem> items = switch (method) {
                case "RSS", "RSSHUB" -> fetchRss(source);
                case "API" -> fetchApi(source);
                case "JSOUP" -> fetchJsoup(source);
                default -> fetchRss(source);
            };

            if (items.isEmpty()) {
                source.setConsecutiveErrors(0);
                newsSourceRepository.save(source);
                return 0;
            }

            // Apply filtering
            List<NewsItem> filtered = newsFilterService.filterAndScore(items, source);

            // Translate EN items
            // - Always translate airline category (user requirement)
            // - For other categories, only if global translateEnabled
            boolean isAirlineCategory = "国际航司".equals(source.getCategory());
            if (isAirlineCategory || newsProxyConfig.getFetch().isTranslateEnabled()) {
                translateEnglishItems(filtered);
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
        String feedUrl = newsProxyConfig.resolveFeedUrl(source.getFeedUrl());
        try {
            URL url = URI.create(feedUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
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
                    if (count >= maxItems) break;
                    NewsItem item = mapRssEntry(entry, source);
                    if (item != null) {
                        items.add(item);
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("RSS fetch failed for {}: {}", source.getName(), e.getMessage());
        }
        return items;
    }

    /** Strip control characters that break XML parsing, and remove DOCTYPE declarations */
    private byte[] sanitizeXmlBytes(byte[] bytes) {
        String s = new String(bytes, StandardCharsets.UTF_8);
        // Remove DOCTYPE declaration (some parsers disallow it)
        s = s.replaceAll("(?i)<!DOCTYPE[^>]*>", "");
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            // Allow tab (9), LF (10), CR (13), and chars >= 0x20 (space)
            if (c == 0x9 || c == 0xA || c == 0xD || c >= 0x20) {
                sb.append(c);
            }
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private NewsItem mapRssEntry(SyndEntry entry, NewsSource source) {
        try {
            String title = entry.getTitle() != null ? entry.getTitle().trim() : "";
            if (title.isEmpty()) return null;

            String summary = entry.getDescription() != null
                    ? entry.getDescription().getValue().replaceAll("<[^>]*>", "").trim()
                    : "";

            String link = entry.getLink() != null ? entry.getLink() : "";

            LocalDateTime publishedAt = null;
            if (entry.getPublishedDate() != null) {
                publishedAt = entry.getPublishedDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
            }

            return NewsItem.builder()
                    .title(title.length() > 500 ? title.substring(0, 497) + "..." : title)
                    .summary(summary.length() > 1000 ? summary.substring(0, 997) + "..." : summary)
                    .sourceUrl(link)
                    .sourcePlatform(source.getPlatformName())
                    .sourceName(source.getName())
                    .category(source.getCategory())
                    .language(source.getLanguage())
                    .publishedAt(publishedAt != null ? publishedAt : LocalDateTime.now())
                    .fetchedAt(LocalDateTime.now())
                    .qualityScore(50)
                    .isFiltered(false)
                    .build();
        } catch (Exception e) {
            return null;
        }
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
            if (storyIds == null) return items;

            int maxItems = Math.min(newsProxyConfig.getFetch().getMaxItemsPerSource(), storyIds.size());
            for (int i = 0; i < maxItems; i++) {
                try {
                    Map<String, Object> story = restTemplate.getForObject(
                            String.format(HN_ITEM, storyIds.get(i)), Map.class);
                    if (story == null || "job".equals(story.get("type"))) continue;

                    String title = (String) story.getOrDefault("title", "");
                    if (title.isEmpty()) continue;

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
        String[] urls = {REDDIT_PROGRAMMING, REDDIT_TECH};
        for (String apiUrl : urls) {
            try {
                // Reddit requires a descriptive User-Agent
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.set("User-Agent", "MyBlob:news-aggregator:v1.0 (by /u/myblob_bot)");
                org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);
                org.springframework.http.ResponseEntity<Map> respEntity = restTemplate.exchange(
                        apiUrl, org.springframework.http.HttpMethod.GET, entity, Map.class);
                Map<String, Object> response = respEntity.getBody();
                if (response == null) continue;

                Map<String, Object> data = (Map<String, Object>) response.get("data");
                if (data == null) continue;

                List<Map<String, Object>> children = (List<Map<String, Object>>) data.get("children");
                if (children == null) continue;

                for (Map<String, Object> child : children) {
                    Map<String, Object> postData = (Map<String, Object>) child.get("data");
                    if (postData == null) continue;

                    String title = (String) postData.getOrDefault("title", "");
                    if (title.isEmpty() || (Boolean) postData.getOrDefault("stickied", false)) continue;

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
        return fetchRss(source);
    }

    @SuppressWarnings("unchecked")
    private List<NewsItem> fetchWeiboHotSearch(NewsSource source) {
        List<NewsItem> items = new ArrayList<>();
        try {
            String apiUrl = "https://weibo.com/ajax/statuses/hot_band";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Referer", "https://weibo.com/");
            headers.set("Accept", "application/json, text/plain, */*");
            headers.set("Accept-Language", "zh-CN,zh;q=0.9");
            org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<Map> respEntity = restTemplate.exchange(
                    apiUrl, org.springframework.http.HttpMethod.GET, entity, Map.class);
            Map<String, Object> resp = respEntity.getBody();
            if (resp == null || resp.get("data") == null) return items;
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            List<Map<String, Object>> bandList = (List<Map<String, Object>>) data.get("band_list");
            if (bandList == null) return items;

            int maxItems = Math.min(newsProxyConfig.getFetch().getMaxItemsPerSource(), bandList.size());
            for (int i = 0; i < maxItems; i++) {
                Map<String, Object> band = bandList.get(i);
                String word = (String) band.getOrDefault("word", "");
                if (word.isEmpty()) continue;
                String rawUrl = (String) band.getOrDefault("word_scheme", "");
                if (rawUrl.isEmpty()) rawUrl = "https://s.weibo.com/weibo?q=" + word;
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
                        .qualityScore(Math.min(100, 40 + hotVal / 20000))
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
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(newsProxyConfig.getProxy().getReadTimeout())
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .get();

            Elements cards = doc.select(".HotList-item, .HotItem");
            int maxItems = Math.min(newsProxyConfig.getFetch().getMaxItemsPerSource(), cards.size());
            for (int i = 0; i < maxItems; i++) {
                Element card = cards.get(i);
                Element titleEl = card.selectFirst(".HotList-itemTitle, h2, .HotItem-title");
                if (titleEl == null) continue;
                String title = titleEl.text().trim();
                if (title.isEmpty()) continue;

                Element linkEl = card.selectFirst("a");
                String link = linkEl != null ? linkEl.attr("href") : "";
                if (!link.startsWith("http")) link = "https://www.zhihu.com" + link;

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
                if (linkEl == null) continue;

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

    /** Translate title and summary for English-language items */
    private void translateEnglishItems(List<NewsItem> items) {
        for (NewsItem item : items) {
            if (!"EN".equals(item.getLanguage())) continue;
            try {
                String tTitle = translationService.translate(item.getTitle());
                if (tTitle != null && !tTitle.equals(item.getTitle())) {
                    item.setTranslatedTitle(tTitle);
                }
                String tSummary = translationService.translate(item.getSummary());
                if (tSummary != null && !tSummary.equals(item.getSummary())) {
                    item.setTranslatedSummary(tSummary);
                }
            } catch (Exception e) {
                log.debug("Translate failed for [{}]: {}", item.getTitle(), e.getMessage());
            }
        }
    }

    @Transactional
    private int saveNewItems(List<NewsItem> items, NewsSource source) {
        int saved = 0;
        for (NewsItem item : items) {
            if (!newsItemRepository.existsBySourceUrl(item.getSourceUrl())) {
                newsItemRepository.save(item);
                saved++;
            }
        }
        return saved;
    }
}
