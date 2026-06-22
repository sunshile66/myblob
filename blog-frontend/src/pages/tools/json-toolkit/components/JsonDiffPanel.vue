<template>
  <div class="json-diff-panel">
    <div class="panel-grid">
      <IOPanel title="原始 JSON" clearable @clear="leftJson = ''">
        <el-input
          v-model="leftJson"
          type="textarea"
          :rows="10"
          resize="none"
          placeholder="粘贴原始 JSON"
        />
      </IOPanel>
      <IOPanel title="对比 JSON" clearable @clear="rightJson = ''">
        <el-input
          v-model="rightJson"
          type="textarea"
          :rows="10"
          resize="none"
          placeholder="粘贴要对比的 JSON"
        />
      </IOPanel>
    </div>
    <div class="diff-actions">
      <el-button type="primary" @click="compare">对比</el-button>
      <el-button @click="clearAll">清空</el-button>
    </div>
    <IOPanel v-if="diffResult" title="对比结果" :content="diffResult" copyable>
      <pre class="diff-output"><code>{{ diffResult }}</code></pre>
    </IOPanel>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import IOPanel from '@/components/tools/IOPanel.vue'

const leftJson = ref('')
const rightJson = ref('')
const diffResult = ref('')

const compare = () => {
  try {
    const left = JSON.parse(leftJson.value)
    const right = JSON.parse(rightJson.value)
    const diffs = findDiffs(left, right, '')
    diffResult.value = diffs.length ? diffs.join('\n') : '两个 JSON 完全相同'
  } catch (e: any) {
    diffResult.value = '解析错误: ' + e.message
  }
}

const findDiffs = (left: any, right: any, path: string): string[] => {
  const diffs: string[] = []
  const allKeys = new Set([...Object.keys(left || {}), ...Object.keys(right || {})])

  for (const key of allKeys) {
    const currentPath = path ? `${path}.${key}` : key
    if (!(key in (left || {}))) {
      diffs.push(`+ ${currentPath}: ${JSON.stringify(right[key])}`)
    } else if (!(key in (right || {}))) {
      diffs.push(`- ${currentPath}: ${JSON.stringify(left[key])}`)
    } else if (typeof left[key] === 'object' && typeof right[key] === 'object') {
      diffs.push(...findDiffs(left[key], right[key], currentPath))
    } else if (left[key] !== right[key]) {
      diffs.push(`~ ${currentPath}: ${JSON.stringify(left[key])} → ${JSON.stringify(right[key])}`)
    }
  }
  return diffs
}

const clearAll = () => {
  leftJson.value = ''
  rightJson.value = ''
  diffResult.value = ''
}
</script>

<style scoped>
.json-diff-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.panel-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
@media (max-width: 1024px) {
  .panel-grid {
    grid-template-columns: 1fr;
  }
}
.diff-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}
.diff-output {
  margin: 0;
  padding: 16px;
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
}
</style>
