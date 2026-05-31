package com.myblob.module.news.service;

import com.myblob.module.news.entity.NewsSource;
import com.myblob.module.news.repository.NewsSourceRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsSourceSeeder {

    private final NewsSourceRepository newsSourceRepository;

    @PostConstruct
    public void seedDefaultSources() {
        if (newsSourceRepository.count() > 0) {
            log.info("News sources already seeded, skipping");
            return;
        }

        List<NewsSource> sources = List.of(
                // 官方媒体（直接RSS）
                createSource("新华社", "新华社", "http://www.xinhuanet.com/politics/xhll.xml", "RSS", "官方媒体", "CN", 1),
                createSource("人民日报", "人民日报", "http://www.people.com.cn/rss/politics.xml", "RSS", "官方媒体", "CN", 2),
                // 社交媒体（Jsoup爬虫）
                createSource("微博热搜", "微博热搜", "WEIBO_API", "JSOUP", "社交媒体", "CN", 4),
                createSource("知乎热榜", "知乎热榜", "ZHIHU_HOT", "JSOUP", "社交媒体", "CN", 5),
                // 科技财经（直接RSS）
                createSource("36氪", "36氪", "https://36kr.com/feed", "RSS", "科技财经", "CN", 8),
                createSource("少数派", "少数派", "https://sspai.com/feed", "RSS", "科技财经", "CN", 9),
                createSource("虎嗅", "虎嗅", "https://www.huxiu.com/rss/0.xml", "RSS", "科技财经", "CN", 10),
                createSource("机器之心", "机器之心", "https://www.jiqizhixin.com/rss", "RSS", "科技财经", "CN", 11),
                createSource("爱范儿", "爱范儿", "https://www.ifanr.com/feed", "RSS", "科技财经", "CN", 12),
                createSource("果壳", "果壳", "https://www.guokr.com/rss/", "RSS", "科技财经", "CN", 13),
                // 海外社交媒体
                createSource("Reddit", "Reddit", "REDDIT_API", "API", "社交媒体", "EN", 14),
                // 海外科技媒体
                createSource("HackerNews", "HackerNews", "HN_API", "API", "科技媒体", "EN", 17),
                createSource("TechCrunch", "TechCrunch", "https://techcrunch.com/feed/", "RSS", "科技媒体", "EN", 18),
                createSource("Dev.to", "Dev.to", "https://dev.to/feed", "RSS", "科技媒体", "EN", 19),
                createSource("The Verge", "The Verge", "https://www.theverge.com/rss/index.xml", "RSS", "科技媒体", "EN", 20),
                createSource("Ars Technica", "Ars Technica", "https://feeds.arstechnica.com/arstechnica/index", "RSS", "科技媒体", "EN", 21),
                // 开源开发者
                createSource("GitHub Trending", "GitHub Trending", "GITHUB_TRENDING", "JSOUP", "开源开发者", "EN", 22),
                createSource("Product Hunt", "Product Hunt", "https://www.producthunt.com/feed", "RSS", "开源开发者", "EN", 23),
                // RSSHub依赖源（默认禁用，部署RSSHub后可启用）
                createDisabled("央视新闻", "央视新闻", "{rsshub}/cctv/world", "RSSHUB", "官方媒体", "CN", 30),
                createDisabled("小红书", "小红书", "{rsshub}/xiaohongshu/explore", "RSSHUB", "社交媒体", "CN", 31),
                createDisabled("抖音热点", "抖音热点", "{rsshub}/douyin/hot", "RSSHUB", "社交媒体", "CN", 32),
                createDisabled("微信公众号", "微信公众号", "{rsshub}/wechat/ce", "RSSHUB", "社交媒体", "CN", 33),
                createDisabled("Twitter", "Twitter", "{rsshub}/twitter/user/OpenAI", "RSSHUB", "社交媒体", "EN", 34),
                createDisabled("Instagram", "Instagram", "{rsshub}/instagram/user/instagram", "RSSHUB", "社交媒体", "EN", 35),
                createDisabled("Facebook", "Facebook", "{rsshub}/facebook/page/Meta", "RSSHUB", "社交媒体", "EN", 36)
        );

        newsSourceRepository.saveAll(sources);
        log.info("Seeded {} default news sources", sources.size());
    }

    private NewsSource createSource(String name, String platform, String feedUrl,
                                     String method, String category, String lang, int priority) {
        return NewsSource.builder()
                .name(name)
                .platformName(platform)
                .feedUrl(feedUrl)
                .fetchMethod(method)
                .category(category)
                .language(lang)
                .enabled(true)
                .priority(priority)
                .fetchIntervalSeconds(1800)
                .errorCount(0)
                .consecutiveErrors(0)
                .build();
    }

    private NewsSource createDisabled(String name, String platform, String feedUrl,
                                       String method, String category, String lang, int priority) {
        return NewsSource.builder()
                .name(name)
                .platformName(platform)
                .feedUrl(feedUrl)
                .fetchMethod(method)
                .category(category)
                .language(lang)
                .enabled(false)
                .priority(priority)
                .fetchIntervalSeconds(1800)
                .errorCount(0)
                .consecutiveErrors(0)
                .build();
    }
}
