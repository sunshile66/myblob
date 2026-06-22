package com.myblob.module.news.service;

import com.myblob.module.news.entity.NewsItem;
import com.myblob.module.news.entity.NewsSource;
import com.myblob.module.news.repository.NewsItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 新闻条目批量保存器
 * 负责去重和批量保存
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NewsItemSaver {

    private final NewsItemRepository newsItemRepository;

    /**
     * 批量保存新条目（自动去重）
     */
    @Transactional
    public int saveNewItems(List<NewsItem> items, NewsSource source) {
        if (items.isEmpty()) {
            return 0;
        }

        // 批量查询已存在的 URL
        List<String> urls = items.stream().map(NewsItem::getSourceUrl).toList();
        Set<String> existingUrls = new HashSet<>(newsItemRepository.findExistingSourceUrls(urls));

        // 过滤出新条目
        List<NewsItem> newItems = items.stream()
                .filter(item -> !existingUrls.contains(item.getSourceUrl()))
                .toList();

        // 批量保存
        if (!newItems.isEmpty()) {
            newsItemRepository.saveAll(newItems);
        }

        return newItems.size();
    }
}
