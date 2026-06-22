package com.myblob.module.news.service;

import com.myblob.module.news.entity.NewsSource;

import java.util.List;

/**
 * 新闻源数据定义
 */
public final class NewsSourceData {

    private NewsSourceData() {}

    /**
     * 通用新闻源
     */
    public static List<NewsSource> getGeneralSources() {
        return List.of(
                createSource("HackerNews", "HackerNews", "https://hacker-news.firebaseio.com/v0", "API", "科技媒体", "EN", 1),
                createSource("Reddit Programming", "Reddit", "https://www.reddit.com/r/programming", "API", "开源开发者", "EN", 2),
                createSource("GitHub Trending", "GitHub Trending", "https://github.com/trending", "JSOUP", "开源开发者", "EN", 5),
                createSource("微博热搜", "微博热搜", "WEIBO_API", "JSOUP", "社交媒体", "CN", 8),
                createSource("知乎热榜", "知乎热榜", "https://www.zhihu.com/hot", "JSOUP", "社交媒体", "CN", 9),
                createSource("TechCrunch", "TechCrunch", "https://techcrunch.com/feed/", "RSS", "科技媒体", "EN", 10),
                createSource("Ars Technica", "Ars Technica", "https://feeds.arstechnica.com/arstechnica/index", "RSS", "科技媒体", "EN", 11),
                createSource("The Verge", "The Verge", "https://www.theverge.com/rss/index.xml", "RSS", "科技媒体", "EN", 12),
                createSource("Wired", "Wired", "https://www.wired.com/feed/rss", "RSS", "科技媒体", "EN", 13),
                createSource("Hacker News", "Hacker News (RSS)", "https://news.ycombinator.com/rss", "RSS", "科技媒体", "EN", 14),
                createSource("Dev.to", "Dev.to", "https://dev.to/feed", "RSS", "开源开发者", "EN", 15),
                createSource("CSS-Tricks", "CSS-Tricks", "https://css-tricks.com/feed/", "RSS", "开源开发者", "EN", 16),
                createSource("Smashing Magazine", "Smashing Mag", "https://www.smashingmagazine.com/feed/", "RSS", "开源开发者", "EN", 17),
                createSource("Product Hunt", "Product Hunt", "https://www.producthunt.com/feed", "RSS", "科技媒体", "EN", 18),
                createSource("36氪", "36氪", "https://36kr.com/feed", "RSS", "科技财经", "CN", 20),
                createSource("虎嗅", "虎嗅", "https://www.huxiu.com/rss/0.xml", "RSS", "科技财经", "CN", 21),
                createSource("少数派", "少数派", "https://sspai.com/feed", "RSS", "科技媒体", "CN", 22),
                createSource("InfoQ", "InfoQ", "https://www.infoq.cn/feed", "RSS", "开源开发者", "CN", 23),
                createSource("V2EX", "V2EX", "https://www.v2ex.com/feed/tab/tech.xml", "RSS", "开源开发者", "CN", 24)
        );
    }

    /**
     * 航空公司新闻源
     */
    public static List<NewsSource> getAirlineSources() {
        return List.of(
                createSource("Emirates News", "Emirates", "https://www.emirates.com/english/about-us/news-and-media/", "JSOUP", "国际航司", "EN", 50),
                createSource("Qatar Airways News", "Qatar Airways", "https://www.qatarairways.com/en/press-releases.html", "JSOUP", "国际航司", "EN", 51),
                createSource("Singapore Airlines News", "Singapore Airlines", "https://www.singaporeair.com/en_UK/sg/media-centre/news/", "JSOUP", "国际航司", "EN", 52),
                createSource("ANA News", "ANA", "https://www.ana.co.jp/en/us/", "JSOUP", "国际航司", "EN", 53),
                createSource("Lufthansa News", "Lufthansa", "https://www.lufthansa.com/us/en/press-releases", "JSOUP", "国际航司", "EN", 54)
        );
    }

