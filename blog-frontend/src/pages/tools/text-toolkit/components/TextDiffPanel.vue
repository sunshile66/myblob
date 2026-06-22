<template>
  <div class="text-diff-panel">
    <div class="panel-grid">
      <IOPanel title="原始文本" clearable @clear="leftText = ''">
        <el-input
          v-model="leftText"
          type="textarea"
          :rows="10"
          resize="none"
          placeholder="输入原始文本"
        />
      </IOPanel>
      <IOPanel title="对比文本" clearable @clear="rightText = ''">
        <el-input
          v-model="rightText"
          type="textarea"
          :rows="10"
          resize="none"
          placeholder="输入要对比的文本"
        />
      </IOPanel>
    </div>
    <ActionToolbar>
      <template #center>
        <el-button type="primary" size="small" @click="compare">对比</el-button>
        <el-button size="small" @click="clearAll">清空</el-button>
      </template>
    </ActionToolbar>
    <IOPanel v-if="diffResult" title="对比结果">
      <div class="diff-output">
        <div v-for="(line, index) in diffResult" :key="index" class="diff-line" :class="line.type">
          <span class="line-number">{{ line.lineNumber }}</span>
          <span class="line-content">{{ line.content }}</span>
        </div>
      </div>
    </IOPanel>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import IOPanel from '@/components/tools/IOPanel.vue'
import ActionToolbar from '@/components/tools/ActionToolbar.vue'

const leftText = ref('')
const rightText = ref('')
const diffResult = ref<Array<{ type: string; lineNumber: string; content: string }>>([])

const compare = () => {
  const leftLines = leftText.value.split('\n')
  const rightLines = rightText.value.split('\n')
  const result: Array<{ type: string; lineNumber: string; content: string }> = []

  const maxLen = Math.max(leftLines.length, rightLines.length)
  for (let i = 0; i < maxLen; i++) {
    const left = leftLines[i] || ''
    const right = rightLines[i] || ''
    if (left === right) {
      result.push({ type: 'equal', lineNumber: `${i + 1}`, content: left })
    } else {
      if (left) result.push({ type: 'removed', lineNumber: `${i + 1}`, content: `- ${left}` })
      if (right) result.push({ type: 'added', lineNumber: `${i + 1}`, content: `+ ${right}` })
    }
  }
  diffResult.value = result
}

const clearAll = () => {
  leftText.value = ''
  rightText.value = ''
  diffResult.value = []
}
</script>

<style scoped>
.text-diff-panel {
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
.diff-output {
  padding: 16px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
}
.diff-line {
  display: flex;
  gap: 12px;
  padding: 2px 8px;
  border-radius: 2px;
}
.diff-line.added {
  background: #dcfce7;
  color: #166534;
}
.diff-line.removed {
  background: #fee2e2;
  color: #991b1b;
}
.line-number {
  min-width: 30px;
  color: var(--el-text-color-secondary);
  user-select: none;
}
.line-content {
  flex: 1;
  white-space: pre-wrap;
}
</style>
