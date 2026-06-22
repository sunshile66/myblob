package com.myblob.seed;

import com.myblob.module.accounts.entity.User;
import com.myblob.module.accounts.entity.UserRole;
import com.myblob.module.accounts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 用户种子数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String SEED_EMAIL = "18945223056@163.com";
    private static final String SEED_USERNAME = "testuser";

    /**
     * 检查是否需要初始化用户
     * @return true 如果已存在或已创建
     */
    public boolean isSeeded() {
        return userRepository.existsByEmail(SEED_EMAIL) && userRepository.existsByUsername(SEED_USERNAME);
    }

    /**
     * 升级旧版用户
     */
    public boolean upgradeOldUser() {
        if (userRepository.existsByUsername(SEED_USERNAME)) {
            User oldUser = userRepository.findByUsername(SEED_USERNAME).orElseThrow();
            log.warn("发现旧版用户(username={}, email={})，升级邮箱和密码...", oldUser.getUsername(), oldUser.getEmail());
            oldUser.setEmail(SEED_EMAIL);
            oldUser.setPassword(passwordEncoder.encode("123456"));
            userRepository.save(oldUser);
            log.info("旧版用户已升级");
            return true;
        }
        return false;
    }

    /**
     * 创建种子用户
     */
    public User createSeedUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail(SEED_EMAIL);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setNickname("测试用户");
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setRole(UserRole.USER);
        return userRepository.save(user);
    }
}
