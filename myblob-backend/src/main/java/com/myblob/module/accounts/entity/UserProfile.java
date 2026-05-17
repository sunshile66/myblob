package com.myblob.module.accounts.entity;

import com.myblob.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "accounts_profile", indexes = {
        @Index(name = "idx_profile_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 200)
    private String website;

    @Column(length = 100)
    private String company;

    @Column(length = 100)
    private String profession;

    @Column(length = 100)
    private String location;

    @Column(length = 20)
    private String phone;

    @Column(length = 50)
    private String wechat;

    @Column(length = 20)
    private String qq;

    @Column(length = 100)
    private String weibo;
}
