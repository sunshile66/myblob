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
  border-radius: 14px;
  padding: 22px 26px;
  overflow: hidden;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

/* Variants */
.bento-card--solid .bento-card__inner {
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.bento-card--solid.bento-card--interactive:hover .bento-card__inner {
  border-color: var(--theme-border-strong);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06), 0 2px 4px rgba(15, 23, 42, 0.04);
}

.bento-card--gradient .bento-card__inner {
  background: linear-gradient(135deg, var(--theme-primary) 0%, var(--theme-secondary) 100%);
  color: #fff;
  box-shadow: 0 8px 24px rgba(79, 70, 229, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.1);
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
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.bento-card--interactive:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.1);
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
  .bento-card__inner {
    transition: none;
  }
}
</style>
