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
public class OrderDTO {

    private Long id;
    private String orderNo;
    private Long userId;
    private Long planId;
    private BigDecimal amount;
    private String status;

    @JsonProperty("status_display")
    private String statusDisplay;

    private String paymentMethod;

    @JsonProperty("payment_method_display")
    private String paymentMethodDisplay;

    private LocalDateTime paidTime;
    private LocalDateTime expiredTime;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
