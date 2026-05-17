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
public class WalletTransactionDTO {

    private Long id;
    private Long walletId;
    private String transactionNo;

    @JsonProperty("transaction_type")
    private String transactionType;

    @JsonProperty("transaction_type_display")
    private String transactionTypeDisplay;

    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String description;
    private Long relatedOrderId;
    private LocalDateTime createdAt;
}
