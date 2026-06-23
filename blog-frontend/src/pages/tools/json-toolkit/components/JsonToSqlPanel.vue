<template>
  <div class="json-sql-panel">
    <IOPanel title="输入 JSON 数组" :subtitle="inputStatus" clearable @clear="inputJson = ''">
      <el-input
        v-model="inputJson"
        type="textarea"
        :rows="8"
        resize="none"
        spellcheck="false"
        placeholder='[{"name":"test","value":1,"active":true}]'
      />
    </IOPanel>

    <ActionToolbar>
      <template #center>
        <el-input v-model="tableName" placeholder="表名" size="small" style="width: 150px" />
        <el-select v-model="sqlType" size="small" style="width: 140px">
          <el-option label="INSERT" value="insert" />
          <el-option label="UPDATE" value="update" />
          <el-option label="CREATE TABLE" value="create" />
          <el-option label="SELECT" value="select" />
          <el-option label="DELETE" value="delete" />
        </el-select>
        <el-checkbox v-model="batchInsert" size="small">批量插入</el-checkbox>
        <el-button type="primary" size="small" @click="generateSql">生成 SQL</el-button>
      </template>
    </ActionToolbar>

    <IOPanel v-if="sqlOutput" title="SQL 输出" :content="sqlOutput" copyable>
      <div class="sql-preview">
        <pre><code>{{ sqlOutput }}</code></pre>
      </div>
      <template #footer>
        <div class="sql-stats">
          <span>列数: {{ columns.length }}</span>
          <span>行数: {{ rowCount }}</span>
          <span>类型: {{ sqlType.toUpperCase() }}</span>
        </div>
      </template>
    </IOPanel>

    <IOPanel v-if="columns.length" title="列信息">
      <div class="columns-table">
        <div class="table-header">
          <span>列名</span>
          <span>类型</span>
          <span>示例值</span>
        </div>
        <div v-for="col in columns" :key="col.name" class="table-row">
          <code>{{ col.name }}</code>
          <span>{{ col.type }}</span>
          <span>{{ col.example }}</span>
        </div>
      </div>
    </IOPanel>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import IOPanel from '@/components/tools/IOPanel.vue'
import ActionToolbar from '@/components/tools/ActionToolbar.vue'

const inputJson = ref('')
const tableName = ref('my_table')
const sqlType = ref('insert')
const sqlOutput = ref('')
const batchInsert = ref(true)

interface ColumnInfo {
  name: string
  type: string
  example: string
}

const columns = computed<ColumnInfo[]>(() => {
  if (!inputJson.value.trim()) return []
  try {
    const data = JSON.parse(inputJson.value)
    if (!Array.isArray(data) || data.length === 0) return []
    const first = data[0]
    return Object.keys(first).map(key => ({
      name: key,
      type: inferSqlType(first[key]),
      example: formatExample(first[key])
    }))
  } catch {
    return []
  }
})

const rowCount = computed(() => {
  if (!inputJson.value.trim()) return 0
  try {
    const data = JSON.parse(inputJson.value)
    return Array.isArray(data) ? data.length : 0
  } catch {
    return 0
  }
})

const inputStatus = computed(() => {
  if (!inputJson.value) return ''
  try {
    const data = JSON.parse(inputJson.value)
    if (Array.isArray(data)) return `✓ ${data.length} 条记录`
    return '✗ 请输入数组'
  } catch {
    return '✗ JSON 格式错误'
  }
})

const inferSqlType = (value: any): string => {
  if (value === null || value === undefined) return 'NULL'
  if (typeof value === 'boolean') return 'BOOLEAN'
  if (typeof value === 'number') {
    return Number.isInteger(value) ? 'INTEGER' : 'DECIMAL(10,2)'
  }
  if (typeof value === 'string') {
    if (/^\d{4}-\d{2}-\d{2}/.test(value)) return 'TIMESTAMP'
    if (value.length > 255) return 'TEXT'
    return `VARCHAR(${Math.max(50, value.length * 2)})`
  }
  if (Array.isArray(value)) return 'JSONB'
  if (typeof value === 'object') return 'JSONB'
  return 'VARCHAR(255)'
}

const formatExample = (value: any): string => {
  if (value === null || value === undefined) return 'NULL'
  const str = String(value)
  return str.length > 30 ? str.substring(0, 27) + '...' : str
}

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

    const cols = Object.keys(data[0])

    switch (sqlType.value) {
      case 'insert':
        if (batchInsert.value) {
          const values = data.map(row => {
            return '(' + cols.map(col => formatValue(row[col])).join(', ') + ')'
          }).join(',\n  ')
          sqlOutput.value = `INSERT INTO ${tableName.value} (${cols.join(', ')})\nVALUES\n  ${values};`
        } else {
          sqlOutput.value = data.map(row => {
            const values = cols.map(col => formatValue(row[col])).join(', ')
            return `INSERT INTO ${tableName.value} (${cols.join(', ')}) VALUES (${values});`
          }).join('\n')
        }
        break

      case 'update':
        sqlOutput.value = data.map(row => {
          const sets = cols.filter(c => c !== 'id').map(c => `${c} = ${formatValue(row[c])}`).join(', ')
          return `UPDATE ${tableName.value} SET ${sets} WHERE id = ${formatValue(row.id)};`
        }).join('\n')
        break

      case 'create':
        const colDefs = cols.map(col => {
          const type = inferSqlType(data[0][col])
          const nullable = data.some((row: any) => row[col] === null) ? '' : ' NOT NULL'
          return `  ${col} ${type}${nullable}`
        }).join(',\n')
        sqlOutput.value = `CREATE TABLE ${tableName.value} (\n  id SERIAL PRIMARY KEY,\n${colDefs},\n  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n);`
        break

      case 'select':
        sqlOutput.value = `SELECT ${cols.join(', ')}\nFROM ${tableName.value}\nWHERE 1=1\nORDER BY id DESC\nLIMIT 100;`
        break

      case 'delete':
        sqlOutput.value = data.map(row => {
          return `DELETE FROM ${tableName.value} WHERE id = ${formatValue(row.id)};`
        }).join('\n')
        break
    }
  } catch (e: any) {
    ElMessage.error('JSON 解析错误: ' + e.message)
  }
}

const formatValue = (val: any): string => {
  if (val === null || val === undefined) return 'NULL'
  if (typeof val === 'boolean') return val ? 'TRUE' : 'FALSE'
  if (typeof val === 'number') return String(val)
  if (typeof val === 'object') return `'${JSON.stringify(val).replace(/'/g, "''")}'`
  return `'${String(val).replace(/'/g, "''")}'`
}
</script>

<style scoped>
.json-sql-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.sql-preview {
  max-height: 400px;
  overflow: auto;
}
.sql-preview pre {
  margin: 0;
  padding: 16px;
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
}
.sql-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.columns-table {
  padding: 16px;
}
.table-header {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 12px;
  padding: 8px 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 4px 4px 0 0;
  font-weight: 600;
  font-size: 13px;
}
.table-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 12px;
  padding: 8px 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  font-size: 13px;
}
.table-row code {
  color: var(--el-color-primary);
}
</style>
