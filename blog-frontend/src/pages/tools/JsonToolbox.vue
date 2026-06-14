<template>
  <div class="json-toolbox">
    <div class="tb-header">
      <router-link to="/tools" class="tb-back">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
        工具中心
      </router-link>
      <div class="tb-tabs">
        <button v-for="t in tabs" :key="t.key" :class="['tb-tab', { active: active === t.key }]" @click="active = t.key">
          {{ t.label }}
        </button>
      </div>
      <span class="tb-meta">JSON 工具箱</span>
    </div>
    <div class="tb-body">
      <keep-alive>
        <JsonFormatter v-if="active === 'format'" />
        <JsonDiff v-else-if="active === 'diff'" />
        <JsonToSql v-else-if="active === 'sql'" />
      </keep-alive>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import JsonFormatter from './JsonFormatter.vue'
import JsonDiff from './JsonDiff.vue'
import JsonToSql from './JsonToSql.vue'

const active = ref('format')
const tabs = [
  { key: 'format', label: '格式化' },
  { key: 'diff', label: 'JSON 对比' },
  { key: 'sql', label: 'JSON 转 SQL' },
]
</script>

<style scoped>
.json-toolbox { height: calc(100vh - 64px); display: flex; flex-direction: column; }
.tb-header { display: flex; align-items: center; gap: 16px; padding: 10px 16px; border-bottom: 1px solid var(--theme-border); background: var(--theme-card); flex-shrink: 0; }
.tb-back { display: flex; align-items: center; gap: 4px; color: var(--theme-text-secondary); text-decoration: none; font-size: 13px; font-weight: 500; }
.tb-back:hover { color: var(--theme-primary); }
.tb-tabs { display: flex; gap: 4px; }
.tb-tab { padding: 6px 16px; border: none; border-radius: var(--radius-md); background: transparent; color: var(--theme-text-secondary); font-size: 13px; font-weight: 500; cursor: pointer; transition: all var(--transition-fast); }
.tb-tab:hover { color: var(--theme-text); background: var(--theme-hover); }
.tb-tab.active { color: var(--theme-primary); background: var(--theme-primary-light); font-weight: 600; }
.tb-meta { margin-left: auto; font-size: 11px; color: var(--theme-text-tertiary); text-transform: uppercase; letter-spacing: .06em; font-weight: 600; }
.tb-body { flex: 1; min-height: 0; overflow: hidden; }

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
