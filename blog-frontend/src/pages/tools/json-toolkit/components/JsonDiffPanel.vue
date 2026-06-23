<template>
  <div class="json-diff-panel">
    <div class="panel-grid">
      <IOPanel title="原始 JSON" :subtitle="leftStatus" clearable @clear="leftJson = ''">
        <el-input
          v-model="leftJson"
          type="textarea"
          :rows="10"
          resize="none"
          spellcheck="false"
          placeholder='粘贴原始 JSON，例如 {"name":"test","value":1}'
        />
      </IOPanel>
      <IOPanel title="对比 JSON" :subtitle="rightStatus" clearable @clear="rightJson = ''">
        <el-input
          v-model="rightJson"
          type="textarea"
          :rows="10"
          resize="none"
          spellcheck="false"
          placeholder='粘贴要对比的 JSON，例如 {"name":"test","value":2}'
        />
      </IOPanel>
    </div>
    <ActionToolbar>
      <template #center>
        <el-button type="primary" size="small" @click="compare">对比</el-button>
        <el-button size="small" @click="swap">交换</el-button>
        <el-button size="small" @click="clearAll">清空</el-button>
        <el-button size="small" text @click="loadSample">示例</el-button>
      </template>
    </ActionToolbar>

    <IOPanel v-if="diffs.length" title="对比结果">
      <template #actions>
        <div class="diff-stats">
          <span class="stat stat-added">+{{ stats.added }} 新增</span>
          <span class="stat stat-removed">-{{ stats.removed }} 删除</span>
          <span class="stat stat-modified">~{{ stats.modified }} 修改</span>
          <span class="stat stat-equal">={{ stats.equal }} 相同</span>
        </div>
      </template>
      <div class="diff-list">
        <div v-for="(diff, index) in diffs" :key="index" class="diff-item" :class="diff.type">
          <span class="diff-indicator">{{ diff.type === 'added' ? '+' : diff.type === 'removed' ? '-' : diff.type === 'modified' ? '~' : '=' }}</span>
          <span class="diff-path">{{ diff.path }}</span>
          <span v-if="diff.type === 'added'" class="diff-value new">{{ formatValue(diff.rightValue) }}</span>
          <span v-else-if="diff.type === 'removed'" class="diff-value old">{{ formatValue(diff.leftValue) }}</span>
          <span v-else-if="diff.type === 'modified'" class="diff-value">
            <span class="old">{{ formatValue(diff.leftValue) }}</span>
            <span class="arrow">→</span>
            <span class="new">{{ formatValue(diff.rightValue) }}</span>
          </span>
          <span v-else class="diff-value equal">{{ formatValue(diff.leftValue) }}</span>
        </div>
      </div>
      <template #footer>
        <CopyButton :content="diffsText" label="复制差异" size="small" />
      </template>
    </IOPanel>

    <IOPanel v-else-if="hasCompared" title="对比结果">
      <div class="no-diff">
        <el-icon :size="48" color="#67c23a"><CircleCheck /></el-icon>
        <p>两个 JSON 完全相同</p>
      </div>
    </IOPanel>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { CircleCheck } from '@element-plus/icons-vue'
import IOPanel from '@/components/tools/IOPanel.vue'
import ActionToolbar from '@/components/tools/ActionToolbar.vue'
import CopyButton from '@/components/tools/CopyButton.vue'

interface DiffItem {
  type: 'added' | 'removed' | 'modified' | 'equal'
  path: string
  leftValue?: any
  rightValue?: any
}

const leftJson = ref('')
const rightJson = ref('')
const diffs = ref<DiffItem[]>([])
const hasCompared = ref(false)

const leftStatus = computed(() => {
  if (!leftJson.value) return ''
  try { JSON.parse(leftJson.value); return '✓ 有效' } catch { return '✗ 无效' }
})

const rightStatus = computed(() => {
  if (!rightJson.value) return ''
  try { JSON.parse(rightJson.value); return '✓ 有效' } catch { return '✗ 无效' }
})

const stats = computed(() => {
  let added = 0, removed = 0, modified = 0, equal = 0
  diffs.value.forEach(d => {
    if (d.type === 'added') added++
    else if (d.type === 'removed') removed++
    else if (d.type === 'modified') modified++
    else equal++
  })
  return { added, removed, modified, equal }
})

const diffsText = computed(() => {
  return diffs.value.map(d => {
    const indicator = d.type === 'added' ? '+' : d.type === 'removed' ? '-' : d.type === 'modified' ? '~' : '='
    if (d.type === 'added') return `${indicator} ${d.path}: ${JSON.stringify(d.rightValue)}`
    if (d.type === 'removed') return `${indicator} ${d.path}: ${JSON.stringify(d.leftValue)}`
    if (d.type === 'modified') return `${indicator} ${d.path}: ${JSON.stringify(d.leftValue)} → ${JSON.stringify(d.rightValue)}`
    return `${indicator} ${d.path}: ${JSON.stringify(d.leftValue)}`
  }).join('\n')
})

