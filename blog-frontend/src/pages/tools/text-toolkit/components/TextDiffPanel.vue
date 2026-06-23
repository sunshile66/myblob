<template>
  <div class="text-diff-panel">
    <div class="panel-grid">
      <IOPanel title="原始文本" :subtitle="`共 ${leftLines.length} 行`" clearable @clear="leftText = ''">
        <el-input
          v-model="leftText"
          type="textarea"
          :rows="10"
          resize="none"
          placeholder="输入原始文本"
        />
      </IOPanel>
      <IOPanel title="对比文本" :subtitle="`共 ${rightLines.length} 行`" clearable @clear="rightText = ''">
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
        <el-button size="small" @click="swap">交换</el-button>
        <el-button size="small" @click="clearAll">清空</el-button>
      </template>
    </ActionToolbar>
    <IOPanel v-if="diffResult.length" title="对比结果">
      <template #actions>
        <div class="diff-stats">
          <span class="stat-added">+{{ stats.added }}</span>
          <span class="stat-removed">-{{ stats.removed }}</span>
          <span class="stat-equal">{{ stats.equal }} 相同</span>
        </div>
      </template>
      <div class="diff-output">
        <div v-for="(line, index) in diffResult" :key="index" class="diff-line" :class="line.type">
          <span class="line-number left">{{ line.leftLine || '' }}</span>
          <span class="line-number right">{{ line.rightLine || '' }}</span>
          <span class="line-indicator">{{ line.type === 'added' ? '+' : line.type === 'removed' ? '-' : ' ' }}</span>
          <span class="line-content">{{ line.content }}</span>
        </div>
      </div>
    </IOPanel>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import IOPanel from '@/components/tools/IOPanel.vue'
import ActionToolbar from '@/components/tools/ActionToolbar.vue'

const leftText = ref('')
const rightText = ref('')
const diffResult = ref<Array<{
  type: string
  leftLine: number | null
  rightLine: number | null
  content: string
}>>([])

const leftLines = computed(() => leftText.value.split('\n'))
const rightLines = computed(() => rightText.value.split('\n'))

const stats = computed(() => {
  let added = 0, removed = 0, equal = 0
  diffResult.value.forEach(line => {
    if (line.type === 'added') added++
    else if (line.type === 'removed') removed++
    else equal++
  })
  return { added, removed, equal }
})

const compare = () => {
  const left = leftText.value.split('\n')
  const right = rightText.value.split('\n')
  const result: typeof diffResult.value = []

  // Simple LCS-based diff
  const lcs = computeLCS(left, right)
  let leftIdx = 0, rightIdx = 0, lcsIdx = 0

  while (leftIdx < left.length || rightIdx < right.length) {
    if (lcsIdx < lcs.length && leftIdx < left.length && rightIdx < right.length &&
        left[leftIdx] === lcs[lcsIdx] && right[rightIdx] === lcs[lcsIdx]) {
      result.push({
        type: 'equal',
        leftLine: leftIdx + 1,
        rightLine: rightIdx + 1,
        content: left[leftIdx]
      })
      leftIdx++
      rightIdx++
      lcsIdx++
    } else if (leftIdx < left.length && (lcsIdx >= lcs.length || left[leftIdx] !== lcs[lcsIdx])) {
      result.push({
        type: 'removed',
        leftLine: leftIdx + 1,
        rightLine: null,
        content: left[leftIdx]
      })
      leftIdx++
    } else if (rightIdx < right.length) {
      result.push({
        type: 'added',
        leftLine: null,
        rightLine: rightIdx + 1,
        content: right[rightIdx]
      })
      rightIdx++
    }
  }

  diffResult.value = result
}

const computeLCS = (a: string[], b: string[]): string[] => {
  const m = a.length
  const n = b.length
  const dp: number[][] = Array.from({ length: m + 1 }, () => Array(n + 1).fill(0))

  for (let i = 1; i <= m; i++) {
    for (let j = 1; j <= n; j++) {
      if (a[i - 1] === b[j - 1]) {
        dp[i][j] = dp[i - 1][j - 1] + 1
      } else {
        dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1])
      }
    }
  }

  const result: string[] = []
  let i = m, j = n
  while (i > 0 && j > 0) {
    if (a[i - 1] === b[j - 1]) {
      result.unshift(a[i - 1])
      i--
      j--
    } else if (dp[i - 1][j] > dp[i][j - 1]) {
      i--
    } else {
      j--
    }
  }

  return result
}

const swap = () => {
  const temp = leftText.value
  leftText.value = rightText.value
  rightText.value = temp
  compare()
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
.diff-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
}
.stat-added {
  color: var(--el-color-success);
  font-weight: 600;
}
.stat-removed {
  color: var(--el-color-danger);
  font-weight: 600;
}
.stat-equal {
  color: var(--el-text-color-secondary);
}
.diff-output {
  padding: 16px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  line-height: 1.6;
  max-height: 500px;
  overflow: auto;
}
.diff-line {
  display: flex;
  gap: 8px;
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
.diff-line.equal {
  color: var(--el-text-color-regular);
}
.line-number {
  min-width: 35px;
  color: var(--el-text-color-secondary);
  user-select: none;
  text-align: right;
  font-size: 11px;
}
.line-indicator {
  min-width: 16px;
  text-align: center;
  font-weight: 600;
}
.line-content {
  flex: 1;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
