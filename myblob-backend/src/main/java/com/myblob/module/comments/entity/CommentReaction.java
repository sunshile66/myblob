package com.myblob.module.comments.entity;

import com.myblob.common.BaseEntity;
import com.myblob.module.accounts.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "comment_reaction", indexes = {
        @Index(name = "idx_reaction_user", columnList = "user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uniq_comment_user_reaction", columnNames = {"comment_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CommentReaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 10)
    private String emoji;
}
