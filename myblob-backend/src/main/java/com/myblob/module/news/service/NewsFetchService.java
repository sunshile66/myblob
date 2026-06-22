package com.myblob.module.news.service;

import com.myblob.module.news.entity.NewsItem;
import com.myblob.module.news.entity.NewsSource;
import com.myblob.module.news.repository.NewsSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 新闻抓取协调器
 * 负责遍历新闻源，委托给不同的抓取策略执行
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsFetchService {

    private final NewsSourceRepository newsSourceRepository;
    private final NewsFilterService newsFilterService;
    private final NewsProxyConfig newsProxyConfig;
    private final NewsTranslationService translationService;
    private final NewsTopicService topicService;
    private final NewsSentimentService sentimentService;
    private final TranslationCircuitBreaker circuitBreaker;
    private final NewsItemSaver newsItemSaver;
    private final List<NewsFetchStrategy> strategies;

    private static final int MAX_ARTICLE_AGE_DAYS = 30;
    private static final java.util.Set<String> SKIP_AGE_FILTER_CATEGORIES = java.util.Set.of("官方媒体");

    /**
     * 抓取所有启用的新闻源
     */
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

    /**
     * 异步抓取单个新闻源
     */
    @Async("newsFetchExecutor")
    public CompletableFuture<Integer> fetchSourceAsync(NewsSource source) {
        return CompletableFuture.completedFuture(fetchSource(source));
    }

    /**
     * 抓取单个新闻源
     */
    @Transactional
    public int fetchSource(NewsSource source) {
        try {
            String method = source.getFetchMethod() != null ? source.getFetchMethod().toUpperCase() : "RSS";

            // 委托给对应的策略执行
            List<NewsItem> items = strategies.stream()
                    .filter(s -> s.supports(method))
                    .findFirst()
                    .orElse(strategies.get(0)) // 默认使用第一个策略
                    .fetch(source);

            if (items.isEmpty()) {
                source.setConsecutiveErrors(0);
                newsSourceRepository.save(source);
                return 0;
            }

            // 过滤过期文章
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
            }

            // 应用过滤和评分
            List<NewsItem> filtered = newsFilterService.filterAndScore(items, source);

            // 话题提取 + 情感分析
            topicService.extractTopics(filtered);
            sentimentService.analyzeSentiment(filtered);

            // 翻译英文条目（带熔断保护）
            boolean isAirlineCategory = "国际航司".equals(source.getCategory());
            if (isAirlineCategory || newsProxyConfig.getFetch().isTranslateEnabled()) {
                if (circuitBreaker.isAllowed()) {
                    translateEnglishItems(filtered);
                }
            }

            // 保存新条目
            int saved = newsItemSaver.saveNewItems(filtered, source);

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

    /**
     * 翻译英文条目
     */
    private void translateEnglishItems(List<NewsItem> items) {
        for (NewsItem item : items) {
            if (!"EN".equals(item.getLanguage())) continue;

            try {
                String tTitle = translationService.translate(item.getTitle());
                if (tTitle == null) {
                    circuitBreaker.recordFailure();
                    continue;
                }
                if (!tTitle.equals(item.getTitle())) {
                    item.setTranslatedTitle(tTitle);
                }

                String tSummary = translationService.translate(item.getSummary());
                if (tSummary == null) {
                    circuitBreaker.recordFailure();
                    continue;
                }
                if (!tSummary.equals(item.getSummary())) {
                    item.setTranslatedSummary(tSummary);
                }

                circuitBreaker.recordSuccess();
            } catch (Exception e) {
                circuitBreaker.recordFailure();
                log.debug("Translate exception for [{}]: {}", item.getTitle(), e.getMessage());
            }
        }
    }
}
