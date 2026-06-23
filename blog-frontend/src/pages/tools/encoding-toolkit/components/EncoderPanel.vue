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
          <el-option label="Hex" value="hex" />
          <el-option label="Binary" value="binary" />
        </el-select>
        <el-button type="primary" size="small" @click="encode">编码</el-button>
        <el-button size="small" @click="decode">解码</el-button>
        <el-button size="small" @click="swap">交换</el-button>
      </template>
    </ActionToolbar>

    <IOPanel v-if="encodingInfo" title="编码说明">
      <div class="encoding-info">
        <p>{{ encodingInfo }}</p>
      </div>
    </IOPanel>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import IOPanel from '@/components/tools/IOPanel.vue'
import ActionToolbar from '@/components/tools/ActionToolbar.vue'

const inputText = ref('')
const outputText = ref('')
const encodingType = ref('base64')

const encodingInfo = computed(() => {
  switch (encodingType.value) {
    case 'base64': return 'Base64 是一种基于 64 个可打印字符来表示二进制数据的编码方式，常用于在 HTTP 环境下传递较长的标识信息。'
    case 'url': return 'URL 编码（百分号编码）用于在 URL 中传递特殊字符，将非 ASCII 字符转换为 %XX 格式。'
    case 'html': return 'HTML 实体编码用于在 HTML 中显示保留字符，如 < 转换为 &lt;，防止 XSS 攻击。'
    case 'unicode': return 'Unicode 转义序列将字符转换为 \\uXXXX 格式，常用于 JavaScript 和 JSON 中。'
    case 'hex': return '十六进制编码将每个字节转换为两个十六进制字符，常用于表示二进制数据。'
    case 'binary': return '二进制编码将每个字节转换为 8 位二进制数字。'
    default: return ''
  }
})

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
      case 'hex':
        outputText.value = Array.from(inputText.value)
          .map(c => c.charCodeAt(0).toString(16).padStart(2, '0'))
          .join('')
        break
      case 'binary':
        outputText.value = Array.from(inputText.value)
          .map(c => c.charCodeAt(0).toString(2).padStart(8, '0'))
          .join(' ')
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
      case 'hex':
        outputText.value = inputText.value.match(/.{1,2}/g)
          ?.map(hex => String.fromCharCode(parseInt(hex, 16)))
          .join('') || ''
        break
      case 'binary':
        outputText.value = inputText.value.split(' ')
          .map(bin => String.fromCharCode(parseInt(bin, 2)))
          .join('')
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
.encoding-info {
  padding: 16px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
}
</style>
