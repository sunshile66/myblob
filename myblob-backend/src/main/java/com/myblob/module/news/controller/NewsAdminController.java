package com.myblob.module.news.controller;

import com.myblob.common.ApiResponse;
import com.myblob.common.PageResponse;
import com.myblob.module.news.dto.NewsItemDTO;
import com.myblob.module.news.dto.NewsSourceDTO;
import com.myblob.module.news.entity.NewsItem;
import com.myblob.module.news.entity.NewsSource;
import com.myblob.module.news.repository.NewsItemRepository;
import com.myblob.module.news.repository.NewsSourceRepository;
import com.myblob.module.news.service.NewsFetchService;
import com.myblob.module.news.service.NewsFilterService;
import com.myblob.module.news.service.NewsProxyConfig;
import com.myblob.module.news.service.NewsSchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/news")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "新闻管理", description = "新闻聚合后台管理接口")
public class NewsAdminController {

    private final NewsSourceRepository newsSourceRepository;
    private final NewsItemRepository newsItemRepository;
    private final NewsFetchService newsFetchService;
    private final NewsFilterService newsFilterService;
    private final NewsProxyConfig newsProxyConfig;
    private final NewsSchedulerService newsSchedulerService;

    // ===== Global Toggle =====

    @GetMapping("/global-status/")
    @Operation(summary = "获取全局开关状态")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> getGlobalStatus() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("enabled", newsProxyConfig.getGlobal().isEnabled())));
    }

    @PostMapping("/global-toggle/")
    @Operation(summary = "一键开关新闻聚合")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleGlobal() {
        boolean ns = !newsProxyConfig.getGlobal().isEnabled();
        newsProxyConfig.getGlobal().setEnabled(ns);
        return ResponseEntity.ok(ApiResponse.success(Map.of("enabled", ns)));
    }

    // ===== Stats =====

    @GetMapping("/stats/")
    @Operation(summary = "聚合统计")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalItems", newsItemRepository.count());
        stats.put("activeItems", newsItemRepository.countByIsFilteredFalse());
        stats.put("totalSources", newsSourceRepository.count());
        stats.put("enabledSources", newsSourceRepository.findByEnabledTrueOrderByPriorityAsc().size());
        stats.put("globalEnabled", newsProxyConfig.getGlobal().isEnabled());
        stats.put("fetchRunning", newsSchedulerService.isFetchRunning());
        stats.put("lastFetchCount", newsSchedulerService.getLastFetchedCount());
        stats.put("lastFetchDurationMs", newsSchedulerService.getLastFetchDurationMs());
        stats.put("lastFetchTime", newsSchedulerService.getLastFetchTime());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ===== Source Management =====

    @GetMapping("/sources/")
    @Operation(summary = "获取全部新闻源")
    public ResponseEntity<ApiResponse<List<NewsSourceDTO>>> getSources() {
        List<NewsSourceDTO> list = newsSourceRepository.findAllByOrderByPlatformNameAsc()
                .stream().map(NewsSourceDTO::from).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/sources/")
    @Operation(summary = "新增新闻源")
    public ResponseEntity<ApiResponse<NewsSourceDTO>> createSource(@RequestBody NewsSource src) {
        src.setId(null);
        return ResponseEntity.ok(ApiResponse.success(NewsSourceDTO.from(newsSourceRepository.save(src))));
    }

    @PutMapping("/sources/{id}/")
    @Operation(summary = "编辑新闻源")
    public ResponseEntity<ApiResponse<NewsSourceDTO>> updateSource(@PathVariable Long id, @RequestBody NewsSource src) {
        return newsSourceRepository.findById(id).map(ex -> {
            ex.setName(src.getName()); ex.setPlatformName(src.getPlatformName());
            ex.setFeedUrl(src.getFeedUrl()); ex.setFetchMethod(src.getFetchMethod());
            ex.setCategory(src.getCategory()); ex.setLanguage(src.getLanguage());
            ex.setEnabled(src.getEnabled()); ex.setPriority(src.getPriority());
            ex.setFetchIntervalSeconds(src.getFetchIntervalSeconds());
            return ResponseEntity.ok(ApiResponse.success(NewsSourceDTO.from(newsSourceRepository.save(ex))));
        }).orElse(ResponseEntity.ok(ApiResponse.error(404, "新闻源不存在")));
    }

    @DeleteMapping("/sources/{id}/")
    @Operation(summary = "删除新闻源")
    public ResponseEntity<ApiResponse<Void>> deleteSource(@PathVariable Long id) {
        newsSourceRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @PatchMapping("/sources/{id}/toggle/")
    @Operation(summary = "启用/禁用")
    public ResponseEntity<ApiResponse<NewsSourceDTO>> toggleSource(@PathVariable Long id) {
        return newsSourceRepository.findById(id).map(s -> {
            s.setEnabled(!s.getEnabled());
            return ResponseEntity.ok(ApiResponse.success(NewsSourceDTO.from(newsSourceRepository.save(s))));
        }).orElse(ResponseEntity.ok(ApiResponse.error(404, "新闻源不存在")));
    }

    @PostMapping("/sources/{id}/test-fetch/")
    @Operation(summary = "测试抓取")
    public ResponseEntity<ApiResponse<String>> testFetch(@PathVariable Long id) {
        return newsSourceRepository.findById(id).map(s -> {
            int n = newsFetchService.fetchSource(s);
            return ResponseEntity.ok(ApiResponse.success("完成，获取 " + n + " 条"));
        }).orElse(ResponseEntity.ok(ApiResponse.error(404, "新闻源不存在")));
    }

    // ===== Content Management =====

    @GetMapping("/items/")
    @Operation(summary = "获取全部新闻（含被过滤）")
    public ResponseEntity<ApiResponse<PageResponse<NewsItemDTO>>> getItems(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<NewsItemDTO> dtoPage = newsItemRepository.findAllIncludingFiltered(pageable).map(NewsItemDTO::from);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(dtoPage)));
    }

    @PutMapping("/items/{id}/")
    @Operation(summary = "编辑新闻")
    public ResponseEntity<ApiResponse<NewsItemDTO>> updateItem(@PathVariable Long id,
                                                                @RequestBody Map<String, Object> body) {
        return newsItemRepository.findById(id).map(it -> {
            if (body.containsKey("title")) it.setTitle((String) body.get("title"));
            if (body.containsKey("summary")) it.setSummary((String) body.get("summary"));
            if (body.containsKey("content")) it.setContent((String) body.get("content"));
            if (body.containsKey("category")) it.setCategory((String) body.get("category"));
            return ResponseEntity.ok(ApiResponse.success(NewsItemDTO.from(newsItemRepository.save(it))));
        }).orElse(ResponseEntity.ok(ApiResponse.error(404, "新闻不存在")));
    }

    @DeleteMapping("/items/{id}/")
    @Operation(summary = "删除新闻")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        newsItemRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @PostMapping("/items/{id}/unfilter/")
    @Operation(summary = "解除过滤")
    public ResponseEntity<ApiResponse<NewsItemDTO>> unfilterItem(@PathVariable Long id) {
        return newsItemRepository.findById(id).map(it -> {
            it.setIsFiltered(false); it.setFilterReason(null); it.setQualityScore(60);
            return ResponseEntity.ok(ApiResponse.success(NewsItemDTO.from(newsItemRepository.save(it))));
        }).orElse(ResponseEntity.ok(ApiResponse.error(404, "新闻不存在")));
    }

    @PostMapping("/items/batch-delete/")
    @Operation(summary = "批量删除")
    public ResponseEntity<ApiResponse<Void>> batchDelete(@RequestBody Map<String, List<Integer>> body) {
        Optional.ofNullable(body.get("ids")).ifPresent(ids ->
                ids.forEach(id -> newsItemRepository.deleteById(Long.valueOf(id))));
        return ResponseEntity.ok(ApiResponse.success("批量删除成功", null));
    }

    // ===== Fetch Control =====

    @PostMapping("/fetch-now/")
    @Operation(summary = "立即全量抓取")
    public ResponseEntity<ApiResponse<String>> fetchNow() {
        if (!newsProxyConfig.getGlobal().isEnabled()) {
            return ResponseEntity.ok(ApiResponse.error("全局开关已关闭"));
        }
        long start = System.currentTimeMillis();
        int count = newsFetchService.fetchAllSources();
        long ms = System.currentTimeMillis() - start;
        return ResponseEntity.ok(ApiResponse.success("抓取完成: " + count + " 条, 耗时 " + ms + "ms"));
    }

    @GetMapping("/fetch-status/")
    @Operation(summary = "抓取状态")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFetchStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("running", newsSchedulerService.isFetchRunning());
        status.put("lastCount", newsSchedulerService.getLastFetchedCount());
        status.put("lastDurationMs", newsSchedulerService.getLastFetchDurationMs());
        status.put("lastTime", newsSchedulerService.getLastFetchTime());
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
