<template>
  <div class="hash-panel">
    <IOPanel title="输入文本" clearable @clear="inputText = ''">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="4"
        resize="none"
        placeholder="输入要计算哈希的文本"
        @input="debouncedCalculate"
      />
    </IOPanel>
    <ActionToolbar>
      <template #center>
        <el-checkbox v-model="uppercase" size="small" @change="calculate">大写</el-checkbox>
      </template>
    </ActionToolbar>
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
import IOPanel from '@/components/tools/IOPanel.vue'
import ActionToolbar from '@/components/tools/ActionToolbar.vue'
import CopyButton from '@/components/tools/CopyButton.vue'

const inputText = ref('')
const uppercase = ref(false)

const hashResults = computed(() => {
  if (!inputText.value) return []
  const results = [
    { name: 'MD5', value: CryptoJS.MD5(inputText.value).toString() },
    { name: 'SHA1', value: CryptoJS.SHA1(inputText.value).toString() },
    { name: 'SHA256', value: CryptoJS.SHA256(inputText.value).toString() },
    { name: 'SHA512', value: CryptoJS.SHA512(inputText.value).toString() },
    { name: 'HMAC-MD5', value: CryptoJS.HmacMD5(inputText.value, 'key').toString() },
    { name: 'HMAC-SHA256', value: CryptoJS.HmacSHA256(inputText.value, 'key').toString() },
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
