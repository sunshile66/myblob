package com.myblob.module.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaAssetDTO {

    private Long id;
    private String file;
    private String mediaType;
    private String title;
    private String altText;
}
