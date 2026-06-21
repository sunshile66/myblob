package com.myblob.module.knowledge.controller;

import com.myblob.common.ApiResponse;
import com.myblob.common.PageResponse;
import com.myblob.module.accounts.entity.User;
import com.myblob.module.knowledge.entity.KnowledgeItem;
import com.myblob.module.knowledge.entity.LearningProgress;
import com.myblob.module.knowledge.entity.VocabularyItem;
import com.myblob.module.knowledge.repository.KnowledgeItemRepository;
import com.myblob.module.knowledge.repository.VocabularyItemRepository;
import com.myblob.module.knowledge.service.SpacedRepetitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识百科", description = "英语词汇/知识卡片API")
public class KnowledgeController {

    private final VocabularyItemRepository vocabularyItemRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final SpacedRepetitionService spacedRepetitionService;

    // ==================== 词汇接口 ====================

    @GetMapping("/vocabulary")
    @Operation(summary = "获取词汇列表")
    public ResponseEntity<ApiResponse<PageResponse<VocabularyItem>>> getVocabulary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<VocabularyItem> items;

        if (search != null && !search.isBlank()) {
            items = vocabularyItemRepository.search(search, pageable);
        } else if (category != null && !category.isBlank() && difficulty != null) {
            items = vocabularyItemRepository.findByCategoryAndDifficulty(category, difficulty, pageable);
        } else if (category != null && !category.isBlank()) {
            items = vocabularyItemRepository.findByCategory(category, pageable);
        } else if (difficulty != null) {
            items = vocabularyItemRepository.findByDifficulty(difficulty, pageable);
        } else {
            items = vocabularyItemRepository.findAll(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(items)));
    }

    @GetMapping("/vocabulary/random")
    @Operation(summary = "随机获取单词（用于测验）")
    public ResponseEntity<ApiResponse<List<VocabularyItem>>> getRandomVocabulary(
            @RequestParam(defaultValue = "10") int count) {
        List<VocabularyItem> items = vocabularyItemRepository.findRandom(Math.min(count, 100));
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/vocabulary/{id}")
    @Operation(summary = "获取词汇详情")
    public ResponseEntity<ApiResponse<VocabularyItem>> getVocabularyDetail(@PathVariable Long id) {
        return vocabularyItemRepository.findById(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success(item)))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "词汇不存在")));
    }

    @GetMapping("/vocabulary/categories")
    @Operation(summary = "获取词汇分类列表")
    public ResponseEntity<ApiResponse<List<String>>> getVocabularyCategories() {
        List<String> categories = List.of("CET4", "CET6", "IELTS", "TOEFL", "考研", "GRE", "商务英语");
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/vocabulary/count")
    @Operation(summary = "获取词汇总数")
    public ResponseEntity<ApiResponse<Long>> getVocabularyCount() {
        return ResponseEntity.ok(ApiResponse.success(vocabularyItemRepository.count()));
    }

    // ==================== 知识卡片接口 ====================

    @GetMapping("/items")
    @Operation(summary = "获取知识卡片列表")
    public ResponseEntity<ApiResponse<PageResponse<KnowledgeItem>>> getKnowledgeItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "hot") String sort) {

        Sort dbSort = "new".equals(sort)
                ? Sort.by(Sort.Direction.DESC, "createdAt")
                : Sort.by(Sort.Direction.DESC, "viewCount");
        Pageable pageable = PageRequest.of(page, size, dbSort);
        Page<KnowledgeItem> items;

        if (search != null && !search.isBlank()) {
            items = knowledgeItemRepository.fullTextSearch(search.trim(), pageable);
            if (items.isEmpty()) {
                items = knowledgeItemRepository.search(search, pageable);
            }
        } else if (category != null && !category.isBlank()) {
            items = knowledgeItemRepository.findByCategory(category, pageable);
        } else {
            items = knowledgeItemRepository.findAll(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(items)));
    }

    @GetMapping("/items/{id}")
    @Operation(summary = "获取知识卡片详情")
    public ResponseEntity<ApiResponse<KnowledgeItem>> getKnowledgeItemDetail(@PathVariable Long id) {
        return knowledgeItemRepository.findById(id)
                .map(item -> {
                    // 使用原生查询更新浏览计数，避免全量更新
                    knowledgeItemRepository.incrementViewCount(id);
                    item.setViewCount(item.getViewCount() + 1);
                    return ResponseEntity.ok(ApiResponse.success(item));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "知识卡片不存在")));
    }

    @GetMapping("/items/categories")
    @Operation(summary = "获取知识分类列表")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getKnowledgeCategories() {
        List<Map<String, String>> categories = List.of(
                Map.of("key", "programming", "name", "编程技术", "icon", "Monitor"),
                Map.of("key", "math", "name", "数学科学", "icon", "TrendCharts"),
                Map.of("key", "history", "name", "历史人文", "icon", "Clock"),
                Map.of("key", "science", "name", "自然科学", "icon", "Sunny"),
                Map.of("key", "literature", "name", "文学艺术", "icon", "Reading"),
                Map.of("key", "philosophy", "name", "哲学思辨", "icon", "MagicStick"),
                Map.of("key", "business", "name", "商业经济", "icon", "Coin"),
                // 新增高级分类
                Map.of("key", "ai", "name", "人工智能", "icon", "Cpu"),
                Map.of("key", "cybersecurity", "name", "网络安全", "icon", "Lock"),
                Map.of("key", "algorithms", "name", "算法与数据结构", "icon", "DataLine"),
                Map.of("key", "system-design", "name", "系统架构", "icon", "SetUp")
        );
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    // ==================== 间隔重复复习接口 ====================

    @GetMapping("/review/due")
    @Operation(summary = "获取待复习卡片")
    public ResponseEntity<ApiResponse<List<LearningProgress>>> getDueReviews(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "10") int limit) {
        List<LearningProgress> reviews = spacedRepetitionService.getDueReviews(user, Math.min(limit, 50));
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @PostMapping("/review/{id}/grade")
    @Operation(summary = "提交复习评分")
    public ResponseEntity<ApiResponse<LearningProgress>> gradeReview(
            @PathVariable Long id,
            @RequestParam int quality,
            @AuthenticationPrincipal User user) {
        LearningProgress progress = spacedRepetitionService.getOrCreateProgress(user, "knowledge", id);
        LearningProgress updated = spacedRepetitionService.calculateNextReview(progress, quality);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @GetMapping("/review/stats")
    @Operation(summary = "获取学习统计")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReviewStats(
            @AuthenticationPrincipal User user) {
        SpacedRepetitionService.LearningStats stats = spacedRepetitionService.getStats(user);
        List<Object[]> heatmap = spacedRepetitionService.getHeatmapData(user);
        List<Object[]> categoryMastery = spacedRepetitionService.getCategoryMastery(user);

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "totalLearned", stats.totalLearned(),
                "mastered", stats.mastered(),
                "dueToday", stats.dueToday(),
                "heatmap", heatmap,
                "categoryMastery", categoryMastery
        )));
    }
}
