<template>
  <KnowledgeLayout>
  <div class="vocab-page">
    <div class="page-header">
      <h1>英语词汇</h1>
      <p>点击卡片翻转到背面查看释义和例句，点击「字典详情」查看完整字典页</p>
    </div>

    <!-- 筛选条 -->
    <div class="filter-bar">
      <el-select v-model="selectedCategory" placeholder="分类" clearable @change="fetchList">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
      <el-select v-model="selectedDifficulty" placeholder="难度" clearable @change="fetchList">
        <el-option label="★ 1" :value="1" />
        <el-option label="★★ 2" :value="2" />
        <el-option label="★★★ 3" :value="3" />
        <el-option label="★★★★ 4" :value="4" />
        <el-option label="★★★★★ 5" :value="5" />
      </el-select>
      <el-input
        v-model="searchQuery"
        placeholder="搜索单词..."
        clearable
        @clear="fetchList"
        @keyup.enter="fetchList"
        style="max-width: 240px"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="$router.push('/knowledge/vocabulary/quiz')">
        <el-icon><EditPen /></el-icon> 词汇测验
      </el-button>
    </div>

    <!-- 词汇卡片网格 -->
    <div class="vocab-grid" v-loading="loading">
      <div
        v-for="item in words"
        :key="item.id"
        class="flip-card"
        :class="{ flipped: item._flipped }"
      >
        <div class="flip-card-inner">
          <!-- 正面：单词 + 音标 + 发音按钮 -->
          <div class="flip-card-front" @click="item._flipped = !item._flipped">
            <div class="card-word">{{ item.word }}</div>
            <div class="card-phonetic">{{ item.phonetic }}</div>
            <div class="card-speaker" @click.stop="speakWord(item.word)">
              <el-icon :size="22"><Headset /></el-icon>
            </div>
            <div class="card-difficulty">
              <span v-for="i in 5" :key="i" class="star" :class="{ active: i <= item.difficulty }">★</span>
            </div>
            <div class="card-category-tag">{{ item.category }}</div>
          </div>
          <!-- 背面：释义 + 词性 + 例句 + 详情按钮 -->
          <div class="flip-card-back" @click="item._flipped = !item._flipped">
            <div class="card-pos">{{ item.partOfSpeech }}</div>
            <div class="card-definition">{{ item.definition }}</div>
            <div class="card-example" v-if="item.exampleSentence">
              <div class="example-en">{{ item.exampleSentence }}</div>
              <div class="example-cn" v-if="item.exampleTranslation">{{ item.exampleTranslation }}</div>
            </div>
            <div class="card-detail-btn" @click.stop="$router.push(`/knowledge/vocabulary/${item.id}`)">
              <el-icon :size="16"><Document /></el-icon> 字典详情
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && words.length === 0" description="暂无词汇数据" />

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="onPageChange"
      />
    </div>
  </div>
  </KnowledgeLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search, Headset, EditPen, Document } from '@element-plus/icons-vue'
import { getVocabularyList, getVocabularyCategories, type VocabularyItem } from '@/api/knowledge'
import KnowledgeLayout from './components/KnowledgeLayout.vue'

interface FlippedVocab extends VocabularyItem { _flipped?: boolean }

const words = ref<FlippedVocab[]>([])
const categories = ref<string[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = 20
const total = ref(0)
const selectedCategory = ref('')
const selectedDifficulty = ref<number | undefined>(undefined)
const searchQuery = ref('')

const speakWord = (word: string) => {
  if ('speechSynthesis' in window) {
    window.speechSynthesis.cancel()
    const utterance = new SpeechSynthesisUtterance(word)
    utterance.lang = 'en-US'
    utterance.rate = 0.8
    utterance.pitch = 1
    window.speechSynthesis.speak(utterance)
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const params: any = { page: currentPage.value - 1, size: pageSize }
    if (selectedCategory.value) params.category = selectedCategory.value
    if (selectedDifficulty.value) params.difficulty = selectedDifficulty.value
    if (searchQuery.value) params.search = searchQuery.value
    const res = await getVocabularyList(params)
    const data = (res as any).data || res
    words.value = (data.content || []).map((v: VocabularyItem) => ({ ...v, _flipped: false }))
    total.value = data.totalElements || 0
  } finally {
    loading.value = false
  }
}

const onPageChange = (p: number) => {
  currentPage.value = p
  fetchList()
}

onMounted(async () => {
  try {
    const res = await getVocabularyCategories()
    categories.value = (res as any).data || res
  } catch { /* ignore */ }
  fetchList()
})
</script>

<style scoped>
.vocab-page {
  padding: 0;
}

.page-header {
  text-align: center;
  margin-bottom: 28px;
}
.page-header h1 { font-size: 30px; font-weight: 700; color: var(--theme-text); margin: 0 0 8px; }
.page-header p { font-size: 14px; color: var(--theme-text-secondary); margin: 0; }

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 28px;
  flex-wrap: wrap;
  justify-content: center;
}

/* 翻转卡片 */
.vocab-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 18px;
}

.flip-card {
  perspective: 1000px;
  height: 240px;
  cursor: pointer;
}

.flip-card-inner {
  position: relative;
  width: 100%;
  height: 100%;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  transform-style: preserve-3d;
}

.flip-card.flipped .flip-card-inner {
  transform: rotateY(180deg);
}

.flip-card-front,
.flip-card-back {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  border-radius: 14px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  border: 1px solid var(--theme-border);
}

.flip-card-front {
  background: var(--theme-card);
  z-index: 2;
  box-shadow: 0 1px 3px rgba(0,0,0,.05);
  transition: box-shadow 0.2s;
}
.flip-card:hover .flip-card-front {
  box-shadow: 0 4px 16px rgba(0,0,0,.08);
}

.flip-card-back {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  transform: rotateY(180deg);
  overflow-y: auto;
}

.card-word {
  font-size: 24px;
  font-weight: 700;
  color: var(--theme-text);
  margin-bottom: 6px;
}

.card-phonetic {
  font-size: 14px;
  color: var(--theme-text-secondary);
  font-family: 'Times New Roman', 'Arial Unicode MS', sans-serif;
  margin-bottom: 10px;
}

.card-speaker {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  margin-bottom: 8px;
}

.card-speaker:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.4);
}

.card-difficulty { margin-bottom: 4px; }
.star { color: var(--theme-border, #ddd); font-size: 13px; }
.star.active { color: #f0ad4e; }

.card-category-tag {
  font-size: 11px;
  padding: 2px 10px;
  border-radius: 10px;
  background: var(--theme-hover);
  color: var(--theme-text-secondary);
}

/* 背面 */
.card-pos {
  font-size: 13px;
  opacity: 0.85;
  margin-bottom: 8px;
  text-transform: capitalize;
}

.card-definition {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  line-height: 1.4;
}

.card-example {
  font-size: 12px;
  opacity: 0.9;
  line-height: 1.4;
}

.example-en { font-style: italic; }
.example-cn { margin-top: 4px; opacity: 0.75; }

.card-detail-btn {
  margin-top: auto;
  padding: 6px 14px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-size: 12px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  transition: background 0.2s;
}

.card-detail-btn:hover {
  background: rgba(255, 255, 255, 0.35);
}

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 36px;
}

@media (max-width: 768px) {
  .vocab-grid { grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 12px; }
  .flip-card { height: 220px; }
  .card-word { font-size: 20px; }
}
</style>
