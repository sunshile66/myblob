<template>
  <ToolPageShell
    :title="activeTool.title"
    :description="activeTool.description"
    eyebrow="爬虫辅助"
    :meta="meta"
  >
    <div class="crawler-workbench">
      <aside class="tool-switcher">
        <button
          v-for="tool in tools"
          :key="tool.key"
          class="switch-item"
          :class="{ active: activeMode === tool.key }"
          type="button"
          @click="switchMode(tool.key)"
        >
          <strong>{{ tool.title }}</strong>
          <span>{{ tool.short }}</span>
        </button>
      </aside>

      <section class="panel input-panel">
        <div class="panel-head">
          <div>
            <h2>{{ activeTool.inputTitle }}</h2>
            <p>{{ activeTool.inputHint }}</p>
          </div>
          <div class="inline-actions">
            <el-button text @click="loadSample">示例</el-button>
            <el-button text @click="clearAll">清空</el-button>
          </div>
        </div>

        <el-input
          v-model="source"
          type="textarea"
          :rows="activeMode === 'html' ? 18 : 14"
          :placeholder="activeTool.placeholder"
        />

        <div v-if="activeMode === 'codec'" class="codec-actions">
          <el-segmented v-model="codecMode" :options="codecOptions" />
        </div>

        <div v-if="activeMode === 'share'" class="share-controls">
          <el-input v-model="shareTitle" placeholder="片段标题" />
          <el-select v-model="shareLanguage" style="width: 150px">
            <el-option label="JavaScript" value="javascript" />
            <el-option label="Python" value="python" />
            <el-option label="HTML" value="html" />
            <el-option label="Text" value="text" />
          </el-select>
        </div>
      </section>

      <section class="panel output-panel">
        <div class="panel-head">
          <div>
            <h2>处理结果</h2>
            <p>{{ activeTool.outputHint }}</p>
          </div>
          <div class="inline-actions">
            <el-segmented v-if="formatOptions.length > 1" v-model="outputFormat" :options="formatOptions" />
            <el-button :icon="DocumentCopy" @click="copy(output)">复制</el-button>
          </div>
        </div>

        <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" class="alert" />

        <iframe v-if="activeMode === 'html' && outputFormat === 'preview'" class="html-preview" :srcdoc="source" sandbox="" />
        <pre v-else class="result-block"><code>{{ output || "结果会显示在这里" }}</code></pre>
      </section>

      <section class="panel insight-panel">
        <div class="insight-card">
          <span>识别数量</span>
          <strong>{{ insightCount }}</strong>
        </div>
        <div class="insight-card insight-card--wide">
          <span>提示</span>
          <strong>{{ activeTool.tip }}</strong>
        </div>
      </section>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { DocumentCopy } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import ToolPageShell from "@features/tools/ui/ToolPageShell.vue";

type Mode = "headers" | "cookies" | "dict" | "url" | "codec" | "html" | "decoder" | "share";
type OutputFormat = "json" | "python" | "raw" | "query" | "preview" | "links" | "text" | "share";

interface ToolConfig {
  key: Mode;
  title: string;
  short: string;
  description: string;
  inputTitle: string;
  inputHint: string;
  outputHint: string;
  placeholder: string;
  tip: string;
  formats: Array<{ label: string; value: OutputFormat }>;
  sample: string;
}

const route = useRoute();
const router = useRouter();

