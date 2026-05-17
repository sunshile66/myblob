package com.myblob.module.interactions.entity;

import com.myblob.common.BaseEntity;
import com.myblob.module.accounts.entity.User;
import com.myblob.module.blog.entity.Post;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "blog_post_like", indexes = {
        @Index(name = "idx_post_like_post", columnList = "post_id"),
        @Index(name = "idx_post_like_user", columnList = "user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uniq_user_post_like", columnNames = {"user_id", "post_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PostLike extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}
