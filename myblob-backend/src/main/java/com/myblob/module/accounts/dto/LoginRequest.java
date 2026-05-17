package com.myblob.module.accounts.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String username;
    private String password;
    private String phoneOrEmail;
    private String code;
}
