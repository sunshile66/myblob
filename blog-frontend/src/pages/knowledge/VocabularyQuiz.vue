<template>
  <KnowledgeLayout>
  <div class="quiz-page">
    <div class="page-header">
      <h1>词汇测验</h1>
      <p v-if="!quizStarted">随机抽取单词，检验你的词汇量</p>
    </div>

    <!-- 开始界面 -->
    <div class="quiz-start" v-if="!quizStarted">
      <div class="start-card">
        <el-icon :size="48" class="start-icon"><EditPen /></el-icon>
        <div class="start-options">
          <div class="option-row">
            <span>题目数量</span>
            <el-input-number v-model="quizCount" :min="5" :max="20" :step="5" />
          </div>
        </div>
        <el-button type="primary" size="large" @click="startQuiz" :loading="loading">
          开始测验
        </el-button>
      </div>
    </div>

    <!-- 测验界面 -->
    <div class="quiz-body" v-else-if="!quizFinished">
      <div class="quiz-progress">
        <span>第 {{ currentIndex + 1 }} / {{ quizData.length }} 题</span>
        <el-progress :percentage="Math.round(((currentIndex) / quizData.length) * 100)" :show-text="false" />
      </div>

      <div class="quiz-card" v-if="currentWord">
        <div class="question-type" v-if="questionType === 'en2cn'">
          <h2>请选择「{{ currentWord.word }}」的中文意思</h2>
          <div class="phonetic-hint">{{ currentWord.phonetic }}</div>
        </div>
        <div class="question-type" v-else>
          <h2>请选择「{{ currentWord.definition }}」的英文</h2>
        </div>

        <div class="options-grid">
          <button
            v-for="(opt, idx) in currentOptions"
            :key="idx"
            class="option-btn"
            :class="{
              correct: answered && opt.correct,
              wrong: answered && opt.selected && !opt.correct,
              selected: opt.selected && !answered
            }"
            :disabled="answered"
            @click="selectOption(opt)"
          >
            <span class="option-label">{{ optionLabels[idx] }}</span>
            <span class="option-text">{{ opt.text }}</span>
          </button>
        </div>

        <div class="answer-feedback" v-if="answered">
          <p v-if="isCorrect" class="feedback-correct">正确！</p>
          <p v-else class="feedback-wrong">
            错误！正确答案是「{{ correctAnswer }}」
            <span v-if="currentWord.exampleSentence"> — {{ currentWord.exampleSentence }}</span>
          </p>
          <el-button type="primary" size="small" @click="nextQuestion" style="margin-top:12px">
            {{ currentIndex + 1 < quizData.length ? '下一题' : '查看结果' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 结果界面 -->
    <div class="quiz-result" v-else>
      <div class="result-card">
        <div class="result-icon">{{ scorePercent >= 60 ? '🎉' : '💪' }}</div>
        <h2>测验完成！</h2>
        <div class="result-score">{{ correctCount }} / {{ quizData.length }}</div>
        <div class="result-percent">正确率 {{ scorePercent }}%</div>
        <el-progress :percentage="scorePercent" :color="scorePercent >= 60 ? '#67c23a' : '#e6a23c'" />

        <div class="wrong-list" v-if="wrongWords.length > 0">
          <h3>错题回顾</h3>
          <div v-for="w in wrongWords" :key="w.word" class="wrong-item">
            <div class="wrong-word">{{ w.word }} <span class="wrong-phonetic">{{ w.phonetic }}</span></div>
            <div class="wrong-def">{{ w.definition }}</div>
          </div>
        </div>

        <div class="result-actions">
          <el-button @click="resetQuiz">再来一次</el-button>
          <el-button type="primary" @click="$router.push('/knowledge/vocabulary')">返回词汇表</el-button>
        </div>
      </div>
    </div>
  </div>
  </KnowledgeLayout>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { EditPen } from '@element-plus/icons-vue'
import { getRandomVocabulary, gradeReview, type VocabularyItem } from '@/api/knowledge'
import KnowledgeLayout from './components/KnowledgeLayout.vue'

const quizStarted = ref(false)
const quizFinished = ref(false)
const quizCount = ref(10)
const loading = ref(false)
const quizData = ref<VocabularyItem[]>([])
const currentIndex = ref(0)
const questionType = ref<'en2cn' | 'cn2en'>('en2cn')
const answered = ref(false)
const isCorrect = ref(false)
const selectedOptionIdx = ref(-1)
const correctCount = ref(0)
const wrongWords = ref<VocabularyItem[]>([])

// 从所有词汇中抽取干扰项
let allWordsPool: VocabularyItem[] = []

type Option = { text: string; correct: boolean; selected: boolean }

const optionLabels = ['A', 'B', 'C', 'D']

const currentWord = computed(() => quizData.value[currentIndex.value] || null)

const currentOptions = computed(() => {
  if (!currentWord.value) return []
  const isEn2Cn = questionType.value === 'en2cn'
  const correctText = isEn2Cn ? currentWord.value.definition : currentWord.value.word
  const pool = allWordsPool.filter(w => w.id !== currentWord.value!.id)
  const distractors = shuffleArray([...pool]).slice(0, 3)
  const distractorTexts = distractors.map(d => isEn2Cn ? d.definition : d.word)
  const allTexts = shuffleArray([correctText, ...distractorTexts])
  return allTexts.map(text => ({
    text,
    correct: text === correctText,
    selected: false
  }))
})

const correctAnswer = computed(() => {
  if (!currentWord.value) return ''
  return questionType.value === 'en2cn' ? currentWord.value.definition : currentWord.value.word
})

const scorePercent = computed(() =>
  quizData.value.length > 0 ? Math.round((correctCount.value / quizData.value.length) * 100) : 0
)

const startQuiz = async () => {
  loading.value = true
  try {
    const res = await getRandomVocabulary(quizCount.value)
    quizData.value = (res as any).data || res
    // Also fetch a larger pool for distractors
    const poolRes = await getRandomVocabulary(50)
    allWordsPool = (poolRes as any).data || poolRes || []
    quizStarted.value = true
    answered.value = false
    questionType.value = Math.random() > 0.3 ? 'en2cn' : 'cn2en'
  } finally {
    loading.value = false
  }
}

const selectOption = (opt: Option) => {
  if (answered.value) return
  answered.value = true
  isCorrect.value = opt.correct
  if (opt.correct) {
    correctCount.value++
  } else {
    wrongWords.value.push(currentWord.value!)
  }
  // 提交评分到间隔重复系统（静默，不阻塞UI）
  const quality = opt.correct ? 4 : 1
  gradeReview(currentWord.value!.id, quality).catch(() => {})
}

const nextQuestion = () => {
  if (currentIndex.value + 1 >= quizData.value.length) {
    quizFinished.value = true
    return
  }
  currentIndex.value++
  answered.value = false
  isCorrect.value = false
  questionType.value = Math.random() > 0.3 ? 'en2cn' : 'cn2en'
}

const resetQuiz = () => {
  quizStarted.value = false
  quizFinished.value = false
  currentIndex.value = 0
  correctCount.value = 0
  wrongWords.value = []
  answered.value = false
  quizData.value = []
}

const shuffleArray = <T>(arr: T[]): T[] => {
  const a = [...arr]
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]]
  }
  return a
}
</script>

