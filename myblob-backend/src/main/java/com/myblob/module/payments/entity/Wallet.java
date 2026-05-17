package com.myblob.module.payments.entity;

import com.myblob.common.BaseEntity;
import com.myblob.module.accounts.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "user_wallet", indexes = {
        @Index(name = "idx_wallet_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Wallet extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("0.00")
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "frozen_balance", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("0.00")
    private BigDecimal frozenBalance = BigDecimal.ZERO;

    @Column(name = "total_income", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("0.00")
    private BigDecimal totalIncome = BigDecimal.ZERO;

    @Column(name = "total_expense", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("0.00")
    private BigDecimal totalExpense = BigDecimal.ZERO;

    public BigDecimal getAvailableBalance() {
        return balance.subtract(frozenBalance);
    }
}
