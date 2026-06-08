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
 * 从新闻标题中提取话题关键词（基于领域词典 + 词频统计）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsTopicService {

    private final NewsItemRepository newsItemRepository;

    /** 领域词典：关键词 -> 标准话题标签 */
    private static final Map<String, String> TOPIC_DICT = new LinkedHashMap<>();
    static {
        // AI / 人工智能
        TOPIC_DICT.put("人工智能", "人工智能"); TOPIC_DICT.put("AI", "AI"); TOPIC_DICT.put("GPT", "GPT");
        TOPIC_DICT.put("ChatGPT", "ChatGPT"); TOPIC_DICT.put("大模型", "大模型"); TOPIC_DICT.put("LLM", "LLM");
        TOPIC_DICT.put("机器学习", "机器学习"); TOPIC_DICT.put("深度学习", "深度学习");
        TOPIC_DICT.put("算力", "算力"); TOPIC_DICT.put("GPU", "GPU"); TOPIC_DICT.put("芯片", "芯片");
        TOPIC_DICT.put("豆包", "豆包"); TOPIC_DICT.put("Kimi", "Kimi"); TOPIC_DICT.put("通义", "通义千问");
        TOPIC_DICT.put("Sora", "Sora"); TOPIC_DICT.put("Gemini", "Gemini"); TOPIC_DICT.put("Claude", "Claude");
        TOPIC_DICT.put("智能体", "智能体"); TOPIC_DICT.put("Agent", "Agent"); TOPIC_DICT.put("RAG", "RAG");
        TOPIC_DICT.put("多模态", "多模态"); TOPIC_DICT.put("AIGC", "AIGC"); TOPIC_DICT.put("生成式", "生成式AI");

        // 科技公司
        TOPIC_DICT.put("苹果", "Apple"); TOPIC_DICT.put("Apple", "Apple"); TOPIC_DICT.put("iPhone", "iPhone");
        TOPIC_DICT.put("谷歌", "Google"); TOPIC_DICT.put("Google", "Google");
        TOPIC_DICT.put("微软", "Microsoft"); TOPIC_DICT.put("Microsoft", "Microsoft");
        TOPIC_DICT.put("特斯拉", "Tesla"); TOPIC_DICT.put("Tesla", "Tesla");
        TOPIC_DICT.put("英伟达", "NVIDIA"); TOPIC_DICT.put("NVIDIA", "NVIDIA");
        TOPIC_DICT.put("华为", "华为"); TOPIC_DICT.put("小米", "小米"); TOPIC_DICT.put("OPPO", "OPPO");
        TOPIC_DICT.put("vivo", "vivo"); TOPIC_DICT.put("荣耀", "荣耀"); TOPIC_DICT.put("一加", "一加");
        TOPIC_DICT.put("字节", "字节跳动"); TOPIC_DICT.put("字节跳动", "字节跳动"); TOPIC_DICT.put("抖音", "抖音");
        TOPIC_DICT.put("腾讯", "腾讯"); TOPIC_DICT.put("微信", "微信"); TOPIC_DICT.put("阿里", "阿里巴巴");
        TOPIC_DICT.put("阿里巴巴", "阿里巴巴"); TOPIC_DICT.put("百度", "百度"); TOPIC_DICT.put("京东", "京东");
        TOPIC_DICT.put("美团", "美团"); TOPIC_DICT.put("拼多多", "拼多多"); TOPIC_DICT.put("网易", "网易");
        TOPIC_DICT.put("Meta", "Meta"); TOPIC_DICT.put("OpenAI", "OpenAI");
        TOPIC_DICT.put("三星", "三星"); TOPIC_DICT.put("Samsung", "三星");
        TOPIC_DICT.put("台积电", "台积电"); TOPIC_DICT.put("TSMC", "台积电");

        // 汽车/出行
        TOPIC_DICT.put("新能源汽车", "新能源汽车"); TOPIC_DICT.put("电动车", "电动车");
        TOPIC_DICT.put("自动驾驶", "自动驾驶"); TOPIC_DICT.put("智驾", "智驾");
        TOPIC_DICT.put("小米汽车", "小米汽车"); TOPIC_DICT.put("小米SU", "小米汽车");
        TOPIC_DICT.put("比亚迪", "比亚迪"); TOPIC_DICT.put("蔚来", "蔚来"); TOPIC_DICT.put("理想汽车", "理想汽车");
        TOPIC_DICT.put("小鹏", "小鹏汽车"); TOPIC_DICT.put("极氪", "极氪");
        TOPIC_DICT.put("航空", "航空"); TOPIC_DICT.put("航线", "航线"); TOPIC_DICT.put("航班", "航班");
        TOPIC_DICT.put("波音", "波音"); TOPIC_DICT.put("空客", "空客");

        // 金融/商业
        TOPIC_DICT.put("融资", "融资"); TOPIC_DICT.put("IPO", "IPO"); TOPIC_DICT.put("上市", "上市");
        TOPIC_DICT.put("股票", "股票"); TOPIC_DICT.put("A股", "A股"); TOPIC_DICT.put("美股", "美股");
        TOPIC_DICT.put("港股", "港股"); TOPIC_DICT.put("比特币", "比特币"); TOPIC_DICT.put("加密货币", "加密货币");
        TOPIC_DICT.put("区块链", "区块链"); TOPIC_DICT.put("投资", "投资");
        TOPIC_DICT.put("财报", "财报"); TOPIC_DICT.put("营收", "营收"); TOPIC_DICT.put("利润", "利润");

        // 开发者/开源
        TOPIC_DICT.put("开源", "开源"); TOPIC_DICT.put("GitHub", "GitHub"); TOPIC_DICT.put("Linux", "Linux");
        TOPIC_DICT.put("Python", "Python"); TOPIC_DICT.put("Rust", "Rust"); TOPIC_DICT.put("Go", "Go");
        TOPIC_DICT.put("React", "React"); TOPIC_DICT.put("Vue", "Vue"); TOPIC_DICT.put("Flutter", "Flutter");
        TOPIC_DICT.put("Docker", "Docker"); TOPIC_DICT.put("Kubernetes", "Kubernetes");
        TOPIC_DICT.put("数据库", "数据库"); TOPIC_DICT.put("API", "API"); TOPIC_DICT.put("框架", "框架");

        // 社会/民生
        TOPIC_DICT.put("疫情", "疫情"); TOPIC_DICT.put("地震", "地震"); TOPIC_DICT.put("洪水", "洪水");
        TOPIC_DICT.put("高考", "高考"); TOPIC_DICT.put("考研", "考研"); TOPIC_DICT.put("就业", "就业");
        TOPIC_DICT.put("裁员", "裁员"); TOPIC_DICT.put("房价", "房价"); TOPIC_DICT.put("楼市", "楼市");

        // 国际
        TOPIC_DICT.put("贸易战", "贸易战"); TOPIC_DICT.put("关税", "关税"); TOPIC_DICT.put("制裁", "制裁");
        TOPIC_DICT.put("中美", "中美关系"); TOPIC_DICT.put("俄乌", "俄乌冲突");
    }

    /**
     * 从新闻标题中提取话题标签（1-3个）
     */
    public List<String> extractTopics(String title) {
        if (title == null || title.isEmpty()) return Collections.emptyList();
        List<String> topics = new ArrayList<>();
        Set<String> added = new HashSet<>();

        for (Map.Entry<String, String> entry : TOPIC_DICT.entrySet()) {
            String keyword = entry.getKey();
            String topic = entry.getValue();
            if (title.contains(keyword) && !added.contains(topic)) {
                topics.add(topic);
                added.add(topic);
                if (topics.size() >= 3) break;
            }
        }
        return topics;
    }

    /**
     * 批量提取话题并存入 NewsItem.topics 字段
     */
    public void extractTopics(List<NewsItem> items) {
        for (NewsItem item : items) {
            List<String> topics = extractTopics(item.getTitle());
            if (!topics.isEmpty()) {
                item.setTopics(String.join(",", topics));
            }
        }
    }

    /**
     * 统计最近 24h 内热门话题 Top N
     */
    public List<Map<String, Object>> getTrendingTopics(int limit) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<NewsItem> recentItems = newsItemRepository.findByPublishedAtAfter(since);

        Map<String, Integer> topicCount = new LinkedHashMap<>();
        Map<String, String> topicSentiment = new HashMap<>();

        for (NewsItem item : recentItems) {
            if (item.getTopics() == null || item.getTopics().isEmpty()) continue;
            String[] topics = item.getTopics().split(",");
            for (String t : topics) {
                String topic = t.trim();
                if (topic.isEmpty()) continue;
                topicCount.merge(topic, 1, Integer::sum);
                // 取最近一条的情感作为该话题的情感
                if (item.getSentiment() != null) {
                    topicSentiment.put(topic, item.getSentiment());
                }
            }
        }

        return topicCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("topic", e.getKey());
                    m.put("count", e.getValue());
                    m.put("sentiment", topicSentiment.getOrDefault(e.getKey(), "neutral"));
                    return m;
                })
                .collect(Collectors.toList());
    }
}