const tools: ToolConfig[] = [
  {
    key: "headers",
    title: "Header 格式化",
    short: "请求头转对象",
    description: "将浏览器复制的请求头转换为 JSON、Python dict 或原始 headers。",
    inputTitle: "原始请求头",
    inputHint: "每行一个 Header，支持冒号分隔和多余空白。",
    outputHint: "可直接复制到 requests、fetch 或爬虫配置中。",
    placeholder: "User-Agent: Mozilla/5.0\nAccept: application/json",
    tip: "从浏览器 Network 面板复制 request headers 后粘贴即可。",
    formats: [
      { label: "JSON", value: "json" },
      { label: "Python", value: "python" },
      { label: "Raw", value: "raw" },
    ],
    sample: "User-Agent: Mozilla/5.0\nAccept: application/json\nReferer: https://example.com\nX-Requested-With: XMLHttpRequest",
  },
  {
    key: "cookies",
    title: "Cookie 格式化",
    short: "Cookie 转 dict",
    description: "解析 Cookie header，生成 JSON、Python dict 或重新拼接后的 Cookie 字符串。",
    inputTitle: "Cookie 字符串",
    inputHint: "支持 session=a; token=b 形式，会自动 URL decode。",
    outputHint: "适合复用登录态或排查重复 Cookie。",
    placeholder: "sessionid=abc123; csrftoken=token-value",
    tip: "敏感 Cookie 不要提交到远程仓库，只在本地临时使用。",
    formats: [
      { label: "JSON", value: "json" },
      { label: "Python", value: "python" },
      { label: "Cookie", value: "raw" },
    ],
    sample: "sessionid=abc123; csrftoken=token-value; theme=light",
  },
  {
    key: "dict",
    title: "Dict 转换",
    short: "JSON / dict / query",
    description: "把键值对、JSON、Python dict 和 query 参数互相转换。",
    inputTitle: "结构化文本",
    inputHint: "可粘贴 JSON、Python dict、a=1&b=2 或每行 key: value。",
    outputHint: "输出为 JSON、Python dict 或 query string。",
    placeholder: "{'page': 1, 'keyword': 'myblob'}",
    tip: "Python dict 会被尽量转换为 JSON 再解析，复杂表达式不执行。",
    formats: [
      { label: "JSON", value: "json" },
      { label: "Python", value: "python" },
      { label: "Query", value: "query" },
    ],
    sample: "page=1&keyword=myblob&sort=created_at",
  },
  {
    key: "url",
    title: "URL 参数提取",
    short: "拆 URL 和 query",
    description: "拆解 URL 的协议、域名、路径和 query 参数，并生成 params 对象。",
    inputTitle: "完整 URL",
    inputHint: "粘贴带 query 的 URL，会自动解析重复参数。",
    outputHint: "输出 URL 结构或 params 对象。",
    placeholder: "https://example.com/search?q=myblob&page=1",
    tip: "重复 query 参数会合并成数组，方便调试列表接口。",
    formats: [
      { label: "JSON", value: "json" },
      { label: "Python", value: "python" },
      { label: "Query", value: "query" },
    ],
    sample: "https://spidertools.cn/search?keyword=header&page=1&page_size=20",
  },
  {
    key: "codec",
    title: "URL 编解码",
    short: "encode/decode",
    description: "对 URL、URL 组件、Base64 和 HTML 实体进行批量编码解码。",
    inputTitle: "待处理文本",
    inputHint: "选择处理方式后会实时生成结果。",
    outputHint: "适合处理接口参数、跳转链接和页面实体。",
    placeholder: "https://example.com/search?q=中文 空格",
    tip: "URL 组件模式适合单个 query value，完整 URL 模式会保留协议与路径。",
    formats: [{ label: "结果", value: "raw" }],
    sample: "https://example.com/search?q=中文 空格&from=工具",
  },
  {
    key: "html",
    title: "HTML 渲染",
    short: "预览和提取",
    description: "预览 HTML 片段，并提取链接、图片地址和纯文本内容。",
    inputTitle: "HTML 源码",
    inputHint: "iframe 使用 sandbox 预览，脚本不会执行。",
    outputHint: "可在预览、链接和纯文本之间切换。",
    placeholder: "<article><a href=\"/post/1\">文章</a><img src=\"/cover.png\"></article>",
    tip: "用于快速检查采集到的 HTML 片段，不适合作为完整浏览器环境。",
    formats: [
      { label: "预览", value: "preview" },
      { label: "链接", value: "links" },
      { label: "文本", value: "text" },
    ],
    sample: "<article><h1>MyBlob</h1><a href=\"/post/1\">查看文章</a><img src=\"/cover.png\"><p>工具中心</p></article>",
  },
  {
    key: "decoder",
    title: "文本解码",
    short: "Unicode/Base64/Hex",
    description: "处理 Unicode 转义、Base64、十六进制和 HTML 实体。",
    inputTitle: "编码文本",
    inputHint: "会尝试输出多种解码结果，便于判断页面混淆内容。",
    outputHint: "无法解码的格式会显示错误原因。",
    placeholder: "\\u4f60\\u597d 或 48656c6c6f 或 SGVsbG8=",
    tip: "多结果对照能快速判断文本到底是哪种编码。",
    formats: [{ label: "全部", value: "raw" }],
    sample: "\\u5de5\\u5177\\u4e2d\\u5fc3\nSGVsbG8gTXlCbG9i\n48656c6c6f",
  },
  {
    key: "share",
    title: "代码分享",
    short: "本地分享链接",
    description: "将短代码片段编码到 URL hash 中，生成可复制的本地分享链接。",
    inputTitle: "代码片段",
    inputHint: "适合临时传递示例，不会上传到服务器。",
    outputHint: "生成的链接只包含当前片段内容。",
    placeholder: "console.log('hello myblob')",
    tip: "链接长度受浏览器限制，建议只分享短片段。",
    formats: [{ label: "链接", value: "share" }],
    sample: "import requests\n\nresponse = requests.get('https://example.com')\nprint(response.text)",
  },
];

