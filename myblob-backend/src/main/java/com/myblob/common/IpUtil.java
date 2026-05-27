package com.myblob.common;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP 工具类 —— 统一从 HttpServletRequest 提取客户端真实 IP。
 * 消除 BlogService 和 RateLimitInterceptor 中的重复代码。
 */
public final class IpUtil {

    private IpUtil() {
    }

    /**
     * 从请求中提取客户端真实 IP。
     * 优先级：X-Forwarded-For > X-Real-IP > RemoteAddr
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            if (ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