<style scoped>
.quiz-page {
  max-width: 700px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}
.page-header h1 { font-size: 30px; font-weight: 700; color: var(--theme-text); margin: 0 0 8px; }
.page-header p { font-size: 14px; color: var(--theme-text-secondary); margin: 0; }

/* 开始界面 */
.quiz-start, .quiz-result { display: flex; justify-content: center; }
.start-card, .result-card {
  background: var(--theme-card);
  border-radius: var(--radius-xl);
  padding: 48px;
  text-align: center;
  border: 1px solid var(--theme-border);
  max-width: 440px;
  width: 100%;
}
.start-icon { color: var(--theme-primary); margin-bottom: 16px; }
.start-options { margin: 24px 0; }
.option-row { display: flex; align-items: center; justify-content: space-between; gap: 16px; font-size: 15px; color: var(--theme-text-secondary); }

/* 测验卡片 */
.quiz-progress { margin-bottom: 24px; display: flex; align-items: center; gap: 12px; font-size: 14px; color: var(--theme-text-secondary); }
.quiz-progress .el-progress { flex: 1; }

.quiz-card {
  background: var(--theme-card);
  border-radius: var(--radius-xl);
  padding: 36px;
  border: 1px solid var(--theme-border);
}

.question-type h2 { font-size: 20px; font-weight: 600; color: var(--theme-text); margin: 0 0 8px; }
.phonetic-hint { font-size: 14px; color: var(--theme-text-secondary); margin-bottom: 20px; font-family: 'Times New Roman', serif; }

.options-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin: 24px 0;
}

.option-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-radius: var(--radius-lg);
  border: 2px solid var(--theme-border);
  background: var(--theme-card);
  color: var(--theme-text);
  font-size: 15px;
  cursor: pointer;
  transition: all var(--transition-fast);
  text-align: left;
}

.option-btn:hover:not(:disabled) {
  border-color: var(--theme-primary);
  background: rgba(var(--theme-primary-rgb), 0.05);
}

.option-btn.selected { border-color: var(--theme-primary); background: rgba(var(--theme-primary-rgb), 0.1); }
.option-btn.correct { border-color: #67c23a; background: rgba(103, 194, 58, 0.1); color: #67c23a; }
.option-btn.wrong { border-color: #f56c6c; background: rgba(245, 108, 108, 0.1); color: #f56c6c; }

.option-label {
  width: 28px; height: 28px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  border-radius: 50%; background: var(--theme-hover);
  font-weight: 600; font-size: 13px;
}

.answer-feedback { margin-top: 16px; text-align: center; }
.feedback-correct { color: #67c23a; font-weight: 600; font-size: 16px; }
.feedback-wrong { color: #f56c6c; font-weight: 500; }

/* 结果 */
.result-icon { font-size: 56px; margin-bottom: 12px; }
.result-score { font-size: 42px; font-weight: 700; color: var(--theme-text); margin: 8px 0; }
.result-percent { font-size: 16px; color: var(--theme-text-secondary); margin-bottom: 16px; }
.wrong-list { margin-top: 28px; text-align: left; }
.wrong-list h3 { font-size: 16px; font-weight: 600; margin-bottom: 12px; color: var(--theme-text); }
.wrong-item { padding: 10px 0; border-bottom: 1px solid var(--theme-border); }
.wrong-word { font-weight: 600; color: var(--theme-text); }
.wrong-phonetic { font-weight: 400; color: var(--theme-text-secondary); font-size: 13px; margin-left: 8px; }
.wrong-def { font-size: 13px; color: var(--theme-text-secondary); margin-top: 2px; }
.result-actions { margin-top: 28px; display: flex; gap: 12px; justify-content: center; }

@media (max-width: 768px) {
  .options-grid { grid-template-columns: 1fr; }
  .start-card, .result-card { padding: 32px 24px; }
}

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
