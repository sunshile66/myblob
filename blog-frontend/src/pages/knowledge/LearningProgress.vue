<template>
  <KnowledgeLayout title="学习进度" subtitle="追踪你的学习历程">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon size="24"><TrendCharts /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalLearned }}</div>
          <div class="stat-label">已学习</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon mastered">
          <el-icon size="24"><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.mastered }}</div>
          <div class="stat-label">已掌握</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon due">
          <el-icon size="24"><Bell /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.dueToday }}</div>
          <div class="stat-label">待复习</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon streak">
          <el-icon size="24"><Calendar /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ streakDays }}</div>
          <div class="stat-label">连续天数</div>
        </div>
      </div>
    </div>

    <!-- 学习热力图 -->
    <el-card class="heatmap-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>学习日历</span>
          <span class="card-subtitle">最近一年的学习记录</span>
        </div>
      </template>
      <div class="heatmap-container">
        <div class="heatmap-months">
          <span v-for="month in months" :key="month">{{ month }}</span>
        </div>
        <div class="heatmap-grid">
          <div
            v-for="(day, index) in heatmapDays"
            :key="index"
            class="heatmap-day"
            :class="getHeatmapClass(day.count)"
            :title="`${day.date}: ${day.count} 次学习`"
          />
        </div>
        <div class="heatmap-legend">
          <span>少</span>
          <div class="legend-item level-0" />
          <div class="legend-item level-1" />
          <div class="legend-item level-2" />
          <div class="legend-item level-3" />
          <div class="legend-item level-4" />
          <span>多</span>
        </div>
      </div>
    </el-card>

    <!-- 分类掌握度 -->
    <el-card class="mastery-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>分类掌握度</span>
          <span class="card-subtitle">各知识领域的掌握程度</span>
        </div>
      </template>
      <div class="mastery-list">
        <div v-for="item in categoryMastery" :key="item.name" class="mastery-item">
          <div class="mastery-header">
            <span class="mastery-name">{{ item.name }}</span>
            <span class="mastery-count">{{ item.count }} 已掌握</span>
          </div>
          <el-progress
            :percentage="getMasteryPercentage(item.count)"
            :stroke-width="8"
            :show-text="false"
          />
        </div>
      </div>
    </el-card>

    <!-- 待复习列表 -->
    <el-card class="review-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>待复习内容</span>
          <el-button type="primary" size="small" @click="startReview">
            开始复习
          </el-button>
        </div>
      </template>
      <el-empty v-if="dueReviews.length === 0" description="暂无待复习内容" />
      <div v-else class="review-list">
        <div v-for="review in dueReviews" :key="review.id" class="review-item">
          <div class="review-info">
            <el-tag :type="getStatusType(review.status)" size="small">
              {{ getStatusText(review.status) }}
            </el-tag>
            <span class="review-type">{{ review.itemType === 'knowledge' ? '知识' : '词汇' }} #{{ review.itemId }}</span>
          </div>
          <div class="review-meta">
            <span>间隔: {{ review.interval }} 天</span>
            <span>下次: {{ formatDate(review.nextReview) }}</span>
          </div>
        </div>
      </div>
    </el-card>
  </KnowledgeLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { TrendCharts, CircleCheck, Bell, Calendar } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import KnowledgeLayout from './components/KnowledgeLayout.vue'
import {
  getDueReviews,
  getReviewStats,
  type LearningProgress,
  type LearningStats
} from '@/api/knowledge'

const router = useRouter()

const stats = ref<LearningStats>({
  totalLearned: 0,
  mastered: 0,
  dueToday: 0,
  heatmap: [],
  categoryMastery: []
})

const dueReviews = ref<LearningProgress[]>([])
const loading = ref(false)

// 计算连续学习天数
const streakDays = computed(() => {
  if (!stats.value.heatmap || stats.value.heatmap.length === 0) return 0
  
  const dates = stats.value.heatmap
    .map(([date]) => date)
    .sort()
    .reverse()
  
  let streak = 0
  const today = new Date().toISOString().split('T')[0]
  
  for (let i = 0; i < dates.length; i++) {
    const expectedDate = new Date()
    expectedDate.setDate(expectedDate.getDate() - i)
    const expected = expectedDate.toISOString().split('T')[0]
    
    if (dates[i] === expected) {
      streak++
    } else {
      break
    }
  }
  
  return streak
})

