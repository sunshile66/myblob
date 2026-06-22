<template>
  <div class="json-sql-panel">
    <IOPanel title="输入 JSON 数组" clearable @clear="inputJson = ''">
      <el-input
        v-model="inputJson"
        type="textarea"
        :rows="8"
        resize="none"
        placeholder='[{"name":"test","value":1}]'
      />
    </IOPanel>
    <div class="sql-options">
      <el-input v-model="tableName" placeholder="表名" size="small" style="width: 200px" />
      <el-select v-model="sqlType" size="small" style="width: 150px">
        <el-option label="INSERT" value="insert" />
        <el-option label="UPDATE" value="update" />
        <el-option label="CREATE TABLE" value="create" />
      </el-select>
      <el-button type="primary" size="small" @click="generateSql">生成 SQL</el-button>
    </div>
    <IOPanel v-if="sqlOutput" title="SQL 输出" :content="sqlOutput" copyable>
      <pre class="sql-output"><code>{{ sqlOutput }}</code></pre>
    </IOPanel>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import IOPanel from '@/components/tools/IOPanel.vue'

const inputJson = ref('')
const tableName = ref('my_table')
const sqlType = ref('insert')
const sqlOutput = ref('')

const generateSql = () => {
  if (!inputJson.value.trim()) {
    ElMessage.warning('请输入 JSON')
    return
  }
  try {
    const data = JSON.parse(inputJson.value)
    if (!Array.isArray(data) || data.length === 0) {
      ElMessage.warning('请输入 JSON 数组')
      return
    }
    const columns = Object.keys(data[0])
    switch (sqlType.value) {
      case 'insert':
        sqlOutput.value = data.map(row => {
          const values = columns.map(col => formatValue(row[col])).join(', ')
          return `INSERT INTO ${tableName.value} (${columns.join(', ')}) VALUES (${values});`
        }).join('\n')
        break
      case 'update':
        sqlOutput.value = data.map(row => {
          const sets = columns.filter(c => c !== 'id').map(c => `${c} = ${formatValue(row[c])}`).join(', ')
          return `UPDATE ${tableName.value} SET ${sets} WHERE id = ${formatValue(row.id)};`
        }).join('\n')
        break
      case 'create':
        const cols = columns.map(col => {
          const val = data[0][col]
          const type = typeof val === 'number' ? (Number.isInteger(val) ? 'INTEGER' : 'DECIMAL') : 'VARCHAR(255)'
          return `  ${col} ${type}`
        }).join(',\n')
        sqlOutput.value = `CREATE TABLE ${tableName.value} (\n${cols}\n);`
        break
    }
  } catch (e: any) {
    ElMessage.error('JSON 解析错误: ' + e.message)
  }
}

const formatValue = (val: any): string => {
  if (val === null || val === undefined) return 'NULL'
  if (typeof val === 'string') return `'${val.replace(/'/g, "''")}'`
  return String(val)
}
</script>

<style scoped>
.json-sql-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.sql-options {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: center;
}
.sql-output {
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
