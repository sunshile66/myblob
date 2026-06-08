package com.myblob.module.news.service;

import com.myblob.module.news.entity.NewsItem;
import com.myblob.module.news.repository.NewsItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于中文情感词典的轻量情感分析（无需外部 ML 模型）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsSentimentService {

    private final NewsItemRepository newsItemRepository;

    /** 正面情感词 */
    private static final Set<String> POSITIVE_WORDS = Set.of(
            "突破", "发布", "增长", "上涨", "利好", "融资", "开源", "获奖", "冠军",
            "新高", "领先", "创新", "升级", "优化", "成功", "签约", "合作", "战略",
            "利润", "盈利", "回报", "分红", "回购", "增持", "买入", "推荐",
            "独家", "深度", "研究", "成果", "专利", "认证", "上市", "量产",
            "good", "great", "best", "win", "launch", "record", "profit", "growth",
            "breakthrough", "innovation", "success", "upgrade", "open", "free",
            "award", "top", "leading", "surge", "rally", "boost", "milestone"
    );

    /** 负面情感词 */
    private static final Set<String> NEGATIVE_WORDS = Set.of(
            "下跌", "裁员", "亏损", "事故", "处罚", "违规", "暴跌", "崩盘", "倒闭",
            "下架", "封禁", "整改", "约谈", "罚款", "诉讼", "侵权", "泄露", "漏洞",
            "危机", "风险", "预警", "警告", "禁止", "限制", "制裁", "封锁",
            "召回", "投诉", "举报", "调查", "丑闻", "造假", "欺诈", "暴雷",
            "缩水", "下滑", "疲软", "低迷", "萎缩", "停滞", "衰退",
            "bad", "worst", "fail", "crash", "loss", "decline", "ban", "hack",
            "breach", "scandal", "fraud", "recall", "lawsuit", "fine", "penalty",
            "crisis", "risk", "warning", "threat", "attack", "vulnerability"
    );

    /** 否定词（翻转情感） */
    private static final Set<String> NEGATION_WORDS = Set.of(
            "不", "没", "未", "无", "非", "别", "莫", "勿",
            "not", "no", "never", "neither", "nor", "hardly", "barely"
    );

    /**
     * 分析单条新闻的情感倾向
     * @return "positive" / "negative" / "neutral"
     */
    public String analyze(String title, String summary) {
        String text = (title != null ? title : "") + " " + (summary != null ? summary : "");
        int posCount = 0;
        int negCount = 0;

        for (String word : POSITIVE_WORDS) {
            if (text.contains(word)) {
                // 检查前面是否有否定词
                int idx = text.indexOf(word);
                boolean negated = isNegated(text, idx);
                if (negated) {
                    negCount++;
                } else {
                    posCount++;
                }
            }
        }

        for (String word : NEGATIVE_WORDS) {
            if (text.contains(word)) {
                int idx = text.indexOf(word);
                boolean negated = isNegated(text, idx);
                if (negated) {
                    posCount++;
                } else {
                    negCount++;
                }
            }
        }

        if (posCount > negCount + 1) return "positive";
        if (negCount > posCount + 1) return "negative";
        return "neutral";
    }

    /**
     * 检查指定位置前 3 个字符内是否有否定词
     */
    private boolean isNegated(String text, int wordIdx) {
        int start = Math.max(0, wordIdx - 4);
        String prefix = text.substring(start, wordIdx);
        for (String neg : NEGATION_WORDS) {
            if (prefix.contains(neg)) return true;
        }
        return false;
    }

    /**
     * 批量分析情感并存入 NewsItem.sentiment 字段
     */
    public void analyzeSentiment(List<NewsItem> items) {
        for (NewsItem item : items) {
            String sentiment = analyze(item.getTitle(), item.getSummary());
            item.setSentiment(sentiment);
        }
    }

    /**
     * 获取舆情概览：各分类的 positive/negative/neutral 统计
     */
    public Map<String, Object> getSentimentSummary() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<NewsItem> recentItems = newsItemRepository.findByPublishedAtAfter(since);

        Map<String, int[]> categoryStats = new LinkedHashMap<>();
        int totalPos = 0, totalNeg = 0, totalNeu = 0;

        for (NewsItem item : recentItems) {
            String cat = item.getCategory() != null ? item.getCategory() : "其他";
            int[] counts = categoryStats.computeIfAbsent(cat, k -> new int[3]);
            String s = item.getSentiment() != null ? item.getSentiment() : "neutral";
            switch (s) {
                case "positive" -> { counts[0]++; totalPos++; }
                case "negative" -> { counts[1]++; totalNeg++; }
                default -> { counts[2]++; totalNeu++; }
            }
        }

        List<Map<String, Object>> categories = categoryStats.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", e.getKey());
                    m.put("positive", e.getValue()[0]);
                    m.put("negative", e.getValue()[1]);
                    m.put("neutral", e.getValue()[2]);
                    return m;
                })
                .collect(Collectors.toList());

        int total = totalPos + totalNeg + totalNeu;
        String overall = total == 0 ? "neutral" :
                (totalPos > totalNeg ? "positive" : (totalNeg > totalPos ? "negative" : "neutral"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("overall", overall);
        result.put("total", total);
        result.put("positive", totalPos);
        result.put("negative", totalNeg);
        result.put("neutral", totalNeu);
        result.put("categories", categories);
        return result;
    }
}
