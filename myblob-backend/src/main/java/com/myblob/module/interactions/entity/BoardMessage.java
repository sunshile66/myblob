package com.myblob.module.interactions.entity;

import com.myblob.common.BaseEntity;
import com.myblob.module.accounts.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "board_message", indexes = {
        @Index(name = "idx_board_user", columnList = "user_id"),
        @Index(name = "idx_board_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BoardMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 254)
    private String email;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("true")
    private Boolean isPublic = true;
}
