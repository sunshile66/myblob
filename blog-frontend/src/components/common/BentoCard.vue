<template>
  <div
    class="bento-card"
    :class="[
      `bento-card--${variant}`,
      `bento-card--col-${colSpan}`,
      `bento-card--row-${rowSpan}`,
      { 'bento-card--interactive': interactive }
    ]"
    @click="interactive && $emit('click')"
  >
    <div class="bento-card__inner">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  variant?: 'solid' | 'gradient' | 'glass' | 'border'
  colSpan?: number
  rowSpan?: number
  interactive?: boolean
}>()

defineEmits<{
  click: []
}>()
</script>

<style scoped>
.bento-card {
  position: relative;
  overflow: hidden;
}

.bento-card__inner {
  height: 100%;
  border-radius: 16px;
  padding: 20px 24px;
  overflow: hidden;
}

/* Variants */
.bento-card--solid .bento-card__inner {
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
}

.bento-card--gradient .bento-card__inner {
  background: linear-gradient(135deg, var(--theme-primary) 0%, var(--theme-secondary) 100%);
  color: #fff;
  box-shadow: 0 8px 24px rgba(79, 70, 229, 0.18);
}

.bento-card--glass .bento-card__inner {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur, 18px));
  -webkit-backdrop-filter: blur(var(--glass-blur, 18px));
  border: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
}

.bento-card--border .bento-card__inner {
  background: transparent;
  border: 1px dashed var(--theme-border);
}

/* Interactive */
.bento-card--interactive {
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.bento-card--interactive:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.12);
}

/* Column spans */
.bento-card--col-1 { grid-column: span 1; }
.bento-card--col-2 { grid-column: span 2; }
.bento-card--col-3 { grid-column: span 3; }

/* Row spans */
.bento-card--row-1 { grid-row: span 1; }
.bento-card--row-2 { grid-row: span 2; }
.bento-card--row-3 { grid-row: span 3; }

@media (prefers-reduced-motion: reduce) {
  .bento-card--interactive {
    transition: none;
  }
  .bento-card--interactive:hover {
    transform: none;
  }
}
</style>
