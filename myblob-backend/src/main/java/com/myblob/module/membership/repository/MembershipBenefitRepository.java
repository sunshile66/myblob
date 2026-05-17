package com.myblob.module.membership.repository;

import com.myblob.module.membership.entity.MembershipBenefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembershipBenefitRepository extends JpaRepository<MembershipBenefit, Long> {

    List<MembershipBenefit> findByActiveTrue();
}
