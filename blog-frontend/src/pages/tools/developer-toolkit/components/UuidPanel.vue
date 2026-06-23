<template>
  <div class="uuid-panel">
    <ActionToolbar>
      <template #left>
        <span>数量:</span>
        <el-input-number v-model="count" :min="1" :max="100" size="small" style="width: 100px" />
      </template>
      <template #center>
        <el-select v-model="version" size="small" style="width: 120px">
          <el-option label="UUID v4" value="v4" />
          <el-option label="Nano ID" value="nano" />
          <el-option label="短 ID" value="short" />
        </el-select>
        <el-checkbox v-model="uppercase" size="small">大写</el-checkbox>
        <el-checkbox v-model="noHyphens" size="small">无连字符</el-checkbox>
      </template>
      <template #right>
        <el-button type="primary" size="small" @click="generate">生成</el-button>
      </template>
    </ActionToolbar>

    <IOPanel v-if="uuids.length" title="生成结果" :content="uuids.join('\n')" copyable>
      <div class="uuid-list">
        <div v-for="(uuid, index) in uuids" :key="index" class="uuid-item">
          <span class="uuid-index">{{ index + 1 }}.</span>
          <code class="uuid-value">{{ uuid }}</code>
          <CopyButton :content="uuid" :show-label="false" text size="small" />
        </div>
      </div>
      <template #footer>
        <div class="uuid-stats">
          <span>格式: {{ version.toUpperCase() }}</span>
          <span>长度: {{ uuids[0]?.length || 0 }} 字符</span>
          <span>数量: {{ uuids.length }}</span>
        </div>
      </template>
    </IOPanel>

    <IOPanel title="UUID 说明">
      <div class="uuid-info">
        <div class="info-item">
          <strong>UUID v4</strong>
          <p>随机生成的 128 位标识符，格式为 8-4-4-4-12，碰撞概率极低（约 2^122 分之一）</p>
        </div>
        <div class="info-item">
          <strong>Nano ID</strong>
          <p>更短的唯一标识符，默认 21 字符，使用 URL 安全字符，比 UUID 更紧凑</p>
        </div>
        <div class="info-item">
          <strong>短 ID</strong>
          <p>8 字符的短标识符，适合临时场景，碰撞概率相对较高</p>
        </div>
      </div>
    </IOPanel>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import IOPanel from '@/components/tools/IOPanel.vue'
import ActionToolbar from '@/components/tools/ActionToolbar.vue'
import CopyButton from '@/components/tools/CopyButton.vue'

const count = ref(5)
const version = ref('v4')
const uppercase = ref(false)
const noHyphens = ref(false)
const uuids = ref<string[]>([])

const generateUuidV4 = (): string => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

const generateNanoId = (): string => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-'
  return Array.from({ length: 21 }, () => chars[Math.floor(Math.random() * chars.length)]).join('')
}

const generateShortId = (): string => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  return Array.from({ length: 8 }, () => chars[Math.floor(Math.random() * chars.length)]).join('')
}

const generate = () => {
  const generator = version.value === 'v4' ? generateUuidV4
    : version.value === 'nano' ? generateNanoId
    : generateShortId

  uuids.value = Array.from({ length: count.value }, () => {
    let id = generator()
    if (uppercase.value) id = id.toUpperCase()
    if (noHyphens.value && version.value === 'v4') id = id.replace(/-/g, '')
    return id
  })
}
</script>

<style scoped>
.uuid-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.uuid-list {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.uuid-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
}
.uuid-index {
  min-width: 30px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.uuid-value {
  flex: 1;
  font-family: 'JetBrains Mono', monospace;
  font-size: 14px;
  letter-spacing: 0.5px;
}
.uuid-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.uuid-info {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.info-item strong {
  display: block;
  margin-bottom: 4px;
  color: var(--el-text-color-primary);
}
.info-item p {
  margin: 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}
</style>