const modeFromRoute = () => (route.meta.crawlerMode as Mode | undefined) || "headers";
const activeMode = ref<Mode>(modeFromRoute());
const source = ref("");
const outputFormat = ref<OutputFormat>("json");
const codecMode = ref("decodeURIComponent");
const shareTitle = ref("MyBlob 代码片段");
const shareLanguage = ref("javascript");
const manualError = ref("");

const codecOptions = [
  { label: "URL 解码", value: "decodeURIComponent" },
  { label: "URL 编码", value: "encodeURIComponent" },
  { label: "完整 URL 编码", value: "encodeURI" },
  { label: "Base64 编码", value: "base64Encode" },
  { label: "Base64 解码", value: "base64Decode" },
  { label: "HTML 实体解码", value: "htmlDecode" },
];

const activeTool = computed(() => tools.find((tool) => tool.key === activeMode.value) || tools[0]);
const formatOptions = computed(() => activeTool.value.formats);

const parsedResult = computed(() => {
  try {
    if (activeMode.value === "headers") return { record: parseColonLines(source.value), error: "" };
    if (activeMode.value === "cookies") return { record: parseCookies(source.value), error: "" };
    if (activeMode.value === "dict") return { record: parseLooseDict(source.value), error: "" };
    if (activeMode.value === "url") return { record: parseUrl(source.value), error: "" };
    return { record: {}, error: "" };
  } catch (error) {
    return {
      record: {},
      error: error instanceof Error ? error.message : "解析失败",
    };
  }
});

const parsedRecord = computed(() => parsedResult.value.record);
const errorMessage = computed(() => manualError.value || parsedResult.value.error);
const htmlInfo = computed(() =>
  activeMode.value === "html"
    ? extractHtml(source.value)
    : { links: [] as string[], images: [] as string[], text: "" },
);

const insightCount = computed(() => {
  if (activeMode.value === "html") return htmlInfo.value.links.length + htmlInfo.value.images.length;
  if (activeMode.value === "decoder") return decoderResults.value.length;
  if (activeMode.value === "share") return source.value.length;
  return Object.keys(parsedRecord.value).length;
});

const output = computed(() => {
  if (!source.value.trim() && activeMode.value !== "share") return "";
  if (activeMode.value === "codec") return runCodec(source.value);
  if (activeMode.value === "html") return runHtmlOutput();
  if (activeMode.value === "decoder") return decoderResults.value.join("\n\n");
  if (activeMode.value === "share") return buildShareLink();

  const record = parsedRecord.value;
  if (outputFormat.value === "python") return toPythonDict(record);
  if (outputFormat.value === "query") return toQuery(record);
  if (outputFormat.value === "raw") {
    if (activeMode.value === "cookies") return toCookieString(record);
    return toRawHeaders(record);
  }
  return JSON.stringify(record, null, 2);
});

const meta = computed(() => [
  { label: "当前工具", value: activeTool.value.title },
  { label: "识别数量", value: `${insightCount.value}` },
  { label: "输出格式", value: formatOptions.value.find((item) => item.value === outputFormat.value)?.label || "结果" },
]);

const parseColonLines = (value: string) => {
  return value
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)
    .reduce<Record<string, string>>((result, line) => {
      const index = line.indexOf(":");
      if (index <= 0) return result;
      result[line.slice(0, index).trim()] = line.slice(index + 1).trim();
      return result;
    }, {});
};

const parseCookies = (value: string) => {
  return value.split(";").reduce<Record<string, string>>((result, part) => {
    const index = part.indexOf("=");
    if (index <= 0) return result;
    const key = part.slice(0, index).trim();
    const rawValue = part.slice(index + 1).trim();
    result[key] = safeDecode(rawValue);
    return result;
  }, {});
};

