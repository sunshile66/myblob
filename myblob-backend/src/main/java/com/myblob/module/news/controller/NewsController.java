package com.myblob.module.news.controller;

import com.myblob.common.ApiResponse;
import com.myblob.common.PageResponse;
import com.myblob.module.news.dto.NewsItemDTO;
import com.myblob.module.news.dto.NewsSourceDTO;
import com.myblob.module.news.entity.NewsItem;
import com.myblob.module.news.repository.NewsItemRepository;
import com.myblob.module.news.repository.NewsSourceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "新闻聚合", description = "多平台新闻聚合公开接口")
public class NewsController {

    private final NewsItemRepository newsItemRepository;
    private final NewsSourceRepository newsSourceRepository;

    @GetMapping("/")
    @Operation(summary = "获取新闻列表")
    public ResponseEntity<ApiResponse<PageResponse<NewsItemDTO>>> getNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int minScore) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "qualityScore", "publishedAt"));
        Page<NewsItem> items;

        if (search != null && !search.isBlank()) {
            items = newsItemRepository.searchNews(search, pageable);
        } else if (category != null && !category.isBlank() && language != null && !language.isBlank()) {
            items = newsItemRepository.findByCategoryAndLanguage(category, language, pageable);
        } else if (category != null && !category.isBlank()) {
            items = newsItemRepository.findByCategory(category, pageable);
        } else if (source != null && !source.isBlank()) {
            items = newsItemRepository.findBySourcePlatform(source, pageable);
        } else if (language != null && !language.isBlank()) {
            items = newsItemRepository.findByLanguage(language, pageable);
        } else {
            items = newsItemRepository.findPublishedNews(pageable);
        }

        Page<NewsItemDTO> dtoPage = items.map(NewsItemDTO::from);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(dtoPage)));
    }

    @GetMapping("/{id}/")
    @Operation(summary = "获取新闻详情")
    public ResponseEntity<ApiResponse<NewsItemDTO>> getNewsDetail(@PathVariable Long id) {
        return newsItemRepository.findById(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success(NewsItemDTO.from(item))))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "新闻不存在")));
    }

    @GetMapping("/sources/")
    @Operation(summary = "获取启用的新闻源列表")
    public ResponseEntity<ApiResponse<List<NewsSourceDTO>>> getSources() {
        List<NewsSourceDTO> sources = newsSourceRepository.findByEnabledTrueOrderByPriorityAsc()
                .stream().map(NewsSourceDTO::from).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(sources));
    }

    @GetMapping("/categories/")
    @Operation(summary = "获取分类列表")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        List<String> categories = List.of("官方媒体", "社交媒体", "科技财经", "科技媒体", "开源开发者");
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}
