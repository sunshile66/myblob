package com.myblob.module.membership.service;

import com.myblob.common.BusinessException;
import com.myblob.module.membership.dto.MembershipPlanDTO;
import com.myblob.module.membership.dto.UserMembershipDTO;
import com.myblob.module.membership.entity.MembershipPlan;
import com.myblob.module.membership.entity.UserMembership;
import com.myblob.module.membership.repository.MembershipPlanRepository;
import com.myblob.module.membership.repository.PlanBenefitRelationRepository;
import com.myblob.module.membership.repository.UserMembershipRepository;
import com.myblob.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipPlanRepository planRepository;
    private final UserMembershipRepository userMembershipRepository;
    private final PlanBenefitRelationRepository planBenefitRelationRepository;

    @Transactional(readOnly = true)
    public List<MembershipPlanDTO> getActivePlans() {
        return planRepository.findByActiveTrueOrderBySortAscCreatedAtDesc().stream()
                .map(this::toPlanDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserMembershipDTO getMyMembership() {
        Long userId = SecurityUtil.getCurrentUserId();
        UserMembership membership = userMembershipRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserMembership m = new UserMembership();
                    m.setUser(com.myblob.module.accounts.entity.User.builder().id(userId).build());
                    return m;
                });
        return toMembershipDTO(membership);
    }

    private MembershipPlanDTO toPlanDTO(MembershipPlan plan) {
        MembershipPlanDTO dto = MembershipPlanDTO.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .durationDays(plan.getDurationDays())
                .active(plan.getActive())
                .sort(plan.getSort())
                .popular(plan.getPopular())
                .badgeText(plan.getBadgeText())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();

        List<MembershipPlanDTO.PlanBenefitRelationDTO> benefitDtos = planBenefitRelationRepository.findByPlanId(plan.getId())
                .stream()
                .map(r -> MembershipPlanDTO.PlanBenefitRelationDTO.builder()
                        .id(r.getId())
                        .benefit(MembershipPlanDTO.MembershipBenefitDTO.builder()
                                .id(r.getBenefit().getId())
                                .name(r.getBenefit().getName())
                                .code(r.getBenefit().getCode())
                                .benefitType(r.getBenefit().getBenefitType())
                                .description(r.getBenefit().getDescription())
                                .icon(r.getBenefit().getIcon())
                                .active(r.getBenefit().getActive())
                                .build())
                        .value(r.getValue())
                        .build())
                .toList();
        dto.setBenefitRelations(benefitDtos);

        return dto;
    }

    private UserMembershipDTO toMembershipDTO(UserMembership membership) {
        UserMembershipDTO dto = UserMembershipDTO.builder()
                .id(membership.getId())
                .userId(membership.getUser().getId())
                .startTime(membership.getStartTime())
                .endTime(membership.getEndTime())
                .active(membership.getActive())
                .lifetime(membership.getLifetime())
                .valid(membership.isValid())
                .createdAt(membership.getCreatedAt())
                .updatedAt(membership.getUpdatedAt())
                .build();

        if (membership.getPlan() != null) {
            dto.setPlan(toPlanDTO(membership.getPlan()));
        }

        if (membership.isValid() && membership.getEndTime() != null) {
            dto.setRemainingDays((int) ChronoUnit.DAYS.between(java.time.LocalDateTime.now(), membership.getEndTime()));
        } else {
            dto.setRemainingDays(0);
        }

        return dto;
    }
}
