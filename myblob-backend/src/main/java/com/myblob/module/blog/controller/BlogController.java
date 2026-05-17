package com.myblob.module.blog.controller;

import com.myblob.common.ApiResponse;
import com.myblob.common.PageResponse;
import com.myblob.module.blog.dto.*;
import com.myblob.module.blog.service.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @GetMapping("/categories/")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(blogService.getCategories()));
    }

    @GetMapping("/tags/")
    public ResponseEntity<ApiResponse<List<TagDTO>>> getTags() {
        return ResponseEntity.ok(ApiResponse.success(blogService.getTags()));
    }

    @GetMapping("/posts/")
    public ResponseEntity<ApiResponse<PageResponse<PostDTO>>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String ordering) {
        return ResponseEntity.ok(ApiResponse.success(blogService.getPosts(page, size, category, tag, search, status, ordering)));
    }

    @GetMapping("/posts/{slug}/")
    public ResponseEntity<ApiResponse<PostDTO>> getPost(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(blogService.getPostBySlug(slug)));
    }

    @PostMapping("/posts/")
    public ResponseEntity<ApiResponse<PostDTO>> createPost(@Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(blogService.createPost(request)));
    }

    @PutMapping("/posts/{slug}/")
    public ResponseEntity<ApiResponse<PostDTO>> updatePost(@PathVariable String slug, @RequestBody UpdatePostRequest request) {
        return ResponseEntity.ok(ApiResponse.success(blogService.updatePost(slug, request)));
    }

    @DeleteMapping("/posts/{slug}/")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable String slug) {
        blogService.deletePost(slug);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @PostMapping("/posts/{slug}/like/")
    public ResponseEntity<ApiResponse<Void>> likePost(@PathVariable String slug) {
        blogService.likePost(slug);
        return ResponseEntity.ok(ApiResponse.success("点赞成功", null));
    }

    @DeleteMapping("/posts/{slug}/like/")
    public ResponseEntity<ApiResponse<Void>> unlikePost(@PathVariable String slug) {
        blogService.unlikePost(slug);
        return ResponseEntity.ok(ApiResponse.success("取消点赞成功", null));
    }

    @PostMapping("/posts/{slug}/favorite/")
    public ResponseEntity<ApiResponse<Void>> favoritePost(@PathVariable String slug) {
        blogService.favoritePost(slug);
        return ResponseEntity.ok(ApiResponse.success("收藏成功", null));
    }

    @DeleteMapping("/posts/{slug}/favorite/")
    public ResponseEntity<ApiResponse<Void>> unfavoritePost(@PathVariable String slug) {
        blogService.unfavoritePost(slug);
        return ResponseEntity.ok(ApiResponse.success("取消收藏成功", null));
    }

    @GetMapping("/posts/my-posts/")
    public ResponseEntity<ApiResponse<PageResponse<PostDTO>>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(blogService.getMyPosts(page, size, status)));
    }
}
