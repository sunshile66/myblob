package com.myblob.module.news.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "news")
public class NewsProxyConfig {

    private Global global = new Global();
    private Proxy proxy = new Proxy();
    private Fetch fetch = new Fetch();

    @Data
    public static class Global {
        private boolean enabled = true;
    }

    @Data
    public static class Proxy {
        private RssHub rsshub = new RssHub();
        private Http http = new Http();
        private String userAgent = "MyBlob-NewsBot/1.0";
        private int connectTimeout = 10000;
        private int readTimeout = 15000;

        @Data
        public static class RssHub {
            private String baseUrl = "http://localhost:1200";
            private int timeout = 30000;
            private int retry = 3;
        }

        @Data
        public static class Http {
            private String host = "";
            private int port = 0;
            private boolean enabled = false;
        }
    }

    @Data
    public static class Fetch {
        private int threadPoolSize = 20;
        private int maxItemsPerSource = 50;
        private boolean translateEnabled = false;
    }

    public String resolveFeedUrl(String feedUrl) {
        if (feedUrl != null && feedUrl.contains("{rsshub}")) {
            return feedUrl.replace("{rsshub}", proxy.getRsshub().getBaseUrl());
        }
        return feedUrl;
    }
}
