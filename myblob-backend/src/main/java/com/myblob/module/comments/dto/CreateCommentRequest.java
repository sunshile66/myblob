package com.myblob.module.comments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCommentRequest {

    @NotNull(message = "文章ID不能为空")
    private Long postId;

    private Long parentId;

    private Long replyToId;

    private String nickname;

    private String email;

    @NotBlank(message = "内容不能为空")
    private String content;
}
