package com.myblob.module.core.controller;

import com.myblob.common.ApiResponse;
import com.myblob.module.core.service.CoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class CoreController {

    private final CoreService coreService;

    @GetMapping("/announcements/")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAnnouncements() {
        return ResponseEntity.ok(ApiResponse.success(coreService.getActiveAnnouncements()));
    }

    @GetMapping("/ads/")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAds(
            @RequestParam(required = false) String position) {
        return ResponseEntity.ok(ApiResponse.success(coreService.getActiveAds(position)));
    }

    @PostMapping("/ads/{id}/click/")
    public ResponseEntity<ApiResponse<Void>> recordAdClick(@PathVariable Long id) {
        coreService.recordAdClick(id);
        return ResponseEntity.ok(ApiResponse.success("点击记录成功", null));
    }

    @GetMapping("/site-config/")
    public ResponseEntity<ApiResponse<Map<String, String>>> getSiteConfig() {
        return ResponseEntity.ok(ApiResponse.success(coreService.getSiteConfig()));
    }
}
