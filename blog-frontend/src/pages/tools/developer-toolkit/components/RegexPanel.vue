<template>
  <div class="regex-panel">
    <IOPanel title="正则表达式">
      <div class="regex-input">
        <div class="pattern-row">
          <el-input v-model="pattern" placeholder="输入正则表达式" size="small" @input="testRegex">
            <template #prepend>/</template>
            <template #append>/{{ flags.join('') }}</template>
          </el-input>
          <el-select v-model="flags" size="small" style="width: 120px" multiple @change="testRegex">
            <el-option label="g (全局)" value="g" />
            <el-option label="i (忽略大小写)" value="i" />
            <el-option label="m (多行)" value="m" />
            <el-option label="s (点号匹配换行)" value="s" />
          </el-select>
        </div>
        <div class="pattern-actions">
          <el-button size="small" text @click="clearPattern">清空</el-button>
        </div>
      </div>
    </IOPanel>

    <IOPanel title="常用正则">
      <div class="preset-list">
        <el-button v-for="preset in presets" :key="preset.name" size="small" @click="usePreset(preset)">
          {{ preset.name }}
        </el-button>
      </div>
    </IOPanel>

    <IOPanel title="测试文本" clearable @clear="testText = ''">
      <el-input
        v-model="testText"
        type="textarea"
        :rows="4"
        resize="none"
        placeholder="输入要测试的文本"
        @input="testRegex"
      />
    </IOPanel>

    <IOPanel v-if="error" title="错误">
      <el-alert :title="error" type="error" show-icon :closable="false" />
    </IOPanel>

    <IOPanel v-if="matches.length" title="匹配结果">
      <div class="match-results">
        <div class="match-count">
          找到 <strong>{{ matches.length }}</strong> 个匹配
          <span v-if="groupCount > 0">，{{ groupCount }} 个捕获组</span>
        </div>
        <div class="match-list">
          <div v-for="(match, index) in matches" :key="index" class="match-item">
            <span class="match-index">#{{ index + 1 }}</span>
            <div class="match-content">
              <code class="match-value">{{ match.value }}</code>
              <div v-if="match.groups.length" class="match-groups">
                <div v-for="(group, gIndex) in match.groups" :key="gIndex" class="group-item">
                  <span class="group-label">组{{ gIndex + 1 }}:</span>
                  <code class="group-value">{{ group || '(空)' }}</code>
                </div>
              </div>
            </div>
            <span class="match-position">{{ match.start }}-{{ match.end }}</span>
            <CopyButton :content="match.value" :show-label="false" text size="small" />
          </div>
        </div>
      </div>
    </IOPanel>

    <IOPanel title="替换">
      <div class="replace-section">
        <el-input v-model="replaceWith" placeholder="替换为（支持 $1, $2 引用捕获组）" size="small" @input="testRegex" />
        <div v-if="replaceText" class="replace-result">
          <div class="replace-header">
            <span>替换结果</span>
            <CopyButton :content="replaceText" :show-label="false" text size="small" />
          </div>
          <pre class="replace-output"><code>{{ replaceText }}</code></pre>
        </div>
      </div>
    </IOPanel>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import IOPanel from '@/components/tools/IOPanel.vue'
import CopyButton from '@/components/tools/CopyButton.vue'

interface Preset {
  name: string
  pattern: string
  flags: string[]
  description: string
}

const pattern = ref('')
const flags = ref(['g'])
const testText = ref('')
const replaceWith = ref('')
const error = ref('')
const matches = ref<Array<{ value: string; start: number; end: number; groups: string[] }>>([])
const replaceText = ref('')

const presets: Preset[] = [
  { name: '手机号', pattern: '1[3-9]\\d{9}', flags: ['g'], description: '中国大陆手机号' },
  { name: '邮箱', pattern: '[\\w.-]+@[\\w.-]+\\.\\w+', flags: ['g'], description: '电子邮箱地址' },
  { name: 'URL', pattern: 'https?://[\\w\\-._~:/?#\\[\\]@!$&\'()*+,;=]+', flags: ['g'], description: 'HTTP/HTTPS URL' },
  { name: 'IP地址', pattern: '\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b', flags: ['g'], description: 'IPv4 地址' },
  { name: '日期', pattern: '\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}', flags: ['g'], description: 'YYYY-MM-DD 格式' },
  { name: 'HTML标签', pattern: '<([^>]+)>.*?</\\1>', flags: ['g'], description: 'HTML 标签及内容' },
  { name: '中文字符', pattern: '[\\u4e00-\\u9fa5]+', flags: ['g'], description: '连续中文字符' },
  { name: '身份证号', pattern: '[1-9]\\d{5}(?:19|20)\\d{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]', flags: ['g'], description: '18位身份证号' },
]

const groupCount = computed(() => {
  if (!pattern.value) return 0
  try {
    const regex = new RegExp(pattern.value, '')
    const match = regex.exec('')
    return match ? match.length - 1 : 0
  } catch {
    return 0
  }
})

const usePreset = (preset: Preset) => {
  pattern.value = preset.pattern
  flags.value = [...preset.flags]
  testRegex()
}

const clearPattern = () => {
  pattern.value = ''
  matches.value = []
  replaceText.value = ''
  error.value = ''
}

const testRegex = () => {
  matches.value = []
  replaceText.value = ''
  error.value = ''

  if (!pattern.value || !testText.value) return

  try {
    const regex = new RegExp(pattern.value, flags.value.join(''))
    let match
    const tempMatches: Array<{ value: string; start: number; end: number; groups: string[] }> = []

    if (flags.value.includes('g')) {
      while ((match = regex.exec(testText.value)) !== null) {
        tempMatches.push({
          value: match[0],
          start: match.index,
          end: match.index + match[0].length,
          groups: Array.from(match).slice(1)
        })
        if (tempMatches.length > 100) break // 防止无限循环
      }
    } else {
      match = regex.exec(testText.value)
      if (match) {
        tempMatches.push({
          value: match[0],
          start: match.index,
          end: match.index + match[0].length,
          groups: Array.from(match).slice(1)
        })
      }
    }

    matches.value = tempMatches

    if (replaceWith.value) {
      replaceText.value = testText.value.replace(regex, replaceWith.value)
    }
  } catch (e: any) {
    error.value = e.message
  }
}
</script>

<style scoped>
.regex-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.regex-input {
  padding: 16px;
}
.pattern-row {
  display: flex;
  gap: 8px;
}
.pattern-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
.preset-list {
  padding: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.match-results {
  padding: 16px;
}
.match-count {
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.match-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.match-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
}
.match-index {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  min-width: 30px;
  padding-top: 2px;
}
.match-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.match-value {
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  word-break: break-all;
  color: var(--el-color-primary);
}
.match-groups {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-left: 12px;
  border-left: 2px solid var(--el-border-color);
}
.group-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.group-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  min-width: 40px;
}
.group-value {
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  color: var(--el-color-success);
}
.match-position {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  padding-top: 2px;
}
.replace-section {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.replace-result {
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
  overflow: hidden;
}
.replace-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.replace-output {
  margin: 0;
  padding: 12px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
