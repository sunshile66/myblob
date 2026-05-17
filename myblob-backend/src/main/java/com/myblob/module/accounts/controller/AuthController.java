package com.myblob.module.accounts.controller;

import com.myblob.common.ApiResponse;
import com.myblob.common.service.VerificationCodeService;
import com.myblob.module.accounts.dto.*;
import com.myblob.module.accounts.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final VerificationCodeService verificationCodeService;

    @PostMapping("/register/")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        if (request.getCode() == null || request.getCode().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请输入验证码"));
        }
        
        boolean codeValid = verificationCodeService.verifyCode(request.getEmail(), request.getCode());
        if (!codeValid) {
            return ResponseEntity.badRequest().body(ApiResponse.error("验证码错误或已过期"));
        }
        
        LoginResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/login/")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout/")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("已退出登录", null));
    }

    @GetMapping("/profile/")
    public ResponseEntity<ApiResponse<UserDTO>> getProfile() {
        return ResponseEntity.ok(ApiResponse.success(userService.getCurrentUser()));
    }

    @PutMapping("/profile/")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(@RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(request)));
    }

    @PostMapping("/avatar/")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAvatar(@RequestParam("avatar") Object avatar) {
        return ResponseEntity.ok(ApiResponse.success(Map.of("avatar", "/media/avatars/default.png")));
    }

    @GetMapping("/users/")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @GetMapping("/users/{id}/")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PostMapping("/users/{id}/follow/")
    public ResponseEntity<ApiResponse<Void>> followUser(@PathVariable Long id) {
        userService.followUser(id);
        return ResponseEntity.ok(ApiResponse.success("关注成功", null));
    }

    @DeleteMapping("/users/{id}/follow/")
    public ResponseEntity<ApiResponse<Void>> unfollowUser(@PathVariable Long id) {
        userService.unfollowUser(id);
        return ResponseEntity.ok(ApiResponse.success("取消关注成功", null));
    }

    @PostMapping("/send-verification-code/")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String phoneOrEmail = body.get("phone_or_email");
        String type = body.get("type");
        
        if (phoneOrEmail == null || phoneOrEmail.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请输入手机号或邮箱"));
        }
        
        String ipAddress = request.getRemoteAddr();
        
        try {
            if ("email".equals(type)) {
                verificationCodeService.sendEmailCode(phoneOrEmail, ipAddress);
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("暂不支持短信验证码"));
            }
            return ResponseEntity.ok(ApiResponse.success("验证码发送成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/security-config/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSecurityConfig() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "verification_code_enabled", true,
                "verification_code_type", "email",
                "verification_code_method", "code",
                "verification_code_only_register", true
        )));
    }
}
