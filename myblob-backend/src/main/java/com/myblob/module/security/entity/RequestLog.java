package com.myblob.module.security.entity;

import com.myblob.common.BaseEntity;
import com.myblob.module.accounts.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "security_request_log", indexes = {
        @Index(name = "idx_rlog_user", columnList = "user_id"),
        @Index(name = "idx_rlog_ip", columnList = "ip_address"),
        @Index(name = "idx_rlog_blocked", columnList = "blocked"),
        @Index(name = "idx_rlog_path", columnList = "path"),
        @Index(name = "idx_rlog_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RequestLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(nullable = false, length = 10)
    private String method;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String path;

    @Column(name = "status_code", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("200")
    private Integer statusCode = 200;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "response_time")
    private Long responseTime;

    @Column(nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("false")
    private Boolean blocked = false;

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;
}
