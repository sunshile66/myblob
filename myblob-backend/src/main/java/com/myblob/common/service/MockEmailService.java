package com.myblob.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
public class MockEmailService implements EmailSender {

    @Override
    public void sendVerificationCode(String to, String code) {
        log.warn("============================================");
        log.warn("【Mock模式】验证码: {}", code);
        log.warn("收件人: {}", to);
        log.warn("设置 myblob.email.mock-mode=false 并配置 spring.mail 以启用真实邮件");
        log.warn("============================================");
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        log.warn("============================================");
        log.warn("【Mock模式】密码重置链接: {}", resetLink);
        log.warn("收件人: {}", to);
        log.warn("设置 myblob.email.mock-mode=false 并配置 spring.mail 以启用真实邮件");
        log.warn("============================================");
    }
}
