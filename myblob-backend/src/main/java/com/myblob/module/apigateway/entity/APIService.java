package com.myblob.module.apigateway.entity;

import com.myblob.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "api_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class APIService extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 100)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(name = "base_url", length = 500)
    private String baseUrl;

    @Column(nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("30000")
    private Integer timeout = 30000;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("true")
    private Boolean active = true;
}
