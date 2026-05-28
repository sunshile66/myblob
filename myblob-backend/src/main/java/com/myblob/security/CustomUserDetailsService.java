package com.myblob.security;

import com.myblob.module.accounts.entity.User;
import com.myblob.module.accounts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // 支持用户名或邮箱登录
        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        return new JwtUserDetails(user);
    }

    /**
     * 根据用户ID加载用户详情（供JWT Filter使用）
     */
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + userId));
        return new JwtUserDetails(user);
    }
}
