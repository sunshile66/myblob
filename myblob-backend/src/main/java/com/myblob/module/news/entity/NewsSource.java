package com.myblob.module.news.entity;

import com.myblob.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "news_source", indexes = {
        @Index(name = "idx_news_source_platform", columnList = "platform_name"),
        @Index(name = "idx_news_source_enabled", columnList = "enabled"),
        @Index(name = "idx_news_source_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NewsSource extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "platform_name", nullable = false, length = 50)
    private String platformName;

    @Column(name = "feed_url", nullable = false, length = 1000)
    private String feedUrl;

    @Column(name = "fetch_method", nullable = false, length = 20)
    @Builder.Default
    private String fetchMethod = "RSS";

    @Column(nullable = false, length = 50)
    private String category;

    @Column(length = 10)
    @Builder.Default
    private String language = "CN";

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 5;

    @Column(name = "fetch_interval_seconds")
    @Builder.Default
    private Integer fetchIntervalSeconds = 1800;

    @Column(name = "last_fetched_at")
    private LocalDateTime lastFetchedAt;

    @Column(name = "error_count")
    @Builder.Default
    private Integer errorCount = 0;

    @Column(name = "consecutive_errors")
    @Builder.Default
    private Integer consecutiveErrors = 0;
}
