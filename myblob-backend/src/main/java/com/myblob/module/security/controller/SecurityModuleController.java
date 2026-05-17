package com.myblob.module.security.controller;

import com.myblob.common.ApiResponse;
import com.myblob.common.PageResponse;
import com.myblob.module.security.service.SecurityModuleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
public class SecurityModuleController {

    private final SecurityModuleService securityModuleService;

    @GetMapping("/ip-blocks/")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> getBlockedIPs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(securityModuleService.getBlockedIPs(page, size)));
    }

    @GetMapping("/request-logs/")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> getRequestLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(securityModuleService.getRequestLogs(page, size)));
    }

    @GetMapping("/sessions/")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMySessions() {
        return ResponseEntity.ok(ApiResponse.success(securityModuleService.getMySessions()));
    }

    @PostMapping("/sessions/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createSession(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                securityModuleService.createSession(request.getRemoteAddr(), request.getHeader("User-Agent"))));
    }

    @DeleteMapping("/sessions/{id}/")
    public ResponseEntity<ApiResponse<Void>> deactivateSession(@PathVariable Long id) {
        securityModuleService.deactivateSession(id);
        return ResponseEntity.ok(ApiResponse.success("会话已终止", null));
    }
}
