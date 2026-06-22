<template>
  <div class="format-selector">
    <el-select
      :model-value="modelValue"
      :size="size"
      :style="{ width: `${width}px` }"
      @update:model-value="$emit('update:modelValue', $event)"
    >
      <el-option
        v-for="option in options"
        :key="option.value"
        :label="option.label"
        :value="option.value"
      />
    </el-select>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  modelValue: string
  options: Array<{ label: string; value: string }>
  size?: 'large' | 'default' | 'small'
  width?: number
}>(), {
  size: 'small',
  width: 120,
})

defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<style scoped>
.format-selector {
  display: inline-flex;
  align-items: center;
}
</style>
