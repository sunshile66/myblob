package com.myblob.module.social.entity;

import com.myblob.common.BaseEntity;
import com.myblob.module.accounts.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "social_login_log", indexes = {
        @Index(name = "idx_sll_user", columnList = "user_id"),
        @Index(name = "idx_sll_provider", columnList = "provider"),
        @Index(name = "idx_sll_status", columnList = "status"),
        @Index(name = "idx_sll_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SocialLoginLog extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SocialAccount.Provider provider;

    @Column(length = 100)
    private String openid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 10)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("'success'")
    private String status = "success";

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
