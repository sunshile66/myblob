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
          <span class="time-label">毫秒时间戳</span>
          <code class="time-value">{{ currentTimestampMs }}</code>
          <CopyButton :content="String(currentTimestampMs)" :show-label="false" text size="small" />
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
        <div class="time-display">
          <span class="time-label">UTC 时间</span>
          <code class="time-value">{{ currentUTC }}</code>
          <CopyButton :content="currentUTC" :show-label="false" text size="small" />
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
            <div class="result-item">
              <span class="result-label">本地时间:</span>
              <code>{{ timestampResult.local }}</code>
            </div>
            <div class="result-item">
              <span class="result-label">ISO 8601:</span>
              <code>{{ timestampResult.iso }}</code>
            </div>
            <div class="result-item">
              <span class="result-label">相对时间:</span>
              <code>{{ timestampResult.relative }}</code>
            </div>
            <CopyButton :content="timestampResult.local" :show-label="false" text size="small" />
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
            <div class="result-item">
              <span class="result-label">秒级时间戳:</span>
              <code>{{ dateResult.seconds }}</code>
              <CopyButton :content="dateResult.seconds" :show-label="false" text size="small" />
            </div>
            <div class="result-item">
              <span class="result-label">毫秒时间戳:</span>
              <code>{{ dateResult.milliseconds }}</code>
              <CopyButton :content="dateResult.milliseconds" :show-label="false" text size="small" />
            </div>
          </div>
        </div>
      </IOPanel>
    </div>

    <IOPanel title="时区转换">
      <div class="timezone-grid">
        <div v-for="tz in timezones" :key="tz.name" class="timezone-item">
          <span class="tz-name">{{ tz.name }}</span>
          <code class="tz-time">{{ tz.time }}</code>
        </div>
      </div>
    </IOPanel>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import IOPanel from '@/components/tools/IOPanel.vue'
import CopyButton from '@/components/tools/CopyButton.vue'

const currentTimestamp = ref(0)
const currentTimestampMs = ref(0)
const currentISO = ref('')
const currentLocal = ref('')
const currentUTC = ref('')
const timestampInput = ref('')
const timestampResult = ref<{ local: string; iso: string; relative: string } | null>(null)
const dateInput = ref<Date | null>(null)
const dateResult = ref<{ seconds: string; milliseconds: string } | null>(null)

let timer: ReturnType<typeof setInterval>

const updateCurrentTime = () => {
  const now = new Date()
  currentTimestamp.value = Math.floor(now.getTime() / 1000)
  currentTimestampMs.value = now.getTime()
  currentISO.value = now.toISOString()
  currentLocal.value = now.toLocaleString('zh-CN')
  currentUTC.value = now.toUTCString()
}

onMounted(() => {
  updateCurrentTime()
  timer = setInterval(updateCurrentTime, 1000)
})

onUnmounted(() => {
  clearInterval(timer)
})

const getRelativeTime = (date: Date): string => {
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (seconds < 0) return '未来'
  if (seconds < 60) return `${seconds} 秒前`
  if (minutes < 60) return `${minutes} 分钟前`
  if (hours < 24) return `${hours} 小时前`
  if (days < 30) return `${days} 天前`
  return `${Math.floor(days / 30)} 个月前`
}

const convertTimestamp = () => {
  const ts = parseInt(timestampInput.value)
  if (isNaN(ts)) return
  const ms = ts.toString().length === 10 ? ts * 1000 : ts
  const date = new Date(ms)
  timestampResult.value = {
    local: date.toLocaleString('zh-CN'),
    iso: date.toISOString(),
    relative: getRelativeTime(date)
  }
}

const convertDate = () => {
  if (!dateInput.value) return
  const ms = dateInput.value.getTime()
  dateResult.value = {
    seconds: Math.floor(ms / 1000).toString(),
    milliseconds: ms.toString()
  }
}

const timezones = computed(() => {
  const now = new Date()
  return [
    { name: '北京 (UTC+8)', time: now.toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai' }) },
    { name: '东京 (UTC+9)', time: now.toLocaleString('zh-CN', { timeZone: 'Asia/Tokyo' }) },
    { name: '纽约 (UTC-5)', time: now.toLocaleString('zh-CN', { timeZone: 'America/New_York' }) },
    { name: '伦敦 (UTC+0)', time: now.toLocaleString('zh-CN', { timeZone: 'Europe/London' }) },
    { name: '巴黎 (UTC+1)', time: now.toLocaleString('zh-CN', { timeZone: 'Europe/Paris' }) },
    { name: '悉尼 (UTC+11)', time: now.toLocaleString('zh-CN', { timeZone: 'Australia/Sydney' }) },
  ]
})
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
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
}
.result-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.result-label {
  min-width: 80px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.result-item code {
  flex: 1;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
}
.timezone-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 12px;
  padding: 16px;
}
.timezone-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
}
.tz-name {
  min-width: 100px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.tz-time {
  flex: 1;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
}
</style>
