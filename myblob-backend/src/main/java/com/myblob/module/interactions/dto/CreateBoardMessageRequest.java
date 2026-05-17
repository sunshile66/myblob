package com.myblob.module.interactions.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBoardMessageRequest {

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    private String email;

    @NotBlank(message = "内容不能为空")
    private String content;

    private Boolean isPublic;
}
