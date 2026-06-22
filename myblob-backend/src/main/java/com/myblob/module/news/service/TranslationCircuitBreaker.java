package com.myblob.module.news.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 翻译熔断器
 * 连续失败达到阈值后，暂停翻译 30 分钟
 */
@Slf4j
@Component
public class TranslationCircuitBreaker {

    private static final int FAIL_THRESHOLD = 5;
    private static final long COOLDOWN_MS = 30 * 60 * 1000L; // 30 min

    private volatile int failCount = 0;
    private volatile long cooldownUntil = 0;

    /**
     * 检查是否允许翻译
     */
    public boolean isAllowed() {
        if (System.currentTimeMillis() < cooldownUntil) {
            log.debug("Translation circuit breaker: cooldown active ({} failures)", failCount);
            return false;
        }
        return true;
    }

    /**
     * 记录翻译失败
     */
    public void recordFailure() {
        failCount++;
        if (failCount >= FAIL_THRESHOLD) {
            cooldownUntil = System.currentTimeMillis() + COOLDOWN_MS;
            log.warn("Translation circuit breaker OPEN ({} fails), cooldown 30min", failCount);
        }
    }

    /**
     * 记录翻译成功，重置失败计数
     */
    public void recordSuccess() {
        failCount = 0;
    }

    /**
     * 重置熔断器
     */
    public void reset() {
        failCount = 0;
        cooldownUntil = 0;
    }
}
