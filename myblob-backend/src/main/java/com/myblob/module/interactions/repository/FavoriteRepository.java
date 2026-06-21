package com.myblob.module.interactions.repository;

import com.myblob.module.interactions.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    Optional<Favorite> findByUserIdAndPostId(Long userId, Long postId);

    void deleteByUserIdAndPostId(Long userId, Long postId);

    Page<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT f FROM Favorite f JOIN FETCH f.post WHERE f.user.id = :userId AND f.deleted = false ORDER BY f.createdAt DESC")
    Page<Favorite> findByUserIdWithPost(@Param("userId") Long userId, Pageable pageable);

    /**
     * 批量查询用户对文章列表的收藏状态，避免 N+1 查询
     */
    @Query("SELECT f.post.id FROM Favorite f WHERE f.user.id = :userId AND f.post.id IN :postIds")
    List<Long> findFavoritedPostIdsByUserIdAndPostIds(@Param("userId") Long userId,
            @Param("postIds") List<Long> postIds);
}
