<template>
  <BentoCard variant="solid" :col-span="1" :row-span="1" interactive @click="$emit('click', tool)">
    <div class="tools-block">
      <span class="tools-block__icon" :style="{ background: tool.accent }">
        {{ tool.name.slice(0, 1) }}
      </span>
      <div class="tools-block__body">
        <strong class="tools-block__name">{{ tool.name }}</strong>
        <span class="tools-block__desc">{{ tool.description }}</span>
      </div>
      <el-icon class="tools-block__arrow" :size="14"><ArrowRight /></el-icon>
    </div>
  </BentoCard>
</template>

<script setup lang="ts">
import { ArrowRight } from '@element-plus/icons-vue'
import BentoCard from '@/components/common/BentoCard.vue'
import type { ToolItem } from '@/data/toolCatalog'

defineProps<{
  tool: ToolItem
}>()

defineEmits<{
  click: [tool: ToolItem]
}>()
</script>

<style scoped>
.tools-block {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 100%;
}

.tools-block__icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: 800;
  flex-shrink: 0;
}

.tools-block__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.tools-block__name {
  font-size: 14px;
  font-weight: 700;
  color: var(--theme-text);
  line-height: 1.3;
}

.tools-block__desc {
  font-size: 12px;
  color: var(--theme-text-secondary);
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tools-block__arrow {
  color: var(--theme-text-secondary);
  opacity: 0;
  transform: translateX(-6px);
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.bento-card--interactive:hover .tools-block__arrow {
  opacity: 1;
  transform: translateX(0);
  color: var(--theme-primary);
}
</style>
