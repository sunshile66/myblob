package com.myblob.module.membership.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MembershipPlanDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationDays;
    private List<String> features;
    private Boolean active;
    private Integer sort;
    private Boolean popular;
    private String badgeText;
    private List<PlanBenefitRelationDTO> benefitRelations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanBenefitRelationDTO {
        private Long id;
        private MembershipBenefitDTO benefit;
        private String value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembershipBenefitDTO {
        private Long id;
        private String name;
        private String code;
        private String benefitType;
        private String description;
        private String icon;
        private Boolean active;
    }
}
