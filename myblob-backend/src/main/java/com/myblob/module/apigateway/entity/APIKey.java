package com.myblob.module.apigateway.entity;

import com.myblob.common.BaseEntity;
import com.myblob.module.accounts.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_key", indexes = {
        @Index(name = "idx_apikey_user", columnList = "user_id"),
        @Index(name = "idx_apikey_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class APIKey extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "api_key", unique = true, nullable = false, length = 200)
    private String apiKey;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "daily_limit", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("10000")
    private Integer dailyLimit = 10000;

    @Column(name = "used_count", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("0")
    private Integer usedCount = 0;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("true")
    private Boolean active = true;

    @Column(name = "expired_time")
    private LocalDateTime expiredTime;
}
