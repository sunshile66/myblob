package com.myblob.module.apigateway.service;

import com.myblob.common.BusinessException;
import com.myblob.common.PageResponse;
import com.myblob.module.accounts.entity.User;
import com.myblob.module.accounts.repository.UserRepository;
import com.myblob.module.apigateway.entity.APICallLog;
import com.myblob.module.apigateway.entity.APIEndpoint;
import com.myblob.module.apigateway.entity.APIKey;
import com.myblob.module.apigateway.entity.APIService;
import com.myblob.module.apigateway.repository.APICallLogRepository;
import com.myblob.module.apigateway.repository.APIEndpointRepository;
import com.myblob.module.apigateway.repository.APIKeyRepository;
import com.myblob.module.apigateway.repository.APIServiceRepository;
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
public class APIGatewayService {

    private final APIServiceRepository apiServiceRepository;
    private final APIEndpointRepository apiEndpointRepository;
    private final APIKeyRepository apiKeyRepository;
    private final APICallLogRepository apiCallLogRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getServices() {
        return apiServiceRepository.findByActiveTrue().stream().map(this::toServiceMap).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getEndpoints(Long serviceId) {
        return apiEndpointRepository.findByServiceIdAndActiveTrue(serviceId).stream()
                .map(this::toEndpointMap).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMyAPIKeys() {
        Long userId = SecurityUtil.getCurrentUserId();
        return apiKeyRepository.findByUserId(userId).stream().map(this::toAPIKeyMap).toList();
    }

    @Transactional
    public Map<String, Object> createAPIKey(String name, String description) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.getReferenceById(userId);

        String key = "mk_" + UUID.randomUUID().toString().replace("-", "");

        APIKey apiKey = APIKey.builder()
                .user(user)
                .apiKey(key)
                .name(name)
                .description(description)
                .dailyLimit(10000)
                .usedCount(0)
                .active(true)
                .expiredTime(LocalDateTime.now().plusYears(1))
                .build();

        apiKey = apiKeyRepository.save(apiKey);
        return toAPIKeyMap(apiKey);
    }

    @Transactional
    public void revokeAPIKey(Long keyId) {
        Long userId = SecurityUtil.getCurrentUserId();
        APIKey apiKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> BusinessException.notFound("API Key"));

        if (!apiKey.getUser().getId().equals(userId)) {
            throw BusinessException.forbidden("无权撤销此 API Key");
        }

        apiKey.setActive(false);
        apiKeyRepository.save(apiKey);
    }

    @Transactional(readOnly = true)
    public PageResponse<Map<String, Object>> getMyCallLogs(int page, int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<Long> keyIds = apiKeyRepository.findByUserId(userId).stream()
                .map(APIKey::getId).toList();
        Pageable pageable = PageRequest.of(page, size);

        Page<Map<String, Object>> result;
        if (keyIds.isEmpty()) {
            result = Page.empty(pageable);
        } else {
            result = apiCallLogRepository.findByApiKeyIdOrderByCreatedAtDesc(keyIds.get(0), pageable)
                    .map(this::toCallLogMap);
        }
        return PageResponse.of(result);
    }

    private Map<String, Object> toServiceMap(APIService service) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", service.getId());
        map.put("name", service.getName());
        map.put("code", service.getCode());
        map.put("description", service.getDescription());
        map.put("base_url", service.getBaseUrl());
        map.put("timeout", service.getTimeout());
        map.put("is_active", service.getActive());
        map.put("created_at", service.getCreatedAt());
        map.put("updated_at", service.getUpdatedAt());
        return map;
    }

    private Map<String, Object> toEndpointMap(APIEndpoint endpoint) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", endpoint.getId());
        map.put("service_id", endpoint.getService().getId());
        map.put("path", endpoint.getPath());
        map.put("method", endpoint.getMethod());
        map.put("name", endpoint.getName());
        map.put("description", endpoint.getDescription());
        map.put("is_public", endpoint.getIsPublic());
        map.put("rate_limit", endpoint.getRateLimit());
        map.put("is_active", endpoint.getActive());
        return map;
    }

    private Map<String, Object> toAPIKeyMap(APIKey key) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", key.getId());
        map.put("user_id", key.getUser().getId());
        map.put("api_key", key.getApiKey());
        map.put("name", key.getName());
        map.put("description", key.getDescription());
        map.put("daily_limit", key.getDailyLimit());
        map.put("used_count", key.getUsedCount());
        map.put("last_used_at", key.getLastUsedAt());
        map.put("is_active", key.getActive());
        map.put("expired_time", key.getExpiredTime());
        map.put("created_at", key.getCreatedAt());
        map.put("updated_at", key.getUpdatedAt());
        return map;
    }

    private Map<String, Object> toCallLogMap(APICallLog log) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", log.getId());
        map.put("api_key_id", log.getApiKey() != null ? log.getApiKey().getId() : null);
        map.put("endpoint_id", log.getEndpoint() != null ? log.getEndpoint().getId() : null);
        map.put("request_method", log.getRequestMethod());
        map.put("request_path", log.getRequestPath());
        map.put("response_status", log.getResponseStatus());
        map.put("ip_address", log.getIpAddress());
        map.put("response_time", log.getResponseTime());
        map.put("success", log.getSuccess());
        map.put("created_at", log.getCreatedAt());
        return map;
    }
}
