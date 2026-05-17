package com.myblob.module.comments.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.myblob.module.accounts.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentDTO {

    private Long id;
    private Long postId;
    private UserDTO user;
    private UserDTO author;
    private Long parentId;
    private UserDTO replyTo;
    private String nickname;
    private String email;
    private String content;
    private Integer likeCount;
    private Boolean isApproved;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private List<CommentDTO> children;
    private Map<String, Integer> reactions;
}
