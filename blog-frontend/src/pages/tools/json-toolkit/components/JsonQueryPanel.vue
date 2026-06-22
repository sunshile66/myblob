<template>
  <div class="json-query-panel">
    <IOPanel title="输入 JSON" clearable @clear="inputJson = ''">
      <el-input
        v-model="inputJson"
        type="textarea"
        :rows="8"
        resize="none"
        placeholder='{"user":{"name":"test","age":25}}'
      />
    </IOPanel>
    <div class="query-input">
      <el-input
        v-model="queryPath"
        placeholder="查询路径，例如 user.name 或 items[0].id"
        size="small"
      >
        <template #append>
          <el-button @click="query">查询</el-button>
        </template>
      </el-input>
    </div>
    <IOPanel v-if="queryResult !== undefined" title="查询结果" :content="queryResult" copyable>
      <pre class="query-output"><code>{{ queryResult }}</code></pre>
    </IOPanel>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import IOPanel from '@/components/tools/IOPanel.vue'

const inputJson = ref('')
const queryPath = ref('')
const queryResult = ref<string | undefined>(undefined)

const query = () => {
  if (!inputJson.value.trim()) {
    ElMessage.warning('请输入 JSON')
    return
  }
  if (!queryPath.value.trim()) {
    ElMessage.warning('请输入查询路径')
    return
  }
  try {
    const data = JSON.parse(inputJson.value)
    const result = getNestedValue(data, queryPath.value)
    queryResult.value = result !== undefined ? JSON.stringify(result, null, 2) : '路径不存在'
  } catch (e: any) {
    ElMessage.error('JSON 解析错误: ' + e.message)
  }
}

const getNestedValue = (obj: any, path: string): any => {
  const keys = path.replace(/\[(\d+)\]/g, '.$1').split('.')
  let current = obj
  for (const key of keys) {
    if (current === null || current === undefined) return undefined
    current = current[key]
  }
  return current
}
</script>

<style scoped>
.json-query-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.query-input {
  max-width: 500px;
  margin: 0 auto;
  width: 100%;
}
.query-output {
  margin: 0;
  padding: 16px;
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
}
</style>
