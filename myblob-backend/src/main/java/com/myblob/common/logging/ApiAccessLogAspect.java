package com.myblob.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * API 访问日志切面
 * 自动记录所有 Controller 的请求/响应信息
 */
@Slf4j
@Aspect
@Component
public class ApiAccessLogAspect {

    private static final long SLOW_REQUEST_THRESHOLD_MS = 1000;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {
    }

    @Around("controllerMethods()")
    public Object logApiAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String method = "UNKNOWN";
        String path = "UNKNOWN";
        String ip = "unknown";
        String user = "anonymous";

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            method = request.getMethod();
            path = request.getRequestURI();
            ip = getClientIp(request);
            String userId = MDC.get("userId");
            if (userId != null) {
                user = userId;
            }
        }

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            if (duration > SLOW_REQUEST_THRESHOLD_MS) {
                log.warn("[API] {} {} | {}.{} | user={} | ip={} | duration={}ms (SLOW)",
                        method, path, className, methodName, user, ip, duration);
            } else {
                log.info("[API] {} {} | {}.{} | user={} | ip={} | duration={}ms",
                        method, path, className, methodName, user, ip, duration);
            }

            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[API] {} {} | {}.{} | user={} | ip={} | duration={}ms | error={}",
                    method, path, className, methodName, user, ip, duration, ex.getMessage());
            throw ex;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
