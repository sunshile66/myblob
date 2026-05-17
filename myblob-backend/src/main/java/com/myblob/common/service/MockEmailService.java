package com.myblob.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnMissingBean(EmailService.class)
public class MockEmailService implements EmailSender {

    @Override
    public void sendVerificationCode(String to, String code) {
        log.warn("===========================================");
        log.warn("【邮件未配置】验证码: {}", code);
        log.warn("收件人: {}", to);
        log.warn("请配置 spring.mail 相关属性以启用真实邮件发送");
        log.warn("===========================================");
    }
}
