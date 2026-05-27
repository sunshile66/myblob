package com.myblob.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于内存的请求频率限制拦截器。
 * 按 IP + 接口路径进行限流，适用于单机部署场景。
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return true;
        }

        String clientIp = IpUtil.getClientIp(request);
        String path = request.getRequestURI();
        String key = clientIp + ":" + path;

        TokenBucket bucket = buckets.computeIfAbsent(key,
                k -> new TokenBucket(rateLimit.maxRequests(), rateLimit.windowSeconds()));

        if (!bucket.tryConsume()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                    ApiResponse.error(rateLimit.message())));
            return false;
        }

        return true;
    }

    /**
     * 简单 Token Bucket 实现
     */
    private static class TokenBucket {
        private final int maxTokens;
        private final long windowMillis;
        private volatile long lastRefill;
        private volatile int tokens;

        TokenBucket(int maxTokens, int windowSeconds) {
            this.maxTokens = maxTokens;
            this.windowMillis = windowSeconds * 1000L;
            this.tokens = maxTokens;
            this.lastRefill = System.currentTimeMillis();
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefill;
            if (elapsed >= windowMillis) {
                tokens = maxTokens;
                lastRefill = now;
            }
        }
    }
}
