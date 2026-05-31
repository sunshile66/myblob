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
                createSource("Simple Flying", "Simple Flying", "https://simpleflying.com/feed/", "RSS", "国际航司", "EN", 40),
                createSource("FlightGlobal", "FlightGlobal", "https://www.flightglobal.com/rss", "RSS", "国际航司", "EN", 41),
                createSource("Airways Magazine", "Airways Magazine", "https://airwaysmag.com/feed/", "RSS", "国际航司", "EN", 42),
                createSource("AeroTime", "AeroTime", "https://www.aerotime.aero/feed", "RSS", "国际航司", "EN", 43),
                createSource("The Aviationist", "The Aviationist", "https://theaviationist.com/feed/", "RSS", "国际航司", "EN", 44),
                createSource("Airline Weekly", "Airline Weekly", "https://www.airlineweekly.com/feed/", "RSS", "国际航司", "EN", 45),
                createSource("One Mile at a Time", "One Mile at a Time", "https://onemileatatime.com/feed/", "RSS", "国际航司", "EN", 46),
                createSource("TPG Aviation", "The Points Guy", "https://thepointsguy.com/category/airlines/feed/", "RSS", "国际航司", "EN", 47),
                createSource("Runway Girl", "Runway Girl Network", "https://www.runwaygirlnetwork.com/feed/", "RSS", "国际航司", "EN", 48),
                createSource("Air Guide", "Air Guide Online", "https://airguideonline.com/feed/", "RSS", "国际航司", "EN", 49),

                // === Google News 航司专题 (每家航司独立RSS) ===
                createSource("Google-Emirates", "Emirates", "https://news.google.com/rss/search?q=Emirates+airline&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 50),
                createSource("Google-Qatar", "Qatar Airways", "https://news.google.com/rss/search?q=Qatar+Airways&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 51),
                createSource("Google-Etihad", "Etihad Airways", "https://news.google.com/rss/search?q=Etihad+Airways&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 52),
                createSource("Google-Lufthansa", "Lufthansa", "https://news.google.com/rss/search?q=Lufthansa+airline&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 53),
                createSource("Google-BA", "British Airways", "https://news.google.com/rss/search?q=British+Airways&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 54),
                createSource("Google-AFKLM", "Air France-KLM", "https://news.google.com/rss/search?q=Air+France+KLM+airline&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 55),
                createSource("Google-Ryanair", "Ryanair", "https://news.google.com/rss/search?q=Ryanair&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 56),
                createSource("Google-easyJet", "easyJet", "https://news.google.com/rss/search?q=easyJet+airline&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 57),
                createSource("Google-Singapore", "Singapore Airlines", "https://news.google.com/rss/search?q=Singapore+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 58),
                createSource("Google-Cathay", "Cathay Pacific", "https://news.google.com/rss/search?q=Cathay+Pacific&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 59),
                createSource("Google-ANA", "All Nippon Airways", "https://news.google.com/rss/search?q=ANA+All+Nippon+Airways&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 60),
                createSource("Google-JAL", "Japan Airlines", "https://news.google.com/rss/search?q=Japan+Airlines+JAL&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 61),
                createSource("Google-Korean", "Korean Air", "https://news.google.com/rss/search?q=Korean+Air&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 62),
                createSource("Google-Turkish", "Turkish Airlines", "https://news.google.com/rss/search?q=Turkish+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 63),
                createSource("Google-Delta", "Delta Air Lines", "https://news.google.com/rss/search?q=Delta+Air+Lines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 64),
                createSource("Google-United", "United Airlines", "https://news.google.com/rss/search?q=United+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 65),
                createSource("Google-American", "American Airlines", "https://news.google.com/rss/search?q=American+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 66),
                createSource("Google-Southwest", "Southwest Airlines", "https://news.google.com/rss/search?q=Southwest+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 67),
                createSource("Google-JetBlue", "JetBlue Airways", "https://news.google.com/rss/search?q=JetBlue+Airways&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 68),
                createSource("Google-Qantas", "Qantas", "https://news.google.com/rss/search?q=Qantas+airline&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 69),
                createSource("Google-Virgin", "Virgin Atlantic", "https://news.google.com/rss/search?q=Virgin+Atlantic+airline&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 70),
                createSource("Google-AirCanada", "Air Canada", "https://news.google.com/rss/search?q=Air+Canada&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 71),
                createSource("Google-Asiana", "Asiana Airlines", "https://news.google.com/rss/search?q=Asiana+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 72),
                createSource("Google-Thai", "Thai Airways", "https://news.google.com/rss/search?q=Thai+Airways&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 73),
                createSource("Google-Vietnam", "Vietnam Airlines", "https://news.google.com/rss/search?q=Vietnam+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 74),
                createSource("Google-Malaysia", "Malaysia Airlines", "https://news.google.com/rss/search?q=Malaysia+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 75),
                createSource("Google-Philippine", "Philippine Airlines", "https://news.google.com/rss/search?q=Philippine+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 76),
                createSource("Google-IndiGo", "IndiGo", "https://news.google.com/rss/search?q=IndiGo+airline+India&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 77),
                createSource("Google-SAS", "SAS Scandinavian", "https://news.google.com/rss/search?q=SAS+Scandinavian+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 78),
                createSource("Google-Swiss", "Swiss Air", "https://news.google.com/rss/search?q=Swiss+International+Air+Lines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 79),

                // === 航空综合话题 Google News ===
                createSource("Google-Airfare", "Airfare Deals", "https://news.google.com/rss/search?q=airline+fare+deals+promo&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 80),
                createSource("Google-Routes", "Airline Routes", "https://news.google.com/rss/search?q=airline+new+routes+launch&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 81),
                createSource("Google-Fuel", "Airline Fuel", "https://news.google.com/rss/search?q=airline+fuel+surcharge+adjustment&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 82),
                createSource("Google-Aviation", "Aviation Industry", "https://news.google.com/rss/search?q=aviation+industry+news&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 83)
        );

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