    /**
     * 补充新闻源（中文热榜、英语外刊等）
     */
    public static List<NewsSource> getSupplementarySources() {
        return List.of(
                // 官方媒体
                createSource("中国新闻网", "中国新闻网", "https://www.chinanews.com/rss/scroll-news.xml", "RSS", "官方媒体", "CN", 6),
                createSource("环球时报", "环球时报", "https://www.globaltimes.cn/rss/outbrain.xml", "RSS", "官方媒体", "EN", 7),
                createSource("人民日报国际", "人民日报", "http://www.people.com.cn/rss/world.xml", "RSS", "官方媒体", "CN", 3),
                createSource("新华网", "新华网", "http://www.news.cn/politics/xhll.xml", "RSS", "官方媒体", "CN", 120),
                createSource("央视新闻", "央视新闻", "https://news.cctv.com/2019/07/ga546k0esaonj2ta4n0gimlk2q6o8l13.xml", "RSS", "官方媒体", "CN", 121),
                createSource("凤凰网", "凤凰网", "https://news.ifeng.com/rss/index.xml", "RSS", "官方媒体", "CN", 122),
                // 中文热榜
                createSource("今日头条热榜", "今日头条", "TOUTIAIO_API", "JSOUP", "社交媒体", "CN", 104),
                createSource("百度热搜", "百度热搜", "BAIDU_API", "JSOUP", "社交媒体", "CN", 105),
                createSource("抖音热榜", "抖音热榜", "DOUYIN_API", "JSOUP", "社交媒体", "CN", 106),
                // 英语外刊
                createSource("BBC Learning English", "BBC", "https://feeds.bbci.co.uk/learningenglish/english/rss", "RSS", "英语外刊", "EN", 110),
                createSource("VOA English", "VOA", "https://www.voanews.com/api/z-qmget_qm_", "RSS", "英语外刊", "EN", 111),
                createSource("The Economist", "The Economist", "https://www.economist.com/latest/rss.xml", "RSS", "英语外刊", "EN", 112),
                createSource("BBC News", "BBC News", "https://feeds.bbci.co.uk/news/world/rss.xml", "RSS", "英语外刊", "EN", 113),
                createSource("Reuters", "Reuters", "https://www.reutersagency.com/feed/", "RSS", "英语外刊", "EN", 114),
                createSource("The Guardian", "The Guardian", "https://www.theguardian.com/world/rss", "RSS", "英语外刊", "EN", 115),
                // 科技财经
                createSource("量子位", "量子位", "https://www.qbitai.com/feed", "RSS", "科技财经", "CN", 103),
                createSource("证券时报", "证券时报", "https://www.stcn.com/rss", "RSS", "科技财经", "CN", 130),
                createSource("第一财经", "第一财经", "https://www.yicai.com/feed/rss", "RSS", "科技财经", "CN", 131),
                createSource("FT中文网", "FT中文网", "https://www.ftchinese.com/rss/feed", "RSS", "科技财经", "CN", 140),
                createSource("华尔街见闻", "华尔街见闻", "https://wallstreetcn.com/rss", "RSS", "科技财经", "CN", 141)
        );
    }

    /**
     * 需要禁用的失效源名称
     */
    public static List<String> getFailedSourceNames() {
        return List.of(
                "新华社", "机器之心", "果壳", "知乎热榜",
                "cnBeta", "品玩", "极客公园",
                "HackerNews", "GitHub Trending"
        );
    }

    /**
     * 需要禁用的源名称前缀
     */
    public static List<String> getFailedPrefixes() {
        return List.of("Google-", "YouTube-");
    }

    private static NewsSource createSource(String name, String platformName, String feedUrl,
                                           String fetchMethod, String category, String language, int priority) {
        NewsSource source = new NewsSource();
        source.setName(name);
        source.setPlatformName(platformName);
        source.setFeedUrl(feedUrl);
        source.setFetchMethod(fetchMethod);
        source.setCategory(category);
        source.setLanguage(language);
        source.setPriority(priority);
        source.setEnabled(true);
        source.setErrorCount(0);
        source.setConsecutiveErrors(0);
        return source;
    }
}
