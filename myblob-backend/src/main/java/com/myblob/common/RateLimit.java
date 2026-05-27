package com.myblob.common;

import java.lang.annotation.*;

/**
 * API 请求频率限制注解。
 * 用于登录、注册、验证码发送等敏感接口。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /** 时间窗口内允许的最大请求次数 */
    int maxRequests() default 10;

    /** 时间窗口，单位：秒 */
    int windowSeconds() default 60;

    /** 限流提示消息 */
    String message() default "请求过于频繁，请稍后再试";
}
