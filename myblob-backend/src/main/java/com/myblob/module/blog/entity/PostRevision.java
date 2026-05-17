package com.myblob.module.blog.entity;

import com.myblob.common.BaseEntity;
import com.myblob.module.accounts.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "blog_post_revision", indexes = {
        @Index(name = "idx_revision_post", columnList = "post_id"),
        @Index(name = "idx_revision_editor", columnList = "editor_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PostRevision extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editor_id")
    private User editor;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 300)
    private String summary;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
}
