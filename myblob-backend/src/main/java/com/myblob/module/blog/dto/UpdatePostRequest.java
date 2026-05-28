package com.myblob.module.blog.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdatePostRequest {

    @Size(max = 200, message = "标题最长200字符")
    private String title;
    @Size(max = 200, message = "Slug最长200字符")
    private String slug;
    @Size(max = 500, message = "摘要最长500字符")
    private String summary;
    @Size(max = 50000, message = "内容最长50000字符")
    private String content;
    @Size(max = 500, message = "封面URL最长500字符")
    private String cover;
    private Long videoId;
    private String postType;
    private Long categoryId;
    private List<String> tags;
    private Boolean allowComment;
    private Boolean original;
    private Boolean top;
    private String status;
}
