package com.myblob.module.comments.controller;

import com.myblob.common.ApiResponse;
import com.myblob.module.comments.dto.CommentDTO;
import com.myblob.module.comments.dto.CreateCommentRequest;
import com.myblob.module.comments.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/comments/")
    public ResponseEntity<ApiResponse<List<CommentDTO>>> getComments(@RequestParam Long post) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getCommentsByPost(post)));
    }

    @PostMapping("/comments/")
    public ResponseEntity<ApiResponse<CommentDTO>> createComment(@Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(commentService.createComment(request)));
    }

    @PostMapping("/comments/{id}/like/")
    public ResponseEntity<ApiResponse<Void>> likeComment(@PathVariable Long id) {
        commentService.likeComment(id);
        return ResponseEntity.ok(ApiResponse.success("点赞成功", null));
    }

    @DeleteMapping("/comments/{id}/like/")
    public ResponseEntity<ApiResponse<Void>> unlikeComment(@PathVariable Long id) {
        commentService.unlikeComment(id);
        return ResponseEntity.ok(ApiResponse.success("取消点赞成功", null));
    }

    @PostMapping("/comments/{id}/react/")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> reactToComment(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        Map<String, Integer> reactions = commentService.reactToComment(id, body.get("emoji"));
        return ResponseEntity.ok(ApiResponse.success("表情反应成功", reactions));
    }

    @DeleteMapping("/comments/{id}/react/")
    public ResponseEntity<ApiResponse<Void>> unreactToComment(@PathVariable Long id) {
        commentService.unreactToComment(id);
        return ResponseEntity.ok(ApiResponse.success("取消表情反应成功", null));
    }

    @GetMapping("/emojis/")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getEmojis(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getEmojis(category)));
    }

    @GetMapping("/stickers/")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStickers(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean is_gif) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getStickers(category, is_gif)));
    }

    @GetMapping("/reaction-emojis/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReactionEmojis() {
        List<List<Object>> emojis = List.of(
                List.of("👍", "赞"),
                List.of("❤️", "喜欢"),
                List.of("😂", "大笑"),
                List.of("😮", "惊讶"),
                List.of("😢", "难过"),
                List.of("😡", "愤怒")
        );
        return ResponseEntity.ok(ApiResponse.success(Map.of("emojis", emojis)));
    }
}
