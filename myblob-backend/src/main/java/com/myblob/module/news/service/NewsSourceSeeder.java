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
        seedMissingSources(); // 补充新增源（不影响已有源）
        // Google News / Twitter / YouTube / Bluesky 已禁用（国内GFW无法访问）
    }

    /**
     * 补充新增源：仅插入 name 不存在的新源，不影响已有数据。
     * 每次新增源时在这里追加即可，无需清库。
     */
    private void seedMissingSources() {
        List<NewsSource> toAdd = new java.util.ArrayList<>();
        List<NewsSource> candidates = List.of(
                createSource("中国新闻网", "中国新闻网", "https://www.chinanews.com/rss/scroll-news.xml", "RSS", "官方媒体", "CN", 6),
                createSource("环球时报", "环球时报", "https://www.globaltimes.cn/rss/outbrain.xml", "RSS", "官方媒体", "EN", 7),
                createSource("人民日报国际", "人民日报", "http://www.people.com.cn/rss/world.xml", "RSS", "官方媒体", "CN", 3),
                // 科技财经补充源
                createSource("cnBeta", "cnBeta", "https://www.cnbeta.com.tw/backend.php", "RSS", "科技财经", "CN", 100),
                createSource("极客公园", "极客公园", "https://www.geekpark.net/rss", "RSS", "科技财经", "CN", 101),
                createSource("品玩", "品玩", "https://www.pingwest.com/feed", "RSS", "科技财经", "CN", 102),
                createSource("量子位", "量子位", "https://www.qbitai.com/feed", "RSS", "科技财经", "CN", 103),
                // 社交媒体补充源
                createSource("今日头条热榜", "今日头条", "TOUTIAIO_API", "JSOUP", "社交媒体", "CN", 104),
                createSource("百度热搜", "百度热搜", "BAIDU_API", "JSOUP", "社交媒体", "CN", 105),
                createSource("抖音热榜", "抖音热榜", "DOUYIN_API", "JSOUP", "社交媒体", "CN", 106)
        );
        for (NewsSource candidate : candidates) {
            if (newsSourceRepository.findByName(candidate.getName()) == null) {
                toAdd.add(candidate);
            }
        }
        if (!toAdd.isEmpty()) {
            newsSourceRepository.saveAll(toAdd);
            log.info("Added {} missing news sources: {}", toAdd.size(),
                    toAdd.stream().map(NewsSource::getName).toList());
        }
        
        // 禁用已失效的源
        disableFailedSources();
    }
    
    /**
     * 禁用已失效或国内无法访问的RSS源（避免浪费资源尝试获取）
     */
    private void disableFailedSources() {
        List<String> failedSourceNames = List.of(
                // 已失效的源
                "新华社",       // RSS域名已弃用
                "机器之心",     // RSS返回非标准XML
                "果壳",        // RSS返回404
                "知乎热榜",     // 403禁止访问
                // 国内无法访问的海外源
                "HackerNews",  // Firebase API国内超时
                "GitHub Trending"  // 国内超时
        );
        
        // 禁用所有Google和YouTube前缀的源
        List<String> prefixes = List.of("Google-", "YouTube-");
        int disabledCount = 0;
        for (String name : failedSourceNames) {
            NewsSource source = newsSourceRepository.findByName(name);
            if (source != null && Boolean.TRUE.equals(source.getEnabled())) {
                source.setEnabled(false);
                newsSourceRepository.save(source);
                disabledCount++;
                log.info("Disabled failed source: {}", name);
            }
        }
        
        // 按前缀禁用（Google-xxx, YouTube-xxx等）
        for (NewsSource source : newsSourceRepository.findAll()) {
            if (Boolean.TRUE.equals(source.getEnabled())) {
                for (String prefix : prefixes) {
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

    // seedNewSocialSources / seedGoogleNewsGeneralSources 已移除：国外源在国内无法访问

    private void seedGeneralSources() {
        if (newsSourceRepository.count() > 0) {
            log.info("General news sources already seeded, skipping");
            return;
        }

        List<NewsSource> sources = List.of(
                // 官方媒体（直接RSS）
                createDisabled("新华社", "新华社", "http://www.xinhuanet.com/politics/xhll.xml", "RSS", "官方媒体", "CN", 1), // RSS域名已弃用，无可用端点
                createSource("人民日报", "人民日报", "http://www.people.com.cn/rss/politics.xml", "RSS", "官方媒体", "CN", 2),
                createSource("人民日报国际", "人民日报", "http://www.people.com.cn/rss/world.xml", "RSS", "官方媒体", "CN", 3),
                createSource("中国新闻网", "中国新闻网", "https://www.chinanews.com/rss/scroll-news.xml", "RSS", "官方媒体", "CN", 6),
                createSource("环球时报", "环球时报", "https://www.globaltimes.cn/rss/outbrain.xml", "RSS", "官方媒体", "EN", 7),
                // 社交媒体（Jsoup爬虫）
                createSource("微博热搜", "微博热搜", "WEIBO_API", "JSOUP", "社交媒体", "CN", 4),
                createDisabled("知乎热榜", "知乎热榜", "ZHIHU_HOT", "JSOUP", "社交媒体", "CN", 5), // 403
                // 科技财经（直接RSS）
                createSource("36氪", "36氪", "https://36kr.com/feed", "RSS", "科技财经", "CN", 8),
                createSource("少数派", "少数派", "https://sspai.com/feed", "RSS", "科技财经", "CN", 9),
                createSource("虎嗅", "虎嗅", "https://www.huxiu.com/rss/0.xml", "RSS", "科技财经", "CN", 10),
                createDisabled("机器之心", "机器之心", "https://www.jiqizhixin.com/rss", "RSS", "科技财经", "CN", 11), // RSS失效
                createSource("爱范儿", "爱范儿", "https://www.ifanr.com/feed", "RSS", "科技财经", "CN", 12),
                createDisabled("果壳", "果壳", "https://www.guokr.com/rss/", "RSS", "科技财经", "CN", 13), // RSS失效
                // 新增中文源
                createSource("IT之家", "IT之家", "https://www.ithome.com/rss/", "RSS", "科技财经", "CN", 14),
                createSource("钛媒体", "钛媒体", "https://www.tmtpost.com/rss.xml", "RSS", "科技财经", "CN", 15),
                createSource("界面新闻", "界面新闻", "https://www.jiemian.com/lists/4.rss", "RSS", "科技财经", "CN", 16),
                createSource("澎湃新闻", "澎湃新闻", "https://www.thepaper.cn/rss_newsDetail_channel_25950", "RSS", "官方媒体",
                        "CN", 17),
                createSource("观察者网", "观察者网", "https://www.guancha.cn/rss/feed.xml", "RSS", "官方媒体", "CN", 18),
                createSource("快科技", "快科技", "https://www.mydrivers.com/rss.aspx", "RSS", "科技财经", "CN", 19),
                // 海外社交媒体（国内GFW无法访问，默认禁用）
                createDisabled("Reddit", "Reddit", "REDDIT_API", "API", "社交媒体", "EN", 20),
                // 海外科技媒体（部分国内可访问）
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
                        "EN", 33));

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

                // === Google News 航司专题 (国内GFW无法访问，默认禁用) ===
                createDisabled("Google-Emirates", "Emirates",
                        "https://news.google.com/rss/search?q=Emirates+airline&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 50),
                createDisabled("Google-Qatar", "Qatar Airways",
                        "https://news.google.com/rss/search?q=Qatar+Airways&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 51),
                createDisabled("Google-Etihad", "Etihad Airways",
                        "https://news.google.com/rss/search?q=Etihad+Airways&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 52),
                createDisabled("Google-Lufthansa", "Lufthansa",
                        "https://news.google.com/rss/search?q=Lufthansa+airline&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 53),
                createDisabled("Google-BA", "British Airways",
                        "https://news.google.com/rss/search?q=British+Airways&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 54),
                createDisabled("Google-AFKLM", "Air France-KLM",
                        "https://news.google.com/rss/search?q=Air+France+KLM+airline&hl=en&gl=US&ceid=US:en", "RSS",
                        "国际航司", "EN", 55),
                createDisabled("Google-Ryanair", "Ryanair",
                        "https://news.google.com/rss/search?q=Ryanair&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN", 56),
                createDisabled("Google-easyJet", "easyJet",
                        "https://news.google.com/rss/search?q=easyJet+airline&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 57),
                createDisabled("Google-Singapore", "Singapore Airlines",
                        "https://news.google.com/rss/search?q=Singapore+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 58),
                createDisabled("Google-Cathay", "Cathay Pacific",
                        "https://news.google.com/rss/search?q=Cathay+Pacific&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 59),
                createDisabled("Google-ANA", "All Nippon Airways",
                        "https://news.google.com/rss/search?q=ANA+All+Nippon+Airways&hl=en&gl=US&ceid=US:en", "RSS",
                        "国际航司", "EN", 60),
                createDisabled("Google-JAL", "Japan Airlines",
                        "https://news.google.com/rss/search?q=Japan+Airlines+JAL&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 61),
                createDisabled("Google-Korean", "Korean Air",
                        "https://news.google.com/rss/search?q=Korean+Air&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN",
                        62),
                createDisabled("Google-Turkish", "Turkish Airlines",
                        "https://news.google.com/rss/search?q=Turkish+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 63),
                createDisabled("Google-Delta", "Delta Air Lines",
                        "https://news.google.com/rss/search?q=Delta+Air+Lines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 64),
                createDisabled("Google-United", "United Airlines",
                        "https://news.google.com/rss/search?q=United+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 65),
                createDisabled("Google-American", "American Airlines",
                        "https://news.google.com/rss/search?q=American+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 66),
                createDisabled("Google-Southwest", "Southwest Airlines",
                        "https://news.google.com/rss/search?q=Southwest+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 67),
                createDisabled("Google-JetBlue", "JetBlue Airways",
                        "https://news.google.com/rss/search?q=JetBlue+Airways&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 68),
                createDisabled("Google-Qantas", "Qantas",
                        "https://news.google.com/rss/search?q=Qantas+airline&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 69),
                createDisabled("Google-Virgin", "Virgin Atlantic",
                        "https://news.google.com/rss/search?q=Virgin+Atlantic+airline&hl=en&gl=US&ceid=US:en", "RSS",
                        "国际航司", "EN", 70),
                createDisabled("Google-AirCanada", "Air Canada",
                        "https://news.google.com/rss/search?q=Air+Canada&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN",
                        71),
                createDisabled("Google-Asiana", "Asiana Airlines",
                        "https://news.google.com/rss/search?q=Asiana+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 72),
                createDisabled("Google-Thai", "Thai Airways",
                        "https://news.google.com/rss/search?q=Thai+Airways&hl=en&gl=US&ceid=US:en", "RSS", "国际航司", "EN",
                        73),
                createDisabled("Google-Vietnam", "Vietnam Airlines",
                        "https://news.google.com/rss/search?q=Vietnam+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 74),
                createDisabled("Google-Malaysia", "Malaysia Airlines",
                        "https://news.google.com/rss/search?q=Malaysia+Airlines&hl=en&gl=US&ceid=US:en", "RSS", "国际航司",
                        "EN", 75),
                createDisabled("Google-Philippine", "Philippine Airlines",
                        "https://news.google.com/rss/search?q=Philippine+Airlines&hl=en&gl=US&ceid=US:en", "RSS",
                        "国际航司", "EN", 76),
                createDisabled("Google-IndiGo", "IndiGo",
                        "https://news.google.com/rss/search?q=IndiGo+airline+India&hl=en&gl=US&ceid=US:en", "RSS",
                        "国际航司", "EN", 77),
                createDisabled("Google-SAS", "SAS Scandinavian",
                        "https://news.google.com/rss/search?q=SAS+Scandinavian+Airlines&hl=en&gl=US&ceid=US:en", "RSS",
                        "国际航司", "EN", 78),
                createDisabled("Google-Swiss", "Swiss Air",
                        "https://news.google.com/rss/search?q=Swiss+International+Air+Lines&hl=en&gl=US&ceid=US:en",
                        "RSS", "国际航司", "EN", 79),

                // === 航空综合话题 Google News (国内GFW无法访问，默认禁用) ===
                createDisabled("Google-Airfare", "Airfare Deals",
                        "https://news.google.com/rss/search?q=airline+fare+deals+promo&hl=en&gl=US&ceid=US:en", "RSS",
                        "国际航司", "EN", 96),
                createDisabled("Google-Routes", "Airline Routes",
                        "https://news.google.com/rss/search?q=airline+new+routes+launch&hl=en&gl=US&ceid=US:en", "RSS",
                        "国际航司", "EN", 97),
                createDisabled("Google-Fuel", "Airline Fuel",
                        "https://news.google.com/rss/search?q=airline+fuel+surcharge+adjustment&hl=en&gl=US&ceid=US:en",
                        "RSS", "国际航司", "EN", 98),
                createDisabled("Google-Aviation", "Aviation Industry",
                        "https://news.google.com/rss/search?q=aviation+industry+news&hl=en&gl=US&ceid=US:en", "RSS",
                        "国际航司", "EN", 99)
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
