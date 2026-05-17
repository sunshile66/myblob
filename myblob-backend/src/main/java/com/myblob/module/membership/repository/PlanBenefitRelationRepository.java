package com.myblob.module.membership.repository;

import com.myblob.module.membership.entity.PlanBenefitRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanBenefitRelationRepository extends JpaRepository<PlanBenefitRelation, Long> {

    List<PlanBenefitRelation> findByPlanId(Long planId);
}
