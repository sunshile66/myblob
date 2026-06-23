<template>
  <div class="password-panel">
    <ActionToolbar>
      <template #left>
        <span>长度:</span>
        <el-input-number v-model="length" :min="4" :max="128" size="small" style="width: 100px" />
      </template>
      <template #center>
        <el-checkbox v-model="includeUppercase" size="small">大写 A-Z</el-checkbox>
        <el-checkbox v-model="includeLowercase" size="small">小写 a-z</el-checkbox>
        <el-checkbox v-model="includeNumbers" size="small">数字 0-9</el-checkbox>
        <el-checkbox v-model="includeSymbols" size="small">符号 !@#$</el-checkbox>
      </template>
      <template #right>
        <el-button type="primary" size="small" @click="generate">生成</el-button>
        <el-button size="small" @click="generateMultiple">批量生成</el-button>
      </template>
    </ActionToolbar>

    <IOPanel v-if="password" title="生成结果" :content="password" copyable>
      <div class="password-display">
        <code class="password-text">{{ password }}</code>
        <div class="password-strength">
          <div class="strength-bar">
            <div class="strength-fill" :style="{ width: strength + '%', background: strengthColor }"></div>
          </div>
          <span :style="{ color: strengthColor }">{{ strengthLabel }}</span>
        </div>
        <div class="password-entropy">
          <span>熵值: {{ entropy }} bits</span>
          <span>破解时间: {{ crackTime }}</span>
        </div>
      </div>
    </IOPanel>

    <IOPanel v-if="multiplePasswords.length" title="批量生成" :content="multiplePasswords.join('\n')" copyable>
      <div class="multiple-list">
        <div v-for="(pw, index) in multiplePasswords" :key="index" class="multiple-item">
          <code>{{ pw }}</code>
          <CopyButton :content="pw" :show-label="false" text size="small" />
        </div>
      </div>
    </IOPanel>

    <IOPanel title="密码强度说明">
      <div class="strength-guide">
        <div class="guide-item">
          <span class="guide-color" style="background: #f56c6c"></span>
          <span class="guide-label">弱</span>
          <span class="guide-desc">少于 8 位，仅包含一种字符类型</span>
        </div>
        <div class="guide-item">
          <span class="guide-color" style="background: #e6a23c"></span>
          <span class="guide-label">中</span>
          <span class="guide-desc">8-12 位，包含两种字符类型</span>
        </div>
        <div class="guide-item">
          <span class="guide-color" style="background: #409eff"></span>
          <span class="guide-label">强</span>
          <span class="guide-desc">12-16 位，包含三种字符类型</span>
        </div>
        <div class="guide-item">
          <span class="guide-color" style="background: #67c23a"></span>
          <span class="guide-label">非常强</span>
          <span class="guide-desc">16 位以上，包含全部字符类型</span>
        </div>
      </div>
    </IOPanel>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import IOPanel from '@/components/tools/IOPanel.vue'
import ActionToolbar from '@/components/tools/ActionToolbar.vue'
import CopyButton from '@/components/tools/CopyButton.vue'

const length = ref(16)
const includeUppercase = ref(true)
const includeLowercase = ref(true)
const includeNumbers = ref(true)
const includeSymbols = ref(true)
const password = ref('')
const multiplePasswords = ref<string[]>([])

const charset = computed(() => {
  let chars = ''
  if (includeUppercase.value) chars += 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  if (includeLowercase.value) chars += 'abcdefghijklmnopqrstuvwxyz'
  if (includeNumbers.value) chars += '0123456789'
  if (includeSymbols.value) chars += '!@#$%^&*()_+-=[]{}|;:,.<>?'
  return chars || 'abcdefghijklmnopqrstuvwxyz'
})

const entropy = computed(() => {
  if (!password.value) return 0
  const charsetSize = charset.value.length
  return Math.round(password.value.length * Math.log2(charsetSize))
})

const crackTime = computed(() => {
  if (entropy.value === 0) return '-'
  const guessesPerSecond = 1e10 // 100亿次/秒
  const seconds = Math.pow(2, entropy.value) / guessesPerSecond
  if (seconds < 1) return '瞬间'
  if (seconds < 60) return `${Math.round(seconds)} 秒`
  if (seconds < 3600) return `${Math.round(seconds / 60)} 分钟`
  if (seconds < 86400) return `${Math.round(seconds / 3600)} 小时`
  if (seconds < 86400 * 365) return `${Math.round(seconds / 86400)} 天`
  if (seconds < 86400 * 365 * 1000) return `${Math.round(seconds / (86400 * 365))} 年`
  return '数百万年以上'
})

const strength = computed(() => {
  if (!password.value) return 0
  let score = 0
  if (password.value.length >= 8) score += 20
  if (password.value.length >= 12) score += 15
  if (password.value.length >= 16) score += 15
  if (/[A-Z]/.test(password.value)) score += 15
  if (/[a-z]/.test(password.value)) score += 10
  if (/[0-9]/.test(password.value)) score += 10
  if (/[^A-Za-z0-9]/.test(password.value)) score += 15
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

const generatePassword = (): string => {
  return Array.from({ length: length.value }, () =>
    charset.value[Math.floor(Math.random() * charset.value.length)]
  ).join('')
}

const generate = () => {
  password.value = generatePassword()
  multiplePasswords.value = []
}

const generateMultiple = () => {
  password.value = generatePassword()
  multiplePasswords.value = Array.from({ length: 10 }, () => generatePassword())
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
  font-size: 20px;
  letter-spacing: 2px;
  word-break: break-all;
  text-align: center;
  padding: 16px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
  width: 100%;
}
.password-strength {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  max-width: 300px;
}
.strength-bar {
  flex: 1;
  height: 8px;
  background: var(--el-fill-color);
  border-radius: 4px;
  overflow: hidden;
}
.strength-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s, background 0.3s;
}
.password-entropy {
  display: flex;
  gap: 24px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.multiple-list {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.multiple-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
}
.multiple-item code {
  flex: 1;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
}
.strength-guide {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.guide-item {
  display: flex;
  align-items: center;
  gap: 12px;
}
.guide-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
}
.guide-label {
  font-weight: 600;
  min-width: 60px;
}
.guide-desc {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
