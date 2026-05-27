package com.myblob.module.blog.controller;

import com.myblob.common.ApiResponse;
import com.myblob.common.PageResponse;
import com.myblob.module.blog.dto.*;
import com.myblob.module.blog.service.BlogService;
import com.myblob.module.blog.service.PostInteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
@Tag(name = "博客管理", description = "文章、分类、标签等博客核心接口")
public class BlogController {

    private final BlogService blogService;
    private final PostInteractionService postInteractionService;

    @GetMapping("/categories/")
    @Operation(summary = "获取分类列表")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(blogService.getCategories()));
    }

    @GetMapping("/tags/")
    @Operation(summary = "获取标签列表")
    public ResponseEntity<ApiResponse<List<TagDTO>>> getTags() {
        return ResponseEntity.ok(ApiResponse.success(blogService.getTags()));
    }

    @GetMapping("/posts/")
    @Operation(summary = "获取文章列表", description = "支持分类、标签、搜索、状态、排序筛选")
    public ResponseEntity<ApiResponse<PageResponse<PostDTO>>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String ordering) {
        return ResponseEntity
                .ok(ApiResponse.success(blogService.getPosts(page, size, category, tag, search, status, ordering)));
    }

    @GetMapping("/posts/{slug}/")
    @Operation(summary = "获取文章详情")
    public ResponseEntity<ApiResponse<PostDTO>> getPost(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(blogService.getPostBySlug(slug)));
    }

    @PostMapping("/posts/")
    @Operation(summary = "创建文章")
    public ResponseEntity<ApiResponse<PostDTO>> createPost(@Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(blogService.createPost(request)));
    }

    @PutMapping("/posts/{slug}/")
    @Operation(summary = "更新文章")
    public ResponseEntity<ApiResponse<PostDTO>> updatePost(@PathVariable String slug,
            @Valid @RequestBody UpdatePostRequest request) {
        return ResponseEntity.ok(ApiResponse.success(blogService.updatePost(slug, request)));
    }

    @DeleteMapping("/posts/{slug}/")
    @Operation(summary = "删除文章")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable String slug) {
        blogService.deletePost(slug);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @PostMapping("/posts/{slug}/like/")
    @Operation(summary = "点赞文章")
    public ResponseEntity<ApiResponse<Void>> likePost(@PathVariable String slug) {
        postInteractionService.likePost(slug);
        return ResponseEntity.ok(ApiResponse.success("点赞成功", null));
    }

    @DeleteMapping("/posts/{slug}/like/")
    @Operation(summary = "取消点赞")
    public ResponseEntity<ApiResponse<Void>> unlikePost(@PathVariable String slug) {
        postInteractionService.unlikePost(slug);
        return ResponseEntity.ok(ApiResponse.success("取消点赞成功", null));
    }

    @PostMapping("/posts/{slug}/favorite/")
    @Operation(summary = "收藏文章")
    public ResponseEntity<ApiResponse<Void>> favoritePost(@PathVariable String slug) {
        postInteractionService.favoritePost(slug);
        return ResponseEntity.ok(ApiResponse.success("收藏成功", null));
    }

    @DeleteMapping("/posts/{slug}/favorite/")
    @Operation(summary = "取消收藏")
    public ResponseEntity<ApiResponse<Void>> unfavoritePost(@PathVariable String slug) {
        postInteractionService.unfavoritePost(slug);
        return ResponseEntity.ok(ApiResponse.success("取消收藏成功", null));
    }

    @GetMapping("/posts/my-posts/")
    @Operation(summary = "获取我的文章列表")
    public ResponseEntity<ApiResponse<PageResponse<PostDTO>>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(blogService.getMyPosts(page, size, status)));
    }

    @GetMapping("/posts/{slug}/revisions/")
    @Operation(summary = "获取文章版本历史")
    public ResponseEntity<ApiResponse<List<PostRevisionDTO>>> getRevisions(@PathVariable String slug) {
        PostDTO post = blogService.getPostBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(blogService.getRevisions(post.getId())));
    }
}
