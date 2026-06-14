<template>
  <KnowledgeLayout>
  <div class="vocab-detail-page">
    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="8" animated />
    </div>

    <div v-else-if="error" class="error-wrap">
      <el-empty :description="error">
        <el-button type="primary" @click="$router.push('/knowledge/vocabulary')">返回词汇列表</el-button>
      </el-empty>
    </div>

    <div v-else-if="word" class="dictionary">
      <!-- 单词标题区 -->
      <div class="word-header">
        <h1 class="word-title">{{ word.word }}</h1>
        <div class="word-phonetics">
          <span v-if="word.phonetic" class="phonetic-us">
            <span class="phonetic-label">美</span>
            <span class="phonetic-text">/{{ word.phonetic }}/</span>
            <span class="speaker-btn" @click="speakWord(word.word)">
              <el-icon :size="18"><Headset /></el-icon>
            </span>
          </span>
          <span v-if="word.ukPhonetic" class="phonetic-uk">
            <span class="phonetic-label">英</span>
            <span class="phonetic-text">/{{ word.ukPhonetic }}/</span>
            <span class="speaker-btn" @click="speakWord(word.word, 'en-GB')">
              <el-icon :size="18"><Headset /></el-icon>
            </span>
          </span>
        </div>
        <div class="word-meta">
          <span class="difficulty-stars">
            <span v-for="i in 5" :key="i" class="star" :class="{ active: i <= word.difficulty }">★</span>
          </span>
          <el-tag size="small" effect="plain">{{ word.category }}</el-tag>
          <span v-if="word.partOfSpeech" class="primary-pos">{{ word.partOfSpeech }}</span>
        </div>
      </div>

      <!-- 义项列表 -->
      <section class="section" v-if="word.meanings && word.meanings.length > 0">
        <h2 class="section-title">释义</h2>
        <div class="meanings-list">
          <div v-for="(m, mi) in word.meanings" :key="mi" class="meaning-item">
            <div class="meaning-head">
              <el-tag v-if="m.pos" size="small" type="info" effect="dark">{{ posLabel(m.pos) }}</el-tag>
              <span class="meaning-def">{{ m.def }}</span>
            </div>
            <div v-if="m.examples && m.examples.length > 0" class="meaning-examples">
              <div v-for="(ex, ei) in m.examples" :key="ei" class="example-item">
                <p class="example-en">{{ ex.en }}</p>
                <p class="example-cn">{{ ex.cn }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 常用短语 -->
      <section class="section" v-if="word.phrases && word.phrases.length > 0">
        <h2 class="section-title">常用短语</h2>
        <div class="phrases-list">
          <div v-for="(p, pi) in word.phrases" :key="pi" class="phrase-item">
            <span class="phrase-text">{{ p.phrase }}</span>
            <span class="phrase-dash">—</span>
            <span class="phrase-trans">{{ p.translation }}</span>
          </div>
        </div>
      </section>

      <!-- 原有例句区（向后兼容：如果无meanings但有旧字段） -->
      <section class="section" v-if="(!word.meanings || word.meanings.length === 0) && word.exampleSentence">
        <h2 class="section-title">例句</h2>
        <div class="legacy-example">
          <p class="example-en">{{ word.exampleSentence }}</p>
          <p class="example-cn" v-if="word.exampleTranslation">{{ word.exampleTranslation }}</p>
        </div>
      </section>
    </div>
  </div>
  </KnowledgeLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Headset } from '@element-plus/icons-vue'
import { getVocabularyDetail, type VocabularyItem } from '@/api/knowledge'
import KnowledgeLayout from './components/KnowledgeLayout.vue'

const route = useRoute()

const word = ref<VocabularyItem | null>(null)
const loading = ref(true)
const error = ref('')

const posLabel = (pos: string): string => {
  const map: Record<string, string> = {
    'n': '名词', 'noun': '名词',
    'v': '动词', 'verb': '动词',
    'vi': '不及物动词', 'vt': '及物动词',
    'adj': '形容词', 'adjective': '形容词',
    'adv': '副词', 'adverb': '副词',
    'prep': '介词', 'preposition': '介词',
    'conj': '连词', 'conjunction': '连词',
    'pron': '代词', 'pronoun': '代词',
    'interj': '感叹词', 'interjection': '感叹词',
    'art': '冠词', 'article': '冠词',
    'num': '数词', 'numeral': '数词',
    'aux': '助动词',
    'phr': '短语',
  }
  return map[pos.toLowerCase()] || pos
}

