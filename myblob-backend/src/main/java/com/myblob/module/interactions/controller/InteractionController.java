package com.myblob.module.interactions.controller;

import com.myblob.common.ApiResponse;
import com.myblob.common.PageResponse;
import com.myblob.module.blog.dto.PostDTO;
import com.myblob.module.interactions.dto.BoardMessageDTO;
import com.myblob.module.interactions.dto.CreateBoardMessageRequest;
import com.myblob.module.interactions.dto.NotificationDTO;
import com.myblob.module.interactions.service.InteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
@Tag(name = "互动管理", description = "留言板、通知、收藏列表相关接口")
public class InteractionController {

    private final InteractionService interactionService;

    @GetMapping("/board-messages/")
    @Operation(summary = "获取留言板消息列表")
    public ResponseEntity<ApiResponse<PageResponse<BoardMessageDTO>>> getBoardMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(interactionService.getBoardMessages(page, size)));
    }

    @PostMapping("/board-messages/")
    @Operation(summary = "发布留言板消息")
    public ResponseEntity<ApiResponse<BoardMessageDTO>> createBoardMessage(
            @Valid @RequestBody CreateBoardMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(interactionService.createBoardMessage(request)));
    }

    @GetMapping("/notifications/")
    @Operation(summary = "获取通知列表")
    public ResponseEntity<ApiResponse<PageResponse<NotificationDTO>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(interactionService.getNotifications(page, size)));
    }

    @GetMapping("/notifications/unread-count/")
    @Operation(summary = "获取未读通知数量")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", interactionService.getUnreadNotificationCount())));
    }

    @PostMapping("/notifications/mark-all-read/")
    @Operation(summary = "全部标记为已读")
    public ResponseEntity<ApiResponse<Void>> markAllRead() {
        interactionService.markAllNotificationsRead();
        return ResponseEntity.ok(ApiResponse.success("已全部标记为已读", null));
    }

    @GetMapping("/favorites/")
    @Operation(summary = "获取我的收藏列表")
    public ResponseEntity<ApiResponse<PageResponse<PostDTO>>> getMyFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(interactionService.getMyFavorites(page, size)));
    }
}
