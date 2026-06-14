<template>
  <ToolPageShell
    title="文本对比"
    description="按行计算差异，支持忽略空白/大小写、并排视图、统一视图和差异统计。"
    eyebrow="文本处理"
    :meta="meta"
  >
    <div class="workbench">
      <section class="editor-grid">
        <article class="panel">
          <div class="panel-head">
            <h2>原文</h2>
            <div class="inline-actions">
              <el-button text :icon="DocumentCopy" @click="pasteInto('left')">粘贴</el-button>
              <el-button text :icon="Delete" @click="clearSide('left')">清空</el-button>
            </div>
          </div>
          <el-input v-model="leftText" type="textarea" :rows="14" placeholder="粘贴原始文本" />
          <div class="stats-line">
            <span>{{ leftLines.length }} 行</span>
            <span>{{ leftText.length }} 字符</span>
          </div>
        </article>

        <article class="panel">
          <div class="panel-head">
            <h2>修改后</h2>
            <div class="inline-actions">
              <el-button text :icon="DocumentCopy" @click="pasteInto('right')">粘贴</el-button>
              <el-button text :icon="Delete" @click="clearSide('right')">清空</el-button>
            </div>
          </div>
          <el-input v-model="rightText" type="textarea" :rows="14" placeholder="粘贴修改后的文本" />
          <div class="stats-line">
            <span>{{ rightLines.length }} 行</span>
            <span>{{ rightText.length }} 字符</span>
          </div>
        </article>
      </section>

      <section class="toolbar panel">
        <div class="toolbar-options">
          <el-checkbox v-model="ignoreWhitespace">忽略首尾空白</el-checkbox>
          <el-checkbox v-model="ignoreCase">忽略大小写</el-checkbox>
          <el-checkbox v-model="hideEqual">只看差异</el-checkbox>
        </div>
        <div class="toolbar-actions">
          <el-button :icon="Sort" @click="swapTexts">交换</el-button>
          <el-button :icon="DocumentCopy" @click="copyUnified">复制差异</el-button>
          <el-button type="primary" :icon="Search" @click="compare">开始对比</el-button>
        </div>
      </section>

      <section class="panel result-panel">
        <div class="panel-head">
          <div>
            <h2>对比结果</h2>
            <p>使用 LCS 按行对齐，插入与删除会保留相邻上下文。</p>
          </div>
          <div class="tag-row">
            <el-tag type="success">+{{ addedCount }}</el-tag>
            <el-tag type="danger">-{{ removedCount }}</el-tag>
            <el-tag>{{ equalCount }} 相同</el-tag>
          </div>
        </div>

        <el-tabs v-model="activeView">
          <el-tab-pane label="并排视图" name="side">
            <div class="side-view">
              <div class="diff-header">原文</div>
              <div class="diff-header">修改后</div>
              <template v-for="row in visibleRows" :key="row.key">
                <div class="diff-line" :class="row.left?.type || 'empty'">
                  <span class="line-no">{{ row.left?.line || '' }}</span>
                  <code>{{ row.left?.text || '' }}</code>
                </div>
                <div class="diff-line" :class="row.right?.type || 'empty'">
                  <span class="line-no">{{ row.right?.line || '' }}</span>
                  <code>{{ row.right?.text || '' }}</code>
                </div>
              </template>
            </div>
          </el-tab-pane>

          <el-tab-pane label="统一视图" name="unified">
            <div class="unified-view">
              <div v-for="line in visibleUnifiedLines" :key="line.key" class="diff-line" :class="line.type">
                <span class="line-no">{{ line.marker }}</span>
                <code>{{ line.text }}</code>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="统计" name="stats">
            <div class="stats-grid">
              <article>
                <strong>{{ changeRate }}%</strong>
                <span>变化比例</span>
              </article>
              <article>
                <strong>{{ similarity }}%</strong>
                <span>相似度</span>
              </article>
              <article>
                <strong>{{ leftText.length - rightText.length }}</strong>
                <span>字符差值</span>
              </article>
              <article>
                <strong>{{ diffRows.length }}</strong>
                <span>对齐行数</span>
              </article>
            </div>
          </el-tab-pane>
        </el-tabs>
      </section>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { Delete, DocumentCopy, Search, Sort } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import ToolPageShell from "@features/tools/ui/ToolPageShell.vue";

