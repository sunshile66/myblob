package com.myblob.module.membership.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserMembershipDTO {

    private Long id;
    private Long userId;
    private MembershipPlanDTO plan;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean active;
    private Boolean lifetime;

    @JsonProperty("is_valid")
    private Boolean valid;

    private Integer remainingDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
