package com.myblob.module.knowledge.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblob.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "vocabulary_item", indexes = {
        @Index(name = "idx_vocab_word", columnList = "word"),
        @Index(name = "idx_vocab_category", columnList = "category"),
        @Index(name = "idx_vocab_difficulty", columnList = "difficulty")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class VocabularyItem extends BaseEntity {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 英文单词 */
    @Column(nullable = false, length = 100)
    private String word;

    /** 美式音标 */
    @Column(length = 200)
    private String phonetic;

    /** 英式音标 */
    @Column(name = "uk_phonetic", length = 200)
    private String ukPhonetic;

    /** 词性: noun/verb/adjective/adverb/preposition/conjunction/pronoun/interjection */
    @Column(name = "part_of_speech", length = 30)
    private String partOfSpeech;

    /** 中文释义 */
    @Column(nullable = false, length = 500)
    private String definition;

    /** 英文例句 */
    @Column(name = "example_sentence", length = 1000)
    private String exampleSentence;

    /** 例句中文翻译 */
    @Column(name = "example_translation", length = 500)
    private String exampleTranslation;

    /** 难度 1-5 */
    @Column(nullable = false)
    @Builder.Default
    private Integer difficulty = 1;

    /** 分类: CET4/CET6/IELTS/TOEFL/考研/GRE/商务英语 */
    @Column(nullable = false, length = 50)
    private String category;

    /** 发音音频URL（可选） */
    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    // ==================== 字典化扩展字段 ====================

    /** 多义项JSON: [{"pos":"n","def":"释义","examples":[{"en":"...","cn":"..."}]}] */
    @JsonIgnore
    @Column(name = "meanings_json", columnDefinition = "TEXT")
    private String meaningsJson;

    /** 常用短语JSON: [{"phrase":"...","translation":"..."}] */
    @JsonIgnore
    @Column(name = "phrases_json", columnDefinition = "TEXT")
    private String phrasesJson;

    // ==================== 字典化结构化getter ====================

    @JsonProperty("meanings")
    public List<Meaning> getMeanings() {
        if (meaningsJson == null || meaningsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(meaningsJson, new TypeReference<List<Meaning>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @JsonProperty("phrases")
    public List<Phrase> getPhrases() {
        if (phrasesJson == null || phrasesJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(phrasesJson, new TypeReference<List<Phrase>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // ==================== 内嵌DTO ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meaning {
        private String pos;
        private String def;
        private List<Example> examples;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Example {
        private String en;
        private String cn;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Phrase {
        private String phrase;
        private String translation;
    }
}
