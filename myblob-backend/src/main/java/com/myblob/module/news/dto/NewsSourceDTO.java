package com.myblob.module.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSourceDTO {
    private Long id;
    private String name;
    private String platformName;
    private String feedUrl;
    private String fetchMethod;
    private String category;
    private String language;
    private Boolean enabled;
    private Integer priority;
    private Integer fetchIntervalSeconds;
    private LocalDateTime lastFetchedAt;
    private Integer errorCount;
    private Integer consecutiveErrors;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NewsSourceDTO from(com.myblob.module.news.entity.NewsSource source) {
        return NewsSourceDTO.builder()
                .id(source.getId())
                .name(source.getName())
                .platformName(source.getPlatformName())
                .feedUrl(source.getFeedUrl())
                .fetchMethod(source.getFetchMethod())
                .category(source.getCategory())
                .language(source.getLanguage())
                .enabled(source.getEnabled())
                .priority(source.getPriority())
                .fetchIntervalSeconds(source.getFetchIntervalSeconds())
                .lastFetchedAt(source.getLastFetchedAt())
                .errorCount(source.getErrorCount())
                .consecutiveErrors(source.getConsecutiveErrors())
                .createdAt(source.getCreatedAt())
                .updatedAt(source.getUpdatedAt())
                .build();
    }
}
