package com.myblob.module.news.service;

import com.myblob.module.news.entity.NewsItem;
import com.myblob.module.news.entity.NewsSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * API 抓取策略（HackerNews、Reddit）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiFetchStrategy implements NewsFetchStrategy {

    private final RestTemplate restTemplate;
    private final NewsProxyConfig newsProxyConfig;

    private static final String HN_TOP_STORIES = "https://hacker-news.firebaseio.com/v0/topstories.json";
    private static final String HN_ITEM = "https://hacker-news.firebaseio.com/v0/item/%d.json";
    private static final String REDDIT_PROGRAMMING = "https://www.reddit.com/r/programming/hot.json?limit=30";
    private static final String REDDIT_TECH = "https://www.reddit.com/r/technology/hot.json?limit=30";

    @Override
    public boolean supports(String method) {
        return "API".equals(method);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<NewsItem> fetch(NewsSource source) {
        String platform = source.getPlatformName().toLowerCase();
        return switch (platform) {
            case "hackernews" -> fetchHackerNews(source);
            case "reddit" -> fetchReddit(source);
            default -> new ArrayList<>();
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
}
