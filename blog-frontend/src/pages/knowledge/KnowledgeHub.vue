<template>
  <KnowledgeLayout>
  <div class="knowledge-hub">
    <div class="hub-header">
      <h1>知识百科</h1>
      <p>探索知识海洋，从英语学习到编程、数学、历史、科学</p>
    </div>

    <!-- 英语学习区块 -->
    <section class="section">
      <h2 class="section-title">
        <el-icon><Reading /></el-icon> 英语学习
      </h2>
      <div class="card-grid">
        <router-link to="/knowledge/vocabulary" class="hub-card card-vocab">
          <div class="card-icon">
            <el-icon :size="32"><Notebook /></el-icon>
          </div>
          <div class="card-body">
            <h3>英语词汇</h3>
            <p>带音标和发音的词汇学习卡片，覆盖CET4/6、雅思、托福、GRE</p>
          </div>
        </router-link>
        <router-link to="/knowledge/vocabulary/quiz" class="hub-card card-quiz">
          <div class="card-icon">
            <el-icon :size="32"><EditPen /></el-icon>
          </div>
          <div class="card-body">
            <h3>词汇测验</h3>
            <p>随机出题，选择题模式，检验单词掌握程度</p>
          </div>
        </router-link>
        <router-link to="/knowledge/articles" class="hub-card card-articles">
          <div class="card-icon">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="card-body">
            <h3>英语外刊</h3>
            <p>BBC、VOA等外刊文章，提升英语阅读能力</p>
          </div>
        </router-link>
      </div>
    </section>

    <!-- 知识分类区块 -->
    <section class="section">
      <h2 class="section-title">
        <el-icon><Collection /></el-icon> 知识分类
      </h2>
      <div class="card-grid" v-loading="categoriesLoading">
        <router-link
          v-for="cat in categories"
          :key="cat.key"
          :to="`/knowledge/${cat.key}`"
          class="hub-card"
          :class="`card-${cat.key}`"
        >
          <div class="card-icon">
            <el-icon :size="32"><component :is="cat.icon" /></el-icon>
          </div>
          <div class="card-body">
            <h3>{{ cat.name }}</h3>
            <p>{{ catDesc(cat.key) }}</p>
          </div>
        </router-link>
      </div>
    </section>
  </div>
  </KnowledgeLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Reading, Notebook, EditPen, Document, Collection, Cpu, Lock, DataLine, SetUp } from '@element-plus/icons-vue'
import { getKnowledgeCategories, type KnowledgeCategory } from '@/api/knowledge'
import KnowledgeLayout from './components/KnowledgeLayout.vue'

const categories = ref<KnowledgeCategory[]>([])
const categoriesLoading = ref(false)

const catDesc = (key: string) => {
  const map: Record<string, string> = {
    programming: '算法、设计模式、Git、系统架构等编程知识',
    math: '数学公式、线性代数、概率论、机器学习基础',
    history: '中国历史朝代、世界文明、工业革命',
    science: '物理相对论、DNA结构、化学元素周期表',
    literature: '文学经典、名著导读、创作技巧',
    philosophy: '哲学流派、思维方法、逻辑推理',
    business: '经济学原理、商业模式、管理知识',
    ai: 'Transformer、LLM、CNN、GAN、强化学习等AI核心知识',
    cybersecurity: 'Web安全、密码学、渗透测试、零信任架构',
    algorithms: '动态规划、图论、高级数据结构、字符串算法',
    'system-design': '分布式系统、微服务、高并发、云原生架构'
  }
  return map[key] || '探索更多知识'
}

onMounted(async () => {
  categoriesLoading.value = true
  try {
    const res = await getKnowledgeCategories()
    categories.value = (res as any).data || res
  } finally {
    categoriesLoading.value = false
  }
})
</script>

<style scoped>
.knowledge-hub {
  padding: 0;
}

.hub-header {
  text-align: center;
  margin-bottom: 48px;
}

.hub-header h1 {
  font-size: 36px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0 0 12px;
}

.hub-header p {
  font-size: 16px;
  color: var(--theme-text-secondary);
}

.section {
  margin-bottom: 48px;
}

.section-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--theme-text);
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--theme-border);
  position: relative;
}
.section-title::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 60px;
  height: 2px;
  background: var(--gradient-primary);
  border-radius: 1px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.hub-card {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 28px;
  border-radius: 16px;
  text-decoration: none;
  transition: transform 0.2s, box-shadow 0.2s;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  cursor: pointer;
}

.hub-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  border-color: var(--theme-primary);
}

.card-icon {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  color: #fff;
}

.card-vocab .card-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
.card-quiz .card-icon { background: linear-gradient(135deg, #f093fb, #f5576c); }
.card-articles .card-icon { background: linear-gradient(135deg, #4facfe, #00f2fe); }

.card-programming .card-icon { background: linear-gradient(135deg, #a18cd1, #fbc2eb); }
.card-math .card-icon { background: linear-gradient(135deg, #ffecd2, #fcb69f); color: #333; }
.card-history .card-icon { background: linear-gradient(135deg, #89f7fe, #66a6ff); }
.card-science .card-icon { background: linear-gradient(135deg, #fddb92, #d1fdff); color: #333; }
.card-literature .card-icon { background: linear-gradient(135deg, #a1c4fd, #c2e9fb); }
.card-philosophy .card-icon { background: linear-gradient(135deg, #d4fc79, #96e6a1); color: #333; }
.card-business .card-icon { background: linear-gradient(135deg, #fccb90, #d57eeb); }
.card-ai .card-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
.card-cybersecurity .card-icon { background: linear-gradient(135deg, #f093fb, #f5576c); }
.card-algorithms .card-icon { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.card-system-design .card-icon { background: linear-gradient(135deg, #43e97b, #38f9d7); color: #333; }

.card-body h3 {
  font-size: 17px;
  font-weight: 600;
  color: var(--theme-text);
  margin: 0 0 6px;
}

.card-body p {
  font-size: 14px;
  color: var(--theme-text-secondary);
  margin: 0;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .card-grid {
    grid-template-columns: 1fr;
  }
  .hub-header h1 { font-size: 28px; }
}
</style>
