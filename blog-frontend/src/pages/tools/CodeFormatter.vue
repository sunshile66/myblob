<template>
  <ToolPageShell
    title="JS / HTML 格式化"
    description="轻量格式化 JavaScript、HTML 和 CSS 片段，支持压缩、缩进调整与结构统计。"
    eyebrow="爬虫辅助"
    :meta="meta"
  >
    <div class="formatter-grid">
      <section class="panel">
        <div class="panel-head">
          <div>
            <h2>源代码</h2>
            <p>适合处理抓包、页面源码和临时代码片段。</p>
          </div>
          <div class="actions">
            <el-button text @click="loadSample">示例</el-button>
            <el-button text @click="source = ''">清空</el-button>
          </div>
        </div>

        <div class="settings">
          <el-segmented v-model="language" :options="languageOptions" />
          <el-select v-model="indentSize" style="width: 120px">
            <el-option label="2 空格" :value="2" />
            <el-option label="4 空格" :value="4" />
          </el-select>
          <el-switch v-model="minify" active-text="压缩" inactive-text="格式化" />
        </div>

        <el-input v-model="source" type="textarea" :rows="22" placeholder="粘贴 JS、HTML 或 CSS" />
      </section>

      <section class="panel">
        <div class="panel-head">
          <div>
            <h2>处理结果</h2>
            <p>{{ statusText }}</p>
          </div>
          <el-button :icon="DocumentCopy" @click="copyResult">复制</el-button>
        </div>

        <pre class="code-output"><code>{{ output || "结果会显示在这里" }}</code></pre>
      </section>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { DocumentCopy } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import ToolPageShell from "@features/tools/ui/ToolPageShell.vue";

type Language = "javascript" | "html" | "css";

const samples: Record<Language, string> = {
  javascript: "function demo(name){const items=[1,2,3];return items.map(item=>({name,value:item}))}\nconsole.log(demo('myblob'))",
  html: "<article><h1>MyBlob</h1><p class=\"lead\">工具中心</p><a href=\"/tools\">打开</a></article>",
  css: ".card{display:flex;gap:12px;color:#0f172a}.card:hover{transform:translateY(-2px)}",
};

const language = ref<Language>("javascript");
const source = ref(samples.javascript);
const indentSize = ref(2);
const minify = ref(false);

const languageOptions = [
  { label: "JavaScript", value: "javascript" },
  { label: "HTML", value: "html" },
  { label: "CSS", value: "css" },
];

const indent = computed(() => " ".repeat(indentSize.value));

const minifyCode = (value: string) => {
  if (language.value === "html") return value.replace(/>\s+</g, "><").trim();
  if (language.value === "css") return value.replace(/\/\*[\s\S]*?\*\//g, "").replace(/\s*([{}:;,>])\s*/g, "$1").trim();
  return value.replace(/\/\/.*$/gm, "").replace(/\s*([{}()[\],;:+\-*/%=<>?])\s*/g, "$1").replace(/\s+/g, " ").trim();
};

const formatHtml = (value: string) => {
  const tokens = value.replace(/>\s*</g, "><").split(/(?=<)|(?<=>)/g).filter(Boolean);
  let depth = 0;

  return tokens
    .map((token) => {
      const trimmed = token.trim();
      if (!trimmed) return "";
      if (/^<\//.test(trimmed)) depth = Math.max(0, depth - 1);
      const line = `${indent.value.repeat(depth)}${trimmed}`;
      if (/^<[^!/][^>]*[^/]>\s*$/.test(trimmed) && !/^<(area|base|br|col|embed|hr|img|input|link|meta|param|source|track|wbr)\b/i.test(trimmed)) {
        depth += 1;
      }
      return line;
    })
    .filter(Boolean)
    .join("\n");
};

const formatBracedCode = (value: string) => {
  const normalized = value
    .replace(/\s*([{};])\s*/g, "$1\n")
    .replace(/\s*,\s*/g, ", ")
    .replace(/\n+/g, "\n")
    .trim();
  let depth = 0;

  return normalized
    .split("\n")
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => {
      if (line.startsWith("}")) depth = Math.max(0, depth - 1);
      const current = `${indent.value.repeat(depth)}${line}`;
      if (line.endsWith("{")) depth += 1;
      return current;
    })
    .join("\n");
};

const output = computed(() => {
  const value = source.value.trim();
  if (!value) return "";
  if (minify.value) return minifyCode(value);
  if (language.value === "html") return formatHtml(value);
  if (language.value === "css") return formatBracedCode(value);
  return formatBracedCode(value)
    .replace(/\)\s*\{/g, ") {")
    .replace(/\b(function|if|for|while|switch)\s*\(/g, "$1 (");
});

const lineCount = computed(() => (output.value ? output.value.split(/\r?\n/).length : 0));
const statusText = computed(() => `${source.value.length} 字符输入，${output.value.length} 字符输出`);

const meta = computed(() => [
  { label: "语言", value: languageOptions.find((item) => item.value === language.value)?.label || "Code" },
  { label: "行数", value: `${lineCount.value}` },
  { label: "模式", value: minify.value ? "压缩" : "格式化" },
]);

const loadSample = () => {
  source.value = samples[language.value];
};

const copyResult = async () => {
  if (!output.value) {
    ElMessage.warning("没有可复制的结果");
    return;
  }
  await navigator.clipboard.writeText(output.value);
  ElMessage.success("已复制");
};
</script>

<style scoped>
.formatter-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.panel {
  padding: 16px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.04);
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

h2 {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
}

p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
}

.actions,
.settings {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.settings {
  justify-content: flex-start;
  margin-bottom: 12px;
}

.code-output {
  min-height: 520px;
  margin: 0;
  padding: 14px;
  overflow: auto;
  border-radius: 8px;
  background: #111827;
  color: #e5e7eb;
  font-size: 13px;
  line-height: 1.65;
  white-space: pre-wrap;
}

@media (max-width: 960px) {
  .formatter-grid {
    grid-template-columns: 1fr;
  }

  .panel-head {
    flex-direction: column;
  }

  .actions {
    justify-content: flex-start;
  }
}
</style>
