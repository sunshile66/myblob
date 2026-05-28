package com.myblob.config;

import com.myblob.common.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${myblob.upload.path:./media}")
    private String uploadPath;

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/media/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
