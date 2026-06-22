<template>
  <div class="base-converter-panel">
    <div class="converter-grid">
      <IOPanel title="十进制">
        <el-input v-model="decimal" placeholder="十进制数" size="small" @input="convert('decimal')" />
      </IOPanel>
      <IOPanel title="二进制">
        <el-input v-model="binary" placeholder="二进制数" size="small" @input="convert('binary')" />
      </IOPanel>
      <IOPanel title="八进制">
        <el-input v-model="octal" placeholder="八进制数" size="small" @input="convert('octal')" />
      </IOPanel>
      <IOPanel title="十六进制">
        <el-input v-model="hex" placeholder="十六进制数" size="small" @input="convert('hex')" />
      </IOPanel>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import IOPanel from '@/components/tools/IOPanel.vue'

const decimal = ref('')
const binary = ref('')
const octal = ref('')
const hex = ref('')

const convert = (source: string) => {
  let value = 0
  try {
    switch (source) {
      case 'decimal':
        value = parseInt(decimal.value)
        break
      case 'binary':
        value = parseInt(binary.value, 2)
        break
      case 'octal':
        value = parseInt(octal.value, 8)
        break
      case 'hex':
        value = parseInt(hex.value, 16)
        break
    }
    if (isNaN(value)) return
    if (source !== 'decimal') decimal.value = value.toString()
    if (source !== 'binary') binary.value = value.toString(2)
    if (source !== 'octal') octal.value = value.toString(8)
    if (source !== 'hex') hex.value = value.toString(16).toUpperCase()
  } catch {
    // Invalid input
  }
}
</script>

<style scoped>
.base-converter-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.converter-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
@media (max-width: 768px) {
  .converter-grid {
    grid-template-columns: 1fr;
  }
}
</style>
