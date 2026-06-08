package com.myblob.module.blog.service;

import com.myblob.module.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 浏览量去重服务。
 * 同一 IP 对同一文章在 30 分钟内重复访问只计 1 次浏览。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ViewCountService {

    private final PostRepository postRepository;

    /**
     * 记录结构：key = "ip:postId", value = 上次记录时间戳(ms)
     */
    private final ConcurrentHashMap<String, Long> viewCache = new ConcurrentHashMap<>();

    /** 去重窗口：30 分钟 */
    private static final long DEDUP_WINDOW_MS = TimeUnit.MINUTES.toMillis(30);

    /**
     * 记录一次文章浏览，自动去重。
     * 
     * @param postId   文章ID
     * @param clientIp 客户端IP
     * @return true=新增浏览，false=窗口内重复
     */
    public boolean recordView(Long postId, String clientIp) {
        String key = clientIp + ":" + postId;
        long now = System.currentTimeMillis();

        Long lastView = viewCache.putIfAbsent(key, now);
        if (lastView == null) {
            // 首次访问，计入浏览
            postRepository.incrementViewCount(postId);
            return true;
        }

        // 窗口内重复，不计入
        if (now - lastView < DEDUP_WINDOW_MS) {
            return false;
        }

        // 窗口已过期，更新时间戳并计入
        viewCache.put(key, now);
        postRepository.incrementViewCount(postId);
        return true;
    }

    /**
     * 清理过期缓存记录（每 10 分钟执行一次）
     */
    @Scheduled(fixedRate = 600_000) // 10 分钟
    public void cleanExpiredEntries() {
        int beforeSize = viewCache.size();
        long now = System.currentTimeMillis();
        viewCache.entrySet().removeIf(entry -> now - entry.getValue() > DEDUP_WINDOW_MS * 2);
        int afterSize = viewCache.size();
        if (beforeSize > afterSize) {
            log.debug("Cleaned {} expired view count entries", beforeSize - afterSize);
        }
    }
}
