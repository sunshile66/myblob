package com.myblob.module.membership.controller;

import com.myblob.common.ApiResponse;
import com.myblob.module.membership.dto.MembershipPlanDTO;
import com.myblob.module.membership.dto.UserMembershipDTO;
import com.myblob.module.membership.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membership")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping("/plans/")
    public ResponseEntity<ApiResponse<List<MembershipPlanDTO>>> getPlans() {
        return ResponseEntity.ok(ApiResponse.success(membershipService.getActivePlans()));
    }

    @GetMapping("/my-membership/")
    public ResponseEntity<ApiResponse<UserMembershipDTO>> getMyMembership() {
        return ResponseEntity.ok(ApiResponse.success(membershipService.getMyMembership()));
    }
}