// 生成热力图数据
const heatmapDays = computed(() => {
  const days = []
  const today = new Date()
  const heatmapMap = new Map(stats.value.heatmap || [])
  
  for (let i = 364; i >= 0; i--) {
    const date = new Date(today)
    date.setDate(date.getDate() - i)
    const dateStr = date.toISOString().split('T')[0]
    
    days.push({
      date: dateStr,
      count: heatmapMap.get(dateStr) || 0
    })
  }
  
  return days
})

// 月份标签
const months = computed(() => {
  const result = []
  const today = new Date()
  
  for (let i = 11; i >= 0; i--) {
    const date = new Date(today.getFullYear(), today.getMonth() - i, 1)
    result.push(date.toLocaleDateString('zh-CN', { month: 'short' }))
  }
  
  return result
})

// 分类掌握度数据
const categoryMastery = computed(() => {
  if (!stats.value.categoryMastery) return []
  
  const categoryNames: Record<string, string> = {
    programming: '编程技术',
    math: '数学科学',
    history: '历史人文',
    science: '自然科学',
    literature: '文学艺术',
    philosophy: '哲学思辨',
    business: '商业经济',
    ai: '人工智能',
    cybersecurity: '网络安全',
    algorithms: '算法与数据结构',
    'system-design': '系统架构',
    vocabulary: '英语词汇'
  }
  
  return stats.value.categoryMastery.map(([name, count]) => ({
    name: categoryNames[name] || name,
    count: Number(count)
  }))
})

// 获取热力图颜色类
const getHeatmapClass = (count: number) => {
  if (count === 0) return 'level-0'
  if (count <= 2) return 'level-1'
  if (count <= 5) return 'level-2'
  if (count <= 10) return 'level-3'
  return 'level-4'
}

// 获取掌握度百分比
const getMasteryPercentage = (count: number) => {
  // 假设每个分类有 20 个条目
  return Math.min(100, Math.round((count / 20) * 100))
}

// 获取状态类型
const getStatusType = (status: number) => {
  if (status === 0) return 'info'
  if (status === 1) return 'warning'
  return 'success'
}

// 获取状态文本
const getStatusText = (status: number) => {
  if (status === 0) return '新卡片'
  if (status === 1) return '学习中'
  return '已掌握'
}

// 格式化日期
const formatDate = (date: string | null) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric'
  })
}

// 开始复习
const startReview = () => {
  router.push('/knowledge/vocabulary/quiz')
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const [statsRes, reviewsRes] = await Promise.all([
      getReviewStats(),
      getDueReviews(5)
    ])
    
    if (statsRes.data) {
      stats.value = statsRes.data
    }
    
    if (reviewsRes.data) {
      dueReviews.value = reviewsRes.data
    }
  } catch (error) {
    console.error('Failed to load learning progress:', error)
    ElMessage.error('加载学习进度失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--el-bg-color);
  border-radius: var(--radius-lg);
  border: 1px solid var(--el-border-color-lighter);
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-icon.mastered {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-icon.due {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-icon.streak {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-subtitle {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.heatmap-card {
  margin-bottom: 24px;
}

.heatmap-container {
  overflow-x: auto;
}

.heatmap-months {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  padding: 0 4px;
}

.heatmap-months span {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.heatmap-grid {
  display: grid;
  grid-template-columns: repeat(53, 1fr);
  grid-template-rows: repeat(7, 1fr);
  gap: 3px;
}

.heatmap-day {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 2px;
  cursor: pointer;
}

.heatmap-day.level-0 {
  background: var(--el-fill-color-lighter);
}

.heatmap-day.level-1 {
  background: #9be9a8;
}

.heatmap-day.level-2 {
  background: #40c463;
}

.heatmap-day.level-3 {
  background: #30a14e;
}

.heatmap-day.level-4 {
  background: #216e39;
}

.heatmap-legend {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.legend-item {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.mastery-card {
  margin-bottom: 24px;
}

.mastery-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.mastery-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.mastery-name {
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.mastery-count {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--el-fill-color-lighter);
  border-radius: var(--radius-md);
}

.review-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.review-type {
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.review-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
