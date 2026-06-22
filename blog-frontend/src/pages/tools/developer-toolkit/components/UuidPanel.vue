<template>
  <div class="uuid-panel">
    <ActionToolbar>
      <template #center>
        <el-input-number v-model="count" :min="1" :max="100" size="small" style="width: 100px" />
        <el-checkbox v-model="uppercase" size="small">大写</el-checkbox>
        <el-checkbox v-model="noHyphens" size="small">无连字符</el-checkbox>
        <el-button type="primary" size="small" @click="generate">生成</el-button>
      </template>
    </ActionToolbar>

    <IOPanel v-if="uuids.length" title="生成结果" :content="uuids.join('\n')" copyable>
      <div class="uuid-list">
        <div v-for="(uuid, index) in uuids" :key="index" class="uuid-item">
          <code>{{ uuid }}</code>
          <CopyButton :content="uuid" :show-label="false" text size="small" />
        </div>
      </div>
    </IOPanel>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import IOPanel from '@/components/tools/IOPanel.vue'
import ActionToolbar from '@/components/tools/ActionToolbar.vue'
import CopyButton from '@/components/tools/CopyButton.vue'

const count = ref(5)
const uppercase = ref(false)
const noHyphens = ref(false)
const uuids = ref<string[]>([])

const generateUuid = (): string => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

const generate = () => {
  uuids.value = Array.from({ length: count.value }, () => {
    let uuid = generateUuid()
    if (uppercase.value) uuid = uuid.toUpperCase()
    if (noHyphens.value) uuid = uuid.replace(/-/g, '')
    return uuid
  })
}
</script>

<style scoped>
.uuid-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.uuid-list {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.uuid-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
}
.uuid-item code {
  flex: 1;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
}
</style>
