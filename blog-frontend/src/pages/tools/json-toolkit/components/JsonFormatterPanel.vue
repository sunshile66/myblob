<template>
  <div class="json-formatter-panel">
    <div class="panel-grid">
      <IOPanel
        title="输入 JSON"
        :subtitle="inputStatus"
        clearable
        @clear="inputJson = ''"
      >
        <template #actions>
          <el-select v-model="indentSize" size="small" style="width: 100px">
            <el-option label="2 空格" :value="2" />
            <el-option label="4 空格" :value="4" />
            <el-option label="Tab" :value="tabValue" />
          </el-select>
          <el-checkbox v-model="sortKeys" size="small">排序</el-checkbox>
          <el-button text size="small" @click="loadSample">示例</el-button>
        </template>
        <el-input
          v-model="inputJson"
          type="textarea"
          :rows="12"
          resize="none"
          spellcheck="false"
          placeholder='请输入 JSON，例如 {"name":"test"}'
          @input="debouncedFormat"
        />
      </IOPanel>

      <IOPanel
        title="格式化结果"
        :content="formattedOutput"
        copyable
      >
        <template #actions>
          <el-button text size="small" @click="minifyJson">压缩</el-button>
          <el-button text size="small" @click="downloadJson">下载</el-button>
        </template>
        <div class="output-area">
          <pre v-if="!parseError" class="code-output"><code>{{ formattedOutput || '格式化结果会显示在这里' }}</code></pre>
          <div v-else class="error-message">
            <el-alert :title="parseError" type="error" show-icon :closable="false" />
          </div>
        </div>
        <template #footer>
          <div v-if="jsonStats" class="stats-bar">
            <span>类型: {{ jsonStats.type }}</span>
            <span v-if="jsonStats.keys">键: {{ jsonStats.keys }}</span>
            <span v-if="jsonStats.depth">深度: {{ jsonStats.depth }}</span>
            <span>大小: {{ jsonStats.size }}</span>
          </div>
        </template>
      </IOPanel>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import { ElMessage } from 'element-plus'
import IOPanel from '@/components/tools/IOPanel.vue'

const inputJson = ref('')
const indentSize = ref(2)
const sortKeys = ref(false)
const formattedOutput = ref('')
const parseError = ref('')
const tabValue = '\t'

const inputStatus = computed(() => {
  if (!inputJson.value) return ''
  try {
    JSON.parse(inputJson.value)
    return '✓ 有效 JSON'
  } catch (e: any) {
    return '✗ ' + e.message.substring(0, 50)
  }
})

const jsonStats = computed(() => {
  if (!formattedOutput.value) return null
  try {
    const parsed = JSON.parse(formattedOutput.value)
    const type = Array.isArray(parsed) ? 'Array' : typeof parsed === 'object' ? 'Object' : typeof parsed
    const keys = typeof parsed === 'object' && parsed !== null ? Object.keys(parsed).length : 0
    const depth = getDepth(parsed)
    const size = new Blob([formattedOutput.value]).size
    return {
      type,
      keys,
      depth,
      size: size < 1024 ? size + ' B' : (size / 1024).toFixed(1) + ' KB'
    }
  } catch {
    return null
  }
})

const getDepth = (obj: any): number => {
  if (typeof obj !== 'object' || obj === null) return 0
  let maxDepth = 0
  for (const key of Object.keys(obj)) {
    const depth = getDepth(obj[key])
    if (depth > maxDepth) maxDepth = depth
  }
  return maxDepth + 1
}

const formatJson = () => {
  parseError.value = ''
  if (!inputJson.value.trim()) {
    formattedOutput.value = ''
    return
  }
  try {
    const parsed = JSON.parse(inputJson.value)
    formattedOutput.value = JSON.stringify(parsed, sortKeys.value ? null : undefined, indentSize.value)
  } catch (e: any) {
    parseError.value = e.message
    formattedOutput.value = ''
  }
}

const debouncedFormat = useDebounceFn(formatJson, 300)

const minifyJson = () => {
  if (!formattedOutput.value) return
  try {
    const parsed = JSON.parse(formattedOutput.value)
    formattedOutput.value = JSON.stringify(parsed)
  } catch {}
}

const downloadJson = () => {
  if (!formattedOutput.value) return
  const blob = new Blob([formattedOutput.value], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'formatted.json'
  a.click()
  URL.revokeObjectURL(url)
}

const loadSample = () => {
  inputJson.value = JSON.stringify({
    name: "MyBlob",
    version: "1.0.0",
    features: ["blog", "news", "tools"],
    config: {
      theme: "dark",
      language: "zh-CN",
      debug: false
    },
    users: [
      { id: 1, name: "Admin", role: "admin" },
      { id: 2, name: "User", role: "user" }
    ]
  }, null, 2)
}
</script>

<style scoped>
.json-formatter-panel {
  height: 100%;
}
.panel-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  height: 100%;
}
@media (max-width: 1024px) {
  .panel-grid {
    grid-template-columns: 1fr;
  }
}
.output-area {
  min-height: 300px;
  max-height: 500px;
  overflow: auto;
}
.code-output {
  margin: 0;
  padding: 16px;
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}
.error-message {
  padding: 16px;
}
.stats-bar {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
