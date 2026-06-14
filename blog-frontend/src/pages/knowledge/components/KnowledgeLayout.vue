<template>
  <SimpleLayout>
    <div class="knowledge-layout">
      <!-- 面包屑 -->
      <nav class="breadcrumb-bar" aria-label="面包屑">
        <router-link to="/" class="crumb-link">首页</router-link>
        <template v-for="(item, i) in crumbs" :key="i">
          <span class="crumb-sep">/</span>
          <router-link v-if="item.to" :to="item.to" class="crumb-link">{{ item.label }}</router-link>
          <span v-else class="crumb-current">{{ item.label }}</span>
        </template>
      </nav>

      <!-- 子导航条 -->
      <div class="sub-nav">
        <router-link
          v-for="nav in navItems"
          :key="nav.to"
          :to="nav.to"
          class="nav-pill"
          :class="{ active: isActive(nav) }"
        >
          <span v-html="nav.icon"></span>
          {{ nav.label }}
        </router-link>
      </div>

      <!-- 页面内容 -->
      <div class="page-content">
        <slot />
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import SimpleLayout from '@/layout/SimpleLayout.vue'

const route = useRoute()

interface Crumb { label: string; to?: string }
interface NavItem { label: string; to: string; icon: string; matchPrefix?: string }

const navItems: NavItem[] = [
  { label: '知识首页', to: '/knowledge', icon: '<svg viewBox="0 0 20 20" fill="currentColor" width="15" height="15"><path d="M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z"/></svg>' },
  { label: '英语词汇', to: '/knowledge/vocabulary', icon: '<svg viewBox="0 0 20 20" fill="currentColor" width="15" height="15"><path d="M9 4.804A7.968 7.968 0 005.5 4c-1.255 0-2.443.29-3.5.804v10A7.969 7.969 0 015.5 14c1.669 0 3.218.51 4.5 1.385A7.962 7.962 0 0114.5 14c1.255 0 2.443.29 3.5.804v-10A7.968 7.968 0 0014.5 4c-1.255 0-2.443.29-3.5.804V12a1 1 0 11-2 0V4.804z"/></svg>', matchPrefix: '/knowledge/vocabulary' },
  { label: '词汇测验', to: '/knowledge/vocabulary/quiz', icon: '<svg viewBox="0 0 20 20" fill="currentColor" width="15" height="15"><path d="M9 2a1 1 0 000 2h2a1 1 0 100-2H9z"/><path fill-rule="evenodd" d="M4 5a2 2 0 012-2 3 3 0 003 3h2a3 3 0 003-3 2 2 0 012 2v11a2 2 0 01-2 2H6a2 2 0 01-2-2V5zm3 4a1 1 0 000 2h.01a1 1 0 100-2H7zm3 0a1 1 0 000 2h3a1 1 0 100-2h-3zm-3 4a1 1 0 100 2h.01a1 1 0 100-2H7zm3 0a1 1 0 100 2h3a1 1 0 100-2h-3z" clip-rule="evenodd"/></svg>' },
  { label: '英语外刊', to: '/knowledge/articles', icon: '<svg viewBox="0 0 20 20" fill="currentColor" width="15" height="15"><path fill-rule="evenodd" d="M2 5a2 2 0 012-2h8a2 2 0 012 2v10a2 2 0 002 2H4a2 2 0 01-2-2V5zm3 1h6v4H5V6zm6 6H5v2h6v-2z" clip-rule="evenodd"/><path d="M15 7h1a2 2 0 012 2v5.5a1.5 1.5 0 01-3 0V7z"/></svg>' },
  { label: '学习进度', to: '/knowledge/progress', icon: '<svg viewBox="0 0 20 20" fill="currentColor" width="15" height="15"><path d="M2 11a1 1 0 011-1h2a1 1 0 011 1v5a1 1 0 01-1 1H3a1 1 0 01-1-1v-5zm6-4a1 1 0 011-1h2a1 1 0 011 1v9a1 1 0 01-1 1H9a1 1 0 01-1-1V7zm6-3a1 1 0 011-1h2a1 1 0 011 1v12a1 1 0 01-1 1h-2a1 1 0 01-1-1V4z"/></svg>' },
]

