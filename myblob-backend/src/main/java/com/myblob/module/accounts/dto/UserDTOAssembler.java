package com.myblob.module.accounts.dto;

import com.myblob.module.accounts.entity.User;
import com.myblob.module.accounts.entity.UserProfile;

import java.util.Map;
import java.util.Set;

/**
 * UserDTO 组装器 —— 统一 User → UserDTO 的转换逻辑。
 * 消除 UserService.toDTO() 在批量查询时的 N+1 问题。
 */
public final class UserDTOAssembler {

    private UserDTOAssembler() {
    }

    /**
     * 转换为基本 DTO（仅包含 id/username/nickname/avatar）。
     * 适用于文章作者展示等只需摘要信息的场景。
     */
    public static UserDTO toBasicDTO(User user) {
        if (user == null)
            return null;
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    /**
     * 转换为完整 DTO（含个人资料 + 关注状态 + 关注数）。
     *
     * @param user            用户实体
     * @param currentUserId   当前登录用户ID（可为 null）
     * @param followingIds    当前用户正在关注的用户ID集合（批量传入避免 N+1）
     * @param followerCounts  粉丝数字典：key=userId, value=followerCount
     * @param followingCounts 关注数字典：key=userId, value=followingCount
     */
    public static UserDTO toFullDTO(User user, Long currentUserId,
            Set<Long> followingIds,
            Map<Long, Long> followerCounts,
            Map<Long, Long> followingCounts) {
        if (user == null)
            return null;
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
                .isFollowing(currentUserId != null
                        && !currentUserId.equals(user.getId())
                        && followingIds.contains(user.getId()))
                .followerCount(followerCounts.getOrDefault(user.getId(), 0L))
                .followingCount(followingCounts.getOrDefault(user.getId(), 0L))
                .build();
    }
}
