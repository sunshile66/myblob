package com.myblob.module.news.entity;

import com.myblob.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "news_item", indexes = {
        @Index(name = "idx_news_source_url", columnList = "source_url", unique = true),
        @Index(name = "idx_news_published_at", columnList = "published_at"),
        @Index(name = "idx_news_category", columnList = "category"),
        @Index(name = "idx_news_item_source_platform", columnList = "source_platform"),
        @Index(name = "idx_news_quality_score", columnList = "quality_score"),
        @Index(name = "idx_news_language", columnList = "language"),
        @Index(name = "idx_news_is_filtered", columnList = "is_filtered")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NewsItem extends BaseEntity {

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 1000)
    private String summary;

    @Column(name = "translated_title", length = 500)
    private String translatedTitle;

    @Column(name = "translated_summary", length = 1000)
    private String translatedSummary;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "source_url", nullable = false, length = 1000, unique = true)
    private String sourceUrl;

    @Column(name = "source_platform", nullable = false, length = 50)
    private String sourcePlatform;

    @Column(name = "source_name", nullable = false, length = 100)
    private String sourceName;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(length = 10)
    @Builder.Default
    private String language = "CN";

    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "quality_score", nullable = false)
    @Builder.Default
    private Integer qualityScore = 50;

    @Column(name = "filter_reason", length = 500)
    private String filterReason;

    @Column(name = "is_filtered", nullable = false)
    @Builder.Default
    private Boolean isFiltered = false;

    @Column(name = "fetched_at")
    private LocalDateTime fetchedAt;

    @PrePersist
    public void prePersist() {
        if (this.fetchedAt == null) {
            this.fetchedAt = LocalDateTime.now();
        }
    }
}
