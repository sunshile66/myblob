package com.myblob.module.accounts.service;

import com.myblob.common.BusinessException;
import com.myblob.common.service.EmailSender;
import com.myblob.module.accounts.dto.*;
import com.myblob.module.accounts.entity.Follow;
import com.myblob.module.accounts.entity.PasswordResetToken;
import com.myblob.module.accounts.entity.User;
import com.myblob.module.accounts.entity.UserProfile;
import com.myblob.module.accounts.repository.FollowRepository;
import com.myblob.module.accounts.repository.PasswordResetTokenRepository;
import com.myblob.module.accounts.repository.UserRepository;
import com.myblob.module.accounts.repository.UserProfileRepository;
import com.myblob.security.JwtUtil;
import com.myblob.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final FollowRepository followRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailSender emailSender;

    @Value("${myblob.frontend-url:http://localhost:3001}")
    private String frontendUrl;

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findByIdWithProfile(userId)
                .orElseThrow(() -> BusinessException.notFound("用户"));
        return toDTOSelf(user);
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

        log.info("新用户注册: username={}, email={}", request.getUsername(), request.getEmail());

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return LoginResponse.builder()
                .token(token)
                .user(toDTOSelf(user))
                .isNew(true)
                .build();
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 单次查询：按用户名或邮箱查找
        User user = userRepository.findByUsernameOrEmail(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("登录失败: username={}, reason=用户不存在", request.getUsername());
                    return new BusinessException("用户名或密码错误");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("登录失败: username={}, reason=密码错误", request.getUsername());
            throw new BusinessException("用户名或密码错误");
        }

        log.info("用户登录: username={}", request.getUsername());

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return LoginResponse.builder()
                .token(token)
                .user(toDTOSelf(user))
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
        if (request.getBio() != null)
            profile.setBio(request.getBio());
        if (request.getWebsite() != null)
            profile.setWebsite(request.getWebsite());
        if (request.getCompany() != null)
            profile.setCompany(request.getCompany());
        if (request.getProfession() != null)
            profile.setProfession(request.getProfession());
        if (request.getLocation() != null)
            profile.setLocation(request.getLocation());
        if (request.getPhone() != null)
            profile.setPhone(request.getPhone());
        if (request.getWechat() != null)
            profile.setWechat(request.getWechat());
        if (request.getQq() != null)
            profile.setQq(request.getQq());
        if (request.getWeibo() != null)
            profile.setWeibo(request.getWeibo());
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
        List<User> users = userRepository.findAll();
        if (users.isEmpty())
            return List.of();

        List<Long> userIds = users.stream().map(User::getId).toList();
        Set<Long> followingIds = currentUserId != null
                ? new HashSet<>(followRepository.findFollowingIds(currentUserId, userIds))
                : Set.of();
        Map<Long, Long> followerCounts = followRepository.countFollowersGrouped(userIds).stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
        Map<Long, Long> followingCounts = followRepository.countFollowingGrouped(userIds).stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));

        final Set<Long> finalFollowing = followingIds;
        final Map<Long, Long> finalFollowerCounts = followerCounts;
        final Map<Long, Long> finalFollowingCounts = followingCounts;
        return users.stream()
                .map(u -> UserDTOAssembler.toFullDTO(u, currentUserId, finalFollowing, finalFollowerCounts,
                        finalFollowingCounts))
                .toList();
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);
        if (user == null) {
            return;
        }

        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        token = token.substring(0, 64);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiryTime(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();
        passwordResetTokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        emailSender.sendPasswordResetEmail(email, resetLink);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new BusinessException("重置链接无效或已过期"));

        if (resetToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);
            throw new BusinessException("重置链接已过期，请重新申请");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    /**
     * 自己查看自己资料，跳过关注状态查询（3次DB查询→0次）
     */
    private UserDTO toDTOSelf(User user) {
        return UserDTOAssembler.toFullDTO(user, user.getId(), Set.of(), Map.of(), Map.of());
    }

    public UserDTO toDTO(User user, Long currentUserId) {
        List<Long> userIds = List.of(user.getId());
        Set<Long> followingIds = Set.of();
        Map<Long, Long> followerCounts = Map.of();
        Map<Long, Long> followingCounts = Map.of();

        // 仅在需要交互状态时查询，避免无效 DB 查询
        if (currentUserId != null) {
            followingIds = new HashSet<>(followRepository.findFollowingIds(currentUserId, userIds));
            followerCounts = followRepository.countFollowersGrouped(userIds).stream()
                    .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
            followingCounts = followRepository.countFollowingGrouped(userIds).stream()
                    .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
        }
        return UserDTOAssembler.toFullDTO(user, currentUserId, followingIds, followerCounts, followingCounts);
    }
}
