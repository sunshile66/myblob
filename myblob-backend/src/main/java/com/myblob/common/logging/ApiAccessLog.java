package com.myblob.common.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 访问日志注解
 * 标记在 Controller 方法上，自动记录请求/响应信息
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiAccessLog {
    /**
     * 操作描述
     */
    String value() default "";
}
