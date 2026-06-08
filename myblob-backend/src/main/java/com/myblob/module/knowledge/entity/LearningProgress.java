package com.myblob.module.knowledge.entity;

import com.myblob.common.BaseEntity;
import com.myblob.module.accounts.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 学习进度实体 - 用于间隔重复算法（SM-2）
 */
@Entity
@Table(name = "learning_progress", indexes = {
        @Index(name = "idx_learning_progress_user", columnList = "user_id"),
        @Index(name = "idx_learning_progress_next_review", columnList = "next_review"),
        @Index(name = "idx_learning_progress_item", columnList = "item_type, item_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LearningProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 条目类型: knowledge / vocabulary */
    @Column(name = "item_type", nullable = false, length = 50)
    private String itemType;

    /** 条目ID */
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    /** 连续正确次数 */
    @Column(nullable = false)
    @Builder.Default
    private Integer repetitions = 0;

    /** 难度因子，初始 2.5 */
    @Column(name = "ease_factor", nullable = false)
    @Builder.Default
    private Double easeFactor = 2.5;

    /** 间隔天数 */
    @Column(nullable = false)
    @Builder.Default
    private Integer interval = 0;

    /** 遗忘次数 */
    @Column(nullable = false)
    @Builder.Default
    private Integer lapses = 0;

    /** 下次复习时间 */
    @Column(name = "next_review")
    private LocalDateTime nextReview;

    /** 上次复习时间 */
    @Column(name = "last_review")
    private LocalDateTime lastReview;

    /** 状态: 0=新卡片 1=学习中 2=已掌握 */
    @Column(nullable = false)
    @Builder.Default
    private Integer status = 0;
}