const speakWord = (text: string, lang = 'en-US') => {
  if ('speechSynthesis' in window) {
    window.speechSynthesis.cancel()
    const utterance = new SpeechSynthesisUtterance(text)
    utterance.lang = lang
    utterance.rate = 0.8
    utterance.pitch = 1
    window.speechSynthesis.speak(utterance)
  }
}

onMounted(async () => {
  const id = Number(route.params.id)
  if (!id || isNaN(id)) {
    error.value = '无效的词汇ID'
    loading.value = false
    return
  }
  try {
    const res = await getVocabularyDetail(id)
    const data = (res as any).data || res
    if (data) {
      word.value = data
    } else {
      error.value = '词汇不存在'
    }
  } catch {
    error.value = '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.vocab-detail-page {
  max-width: 800px;
  margin: 0 auto;
}

.loading-wrap, .error-wrap {
  padding: 60px 0;
}

.word-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 36px 24px 32px;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-xl);
}

.word-title {
  font-size: 42px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0 0 16px;
  letter-spacing: 1px;
}

.word-phonetics {
  display: flex;
  justify-content: center;
  gap: 28px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.phonetic-us, .phonetic-uk {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
}

.phonetic-label {
  font-size: 11px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: var(--radius-xs);
  color: #fff;
}

.phonetic-us .phonetic-label { background: #667eea; }
.phonetic-uk .phonetic-label { background: #e07b5a; }

.phonetic-text {
  font-family: 'Times New Roman', 'Arial Unicode MS', serif;
  color: var(--theme-text-secondary);
}

.speaker-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  cursor: pointer;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.speaker-btn:hover {
  transform: scale(1.15);
  box-shadow: 0 3px 12px rgba(102, 126, 234, 0.35);
}

.word-meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  flex-wrap: wrap;
}

.difficulty-stars {
  display: flex;
  gap: 1px;
}

.star {
  color: var(--theme-border);
  font-size: 15px;
}

.star.active {
  color: #f0ad4e;
}

.primary-pos {
  font-size: 13px;
  color: var(--theme-text-secondary);
  text-transform: capitalize;
}

/* ==================== 通用区块 ==================== */
.section {
  margin-bottom: 36px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--theme-text);
  margin: 0 0 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid var(--theme-border);
}

/* ==================== 义项列表 ==================== */
.meanings-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.meaning-item {
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-lg);
  padding: 20px 22px;
}

.meaning-head {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.meaning-def {
  font-size: 16px;
  font-weight: 500;
  color: var(--theme-text);
  line-height: 1.5;
  flex: 1;
}

.meaning-examples {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-left: 6px;
  padding-left: 16px;
  border-left: 3px solid var(--theme-border);
}

.example-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.example-en {
  font-size: 14px;
  color: var(--theme-text);
  font-style: italic;
  margin: 0;
  line-height: 1.5;
}

.example-cn {
  font-size: 13px;
  color: var(--theme-text-secondary);
  margin: 0;
  line-height: 1.4;
}

/* ==================== 短语列表 ==================== */
.phrases-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.phrase-item {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding: 13px 18px;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-md);
}

.phrase-text {
  font-size: 15px;
  font-weight: 600;
  color: #667eea;
  white-space: nowrap;
}

.phrase-dash {
  color: var(--theme-border);
  flex-shrink: 0;
}

.phrase-trans {
  font-size: 14px;
  color: var(--theme-text-secondary);
  line-height: 1.4;
}

/* ==================== 旧版例句兼容 ==================== */
.legacy-example {
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-lg);
  padding: 20px 22px;
}

.legacy-example .example-en {
  margin-bottom: 6px;
}

@media (max-width: 768px) {
  .vocab-detail-page { padding: 20px 16px; }
  .word-title { font-size: 32px; }
  .word-header { padding: 28px 16px 24px; }
  .word-phonetics { gap: 16px; }
}

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
