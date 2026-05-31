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
        seedGeneralSources();
        seedAirlineSources();
        seedGoogleNewsGeneralSources();
    }

    private void seedGoogleNewsGeneralSources() {
        // Check if Google News general sources exist by looking for a known source name
        if (newsSourceRepository.findByName("Google-头条") != null) {
            log.info("Google News general sources already seeded, skipping");
            return;
        }

        List<NewsSource> sources = List.of(
                // Google News 综合热点（直连RSS，北京服务器需配置HTTP代理）
                createSource("Google-头条", "Google News", "https://news.google.com/rss?hl=zh-CN&gl=CN&ceid=CN:zh-Hans",
                        "RSS", "官方媒体", "CN", 1),
                createSource("Google-Top", "Google News", "https://news.google.com/rss?hl=en-US&gl=US&ceid=US:en",
                        "RSS", "科技财经", "EN", 2),
                createSource("Google-科技", "Google News",
                        "https://news.google.com/rss/topics/CAAqJggKIiBDQkFTRWdvSUwyMHZNRGRqTVhZU0FtVnVHZ0pWVXlnQVAB?hl=en-US&gl=US&ceid=US:en",
                        "RSS", "科技媒体", "EN", 3),
                createSource("Google-财经", "Google News",
                        "https://news.google.com/rss/topics/CAAqJggKIiBDQkFTRWdvSUwyMHZNRGx6TVdZU0FtVnVHZ0pWVXlnQVAB?hl=en-US&gl=US&ceid=US:en",
                        "RSS", "科技财经", "EN", 4),
                createSource("Google-世界", "Google News",
                        "https://news.google.com/rss/topics/CAAqJggKIiBDQkFTRWdvSUwyMHZNRGx1YlY4U0FtVnVHZ0pWVXlnQVAB?hl=en-US&gl=US&ceid=US:en",
                        "RSS", "官方媒体", "EN", 5),
                createSource("Google-AI", "Google News",
                        "https://news.google.com/rss/search?q=artificial+intelligence+AI+tech&hl=en-US&gl=US&ceid=US:en",
                        "RSS", "科技媒体", "EN", 7),
                createSource("Google-航空", "Google News",
                        "https://news.google.com/rss/search?q=%E8%88%AA%E7%A9%BA+%E9%A3%9E%E6%9C%BA&hl=zh-CN&gl=CN&ceid=CN:zh-Hans",
                        "RSS", "国际航司", "CN", 8));

        newsSourceRepository.saveAll(sources);
        log.info("Seeded {} Google News general sources", sources.size());
    }

    private void seedGeneralSources() {
        if (newsSourceRepository.count() > 0) {
            log.info("General news sources already seeded, skipping");
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
                // 新增中文源
                createSource("IT之家", "IT之家", "https://www.ithome.com/rss/", "RSS", "科技财经", "CN", 14),
                createSource("钛媒体", "钛媒体", "https://www.tmtpost.com/rss.xml", "RSS", "科技财经", "CN", 15),
                createSource("界面新闻", "界面新闻", "https://www.jiemian.com/lists/4.rss", "RSS", "科技财经", "CN", 16),
                createSource("澎湃新闻", "澎湃新闻", "https://www.thepaper.cn/rss_newsDetail_channel_25950", "RSS", "官方媒体",
                        "CN", 17),
                createSource("观察者网", "观察者网", "https://www.guancha.cn/rss/feed.xml", "RSS", "官方媒体", "CN", 18),
                createSource("快科技", "快科技", "https://www.mydrivers.com/rss.aspx", "RSS", "科技财经", "CN", 19),
                // 海外社交媒体
                createSource("Reddit", "Reddit", "REDDIT_API", "API", "社交媒体", "EN", 20),
                // 海外科技媒体
                createSource("HackerNews", "HackerNews", "HN_API", "API", "科技媒体", "EN", 21),
                createSource("TechCrunch", "TechCrunch", "https://techcrunch.com/feed/", "RSS", "科技媒体", "EN", 22),
                createSource("Dev.to", "Dev.to", "https://dev.to/feed", "RSS", "科技媒体", "EN", 23),
                createSource("The Verge", "The Verge", "https://www.theverge.com/rss/index.xml", "RSS", "科技媒体", "EN",
                        24),
                createSource("Ars Technica", "Ars Technica", "https://feeds.arstechnica.com/arstechnica/index", "RSS",
                        "科技媒体", "EN", 25),
                createSource("Wired", "Wired", "https://www.wired.com/feed/rss", "RSS", "科技媒体", "EN", 26),
                createSource("Engadget", "Engadget", "https://www.engadget.com/rss.xml", "RSS", "科技媒体", "EN", 27),
                createSource("ZDNet", "ZDNet", "https://www.zdnet.com/news/rss.xml", "RSS", "科技媒体", "EN", 28),
                // 开源开发者
                createSource("GitHub Trending", "GitHub Trending", "GITHUB_TRENDING", "JSOUP", "开源开发者", "EN", 30),
                createSource("Product Hunt", "Product Hunt", "https://www.producthunt.com/feed", "RSS", "开源开发者", "EN",
                        31),
                createSource("HackerNoon", "HackerNoon", "https://hackernoon.com/feed", "RSS", "科技媒体", "EN", 32),
                createSource("freeCodeCamp", "freeCodeCamp", "https://www.freecodecamp.org/news/rss/", "RSS", "开源开发者",
                        "EN", 33),
                // YouTube 原生 RSS (channel_id格式，北京需HTTP代理)
                createSource("YouTube-MKBHD", "YouTube", "https://www.youtube.com/feeds/videos.xml?channel_id=UCBcRF18a7Qf58cCRy5xuWwQ", "RSS", "科技媒体", "EN", 44),
                createSource("YouTube-LinusTech", "YouTube", "https://www.youtube.com/feeds/videos.xml?channel_id=UCXuqSBlHAE6Xw-yeJA0Tunw", "RSS", "科技媒体", "EN", 45),
                // Twitter/X (via twitterapi.io)
                createSource("Twitter-OpenAI", "Twitter/X", "TWITTER_API:OpenAI", "TWITTER", "社交媒体", "EN", 40),
                createSource("Twitter-Google", "Twitter/X", "TWITTER_API:Google", "TWITTER", "社交媒体", "EN", 41),
                createSource("Twitter-Apple", "Twitter/X", "TWITTER_API:Apple", "TWITTER", "社交媒体", "EN", 42),
                createSource("Twitter-Microsoft", "Twitter/X", "TWITTER_API:Microsoft", "TWITTER", "社交媒体", "EN", 43),
                // Bluesky (AT Protocol 免费 API)
                createSource("Bluesky-OpenAI", "Bluesky", "BLUESKY_API:openai.com", "BLUESKY", "社交媒体", "EN", 46),
                createSource("Bluesky-Google", "Bluesky", "BLUESKY_API:google.com", "BLUESKY", "社交媒体", "EN", 47));

        newsSourceRepository.saveAll(sources);
        log.info("Seeded {} general news sources", sources.size());
    }

    private void seedAirlineSources() {
        // Check if airline sources already exist
        long airlineCount = newsSourceRepository.countByCategory("国际航司");
        if (airlineCount > 0) {
            log.info("Airline news sources already seeded ({}), skipping", airlineCount);
            return;
        }

        List<NewsSource> sources = List.of(
                // === 航空新闻媒体 ===
                createSource("Simple Flying", "Simple Flying", "https://simpleflying.com/feed/", "RSS", "国际航司", "EN",
                        40),
                createSource("FlightGlobal", "FlightGlobal", "https://www.flightglobal.com/rss", "RSS", "国际航司", "EN",
                        41),
                createSource("Airways Magazine", "Airways Magazine", "https://airwaysmag.com/feed/", "RSS", "国际航司",
                        "EN", 42),
                createSource("AeroTime", "AeroTime", "https://www.aerotime.aero/feed", "RSS", "国际航司", "EN", 43),
                createSource("The Aviationist", "The Aviationist", "https://theaviationist.com/feed/", "RSS", "国际航司",
                        "EN", 44),
                createSource("Airline Weekly", "Airline Weekly", "https://www.airlineweekly.com/feed/", "RSS", "国际航司",
                        "EN", 45),
                createSource("One Mile at a Time", "One Mile at a Time", "https://onemileatatime.com/feed/", "RSS",
                        "国际航司", "EN", 46),
                createSource("TPG Aviation", "The Points Guy", "https://thepointsguy.com/category/airlines/feed/",
                        "RSS", "国际航司", "EN", 47),
                createSource("Runway Girl", "Runway Girl Network", "https://www.runwaygirlnetwork.com/feed/", "RSS",
                        "国际航司", "EN", 48),
                createSource("Air Guide", "Air Guide Online", "https://airguideonline.com/feed/", "RSS", "国际航司", "EN",
                        49),

                // === Google News 航司专题 (每家航司独立RSS) ===
                createSource("Google-Emirates", "Emirates",
                        "https://news.google.com/rss/search?q=Emirates+airline&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 50),
                createSource("Google-Qatar", "Qatar Airways",
                        "https://news.google.com/rss/search?q=Qatar+Airways&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 51),
                createSource("Google-Etihad", "Etihad Airways",
                        "https://news.google.com/rss/search?q=Etihad+Airways&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 52),
                createSource("Google-Lufthansa", "Lufthansa",
                        "https://news.google.com/rss/search?q=Lufthansa+airline&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 53),
                createSource("Google-BA", "British Airways",
                        "https://news.google.com/rss/search?q=British+Airways&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 54),
                createSource("Google-AFKLM", "Air France-KLM",
                        "https://news.google.com/rss/search?q=Air+France+KLM+airline&hl=en&gl=US&ceid=US:en", "RSS",
                        "国际航司", "EN", 55),
                createSource("Google-Ryanair", "Ryanair",
                        "https://news.google.com/rss/search?q=Ryanair&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 56),
                createSource("Google-easyJet", "easyJet",
                        "https://news.google.com/rss/search?q=easyJet+airline&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 57),
                createSource("Google-Singapore", "Singapore Airlines",
                        "https://news.google.com/rss/search?q=Singapore+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 58),
                createSource("Google-Cathay", "Cathay Pacific",
                        "https://news.google.com/rss/search?q=Cathay+Pacific&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 59),
                createSource("Google-ANA", "All Nippon Airways",
                        "https://news.google.com/rss/search?q=ANA+All+Nippon+Airways&hl=en&gl=US&ceid=US:en", "RSS",
                        "国际航司", "EN", 60),
                createSource("Google-JAL", "Japan Airlines",
                        "https://news.google.com/rss/search?q=Japan+Airlines+JAL&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 61),
                createSource("Google-Korean", "Korean Air",
                        "https://news.google.com/rss/search?q=Korean+Air&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN",
                        62),
                createSource("Google-Turkish", "Turkish Airlines",
                        "https://news.google.com/rss/search?q=Turkish+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 63),
                createSource("Google-Delta", "Delta Air Lines",
                        "https://news.google.com/rss/search?q=Delta+Air+Lines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 64),
                createSource("Google-United", "United Airlines",
                        "https://news.google.com/rss/search?q=United+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 65),
                createSource("Google-American", "American Airlines",
                        "https://news.google.com/rss/search?q=American+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 66),
                createSource("Google-Southwest", "Southwest Airlines",
                        "https://news.google.com/rss/search?q=Southwest+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 67),
                createSource("Google-JetBlue", "JetBlue Airways",
                        "https://news.google.com/rss/search?q=JetBlue+Airways&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 68),
                createSource("Google-Qantas", "Qantas",
                        "https://news.google.com/rss/search?q=Qantas+airline&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 69),
                createSource("Google-Virgin", "Virgin Atlantic",
                        "https://news.google.com/rss/search?q=Virgin+Atlantic+airline&hl=en&gl=US&ceid=US:en", "RSS",
                        "国际航司", "EN", 70),
                createSource("Google-AirCanada", "Air Canada",
                        "https://news.google.com/rss/search?q=Air+Canada&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN",
                        71),
                createSource("Google-Asiana", "Asiana Airlines",
                        "https://news.google.com/rss/search?q=Asiana+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 72),
                createSource("Google-Thai", "Thai Airways",
                        "https://news.google.com/rss/search?q=Thai+Airways&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN",
                        73),
                createSource("Google-Vietnam", "Vietnam Airlines",
                        "https://news.google.com/rss/search?q=Vietnam+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 74),
                createSource("Google-Malaysia", "Malaysia Airlines",
                        "https://news.google.com/rss/search?q=Malaysia+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 75),
                createSource("Google-Philippine", "Philippine Airlines",
                        "https://news.google.com/rss/search?q=Philippine+Airlines&hl=en&gl=US&ceid=US:en", "RSS",
                        "国际航司", "EN", 76),
                createSource("Google-IndiGo", "IndiGo",
                        "https://news.google.com/rss/search?q=IndiGo+airline+India&hl=en&gl=US&ceid=US:en", "RSS",
                        "国际航司", "EN", 77),
                createSource("Google-SAS", "SAS Scandinavian",
                        "https://news.google.com/rss/search?q=SAS+Scandinavian+Airlines&hl=en&gl=US&ceid=US:en", "RSS",
                        "国际航司", "EN", 78),
                createSource("Google-Swiss", "Swiss Air",
                        "https://news.google.com/rss/search?q=Swiss+International+Air+Lines&hl=en&gl=US&ceid=US:en",
                        "RSS", "国际航司", "EN", 79),

                // === 航空综合话题 Google News ===
                createSource("Google-Airfare", "Airfare Deals",
                        "https://news.google.com/rss/search?q=airline+fare+deals+promo&hl=en&gl=US&ceid=US:en", "RSS",
                        "国际航司", "EN", 96),
                createSource("Google-Routes", "Airline Routes",
                        "https://news.google.com/rss/search?q=airline+new+routes+launch&hl=en&gl=US&ceid=US:en", "RSS",
                        "国际航司", "EN", 97),
                createSource("Google-Fuel", "Airline Fuel",
                        "https://news.google.com/rss/search?q=airline+fuel+surcharge+adjustment&hl=en&gl=US&ceid=US:en",
                        "RSS", "国际航司", "EN", 98),
                createSource("Google-Aviation", "Aviation Industry",
                        "https://news.google.com/rss/search?q=aviation+industry+news&hl=en&gl=US&ceid=US:en", "RSS",
                        "国际航司", "EN", 99),

                // === 航空公司社交媒体 (Twitter/X via twitterapi.io) ===
                createSource("Twitter-Emirates", "Twitter/X", "TWITTER_API:emirates", "TWITTER", "国际航司", "EN", 84),
                createSource("Twitter-Qatar", "Twitter/X", "TWITTER_API:qatarairways", "TWITTER", "国际航司", "EN", 85),
                createSource("Twitter-Lufthansa", "Twitter/X", "TWITTER_API:lufthansa", "TWITTER", "国际航司", "EN", 86),
                createSource("Twitter-BA", "Twitter/X", "TWITTER_API:british_airways", "TWITTER", "国际航司", "EN", 87),
                createSource("Twitter-Delta", "Twitter/X", "TWITTER_API:Delta", "TWITTER", "国际航司", "EN", 88),
                createSource("Twitter-United", "Twitter/X", "TWITTER_API:United", "TWITTER", "国际航司", "EN", 89),

                // === 航空公司 YouTube 原生 RSS ===
                createSource("YouTube-Emirates", "YouTube", "https://www.youtube.com/feeds/videos.xml?channel_id=UC8C9yMnNvD1aGvRNTqEFWqA", "RSS", "国际航司", "EN", 94),
                createSource("YouTube-Lufthansa", "YouTube", "https://www.youtube.com/feeds/videos.xml?channel_id=UC8mDk6eZ62EiMxv3Y3gE7qQ", "RSS", "国际航司", "EN", 95));

        newsSourceRepository.saveAll(sources);
        log.info("Seeded {} airline news sources", sources.size());
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
                .fetchIntervalSeconds(900)
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
                .fetchIntervalSeconds(900)
                .errorCount(0)
                .consecutiveErrors(0)
                .build();
    }
}
