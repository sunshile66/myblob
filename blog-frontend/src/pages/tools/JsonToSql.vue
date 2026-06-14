<template>
  <ToolPageShell
    title="JSON 转 SQL"
    description="把 JSON 对象或数组转换成 INSERT、UPDATE、UPSERT SQL，支持字段类型推断和批量生成。"
    eyebrow="开发调试"
    :meta="meta"
  >
    <div class="sql-workbench">
      <section class="panel">
        <div class="panel-head">
          <div>
            <h2>输入 JSON</h2>
            <p>支持单个对象或对象数组，会自动收集字段并补齐 NULL。</p>
          </div>
          <el-button text @click="loadSample">示例</el-button>
        </div>

        <el-input v-model="source" type="textarea" :rows="20" placeholder='[{"id": 1, "title": "hello"}]' />
      </section>

      <section class="panel">
        <div class="panel-head">
          <div>
            <h2>SQL 输出</h2>
            <p>{{ statusText }}</p>
          </div>
          <el-button :icon="DocumentCopy" @click="copySql">复制</el-button>
        </div>

        <div class="settings-grid">
          <el-input v-model="tableName" placeholder="表名" />
          <el-select v-model="dialect" placeholder="SQL 方言">
            <el-option label="MySQL" value="mysql" />
            <el-option label="PostgreSQL" value="postgres" />
            <el-option label="SQLite" value="sqlite" />
          </el-select>
          <el-select v-model="mode" placeholder="输出模式">
            <el-option label="INSERT" value="insert" />
            <el-option label="UPDATE" value="update" />
            <el-option label="UPSERT" value="upsert" />
            <el-option label="CREATE TABLE" value="create" />
          </el-select>
          <el-input v-model="primaryKey" placeholder="主键字段，如 id" />
        </div>

        <el-alert
          v-if="parseError"
          :title="parseError"
          type="error"
          show-icon
          :closable="false"
          class="alert"
        />

        <pre class="sql-output"><code>{{ sqlOutput || "SQL 会显示在这里" }}</code></pre>
      </section>

      <section class="panel schema-panel">
        <div class="panel-head">
          <div>
            <h2>字段推断</h2>
            <p>根据样本值推断字段类型，生成建表语句时会使用这些类型。</p>
          </div>
        </div>

        <div class="schema-grid">
          <div class="schema-row schema-head">
            <span>字段</span>
            <span>类型</span>
            <span>样本值</span>
          </div>
          <div v-for="field in schema" :key="field.name" class="schema-row">
            <code>{{ field.name }}</code>
            <span>{{ field.type }}</span>
            <code>{{ field.sample }}</code>
          </div>
        </div>
      </section>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { DocumentCopy } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import ToolPageShell from "@features/tools/ui/ToolPageShell.vue";

type Dialect = "mysql" | "postgres" | "sqlite";
type SqlMode = "insert" | "update" | "upsert" | "create";
type JsonRecord = Record<string, unknown>;

const sample = JSON.stringify(
  [
    {
      id: 1,
      title: "Django 6 新特性",
      slug: "django-6-new-features",
      published: true,
      views: 1280,
      published_at: "2026-05-20 10:30:00",
    },
    {
      id: 2,
      title: "工具中心优化",
      slug: "tool-center-upgrade",
      published: false,
      views: 380,
      published_at: null,
    },
  ],
  null,
  2,
);

const source = ref(sample);
const tableName = ref("blog_posts");
const dialect = ref<Dialect>("mysql");
const mode = ref<SqlMode>("insert");
const primaryKey = ref("id");

const parsedRows = computed(() => {
  try {
    const parsed = JSON.parse(source.value);
    const rows = Array.isArray(parsed) ? parsed : [parsed];
    if (!rows.every((row) => row && typeof row === "object" && !Array.isArray(row))) {
      return { rows: [] as JsonRecord[], error: "JSON 顶层必须是对象或对象数组" };
    }
    return { rows: rows as JsonRecord[], error: "" };
  } catch (error) {
    return {
      rows: [] as JsonRecord[],
      error: error instanceof Error ? error.message : "JSON 解析失败",
    };
  }
});

