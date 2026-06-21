package com.myblob.module.security.service;

import com.myblob.common.PageResponse;
import com.myblob.module.security.entity.IPBlock;
import com.myblob.module.security.entity.RequestLog;
import com.myblob.module.security.entity.UserSession;
import com.myblob.module.security.repository.IPBlockRepository;
import com.myblob.module.security.repository.RequestLogRepository;
import com.myblob.module.security.repository.UserSessionRepository;
import com.myblob.module.security.repository.VerificationLogRepository;
import com.myblob.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityModuleService {

    private final IPBlockRepository ipBlockRepository;
    private final RequestLogRepository requestLogRepository;
    private final UserSessionRepository userSessionRepository;
    private final VerificationLogRepository verificationLogRepository;

    @Transactional(readOnly = true)
    public PageResponse<Map<String, Object>> getBlockedIPs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Map<String, Object>> result = ipBlockRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toIPBlockMap);
        return PageResponse.of(result);
    }

    @Transactional(readOnly = true)
    public PageResponse<Map<String, Object>> getRequestLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Map<String, Object>> result = requestLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toRequestLogMap);
        return PageResponse.of(result);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMySessions() {
        Long userId = SecurityUtil.getCurrentUserId();
        return userSessionRepository.findByUserIdAndActiveTrue(userId).stream()
                .map(this::toSessionMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> createSession(String ipAddress, String userAgent) {
        Long userId = SecurityUtil.getCurrentUserId();
        UserSession session = UserSession.builder()
                .user(com.myblob.module.accounts.entity.User.builder().id(userId).build())
                .sessionKey(UUID.randomUUID().toString().replace("-", ""))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .active(true)
                .lastActivity(LocalDateTime.now())
                .expiredTime(LocalDateTime.now().plusHours(24))
                .build();
        session = userSessionRepository.save(session);
        return toSessionMap(session);
    }

    @Transactional
    public void deactivateSession(Long sessionId) {
        Long userId = SecurityUtil.getCurrentUserId();
        userSessionRepository.findById(sessionId).ifPresent(session -> {
            if (session.getUser().getId().equals(userId)) {
                session.setActive(false);
                userSessionRepository.save(session);
            }
        });
    }

    private Map<String, Object> toRequestLogMap(RequestLog log) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", log.getId());
        map.put("user_id", log.getUser() != null ? log.getUser().getId() : null);
        map.put("ip_address", log.getIpAddress());
        map.put("method", log.getMethod());
        map.put("path", log.getPath());
        map.put("status_code", log.getStatusCode());
        map.put("user_agent", log.getUserAgent());
        map.put("response_time", log.getResponseTime());
        map.put("blocked", log.getBlocked());
        map.put("created_at", log.getCreatedAt());
        return map;
    }

    private Map<String, Object> toIPBlockMap(IPBlock block) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", block.getId());
        map.put("ip", block.getIp());
        map.put("reason", block.getReason());
        map.put("blocked_until", block.getBlockedUntil());
        map.put("is_active", block.getActive());
        map.put("created_at", block.getCreatedAt());
        return map;
    }

    private Map<String, Object> toSessionMap(UserSession session) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", session.getId());
        map.put("user_id", session.getUser().getId());
        map.put("session_key", session.getSessionKey());
        map.put("ip_address", session.getIpAddress());
        map.put("user_agent", session.getUserAgent());
        map.put("is_active", session.getActive());
        map.put("last_activity", session.getLastActivity());
        map.put("expired_time", session.getExpiredTime());
        map.put("created_at", session.getCreatedAt());
        return map;
    }
}
