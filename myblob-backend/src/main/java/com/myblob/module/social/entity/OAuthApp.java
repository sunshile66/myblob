package com.myblob.module.social.entity;

import com.myblob.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "oauth_app")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OAuthApp extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false, length = 20)
    private SocialAccount.Provider provider;

    @Column(name = "app_id", nullable = false, length = 200)
    private String appId;

    @Column(name = "app_secret", nullable = false, length = 200)
    private String appSecret;

    @Column(name = "redirect_uri", length = 500)
    private String redirectUri;

    @Column(length = 200)
    private String scope;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("true")
    private Boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String config;
}
