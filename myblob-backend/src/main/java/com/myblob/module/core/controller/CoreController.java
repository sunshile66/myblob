package com.myblob.module.core.controller;

import com.myblob.common.ApiResponse;
import com.myblob.module.core.service.CoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
@Tag(name = "核心服务", description = "公告、广告、站点配置相关接口")
public class CoreController {

    private final CoreService coreService;

    @GetMapping("/announcements/")
    @Operation(summary = "获取公告列表")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAnnouncements() {
        return ResponseEntity.ok(ApiResponse.success(coreService.getActiveAnnouncements()));
    }

    @GetMapping("/ads/")
    @Operation(summary = "获取广告列表")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAds(
            @RequestParam(required = false) String position) {
        return ResponseEntity.ok(ApiResponse.success(coreService.getActiveAds(position)));
    }

    @PostMapping("/ads/{id}/click/")
    @Operation(summary = "记录广告点击")
    public ResponseEntity<ApiResponse<Void>> recordAdClick(@PathVariable Long id) {
        coreService.recordAdClick(id);
        return ResponseEntity.ok(ApiResponse.success("点击记录成功", null));
    }

    @GetMapping("/site-config/")
    @Operation(summary = "获取站点配置")
    public ResponseEntity<ApiResponse<Map<String, String>>> getSiteConfig() {
        return ResponseEntity.ok(ApiResponse.success(coreService.getSiteConfig()));
    }
}
