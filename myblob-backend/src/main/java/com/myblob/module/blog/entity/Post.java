package com.myblob.module.blog.entity;

import com.myblob.common.BaseEntity;
import com.myblob.module.accounts.entity.User;
import com.myblob.module.media.entity.MediaAsset;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "blog_post", indexes = {
        @Index(name = "idx_post_status_published", columnList = "status, published_at"),
        @Index(name = "idx_post_author_status", columnList = "author_id, status"),
        @Index(name = "idx_post_category_status", columnList = "category_id, status"),
        @Index(name = "idx_post_slug", columnList = "slug"),
        @Index(name = "idx_post_type", columnList = "post_type"),
        @Index(name = "idx_post_top", columnList = "is_top, published_at"),
        @Index(name = "idx_post_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Post extends BaseEntity {

    public enum Status {
        DRAFT, REVIEW, PUBLISHED, HIDDEN
    }

    public enum PostType {
        ARTICLE, NOTE, VIDEO
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(unique = true, nullable = false, length = 220)
    private String slug;

    @Column(length = 300)
    private String summary;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(length = 500)
    private String cover;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id")
    private MediaAsset video;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 20)
    @Builder.Default
    private PostType postType = PostType.NOTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.DRAFT;

    @Column(name = "is_original", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("true")
    private Boolean original = true;

    @Column(name = "allow_comment", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("true")
    private Boolean allowComment = true;

    @Column(name = "is_top", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("false")
    private Boolean top = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("0")
    private Integer viewCount = 0;

    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("0")
    private Integer commentCount = 0;

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("0")
    private Integer likeCount = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "blog_post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @PrePersist
    @PreUpdate
    public void autoPublish() {
        if (this.status == Status.PUBLISHED && this.publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
    }
}
