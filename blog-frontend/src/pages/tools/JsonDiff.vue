<template>
  <ToolPageShell
    title="JSON 对比"
    description="按字段路径对比两个 JSON，识别新增、删除、修改和值相同项，适合接口回归和配置检查。"
    eyebrow="文本处理"
    :meta="meta"
  >
    <div class="json-diff">
      <section class="diff-editors">
        <article class="panel">
          <div class="panel-head">
            <div>
              <h2>旧 JSON</h2>
              <p>{{ leftStatus }}</p>
            </div>
            <el-button text @click="leftSource = sampleLeft">示例</el-button>
          </div>
          <el-input v-model="leftSource" type="textarea" :rows="18" placeholder='{"id": 1}' />
        </article>

        <article class="panel">
          <div class="panel-head">
            <div>
              <h2>新 JSON</h2>
              <p>{{ rightStatus }}</p>
            </div>
            <el-button text @click="rightSource = sampleRight">示例</el-button>
          </div>
          <el-input v-model="rightSource" type="textarea" :rows="18" placeholder='{"id": 1}' />
        </article>
      </section>

      <section class="panel result-panel">
        <div class="panel-head">
          <div>
            <h2>差异结果</h2>
            <p>按路径扁平化对比，数组会以 users[0].name 形式展示。</p>
          </div>
          <div class="actions">
            <el-input v-model="pathFilter" clearable placeholder="筛选路径或值" />
            <el-switch v-model="showEqual" active-text="显示相同项" />
            <el-button :icon="DocumentCopy" @click="copyReport">复制报告</el-button>
          </div>
        </div>

        <el-alert
          v-if="parseError"
          :title="parseError"
          type="error"
          show-icon
          :closable="false"
          class="alert"
        />

        <div class="summary-grid">
          <article v-for="item in summary" :key="item.label">
            <strong>{{ item.value }}</strong>
            <span>{{ item.label }}</span>
          </article>
        </div>

        <div class="diff-table">
          <div class="table-row table-head">
            <span>状态</span>
            <span>路径</span>
            <span>旧值</span>
            <span>新值</span>
          </div>
          <div
            v-for="row in visibleRows"
            :key="row.path"
            class="table-row"
            :class="`is-${row.kind}`"
          >
            <span>{{ kindLabel[row.kind] }}</span>
            <code>{{ row.path }}</code>
            <code>{{ row.left }}</code>
            <code>{{ row.right }}</code>
          </div>
          <el-empty v-if="!visibleRows.length" description="没有可展示的差异" />
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

type DiffKind = "added" | "removed" | "changed" | "equal";

interface DiffRow {
  kind: DiffKind;
  path: string;
  left: string;
  right: string;
}

const sampleLeft = JSON.stringify(
  {
    id: 1001,
    name: "MyBlob",
    tags: ["blog", "django"],
    profile: { theme: "light", score: 80 },
    deprecated: true,
  },
  null,
  2,
);

const sampleRight = JSON.stringify(
  {
    id: 1001,
    name: "MyBlob Pro",
    tags: ["blog", "django", "tools"],
    profile: { theme: "dark", score: 92 },
    enabled: true,
  },
  null,
  2,
);

const leftSource = ref(sampleLeft);
const rightSource = ref(sampleRight);
const pathFilter = ref("");
const showEqual = ref(false);

const kindLabel: Record<DiffKind, string> = {
  added: "新增",
  removed: "删除",
  changed: "修改",
  equal: "相同",
};

const parseJson = (value: string) => {
  try {
    return { data: JSON.parse(value), error: "" };
  } catch (error) {
    return {
      data: null,
      error: error instanceof Error ? error.message : "JSON 解析失败",
    };
  }
};

const leftParsed = computed(() => parseJson(leftSource.value));
const rightParsed = computed(() => parseJson(rightSource.value));
const parseError = computed(() => leftParsed.value.error || rightParsed.value.error);

const leftStatus = computed(() =>
  leftParsed.value.error ? `解析失败：${leftParsed.value.error}` : "JSON 有效",
);
const rightStatus = computed(() =>
  rightParsed.value.error ? `解析失败：${rightParsed.value.error}` : "JSON 有效",
);

const flattenJson = (value: unknown, prefix = "$") => {
  const result = new Map<string, unknown>();

  const visit = (node: unknown, path: string) => {
    if (Array.isArray(node)) {
      if (!node.length) result.set(path, []);
      node.forEach((item, index) => visit(item, `${path}[${index}]`));
      return;
    }

    if (node && typeof node === "object") {
      const entries = Object.entries(node as Record<string, unknown>);
      if (!entries.length) result.set(path, {});
      entries.forEach(([key, item]) => visit(item, `${path}.${key}`));
      return;
    }

    result.set(path, node);
  };

  visit(value, prefix);
  return result;
};

