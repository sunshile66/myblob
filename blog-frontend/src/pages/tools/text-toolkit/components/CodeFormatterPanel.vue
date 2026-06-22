<template>
  <div class="code-formatter-panel">
    <ActionToolbar>
      <template #left>
        <el-select v-model="language" size="small" style="width: 120px">
          <el-option label="JavaScript" value="javascript" />
          <el-option label="HTML" value="html" />
          <el-option label="CSS" value="css" />
          <el-option label="JSON" value="json" />
        </el-select>
      </template>
      <template #center>
        <el-button type="primary" size="small" @click="format">格式化</el-button>
        <el-button size="small" @click="minify">压缩</el-button>
        <el-button size="small" @click="clearAll">清空</el-button>
      </template>
    </ActionToolbar>

    <div class="panel-grid">
      <IOPanel title="输入代码">
        <el-input
          v-model="inputCode"
          type="textarea"
          :rows="12"
          resize="none"
          :placeholder="`输入 ${language} 代码`"
        />
      </IOPanel>
      <IOPanel title="格式化结果" :content="outputCode" copyable>
        <el-input
          v-model="outputCode"
          type="textarea"
          :rows="12"
          resize="none"
          readonly
          placeholder="结果会显示在这里"
        />
      </IOPanel>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import IOPanel from '@/components/tools/IOPanel.vue'
import ActionToolbar from '@/components/tools/ActionToolbar.vue'

const language = ref('javascript')
const inputCode = ref('')
const outputCode = ref('')

const format = () => {
  if (!inputCode.value.trim()) return
  try {
    switch (language.value) {
      case 'json':
        const parsed = JSON.parse(inputCode.value)
        outputCode.value = JSON.stringify(parsed, null, 2)
        break
      case 'html':
        outputCode.value = formatHtml(inputCode.value)
        break
      case 'css':
        outputCode.value = formatCss(inputCode.value)
        break
      case 'javascript':
        outputCode.value = formatJs(inputCode.value)
        break
    }
  } catch (e: any) {
    ElMessage.error('格式化失败: ' + e.message)
  }
}

const minify = () => {
  if (!inputCode.value.trim()) return
  try {
    switch (language.value) {
      case 'json':
        const parsed = JSON.parse(inputCode.value)
        outputCode.value = JSON.stringify(parsed)
        break
      case 'html':
        outputCode.value = inputCode.value.replace(/>\s+</g, '><').replace(/\s+/g, ' ').trim()
        break
      case 'css':
        outputCode.value = inputCode.value.replace(/\s*{\s*/g, '{').replace(/\s*}\s*/g, '}').replace(/\s*;\s*/g, ';').replace(/\s*:\s*/g, ':')
        break
      case 'javascript':
        outputCode.value = inputCode.value.replace(/\/\/.*$/gm, '').replace(/\/\*[\s\S]*?\*\//g, '').replace(/\s+/g, ' ').trim()
        break
    }
  } catch (e: any) {
    ElMessage.error('压缩失败: ' + e.message)
  }
}

const formatHtml = (html: string): string => {
  let formatted = ''
  let indent = 0
  const lines = html.replace(/>\s*</g, '>\n<').split('\n')
  for (const line of lines) {
    const trimmed = line.trim()
    if (!trimmed) continue
    if (trimmed.startsWith('</')) indent--
    formatted += '  '.repeat(Math.max(0, indent)) + trimmed + '\n'
    if (trimmed.startsWith('<') && !trimmed.startsWith('</') && !trimmed.endsWith('/>')) indent++
  }
  return formatted.trim()
}

const formatCss = (css: string): string => {
  return css
    .replace(/\s*{\s*/g, ' {\n  ')
    .replace(/\s*}\s*/g, '\n}\n')
    .replace(/\s*;\s*/g, ';\n  ')
    .replace(/\s*:\s*/g, ': ')
    .replace(/\n\s*\n/g, '\n')
    .trim()
}

const formatJs = (js: string): string => {
  let formatted = js
  let indent = 0
  const lines = js.split('\n')
  const result: string[] = []
  for (const line of lines) {
    const trimmed = line.trim()
    if (!trimmed) continue
    if (trimmed.startsWith('}') || trimmed.startsWith(']')) indent--
    result.push('  '.repeat(Math.max(0, indent)) + trimmed)
    if (trimmed.endsWith('{') || trimmed.endsWith('[')) indent++
  }
  return result.join('\n')
}

const clearAll = () => {
  inputCode.value = ''
  outputCode.value = ''
}
</script>

<style scoped>
.code-formatter-panel {
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
</style>
