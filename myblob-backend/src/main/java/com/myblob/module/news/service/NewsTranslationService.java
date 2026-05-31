package com.myblob.module.news.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class NewsTranslationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewsTranslationService() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {
            {
                setConnectTimeout(5000);
                setReadTimeout(5000);
            }
        });
    }

    // MyMemory API - accessible from China, free, no key needed
    private static final String MYMEMORY_API = "https://api.mymemory.translated.net/get?q=%s&langpair=en|zh-CN";

    // Google Translate fallback
    private static final String GOOGLE_API = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=zh-CN&dt=t&q=";

    /**
     * Translate English text to Chinese.
     * Tries MyMemory first, then falls back to Google.
     */
    public String translate(String text) {
        if (text == null || text.isBlank() || text.length() > 2000) {
            return null;
        }

        // Try MyMemory first (accessible from China)
        String result = translateMyMemory(text);
        if (result != null)
            return result;

        // Fallback to Google (may be blocked)
        return translateGoogle(text);
    }

    private String translateMyMemory(String text) {
        try {
            // MyMemory has a 500 char limit per request
            String truncated = text.length() > 500 ? text.substring(0, 500) : text;
            String encoded = URLEncoder.encode(truncated, StandardCharsets.UTF_8);
            String url = String.format(MYMEMORY_API, encoded);
            String response = restTemplate.getForObject(url, String.class);
            if (response == null)
                return null;

            JsonNode root = objectMapper.readTree(response);
            JsonNode responseData = root.get("responseData");
            if (responseData == null)
                return null;

            String translated = responseData.get("translatedText").asText();
            if (translated == null || translated.isBlank())
                return null;

            // MyMemory sometimes returns the original text if it can't translate
            if (translated.equalsIgnoreCase(text))
                return null;

            return translated.trim();
        } catch (Exception e) {
            log.debug("MyMemory translation failed: {}", e.getMessage());
            return null;
        }
    }

    private String translateGoogle(String text) {
        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = GOOGLE_API + encoded;
            String response = restTemplate.getForObject(url, String.class);
            if (response == null)
                return null;

            JsonNode root = objectMapper.readTree(response);
            StringBuilder result = new StringBuilder();
            for (JsonNode sentence : root.get(0)) {
                String part = sentence.get(0).asText();
                if (part != null && !part.isBlank()) {
                    result.append(part);
                }
            }
            String translated = result.toString().trim();
            return translated.isEmpty() ? null : translated;
        } catch (Exception e) {
            log.debug("Google translation failed: {}", e.getMessage());
            return null;
        }
    }
}
