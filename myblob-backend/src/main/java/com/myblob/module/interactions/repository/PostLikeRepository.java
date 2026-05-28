package com.myblob.module.interactions.repository;

import com.myblob.module.interactions.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId);

    void deleteByUserIdAndPostId(Long userId, Long postId);

    /**
     * 批量查询用户对文章列表的点赞状态，避免 N+1 查询
     */
    @Query("SELECT pl.post.id FROM PostLike pl WHERE pl.user.id = :userId AND pl.post.id IN :postIds")
    List<Long> findLikedPostIdsByUserIdAndPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

    /**
     * 原子递增文章点赞数
     */
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    void incrementLikeCount(@Param("postId") Long postId);

    /**
     * 原子递减文章点赞数（不少于0）
     */
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = GREATEST(p.likeCount - 1, 0) WHERE p.id = :postId")
    void decrementLikeCount(@Param("postId") Long postId);
}
