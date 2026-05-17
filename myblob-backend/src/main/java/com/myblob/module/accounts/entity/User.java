package com.myblob.module.accounts.entity;

import com.myblob.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "accounts_user", indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends BaseEntity {

    @Column(unique = true, nullable = false, length = 150)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 30)
    private String nickname;

    @Column(length = 500)
    private String avatar;

    @Column(name = "is_email_verified", nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(nullable = false)
    @ColumnDefault("true")
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "is_superuser", nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private Boolean superuser = false;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile profile;
}
