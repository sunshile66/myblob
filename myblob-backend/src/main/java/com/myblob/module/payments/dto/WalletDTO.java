package com.myblob.module.payments.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WalletDTO {

    private Long id;
    private Long userId;
    private BigDecimal balance;
    private BigDecimal frozenBalance;

    @JsonProperty("available_balance")
    private BigDecimal availableBalance;

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