const formatValue = (val: any): string => {
  if (val === undefined) return 'undefined'
  if (val === null) return 'null'
  if (typeof val === 'string') return `"${val.length > 50 ? val.substring(0, 47) + '...' : val}"`
  if (typeof val === 'object') return JSON.stringify(val).substring(0, 50) + (JSON.stringify(val).length > 50 ? '...' : '')
  return String(val)
}

const compare = () => {
  hasCompared.value = true
  diffs.value = []
  try {
    const left = JSON.parse(leftJson.value)
    const right = JSON.parse(rightJson.value)
    diffs.value = findDiffs(left, right, '$')
  } catch (e: any) {
    diffs.value = [{ type: 'modified', path: '错误', leftValue: e.message, rightValue: null }]
  }
}

const findDiffs = (left: any, right: any, path: string): DiffItem[] => {
  const result: DiffItem[] = []

  if (typeof left !== typeof right) {
    result.push({ type: 'modified', path, leftValue: left, rightValue: right })
    return result
  }

  if (typeof left !== 'object' || left === null || right === null) {
    if (left !== right) {
      result.push({ type: 'modified', path, leftValue: left, rightValue: right })
    } else {
      result.push({ type: 'equal', path, leftValue: left })
    }
    return result
  }

  if (Array.isArray(left) && Array.isArray(right)) {
    const maxLen = Math.max(left.length, right.length)
    for (let i = 0; i < maxLen; i++) {
      const itemPath = `${path}[${i}]`
      if (i >= left.length) {
        result.push({ type: 'added', path: itemPath, rightValue: right[i] })
      } else if (i >= right.length) {
        result.push({ type: 'removed', path: itemPath, leftValue: left[i] })
      } else {
        result.push(...findDiffs(left[i], right[i], itemPath))
      }
    }
    return result
  }

  const allKeys = new Set([...Object.keys(left), ...Object.keys(right)])
  for (const key of allKeys) {
    const keyPath = /^[a-zA-Z_$][a-zA-Z0-9_$]*$/.test(key) ? `${path}.${key}` : `${path}["${key}"]`
    if (!(key in left)) {
      result.push({ type: 'added', path: keyPath, rightValue: right[key] })
    } else if (!(key in right)) {
      result.push({ type: 'removed', path: keyPath, leftValue: left[key] })
    } else {
      result.push(...findDiffs(left[key], right[key], keyPath))
    }
  }

  return result
}

const swap = () => {
  const temp = leftJson.value
  leftJson.value = rightJson.value
  rightJson.value = temp
  if (hasCompared.value) compare()
}

const clearAll = () => {
  leftJson.value = ''
  rightJson.value = ''
  diffs.value = []
  hasCompared.value = false
}

const loadSample = () => {
  leftJson.value = JSON.stringify({
    name: "MyBlob",
    version: "1.0.0",
    config: { theme: "dark", lang: "zh" },
    features: ["blog", "news"]
  }, null, 2)
  rightJson.value = JSON.stringify({
    name: "MyBlob",
    version: "1.1.0",
    config: { theme: "light", lang: "en", debug: true },
    features: ["blog", "news", "tools"],
    newField: "added"
  }, null, 2)
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
.diff-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
}
.stat {
  font-weight: 600;
}
.stat-added { color: var(--theme-success); }
.stat-removed { color: var(--theme-danger); }
.stat-modified { color: var(--theme-warning); }
.stat-equal { color: var(--theme-text-tertiary); }
.diff-list {
  max-height: 400px;
  overflow: auto;
  padding: 16px;
}
.diff-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 4px;
  margin-bottom: 4px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
}
.diff-item.added { background: var(--diff-added-bg); }
.diff-item.removed { background: var(--diff-removed-bg); }
.diff-item.modified { background: var(--diff-modified-bg); }
.diff-item.equal { background: var(--theme-muted); }
.diff-indicator {
  min-width: 16px;
  text-align: center;
  font-weight: 600;
}
.diff-item.added .diff-indicator { color: var(--diff-added-text); }
.diff-item.removed .diff-indicator { color: var(--diff-removed-text); }
.diff-item.modified .diff-indicator { color: var(--diff-modified-text); }
.diff-item.equal .diff-indicator { color: var(--theme-text-tertiary); }
.diff-path {
  color: var(--theme-info);
  min-width: 150px;
}
.diff-value {
  flex: 1;
  word-break: break-all;
}
.diff-value .old { color: var(--theme-danger); text-decoration: line-through; }
.diff-value .new { color: var(--theme-success); }
.diff-value .arrow { margin: 0 8px; color: var(--theme-text-tertiary); }
.diff-value.equal { color: var(--theme-text-secondary); }
.no-diff {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  gap: 16px;
}
.no-diff p {
  font-size: 16px;
  color: var(--theme-success);
  margin: 0;
}
</style>
