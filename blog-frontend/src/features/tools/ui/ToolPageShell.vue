<template>
  <LegacyToolPageShell
    :title="title"
    :description="description"
    :eyebrow="eyebrow"
    :back-label="backLabel"
    :meta="meta"
  >
    <slot />
  </LegacyToolPageShell>
</template>

<script setup lang="ts">
import LegacyToolPageShell from "@/components/tools/ToolPageShell.vue";

interface ToolMetaItem {
  label: string
  value: string
}

withDefaults(
  defineProps<{
    title: string
    description: string
    eyebrow?: string
    backLabel?: string
    meta?: ToolMetaItem[]
  }>(),
  {
    eyebrow: "效率工具",
    backLabel: "返回工具箱",
    meta: () => [],
  }
);
</script>
