<template>
  <div class="bento-grid" :style="gridStyle">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  cols?: number
  gap?: number
}>(), {
  cols: 3,
  gap: 16,
})

const gridStyle = computed(() => ({
  gridTemplateColumns: `repeat(${props.cols}, 1fr)`,
  gap: `${props.gap}px`,
}))
</script>

<style scoped>
.bento-grid {
  display: grid;
  grid-auto-rows: minmax(80px, auto);
}
</style>
