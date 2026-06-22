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
          <el-button text size="small" @click="copyOutput">复制</el-button>
        </template>
        <div class="output-area">
          <pre v-if="!parseError" class="code-output"><code>{{ formattedOutput || '格式化结果会显示在这里' }}</code></pre>
          <div v-else class="error-message">
            <el-alert :title="parseError" type="error" show-icon :closable="false" />
          </div>
        </div>
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
    return '有效 JSON'
  } catch {
    return '无效 JSON'
  }
})

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

const copyOutput = async () => {
  if (!formattedOutput.value) return
  await navigator.clipboard.writeText(formattedOutput.value)
  ElMessage.success('已复制到剪贴板')
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
</style>
