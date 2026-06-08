package com.myblob.module.knowledge.service;

import com.myblob.module.accounts.entity.User;
import com.myblob.module.knowledge.entity.LearningProgress;
import com.myblob.module.knowledge.repository.LearningProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 间隔重复服务 - SM-2 算法实现
 * <p>
 * 参考: https://en.wikipedia.org/wiki/SuperMemo#Description_of_SM-2_algorithm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpacedRepetitionService {

    private static final double MIN_EASE_FACTOR = 1.3;
    private static final double INITIAL_EASE_FACTOR = 2.5;
    private static final int STATUS_NEW = 0;
    private static final int STATUS_LEARNING = 1;
    private static final int STATUS_MASTERED = 2;
    private static final int MASTERY_THRESHOLD_DAYS = 21;

    private final LearningProgressRepository learningProgressRepository;

    /**
     * 获取或创建学习进度
     */
    public LearningProgress getOrCreateProgress(User user, String itemType, Long itemId) {
        Optional<LearningProgress> existing = learningProgressRepository
                .findByUserAndItemTypeAndItemId(user, itemType, itemId);

        return existing.orElseGet(() -> {
            LearningProgress progress = LearningProgress.builder()
                    .user(user)
                    .itemType(itemType)
                    .itemId(itemId)
                    .repetitions(0)
                    .easeFactor(INITIAL_EASE_FACTOR)
                    .interval(0)
                    .lapses(0)
                    .status(STATUS_NEW)
                    .build();
            return learningProgressRepository.save(progress);
        });
    }

    /**
     * 获取待复习卡片
     */
    public List<LearningProgress> getDueReviews(User user, int limit) {
        return learningProgressRepository.findDueReviews(user, PageRequest.of(0, limit));
    }

    /**
     * 计算下次复习时间（SM-2 算法核心）
     *
     * @param progress 学习进度
     * @param quality  评分: 0-5 (0=完全忘记, 5=完美记忆)
     * @return 更新后的学习进度
     */
    @Transactional
    public LearningProgress calculateNextReview(LearningProgress progress, int quality) {
        // 质量评分标准化到 0-5
        quality = Math.max(0, Math.min(5, quality));

        double easeFactor = progress.getEaseFactor();
        int repetitions = progress.getRepetitions();
        int interval = progress.getInterval();
        int lapses = progress.getLapses();

        if (quality >= 3) {
            // 回答正确
            repetitions++;
            if (repetitions == 1) {
                interval = 1;
            } else if (repetitions == 2) {
                interval = 6;
            } else {
                interval = (int) Math.round(interval * easeFactor);
            }

            // 更新状态
            if (interval >= MASTERY_THRESHOLD_DAYS) {
                progress.setStatus(STATUS_MASTERED);
            } else {
                progress.setStatus(STATUS_LEARNING);
            }
        } else {
            // 回答错误，重置
            repetitions = 0;
            interval = 1;
            lapses++;
            progress.setStatus(STATUS_LEARNING);
        }

        // 更新难度因子 (SM-2 公式)
        easeFactor = easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        easeFactor = Math.max(MIN_EASE_FACTOR, easeFactor);

        // 计算下次复习时间
        LocalDateTime nextReview = LocalDateTime.now().plusDays(interval);

        progress.setRepetitions(repetitions);
        progress.setEaseFactor(easeFactor);
        progress.setInterval(interval);
        progress.setLapses(lapses);
        progress.setNextReview(nextReview);
        progress.setLastReview(LocalDateTime.now());

        return learningProgressRepository.save(progress);
    }

    /**
     * 获取学习统计
     */
    public LearningStats getStats(User user) {
        long totalLearned = learningProgressRepository.countByUserAndStatusGreaterThan(user, STATUS_NEW);
        long mastered = learningProgressRepository.countByUserAndStatus(user, STATUS_MASTERED);
        long dueToday = learningProgressRepository.countByUserAndNextReviewBefore(user, LocalDateTime.now());

        return new LearningStats(totalLearned, mastered, dueToday);
    }

    /**
     * 获取热力图数据（最近一年）
     */
    public List<Object[]> getHeatmapData(User user) {
        LocalDateTime since = LocalDateTime.now().minusYears(1);
        return learningProgressRepository.getHeatmapData(user.getId(), since);
    }

    /**
     * 获取分类掌握度
     */
    public List<Object[]> getCategoryMastery(User user) {
        return learningProgressRepository.getCategoryMastery(user);
    }

    /**
     * 学习统计 DTO
     */
    public record LearningStats(long totalLearned, long mastered, long dueToday) {}
}
