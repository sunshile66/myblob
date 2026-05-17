package com.myblob.module.membership.entity;

import com.myblob.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "plan_benefit_relation", indexes = {
        @Index(name = "idx_pbr_plan", columnList = "plan_id"),
        @Index(name = "idx_pbr_benefit", columnList = "benefit_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uniq_plan_benefit", columnNames = {"plan_id", "benefit_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PlanBenefitRelation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benefit_id", nullable = false)
    private MembershipBenefit benefit;

    @Column(length = 200)
    private String value;
}
