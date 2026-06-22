package com.myblob.module.news.service;

import com.myblob.module.news.entity.NewsItem;
import com.myblob.module.news.entity.NewsSource;
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * RSS 抓取策略
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RssFetchStrategy implements NewsFetchStrategy {

    private final NewsProxyConfig newsProxyConfig;

    @Override
    public boolean supports(String method) {
        return "RSS".equals(method);
    }

    @Override
    public List<NewsItem> fetch(NewsSource source) {
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
                byte[] rawBytes = is.readAllBytes();
                byte[] cleaned = sanitizeXmlBytes(rawBytes);
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

    private boolean needsProxy(String feedUrl, NewsSource source) {
        if (newsProxyConfig.getProxy().getHttp().isEnabled()) {
            String host = newsProxyConfig.getProxy().getHttp().getHost();
            return host != null && !host.isEmpty();
        }
        return false;
    }

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

    private byte[] sanitizeXmlBytes(byte[] bytes) {
        String s = new String(bytes, StandardCharsets.UTF_8);
        if (s.startsWith("\uFEFF")) s = s.substring(1);
        s = s.replaceAll("(?i)<!DOCTYPE[^>]*>", "");
        s = s.replaceAll("(?is)<script[^>]*>.*?</script>", "");
        s = s.replaceAll("(?is)<style[^>]*>.*?</style>", "");
        s = s.replaceAll("(?i)\\sdefer(\\s|>)", " defer=\"defer\"$1");
        s = s.replaceAll("(?i)\\sasync(\\s|>)", " async=\"async\"$1");
        s = s.replaceAll("&(?!amp;|lt;|gt;|quot;|apos;|#\\d+;|#x[0-9a-fA-F]+;)", "&amp;");
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
            if (title.isEmpty()) return null;

            String summary = entry.getDescription() != null
                    ? entry.getDescription().getValue().replaceAll("<[^>]*>", "").trim() : "";
            String link = entry.getLink() != null ? entry.getLink() : "";

            LocalDateTime publishedAt = null;
            if (entry.getPublishedDate() != null) {
                publishedAt = entry.getPublishedDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
            }

            String thumbnailUrl = extractThumbnail(entry, link);
            String mediaType = "image";
            String videoUrl = null;

            if (link.contains("youtube.com/watch") || link.contains("youtu.be/") || link.contains("vimeo.com/")) {
                mediaType = "video";
                videoUrl = link;
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

    private String extractThumbnail(SyndEntry entry, String link) {
        List<SyndEnclosure> enclosures = entry.getEnclosures();
        if (enclosures != null && !enclosures.isEmpty()) {
            for (SyndEnclosure enc : enclosures) {
                String url = enc.getUrl();
                String type = enc.getType();
                if (url != null && type != null && type.startsWith("image/")) return url;
                if (url != null && (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png") || url.endsWith(".webp"))) return url;
            }
        }
        try {
            List<org.jdom2.Element> foreignMarkup = entry.getForeignMarkup();
            if (foreignMarkup != null) {
                for (org.jdom2.Element el : foreignMarkup) {
                    String name = el.getName();
                    if ("content".equals(name) || "thumbnail".equals(name)) {
                        String url = el.getAttributeValue("url");
                        if (url != null && !url.isEmpty()) return url;
                    }
                    if ("group".equals(name)) {
                        for (org.jdom2.Element child : el.getChildren()) {
                            if ("content".equals(child.getName()) || "thumbnail".equals(child.getName())) {
                                String url = child.getAttributeValue("url");
                                if (url != null && !url.isEmpty()) return url;
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        if (entry.getDescription() != null) {
            String html = entry.getDescription().getValue();
            if (html != null) {
                String imgUrl = extractImgFromHtml(html);
                if (imgUrl != null) return imgUrl;
            }
        }
        return null;
    }

    private String extractImgFromHtml(String html) {
        if (html == null || html.isEmpty()) return null;
        try {
            Document doc = Jsoup.parse(html);
            Element img = doc.selectFirst("img[src]");
            if (img != null) {
                String src = img.attr("src");
                if (src != null && !src.isEmpty() && src.startsWith("http")) return src;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String extractYouTubeId(String url) {
        if (url == null) return null;
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
}
