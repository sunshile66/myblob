package com.myblob.module.news.service;

import com.myblob.module.news.repository.NewsItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsSchedulerService {

    private final NewsFetchService newsFetchService;
    private final NewsItemRepository newsItemRepository;
    private final NewsProxyConfig newsProxyConfig;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicInteger lastFetchedCount = new AtomicInteger(0);
    private final AtomicLong lastFetchDurationMs = new AtomicLong(0);
    private final AtomicLong lastFetchTime = new AtomicLong(0);

    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void scheduledFetch() {
        if (!newsProxyConfig.getGlobal().isEnabled()) {
            log.debug("News fetch skipped: global switch is OFF");
            return;
        }

        if (isRunning.get()) {
            log.debug("News fetch skipped: previous fetch still running");
            return;
        }

        try {
            isRunning.set(true);
            long start = System.currentTimeMillis();
            log.info("=== Scheduled news fetch START ===");

            int count = newsFetchService.fetchAllSources();

            long duration = System.currentTimeMillis() - start;
            lastFetchedCount.set(count);
            lastFetchDurationMs.set(duration);
            lastFetchTime.set(System.currentTimeMillis());

            log.info("=== Scheduled news fetch END: {} items in {}ms ===", count, duration);
        } catch (Exception e) {
            log.error("Scheduled news fetch failed: {}", e.getMessage(), e);
        } finally {
            isRunning.set(false);
        }
    }

    @Scheduled(cron = "0 0 3 * * ?") // Daily at 3 AM
    public void cleanupOldItems() {
        if (!newsProxyConfig.getGlobal().isEnabled()) {
            return;
        }
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
            newsItemRepository.deleteByFetchedAtBefore(cutoff);
            log.info("Cleaned up news items older than 30 days");
        } catch (Exception e) {
            log.error("News cleanup failed: {}", e.getMessage(), e);
        }
    }

    public boolean isFetchRunning() {
        return isRunning.get();
    }

    public int getLastFetchedCount() {
        return lastFetchedCount.get();
    }

    public long getLastFetchDurationMs() {
        return lastFetchDurationMs.get();
    }

    public long getLastFetchTime() {
        return lastFetchTime.get();
    }
}
