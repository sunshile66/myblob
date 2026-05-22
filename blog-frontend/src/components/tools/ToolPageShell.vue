<template>
  <SimpleLayout>
    <div class="tool-shell">
      <div class="tool-shell__container">
        <section class="tool-shell__hero">
          <div class="tool-shell__copy">
            <el-button link class="tool-shell__back" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              {{ backLabel }}
            </el-button>
            <span class="tool-shell__eyebrow">{{ eyebrow }}</span>
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
        </section>

        <section class="tool-shell__body">
          <slot />
        </section>
      </div>
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
    backLabel: '返回工具箱',
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
  position: relative;
  overflow: hidden;
  padding: 8px 0 28px;
}

.tool-shell__container {
  position: relative;
  max-width: 1240px;
  margin: 0 auto;
}

.tool-shell__hero {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(260px, 0.8fr);
  gap: 20px;
  margin-bottom: 20px;
  padding: 20px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.tool-shell__copy {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tool-shell__back {
  width: fit-content;
  padding: 0;
  color: var(--theme-text-secondary);
}

.tool-shell__back:hover {
  color: var(--theme-primary);
}

.tool-shell__eyebrow {
  display: inline-flex;
  width: fit-content;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(20, 184, 166, 0.12);
  color: #0f766e;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
}

.tool-shell__hero h1 {
  margin: 0;
  font-size: 28px;
  line-height: 1.25;
  font-weight: 700;
  letter-spacing: 0;
  color: #0f172a;
}

.tool-shell__hero p {
  margin: 0;
  max-width: 640px;
  font-size: 14px;
  line-height: 1.7;
  color: #64748b;
}

.tool-shell__meta {
  display: grid;
  gap: 10px;
  align-content: start;
}

.tool-shell__meta-card {
  padding: 14px;
  border-radius: 8px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: #f8fafc;
}

.tool-shell__meta-card span {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  color: #64748b;
}

.tool-shell__meta-card strong {
  display: block;
  color: #0f172a;
  font-size: 18px;
  line-height: 1.4;
}

.tool-shell__body {
  position: relative;
}

@media (max-width: 960px) {
  .tool-shell__hero {
    grid-template-columns: 1fr;
    padding: 18px;
  }
}

@media (max-width: 768px) {
  .tool-shell {
    padding-top: 0;
  }

  .tool-shell__hero {
    gap: 18px;
    margin-bottom: 20px;
  }

  .tool-shell__hero p {
    font-size: 15px;
  }
}
</style>
