package com.myblob.module.news.service;

import com.myblob.module.news.entity.NewsItem;
import com.myblob.module.news.entity.NewsSource;

import java.util.List;

/**
 * 新闻抓取策略接口
 */
public interface NewsFetchStrategy {

    /**
     * 判断是否支持该抓取方法
     */
    boolean supports(String method);

    /**
     * 从新闻源抓取新闻条目
     */
    List<NewsItem> fetch(NewsSource source);
}
