package com.myblob.module.accounts.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;

    @JsonProperty("is_email_verified")
    private Boolean emailVerified;

    private String role;

    @JsonProperty("is_superuser")
    private Boolean isSuperuser;

    private String bio;
    private String website;
    private String company;
    private String profession;
    private String location;
    private String phone;
    private String wechat;
    private String qq;
    private String weibo;

    @JsonProperty("is_following")
    private Boolean isFollowing;

    @JsonProperty("follower_count")
    private Long followerCount;

    @JsonProperty("following_count")
    private Long followingCount;

    @JsonProperty("post_count")
    private Long postCount;
}
