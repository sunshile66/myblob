<template>
  <div class="hash-panel">
    <IOPanel title="输入文本" clearable @clear="inputText = ''">
      <template #actions>
        <el-checkbox v-model="uppercase" size="small">大写</el-checkbox>
        <el-select v-model="encoding" size="small" style="width: 100px">
          <el-option label="UTF-8" value="utf8" />
          <el-option label="Hex" value="hex" />
          <el-option label="Base64" value="base64" />
        </el-select>
      </template>
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="4"
        resize="none"
        :placeholder="inputPlaceholder"
        @input="debouncedCalculate"
      />
    </IOPanel>
    <div class="hash-results">
      <div v-for="hash in hashResults" :key="hash.name" class="hash-item">
        <div class="hash-label">{{ hash.name }}</div>
        <div class="hash-value">
          <code>{{ hash.value || '-' }}</code>
          <CopyButton v-if="hash.value" :content="hash.value" :show-label="false" text size="small" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import CryptoJS from 'crypto-js'
import { sm3 } from '@/utils/sm3'
import IOPanel from '@/components/tools/IOPanel.vue'
import CopyButton from '@/components/tools/CopyButton.vue'

const inputText = ref('')
const uppercase = ref(false)
const encoding = ref('utf8')

const inputPlaceholder = computed(() => {
  switch (encoding.value) {
    case 'hex': return '输入十六进制字符串，例如 48656c6c6f'
    case 'base64': return '输入 Base64 字符串，例如 SGVsbG8='
    default: return '输入要计算哈希的文本'
  }
})

const getInputBytes = (): string => {
  if (!inputText.value) return ''
  switch (encoding.value) {
    case 'hex':
      return CryptoJS.enc.Hex.parse(inputText.value).toString(CryptoJS.enc.Utf8)
    case 'base64':
      return CryptoJS.enc.Base64.parse(inputText.value).toString(CryptoJS.enc.Utf8)
    default:
      return inputText.value
  }
}

const hashResults = computed(() => {
  if (!inputText.value) return []
  const input = getInputBytes()
  if (!input) return []

  const results = [
    { name: 'MD5', value: CryptoJS.MD5(input).toString() },
    { name: 'SHA1', value: CryptoJS.SHA1(input).toString() },
    { name: 'SHA256', value: CryptoJS.SHA256(input).toString() },
    { name: 'SHA512', value: CryptoJS.SHA512(input).toString() },
    { name: 'SM3', value: sm3(input) },
    { name: 'HMAC-MD5', value: CryptoJS.HmacMD5(input, 'key').toString() },
    { name: 'HMAC-SHA256', value: CryptoJS.HmacSHA256(input, 'key').toString() },
  ]

  return results.map(r => ({
    ...r,
    value: uppercase.value ? r.value.toUpperCase() : r.value
  }))
})

const calculate = () => {
  // Trigger computed recalculation
}

const debouncedCalculate = useDebounceFn(calculate, 300)
</script>

<style scoped>
.hash-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.hash-results {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 12px;
}
.hash-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
  transition: background-color 0.2s;
}
.hash-item:hover {
  background: var(--el-fill-color-light);
}
.hash-label {
  font-weight: 600;
  font-size: 13px;
  min-width: 100px;
  color: var(--el-text-color-regular);
}
.hash-value {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
}
.hash-value code {
  flex: 1;
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  word-break: break-all;
  color: var(--el-text-color-primary);
}
</style>
