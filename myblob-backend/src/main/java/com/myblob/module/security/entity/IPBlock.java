package com.myblob.module.security.entity;

import com.myblob.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_ip_block", indexes = {
        @Index(name = "idx_ipblock_ip", columnList = "ip"),
        @Index(name = "idx_ipblock_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class IPBlock extends BaseEntity {

    @Column(nullable = false, length = 45)
    private String ip;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("true")
    private Boolean active = true;
}
