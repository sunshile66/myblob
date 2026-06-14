<template>
  <SimpleLayout no-footer>
    <div class="tool-shell">
      <header class="tool-shell__bar">
        <div class="tool-shell__title">
          <el-button link class="tool-shell__back" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            {{ backLabel }}
          </el-button>
          <span>{{ eyebrow }}</span>
          <h1>{{ title }}</h1>
          <p>{{ description }}</p>
        </div>

        <div v-if="meta.length" class="tool-shell__meta">
          <article
            v-for="item in meta"
            :key="`${item.label}-${item.value}`"
            class="tool-shell__meta-card"
          >
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </article>
        </div>
      </header>

      <main class="tool-shell__body">
        <slot />
      </main>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import SimpleLayout from '@/layout/SimpleLayout.vue'

interface ToolMetaItem {
  label: string
  value: string
}

withDefaults(
  defineProps<{
    title: string
    description: string
    eyebrow?: string
    backLabel?: string
    meta?: ToolMetaItem[]
  }>(),
  {
    eyebrow: '效率工具',
    backLabel: '工具中心',
    meta: () => [],
  },
)

const router = useRouter()

const goBack = () => {
  router.push('/tools')
}
</script>

<style scoped>
.tool-shell {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 10px;
  min-height: calc(100vh - 126px);
}

.tool-shell__bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 10px 12px;
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-lg);
  background: var(--theme-card);
  box-shadow: none;
  transform: translateZ(0);
  will-change: transform;
}

.tool-shell__title {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 10px;
}

.tool-shell__back {
  flex: 0 0 auto;
  padding: 0;
  color: var(--theme-text-secondary);
  font-weight: 700;
  transition: color var(--transition-fast);
}

.tool-shell__back:hover {
  color: var(--theme-primary);
}

.tool-shell__title > span {
  flex: 0 0 auto;
  padding: 4px 8px;
  border-radius: var(--radius-full);
  background: var(--theme-primary-light);
  color: var(--theme-primary);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.03em;
}

.tool-shell__title h1 {
  flex: 0 0 auto;
  margin: 0;
  color: var(--theme-text);
  font-size: 18px;
  line-height: 1.25;
  font-weight: 600;
}

.tool-shell__title p {
  overflow: hidden;
  margin: 0;
  color: var(--theme-text-secondary);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tool-shell__meta {
  display: flex;
  flex: 0 0 auto;
  gap: 8px;
}

.tool-shell__meta-card {
  min-width: 78px;
  padding: 6px 9px;
  border-radius: var(--radius-md);
  background: var(--theme-background);
  border: 1px solid var(--theme-border);
}

.tool-shell__meta-card span,
.tool-shell__meta-card strong {
  display: block;
  line-height: 1.2;
}

.tool-shell__meta-card span {
  color: var(--theme-text-tertiary);
  font-size: 11px;
}

.tool-shell__meta-card strong {
  margin-top: 3px;
  color: var(--theme-text);
  font-size: 13px;
}

.tool-shell__body {
  min-height: 0;
}

@media (max-width: 960px) {
  .tool-shell__bar {
    align-items: center;
    flex-direction: row;
    padding: 8px 10px;
  }

  .tool-shell__title {
    flex: 1;
  }

  .tool-shell__title p {
    display: none;
  }

  .tool-shell__meta {
    max-width: 42%;
    overflow-x: auto;
  }
}

@media (max-width: 640px) {
  .tool-shell {
    min-height: calc(100vh - 112px);
  }

  .tool-shell__title > span,
  .tool-shell__meta-card span {
    display: none;
  }

  .tool-shell__title h1 {
    font-size: 16px;
  }

  .tool-shell__meta-card {
    min-width: 54px;
    padding: 5px 7px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .tool-shell__back {
    transition: none;
  }
}
</style>
