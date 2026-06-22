<template>
  <div class="news-filters">
    <div class="cat-bar">
      <button
        v-for="c in categories"
        :key="c.value"
        :class="['cat-tab', { active: modelValue.category === c.value, airline: c.value === '国际航司' }]"
        @click="$emit('update:modelValue', { ...modelValue, category: c.value })"
      >
        {{ c.label }}
      </button>
    </div>
    <div class="lang-switch">
      <button
        v-for="l in languages"
        :key="l.value"
        :class="['lang-btn', { active: modelValue.language === l.value }]"
        @click="$emit('update:modelValue', { ...modelValue, language: l.value })"
      >
        {{ l.label }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
interface FilterState {
  category: string
  language: string
}

defineProps<{
  modelValue: FilterState
  categories: Array<{ value: string; label: string }>
  languages: Array<{ value: string; label: string }>
}>()

defineEmits<{
  'update:modelValue': [value: FilterState]
}>()
</script>

<style scoped>
.news-filters {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}
.cat-bar {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
.cat-tab {
  padding: 6px 12px;
  border: none;
  background: var(--el-fill-color-light);
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}
.cat-tab:hover {
  background: var(--el-fill-color);
}
.cat-tab.active {
  background: var(--el-color-primary);
  color: white;
}
.cat-tab.airline.active {
  background: #e6a23c;
}
.lang-switch {
  display: flex;
  gap: 4px;
}
.lang-btn {
  padding: 4px 8px;
  border: 1px solid var(--el-border-color);
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}
.lang-btn.active {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
</style>
