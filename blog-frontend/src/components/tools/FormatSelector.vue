<!--
/**
 * FormatSelector - 格式选择器组件
 * @description 通用的格式/选项选择器，封装 el-select
 *
 * @usage 基本用法
 * ```vue
 * <FormatSelector
 *   v-model="format"
 *   :options="[
 *     { label: 'JSON', value: 'json' },
 *     { label: 'XML', value: 'xml' }
 *   ]"
 * />
 * ```
 -->
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
/**
 * FormatSelector 组件属性
 */
withDefaults(defineProps<{
  /** 当前选中的值 */
  modelValue: string
  /** 选项列表 */
  options: Array<{ label: string; value: string }>
  /** 选择器大小 */
  size?: 'large' | 'default' | 'small'
  /** 选择器宽度（像素） */
  width?: number
}>(), {
  size: 'small',
  width: 120,
})

/**
 * FormatSelector 组件事件
 */
defineEmits<{
  /** 值变更时触发 */
  'update:modelValue': [value: string]
}>()
</script>

<style scoped>
.format-selector {
  display: inline-flex;
  align-items: center;
}
</style>