const parseLooseDict = (value: string) => {
  const text = value.trim();
  if (!text) return {};
  if (text.includes("=") && !text.startsWith("{")) {
    return Object.fromEntries(new URLSearchParams(text));
  }

  try {
    return JSON.parse(text);
  } catch {
    const normalized = text
      .replace(/'/g, '"')
      .replace(/\bTrue\b/g, "true")
      .replace(/\bFalse\b/g, "false")
      .replace(/\bNone\b/g, "null");
    try {
      return JSON.parse(normalized);
    } catch {
      return parseColonLines(text);
    }
  }
};

const parseUrl = (value: string) => {
  const url = new URL(value.trim());
  const params: Record<string, string | string[]> = {};
  url.searchParams.forEach((paramValue, key) => {
    const existing = params[key];
    if (Array.isArray(existing)) existing.push(paramValue);
    else if (existing !== undefined) params[key] = [existing, paramValue];
    else params[key] = paramValue;
  });
  return {
    protocol: url.protocol.replace(":", ""),
    host: url.host,
    pathname: url.pathname,
    hash: url.hash.replace("#", ""),
    params,
  };
};

const safeDecode = (value: string) => {
  try {
    return decodeURIComponent(value);
  } catch {
    return value;
  }
};

const toPythonDict = (record: unknown) => {
  return JSON.stringify(record, null, 2)
    .replace(/"([^"]+)":/g, "'$1':")
    .replace(/: "([^"]*)"/g, ": '$1'")
    .replace(/\btrue\b/g, "True")
    .replace(/\bfalse\b/g, "False")
    .replace(/\bnull\b/g, "None");
};

const toQuery = (record: unknown) => {
  const target = activeMode.value === "url" ? (record as { params?: Record<string, string> }).params || {} : record;
  return new URLSearchParams(flatRecord(target as Record<string, string | string[]>)).toString();
};

const flatRecord = (record: Record<string, string | string[]>) => {
  const params = new URLSearchParams();
  Object.entries(record).forEach(([key, value]) => {
    if (Array.isArray(value)) value.forEach((item) => params.append(key, item));
    else params.append(key, String(value));
  });
  return params;
};

const toRawHeaders = (record: unknown) =>
  Object.entries(record as Record<string, string>).map(([key, value]) => `${key}: ${value}`).join("\n");

const toCookieString = (record: unknown) =>
  Object.entries(record as Record<string, string>).map(([key, value]) => `${key}=${encodeURIComponent(value)}`).join("; ");

const runCodec = (value: string) => {
  try {
    if (codecMode.value === "decodeURIComponent") return decodeURIComponent(value);
    if (codecMode.value === "encodeURIComponent") return encodeURIComponent(value);
    if (codecMode.value === "encodeURI") return encodeURI(value);
    if (codecMode.value === "base64Encode") return btoa(unescape(encodeURIComponent(value)));
    if (codecMode.value === "base64Decode") return decodeURIComponent(escape(atob(value)));
    if (codecMode.value === "htmlDecode") return htmlToText(value);
    return value;
  } catch (error) {
    return error instanceof Error ? error.message : "处理失败";
  }
};

const extractHtml = (html: string) => {
  const doc = new DOMParser().parseFromString(html, "text/html");
  return {
    links: Array.from(doc.querySelectorAll("a[href]")).map((item) => (item as HTMLAnchorElement).getAttribute("href") || ""),
    images: Array.from(doc.querySelectorAll("img[src]")).map((item) => (item as HTMLImageElement).getAttribute("src") || ""),
    text: doc.body.textContent?.trim() || "",
  };
};

const runHtmlOutput = () => {
  if (outputFormat.value === "links") {
    return [
      "Links:",
      ...htmlInfo.value.links.map((link) => `- ${link}`),
      "",
      "Images:",
      ...htmlInfo.value.images.map((image) => `- ${image}`),
    ].join("\n");
  }
  if (outputFormat.value === "text") return htmlInfo.value.text;
  return "";
};

const htmlToText = (value: string) => new DOMParser().parseFromString(value, "text/html").documentElement.textContent || "";

const decodeUnicodeEscapes = (value: string) =>
  value.replace(/\\u([\dA-Fa-f]{4})/g, (_, code) => String.fromCharCode(Number.parseInt(code, 16)));

const decodeHex = (value: string) => {
  const clean = value.replace(/[^0-9A-Fa-f]/g, "");
  if (!clean || clean.length % 2) throw new Error("十六进制长度不是偶数");
  return clean.match(/.{2}/g)?.map((chunk) => String.fromCharCode(Number.parseInt(chunk, 16))).join("") || "";
};

