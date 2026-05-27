package com.myblob.module.blog.service;

import com.myblob.common.BusinessException;
import com.myblob.module.accounts.repository.UserRepository;
import com.myblob.module.blog.entity.Post;
import com.myblob.module.blog.repository.PostRepository;
import com.myblob.module.interactions.entity.Favorite;
import com.myblob.module.interactions.entity.PostLike;
import com.myblob.module.interactions.repository.FavoriteRepository;
import com.myblob.module.interactions.repository.PostLikeRepository;
import com.myblob.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 文章互动服务：点赞/取消点赞、收藏/取消收藏、批量查询互动状态
 */
@Service
@RequiredArgsConstructor
public class PostInteractionService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final FavoriteRepository favoriteRepository;

    @Transactional
    public void likePost(String slug) {
        Long userId = SecurityUtil.getCurrentUserId();
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> BusinessException.notFound("文章"));
        if (postLikeRepository.existsByUserIdAndPostId(userId, post.getId())) {
            throw new BusinessException("已经点赞过了");
        }
        PostLike like = PostLike.builder()
                .user(userRepository.getReferenceById(userId))
                .post(post)
                .build();
        postLikeRepository.save(like);
        postLikeRepository.incrementLikeCount(post.getId());
    }

    @Transactional
    public void unlikePost(String slug) {
        Long userId = SecurityUtil.getCurrentUserId();
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> BusinessException.notFound("文章"));
        if (!postLikeRepository.existsByUserIdAndPostId(userId, post.getId())) {
            throw new BusinessException("还没有点赞");
        }
        postLikeRepository.deleteByUserIdAndPostId(userId, post.getId());
        postLikeRepository.decrementLikeCount(post.getId());
    }

    @Transactional
    public void favoritePost(String slug) {
        Long userId = SecurityUtil.getCurrentUserId();
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> BusinessException.notFound("文章"));
        if (favoriteRepository.existsByUserIdAndPostId(userId, post.getId())) {
            throw new BusinessException("已经收藏过了");
        }
        Favorite fav = Favorite.builder()
                .user(userRepository.getReferenceById(userId))
                .post(post)
                .build();
        favoriteRepository.save(fav);
    }

    @Transactional
    public void unfavoritePost(String slug) {
        Long userId = SecurityUtil.getCurrentUserId();
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> BusinessException.notFound("文章"));
        if (!favoriteRepository.existsByUserIdAndPostId(userId, post.getId())) {
            throw new BusinessException("还没有收藏");
        }
        favoriteRepository.deleteByUserIdAndPostId(userId, post.getId());
    }

    /**
     * 批量查询用户对文章列表的点赞状态
     */
    public Set<Long> getLikedPostIds(Long userId, List<Long> postIds) {
        if (userId == null || postIds.isEmpty())
            return Set.of();
        return new HashSet<>(postLikeRepository.findLikedPostIdsByUserIdAndPostIds(userId, postIds));
    }

    /**
     * 批量查询用户对文章列表的收藏状态
     */
    public Set<Long> getFavoritedPostIds(Long userId, List<Long> postIds) {
        if (userId == null || postIds.isEmpty())
            return Set.of();
        return new HashSet<>(favoriteRepository.findFavoritedPostIdsByUserIdAndPostIds(userId, postIds));
    }
}
