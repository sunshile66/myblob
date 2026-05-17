package com.myblob.module.security.entity;

import com.myblob.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "security_verification_log", indexes = {
        @Index(name = "idx_vlog_type_target", columnList = "type, target"),
        @Index(name = "idx_vlog_status", columnList = "status"),
        @Index(name = "idx_vlog_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class VerificationLog extends BaseEntity {

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 100)
    private String target;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false, length = 10)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("'pending'")
    private String status = "pending";

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "send_status", length = 20)
    private String sendStatus;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
