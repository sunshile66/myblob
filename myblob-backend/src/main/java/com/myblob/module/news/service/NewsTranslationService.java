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
        // Short timeout since translate API is unreliable in China
        this.restTemplate = new RestTemplate();
        this.restTemplate.setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
            setConnectTimeout(3000);
            setReadTimeout(3000);
        }});
    }

    private static final String TRANSLATE_API =
            "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=zh-CN&dt=t&q=";

    /**
     * Translate English text to Chinese.
     * Returns null if translation fails.
     */
    public String translate(String text) {
        if (text == null || text.isBlank() || text.length() > 2000) {
            return null;
        }
        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = TRANSLATE_API + encoded;
            String response = restTemplate.getForObject(url, String.class);
            if (response == null) return null;

            // Parse Google Translate response: [[["translated text","original",...]],...]
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
            log.debug("Translation failed: {}", e.getMessage());
            return null;
        }
    }
}
