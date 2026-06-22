<template>
  <div class="password-panel">
    <ActionToolbar>
      <template #center>
        <span>长度:</span>
        <el-input-number v-model="length" :min="4" :max="128" size="small" style="width: 100px" />
        <el-checkbox v-model="includeUppercase" size="small">大写</el-checkbox>
        <el-checkbox v-model="includeLowercase" size="small">小写</el-checkbox>
        <el-checkbox v-model="includeNumbers" size="small">数字</el-checkbox>
        <el-checkbox v-model="includeSymbols" size="small">符号</el-checkbox>
        <el-button type="primary" size="small" @click="generate">生成</el-button>
      </template>
    </ActionToolbar>

    <IOPanel v-if="password" title="生成结果" :content="password" copyable>
      <div class="password-display">
        <code class="password-text">{{ password }}</code>
        <div class="password-strength">
          <span>强度:</span>
          <el-progress :percentage="strength" :color="strengthColor" :show-text="false" style="width: 100px" />
          <span :style="{ color: strengthColor }">{{ strengthLabel }}</span>
        </div>
      </div>
    </IOPanel>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import IOPanel from '@/components/tools/IOPanel.vue'
import ActionToolbar from '@/components/tools/ActionToolbar.vue'

const length = ref(16)
const includeUppercase = ref(true)
const includeLowercase = ref(true)
const includeNumbers = ref(true)
const includeSymbols = ref(true)
const password = ref('')

const strength = computed(() => {
  if (!password.value) return 0
  let score = 0
  if (password.value.length >= 12) score += 25
  if (password.value.length >= 16) score += 25
  if (/[A-Z]/.test(password.value)) score += 15
  if (/[a-z]/.test(password.value)) score += 15
  if (/[0-9]/.test(password.value)) score += 10
  if (/[^A-Za-z0-9]/.test(password.value)) score += 10
  return Math.min(100, score)
})

const strengthColor = computed(() => {
  if (strength.value < 30) return '#f56c6c'
  if (strength.value < 60) return '#e6a23c'
  if (strength.value < 80) return '#409eff'
  return '#67c23a'
})

const strengthLabel = computed(() => {
  if (strength.value < 30) return '弱'
  if (strength.value < 60) return '中'
  if (strength.value < 80) return '强'
  return '非常强'
})

const generate = () => {
  let chars = ''
  if (includeUppercase.value) chars += 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  if (includeLowercase.value) chars += 'abcdefghijklmnopqrstuvwxyz'
  if (includeNumbers.value) chars += '0123456789'
  if (includeSymbols.value) chars += '!@#$%^&*()_+-=[]{}|;:,.<>?'
  if (!chars) {
    chars = 'abcdefghijklmnopqrstuvwxyz'
  }
  password.value = Array.from({ length: length.value }, () =>
    chars[Math.floor(Math.random() * chars.length)]
  ).join('')
}
</script>

<style scoped>
.password-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.password-display {
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}
.password-text {
  font-family: 'JetBrains Mono', monospace;
  font-size: 18px;
  letter-spacing: 2px;
  word-break: break-all;
  text-align: center;
}
.password-strength {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
}
</style>