const decoderResults = computed(() => {
  if (!source.value.trim()) return [];
  const attempts: Array<[string, () => string]> = [
    ["Unicode 转义", () => decodeUnicodeEscapes(source.value)],
    ["URL 解码", () => decodeURIComponent(source.value)],
    ["Base64 解码", () => decodeURIComponent(escape(atob(source.value.trim())))],
    ["十六进制解码", () => decodeHex(source.value)],
    ["HTML 实体解码", () => htmlToText(source.value)],
  ];

  return attempts.map(([label, action]) => {
    try {
      return `${label}:\n${action()}`;
    } catch (error) {
      return `${label}:\n${error instanceof Error ? error.message : "解码失败"}`;
    }
  });
});

const buildShareLink = () => {
  if (!source.value.trim()) return "";
  const payload = btoa(unescape(encodeURIComponent(JSON.stringify({
    title: shareTitle.value,
    language: shareLanguage.value,
    code: source.value,
  }))));
  return `${window.location.origin}/tools/code-share#${payload}`;
};

const loadFromHash = () => {
  if (activeMode.value !== "share" || !window.location.hash.slice(1)) return;
  try {
    const payload = JSON.parse(decodeURIComponent(escape(atob(window.location.hash.slice(1)))));
    shareTitle.value = payload.title || "MyBlob 代码片段";
    shareLanguage.value = payload.language || "text";
    source.value = payload.code || "";
  } catch {
    // Ignore malformed local share links.
  }
};

const copy = async (value: string) => {
  if (!value) {
    ElMessage.warning("没有内容可复制");
    return;
  }
  await navigator.clipboard.writeText(value);
  ElMessage.success("已复制");
};

const switchMode = (mode: Mode) => {
  const routeMap: Record<Mode, string> = {
    headers: "header-formatter",
    cookies: "cookie-formatter",
    dict: "dict-formatter",
    url: "url-params",
    codec: "url-codec",
    html: "html-renderer",
    decoder: "text-decoder",
    share: "code-share",
  };
  router.push(`/tools/${routeMap[mode]}`);
};

const setDefaults = () => {
  activeMode.value = modeFromRoute();
  outputFormat.value = activeTool.value.formats[0]?.value || "json";
  if (!source.value || activeMode.value === "share") {
    source.value = activeTool.value.sample;
  }
  loadFromHash();
};

const loadSample = () => {
  source.value = activeTool.value.sample;
};

const clearAll = () => {
  source.value = "";
  manualError.value = "";
};

watch(() => route.meta.crawlerMode, setDefaults, { immediate: true });
</script>

<style scoped>
.crawler-workbench {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.tool-switcher,
.panel {
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.tool-switcher {
  display: grid;
  gap: 6px;
  padding: 8px;
  position: sticky;
  top: 92px;
}

.switch-item {
  display: grid;
  gap: 4px;
  padding: 10px;
  border: 1px solid transparent;
  border-radius: 8px;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.switch-item:hover,
.switch-item.active {
  border-color: rgba(20, 184, 166, 0.28);
  background: rgba(20, 184, 166, 0.08);
}

.switch-item strong {
  color: #0f172a;
  font-size: 13px;
}

.switch-item span {
  color: #64748b;
  font-size: 12px;
}

.panel {
  padding: 16px;
}

.insight-panel {
  grid-column: 2 / -1;
  display: grid;
  grid-template-columns: 160px minmax(0, 1fr);
  gap: 12px;
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
  line-height: 1.6;
}

.inline-actions,
.codec-actions,
.share-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

.codec-actions,
.share-controls {
  justify-content: flex-start;
  margin-top: 12px;
}

.alert {
  margin-bottom: 12px;
}

.result-block,
.html-preview {
  width: 100%;
  min-height: 430px;
  margin: 0;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
  background: #111827;
}

.result-block {
  padding: 14px;
  overflow: auto;
  color: #e5e7eb;
  font-size: 13px;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
}

.html-preview {
  background: white;
}

.insight-card {
  padding: 12px;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid rgba(15, 23, 42, 0.08);
}

.insight-card span,
.insight-card strong {
  display: block;
}

.insight-card span {
  color: #64748b;
  font-size: 12px;
}

.insight-card strong {
  margin-top: 4px;
  color: #0f172a;
  font-size: 15px;
  line-height: 1.5;
}

@media (max-width: 1180px) {
  .crawler-workbench {
    grid-template-columns: 1fr;
  }

  .tool-switcher {
    position: static;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .insight-panel {
    grid-column: auto;
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .tool-switcher {
    grid-template-columns: 1fr;
  }

  .panel-head {
    flex-direction: column;
  }
}
</style>
