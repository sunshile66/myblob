<template>
  <div class="encoder-panel">
    <div class="panel-grid">
      <IOPanel title="输入文本" clearable @clear="inputText = ''">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="6"
          resize="none"
          placeholder="输入要编码/解码的文本"
        />
      </IOPanel>
      <IOPanel title="输出结果" :content="outputText" copyable>
        <el-input
          v-model="outputText"
          type="textarea"
          :rows="6"
          resize="none"
          readonly
          placeholder="结果会显示在这里"
        />
      </IOPanel>
    </div>
    <ActionToolbar>
      <template #center>
        <el-select v-model="encodingType" size="small" style="width: 120px">
          <el-option label="Base64" value="base64" />
          <el-option label="URL" value="url" />
          <el-option label="HTML" value="html" />
          <el-option label="Unicode" value="unicode" />
        </el-select>
        <el-button type="primary" size="small" @click="encode">编码</el-button>
        <el-button size="small" @click="decode">解码</el-button>
        <el-button size="small" @click="swap">交换</el-button>
      </template>
    </ActionToolbar>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import IOPanel from '@/components/tools/IOPanel.vue'
import ActionToolbar from '@/components/tools/ActionToolbar.vue'

const inputText = ref('')
const outputText = ref('')
const encodingType = ref('base64')

const encode = () => {
  if (!inputText.value) return
  try {
    switch (encodingType.value) {
      case 'base64':
        outputText.value = btoa(unescape(encodeURIComponent(inputText.value)))
        break
      case 'url':
        outputText.value = encodeURIComponent(inputText.value)
        break
      case 'html':
        outputText.value = inputText.value
          .replace(/&/g, '&amp;')
          .replace(/</g, '&lt;')
          .replace(/>/g, '&gt;')
          .replace(/"/g, '&quot;')
          .replace(/'/g, '&#039;')
        break
      case 'unicode':
        outputText.value = Array.from(inputText.value)
          .map(c => '\\u' + c.charCodeAt(0).toString(16).padStart(4, '0'))
          .join('')
        break
    }
  } catch (e: any) {
    ElMessage.error('编码失败: ' + e.message)
  }
}

const decode = () => {
  if (!inputText.value) return
  try {
    switch (encodingType.value) {
      case 'base64':
        outputText.value = decodeURIComponent(escape(atob(inputText.value)))
        break
      case 'url':
        outputText.value = decodeURIComponent(inputText.value)
        break
      case 'html':
        outputText.value = inputText.value
          .replace(/&amp;/g, '&')
          .replace(/&lt;/g, '<')
          .replace(/&gt;/g, '>')
          .replace(/&quot;/g, '"')
          .replace(/&#039;/g, "'")
        break
      case 'unicode':
        outputText.value = inputText.value.replace(/\\u([0-9a-fA-F]{4})/g, (_, hex) =>
          String.fromCharCode(parseInt(hex, 16))
        )
        break
    }
  } catch (e: any) {
    ElMessage.error('解码失败: ' + e.message)
  }
}

const swap = () => {
  const temp = inputText.value
  inputText.value = outputText.value
  outputText.value = temp
}
</script>

<style scoped>
.encoder-panel {
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