const formatValue = (value: unknown) => {
  if (value === undefined) return "";
  if (typeof value === "string") return value;
  return JSON.stringify(value);
};

const diffRows = computed<DiffRow[]>(() => {
  if (parseError.value) return [];

  const left = flattenJson(leftParsed.value.data);
  const right = flattenJson(rightParsed.value.data);
  const paths = Array.from(new Set([...left.keys(), ...right.keys()])).sort();

  return paths.map((path) => {
    const hasLeft = left.has(path);
    const hasRight = right.has(path);
    const leftValue = left.get(path);
    const rightValue = right.get(path);
    const isSame = JSON.stringify(leftValue) === JSON.stringify(rightValue);

    let kind: DiffKind = "equal";
    if (!hasLeft) kind = "added";
    else if (!hasRight) kind = "removed";
    else if (!isSame) kind = "changed";

    return {
      kind,
      path,
      left: formatValue(leftValue),
      right: formatValue(rightValue),
    };
  });
});

const visibleRows = computed(() => {
  const keyword = pathFilter.value.trim().toLowerCase();
  return diffRows.value
    .filter((row) => showEqual.value || row.kind !== "equal")
    .filter((row) => {
      if (!keyword) return true;
      return [row.path, row.left, row.right, kindLabel[row.kind]]
        .join(" ")
        .toLowerCase()
        .includes(keyword);
    })
    .slice(0, 500);
});

const summary = computed(() => {
  const count = (kind: DiffKind) => diffRows.value.filter((row) => row.kind === kind).length;
  return [
    { label: "新增", value: count("added") },
    { label: "删除", value: count("removed") },
    { label: "修改", value: count("changed") },
    { label: "相同", value: count("equal") },
  ];
});

const meta = computed(() => [
  { label: "差异项", value: `${summary.value[0].value + summary.value[1].value + summary.value[2].value}` },
  { label: "展示行", value: `${visibleRows.value.length}` },
  { label: "最大展示", value: "500" },
]);

const copyReport = async () => {
  if (!visibleRows.value.length) {
    ElMessage.warning("没有可复制的差异");
    return;
  }

  const report = visibleRows.value
    .map((row) => `[${kindLabel[row.kind]}] ${row.path}\n- ${row.left}\n+ ${row.right}`)
    .join("\n\n");
  await navigator.clipboard.writeText(report);
  ElMessage.success("差异报告已复制");
};
</script>

<style scoped>
.json-diff {
  display: grid;
  gap: 16px;
}

.diff-editors {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.panel {
  padding: 16px;
  border: 1px solid var(--theme-border);
  border-radius: 10px;
  background: var(--theme-card);
  box-shadow: var(--shadow-xs);
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

.actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
  max-width: 620px;
}

.alert {
  margin-bottom: 12px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}

.summary-grid article {
  padding: 12px;
  border-radius: 8px;
  background: var(--theme-hover);
}

.summary-grid strong,
.summary-grid span {
  display: block;
}

.summary-grid strong {
  color: var(--theme-text);
  font-size: 22px;
}

.summary-grid span {
  color: var(--theme-text-secondary);
  font-size: 12px;
}

.diff-table {
  overflow: auto;
  border: 1px solid var(--theme-border);
  border-radius: 8px;
}

.table-row {
  display: grid;
  grid-template-columns: 80px minmax(180px, 0.9fr) minmax(180px, 1fr) minmax(180px, 1fr);
  gap: 10px;
  align-items: start;
  min-width: 860px;
  padding: 10px 12px;
  border-bottom: 1px solid var(--theme-border);
  font-size: 13px;
}

.table-row:last-child {
  border-bottom: 0;
}

.table-head {
  position: sticky;
  top: 0;
  z-index: 1;
  background: var(--theme-hover);
  color: var(--theme-text-secondary);
  font-weight: 700; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;
}

code {
  overflow-wrap: anywhere;
  color: var(--theme-text-secondary);
  font-family: "JetBrains Mono", "Consolas", monospace;
}

.is-added {
  background: rgba(34, 197, 94, 0.08);
}

.is-removed {
  background: rgba(239, 68, 68, 0.08);
}

.is-changed {
  background: rgba(245, 158, 11, 0.1);
}

@media (max-width: 960px) {
  .diff-editors,
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .panel-head {
    flex-direction: column;
  }

  .actions {
    max-width: none;
    justify-content: flex-start;
  }
}
:deep(.el-textarea__inner) { font-family: var(--font-mono); font-size: 14px; line-height: 1.7; }
</style>
