package com.myblob.module.knowledge.service;

import com.myblob.module.knowledge.entity.VocabularyItem;
import com.myblob.module.knowledge.repository.VocabularyItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 词汇种子数据：从 classpath:vocabulary/*.json 加载海量词汇数据（53,000+ 词）。
 * 首次启动时分批导入数据库，后续启动根据 category 计数跳过。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VocabularySeeder {

    private static final int BATCH_SIZE = 2000;

    private final VocabularyItemRepository repo;
    private final VocabularyDataLoader loader;

    @PostConstruct
    public void seed() {
        long totalCount = repo.count();
        if (totalCount > 10000) {
            log.info("Vocabulary already seeded ({} words total), skip", totalCount);
            return;
        }

        seedCategory("CET4", "cet4.json");
        seedCategory("CET6", "cet6.json");
        seedCategory("IELTS", "ielts.json");
        seedCategory("TOEFL", "toefl.json");
        seedCategory("考研", "kaoyan.json");
        seedCategory("GRE", "gre.json");
        seedCategory("商务英语", "biz.json");

        log.info("Vocabulary seeding complete. Total words: {}", repo.count());
    }

    private void seedCategory(String category, String fileName) {
        long catCount = repo.countByCategory(category);
        if (catCount > 500) {
            log.info("Category '{}' already has {} words, skip", category, catCount);
            return;
        }
        List<VocabularyItem> items = loader.loadCategory(category, fileName);
        if (items.isEmpty()) {
            return;
        }
        // 分批保存，避免单次事务过大
        for (int i = 0; i < items.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, items.size());
            List<VocabularyItem> batch = items.subList(i, end);
            repo.saveAll(batch);
            log.debug("Seeded {} batch {}-{}/{}", category, i, end, items.size());
        }
        log.info("Seeded {}: {} words", category, items.size());
    }
}
