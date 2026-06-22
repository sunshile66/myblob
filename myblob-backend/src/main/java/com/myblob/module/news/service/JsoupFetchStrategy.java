package com.myblob.module.news.service;

import com.myblob.module.news.entity.NewsItem;
import com.myblob.module.news.entity.NewsSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Jsoup 抓取策略（微博、知乎、头条、百度、抖音、GitHub Trending）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsoupFetchStrategy implements NewsFetchStrategy {

    private final RestTemplate restTemplate;
    private final NewsProxyConfig newsProxyConfig;

    @Override
    public boolean supports(String method) {
        return "JSOUP".equals(method);
    }

    @Override
    public List<NewsItem> fetch(NewsSource source) {
        String platform = source.getPlatformName();
        return switch (platform) {
            case "GitHub Trending" -> fetchGitHubTrending(source);
            case "微博热搜" -> fetchWeiboHotSearch(source);
            case "知乎热榜" -> fetchZhihuHotList(source);
            case "今日头条" -> fetchToutiaoHot(source);
            case "百度热搜" -> fetchBaiduHot(source);
            case "抖音热榜" -> fetchDouyinHot(source);
            default -> fetchJsoupGeneric(source);
        };
    }

    private List<NewsItem> fetchJsoupGeneric(NewsSource source) {
        List<NewsItem> items = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(source.getFeedUrl())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(newsProxyConfig.getProxy().getReadTimeout())
                    .ignoreContentType(true)
                    .get();

            Elements rssItems = doc.select("item, entry");
            int maxItems = newsProxyConfig.getFetch().getMaxItemsPerSource();
            int count = 0;

            for (Element item : rssItems) {
                if (count >= maxItems) break;
                String title = item.selectFirst("title") != null
                        ? item.selectFirst("title").text().trim() : "";
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
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Referer", "https://weibo.com/");
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
                        .title(word).summary("热度: " + hotVal).sourceUrl(rawUrl)
                        .sourcePlatform(source.getPlatformName()).sourceName(source.getName())
                        .category(source.getCategory()).language("CN")
                        .publishedAt(LocalDateTime.now()).fetchedAt(LocalDateTime.now())
                        .hotScore(hotVal).qualityScore(Math.min(100, 30 + (int)(Math.log10(Math.max(hotVal, 1)) * 12)))
                        .isFiltered(false).build();
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
            Document doc = Jsoup.connect("https://www.zhihu.com/hot")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(newsProxyConfig.getProxy().getReadTimeout())
                    .header("Accept-Language", "zh-CN,zh;q=0.9").get();

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

                NewsItem item = NewsItem.builder()
                        .title(title).sourceUrl(link.isEmpty() ? "https://www.zhihu.com/hot" : link)
                        .sourcePlatform(source.getPlatformName()).sourceName(source.getName())
                        .category(source.getCategory()).language("CN")
                        .publishedAt(LocalDateTime.now()).fetchedAt(LocalDateTime.now())
                        .qualityScore(55).isFiltered(false).build();
                items.add(item);
            }
        } catch (Exception e) {
            log.warn("Zhihu fetch failed: {}", e.getMessage());
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    private List<NewsItem> fetchToutiaoHot(NewsSource source) {
        List<NewsItem> items = new ArrayList<>();
        try {
            String apiUrl = "https://www.toutiao.com/hot-event/hot-board/?origin=toutiao_pc";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Referer", "https://www.toutiao.com/");
            org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<Map> respEntity = restTemplate.exchange(
                    apiUrl, org.springframework.http.HttpMethod.GET, entity, Map.class);
            Map<String, Object> resp = respEntity.getBody();
            if (resp == null || !resp.containsKey("data")) return items;

            List<Map<String, Object>> dataList = (List<Map<String, Object>>) resp.get("data");
            if (dataList == null) return items;

            int maxItems = Math.min(newsProxyConfig.getFetch().getMaxItemsPerSource(), dataList.size());
            for (int i = 0; i < maxItems; i++) {
                Map<String, Object> item = dataList.get(i);
                String title = (String) item.getOrDefault("Title", "");
                if (title.isEmpty()) continue;
                String hotValue = String.valueOf(item.getOrDefault("HotValue", 0));
                String url = (String) item.getOrDefault("Url", "");
                if (url.isEmpty()) {
                    String clusterId = (String) item.getOrDefault("ClusterId", "");
                    url = "https://www.toutiao.com/trending/" + clusterId + "/";
                }

                NewsItem newsItem = NewsItem.builder()
                        .title(title).summary("热度: " + hotValue).sourceUrl(url)
                        .sourcePlatform(source.getPlatformName()).sourceName(source.getName())
                        .category(source.getCategory()).language("CN")
                        .publishedAt(LocalDateTime.now()).fetchedAt(LocalDateTime.now())
                        .qualityScore(60).isFiltered(false).build();
                items.add(newsItem);
            }
        } catch (Exception e) {
            log.warn("Toutiao fetch failed: {}", e.getMessage());
        }
        return items;
    }

    private List<NewsItem> fetchBaiduHot(NewsSource source) {
        List<NewsItem> items = new ArrayList<>();
        try {
            Document doc = Jsoup.connect("https://top.baidu.com/board?tab=realtime")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(newsProxyConfig.getProxy().getReadTimeout()).get();

            Elements hotItems = doc.select(".category-wrap_iQLoo .content_1YWBm");
            int maxItems = Math.min(newsProxyConfig.getFetch().getMaxItemsPerSource(), hotItems.size());
            for (int i = 0; i < maxItems; i++) {
                Element el = hotItems.get(i);
                Element titleEl = el.selectFirst(".c-single-text-ellipsis");
                if (titleEl == null) continue;
                String title = titleEl.text().trim();
                if (title.isEmpty()) continue;

                Element linkEl = el.selectFirst("a");
                String link = linkEl != null ? linkEl.attr("href") : "";
                if (!link.startsWith("http")) link = "https://top.baidu.com" + link;

                NewsItem newsItem = NewsItem.builder()
                        .title(title).sourceUrl(link)
                        .sourcePlatform(source.getPlatformName()).sourceName(source.getName())
                        .category(source.getCategory()).language("CN")
                        .publishedAt(LocalDateTime.now()).fetchedAt(LocalDateTime.now())
                        .qualityScore(58).isFiltered(false).build();
                items.add(newsItem);
            }
        } catch (Exception e) {
            log.warn("Baidu fetch failed: {}", e.getMessage());
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    private List<NewsItem> fetchDouyinHot(NewsSource source) {
        List<NewsItem> items = new ArrayList<>();
        try {
            String apiUrl = "https://www.douyin.com/aweme/v1/web/hot/search/list/?device_platform=webapp&aid=6383&channel=channel_pc_web&detail_list=1";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Referer", "https://www.douyin.com/");
            org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<Map> respEntity = restTemplate.exchange(
                    apiUrl, org.springframework.http.HttpMethod.GET, entity, Map.class);
            Map<String, Object> resp = respEntity.getBody();
            if (resp == null || !resp.containsKey("data")) return items;

            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            List<Map<String, Object>> wordList = (List<Map<String, Object>>) data.getOrDefault("word_list", new ArrayList<>());
            if (wordList == null) return items;

            int maxItems = Math.min(newsProxyConfig.getFetch().getMaxItemsPerSource(), wordList.size());
            for (int i = 0; i < maxItems; i++) {
                Map<String, Object> word = wordList.get(i);
                String title = (String) word.getOrDefault("word", "");
                if (title.isEmpty()) continue;
                Object hotVal = word.getOrDefault("hot_value", 0);
                String hot = String.valueOf(hotVal);

                NewsItem newsItem = NewsItem.builder()
                        .title(title).summary("热度: " + hot)
                        .sourceUrl("https://www.douyin.com/search/" + title)
                        .sourcePlatform(source.getPlatformName()).sourceName(source.getName())
                        .category(source.getCategory()).language("CN")
                        .publishedAt(LocalDateTime.now()).fetchedAt(LocalDateTime.now())
                        .qualityScore(58).isFiltered(false).build();
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
            Document doc = Jsoup.connect("https://github.com/trending")
                    .userAgent(newsProxyConfig.getProxy().getUserAgent())
                    .timeout(newsProxyConfig.getProxy().getReadTimeout()).get();

            Elements repos = doc.select("article.Box-row");
            int maxItems = Math.min(newsProxyConfig.getFetch().getMaxItemsPerSource(), repos.size());

            for (int i = 0; i < maxItems; i++) {
                Element repo = repos.get(i);
                Element linkEl = repo.selectFirst("h2 a");
                if (linkEl == null) continue;

                String[] parts = linkEl.text().replaceAll("\\s+", " ").trim().split(" / ");
                String repoName = parts.length > 1 ? parts[1].trim() : parts[0].trim();

                Element descEl = repo.selectFirst("p");
                String description = descEl != null ? descEl.text().trim() : "";

                Element langEl = repo.selectFirst("[itemprop=programmingLanguage]");
                String language = langEl != null ? langEl.text().trim() : "";

                NewsItem item = NewsItem.builder()
                        .title("GitHub Trending: " + repoName)
                        .summary(description + (language.isEmpty() ? "" : " [" + language + "]"))
                        .sourceUrl("https://github.com" + linkEl.attr("href"))
                        .sourcePlatform(source.getPlatformName()).sourceName(source.getName())
                        .category(source.getCategory()).language("EN")
                        .publishedAt(LocalDateTime.now()).fetchedAt(LocalDateTime.now())
                        .qualityScore(55).isFiltered(false).build();
                items.add(item);
            }
        } catch (Exception e) {
            log.warn("GitHub Trending fetch failed: {}", e.getMessage());
        }
        return items;
    }
}
