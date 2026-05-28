package com.myblob.module.accounts.repository;

import com.myblob.module.accounts.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    long countByFollowingId(Long userId);

    long countByFollowerId(Long userId);

    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    /**
     * 批量查询：当前用户关注的用户ID集合
     */
    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :followerId AND f.following.id IN :userIds")
    List<Long> findFollowingIds(@Param("followerId") Long followerId, @Param("userIds") List<Long> userIds);

    /**
     * 批量统计粉丝数：key=userId, value=followerCount
     */
    @Query("SELECT f.following.id, COUNT(f) FROM Follow f WHERE f.following.id IN :userIds GROUP BY f.following.id")
    List<Object[]> countFollowersGrouped(@Param("userIds") List<Long> userIds);

    /**
     * 批量统计关注数：key=userId, value=followingCount
     */
    @Query("SELECT f.follower.id, COUNT(f) FROM Follow f WHERE f.follower.id IN :userIds GROUP BY f.follower.id")
    List<Object[]> countFollowingGrouped(@Param("userIds") List<Long> userIds);
}
