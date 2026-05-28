package com.myblob.module.accounts.controller;

import com.myblob.common.ApiResponse;
import com.myblob.common.RateLimit;
import com.myblob.common.service.VerificationCodeService;
import com.myblob.module.accounts.dto.*;
import com.myblob.module.accounts.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录、注册、资料管理等认证接口")
public class AuthController {

    private final UserService userService;
    private final VerificationCodeService verificationCodeService;

    @PostMapping("/register/")
    @Operation(summary = "用户注册")
    @RateLimit(maxRequests = 5, windowSeconds = 60, message = "注册请求过于频繁，请60秒后再试")
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
    @Operation(summary = "用户登录")
    @RateLimit(maxRequests = 10, windowSeconds = 60, message = "登录请求过于频繁，请60秒后再试")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout/")
    @Operation(summary = "退出登录")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("已退出登录", null));
    }

    @GetMapping("/profile/")
    @Operation(summary = "获取当前用户资料")
    public ResponseEntity<ApiResponse<UserDTO>> getProfile() {
        return ResponseEntity.ok(ApiResponse.success(userService.getCurrentUser()));
    }

    @PutMapping("/profile/")
    @Operation(summary = "更新用户资料")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(@RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(request)));
    }

    @PostMapping("/avatar/")
    @Operation(summary = "上传头像")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAvatar(@RequestParam("avatar") Object avatar) {
        return ResponseEntity.ok(ApiResponse.success(Map.of("avatar", "/media/avatars/default.png")));
    }

    @GetMapping("/users/")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有用户列表", description = "需要管理员权限")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @GetMapping("/users/{id}/")
    @Operation(summary = "根据ID获取用户信息")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PostMapping("/users/{id}/follow/")
    @Operation(summary = "关注用户")
    public ResponseEntity<ApiResponse<Void>> followUser(@PathVariable Long id) {
        userService.followUser(id);
        return ResponseEntity.ok(ApiResponse.success("关注成功", null));
    }

    @DeleteMapping("/users/{id}/follow/")
    @Operation(summary = "取消关注")
    public ResponseEntity<ApiResponse<Void>> unfollowUser(@PathVariable Long id) {
        userService.unfollowUser(id);
        return ResponseEntity.ok(ApiResponse.success("取消关注成功", null));
    }

    @PostMapping("/send-verification-code/")
    @Operation(summary = "发送验证码")
    @RateLimit(maxRequests = 3, windowSeconds = 60, message = "验证码发送过于频繁，请60秒后再试")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(@RequestBody Map<String, String> body,
            HttpServletRequest request) {
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
    @Operation(summary = "获取安全配置")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSecurityConfig() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "verification_code_enabled", true,
                "verification_code_type", "email",
                "verification_code_method", "code",
                "verification_code_only_register", true)));
    }

    @PostMapping("/forgot-password/")
    @Operation(summary = "忘记密码")
    @RateLimit(maxRequests = 3, windowSeconds = 60, message = "操作过于频繁，请60秒后再试")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请输入邮箱"));
        }
        userService.forgotPassword(email);
        return ResponseEntity.ok(ApiResponse.success("如果该邮箱已注册，您将收到密码重置邮件", null));
    }

    @PostMapping("/reset-password/")
    @Operation(summary = "重置密码")
    @RateLimit(maxRequests = 5, windowSeconds = 60, message = "操作过于频繁，请60秒后再试")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String password = body.get("password");

        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("重置链接无效"));
        }
        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body(ApiResponse.error("密码长度不能少于6位"));
        }

        try {
            userService.resetPassword(token, password);
            return ResponseEntity.ok(ApiResponse.success("密码重置成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
