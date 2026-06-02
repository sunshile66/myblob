<template>
  <BentoCard variant="gradient" :col-span="2" :row-span="1">
    <div class="brand-hero">
      <div class="brand-hero__content">
        <span class="brand-hero__eyebrow">{{ eyebrow }}</span>
        <h1 class="brand-hero__title">{{ title }}</h1>
        <p class="brand-hero__desc">{{ description }}</p>
        <div class="brand-hero__actions">
          <button class="brand-hero__btn brand-hero__btn--primary" @click="$emit('primary')">
            {{ primaryLabel }}
            <el-icon class="brand-hero__btn-icon"><ArrowRight /></el-icon>
          </button>
          <button class="brand-hero__btn brand-hero__btn--secondary" @click="$emit('secondary')">
            {{ secondaryLabel }}
          </button>
        </div>
      </div>
      <div class="brand-hero__visual" aria-hidden="true">
        <div class="brand-hero__bubble brand-hero__bubble--1">✍️</div>
        <div class="brand-hero__bubble brand-hero__bubble--2">🔧</div>
        <div class="brand-hero__bubble brand-hero__bubble--3">📊</div>
        <div class="brand-hero__bubble brand-hero__bubble--4">⚡</div>
      </div>
    </div>
  </BentoCard>
</template>

<script setup lang="ts">
import { ArrowRight } from '@element-plus/icons-vue'
import BentoCard from '@/components/common/BentoCard.vue'

defineProps<{
  eyebrow?: string
  title: string
  description?: string
  primaryLabel: string
  secondaryLabel?: string
}>()

defineEmits<{
  primary: []
  secondary: []
}>()
</script>

<style scoped>
.brand-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  min-height: 140px;
}

.brand-hero__content {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.brand-hero__eyebrow {
  display: inline-flex;
  width: fit-content;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.2);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.brand-hero__title {
  margin: 0;
  font-size: clamp(22px, 3vw, 32px);
  font-weight: 800;
  line-height: 1.2;
  letter-spacing: -0.03em;
}

.brand-hero__desc {
  margin: 0;
  max-width: 520px;
  font-size: 14px;
  line-height: 1.6;
  opacity: 0.85;
}

.brand-hero__actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.brand-hero__btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 22px;
  border: none;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
}

.brand-hero__btn--primary {
  background: #fff;
  color: var(--theme-primary);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12);
}

.brand-hero__btn--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.18);
}

.brand-hero__btn--secondary {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  backdrop-filter: blur(8px);
}

.brand-hero__btn--secondary:hover {
  background: rgba(255, 255, 255, 0.3);
}

.brand-hero__btn-icon {
  font-size: 16px;
}

/* Visual decoration */
.brand-hero__visual {
  position: relative;
  width: 160px;
  height: 130px;
  flex-shrink: 0;
  display: none;
}

@media (min-width: 768px) {
  .brand-hero__visual {
    display: block;
  }
}

.brand-hero__bubble {
  position: absolute;
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(8px);
  font-size: 22px;
  animation: floatBubble 4s ease-in-out infinite;
}

.brand-hero__bubble--1 { top: 0; left: 10px; animation-delay: 0s; }
.brand-hero__bubble--2 { top: 10px; right: 0; animation-delay: 0.8s; }
.brand-hero__bubble--3 { bottom: 0; left: 30px; animation-delay: 1.6s; }
.brand-hero__bubble--4 { top: 40px; left: 55px; animation-delay: 2.4s;
  width: 44px; height: 44px; font-size: 18px; border-radius: 12px; }

@keyframes floatBubble {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-8px) rotate(3deg); }
}

@media (prefers-reduced-motion: reduce) {
  .brand-hero__bubble {
    animation: none;
  }
}
</style>