type DiffType = "equal" | "added" | "removed";

interface DiffCell {
  type: DiffType;
  text: string;
  line: number;
}

interface DiffRow {
  key: string;
  type: DiffType;
  left?: DiffCell;
  right?: DiffCell;
}

interface UnifiedLine {
  key: string;
  type: DiffType;
  text: string;
  marker: string;
}

const leftText = ref("const name = 'MyBlob';\nconsole.log(name);\n");
const rightText = ref("const name = 'MyBlob';\nconsole.info(name);\n");
const ignoreWhitespace = ref(false);
const ignoreCase = ref(false);
const hideEqual = ref(false);
const activeView = ref("side");
const diffRows = ref<DiffRow[]>([]);

const leftLines = computed(() => splitLines(leftText.value));
const rightLines = computed(() => splitLines(rightText.value));

const addedCount = computed(() => diffRows.value.filter((row) => row.type === "added").length);
const removedCount = computed(() => diffRows.value.filter((row) => row.type === "removed").length);
const equalCount = computed(() => diffRows.value.filter((row) => row.type === "equal").length);

const visibleRows = computed(() =>
  hideEqual.value ? diffRows.value.filter((row) => row.type !== "equal") : diffRows.value
);

const unifiedLines = computed<UnifiedLine[]>(() =>
  diffRows.value.flatMap((row, index) => {
    if (row.type === "equal" && row.left) {
      return [{ key: `u-${index}-equal`, type: "equal", marker: " ", text: row.left.text }];
    }
    const lines: UnifiedLine[] = [];
    if (row.left) {
      lines.push({ key: `u-${index}-removed`, type: "removed", marker: "-", text: row.left.text });
    }
    if (row.right) {
      lines.push({ key: `u-${index}-added`, type: "added", marker: "+", text: row.right.text });
    }
    return lines;
  })
);

const visibleUnifiedLines = computed(() =>
  hideEqual.value ? unifiedLines.value.filter((line) => line.type !== "equal") : unifiedLines.value
);

const totalChanges = computed(() => addedCount.value + removedCount.value);

const changeRate = computed(() => {
  const base = Math.max(leftLines.value.length, rightLines.value.length, 1);
  return Math.min(100, Math.round((totalChanges.value / base) * 100));
});

const similarity = computed(() => Math.max(0, 100 - changeRate.value));

const meta = computed(() => [
  { label: "新增行", value: `+${addedCount.value}` },
  { label: "删除行", value: `-${removedCount.value}` },
  { label: "相似度", value: `${similarity.value}%` },
]);

const splitLines = (value: string) => value.replace(/\r\n/g, "\n").split("\n");

const normalize = (value: string) => {
  let result = ignoreWhitespace.value ? value.trim() : value;
  if (ignoreCase.value) result = result.toLowerCase();
  return result;
};

const buildLcsTable = (left: string[], right: string[]) => {
  const rows = left.length;
  const cols = right.length;
  const table = Array.from({ length: rows + 1 }, () => Array(cols + 1).fill(0));

  for (let row = rows - 1; row >= 0; row -= 1) {
    for (let col = cols - 1; col >= 0; col -= 1) {
      table[row][col] =
        normalize(left[row]) === normalize(right[col])
          ? table[row + 1][col + 1] + 1
          : Math.max(table[row + 1][col], table[row][col + 1]);
    }
  }

  return table;
};

const MAX_LINES = 5000; // 最大行数限制，防止UI冻结

