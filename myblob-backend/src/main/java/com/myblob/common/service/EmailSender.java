package com.myblob.common.service;

public interface EmailSender {
    void sendVerificationCode(String to, String code);

    void sendPasswordResetEmail(String to, String resetLink);
}
