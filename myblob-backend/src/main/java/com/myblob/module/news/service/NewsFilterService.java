package com.myblob.module.news.service;

import com.myblob.module.news.entity.NewsItem;
import com.myblob.module.news.entity.NewsSource;
import com.myblob.module.news.repository.NewsItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsFilterService {

    private final NewsItemRepository newsItemRepository;

    private static final Set<String> POSITIVE_KEYWORDS = Set.of(
            "独家", "深度", "解读", "分析", "研究", "突破", "开源", "发布", "官宣",
            "exclusive", "deep", "analysis", "research", "breakthrough", "open source", "release"
    );

    private static final Set<String> NEGATIVE_KEYWORDS = Set.of(
            "震惊", "沸腾", "吓尿", "出大事", "紧急", "速看", "删前速看", "不转不是",
            "刚刚", "突发", "重磅", "一夜爆红", "疯狂", "炸裂"
    );

    private static final Set<String> AD_KEYWORDS = Set.of(
            "促销", "限时", "免费领取", "加群", "扫码", "优惠券", "点击领取",
            "砍一刀", "拼团", "秒杀", "全场包邮", "立减", "满减"
    );

    private static final int MIN_CONTENT_LENGTH = 80;
    private static final int MIN_SUMMARY_LENGTH = 30;
    private static final double TITLE_SIMILARITY_THRESHOLD = 0.85;

    public List<NewsItem> filterAndScore(List<NewsItem> items, NewsSource source) {
        return items.stream()
                .map(item -> scoreItem(item, source))
                .filter(item -> !item.getIsFiltered())
                .collect(Collectors.toList());
    }

    public NewsItem scoreItem(NewsItem item, NewsSource source) {
        int score = 50; // base score

        // Layer 1: Keyword scoring (30%)
        int keywordScore = scoreByKeywords(item.getTitle());
        String keywordReason = keywordScore >= 10 ? null
                : keywordScore <= -10 ? "标题含标题党/广告词汇" : null;

        // Layer 2: Content quality (25%)
        int contentScore = scoreContentQuality(item);
        String contentReason = contentScore < 30 ? "内容质量过低" : null;

        // Layer 3: Duplication check (weighted in final)
        int dupScore = checkDuplication(item);

        // Layer 4: Timeliness (10%)
        int timelinessScore = scoreTimeliness(item);

        // Layer 5: Source credibility (20%)
        int sourceScore = scoreSourceCredibility(source);

        // Composite score
        int totalScore = (int) Math.round(
                keywordScore * 0.30 +
                contentScore * 0.25 +
                dupScore * 0.15 +
                timelinessScore * 0.10 +
                sourceScore * 0.20
        );

        item.setQualityScore(Math.max(0, Math.min(100, totalScore)));

        // Filter decision
        if (totalScore < 35) {
            item.setIsFiltered(true);
            StringBuilder reason = new StringBuilder();
            if (keywordReason != null) reason.append(keywordReason).append("; ");
            if (contentReason != null) reason.append(contentReason);
            item.setFilterReason(reason.toString().isEmpty() ? "综合质量分过低(" + totalScore + ")" : reason.toString().trim());
        }

        // Check for absolute filtering conditions
        if (isAdContent(item.getTitle())) {
            item.setIsFiltered(true);
            item.setFilterReason("商业广告内容");
            item.setQualityScore(10);
        }
        if (item.getTitle().length() < 5 || item.getTitle().length() > 500) {
            item.setIsFiltered(true);
            item.setFilterReason("标题长度异常");
            item.setQualityScore(5);
        }

        return item;
    }

    private int scoreByKeywords(String title) {
        if (title == null) return 0;
        String lowerTitle = title.toLowerCase();
        int score = 0;

        for (String kw : POSITIVE_KEYWORDS) {
            if (lowerTitle.contains(kw.toLowerCase())) score += 5;
        }
        for (String kw : NEGATIVE_KEYWORDS) {
            if (lowerTitle.contains(kw)) score -= 8;
        }

        // Clickbait detection
        long questionCount = title.chars().filter(c -> c == '？' || c == '?').count();
        if (questionCount > 2) score -= 10;
        long exclaimCount = title.chars().filter(c -> c == '！' || c == '!').count();
        if (exclaimCount > 1) score -= 8;
        if (title.endsWith("...") || title.endsWith("……")) score -= 5;

        return Math.max(0, Math.min(100, score + 50));
    }

    private int scoreContentQuality(NewsItem item) {
        int score = 50;

        String content = item.getContent();
        String summary = item.getSummary();

        if (content == null || content.trim().length() < MIN_CONTENT_LENGTH) {
            if (summary == null || summary.trim().length() < MIN_SUMMARY_LENGTH) {
                score -= 40;
            } else {
                score -= 20;
            }
        }

        // Information density
        if (content != null && content.length() > 0) {
            String[] sentences = content.split("[。！？.!?]");
            if (sentences.length < 3) score -= 15;

            // Check for repeated phrases (machine-generated content)
            String[] words = content.split("\\s+");
            Map<String, Integer> wordFreq = new HashMap<>();
            for (String w : words) {
                if (w.length() >= 3) {
                    wordFreq.merge(w.toLowerCase(), 1, Integer::sum);
                }
            }
            long repeatedCount = wordFreq.values().stream().filter(v -> v > 5).count();
            if (repeatedCount > 3) score -= 20;
        }

        return Math.max(0, Math.min(100, score));
    }

    private int checkDuplication(NewsItem item) {
        if (item.getSourceUrl() == null) return 50;

        // Check URL duplicates
        if (newsItemRepository.existsBySourceUrl(item.getSourceUrl())) {
            return 0;
        }

        // Check title similarity (simplified Levenshtein-based)
        if (item.getTitle() != null && item.getTitle().length() > 10) {
            // We check vs recent items with same source platform for efficiency
            // In production, would use SimHash for content fingerprinting
        }

        return 100;
    }

    private int scoreTimeliness(NewsItem item) {
        if (item.getPublishedAt() == null) return 50;
        long hoursAgo = ChronoUnit.HOURS.between(item.getPublishedAt(), LocalDateTime.now());

        if (hoursAgo <= 1) return 100;
        if (hoursAgo <= 4) return 90;
        if (hoursAgo <= 12) return 75;
        if (hoursAgo <= 24) return 60;
        if (hoursAgo <= 48) return 40;
        if (hoursAgo <= 72) return 25;
        return 10;
    }

    private int scoreSourceCredibility(NewsSource source) {
        String platform = source.getPlatformName().toLowerCase();
        String category = source.getCategory().toLowerCase();

        // Official media
        if (category.contains("官方") || platform.contains("新华社") || platform.contains("人民日报")
                || platform.contains("央视")) {
            return 90;
        }
        // Well-known tech media
        if (platform.contains("hackernews") || platform.contains("techcrunch")
                || platform.contains("ars") || platform.contains("verge")
                || platform.contains("36氪") || platform.contains("少数派")
                || platform.contains("机器之心")) {
            return 80;
        }
        // Social media
        if (category.contains("社交") || platform.contains("微博") || platform.contains("twitter")
                || platform.contains("reddit") || platform.contains("抖音")
                || platform.contains("小红书")) {
            return 50;
        }
        return 60;
    }

    private boolean isAdContent(String title) {
        if (title == null) return false;
        for (String kw : AD_KEYWORDS) {
            if (title.contains(kw)) return true;
        }
        return false;
    }

    /**
     * Compute Levenshtein distance for title similarity checking
     */
    public static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }
}