const compare = () => {
  if (!leftText.value && !rightText.value) {
    diffRows.value = [];
    return;
  }

  const left = leftLines.value;
  const right = rightLines.value;

  // 检查行数限制
  if (left.length > MAX_LINES || right.length > MAX_LINES) {
    ElMessage.warning(`文本行数超过限制(${MAX_LINES}行)，请减少文本量`);
    return;
  }

  const table = buildLcsTable(left, right);
  const rows: DiffRow[] = [];
  let leftIndex = 0;
  let rightIndex = 0;

  while (leftIndex < left.length || rightIndex < right.length) {
    const leftValue = left[leftIndex];
    const rightValue = right[rightIndex];

    if (leftIndex < left.length && rightIndex < right.length && normalize(leftValue) === normalize(rightValue)) {
      rows.push({
        key: `r-${rows.length}`,
        type: "equal",
        left: { type: "equal", text: leftValue, line: leftIndex + 1 },
        right: { type: "equal", text: rightValue, line: rightIndex + 1 },
      });
      leftIndex += 1;
      rightIndex += 1;
    } else if (rightIndex < right.length && (leftIndex === left.length || table[leftIndex][rightIndex + 1] >= table[leftIndex + 1][rightIndex])) {
      rows.push({
        key: `r-${rows.length}`,
        type: "added",
        right: { type: "added", text: rightValue, line: rightIndex + 1 },
      });
      rightIndex += 1;
    } else if (leftIndex < left.length) {
      rows.push({
        key: `r-${rows.length}`,
        type: "removed",
        left: { type: "removed", text: leftValue, line: leftIndex + 1 },
      });
      leftIndex += 1;
    }
  }

  diffRows.value = rows;
};

const clearSide = (side: "left" | "right") => {
  if (side === "left") leftText.value = "";
  else rightText.value = "";
};

const pasteInto = async (side: "left" | "right") => {
  try {
    const value = await navigator.clipboard.readText();
    if (side === "left") leftText.value = value;
    else rightText.value = value;
  } catch {
    ElMessage.error("读取剪贴板失败");
  }
};

const swapTexts = () => {
  [leftText.value, rightText.value] = [rightText.value, leftText.value];
};

const copyUnified = async () => {
  const content = unifiedLines.value.map((line) => `${line.marker} ${line.text}`).join("\n");
  if (!content) {
    ElMessage.warning("没有可复制的差异内容");
    return;
  }
  await navigator.clipboard.writeText(content);
  ElMessage.success("差异内容已复制");
};

// 防抖比较，避免频繁触发
let compareTimer: ReturnType<typeof setTimeout> | null = null;

const debouncedCompare = () => {
  if (compareTimer) clearTimeout(compareTimer);
  compareTimer = setTimeout(compare, 300);
};

watch([leftText, rightText, ignoreWhitespace, ignoreCase], debouncedCompare, { immediate: true });
</script>

<style scoped>
.workbench {
  display: grid;
  gap: 16px;
}

.editor-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.panel {
  padding: 16px;
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: var(--shadow-xs);
}

.panel-head,
.toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
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
  line-height: 1.6;
}

.inline-actions,
.toolbar-actions,
.toolbar-options,
.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.stats-line {
  display: flex;
  gap: 14px;
  margin-top: 8px;
  color: var(--theme-text-secondary);
  font-size: 12px;
}

.side-view {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.diff-header {
  padding: 10px 12px;
  background: #f8fafc;
  color: var(--theme-text-secondary);
  font-size: 13px;
  font-weight: 700;
  border-bottom: 1px solid var(--theme-border);
}

.diff-header:first-child,
.side-view .diff-line:nth-child(2n + 1) {
  border-right: 1px solid var(--theme-border);
}

.diff-line {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  min-height: 30px;
  padding: 6px 10px;
  color: var(--theme-text);
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.diff-line code {
  font-family: inherit;
  word-break: break-word;
}

.line-no {
  color: var(--theme-text-tertiary);
  user-select: none;
}

.diff-line.added {
  background: rgba(16, 185, 129, 0.12);
}

.diff-line.removed {
  background: rgba(239, 68, 68, 0.12);
}

.diff-line.empty {
  background: var(--theme-hover);
}

.unified-view {
  max-height: 560px;
  overflow: auto;
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-md);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.stats-grid article {
  padding: 16px;
  border-radius: var(--radius-md);
  background: #f8fafc;
  border: 1px solid var(--theme-border);
}

.stats-grid strong {
  display: block;
  color: var(--theme-text);
  font-size: 24px;
}

.stats-grid span {
  color: var(--theme-text-secondary);
  font-size: 13px;
}

@media (max-width: 960px) {
  .editor-grid,
  .side-view,
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .panel-head,
  .toolbar {
    flex-direction: column;
  }
}
:deep(.el-textarea__inner) { font-family: var(--font-mono); font-size: 14px; line-height: 1.7; }

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
