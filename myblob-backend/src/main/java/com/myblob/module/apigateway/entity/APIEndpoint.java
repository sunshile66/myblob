package com.myblob.module.apigateway.entity;

import com.myblob.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "api_endpoint", indexes = {
        @Index(name = "idx_endpoint_service", columnList = "service_id"),
        @Index(name = "idx_endpoint_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class APIEndpoint extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private APIService service;

    @Column(nullable = false, length = 200)
    private String path;

    @Column(nullable = false, length = 10)
    private String method;

    @Column(length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("false")
    private Boolean isPublic = false;

    @Column(name = "rate_limit", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("100")
    private Integer rateLimit = 100;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("true")
    private Boolean active = true;
}
