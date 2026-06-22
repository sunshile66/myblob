<template>
  <div class="regex-panel">
    <IOPanel title="正则表达式">
      <div class="regex-input">
        <div class="pattern-row">
          <el-input v-model="pattern" placeholder="输入正则表达式" size="small" @input="testRegex">
            <template #prepend>/</template>
            <template #append>/{{ flags }}</template>
          </el-input>
          <el-select v-model="flags" size="small" style="width: 100px" multiple @change="testRegex">
            <el-option label="g" value="g" />
            <el-option label="i" value="i" />
            <el-option label="m" value="m" />
            <el-option label="s" value="s" />
          </el-select>
        </div>
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

    <IOPanel v-if="matches.length" title="匹配结果">
      <div class="match-results">
        <div class="match-count">
          找到 <strong>{{ matches.length }}</strong> 个匹配
        </div>
        <div class="match-list">
          <div v-for="(match, index) in matches" :key="index" class="match-item">
            <span class="match-index">#{{ index + 1 }}</span>
            <code class="match-value">{{ match.value }}</code>
            <span class="match-position">位置: {{ match.start }}-{{ match.end }}</span>
            <CopyButton :content="match.value" :show-label="false" text size="small" />
          </div>
        </div>
      </div>
    </IOPanel>

    <IOPanel v-if="replaceText" title="替换结果">
      <div class="replace-result">
        <el-input v-model="replaceWith" placeholder="替换为" size="small" @input="testRegex" />
        <pre class="replace-output"><code>{{ replaceText }}</code></pre>
      </div>
    </IOPanel>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import IOPanel from '@/components/tools/IOPanel.vue'
import CopyButton from '@/components/tools/CopyButton.vue'

const pattern = ref('')
const flags = ref(['g'])
const testText = ref('')
const replaceWith = ref('')
const matches = ref<Array<{ value: string; start: number; end: number }>>([])
const replaceText = ref('')

const testRegex = () => {
  matches.value = []
  replaceText.value = ''
  if (!pattern.value || !testText.value) return

  try {
    const regex = new RegExp(pattern.value, flags.value.join(''))
    let match
    const tempMatches: Array<{ value: string; start: number; end: number }> = []

    if (flags.value.includes('g')) {
      while ((match = regex.exec(testText.value)) !== null) {
        tempMatches.push({
          value: match[0],
          start: match.index,
          end: match.index + match[0].length
        })
      }
    } else {
      match = regex.exec(testText.value)
      if (match) {
        tempMatches.push({
          value: match[0],
          start: match.index,
          end: match.index + match[0].length
        })
      }
    }

    matches.value = tempMatches

    if (replaceWith.value) {
      replaceText.value = testText.value.replace(regex, replaceWith.value)
    }
  } catch {
    // Invalid regex
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
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
}
.match-index {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  min-width: 30px;
}
.match-value {
  flex: 1;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  word-break: break-all;
}
.match-position {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.replace-result {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.replace-output {
  margin: 0;
  padding: 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
