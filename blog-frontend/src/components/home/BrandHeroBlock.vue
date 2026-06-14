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
  gap: 32px;
  min-height: 160px;
  position: relative;
  overflow: hidden;
}

.brand-hero::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.08) 0%, transparent 70%);
  pointer-events: none;
}

.brand-hero__content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  position: relative;
  z-index: 1;
}

.brand-hero__eyebrow {
  display: inline-flex;
  width: fit-content;
  padding: 5px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(4px);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.brand-hero__title {
  margin: 0;
  font-size: clamp(24px, 3.5vw, 36px);
  font-weight: 800;
  line-height: 1.15;
  letter-spacing: -0.025em;
  text-wrap: balance;
}

.brand-hero__desc {
  margin: 0;
  max-width: 480px;
  font-size: 15px;
  line-height: 1.65;
  opacity: 0.9;
  text-wrap: pretty;
}

.brand-hero__actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 4px;
}

.brand-hero__btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 11px 24px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  font-family: inherit;
  letter-spacing: -0.01em;
}

.brand-hero__btn--primary {
  background: rgba(255, 255, 255, 0.95);
  color: #4F46E5;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.brand-hero__btn--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  background: #fff;
}

.brand-hero__btn--secondary {
  background: rgba(255, 255, 255, 0.12);
  color: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.brand-hero__btn--secondary:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

.brand-hero__btn-icon {
  font-size: 16px;
  transition: transform 0.2s ease;
}

.brand-hero__btn--primary:hover .brand-hero__btn-icon {
  transform: translateX(3px);
}

/* Visual decoration */
.brand-hero__visual {
  position: relative;
  width: 180px;
  height: 150px;
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
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(12px);
  font-size: 24px;
  animation: floatBubble 5s ease-in-out infinite;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.brand-hero__bubble--1 { top: 0; left: 15px; animation-delay: 0s; }
.brand-hero__bubble--2 { top: 15px; right: 0; animation-delay: 1s; }
.brand-hero__bubble--3 { bottom: 5px; left: 35px; animation-delay: 2s; }
.brand-hero__bubble--4 {
  top: 50px;
  left: 65px;
  animation-delay: 3s;
  width: 48px;
  height: 48px;
  font-size: 20px;
  border-radius: 14px;
}

@keyframes floatBubble {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  33% { transform: translateY(-6px) rotate(2deg); }
  66% { transform: translateY(3px) rotate(-1deg); }
}

@media (prefers-reduced-motion: reduce) {
  .brand-hero__bubble {
    animation: none;
  }
  .brand-hero__btn-icon {
    transition: none;
  }
}
</style>
