package com.myblob.module.accounts.service;

import com.myblob.common.BusinessException;
import com.myblob.module.accounts.dto.*;
import com.myblob.module.accounts.entity.Follow;
import com.myblob.module.accounts.entity.User;
import com.myblob.module.accounts.entity.UserProfile;
import com.myblob.module.accounts.repository.FollowRepository;
import com.myblob.module.accounts.repository.UserRepository;
import com.myblob.module.accounts.repository.UserProfileRepository;
import com.myblob.security.JwtUtil;
import com.myblob.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findByIdWithProfile(userId)
                .orElseThrow(() -> BusinessException.notFound("用户"));
        return toDTO(user, userId);
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        user = userRepository.save(user);

        UserProfile profile = UserProfile.builder().user(user).build();
        userProfileRepository.save(profile);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return LoginResponse.builder()
                .token(token)
                .user(toDTO(user, user.getId()))
                .isNew(true)
                .build();
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(request.getUsername());
        }

        User user = userOpt.orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return LoginResponse.builder()
                .token(token)
                .user(toDTO(user, user.getId()))
                .build();
    }

    @Transactional
    public UserDTO updateProfile(UpdateProfileRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findByIdWithProfile(userId)
                .orElseThrow(() -> BusinessException.notFound("用户"));

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        userRepository.save(user);

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = UserProfile.builder().user(user).build();
        }
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getWebsite() != null) profile.setWebsite(request.getWebsite());
        if (request.getCompany() != null) profile.setCompany(request.getCompany());
        if (request.getProfession() != null) profile.setProfession(request.getProfession());
        if (request.getLocation() != null) profile.setLocation(request.getLocation());
        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getWechat() != null) profile.setWechat(request.getWechat());
        if (request.getQq() != null) profile.setQq(request.getQq());
        if (request.getWeibo() != null) profile.setWeibo(request.getWeibo());
        userProfileRepository.save(profile);

        return toDTO(user, userId);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findByIdWithProfile(id)
                .orElseThrow(() -> BusinessException.notFound("用户"));
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();
        return toDTO(user, currentUserId);
    }

    @Transactional
    public void followUser(Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId.equals(userId)) {
            throw new BusinessException("不能关注自己");
        }
        if (followRepository.existsByFollowerIdAndFollowingId(currentUserId, userId)) {
            throw new BusinessException("已经关注过了");
        }
        User follower = userRepository.getReferenceById(currentUserId);
        User following = userRepository.getReferenceById(userId);
        Follow follow = Follow.builder().follower(follower).following(following).build();
        followRepository.save(follow);
    }

    @Transactional
    public void unfollowUser(Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!followRepository.existsByFollowerIdAndFollowingId(currentUserId, userId)) {
            throw new BusinessException("还没有关注");
        }
        followRepository.deleteByFollowerIdAndFollowingId(currentUserId, userId);
    }

    @Transactional(readOnly = true)
    public java.util.List<UserDTO> getAllUsers() {
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();
        return userRepository.findAll().stream()
                .map(u -> toDTO(u, currentUserId))
                .toList();
    }

    public UserDTO toDTO(User user, Long currentUserId) {
        UserProfile profile = user.getProfile();
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .emailVerified(user.getEmailVerified())
                .role(user.getRole() != null ? user.getRole().name() : "USER")
                .isSuperuser(Boolean.TRUE.equals(user.getSuperuser()))
                .bio(profile != null ? profile.getBio() : null)
                .website(profile != null ? profile.getWebsite() : null)
                .company(profile != null ? profile.getCompany() : null)
                .profession(profile != null ? profile.getProfession() : null)
                .location(profile != null ? profile.getLocation() : null)
                .phone(profile != null ? profile.getPhone() : null)
                .wechat(profile != null ? profile.getWechat() : null)
                .qq(profile != null ? profile.getQq() : null)
                .weibo(profile != null ? profile.getWeibo() : null)
                .isFollowing(currentUserId != null && !currentUserId.equals(user.getId())
                        && followRepository.existsByFollowerIdAndFollowingId(currentUserId, user.getId()))
                .followerCount(followRepository.countByFollowingId(user.getId()))
                .followingCount(followRepository.countByFollowerId(user.getId()))
                .build();
    }
}
