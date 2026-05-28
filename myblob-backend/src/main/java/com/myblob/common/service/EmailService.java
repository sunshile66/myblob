package com.myblob.common.service;

import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnExpression("'${myblob.email.mock-mode:true}' == 'false' and '${spring.mail.host:}' != ''")
public class EmailService implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationCode(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("【MyBlob】验证码");
            message.setText("您的验证码是：" + code + "\n\n验证码有效期为5分钟，请勿泄露给他人。\n\n如非本人操作，请忽略此邮件。");

            mailSender.send(message);
            log.info("验证码邮件已发送至: {}", to);
        } catch (Exception e) {
            String detail = extractMailError(e);
            log.error("发送验证码邮件失败 [to={}]: {}", to, detail);
            throw new RuntimeException(detail);
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("【MyBlob】密码重置");
            message.setText("您好，\n\n请点击以下链接重置您的密码：\n" + resetLink + "\n\n该链接有效期为30分钟。\n\n如非本人操作，请忽略此邮件。");

            mailSender.send(message);
            log.info("密码重置邮件已发送至: {}", to);
        } catch (Exception e) {
            String detail = extractMailError(e);
            log.error("发送密码重置邮件失败 [to={}]: {}", to, detail);
            throw new RuntimeException(detail);
        }
    }

    /**
     * 提取邮件发送错误的用户友好描述
     */
    private String extractMailError(Exception e) {
        String msg = e.getMessage();
        if (msg != null) {
            // QQ邮箱常见错误
            if (msg.contains("535") || msg.contains("authentication failed")) {
                return "邮箱认证失败，请检查SMTP授权码是否正确且未过期";
            }
            if (msg.contains("554") || msg.contains("connection refused")) {
                return "邮件服务拒绝连接，可能是发送频率过高被限制";
            }
            if (msg.contains("550")) {
                return "收件人邮箱地址不存在或拒绝接收";
            }
            if (msg.contains("timeout") || msg.contains("timed out")) {
                return "邮件服务器连接超时，请稍后重试";
            }
        }
        if (e.getCause() instanceof MessagingException && msg == null) {
            msg = e.getCause().getMessage();
        }
        return "邮件发送失败: " + (msg != null ? msg : "未知错误");
    }
}
