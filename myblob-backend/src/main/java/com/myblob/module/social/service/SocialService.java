package com.myblob.module.social.service;

import com.myblob.common.BusinessException;
import com.myblob.module.accounts.dto.UserDTO;
import com.myblob.module.accounts.entity.User;
import com.myblob.module.accounts.repository.UserRepository;
import com.myblob.module.social.entity.OAuthApp;
import com.myblob.module.social.entity.SocialAccount;
import com.myblob.module.social.entity.SocialLoginLog;
import com.myblob.module.social.repository.OAuthAppRepository;
import com.myblob.module.social.repository.SocialAccountRepository;
import com.myblob.module.social.repository.SocialLoginLogRepository;
import com.myblob.security.JwtUtil;
import com.myblob.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialService {

    private final OAuthAppRepository oauthAppRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final SocialLoginLogRepository socialLoginLogRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getActiveOAuthApps() {
        return oauthAppRepository.findByActiveTrue().stream().map(app -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", app.getId());
            map.put("provider", app.getProvider().name().toLowerCase());
            map.put("provider_display", getProviderDisplay(app.getProvider()));
            map.put("app_id", app.getAppId());
            map.put("redirect_uri", app.getRedirectUri());
            map.put("scope", app.getScope());
            map.put("is_active", app.getActive());
            return map;
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMySocialAccounts() {
        Long userId = SecurityUtil.getCurrentUserId();
        return socialAccountRepository.findByUserId(userId).stream().map(account -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", account.getId());
            map.put("user_id", account.getUser().getId());
            map.put("provider", account.getProvider().name().toLowerCase());
            map.put("provider_display", getProviderDisplay(account.getProvider()));
            map.put("openid", account.getOpenid());
            map.put("nickname", account.getNickname());
            map.put("avatar", account.getAvatar());
            return map;
        }).toList();
    }

    @Transactional
    public Map<String, Object> oauthCallback(String providerStr, String code, String ipAddress, String userAgent) {
        SocialAccount.Provider provider = SocialAccount.Provider.valueOf(providerStr.toUpperCase());
        OAuthApp app = oauthAppRepository.findByProviderAndActiveTrue(provider)
                .orElseThrow(() -> BusinessException.notFound("OAuth应用"));

        String openid = generateOpenid(providerStr, code);

        SocialAccount existing = socialAccountRepository.findByProviderAndOpenid(provider, openid).orElse(null);
        if (existing != null) {
            User user = existing.getUser();
            socialLoginLogRepository.save(SocialLoginLog.builder()
                    .provider(provider).openid(openid).user(user).status("success")
                    .ipAddress(ipAddress).userAgent(userAgent).build());

            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            UserDTO userDTO = UserDTO.builder()
                    .id(user.getId()).username(user.getUsername())
                    .nickname(user.getNickname()).avatar(user.getAvatar()).email(user.getEmail()).build();
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("token", token);
            result.put("user", userDTO);
            result.put("is_new", false);
            return result;
        }

        String nickname = getProviderDisplay(provider) + "_用户_" + openid.substring(Math.max(0, openid.length() - 8));
        String username = providerStr.toLowerCase() + "_" + openid.substring(Math.max(0, openid.length() - 12));

        User newUser = User.builder()
                .username(username)
                .email(openid + "@" + providerStr.toLowerCase() + ".com")
                .nickname(nickname)
                .password(UUID.randomUUID().toString())
                .build();
        newUser = userRepository.save(newUser);

        socialAccountRepository.save(SocialAccount.builder()
                .user(newUser).provider(provider).openid(openid).nickname(nickname).build());

        socialLoginLogRepository.save(SocialLoginLog.builder()
                .provider(provider).openid(openid).user(newUser).status("success")
                .ipAddress(ipAddress).userAgent(userAgent).build());

        String token = jwtUtil.generateToken(newUser.getId(), newUser.getUsername());
        UserDTO userDTO = UserDTO.builder()
                .id(newUser.getId()).username(newUser.getUsername())
                .nickname(newUser.getNickname()).avatar(newUser.getAvatar()).email(newUser.getEmail()).build();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("user", userDTO);
        result.put("is_new", true);
        return result;
    }

    private String generateOpenid(String provider, String code) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest((provider + "_" + code).getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().substring(0, 32);
        } catch (Exception e) {
            return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        }
    }

    private String getProviderDisplay(SocialAccount.Provider provider) {
        return switch (provider) {
            case WECHAT -> "微信";
            case QQ -> "QQ";
            case WEIBO -> "微博";
            case GITHUB -> "GitHub";
            case GOOGLE -> "Google";
            case FACEBOOK -> "Facebook";
            case TWITTER -> "Twitter";
        };
    }
}
