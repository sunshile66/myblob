<template>
  <ToolPageShell
    :title="pageTitle"
    :description="pageDescription"
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
          <el-segmented v-model="language" :options="languageOptions" @change="loadSample" />
          <el-select v-model="indentSize" style="width: 120px">
            <el-option label="2 空格" :value="2" />
            <el-option label="4 空格" :value="4" />
          </el-select>
          <el-select v-if="language === 'javascript'" v-model="jsMode" style="width: 160px">
            <el-option label="格式化" value="format" />
            <el-option label="去注释" value="stripComments" />
            <el-option label="Unicode 转义" value="unicodeEscape" />
            <el-option label="Unicode 还原" value="unicodeUnescape" />
            <el-option label="字符串混淆" value="stringArray" />
            <el-option label="fromCharCode 还原" value="fromCharCode" />
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
import { computed, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { DocumentCopy } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import ToolPageShell from "@features/tools/ui/ToolPageShell.vue";

type Language = "javascript" | "html" | "css";

const samples: Record<Language, string> = {
  javascript: '(function test(){console.log("hello word")})()',
  html: "<article><h1>MyBlob</h1><p class=\"lead\">工具中心</p><a href=\"/tools\">打开</a></article>",
  css: ".card{display:flex;gap:12px;color:#0f172a}.card:hover{transform:translateY(-2px)}",
};

const route = useRoute();
const language = ref<Language>("javascript");
const source = ref(samples.javascript);
const indentSize = ref(2);
const minify = ref(false);
const jsMode = ref<"format" | "stripComments" | "unicodeEscape" | "unicodeUnescape" | "stringArray" | "fromCharCode">("format");

const languageOptions = [
  { label: "JavaScript", value: "javascript" },
  { label: "HTML", value: "html" },
  { label: "CSS", value: "css" },
];

const pageTitle = computed(() => {
  if (language.value === "html") return "HTML 格式化";
  if (language.value === "css") return "CSS 格式化";
  return "JS 格式化";
});

const pageDescription = computed(() => {
  if (language.value === "html") return "参考 spidertools 的 HTML 格式化入口，输入即格式化并保留源码层级。";
  if (language.value === "css") return "格式化或压缩 CSS 片段，适合清理页面样式源码。";
  return "参考 spidertools 的 JS 格式化逻辑，输入压缩 JS 后输出更易阅读的代码。";
});

const indent = computed(() => " ".repeat(indentSize.value));

const minifyCode = (value: string) => {
  if (language.value === "html") return value.replace(/>\s+</g, "><").trim();
  if (language.value === "css") return value.replace(/\/\*[\s\S]*?\*\//g, "").replace(/\s*([{}:;,>])\s*/g, "$1").trim();
  return stripJsComments(value).replace(/\s*([{}()[\],;:+\-*/%=<>?])\s*/g, "$1").replace(/\s+/g, " ").trim();
};

const stripJsComments = (value: string) => {
  let output = "";
  let quote = "";
  let escaped = false;
  let blockComment = false;
  let lineComment = false;

  for (let index = 0; index < value.length; index += 1) {
    const current = value[index];
    const next = value[index + 1] || "";

    if (lineComment) {
      if (current === "\n") {
        lineComment = false;
        output += current;
      }
      continue;
    }
    if (blockComment) {
      if (current === "*" && next === "/") {
        blockComment = false;
        index += 1;
      }
      continue;
    }
    if (quote) {
      output += current;
      if (escaped) {
        escaped = false;
      } else if (current === "\\") {
        escaped = true;
      } else if (current === quote) {
        quote = "";
      }
      continue;
    }
    if (current === "\"" || current === "'" || current === "`") {
      quote = current;
      output += current;
      continue;
    }
    if (current === "/" && next === "/") {
      lineComment = true;
      index += 1;
      continue;
    }
    if (current === "/" && next === "*") {
      blockComment = true;
      index += 1;
      continue;
    }
    output += current;
  }

  return output;
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

const formatCss = (value: string) => {
  return formatBracedCode(value)
    .replace(/\s*:\s*/g, ": ")
    .replace(/;\n/g, ";\n")
    .replace(/\{\n/g, " {\n");
};

const unicodeEscape = (value: string) =>
  Array.from(value).map((char) => {
    const code = char.codePointAt(0) || 0;
    if (code < 128) return char;
    return code <= 0xffff
      ? `\\u${code.toString(16).padStart(4, "0")}`
      : `\\u{${code.toString(16)}}`;
  }).join("");

const unicodeUnescape = (value: string) =>
  value
    .replace(/\\u\{([0-9a-f]+)\}/gi, (_, code) => String.fromCodePoint(Number.parseInt(code, 16)))
    .replace(/\\u([0-9a-f]{4})/gi, (_, code) => String.fromCharCode(Number.parseInt(code, 16)))
    .replace(/\\x([0-9a-f]{2})/gi, (_, code) => String.fromCharCode(Number.parseInt(code, 16)));

const restoreFromCharCode = (value: string) =>
  value.replace(/String\.fromCharCode\(([\d,\s]+)\)/g, (_, list) =>
    JSON.stringify(
      String.fromCharCode(
        ...String(list)
          .split(",")
          .map((item) => Number.parseInt(item.trim(), 10))
          .filter((item) => Number.isFinite(item)),
      ),
    ),
  );

const obfuscateStringArray = (value: string) => {
  const strings: string[] = [];
  const body = value.replace(/(["'])(?:(?=(\\?))\2.)*?\1/g, (match) => {
    const index = strings.length;
    strings.push(match);
    return `__myblob_strings[${index}]`;
  });
  if (!strings.length) return value;
  return `const __myblob_strings = [${strings.join(", ")}];\n${body}`;
};

const runJsMode = (value: string) => {
  if (jsMode.value === "stripComments") return stripJsComments(value).trim();
  if (jsMode.value === "unicodeEscape") return unicodeEscape(value);
  if (jsMode.value === "unicodeUnescape") return unicodeUnescape(value);
  if (jsMode.value === "fromCharCode") return restoreFromCharCode(unicodeUnescape(value));
  if (jsMode.value === "stringArray") return obfuscateStringArray(minifyCode(value));
  return formatBracedCode(value)
    .replace(/\)\s*\{/g, ") {")
    .replace(/\b(function|if|for|while|switch)\s*\(/g, "$1 (");
};

const output = computed(() => {
  const value = source.value.trim();
  if (!value) return "";
  if (minify.value) return minifyCode(value);
  if (language.value === "html") return formatHtml(value);
  if (language.value === "css") return formatCss(value);
  return runJsMode(value);
});

const lineCount = computed(() => (output.value ? output.value.split(/\r?\n/).length : 0));
const statusText = computed(() => `${source.value.length} 字符输入，${output.value.length} 字符输出`);

const meta = computed(() => [
  { label: "语言", value: languageOptions.find((item) => item.value === language.value)?.label || "Code" },
  { label: "行数", value: `${lineCount.value}` },
  { label: "模式", value: language.value === "javascript" && !minify.value ? jsMode.value : minify.value ? "压缩" : "格式化" },
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

watch(
  () => route.meta.codeMode,
  (mode) => {
    if (mode === "html") language.value = "html";
    else if (mode === "css") language.value = "css";
    else language.value = "javascript";
    jsMode.value = mode === "obfuscator" ? "stringArray" : "format";
    loadSample();
  },
  { immediate: true },
);
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
