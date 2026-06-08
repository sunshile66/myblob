package com.myblob.module.knowledge.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblob.module.knowledge.entity.VocabularyItem;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VocabularyDataLoader {

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        log.info("VocabularyDataLoader initialized (dictionary mode)");
    }

    /**
     * 从 classpath:vocabulary/ 加载结构化字典JSON，映射到 VocabularyItem。
     * JSON格式:
     * [{"word":"abandon","usPhonetic":"...","ukPhonetic":"...",
     *   "meanings":[{"pos":"v","def":"...","examples":[{"en":"...","cn":"..."}]}],
     *   "phrases":[{"phrase":"...","translation":"..."}],"difficulty":3}]
     */
    public List<VocabularyItem> loadCategory(String category, String fileName) {
        try {
            String path = "classpath:vocabulary/" + fileName;
            Resource resource = resourceLoader.getResource(path);
            if (!resource.exists()) {
                log.warn("Vocabulary resource not found: {}", path);
                return List.of();
            }
            try (InputStream is = resource.getInputStream()) {
                List<Map<String, Object>> raw = objectMapper.readValue(is,
                    new TypeReference<List<Map<String, Object>>>() {});
                List<VocabularyItem> items = new ArrayList<>();
                for (Map<String, Object> entry : raw) {
                    VocabularyItem item = buildItem(entry, category);
                    items.add(item);
                }
                log.info("Loaded {} vocabulary items (dictionary) from {}", items.size(), fileName);
                return items;
            }
        } catch (Exception e) {
            log.error("Failed to load vocabulary from {}: {}", fileName, e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private VocabularyItem buildItem(Map<String, Object> entry, String category) {
        String word = (String) entry.get("word");
        String usPhonetic = (String) entry.getOrDefault("usPhonetic", "");
        String ukPhonetic = (String) entry.getOrDefault("ukPhonetic", "");
        int difficulty = ((Number) entry.getOrDefault("difficulty", 1)).intValue();

        // Parse meanings
        List<Map<String, Object>> meaningsRaw = (List<Map<String, Object>>) entry.get("meanings");
        String firstPos = "";
        String firstDef = "";
        String firstExEn = "";
        String firstExCn = "";

        if (meaningsRaw != null && !meaningsRaw.isEmpty()) {
            Map<String, Object> firstMeaning = meaningsRaw.get(0);
            firstPos = (String) firstMeaning.getOrDefault("pos", "");
            firstDef = (String) firstMeaning.getOrDefault("def", "");

            List<Map<String, String>> examples = (List<Map<String, String>>) firstMeaning.get("examples");
            if (examples != null && !examples.isEmpty()) {
                firstExEn = examples.get(0).getOrDefault("en", "");
                firstExCn = examples.get(0).getOrDefault("cn", "");
            }
        }

        // Serialize meanings and phrases back to JSON strings for storage
        String meaningsJson = null;
        String phrasesJson = null;
        try {
            if (meaningsRaw != null && !meaningsRaw.isEmpty()) {
                meaningsJson = objectMapper.writeValueAsString(meaningsRaw);
            }
            List<Map<String, String>> phrasesRaw = (List<Map<String, String>>) entry.get("phrases");
            if (phrasesRaw != null && !phrasesRaw.isEmpty()) {
                phrasesJson = objectMapper.writeValueAsString(phrasesRaw);
            }
        } catch (Exception e) {
            log.warn("Failed to serialize meanings/phrases for word '{}': {}", word, e.getMessage());
        }

        return VocabularyItem.builder()
            .word(word)
            .phonetic(usPhonetic)
            .ukPhonetic(ukPhonetic)
            .partOfSpeech(firstPos)
            .definition(firstDef)
            .exampleSentence(firstExEn)
            .exampleTranslation(firstExCn)
            .category(category)
            .difficulty(difficulty)
            .meaningsJson(meaningsJson)
            .phrasesJson(phrasesJson)
            .build();
    }
}
