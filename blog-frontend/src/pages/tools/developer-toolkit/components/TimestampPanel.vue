<template>
  <div class="timestamp-panel">
    <IOPanel title="当前时间">
      <div class="current-time">
        <div class="time-display">
          <span class="time-label">Unix 时间戳</span>
          <code class="time-value">{{ currentTimestamp }}</code>
          <CopyButton :content="String(currentTimestamp)" :show-label="false" text size="small" />
        </div>
        <div class="time-display">
          <span class="time-label">ISO 8601</span>
          <code class="time-value">{{ currentISO }}</code>
          <CopyButton :content="currentISO" :show-label="false" text size="small" />
        </div>
        <div class="time-display">
          <span class="time-label">本地时间</span>
          <code class="time-value">{{ currentLocal }}</code>
          <CopyButton :content="currentLocal" :show-label="false" text size="small" />
        </div>
      </div>
    </IOPanel>

    <div class="converter-grid">
      <IOPanel title="时间戳 → 日期">
        <div class="converter-input">
          <el-input v-model="timestampInput" placeholder="输入时间戳（秒或毫秒）" size="small">
            <template #append>
              <el-button @click="convertTimestamp">转换</el-button>
            </template>
          </el-input>
          <div v-if="timestampResult" class="result">
            <code>{{ timestampResult }}</code>
            <CopyButton :content="timestampResult" :show-label="false" text size="small" />
          </div>
        </div>
      </IOPanel>

      <IOPanel title="日期 → 时间戳">
        <div class="converter-input">
          <el-date-picker
            v-model="dateInput"
            type="datetime"
            placeholder="选择日期时间"
            size="small"
            style="width: 100%"
            @change="convertDate"
          />
          <div v-if="dateResult" class="result">
            <code>{{ dateResult }}</code>
            <CopyButton :content="dateResult" :show-label="false" text size="small" />
          </div>
        </div>
      </IOPanel>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import IOPanel from '@/components/tools/IOPanel.vue'
import CopyButton from '@/components/tools/CopyButton.vue'

const currentTimestamp = ref(0)
const currentISO = ref('')
const currentLocal = ref('')
const timestampInput = ref('')
const timestampResult = ref('')
const dateInput = ref<Date | null>(null)
const dateResult = ref('')

let timer: ReturnType<typeof setInterval>

const updateCurrentTime = () => {
  const now = new Date()
  currentTimestamp.value = Math.floor(now.getTime() / 1000)
  currentISO.value = now.toISOString()
  currentLocal.value = now.toLocaleString('zh-CN')
}

onMounted(() => {
  updateCurrentTime()
  timer = setInterval(updateCurrentTime, 1000)
})

onUnmounted(() => {
  clearInterval(timer)
})

const convertTimestamp = () => {
  const ts = parseInt(timestampInput.value)
  if (isNaN(ts)) return
  const ms = ts.toString().length === 10 ? ts * 1000 : ts
  const date = new Date(ms)
  timestampResult.value = date.toLocaleString('zh-CN') + '\n' + date.toISOString()
}

const convertDate = () => {
  if (!dateInput.value) return
  dateResult.value = Math.floor(dateInput.value.getTime() / 1000).toString()
}
</script>

<style scoped>
.timestamp-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.current-time {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
}
.time-display {
  display: flex;
  align-items: center;
  gap: 12px;
}
.time-label {
  min-width: 80px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.time-value {
  flex: 1;
  font-family: 'JetBrains Mono', monospace;
  font-size: 14px;
}
.converter-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
@media (max-width: 1024px) {
  .converter-grid {
    grid-template-columns: 1fr;
  }
}
.converter-input {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.result {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
}
.result code {
  flex: 1;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  white-space: pre-wrap;
}
</style>
