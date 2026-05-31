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
public class NewsItemDTO {
    private Long id;
    private String title;
    private String summary;
    private String translatedTitle;
    private String translatedSummary;
    private String content;
    private String sourceUrl;
    private String sourcePlatform;
    private String sourceName;
    private String category;
    private String language;
    private String thumbnailUrl;
    private LocalDateTime publishedAt;
    private Integer qualityScore;
    private String filterReason;
    private Boolean isFiltered;
    private LocalDateTime fetchedAt;
    private LocalDateTime createdAt;

    public static NewsItemDTO from(com.myblob.module.news.entity.NewsItem item) {
        return NewsItemDTO.builder()
                .id(item.getId())
                .title(item.getTitle())
                .summary(item.getSummary())
                .translatedTitle(item.getTranslatedTitle())
                .translatedSummary(item.getTranslatedSummary())
                .content(item.getContent())
                .sourceUrl(item.getSourceUrl())
                .sourcePlatform(item.getSourcePlatform())
                .sourceName(item.getSourceName())
                .category(item.getCategory())
                .language(item.getLanguage())
                .thumbnailUrl(item.getThumbnailUrl())
                .publishedAt(item.getPublishedAt())
                .qualityScore(item.getQualityScore())
                .filterReason(item.getFilterReason())
                .isFiltered(item.getIsFiltered())
                .fetchedAt(item.getFetchedAt())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
