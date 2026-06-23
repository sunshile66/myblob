<template>
  <div class="base-converter-panel">
    <IOPanel title="输入数值">
      <div class="input-section">
        <el-select v-model="inputBase" size="small" style="width: 120px">
          <el-option label="十进制" :value="10" />
          <el-option label="二进制" :value="2" />
          <el-option label="八进制" :value="8" />
          <el-option label="十六进制" :value="16" />
        </el-select>
        <el-input v-model="inputValue" :placeholder="inputPlaceholder" size="small" @input="convert">
          <template #append>
            <el-button @click="clearAll">清空</el-button>
          </template>
        </el-input>
      </div>
    </IOPanel>

    <div class="results-grid">
      <IOPanel title="十进制 (DEC)">
        <div class="result-item">
          <code>{{ decimal }}</code>
          <CopyButton v-if="decimal" :content="decimal" :show-label="false" text size="small" />
        </div>
      </IOPanel>
      <IOPanel title="二进制 (BIN)">
        <div class="result-item">
          <code>{{ binary }}</code>
          <CopyButton v-if="binary" :content="binary" :show-label="false" text size="small" />
        </div>
      </IOPanel>
      <IOPanel title="八进制 (OCT)">
        <div class="result-item">
          <code>{{ octal }}</code>
          <CopyButton v-if="octal" :content="octal" :show-label="false" text size="small" />
        </div>
      </IOPanel>
      <IOPanel title="十六进制 (HEX)">
        <div class="result-item">
          <code>{{ hex }}</code>
          <CopyButton v-if="hex" :content="hex" :show-label="false" text size="small" />
        </div>
      </IOPanel>
    </div>

    <IOPanel v-if="inputValue" title="数值信息">
      <div class="info-grid">
        <div class="info-item">
          <span class="info-label">字节数:</span>
          <span class="info-value">{{ byteCount }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">位数:</span>
          <span class="info-value">{{ bitCount }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">是否为 2 的幂:</span>
          <span class="info-value" :class="{ 'is-yes': isPowerOfTwo }">{{ isPowerOfTwo ? '是' : '否' }}</span>
        </div>
      </div>
    </IOPanel>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import IOPanel from '@/components/tools/IOPanel.vue'
import CopyButton from '@/components/tools/CopyButton.vue'

const inputBase = ref(10)
const inputValue = ref('')
const decimal = ref('')
const binary = ref('')
const octal = ref('')
const hex = ref('')

const inputPlaceholder = computed(() => {
  switch (inputBase.value) {
    case 2: return '输入二进制数，例如 1010'
    case 8: return '输入八进制数，例如 17'
    case 10: return '输入十进制数，例如 42'
    case 16: return '输入十六进制数，例如 2A'
    default: return '输入数值'
  }
})

const byteCount = computed(() => {
  if (!decimal.value) return 0
  const num = parseInt(decimal.value)
  if (isNaN(num)) return 0
  return Math.ceil(num.toString(2).length / 8)
})

const bitCount = computed(() => {
  if (!decimal.value) return 0
  const num = parseInt(decimal.value)
  if (isNaN(num)) return 0
  return num.toString(2).length
})

const isPowerOfTwo = computed(() => {
  if (!decimal.value) return false
  const num = parseInt(decimal.value)
  if (isNaN(num) || num <= 0) return false
  return (num & (num - 1)) === 0
})

const convert = () => {
  if (!inputValue.value.trim()) {
    decimal.value = ''
    binary.value = ''
    octal.value = ''
    hex.value = ''
    return
  }

  try {
    let value: number
    switch (inputBase.value) {
      case 2:
        value = parseInt(inputValue.value, 2)
        break
      case 8:
        value = parseInt(inputValue.value, 8)
        break
      case 16:
        value = parseInt(inputValue.value, 16)
        break
      default:
        value = parseInt(inputValue.value)
    }

    if (isNaN(value)) {
      ElMessage.warning('输入的数值无效')
      return
    }

    decimal.value = value.toString()
    binary.value = value.toString(2)
    octal.value = value.toString(8)
    hex.value = value.toString(16).toUpperCase()
  } catch {
    ElMessage.warning('转换失败')
  }
}

const clearAll = () => {
  inputValue.value = ''
  decimal.value = ''
  binary.value = ''
  octal.value = ''
  hex.value = ''
}
</script>

<style scoped>
.base-converter-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.input-section {
  padding: 16px;
  display: flex;
  gap: 12px;
}
.results-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
@media (max-width: 768px) {
  .results-grid {
    grid-template-columns: 1fr;
  }
}
.result-item {
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.result-item code {
  flex: 1;
  font-family: 'JetBrains Mono', monospace;
  font-size: 16px;
  word-break: break-all;
}
.info-grid {
  padding: 16px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.info-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.info-value {
  font-size: 14px;
  font-weight: 600;
}
.info-value.is-yes {
  color: #67c23a;
}
</style>
