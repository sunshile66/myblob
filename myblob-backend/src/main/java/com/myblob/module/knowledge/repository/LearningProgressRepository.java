package com.myblob.module.knowledge.repository;

import com.myblob.module.accounts.entity.User;
import com.myblob.module.knowledge.entity.LearningProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {

    @Query("SELECT lp FROM LearningProgress lp WHERE lp.user = :user AND lp.itemType = :itemType AND lp.itemId = :itemId AND lp.deleted = false")
    Optional<LearningProgress> findByUserAndItemTypeAndItemId(@Param("user") User user, @Param("itemType") String itemType, @Param("itemId") Long itemId);

    @Query("SELECT lp FROM LearningProgress lp WHERE lp.user = :user AND lp.nextReview < :dateTime AND lp.deleted = false")
    List<LearningProgress> findByUserAndNextReviewBefore(@Param("user") User user, @Param("dateTime") LocalDateTime dateTime);

    @Query("SELECT lp FROM LearningProgress lp WHERE lp.user = :user AND lp.nextReview BETWEEN :start AND :end AND lp.deleted = false")
    List<LearningProgress> findByUserAndNextReviewBetween(@Param("user") User user, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(lp) FROM LearningProgress lp WHERE lp.user = :user AND lp.status > :status AND lp.deleted = false")
    long countByUserAndStatusGreaterThan(@Param("user") User user, @Param("status") Integer status);

    @Query("SELECT COUNT(lp) FROM LearningProgress lp WHERE lp.user = :user AND lp.status = :status AND lp.deleted = false")
    long countByUserAndStatus(@Param("user") User user, @Param("status") Integer status);

    @Query("SELECT COUNT(lp) FROM LearningProgress lp WHERE lp.user = :user AND lp.nextReview < :dateTime AND lp.deleted = false")
    long countByUserAndNextReviewBefore(@Param("user") User user, @Param("dateTime") LocalDateTime dateTime);

    @Query("SELECT lp.itemType, COUNT(lp) FROM LearningProgress lp WHERE lp.user = :user AND lp.status = 2 AND lp.deleted = false GROUP BY lp.itemType")
    List<Object[]> getCategoryMastery(@Param("user") User user);

    @Query(value = """
        SELECT DATE(last_review) as review_date, COUNT(*) as count
        FROM learning_progress
        WHERE user_id = :userId AND last_review IS NOT NULL AND last_review >= :since AND deleted = false
        GROUP BY DATE(last_review)
        """, nativeQuery = true)
    List<Object[]> getHeatmapData(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT lp FROM LearningProgress lp WHERE lp.user = :user AND lp.status = 1 AND lp.deleted = false ORDER BY lp.nextReview ASC")
    List<LearningProgress> findDueReviews(@Param("user") User user, Pageable pageable);
}
