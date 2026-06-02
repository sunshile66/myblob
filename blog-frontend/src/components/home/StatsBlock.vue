<template>
  <BentoCard variant="solid" :col-span="1" :row-span="1" interactive @click="$emit('click')">
    <div class="stats-block">
      <div class="stats-block__icon" :style="{ background: accent }">
        <el-icon :size="18"><component :is="icon" /></el-icon>
      </div>
      <div class="stats-block__body">
        <span class="stats-block__value">{{ animatedValue }}</span>
        <span class="stats-block__label">{{ label }}</span>
      </div>
    </div>
  </BentoCard>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { TrendCharts } from '@element-plus/icons-vue'
import type { Component } from 'vue'
import BentoCard from '@/components/common/BentoCard.vue'

const props = withDefaults(defineProps<{
  label: string
  value: number
  accent?: string
  icon?: Component
}>(), {
  accent: 'var(--gradient-primary)',
  icon: () => TrendCharts,
})

defineEmits<{
  click: []
}>()

const animatedValue = ref(0)
let animFrame: number

const animateCount = (target: number) => {
  const start = animatedValue.value
  const duration = 800
  const startTime = performance.now()

  const step = (now: number) => {
    const elapsed = now - startTime
    const progress = Math.min(elapsed / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    animatedValue.value = Math.round(start + (target - start) * eased)
    if (progress < 1) {
      animFrame = requestAnimationFrame(step)
    }
  }

  cancelAnimationFrame(animFrame)
  animFrame = requestAnimationFrame(step)
}

onMounted(() => {
  animateCount(props.value)
})

watch(() => props.value, (newVal) => {
  animateCount(newVal)
})
</script>

<style scoped>
.stats-block {
  display: flex;
  align-items: center;
  gap: 14px;
  height: 100%;
  min-height: 60px;
}

.stats-block__icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stats-block__body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stats-block__value {
  font-size: 26px;
  font-weight: 800;
  line-height: 1;
  color: var(--theme-text);
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
}

.stats-block__label {
  font-size: 13px;
  font-weight: 500;
  color: var(--theme-text-secondary);
}
</style>
