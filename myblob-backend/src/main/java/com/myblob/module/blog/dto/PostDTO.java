package com.myblob.module.blog.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.myblob.module.accounts.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostDTO {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String cover;
    private MediaAssetDTO video;
    private String postType;
    private String status;
    private Boolean top;
    private Boolean allowComment;
    private Boolean original;
    private Integer viewCount;
    private Integer commentCount;
    private Integer likeCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDTO author;
    private CategoryDTO category;
    private List<TagDTO> tags;

    @JsonProperty("is_liked")
    private Boolean isLiked;

    @JsonProperty("is_favorited")
    private Boolean isFavorited;
}
