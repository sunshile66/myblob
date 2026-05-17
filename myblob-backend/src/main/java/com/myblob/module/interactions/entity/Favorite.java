package com.myblob.module.interactions.entity;

import com.myblob.common.BaseEntity;
import com.myblob.module.accounts.entity.User;
import com.myblob.module.blog.entity.Post;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "blog_favorite", indexes = {
        @Index(name = "idx_favorite_post", columnList = "post_id"),
        @Index(name = "idx_favorite_user", columnList = "user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uniq_user_post_favorite", columnNames = {"user_id", "post_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Favorite extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}
