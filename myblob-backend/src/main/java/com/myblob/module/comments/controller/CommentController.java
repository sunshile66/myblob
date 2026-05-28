package com.myblob.module.comments.controller;

import com.myblob.common.ApiResponse;
import com.myblob.module.comments.dto.CommentDTO;
import com.myblob.module.comments.dto.CreateCommentRequest;
import com.myblob.module.comments.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "评论管理", description = "评论发布、点赞、表情反应相关接口")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/comments/")
    @Operation(summary = "获取文章评论列表")
    public ResponseEntity<ApiResponse<List<CommentDTO>>> getComments(@RequestParam Long post) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getCommentsByPost(post)));
    }

    @PostMapping("/comments/")
    @Operation(summary = "发表评论")
    public ResponseEntity<ApiResponse<CommentDTO>> createComment(@Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(commentService.createComment(request)));
    }

    @PostMapping("/comments/{id}/like/")
    @Operation(summary = "点赞评论")
    public ResponseEntity<ApiResponse<Void>> likeComment(@PathVariable Long id) {
        commentService.likeComment(id);
        return ResponseEntity.ok(ApiResponse.success("点赞成功", null));
    }

    @DeleteMapping("/comments/{id}/like/")
    @Operation(summary = "取消点赞评论")
    public ResponseEntity<ApiResponse<Void>> unlikeComment(@PathVariable Long id) {
        commentService.unlikeComment(id);
        return ResponseEntity.ok(ApiResponse.success("取消点赞成功", null));
    }

    @PostMapping("/comments/{id}/react/")
    @Operation(summary = "对评论发表表情反应")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> reactToComment(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        Map<String, Integer> reactions = commentService.reactToComment(id, body.get("emoji"));
        return ResponseEntity.ok(ApiResponse.success("表情反应成功", reactions));
    }

    @DeleteMapping("/comments/{id}/react/")
    @Operation(summary = "取消表情反应")
    public ResponseEntity<ApiResponse<Void>> unreactToComment(@PathVariable Long id) {
        commentService.unreactToComment(id);
        return ResponseEntity.ok(ApiResponse.success("取消表情反应成功", null));
    }

    @PutMapping("/comments/{id}/")
    @Operation(summary = "编辑评论")
    public ResponseEntity<ApiResponse<CommentDTO>> updateComment(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("评论内容不能为空"));
        }
        return ResponseEntity.ok(ApiResponse.success(commentService.updateComment(id, content)));
    }

    @DeleteMapping("/comments/{id}/")
    @Operation(summary = "删除评论")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(ApiResponse.success("评论已删除", null));
    }

    @GetMapping("/emojis/")
    @Operation(summary = "获取表情列表")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getEmojis(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getEmojis(category)));
    }

    @GetMapping("/stickers/")
    @Operation(summary = "获取贴纸列表")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStickers(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean is_gif) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getStickers(category, is_gif)));
    }

    @GetMapping("/reaction-emojis/")
    @Operation(summary = "获取表情反应预设列表")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReactionEmojis() {
        List<List<Object>> emojis = List.of(
                List.of("👍", "赞"),
                List.of("❤️", "喜欢"),
                List.of("😂", "大笑"),
                List.of("😮", "惊讶"),
                List.of("😢", "难过"),
                List.of("😡", "愤怒"));
        return ResponseEntity.ok(ApiResponse.success(Map.of("emojis", emojis)));
    }
}
