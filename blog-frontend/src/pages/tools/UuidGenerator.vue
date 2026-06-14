<template>
  <ToolPageShell
    title="UUID 生成器"
    description="在线生成 UUID v1/v4/NIL，支持批量生成和多种格式导出。"
    eyebrow="开发调试"
    :meta="meta"
  >
    <div class="uuid-workspace">
      <section class="panel settings-panel">
        <h2>生成选项</h2>
        <div class="settings-row">
          <div class="setting-group">
            <label>UUID 版本</label>
            <el-select v-model="version" @change="generateUuids">
              <el-option label="v4 (随机)" value="v4" />
              <el-option label="v1 (时间戳)" value="v1" />
              <el-option label="NIL (全零)" value="nil" />
            </el-select>
          </div>
          <div class="setting-group">
            <label>批量数量</label>
            <el-input-number v-model="count" :min="1" :max="50" @change="generateUuids" />
          </div>
          <div class="setting-group">
            <label>输出格式</label>
            <el-select v-model="format" @change="generateUuids">
              <el-option label="标准 (带连字符)" value="standard" />
              <el-option label="紧凑 (无连字符)" value="compact" />
              <el-option label="大写" value="upper" />
              <el-option label="大写紧凑" value="upperCompact" />
            </el-select>
          </div>
          <el-button type="primary" @click="generateUuids" class="generate-btn">
            重新生成
          </el-button>
        </div>
      </section>

      <section class="panel result-panel">
        <div class="result-head">
          <div>
            <h2>生成结果</h2>
            <p>共 {{ uuids.length }} 个 UUID</p>
          </div>
          <div class="result-actions">
            <el-button @click="copyAll" :disabled="uuids.length === 0">复制全部</el-button>
            <el-button @click="generateUuids">刷新</el-button>
          </div>
        </div>
        <div v-if="uuids.length" class="uuid-list">
          <div v-for="(uuid, index) in uuids" :key="index" class="uuid-item">
            <span class="uuid-index">{{ index + 1 }}</span>
            <code class="uuid-text">{{ uuid }}</code>
            <el-button size="small" text @click="copyOne(uuid)">
              <el-icon><DocumentCopy /></el-icon>
            </el-button>
          </div>
        </div>
        <el-empty v-else description="点击生成按钮" :image-size="48" />
      </section>

      <section v-if="history.length" class="panel history-panel">
        <div class="result-head">
          <h2>历史记录</h2>
          <el-button text @click="history = []">清空</el-button>
        </div>
        <div class="uuid-list history-list">
          <div v-for="(item, index) in history" :key="index" class="uuid-item">
            <code class="uuid-text">{{ item }}</code>
            <el-button size="small" text @click="copyOne(item)">
              <el-icon><DocumentCopy /></el-icon>
            </el-button>
          </div>
        </div>
      </section>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentCopy } from '@element-plus/icons-vue'
import ToolPageShell from "@/components/tools/ToolPageShell.vue"

type UuidVersion = 'v4' | 'v1' | 'nil'
type UuidFormat = 'standard' | 'compact' | 'upper' | 'upperCompact'

const count = ref(5)
const version = ref<UuidVersion>('v4')
const format = ref<UuidFormat>('standard')
const uuids = ref<string[]>([])
const history = ref<string[]>([])

const meta = computed(() => [
  { label: '版本', value: version.value.toUpperCase() },
  { label: '格式', value: format.value === 'standard' ? '标准' : format.value === 'compact' ? '紧凑' : format.value === 'upper' ? '大写' : '大写紧凑' },
  { label: '数量', value: `${uuids.value.length}` },
])

const generateV4 = (): string => {
  const hex = () => Math.floor(Math.random() * 16).toString(16)
  return `${hex()}${hex()}${hex()}${hex()}${hex()}${hex()}${hex()}${hex()}-${hex()}${hex()}${hex()}${hex()}-4${hex()}${hex()}${hex()}-${[8, 9, 'a', 'b'][Math.floor(Math.random() * 4)]}${hex()}${hex()}${hex()}-${hex()}${hex()}${hex()}${hex()}${hex()}${hex()}${hex()}${hex()}${hex()}${hex()}${hex()}${hex()}`
}

const generateV1 = (): string => {
  const now = Date.now()
  const t = Math.floor(now / 1000) * 10000000 + 122192928000000000
  const hex = (n: number, len: number) => n.toString(16).padStart(len, '0')
  return `${hex(t, 8)}-${hex(t >> 32, 4)}-1${hex(Math.floor(Math.random() * 4096), 3)}-${hex(8 + Math.floor(Math.random() * 4), 1)}${hex(Math.floor(Math.random() * 4096), 3)}-${hex(Math.floor(Math.random() * 0x100000000000), 12)}`
}

const generateNil = (): string => '00000000-0000-0000-0000-000000000000'

const generateRawUuid = (): string => {
  switch (version.value) {
    case 'v1': return generateV1()
    case 'nil': return generateNil()
    default: return generateV4()
  }
}

const formatUuid = (uuid: string): string => {
  switch (format.value) {
    case 'compact': return uuid.replace(/-/g, '')
    case 'upper': return uuid.toUpperCase()
    case 'upperCompact': return uuid.replace(/-/g, '').toUpperCase()
    default: return uuid
  }
}

const generateUuids = () => {
  const rawList: string[] = []
  for (let i = 0; i < count.value; i++) {
    rawList.push(generateRawUuid())
  }
  uuids.value = rawList.map(formatUuid)
  history.value = [...rawList.map(formatUuid), ...history.value].slice(0, 20)
  ElMessage.success(`生成了 ${count.value} 个 UUID`)
}

const copyOne = async (uuid: string) => {
  await navigator.clipboard.writeText(uuid)
  ElMessage.success('已复制')
}

const copyAll = async () => {
  await navigator.clipboard.writeText(uuids.value.join('\n'))
  ElMessage.success('已全部复制')
}

onMounted(() => generateUuids())
</script>

<style scoped>
.uuid-generator {
  background: var(--theme-background);
  min-height: calc(100vh - 80px);
  padding: 24px 0;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
}

.back-btn {
  padding: 0;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 800;
  color: var(--theme-text);
  margin: 0;
}

.content {
  background: var(--theme-card);
  border-radius: var(--radius-xl);
  padding: 32px;
}

.options {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 32px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.result-header h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0;
}

.uuid-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.uuid-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--theme-background);
  border-radius: var(--radius-md);
  padding: 12px 16px;
  font-family: 'Fira Code', monospace;
  font-size: 14px;
}

.uuid-text {
  color: var(--theme-text);
  word-break: break-all;
}

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
