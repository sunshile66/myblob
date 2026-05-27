<template>
  <ToolPageShell
    title="JSON 格式化"
    description="左右分栏编辑、树形查看、搜索路径、折叠层级、压缩复制。"
    eyebrow="文本处理"
    :meta="toolMeta"
  >
    <div class="json-studio">
      <section class="editor-pane">
        <header class="pane-toolbar">
          <div class="pane-title">
            <strong>输入 JSON</strong>
            <span>{{ inputStatus }}</span>
          </div>
          <div class="toolbar-actions">
            <el-select v-model="indentSize" size="small" style="width: 104px" @change="formatJson()">
              <el-option label="2 空格" :value="2" />
              <el-option label="4 空格" :value="4" />
              <el-option label="Tab" :value="'\t'" />
            </el-select>
            <el-checkbox v-model="sortKeys" @change="formatJson()">排序</el-checkbox>
          </div>
        </header>

        <el-input
          v-model="inputJson"
          class="json-input"
          type="textarea"
          resize="none"
          spellcheck="false"
          placeholder='请输入 JSON，例如 {"name":"MyBlob","enabled":true}'
          @input="runLiveFormat"
        />

        <footer class="pane-footer">
          <el-button text @click="clearAll">清空</el-button>
          <el-button text @click="copyText(inputJson, '输入内容已复制')">复制输入</el-button>
        </footer>
      </section>

      <section class="result-pane">
        <header class="result-toolbar">
          <button type="button" class="tool-action" @click="formatJson()">
            <Search /> 格式化
          </button>
          <button type="button" class="tool-action" @click="toggleFullscreen">
            <FullScreen /> {{ fullScreen ? "还原" : "全屏" }}
          </button>
          <button type="button" class="tool-action" @click="search">
            <Search /> 搜索
          </button>
          <button type="button" class="tool-action" @click="toggleClosed">
            <CaretTop /> {{ closed ? "展开" : "折叠" }}
          </button>
          <label class="level-control">
            层级:
            <span
              id="deep_input"
              class="deep-input"
              contenteditable="true"
              spellcheck="false"
              @click="deepSelected"
              @input="deep = parseInt(($event.target as HTMLElement).innerText)"
              @blur="verifyDeep(($event.target as HTMLElement).innerText)"
            >{{ deep }}</span>
          </label>
          <button type="button" class="tool-action" @click="unescapeJson">
            <MagicStick /> {{ translation ? "去除转义" : "转义" }}
          </button>
          <button type="button" class="tool-action" @click="decodeUrlInput">
            <Link /> 解码
          </button>
          <button type="button" class="tool-action" @click="sortAndFormat">
            <Sort /> 排序
          </button>
          <button type="button" class="tool-action" @click="minifyJson">
            <Coin /> 压缩
          </button>
          <button type="button" class="tool-action" @click="copyText(activeOutput, '结果已复制')">
            <DocumentCopy /> 复制
          </button>
          <button type="button" class="tool-action" @click="downloadOutput">
            <Download /> 下载
          </button>
        </header>

        <div v-if="parseError" class="json-error">
          <div class="error-actions">
            <button type="button" class="tool-action" @click="unescapeJson">
              <MagicStick /> 去除转义
            </button>
            <button type="button" class="tool-action" @click="decodeUrlInput">
              <Link /> 解码
            </button>
          </div>
          <strong>{{ parseError }}</strong>
          <ol v-if="errorPreview" class="error-code" v-html="errorPreview"></ol>
        </div>

        <div class="result-body" :class="{ fullscreen: fullScreen }">
          <div v-if="!hidenSearchBar" class="search-bar">
            <el-input
              v-model="searchQuery"
              clearable
              size="small"
              placeholder="搜索 key / value / path"
              @keyup.enter="selectNextMatch"
            />
            <span>{{ matchRows.length ? `${selectedMatchIndex + 1}/${matchRows.length}` : "0/0" }}</span>
            <el-button size="small" @click="selectPrevMatch">上一个</el-button>
            <el-button size="small" @click="selectNextMatch">下一个</el-button>
            <el-button size="small" text @click="hidenSearchBar = true">关闭</el-button>
          </div>

          <div class="result-tabs">
            <el-segmented v-model="viewMode" :options="viewOptions" />
          </div>

          <div v-if="viewMode === 'tree'" class="json-tree">
            <div v-if="!treeRows.length" class="empty-state">格式化后的 JSON 会显示在这里。</div>
            <div
              v-for="row in visibleRows"
              :key="row.id"
              class="tree-row"
              :class="{ matched: row.matched }"
              :style="{ paddingLeft: `${row.depth * 20 + 8}px` }"
              @click="selectNode(row)"
            >
              <button
                v-if="row.expandable"
                class="toggle-btn"
                type="button"
                @click.stop="toggleNode(row.id)"
              >
                {{ expandedIds.has(row.id) ? "⊟" : "⊞" }}
              </button>
              <span v-else class="toggle-placeholder"></span>
              <span
                v-if="row.keyName"
                class="json-key"
                contenteditable="true"
                spellcheck="false"
                @click.stop
              >"{{ row.keyName }}"</span>
              <span v-if="row.keyName" class="colon">:</span>
              <span
                class="json-value"
                :class="`is-${row.type}`"
                :contenteditable="!row.expandable"
                spellcheck="false"
                @click.stop
              >{{ row.preview }}</span>
              <span v-if="row.meta" class="json-meta">{{ row.meta }}</span>
              <span class="row-actions">
                <button type="button" @click.stop="copyText(row.path, '路径已复制')">复制路径</button>
                <button type="button" @click.stop="copyText(getRowNodeText(row), '节点已复制')">复制节点</button>
              </span>
            </div>
            <button v-if="canShowMoreRows" type="button" class="load-more" @click="showMoreRows">
              继续加载 {{ remainingRows }} 行
            </button>
          </div>

          <pre v-else class="code-output"><code>{{ formattedOutput || "处理结果会显示在这里。" }}</code></pre>
        </div>

        <footer class="query-bar">
          <el-input v-model="jsonPath" placeholder="查询路径，例如 user.profile.name 或 items[0].id">
            <template #append>
              <el-button @click="queryJsonPath">查询</el-button>
            </template>
          </el-input>
          <pre v-if="pathResult" class="path-result">{{ pathResult }}</pre>
        </footer>

        <div v-if="currentPath" class="path-dock">
          <span>路径: <strong>{{ currentPath }}</strong></span>
          <button type="button" @click="copyText(currentPath, '路径已复制')">复制路径</button>
          <button type="button" @click="copyText(currentDataText, '节点已复制')">复制节点</button>
          <button type="button" @click="currentPath = ''">关闭</button>
        </div>
      </section>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import {
  CaretTop,
  Coin,
  DocumentCopy,
  Download,
  FullScreen,
  Link,
  MagicStick,
  Search,
  Sort,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import ToolPageShell from "@features/tools/ui/ToolPageShell.vue";

type JsonValue = null | boolean | number | string | JsonValue[] | { [key: string]: JsonValue };
type ViewMode = "tree" | "code";
type JsonType = "object" | "array" | "string" | "number" | "boolean" | "null";

interface TreeRow {
  id: string;
  parentId: string;
  depth: number;
  keyName: string;
  path: string;
  type: JsonType;
  preview: string;
  meta: string;
  expandable: boolean;
  matched: boolean;
}

const sampleJson = JSON.stringify(
  {
    base_resp: null,
    has_more: true,
    offset: 6,
    status_code: 0,
    status_msg: "Operation successful",
    video_items: [
      {
        activity: null,
        activity_video_type: 0,
        ad_aweme_source: null,
        ai_follow_images: null,
        author: {
          uid: "10001",
          nickname: "MyBlob",
          tags: ["blog", "tools", "json"],
        },
      },
    ],
  },
  null,
  2,
);

const inputJson = ref(sampleJson);
const formattedOutput = ref(sampleJson);
const parseError = ref("");
const indentSize = ref<number | "\t">(2);
const sortKeys = ref(false);
const deep = ref(5);
const searchQuery = ref("");
const jsonPath = ref("");
const pathResult = ref("");
const viewMode = ref<ViewMode>("tree");
const fullScreen = ref(false);
const closed = ref(false);
const translation = ref(true);
const hidenSearchBar = ref(true);
const expandedIds = ref(new Set<string>());
const currentPath = ref("");
const currentDataText = ref("");
const errorPreview = ref("");
const selectedMatchIndex = ref(0);
const renderBatchSize = 800;
const visibleLimit = ref(renderBatchSize);
let liveTimer: number | undefined;

const viewOptions = [
  { label: "树形", value: "tree" },
  { label: "源码", value: "code" },
];

const parsedOutput = computed(() => {
  if (!formattedOutput.value) return undefined;
  try {
    return JSON.parse(formattedOutput.value) as JsonValue;
  } catch {
    return undefined;
  }
});

const activeOutput = computed(() => formattedOutput.value);

const inputStatus = computed(() => {
  if (!inputJson.value.trim()) return "等待输入";
  return parseError.value ? "格式异常" : `${inputJson.value.length} 字符`;
});

const jsonStats = computed(() => {
  if (parsedOutput.value === undefined) return { keys: 0, arrays: 0, objects: 0, depth: 0 };
  return collectStats(parsedOutput.value);
});

const toolMeta = computed(() => [
  { label: "状态", value: parseError.value ? "错误" : "正常" },
  { label: "层级", value: `${jsonStats.value.depth}` },
  { label: "节点", value: `${treeRows.value.length}` },
  { label: "渲染", value: `${visibleRows.value.length}/${visibleRowsAll.value.length}` },
]);

const getType = (value: JsonValue): JsonType => {
  if (value === null) return "null";
  if (Array.isArray(value)) return "array";
  if (typeof value === "object") return "object";
  return typeof value as JsonType;
};

const escapeDisplayString = (value: string) => translation.value ? value.replace(/"/g, '\\"') : value;

const getPreview = (value: JsonValue) => {
  const type = getType(value);
  if (type === "array") return "[";
  if (type === "object") return "{";
  if (type === "string") return `"${escapeDisplayString(value)}"`;
  return String(value);
};

const getMeta = (value: JsonValue) => {
  if (Array.isArray(value)) return `${value.length} items`;
  if (value && typeof value === "object") return `${Object.keys(value).length} keys`;
  return "";
};

const getSearchText = (value: JsonValue, keyName: string, path: string) => {
  const type = getType(value);
  if (type === "array" || type === "object") {
    return `${keyName} ${path} ${getMeta(value)} ${type}`.toLowerCase();
  }
  return `${keyName} ${path} ${getPreview(value)}`.toLowerCase();
};

const makeChildPath = (parentPath: string, key: string, isArrayParent: boolean) =>
  isArrayParent ? `${parentPath}[${key}]` : `${parentPath}["${key}"]`;

const treeRows = computed<TreeRow[]>(() => {
  if (parsedOutput.value === undefined) return [];
  const rows: TreeRow[] = [];
  const keyword = searchQuery.value.trim().toLowerCase();

  const visit = (value: JsonValue, keyName: string, path: string, parentId: string, depth: number) => {
    if (rows.length >= 3000) return;
    const type = getType(value);
    const id = path || "__root__";
    const searchable = getSearchText(value, keyName, path);
    rows.push({
      id,
      parentId,
      depth,
      keyName,
      path,
      type,
      preview: getPreview(value),
      meta: getMeta(value),
      expandable: type === "array" || type === "object",
      matched: Boolean(keyword && searchable.includes(keyword)),
    });

    if (Array.isArray(value)) {
      value.forEach((item, index) => visit(item, "", makeChildPath(path, String(index), true), id, depth + 1));
      return;
    }

    if (value && typeof value === "object") {
      Object.entries(value).forEach(([key, item]) =>
        visit(item, key, makeChildPath(path, key, false), id, depth + 1),
      );
    }
  };

  visit(parsedOutput.value, "", "", "", 0);
  return rows;
});

const visibleRowsAll = computed(() => {
  const rowMap = new Map(treeRows.value.map((row) => [row.id, row]));
  return treeRows.value.filter((row) => {
    let parentId = row.parentId;
    while (parentId) {
      if (!expandedIds.value.has(parentId)) return false;
      parentId = rowMap.get(parentId)?.parentId || "";
    }
    return true;
  });
});

const visibleRows = computed(() => visibleRowsAll.value.slice(0, visibleLimit.value));
const remainingRows = computed(() => Math.max(visibleRowsAll.value.length - visibleRows.value.length, 0));
const canShowMoreRows = computed(() => remainingRows.value > 0);
const matchRows = computed(() => visibleRowsAll.value.filter((row) => row.matched));

const resetRenderWindow = () => {
  visibleLimit.value = renderBatchSize;
};

const showMoreRows = () => {
  visibleLimit.value += renderBatchSize;
};

const sortObjectKeys = (value: JsonValue): JsonValue => {
  if (Array.isArray(value)) return value.map(sortObjectKeys);
  if (value && typeof value === "object") {
    return Object.keys(value)
      .sort((left, right) => left.localeCompare(right))
      .reduce<Record<string, JsonValue>>((result, key) => {
        result[key] = sortObjectKeys(value[key]);
        return result;
      }, {});
  }
  return value;
};

const collectStats = (value: JsonValue, depth = 1) => {
  if (Array.isArray(value)) {
    return value.reduce(
      (stats, item) => {
        const child = collectStats(item, depth + 1);
        return {
          keys: stats.keys + child.keys,
          arrays: stats.arrays + child.arrays,
          objects: stats.objects + child.objects,
          depth: Math.max(stats.depth, child.depth),
        };
      },
      { keys: 0, arrays: 1, objects: 0, depth },
    );
  }

  if (value && typeof value === "object") {
    return Object.entries(value).reduce(
      (stats, [, item]) => {
        const child = collectStats(item, depth + 1);
        return {
          keys: stats.keys + 1 + child.keys,
          arrays: stats.arrays + child.arrays,
          objects: stats.objects + child.objects,
          depth: Math.max(stats.depth, child.depth),
        };
      },
      { keys: 0, arrays: 0, objects: 1, depth },
    );
  }

  return { keys: 0, arrays: 0, objects: 0, depth };
};

const parseInput = (showToast = true) => {
  parseError.value = "";
  errorPreview.value = "";
  try {
    return JSON.parse(normalizeInput(inputJson.value)) as JsonValue;
  } catch (error) {
    const message = error instanceof Error ? error.message : "JSON 格式错误";
    parseError.value = message;
    errorPreview.value = buildErrorPreview(inputJson.value, message);
    if (showToast) ElMessage.error("JSON 格式错误，请检查逗号、引号或括号");
    return undefined;
  }
};

const normalizeInput = (value: string) => {
  let text = value.trim();
  text = text.replace(/\t/gm, "    ");
  text = text.replace(/\bundefined\b/gm, "null");
  text = text.replace(/ObjectId\((.*?)\)/gm, "$1");

  const firstObject = text.search(/[{[]/);
  const lastObject = Math.max(text.lastIndexOf("}"), text.lastIndexOf("]"));
  if (firstObject >= 0 && lastObject > firstObject) {
    text = text.slice(firstObject, lastObject + 1);
  }

  try {
    JSON.parse(text);
    return text;
  } catch {
    return text
      .replace(/([{,]\s*)'([^']+)'\s*:/g, '$1"$2":')
      .replace(/:\s*'([^']*)'/g, ': "$1"')
      .replace(/\bTrue\b/g, "true")
      .replace(/\bFalse\b/g, "false")
      .replace(/\bNone\b/g, "null");
  }
};

const buildErrorPreview = (value: string, message: string) => {
  const lines = value.split(/\r?\n/);
  const lineMatch = message.match(/line\s+(\d+)/i);
  const positionMatch = message.match(/position\s+(\d+)/i);
  let errorLine = lineMatch ? Number(lineMatch[1]) - 1 : -1;

  if (errorLine < 0 && positionMatch) {
    const position = Number(positionMatch[1]);
    errorLine = value.slice(0, position).split(/\r?\n/).length - 1;
  }

  return lines
    .map((line, index) => {
      const escaped = line.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
      const cls = index === errorLine ? "error-line" : "";
      return `<li class="${cls}">${escaped || " "}</li>`;
    })
    .join("");
};

const resetExpanded = () => {
  resetRenderWindow();
  expandedIds.value = new Set(
    treeRows.value
      .filter((row) => row.expandable && !isClose(row.depth + 1))
      .map((row) => row.id),
  );
};

const applyOutput = (value: JsonValue, compact = false) => {
  const target = sortKeys.value ? sortObjectKeys(value) : value;
  formattedOutput.value = JSON.stringify(target, null, compact ? 0 : indentSize.value);
  viewMode.value = compact ? "code" : "tree";
  resetRenderWindow();
  setTimeout(resetExpanded, 0);
};

const formatJson = (showToast = true) => {
  const parsed = parseInput(showToast);
  if (parsed === undefined) return;
  applyOutput(parsed);
  if (showToast) ElMessage.success("格式化完成");
};

const runLiveFormat = () => {
  window.clearTimeout(liveTimer);
  liveTimer = window.setTimeout(() => {
    const parsed = parseInput(false);
    if (parsed !== undefined) applyOutput(parsed);
  }, 260);
};

const sortAndFormat = () => {
  const parsed = parseInput();
  if (parsed === undefined) return;
  sortKeys.value = true;
  applyOutput(parsed);
  ElMessage.success("已按键名排序");
};

const minifyJson = () => {
  const parsed = parseInput();
  if (parsed === undefined) return;
  inputJson.value = JSON.stringify(sortKeys.value ? sortObjectKeys(parsed) : parsed);
  applyOutput(parsed, true);
  ElMessage.success("压缩完成");
};

const unescapeJson = () => {
  translation.value = !translation.value;
  if (translation.value) {
    formatJson();
  } else {
    inputJson.value = inputJson.value.replace(/\\"/gm, '"');
    formatJson();
  }
};

const toggleClosed = () => {
  closed.value = !closed.value;
  resetRenderWindow();
  if (closed.value) {
    expandedIds.value = new Set();
  } else {
    resetExpanded();
  }
};

const toggleNode = (id: string) => {
  const next = new Set(expandedIds.value);
  if (next.has(id)) next.delete(id);
  else next.add(id);
  expandedIds.value = next;
  resetRenderWindow();
};

const toggleFullscreen = () => {
  fullScreen.value = !fullScreen.value;
};

const decodeUrlInput = () => {
  try {
    inputJson.value = decodeURIComponent(inputJson.value);
    formatJson();
    ElMessage.success("URL 解码完成");
  } catch {
    ElMessage.error("URL 解码失败");
  }
};

const getValueByPath = (path: string, source: JsonValue | undefined = parsedOutput.value) => {
  if (source === undefined) return undefined;
  const tokens = parsePathTokens(path);

  return tokens.reduce<unknown>((current, token) => {
    if (current === undefined || current === null) return undefined;
    return (current as Record<string, unknown>)[token];
  }, source);
};

const stringifyNode = (value: unknown) => {
  if (value === undefined) return "";
  return typeof value === "string" ? value : JSON.stringify(value, null, 2);
};

const getRowNodeText = (row: TreeRow) => {
  return stringifyNode(getValueByPath(row.path));
};

const selectNode = (row: TreeRow) => {
  currentPath.value = row.path;
  currentDataText.value = getRowNodeText(row);
};

const queryJsonPath = () => {
  const source = parsedOutput.value ?? parseInput();
  if (source === undefined) return;

  const tokens = jsonPath.value
    ? parsePathTokens(jsonPath.value)
    : [];

  const result = tokens.length ? getValueByPath(jsonPath.value, source) : source;

  pathResult.value = result === undefined
    ? "未找到该路径"
    : typeof result === "string"
      ? result
      : JSON.stringify(result, null, 2);
};

const clearAll = () => {
  inputJson.value = "";
  formattedOutput.value = "";
  parseError.value = "";
  searchQuery.value = "";
  jsonPath.value = "";
  pathResult.value = "";
  expandedIds.value = new Set();
  currentPath.value = "";
  currentDataText.value = "";
};

const isClose = (currentDeep: number) => closed.value || currentDeep > deep.value;

const search = () => {
  deep.value = 100000;
  closed.value = false;
  hidenSearchBar.value = !hidenSearchBar.value;
  visibleLimit.value = renderBatchSize * 2;
  resetExpanded();
};

const selectNextMatch = () => {
  if (!matchRows.value.length) return;
  selectedMatchIndex.value = (selectedMatchIndex.value + 1) % matchRows.value.length;
  selectNode(matchRows.value[selectedMatchIndex.value]);
};

const selectPrevMatch = () => {
  if (!matchRows.value.length) return;
  selectedMatchIndex.value =
    (selectedMatchIndex.value - 1 + matchRows.value.length) % matchRows.value.length;
  selectNode(matchRows.value[selectedMatchIndex.value]);
};

const deepSelected = () => {
  const element = document.querySelector("#deep_input");
  if (!(element instanceof HTMLElement)) return;
  element.focus();
  document.execCommand("selectAll", false);
};

const verifyDeep = (value: string) => {
  const parsed = Number.parseInt(value, 10);
  deep.value = Number.isFinite(parsed) && parsed >= 0 ? parsed : 5;
  const element = document.querySelector("#deep_input");
  if (element) element.textContent = String(deep.value);
  resetRenderWindow();
  resetExpanded();
};

const downloadOutput = () => {
  if (!formattedOutput.value) {
    ElMessage.warning("当前没有可下载的结果");
    return;
  }
  const blob = new Blob([formattedOutput.value], { type: "application/json;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `myblob-json-${Date.now()}.json`;
  link.click();
  URL.revokeObjectURL(url);
  ElMessage.success("JSON 文件已生成");
};

const parsePathTokens = (path: string) => {
  const tokens: string[] = [];
  const source = path.trim().replace(/^\$\.?/, "");
  const pattern = /\["([^"]+)"\]|\[(\d+)\]|\.?([^.[\]]+)/g;
  let match: RegExpExecArray | null;
  while ((match = pattern.exec(source))) {
    tokens.push(match[1] ?? match[2] ?? match[3]);
  }
  return tokens.filter(Boolean);
};

const copyText = async (text: string, successMessage: string) => {
  if (!text) {
    ElMessage.warning("当前没有内容可复制");
    return;
  }
  await navigator.clipboard.writeText(text);
  ElMessage.success(successMessage);
};

watch(searchQuery, () => {
  selectedMatchIndex.value = 0;
  resetRenderWindow();
});

resetExpanded();
</script>

<style scoped>
.json-studio {
  display: grid;
  grid-template-columns: minmax(360px, 0.9fr) minmax(420px, 1.1fr);
  gap: 10px;
  height: calc(100vh - 186px);
  min-height: 620px;
}

.editor-pane,
.result-pane {
  display: grid;
  min-height: 0;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.editor-pane {
  grid-template-rows: auto minmax(0, 1fr) auto;
}

.result-pane {
  grid-template-rows: auto auto minmax(0, 1fr) auto;
}

.pane-toolbar,
.result-toolbar,
.pane-footer,
.query-bar {
  padding: 10px 12px;
}

.pane-toolbar,
.result-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
}

.pane-title {
  display: grid;
  gap: 3px;
}

.pane-title strong {
  color: #0f172a;
  font-size: 16px;
}

.pane-title span {
  color: #64748b;
  font-size: 12px;
}

.toolbar-actions,
.result-toolbar {
  flex-wrap: wrap;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.json-input {
  min-height: 0;
}

.json-input :deep(.el-textarea__inner) {
  height: 100%;
  min-height: 100% !important;
  border: 0;
  border-radius: 0;
  box-shadow: none;
  color: #334155;
  font-family: "JetBrains Mono", "Consolas", monospace;
  font-size: 15px;
  line-height: 1.65;
}

.pane-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid rgba(15, 23, 42, 0.08);
}

.result-toolbar {
  justify-content: flex-start;
  padding: 8px 10px;
}

.tool-action {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: 0;
  border-radius: 8px;
  padding: 7px 9px;
  background: transparent;
  color: #0f172a;
  cursor: pointer;
  font-size: 14px;
  font-weight: 800;
}

.tool-action:hover {
  background: #f1f5f9;
  color: #0f766e;
}

.tool-action svg {
  width: 16px;
  height: 16px;
}

.level-control {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #0f172a;
  font-size: 14px;
  font-weight: 800;
}

.deep-input {
  min-width: 34px;
  border-radius: 7px;
  padding: 2px 7px;
  background: #f8fafc;
  color: #0f766e;
  font-family: "JetBrains Mono", "Consolas", monospace;
  outline: 1px solid rgba(15, 23, 42, 0.12);
  text-align: center;
}

.deep-input:focus {
  background: #ecfeff;
  outline-color: rgba(20, 184, 166, 0.55);
}

.json-error {
  display: grid;
  gap: 8px;
  margin: 10px 12px 0;
  padding: 9px 11px;
  border-radius: 8px;
  background: rgba(239, 68, 68, 0.1);
  color: #b91c1c;
  font-size: 13px;
  font-weight: 700;
}

.error-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.error-code {
  max-height: 180px;
  margin: 0;
  overflow: auto;
  padding: 8px 8px 8px 32px;
  border-radius: 8px;
  background: #fff;
  color: #334155;
  font-family: "JetBrains Mono", "Consolas", monospace;
  font-size: 12px;
  font-weight: 500;
}

.error-code :deep(.error-line) {
  background: rgba(239, 68, 68, 0.16);
  color: #b91c1c;
  font-weight: 800;
}

.result-body {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  min-height: 0;
  overflow: hidden;
}

.result-body.fullscreen {
  position: fixed;
  z-index: 3000;
  inset: 16px;
  border: 1px solid rgba(15, 23, 42, 0.12);
  border-radius: 10px;
  background: white;
  box-shadow: 0 24px 80px rgba(15, 23, 42, 0.22);
}

.search-bar {
  display: grid;
  grid-template-columns: minmax(180px, 1fr) auto auto auto auto;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
  background: linear-gradient(90deg, #f8fafc, #ecfeff);
  color: #475569;
  font-size: 12px;
  font-weight: 800;
}

.result-tabs {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
}

.search-input {
  max-width: 280px;
}

.json-tree,
.code-output {
  min-height: 0;
  margin: 0;
  overflow: auto;
}

.json-tree {
  padding: 8px 0 16px;
  background: #fff;
  font-family: "JetBrains Mono", "Consolas", monospace;
  font-size: 14px;
  line-height: 1.8;
}

.tree-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  min-height: 28px;
  border-left: 1px dotted rgba(100, 116, 139, 0.32);
  color: #0f172a;
  white-space: nowrap;
}

.tree-row:hover,
.tree-row.matched {
  background: rgba(20, 184, 166, 0.08);
}

.toggle-btn,
.toggle-placeholder {
  width: 22px;
  flex: 0 0 auto;
}

.toggle-btn {
  border: 0;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  font-family: inherit;
}

.json-key {
  color: #9d174d;
  font-weight: 800;
}

.json-key[contenteditable="true"],
.json-value[contenteditable="true"] {
  border-radius: 4px;
  padding: 0 2px;
  outline: none;
}

.json-key[contenteditable="true"]:focus,
.json-value[contenteditable="true"]:focus {
  background: rgba(14, 165, 233, 0.12);
  box-shadow: 0 0 0 1px rgba(14, 165, 233, 0.3);
}

.colon {
  color: #0f172a;
  font-weight: 800;
}

.json-value {
  color: #0f172a;
  font-weight: 700;
}

.json-value.is-string {
  color: #16a34a;
}

.json-value.is-number {
  color: #0284c7;
}

.json-value.is-boolean {
  color: #ea580c;
}

.json-value.is-null {
  color: #f87171;
}

.json-meta {
  color: #94a3b8;
  font-size: 12px;
}

.row-actions {
  display: none;
  gap: 6px;
  padding-left: 8px;
  font-family: inherit;
}

.tree-row:hover .row-actions,
.tree-row.matched .row-actions {
  display: inline-flex;
}

.row-actions button,
.path-dock button {
  border: 0;
  background: transparent;
  color: #0f766e;
  cursor: pointer;
  font-size: 12px;
  font-weight: 800;
}

.load-more {
  display: block;
  width: calc(100% - 24px);
  margin: 12px;
  border: 1px dashed rgba(15, 118, 110, 0.35);
  border-radius: 10px;
  padding: 10px;
  background: #f0fdfa;
  color: #0f766e;
  cursor: pointer;
  font-weight: 900;
}

.load-more:hover {
  background: #ccfbf1;
}

.code-output {
  padding: 14px;
  background: #111827;
  color: #e5e7eb;
  font-family: "JetBrains Mono", "Consolas", monospace;
  font-size: 13px;
  line-height: 1.65;
  white-space: pre-wrap;
}

.empty-state {
  padding: 24px;
  color: #94a3b8;
}

.query-bar {
  display: grid;
  gap: 8px;
  border-top: 1px solid rgba(15, 23, 42, 0.08);
}

.path-result {
  max-height: 120px;
  margin: 0;
  overflow: auto;
  padding: 10px;
  border-radius: 8px;
  background: #f8fafc;
  color: #0f172a;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.path-dock {
  position: fixed;
  right: 24px;
  bottom: 16px;
  left: 24px;
  z-index: 3001;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border: 1px solid rgba(15, 23, 42, 0.12);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 16px 42px rgba(15, 23, 42, 0.18);
  color: #334155;
  font-size: 13px;
}

.path-dock span {
  overflow: hidden;
  flex: 1;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.path-dock strong {
  color: #0f172a;
  font-family: "JetBrains Mono", "Consolas", monospace;
}

@media (max-width: 980px) {
  .json-studio {
    grid-template-columns: 1fr;
    grid-template-rows: minmax(210px, 0.78fr) minmax(320px, 1.22fr);
    height: calc(100vh - 168px);
    min-height: 540px;
  }

  .editor-pane,
  .result-pane {
    min-height: 0;
  }

  .result-tabs {
    align-items: stretch;
    flex-direction: column;
  }

  .search-bar {
    grid-template-columns: 1fr 52px 1fr 1fr auto;
  }
}

@media (max-width: 640px) {
  .json-studio {
    height: calc(100vh - 152px);
    min-height: 560px;
  }

  .pane-toolbar,
  .result-toolbar,
  .pane-footer,
  .query-bar {
    padding: 8px;
  }

  .json-input :deep(.el-textarea__inner),
  .json-tree {
    font-size: 13px;
  }

  .search-bar {
    grid-template-columns: 1fr;
  }
}
</style>
