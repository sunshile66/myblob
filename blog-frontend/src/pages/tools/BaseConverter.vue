<template>
  <ToolPageShell
    title="进制转换"
    description="实时转换十六进制、十进制、八进制、二进制，支持复制和格式化显示。"
    eyebrow="开发调试"
    :meta="meta"
  >
    <div class="workspace">
      <section class="panel input-panel">
        <div class="panel-head">
          <h2>输入</h2>
          <el-button text @click="clearInput">清空</el-button>
        </div>
        <div class="input-row">
          <el-input v-model="inputValue" placeholder="输入数值，自动识别或手动选择进制" size="large" class="main-input" @input="autoConvert" />
          <el-select v-model="fromBase" size="large" @change="autoConvert">
            <el-option label="二进制 (2)" :value="2" />
            <el-option label="八进制 (8)" :value="8" />
            <el-option label="十进制 (10)" :value="10" />
            <el-option label="十六进制 (16)" :value="16" />
          </el-select>
        </div>
        <div class="bit-controls">
          <label>
            位宽:
            <el-input-number v-model="bitWidth" :min="0" :max="64" size="small" @change="autoConvert" />
            <span class="bit-hint">(0=不填充)</span>
          </label>
        </div>
      </section>

      <section class="panel result-panel">
        <div class="panel-head">
          <div>
            <h2>转换结果</h2>
            <p>{{ decimalValue !== null ? `十进制值: ${decimalValue}` : '等待输入' }}</p>
          </div>
        </div>

        <div v-for="item in results" :key="item.name" class="result-row">
          <div class="result-label">
            <span>{{ item.label }}</span>
            <code class="result-base">{{ item.base }}</code>
          </div>
          <code class="result-value">{{ item.value || '-' }}</code>
          <el-button size="small" text @click="copyOne(item.value)" :disabled="!item.value">
            <el-icon><DocumentCopy /></el-icon>
          </el-button>
        </div>

        <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon :closable="false" />
      </section>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentCopy } from '@element-plus/icons-vue'
import ToolPageShell from "@/components/tools/ToolPageShell.vue"

const inputValue = ref('')
const fromBase = ref(10)
const bitWidth = ref(0)
const errorMsg = ref('')

const decimalValue = computed<number | null>(() => {
  if (!inputValue.value.trim()) return null
  try {
    const val = parseInt(inputValue.value.trim(), fromBase.value)
    if (isNaN(val)) { errorMsg.value = '无效的数值'; return null }
    errorMsg.value = ''
    return val
  } catch { errorMsg.value = '解析失败'; return null }
})

const padValue = (val: string) => {
  if (bitWidth.value > 0 && val.length < bitWidth.value) {
    return val.padStart(bitWidth.value, '0')
  }
  return val
}

const results = computed(() => {
  const val = decimalValue.value
  if (val === null) return [
    { label: '十六进制', base: 'HEX', value: '' },
    { label: '十进制',   base: 'DEC', value: '' },
    { label: '八进制',   base: 'OCT', value: '' },
    { label: '二进制',   base: 'BIN', value: '' },
  ]
  try {
    return [
      { label: '十六进制', base: 'HEX', value: padValue(val.toString(16).toUpperCase()) },
      { label: '十进制',   base: 'DEC', value: String(val) },
      { label: '八进制',   base: 'OCT', value: padValue(val.toString(8)) },
      { label: '二进制',   base: 'BIN', value: padValue(val.toString(2)) },
    ]
  } catch {
    return [
      { label: '十六进制', base: 'HEX', value: '' },
      { label: '十进制',   base: 'DEC', value: '' },
      { label: '八进制',   base: 'OCT', value: '' },
      { label: '二进制',   base: 'BIN', value: '' },
    ]
  }
})

const meta = computed(() => [
  { label: '原进制', value: `base-${fromBase.value}` },
  { label: '位宽', value: bitWidth.value > 0 ? `${bitWidth.value}bit` : '自动' },
  { label: '值有效', value: decimalValue.value !== null ? '✓' : '✗' },
])

const autoConvert = () => { /* computed 自动触发 */ }
const clearInput = () => { inputValue.value = ''; errorMsg.value = '' }

const copyOne = async (val: string) => {
  if (!val) return
  await navigator.clipboard.writeText(val)
  ElMessage.success('已复制')
}
</script>

<style scoped>
.workspace { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); gap: 16px; }

.panel { padding: 20px; border: 1px solid var(--theme-border); border-radius: var(--radius-lg); background: var(--theme-card); box-shadow: var(--shadow-xs); }
.panel-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; margin-bottom: 18px; }
.panel-head h2 { margin: 0; font-size: 18px; font-weight: 600; color: var(--theme-text); }
.panel-head p { margin: 4px 0 0; font-size: 12px; color: var(--theme-text-secondary); }

.input-row { display: grid; grid-template-columns: 1fr 180px; gap: 12px; margin-bottom: 16px; }
.bit-controls { display: flex; align-items: center; gap: 12px; font-size: 13px; color: var(--theme-text-secondary); }
.bit-hint { font-size: 11px; opacity: 0.7; margin-left: 4px; }

.result-row { display: grid; grid-template-columns: 180px 1fr 40px; align-items: center; gap: 12px; padding: 12px 16px; margin-bottom: 8px; background: var(--theme-background); border-radius: var(--radius-md); border: 1px solid var(--theme-border); }
.result-label { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: var(--theme-text); }
.result-base { font-size: 10px; padding: 2px 6px; border-radius: 4px; background: var(--theme-primary-light); color: var(--theme-primary); font-family: var(--font-mono); }
.result-value { font-family: var(--font-mono); font-size: 15px; color: var(--theme-text); word-break: break-all; letter-spacing: 0.04em; }

@media (max-width: 768px) {
  .workspace { grid-template-columns: 1fr; }
  .input-row { grid-template-columns: 1fr; }
  .result-row { grid-template-columns: 120px 1fr 40px; }
}
</style>
