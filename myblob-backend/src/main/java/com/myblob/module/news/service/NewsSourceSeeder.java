package com.myblob.module.news.service;

import com.myblob.module.news.entity.NewsSource;
import com.myblob.module.news.repository.NewsSourceRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 新闻源种子数据初始化
 * 仅插入不存在的新源，不影响已有数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NewsSourceSeeder {

    private final NewsSourceRepository newsSourceRepository;

    @PostConstruct
    public void seedDefaultSources() {
        seedSources(NewsSourceData.getGeneralSources(), "通用");
        seedSources(NewsSourceData.getAirlineSources(), "航空公司");
        seedSources(NewsSourceData.getSupplementarySources(), "补充");
        disableFailedSources();
    }

    /**
     * 批量插入不存在的新闻源
     */
    private void seedSources(List<NewsSource> candidates, String category) {
        List<NewsSource> toAdd = new ArrayList<>();
        for (NewsSource candidate : candidates) {
            if (newsSourceRepository.findByName(candidate.getName()) == null) {
                toAdd.add(candidate);
            }
        }
        if (!toAdd.isEmpty()) {
            newsSourceRepository.saveAll(toAdd);
            log.info("Added {} {} news sources: {}", toAdd.size(), category,
                    toAdd.stream().map(NewsSource::getName).toList());
        }
    }

    /**
     * 禁用已失效或国内无法访问的源
     */
    private void disableFailedSources() {
        List<String> failedNames = NewsSourceData.getFailedSourceNames();
        List<String> failedPrefixes = NewsSourceData.getFailedPrefixes();
        int disabledCount = 0;

        // 按名称禁用
        for (String name : failedNames) {
            NewsSource source = newsSourceRepository.findByName(name);
            if (source != null && Boolean.TRUE.equals(source.getEnabled())) {
                source.setEnabled(false);
                newsSourceRepository.save(source);
                disabledCount++;
                log.info("Disabled failed source: {}", name);
            }
        }

        // 按前缀禁用
        for (NewsSource source : newsSourceRepository.findAll()) {
            if (Boolean.TRUE.equals(source.getEnabled())) {
                for (String prefix : failedPrefixes) {
                    if (source.getName().startsWith(prefix)) {
                        source.setEnabled(false);
                        newsSourceRepository.save(source);
                        disabledCount++;
                        log.info("Disabled inaccessible source: {}", source.getName());
                        break;
                    }
                }
            }
        }

        if (disabledCount > 0) {
            log.info("Disabled {} failed/inaccessible news sources", disabledCount);
        }
    }
}
