<template>
  <SimpleLayout>
    <div class="tool-shell">
      <div class="tool-shell__glow tool-shell__glow--left" />
      <div class="tool-shell__glow tool-shell__glow--right" />

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
  padding: 8px 0 32px;
}

.tool-shell__glow {
  position: absolute;
  width: 320px;
  height: 320px;
  border-radius: 50%;
  filter: blur(70px);
  opacity: 0.28;
  pointer-events: none;
}

.tool-shell__glow--left {
  top: 20px;
  left: -100px;
  background: rgba(34, 197, 94, 0.24);
}

.tool-shell__glow--right {
  top: 60px;
  right: -120px;
  background: rgba(59, 130, 246, 0.22);
}

.tool-shell__container {
  position: relative;
  max-width: 1240px;
  margin: 0 auto;
}

.tool-shell__hero {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(260px, 0.8fr);
  gap: 24px;
  margin-bottom: 28px;
  padding: 28px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 28px;
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.96), rgba(255, 255, 255, 0.74)),
    linear-gradient(135deg, rgba(15, 23, 42, 0.04), rgba(59, 130, 246, 0.08));
  box-shadow: 0 20px 45px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
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
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  color: var(--theme-primary);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.tool-shell__hero h1 {
  margin: 0;
  font-size: clamp(28px, 4vw, 44px);
  line-height: 1.08;
  color: #0f172a;
}

.tool-shell__hero p {
  margin: 0;
  max-width: 680px;
  font-size: 16px;
  line-height: 1.75;
  color: #475569;
}

.tool-shell__meta {
  display: grid;
  gap: 14px;
  align-content: start;
}

.tool-shell__meta-card {
  padding: 18px;
  border-radius: 22px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.78);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.5);
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
    padding: 22px;
    border-radius: 24px;
  }
}

@media (max-width: 768px) {
  .tool-shell {
    padding-top: 0;
  }

  .tool-shell__hero {
    gap: 18px;
    padding: 18px;
    margin-bottom: 20px;
  }

  .tool-shell__hero p {
    font-size: 15px;
  }
}
</style>
