package com.myblob.module.knowledge.entity;

import com.myblob.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "knowledge_item", indexes = {
        @Index(name = "idx_knowledge_category", columnList = "category"),
        @Index(name = "idx_knowledge_title", columnList = "title")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class KnowledgeItem extends BaseEntity {

    /** 标题 */
    @Column(nullable = false, length = 500)
    private String title;

    /** 内容，支持 Markdown */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** 分类: programming/math/history/science/literature/philosophy/law/business */
    @Column(nullable = false, length = 50)
    private String category;

    /** 标签，逗号分隔 */
    @Column(length = 500)
    private String tags;

    /** 来源 */
    @Column(length = 200)
    private String source;

    /** 难度 1-5 */
    @Column(nullable = false)
    @Builder.Default
    private Integer difficulty = 1;

    /** 浏览数 */
    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    /** 摘要，用于列表展示 */
    @Column(length = 500)
    private String summary;
}
