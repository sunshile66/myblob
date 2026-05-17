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
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Long id;
        try {
            id = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            User user = userRepository.findByUsername(userId)
                    .or(() -> userRepository.findByEmail(userId))
                    .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
            return new JwtUserDetails(user);
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + id));
        return new JwtUserDetails(user);
    }
}
