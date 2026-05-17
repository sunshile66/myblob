package com.myblob.module.blog.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdatePostRequest {

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
    private Boolean top;
    private String status;
}
