package com.myblob.module.social.entity;

import com.myblob.common.BaseEntity;
import com.myblob.module.accounts.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_account", indexes = {
        @Index(name = "idx_social_user", columnList = "user_id"),
        @Index(name = "idx_social_provider", columnList = "provider")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uniq_provider_openid", columnNames = {"provider", "openid"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SocialAccount extends BaseEntity {

    public enum Provider {
        WECHAT, QQ, WEIBO, GITHUB, GOOGLE, FACEBOOK, TWITTER
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(nullable = false, length = 100)
    private String openid;

    @Column(length = 100)
    private String unionid;

    @Column(length = 100)
    private String nickname;

    @Column(length = 500)
    private String avatar;

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "extra_data", columnDefinition = "TEXT")
    private String extraData;
}
