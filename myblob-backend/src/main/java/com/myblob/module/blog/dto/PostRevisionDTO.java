package com.myblob.module.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostRevisionDTO {

    private Long id;
    private String title;
    private String summary;
    private String content;
    private String editorName;
    private LocalDateTime createdAt;
}
