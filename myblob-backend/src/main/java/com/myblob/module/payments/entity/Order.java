package com.myblob.module.payments.entity;

import com.myblob.common.BaseEntity;
import com.myblob.module.accounts.entity.User;
import com.myblob.module.membership.entity.MembershipPlan;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_order", indexes = {
        @Index(name = "idx_order_user", columnList = "user_id"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Order extends BaseEntity {

    public enum OrderStatus {
        PENDING, PAID, CANCELLED, REFUNDED, EXPIRED
    }

    public enum PaymentMethod {
        ALIPAY, WECHAT, BALANCE
    }

    @Column(name = "order_no", unique = true, nullable = false, length = 32)
    private String orderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private MembershipPlan plan;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "paid_time")
    private LocalDateTime paidTime;

    @Column(name = "expired_time")
    private LocalDateTime expiredTime;

    @Column(columnDefinition = "TEXT")
    private String remark;

    public boolean isPaid() {
        return status == OrderStatus.PAID;
    }

    public boolean isExpired() {
        return status == OrderStatus.PENDING && expiredTime != null && LocalDateTime.now().isAfter(expiredTime);
    }
}