const parseError = computed(() => parsedRows.value.error);
const fields = computed(() =>
  Array.from(new Set(parsedRows.value.rows.flatMap((row) => Object.keys(row)))),
);

const quoteIdentifier = (name: string) => {
  if (dialect.value === "postgres") return `"${name.replace(/"/g, '""')}"`;
  return `\`${name.replace(/`/g, "``")}\``;
};

const quoteTable = computed(() => quoteIdentifier(tableName.value.trim() || "my_table"));

const escapeSql = (value: string) => value.replace(/'/g, "''");

const toSqlValue = (value: unknown): string => {
  if (value === null || value === undefined) return "NULL";
  if (typeof value === "number") return Number.isFinite(value) ? String(value) : "NULL";
  if (typeof value === "boolean") {
    if (dialect.value === "postgres") return value ? "TRUE" : "FALSE";
    return value ? "1" : "0";
  }
  if (value instanceof Date) return `'${escapeSql(value.toISOString())}'`;
  if (typeof value === "object") return `'${escapeSql(JSON.stringify(value))}'`;
  return `'${escapeSql(String(value))}'`;
};

const inferType = (values: unknown[]) => {
  const meaningful = values.filter((value) => value !== null && value !== undefined);
  if (!meaningful.length) return dialect.value === "postgres" ? "TEXT" : "VARCHAR(255)";
  if (meaningful.every((value) => typeof value === "number" && Number.isInteger(value))) return "INTEGER";
  if (meaningful.every((value) => typeof value === "number")) return dialect.value === "postgres" ? "DOUBLE PRECISION" : "DOUBLE";
  if (meaningful.every((value) => typeof value === "boolean")) return dialect.value === "postgres" ? "BOOLEAN" : "TINYINT(1)";
  if (meaningful.every((value) => typeof value === "string" && /^\d{4}-\d{2}-\d{2}/.test(value))) return "DATETIME";
  if (meaningful.some((value) => typeof value === "object")) return dialect.value === "postgres" ? "JSONB" : "JSON";
  const maxLength = Math.max(...meaningful.map((value) => String(value).length));
  return maxLength > 255 ? "TEXT" : `VARCHAR(${Math.max(64, Math.min(255, maxLength + 32))})`;
};

const schema = computed(() =>
  fields.value.map((name) => {
    const values = parsedRows.value.rows.map((row) => row[name]);
    return {
      name,
      type: inferType(values),
      sample: values.find((value) => value !== null && value !== undefined) === undefined
        ? "NULL"
        : JSON.stringify(values.find((value) => value !== null && value !== undefined)),
    };
  }),
);

const buildInsert = () => {
  if (!parsedRows.value.rows.length || !fields.value.length) return "";
  const columns = fields.value.map(quoteIdentifier).join(", ");
  const values = parsedRows.value.rows
    .map((row) => `(${fields.value.map((field) => toSqlValue(row[field])).join(", ")})`)
    .join(",\n");
  return `INSERT INTO ${quoteTable.value} (${columns}) VALUES\n${values};`;
};

const buildUpdate = () => {
  if (!parsedRows.value.rows.length || !primaryKey.value.trim()) return "";
  const pk = primaryKey.value.trim();
  return parsedRows.value.rows
    .map((row) => {
      const assignments = fields.value
        .filter((field) => field !== pk)
        .map((field) => `${quoteIdentifier(field)} = ${toSqlValue(row[field])}`)
        .join(", ");
      return `UPDATE ${quoteTable.value} SET ${assignments} WHERE ${quoteIdentifier(pk)} = ${toSqlValue(row[pk])};`;
    })
    .join("\n");
};

const buildCreate = () => {
  if (!schema.value.length) return "";
  const pk = primaryKey.value.trim();
  const columns = schema.value
    .map((field) => {
      const primary = field.name === pk ? " PRIMARY KEY" : "";
      return `  ${quoteIdentifier(field.name)} ${field.type}${primary}`;
    })
    .join(",\n");
  return `CREATE TABLE ${quoteTable.value} (\n${columns}\n);`;
};

const buildUpsert = () => {
  const insert = buildInsert().replace(/;$/, "");
  const pk = primaryKey.value.trim();
  if (!insert || !pk) return insert ? `${insert};` : "";
  const updateFields = fields.value.filter((field) => field !== pk);

  if (dialect.value === "postgres") {
    const setSql = updateFields
      .map((field) => `${quoteIdentifier(field)} = EXCLUDED.${quoteIdentifier(field)}`)
      .join(", ");
    return `${insert}\nON CONFLICT (${quoteIdentifier(pk)}) DO UPDATE SET ${setSql};`;
  }

  if (dialect.value === "sqlite") {
    const setSql = updateFields
      .map((field) => `${quoteIdentifier(field)} = excluded.${quoteIdentifier(field)}`)
      .join(", ");
    return `${insert}\nON CONFLICT(${quoteIdentifier(pk)}) DO UPDATE SET ${setSql};`;
  }

  const setSql = updateFields
    .map((field) => `${quoteIdentifier(field)} = VALUES(${quoteIdentifier(field)})`)
    .join(", ");
  return `${insert}\nON DUPLICATE KEY UPDATE ${setSql};`;
};

const sqlOutput = computed(() => {
  if (parseError.value) return "";
  if (mode.value === "create") return buildCreate();
  if (mode.value === "update") return buildUpdate();
  if (mode.value === "upsert") return buildUpsert();
  return buildInsert();
});

const statusText = computed(() => {
  if (parseError.value) return "请先修复 JSON 格式";
  return `${parsedRows.value.rows.length} 行，${fields.value.length} 个字段`;
});

const meta = computed(() => [
  { label: "记录数", value: `${parsedRows.value.rows.length}` },
  { label: "字段数", value: `${fields.value.length}` },
  { label: "模式", value: mode.value.toUpperCase() },
]);

const loadSample = () => {
  source.value = sample;
};

const copySql = async () => {
  if (!sqlOutput.value) {
    ElMessage.warning("没有可复制的 SQL");
    return;
  }
  await navigator.clipboard.writeText(sqlOutput.value);
  ElMessage.success("SQL 已复制");
};
</script>

<style scoped>
.sql-workbench {
  display: grid;
  grid-template-columns: minmax(0, 0.9fr) minmax(0, 1.1fr);
  gap: 16px;
}

.panel {
  padding: 16px;
  border: 1px solid var(--theme-border);
  border-radius: 10px;
  background: var(--theme-card);
  box-shadow: var(--shadow-xs);
}

.schema-panel {
  grid-column: 1 / -1;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

h2 {
  margin: 0;
  color: var(--theme-text);
  font-size: 18px;
}

p {
  margin: 6px 0 0;
  color: var(--theme-text-secondary);
  font-size: 13px;
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}

.alert {
  margin-bottom: 12px;
}

.sql-output {
  min-height: 394px;
  margin: 0;
  padding: 14px;
  overflow: auto;
  border-radius: 8px;
  background: var(--theme-text);
  color: #e5e7eb;
  font-size: 13px;
  line-height: 1.65;
  white-space: pre-wrap;
}

.schema-grid {
  overflow: auto;
  border: 1px solid var(--theme-border);
  border-radius: 8px;
}

.schema-row {
  display: grid;
  grid-template-columns: 220px 180px minmax(260px, 1fr);
  gap: 12px;
  min-width: 720px;
  padding: 10px 12px;
  border-bottom: 1px solid var(--theme-border);
  font-size: 13px;
}

.schema-row:last-child {
  border-bottom: 0;
}

.schema-head {
  background: var(--theme-hover);
  color: var(--theme-text-secondary);
  font-weight: 800;
}

code {
  overflow-wrap: anywhere;
  color: var(--theme-text-secondary);
  font-family: "JetBrains Mono", "Consolas", monospace;
}

@media (max-width: 1024px) {
  .sql-workbench,
  .settings-grid {
    grid-template-columns: 1fr;
  }

  .panel-head {
    flex-direction: column;
  }
}
:deep(.el-textarea__inner) { font-family: var(--font-mono); font-size: 14px; line-height: 1.7; }
</style>
