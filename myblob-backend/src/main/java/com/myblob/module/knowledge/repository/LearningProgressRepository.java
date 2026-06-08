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

    Optional<LearningProgress> findByUserAndItemTypeAndItemId(User user, String itemType, Long itemId);

    List<LearningProgress> findByUserAndNextReviewBefore(User user, LocalDateTime dateTime);

    List<LearningProgress> findByUserAndNextReviewBetween(User user, LocalDateTime start, LocalDateTime end);

    long countByUserAndStatusGreaterThan(User user, Integer status);

    long countByUserAndStatus(User user, Integer status);

    long countByUserAndNextReviewBefore(User user, LocalDateTime dateTime);

    @Query("SELECT lp.itemType, COUNT(lp) FROM LearningProgress lp WHERE lp.user = :user AND lp.status = 2 GROUP BY lp.itemType")
    List<Object[]> getCategoryMastery(@Param("user") User user);

    @Query(value = """
        SELECT DATE(last_review) as review_date, COUNT(*) as count
        FROM learning_progress
        WHERE user_id = :userId AND last_review IS NOT NULL AND last_review >= :since
        GROUP BY DATE(last_review)
        """, nativeQuery = true)
    List<Object[]> getHeatmapData(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT lp FROM LearningProgress lp WHERE lp.user = :user AND lp.status = 1 ORDER BY lp.nextReview ASC")
    List<LearningProgress> findDueReviews(@Param("user") User user, Pageable pageable);
}
