package com.myblob.module.accounts.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String nickname;
    private String bio;
    private String website;
    private String company;
    private String profession;
    private String location;
    private String phone;
    private String wechat;
    private String qq;
    private String weibo;
}
