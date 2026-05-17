package com.myblob.module.membership.entity;

import com.myblob.common.BaseEntity;
import com.myblob.module.accounts.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_membership", indexes = {
        @Index(name = "idx_membership_user", columnList = "user_id"),
        @Index(name = "idx_membership_plan", columnList = "plan_id"),
        @Index(name = "idx_membership_active", columnList = "is_active, end_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserMembership extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private MembershipPlan plan;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("false")
    private Boolean active = false;

    @Column(name = "is_lifetime", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("false")
    private Boolean lifetime = false;

    public boolean isValid() {
        if (!active) return false;
        if (lifetime) return true;
        return endTime != null && LocalDateTime.now().isBefore(endTime);
    }
}
