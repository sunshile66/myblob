package com.myblob.module.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreatePostRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不超过200个字符")
    private String title;

    private String slug;
    private String summary;
    private String content;
    private String cover;
    private Long videoId;
    private String postType;
    private Long categoryId;
    private List<String> tags;
    private Boolean allowComment;
    private Boolean original;
    private String status;
}
