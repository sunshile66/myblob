package com.myblob.module.social.controller;

import com.myblob.common.ApiResponse;
import com.myblob.module.social.service.SocialService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialController {

    private final SocialService socialService;

    @GetMapping("/oauth-apps/")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getOAuthApps() {
        return ResponseEntity.ok(ApiResponse.success(socialService.getActiveOAuthApps()));
    }

    @GetMapping("/accounts/my-accounts/")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMyAccounts() {
        return ResponseEntity.ok(ApiResponse.success(socialService.getMySocialAccounts()));
    }

    @PostMapping("/oauth/callback/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> oauthCallback(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        String provider = body.get("provider");
        String code = body.get("code");
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        return ResponseEntity.ok(ApiResponse.success(socialService.oauthCallback(provider, code, ipAddress, userAgent)));
    }

    @PostMapping("/oauth/login/")
    public ResponseEntity<ApiResponse<Map<String, String>>> oauthLogin(@RequestBody Map<String, String> body) {
        String provider = body.get("provider");
        Map<String, String> authUrls = Map.of(
                "github", "https://github.com/login/oauth/authorize",
                "google", "https://accounts.google.com/o/oauth2/auth"
        );
        String url = authUrls.getOrDefault(provider, "");
        return ResponseEntity.ok(ApiResponse.success(Map.of("auth_url", url, "provider", provider)));
    }
}
