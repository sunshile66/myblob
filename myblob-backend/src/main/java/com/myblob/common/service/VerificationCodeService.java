package com.myblob.common.service;

import com.myblob.module.security.entity.VerificationLog;
import com.myblob.module.security.repository.VerificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final VerificationLogRepository verificationLogRepository;
    private final EmailSender emailSender;

    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRY_MINUTES = 5;
    private static final int MAX_SEND_PER_HOUR = 5;

    public String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    @Transactional
    public void sendEmailCode(String email, String ipAddress) {
        long count = verificationLogRepository.countByTargetAndCreatedAtAfter(
                email, LocalDateTime.now().minusHours(1));
        
        if (count >= MAX_SEND_PER_HOUR) {
            throw new RuntimeException("发送频率过高，请稍后再试");
        }

        String code = generateCode();

        VerificationLog verificationLog = new VerificationLog();
        verificationLog.setType("email");
        verificationLog.setTarget(email);
        verificationLog.setCode(code);
        verificationLog.setStatus("pending");
        verificationLog.setIpAddress(ipAddress);
        
        try {
            emailSender.sendVerificationCode(email, code);
            verificationLog.setSendStatus("success");
            log.info("验证码已发送至邮箱: {}", email);
        } catch (Exception e) {
            verificationLog.setSendStatus("failed");
            verificationLog.setErrorMessage(e.getMessage());
            log.error("发送验证码失败: {}", e.getMessage());
            throw new RuntimeException("发送验证码失败: " + e.getMessage());
        }

        verificationLogRepository.save(verificationLog);
    }

    @Transactional
    public boolean verifyCode(String target, String code) {
        VerificationLog verificationLog = verificationLogRepository
                .findTopByTargetAndStatusOrderByCreatedAtDesc(target, "pending")
                .orElse(null);

        if (verificationLog == null) {
            log.warn("未找到待验证的验证码，target: {}", target);
            return false;
        }

        LocalDateTime expiryTime = verificationLog.getCreatedAt().plusMinutes(CODE_EXPIRY_MINUTES);
        if (LocalDateTime.now().isAfter(expiryTime)) {
            verificationLog.setStatus("expired");
            verificationLogRepository.save(verificationLog);
            log.warn("验证码已过期，target: {}", target);
            return false;
        }

        if (!verificationLog.getCode().equals(code)) {
            log.warn("验证码错误，target: {}", target);
            return false;
        }

        verificationLog.setStatus("verified");
        verificationLogRepository.save(verificationLog);
        log.info("验证码验证成功，target: {}", target);
        return true;
    }
}
