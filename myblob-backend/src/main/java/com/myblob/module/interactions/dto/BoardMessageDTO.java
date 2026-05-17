package com.myblob.module.interactions.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BoardMessageDTO {

    private Long id;
    private Long userId;
    private String nickname;
    private String email;
    private String content;
    private Boolean isPublic;
    private LocalDateTime createdAt;
}