// 分类名称映射
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
}

const crumbs = computed<Crumb[]>(() => {
  const path = route.path
  const name = route.name as string

  // /knowledge
  if (path === '/knowledge' || name === 'KnowledgeHub') {
    return [{ label: '知识百科' }]
  }

  // /knowledge/vocabulary/quiz
  if (name === 'VocabularyQuiz') {
    return [
      { label: '知识百科', to: '/knowledge' },
      { label: '英语词汇', to: '/knowledge/vocabulary' },
      { label: '词汇测验' },
    ]
  }

  // /knowledge/vocabulary/:id
  if (name === 'VocabularyDetail') {
    return [
      { label: '知识百科', to: '/knowledge' },
      { label: '英语词汇', to: '/knowledge/vocabulary' },
      { label: '单词详情' },
    ]
  }

  // /knowledge/vocabulary
  if (name === 'VocabularyList') {
    return [
      { label: '知识百科', to: '/knowledge' },
      { label: '英语词汇' },
    ]
  }

  // /knowledge/articles
  if (name === 'EnglishArticles') {
    return [
      { label: '知识百科', to: '/knowledge' },
      { label: '英语外刊' },
    ]
  }

  // /knowledge/progress
  if (name === 'LearningProgress') {
    return [
      { label: '知识百科', to: '/knowledge' },
      { label: '学习进度' },
    ]
  }

  // /knowledge/:category
  if (name === 'KnowledgeCards') {
    const cat = route.params.category as string
    return [
      { label: '知识百科', to: '/knowledge' },
      { label: categoryNames[cat] || cat },
    ]
  }

  return [{ label: '知识百科', to: '/knowledge' }]
})

const isActive = (nav: NavItem) => {
  const path = route.path
  if (nav.to === '/knowledge') return path === '/knowledge'
  if (nav.matchPrefix) return path.startsWith(nav.matchPrefix) && path !== '/knowledge/vocabulary/quiz'
  return path === nav.to
}
</script>

<style scoped>
.knowledge-layout {
  max-width: 1200px;
  margin: 0 auto;
}

/* 面包屑 */
.breadcrumb-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 0 16px;
  font-size: 13px;
  flex-wrap: wrap;
}

.crumb-link {
  color: var(--theme-text-secondary);
  text-decoration: none;
  transition: color .15s;
  padding: 2px 4px;
  border-radius: 4px;
}
.crumb-link:hover {
  color: var(--theme-primary);
  background: var(--theme-primary-light);
}

.crumb-sep {
  color: var(--theme-text-tertiary, #94a3b8);
  font-size: 12px;
  user-select: none;
}

.crumb-current {
  color: var(--theme-text);
  font-weight: 600;
  padding: 2px 4px;
}

/* 子导航 */
.sub-nav {
  display: flex;
  gap: 6px;
  padding: 4px;
  margin-bottom: 24px;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-lg);
  overflow-x: auto;
}

.nav-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 9px;
  font-size: 13px;
  font-weight: 600;
  color: var(--theme-text-secondary);
  text-decoration: none;
  white-space: nowrap;
  transition: all .2s;
  flex-shrink: 0;
}
.nav-pill:hover {
  color: var(--theme-text);
  background: var(--theme-hover);
}
.nav-pill.active {
  color: #fff;
  background: var(--gradient-primary, var(--theme-primary));
  box-shadow: 0 2px 8px rgba(0,0,0,.12);
}
.nav-pill.active svg { color: #fff; }

.page-content {
  min-height: 400px;
}

@media (max-width: 640px) {
  .sub-nav { gap: 4px; padding: 3px; }
  .nav-pill { padding: 7px 12px; font-size: 12px; }
}
</style>
