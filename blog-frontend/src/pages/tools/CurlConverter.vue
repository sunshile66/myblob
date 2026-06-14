<template>
  <ToolPageShell
    title="curl 转代码"
    description="基于 curlconverter 开源核心重写，支持多语言转换、请求摘要、警告提示。"
    eyebrow="开发调试"
    :meta="meta"
  >
    <div class="curl-lab">
      <section class="panel input-panel">
        <header class="panel-head">
          <div>
            <h2>curl 命令</h2>
            <p>支持浏览器复制的 bash/zsh curl，多 header、cookie、query、JSON、表单、文件上传和常见 curl 参数。</p>
          </div>
          <div class="head-actions">
            <el-switch v-model="autoConvert" active-text="自动转换" />
            <el-button text :icon="Delete" @click="clearAll">清空</el-button>
          </div>
        </header>

        <el-input
          v-model="curlInput"
          class="curl-input"
          type="textarea"
          resize="none"
          spellcheck="false"
          :placeholder="placeholder"
          @keydown.ctrl.enter.prevent="convert"
          @keydown.meta.enter.prevent="convert"
        />

        <div class="input-actions">
          <el-button type="primary" @click="convert">转换 Ctrl/⌘ + Enter</el-button>
          <el-button @click="copy(curlInput)">复制 curl</el-button>
          <el-button @click="loadBrowserExample">浏览器示例</el-button>
        </div>

        <div class="example-grid">
          <button v-for="item in examples" :key="item.title" class="example-card" @click="useExample(item.curl)">
            <strong>{{ item.title }}</strong>
            <span>{{ item.description }}</span>
          </button>
        </div>
      </section>

      <aside class="panel target-panel">
        <header class="panel-head compact-head">
          <div>
            <h2>输出目标</h2>
            <p>{{ activeConverter?.description || "选择一个转换目标。" }}</p>
          </div>
        </header>

        <el-input v-model="targetKeyword" clearable placeholder="搜索 Python / Go / HAR..." />

        <div class="target-list">
          <button
            v-for="item in filteredTargets"
            :key="item.value"
            class="target-item"
            :class="{ active: target === item.value }"
            @click="target = item.value"
          >
            <span>{{ item.label }}</span>
            <small>{{ item.group }}</small>
          </button>
        </div>
      </aside>

      <section class="panel output-panel">
        <header class="panel-head">
          <div>
            <h2>输出代码</h2>
            <p>转换结果在浏览器本地生成，不上传 curl 内容。</p>
          </div>
          <div class="head-actions">
            <el-button :icon="DocumentCopy" @click="copy(outputCode)">复制结果</el-button>
            <el-button @click="downloadOutput">下载</el-button>
          </div>
        </header>

        <el-alert v-if="parseError" :title="parseError" type="error" show-icon :closable="false" class="alert" />

        <div v-if="warnings.length" class="warning-box">
          <strong>转换提示</strong>
          <ul>
            <li v-for="(warning, index) in warnings" :key="`${warning[0]}-${index}`">
              <span>{{ warning[0] }}</span>
              <em>{{ warning[1] }}</em>
            </li>
          </ul>
        </div>

        <pre class="code-block"><code>{{ outputCode || "转换结果会显示在这里" }}</code></pre>
      </section>

      <section class="browser-guide">
        <h2>从浏览器复制 curl 命令</h2>
        <p>从浏览器的开发者工具中复制任意网络请求为 curl 命令，粘贴到上方输入框中即可转换。</p>
        <div class="browser-steps">
          <article class="browser-step">
            <h3>Chrome / Edge</h3>
            <ol>
              <li>打开 DevTools（F12）</li>
              <li>切换到 Network 标签页</li>
              <li>右键点击（或 Ctrl+点击）一个请求</li>
              <li>选择 "Copy" → "Copy as cURL (bash)"</li>
            </ol>
          </article>
          <article class="browser-step">
            <h3>Safari</h3>
            <ol>
              <li>打开 Developer Tools（Cmd+Option+I）</li>
              <li>切换到 Network 标签页</li>
              <li>右键点击（或双指点击）一个请求</li>
              <li>选择 "Copy as cURL"</li>
            </ol>
          </article>
          <article class="browser-step">
            <h3>Firefox</h3>
            <ol>
              <li>打开 DevTools（F12）</li>
              <li>切换到 Network Monitor 标签页</li>
              <li>右键点击一个请求</li>
              <li>选择 "Copy" → "Copy as cURL"</li>
            </ol>
          </article>
        </div>
      </section>

      <section class="summary-grid">
        <article class="summary-item">
          <span>Method</span>
          <strong>{{ requestInfo.method || "-" }}</strong>
        </article>
        <article class="summary-item summary-item--url">
          <span>URL</span>
          <strong>{{ requestInfo.url || "-" }}</strong>
        </article>
        <article class="summary-item">
          <span>Headers</span>
          <strong>{{ countRecord(requestInfo.headers) }}</strong>
        </article>
        <article class="summary-item">
          <span>Cookies</span>
          <strong>{{ countRecord(requestInfo.cookies) }}</strong>
        </article>
        <article class="summary-item">
          <span>Query</span>
          <strong>{{ countRecord(requestInfo.queries) }}</strong>
        </article>
        <article class="summary-item">
          <span>Body</span>
          <strong>{{ bodyLabel }}</strong>
        </article>
      </section>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { Delete, DocumentCopy } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import {
  toAnsibleWarn,
  toCWarn,
  toCFMLWarn,
  toClojureWarn,
  toCSharpWarn,
  toDartWarn,
  toElixirWarn,
  toGoWarn,
  toHarStringWarn,
  toHTTPWarn,
  toHttpieWarn,
  toJavaWarn,
  toJavaHttpUrlConnectionWarn,
  toJavaJsoupWarn,
  toJavaOkHttpWarn,
  toJavaScriptWarn,
  toJavaScriptJqueryWarn,
  toJavaScriptXHRWarn,
  toJsonObjectWarn,
  toJsonStringWarn,
  toJuliaWarn,
  toKotlinWarn,
  toLuaWarn,
  toMATLABWarn,
  toNodeAxiosWarn,
  toNodeFetchWarn,
  toNodeGotWarn,
  toNodeHttpWarn,
  toNodeKyWarn,
  toNodeRequestWarn,
  toNodeSuperAgentWarn,
  toObjectiveCWarn,
  toOCamlWarn,
  toPerlWarn,
  toPhpWarn,
  toPhpGuzzleWarn,
  toPhpRequestsWarn,
  toPowershellRestMethodWarn,
  toPowershellWebRequestWarn,
  toPythonWarn,
  toPythonHttpWarn,
  toRWarn,
  toRHttr2Warn,
  toRubyWarn,
  toRubyHttpartyWarn,
  toRustWarn,
  toSwiftWarn,
  toWgetWarn,
} from "curlconverter";
import type { JSONOutput, Warnings } from "curlconverter";
import ToolPageShell from "@features/tools/ui/ToolPageShell.vue";

type Target =
  | "python"
  | "python-http"
  | "fetch"
  | "jquery"
  | "xhr"
  | "node-fetch"
  | "axios"
  | "node-got"
  | "node-ky"
  | "node-request"
  | "node-superagent"
  | "node-http"
  | "go"
  | "java-okhttp"
  | "java-httpclient"
  | "java-httpurlconnection"
  | "java-jsoup"
  | "php"
  | "php-guzzle"
  | "php-requests"
  | "ruby"
  | "ruby-httparty"
  | "rust"
  | "csharp"
  | "powershell"
  | "powershell-webrequest"
  | "http"
  | "httpie"
  | "wget"
  | "har"
  | "json"
  | "ansible"
  | "c"
  | "cfml"
  | "clojure"
  | "dart"
  | "elixir"
  | "julia"
  | "kotlin"
  | "lua"
  | "matlab"
  | "ocaml"
  | "objectivec"
  | "perl"
  | "swift"
  | "r"
  | "r-httr2"
  | "feapder";

interface ConverterOption {
  value: Target;
  label: string;
  group: string;
  description: string;
  extension: string;
  convert: (command: string) => [string, Warnings];
}

const placeholder = `curl 'https://api.example.com/users?source=browser' \\
  -X POST \\
  -H 'Authorization: Bearer token' \\
  -H 'Content-Type: application/json' \\
  -H 'Accept: application/json' \\
  --data-raw '{"name":"MyBlob","roles":["admin","editor"]}'`;

const browserExample = `curl 'https://example.com/api/search?q=myblob&page=1' \\
  -H 'accept: application/json, text/plain, */*' \\
  -H 'user-agent: Mozilla/5.0' \\
  -H 'cookie: sessionid=abc123; theme=light' \\
  --compressed`;

const curlInput = ref(placeholder);
const target = ref<Target>("python");
const targetKeyword = ref("");
const outputCode = ref("");
const parseError = ref("");
const warnings = ref<Warnings>([]);
const requestInfo = ref<JSONOutput | undefined>();
const autoConvert = ref(true);
const route = useRoute();
let convertTimer: number | undefined;

const examples = [
  {
    title: "GET 请求",
    description: "简单 GET、query 参数",
    curl: `curl 'https://api.example.com/users?page=1&limit=10' -H 'Accept: application/json'`,
  },
  {
    title: "JSON POST",
    description: "认证头、JSON body、query 参数",
    curl: placeholder,
  },
  {
    title: "Basic Auth",
    description: "Authorization: Basic 认证",
    curl: `curl -u admin:secret123 https://api.example.com/admin/users`,
  },
  {
    title: "浏览器复制",
    description: "Cookie、UA、压缩响应",
    curl: browserExample,
  },
  {
    title: "表单提交",
    description: "application/x-www-form-urlencoded",
    curl: `curl -X POST https://api.example.com/login -d 'username=demo&password=secret'`,
  },
  {
    title: "文件上传",
    description: "multipart/form-data",
    curl: `curl -X POST https://api.example.com/upload -F 'file=@/tmp/report.pdf' -F 'name=report'`,
  },
];

const emptyWarnings = (): Warnings => [];

const converterOptions: ConverterOption[] = [
  // Python
  { value: "python", label: "Python requests", group: "Python", description: "最常用的 requests 写法。", extension: "py", convert: (command) => toPythonWarn(command, emptyWarnings()) },
  { value: "python-http", label: "Python http.client", group: "Python", description: "标准库 http.client 写法。", extension: "py", convert: (command) => toPythonHttpWarn(command, emptyWarnings()) },

  // JavaScript
  { value: "fetch", label: "Browser fetch", group: "JavaScript", description: "浏览器 fetch 写法。", extension: "js", convert: (command) => toJavaScriptWarn(command, emptyWarnings()) },
  { value: "jquery", label: "JavaScript jQuery", group: "JavaScript", description: "jQuery.ajax 写法。", extension: "js", convert: (command) => toJavaScriptJqueryWarn(command, emptyWarnings()) },
  { value: "xhr", label: "JavaScript XHR", group: "JavaScript", description: "XMLHttpRequest 写法。", extension: "js", convert: (command) => toJavaScriptXHRWarn(command, emptyWarnings()) },

  // Node.js
  { value: "node-fetch", label: "Node.js fetch", group: "Node.js", description: "Node.js fetch 写法。", extension: "js", convert: (command) => toNodeFetchWarn(command, emptyWarnings()) },
  { value: "axios", label: "Node.js Axios", group: "Node.js", description: "axios 请求写法。", extension: "js", convert: (command) => toNodeAxiosWarn(command, emptyWarnings()) },
  { value: "node-got", label: "Node.js Got", group: "Node.js", description: "Got 请求写法。", extension: "js", convert: (command) => toNodeGotWarn(command, emptyWarnings()) },
  { value: "node-ky", label: "Node.js Ky", group: "Node.js", description: "Ky 请求写法。", extension: "js", convert: (command) => toNodeKyWarn(command, emptyWarnings()) },
  { value: "node-request", label: "Node.js Request", group: "Node.js", description: "Request 请求写法。", extension: "js", convert: (command) => toNodeRequestWarn(command, emptyWarnings()) },
  { value: "node-superagent", label: "Node.js SuperAgent", group: "Node.js", description: "SuperAgent 请求写法。", extension: "js", convert: (command) => toNodeSuperAgentWarn(command, emptyWarnings()) },
  { value: "node-http", label: "Node.js http", group: "Node.js", description: "原生 http/https 模块写法。", extension: "js", convert: (command) => toNodeHttpWarn(command, emptyWarnings()) },

  // Java
  { value: "java-okhttp", label: "Java OkHttp", group: "Java", description: "OkHttpClient 写法。", extension: "java", convert: (command) => toJavaOkHttpWarn(command, emptyWarnings()) },
  { value: "java-httpclient", label: "Java HttpClient", group: "Java", description: "java.net.http.HttpClient 写法。", extension: "java", convert: (command) => toJavaWarn(command, emptyWarnings()) },
  { value: "java-httpurlconnection", label: "Java HttpURLConnection", group: "Java", description: "HttpURLConnection 写法。", extension: "java", convert: (command) => toJavaHttpUrlConnectionWarn(command, emptyWarnings()) },
  { value: "java-jsoup", label: "Java jsoup", group: "Java", description: "jsoup 连接写法。", extension: "java", convert: (command) => toJavaJsoupWarn(command, emptyWarnings()) },

  // PHP
  { value: "php", label: "PHP cURL", group: "PHP", description: "PHP curl 扩展写法。", extension: "php", convert: (command) => toPhpWarn(command, emptyWarnings()) },
  { value: "php-guzzle", label: "PHP Guzzle", group: "PHP", description: "GuzzleHttp 客户端写法。", extension: "php", convert: (command) => toPhpGuzzleWarn(command, emptyWarnings()) },
  { value: "php-requests", label: "PHP Requests", group: "PHP", description: "Requests 库写法。", extension: "php", convert: (command) => toPhpRequestsWarn(command, emptyWarnings()) },

  // PowerShell
  { value: "powershell", label: "PowerShell RestMethod", group: "PowerShell", description: "Invoke-RestMethod 写法。", extension: "ps1", convert: (command) => toPowershellRestMethodWarn(command, emptyWarnings()) },
  { value: "powershell-webrequest", label: "PowerShell WebRequest", group: "PowerShell", description: "Invoke-WebRequest 写法。", extension: "ps1", convert: (command) => toPowershellWebRequestWarn(command, emptyWarnings()) },

  // Go
  { value: "go", label: "Go", group: "Go", description: "Go net/http 写法。", extension: "go", convert: (command) => toGoWarn(command, emptyWarnings()) },

  // Ruby
  { value: "ruby", label: "Ruby Net::HTTP", group: "Ruby", description: "Net::HTTP 写法。", extension: "rb", convert: (command) => toRubyWarn(command, emptyWarnings()) },
  { value: "ruby-httparty", label: "Ruby HTTParty", group: "Ruby", description: "HTTParty 写法。", extension: "rb", convert: (command) => toRubyHttpartyWarn(command, emptyWarnings()) },

  // R
  { value: "r", label: "R httr", group: "R", description: "httr 库写法。", extension: "r", convert: (command) => toRWarn(command, emptyWarnings()) },
  { value: "r-httr2", label: "R httr2", group: "R", description: "httr2 库写法。", extension: "r", convert: (command) => toRHttr2Warn(command, emptyWarnings()) },

  // Shell
  { value: "http", label: "Raw HTTP", group: "Shell", description: "标准 HTTP 报文预览。", extension: "http", convert: (command) => toHTTPWarn(command, emptyWarnings()) },
  { value: "httpie", label: "HTTPie", group: "Shell", description: "httpie 命令写法。", extension: "sh", convert: (command) => toHttpieWarn(command, emptyWarnings()) },
  { value: "wget", label: "Wget", group: "Shell", description: "wget 命令写法。", extension: "sh", convert: (command) => toWgetWarn(command, emptyWarnings()) },

  // Data
  { value: "har", label: "HAR", group: "Data", description: "HAR JSON，请求回放和接口归档可用。", extension: "har", convert: (command) => toHarStringWarn(command, emptyWarnings()) },
  { value: "json", label: "Request JSON", group: "Data", description: "结构化请求对象，便于二次处理。", extension: "json", convert: (command) => toJsonStringWarn(command, emptyWarnings()) },

  // Crawler
  { value: "feapder", label: "feapder", group: "Crawler", description: "结合请求结构生成 feapder.AirSpider 模板。", extension: "py", convert: convertToFeapder },

  // Other Languages
  { value: "ansible", label: "Ansible", group: "Other", description: "Ansible uri 模块写法。", extension: "yml", convert: (command) => toAnsibleWarn(command, emptyWarnings()) },
  { value: "c", label: "C (libcurl)", group: "Other", description: "C libcurl 写法。", extension: "c", convert: (command) => toCWarn(command, emptyWarnings()) },
  { value: "csharp", label: "C#", group: "Other", description: "C# HttpClient 写法。", extension: "cs", convert: (command) => toCSharpWarn(command, emptyWarnings()) },
  { value: "cfml", label: "ColdFusion", group: "Other", description: "ColdFusion cfhttp 写法。", extension: "cfm", convert: (command) => toCFMLWarn(command, emptyWarnings()) },
  { value: "clojure", label: "Clojure", group: "Other", description: "clj-http 写法。", extension: "clj", convert: (command) => toClojureWarn(command, emptyWarnings()) },
  { value: "dart", label: "Dart", group: "Other", description: "Dart http 包写法。", extension: "dart", convert: (command) => toDartWarn(command, emptyWarnings()) },
  { value: "elixir", label: "Elixir", group: "Other", description: "Elixir HTTPoison 写法。", extension: "ex", convert: (command) => toElixirWarn(command, emptyWarnings()) },
  { value: "julia", label: "Julia", group: "Other", description: "Julia HTTP 包写法。", extension: "jl", convert: (command) => toJuliaWarn(command, emptyWarnings()) },
  { value: "kotlin", label: "Kotlin", group: "Other", description: "Kotlin java.net.URL 写法。", extension: "kt", convert: (command) => toKotlinWarn(command, emptyWarnings()) },
  { value: "lua", label: "Lua", group: "Other", description: "Lua socket.http 写法。", extension: "lua", convert: (command) => toLuaWarn(command, emptyWarnings()) },
  { value: "matlab", label: "MATLAB", group: "Other", description: "MATLAB webread 写法。", extension: "m", convert: (command) => toMATLABWarn(command, emptyWarnings()) },
  { value: "ocaml", label: "OCaml", group: "Other", description: "OCaml cohttp 写法。", extension: "ml", convert: (command) => toOCamlWarn(command, emptyWarnings()) },
  { value: "objectivec", label: "Objective-C", group: "Other", description: "Objective-C NSURLSession 写法。", extension: "m", convert: (command) => toObjectiveCWarn(command, emptyWarnings()) },
  { value: "perl", label: "Perl", group: "Other", description: "Perl LWP::UserAgent 写法。", extension: "pl", convert: (command) => toPerlWarn(command, emptyWarnings()) },
  { value: "rust", label: "Rust", group: "Other", description: "Rust reqwest 写法。", extension: "rs", convert: (command) => toRustWarn(command, emptyWarnings()) },
  { value: "swift", label: "Swift", group: "Other", description: "Swift URLSession 写法。", extension: "swift", convert: (command) => toSwiftWarn(command, emptyWarnings()) },
];

const activeConverter = computed(() => converterOptions.find((item) => item.value === target.value));

const filteredTargets = computed(() => {
  const keyword = targetKeyword.value.trim().toLowerCase();
  if (!keyword) return converterOptions;
  return converterOptions.filter((item) =>
    [item.label, item.group, item.description, item.value].join(" ").toLowerCase().includes(keyword),
  );
});

const bodyLabel = computed(() => {
  const info = requestInfo.value;
  if (!info) return "none";
  if (info.files && Object.keys(info.files).length) return "multipart";
  if (info.data !== undefined) return typeof info.data === "string" ? "raw/form" : "json/object";
  return "none";
});

const meta = computed(() => [
  { label: "目标", value: activeConverter.value?.label || "-" },
  { label: "Method", value: requestInfo.value?.method?.toUpperCase() || "-" },
  { label: "Header", value: `${countRecord(requestInfo.value?.headers)}` },
  { label: "提示", value: `${warnings.value.length}` },
]);

function countRecord(value?: Record<string, unknown>) {
  return value ? Object.keys(value).length : 0;
}

function normalizeCommand(command: string) {
  return command.trim().replace(/^\$\s*/, "");
}

function convert() {
  parseError.value = "";
  warnings.value = [];
  outputCode.value = "";
  requestInfo.value = undefined;

  const command = normalizeCommand(curlInput.value);
  if (!command) {
    parseError.value = "请输入 curl 命令";
    return;
  }
  if (!/^curl(\s|$)/i.test(command)) {
    parseError.value = "命令需要以 curl 开头";
    return;
  }

  try {
    const [json, jsonWarnings] = toJsonObjectWarn(command, emptyWarnings());
    requestInfo.value = json;
    warnings.value = jsonWarnings;

    const converter = activeConverter.value || converterOptions[0];
    const [code, codeWarnings] = converter.convert(command);
    outputCode.value = code;
    warnings.value = mergeWarnings(jsonWarnings, codeWarnings);
  } catch (error) {
    parseError.value = error instanceof Error ? error.message : "curl 解析失败，请检查引号、换行和参数";
  }
}

function mergeWarnings(left: Warnings, right: Warnings) {
  const seen = new Set<string>();
  return [...left, ...right].filter((warning) => {
    const key = warning.join("::");
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });
}

function convertToFeapder(command: string): [string, Warnings] {
  const [json, nextWarnings] = toJsonObjectWarn(command, emptyWarnings());
  return [buildFeapderTemplate(json), nextWarnings];
}

function buildFeapderTemplate(info: JSONOutput) {
  const args: string[] = [toPythonLiteral(info.url)];
  if (info.method && info.method.toUpperCase() !== "GET") args.push(`method=${toPythonLiteral(info.method.toUpperCase())}`);
  if (info.queries && Object.keys(info.queries).length) args.push(`params=${toPythonLiteral(info.queries, 3)}`);
  if (info.headers && Object.keys(info.headers).length) args.push(`headers=${toPythonLiteral(info.headers, 3)}`);
  if (info.cookies && Object.keys(info.cookies).length) args.push(`cookies=${toPythonLiteral(info.cookies, 3)}`);
  if (info.data !== undefined) {
    const contentType = Object.entries(info.headers || {}).find(([key]) => key.toLowerCase() === "content-type")?.[1];
    const key = typeof info.data === "object" && String(contentType || "").includes("json") ? "json" : "data";
    args.push(`${key}=${toPythonLiteral(info.data, 3)}`);
  }
  if (info.files && Object.keys(info.files).length) args.push(`files=${toPythonLiteral(info.files, 3)}`);
  if (info.follow_redirects) args.push("allow_redirects=True");
  if (info.timeout) args.push(`timeout=${info.timeout}`);
  if (info.proxy) args.push(`proxies=${toPythonLiteral({ http: info.proxy, https: info.proxy }, 3)}`);

  return [
    "import feapder",
    "",
    "",
    "class CurlSpider(feapder.AirSpider):",
    "    def start_requests(self):",
    `        yield feapder.Request(${args.join(", ")})`,
    "",
    "    def parse(self, request, response):",
    "        print(response.status_code)",
    "        print(response.text)",
    "",
    "",
    "if __name__ == \"__main__\":",
    "    CurlSpider().start()",
  ].join("\n");
}

function toPythonLiteral(value: unknown, indent = 0): string {
  const pad = "    ".repeat(indent);
  const nextPad = "    ".repeat(indent + 1);
  if (value === null || value === undefined) return "None";
  if (typeof value === "boolean") return value ? "True" : "False";
  if (typeof value === "number") return Number.isFinite(value) ? String(value) : "None";
  if (typeof value === "string") return JSON.stringify(value);
  if (Array.isArray(value)) {
    if (!value.length) return "[]";
    return `[\n${value.map((item) => `${nextPad}${toPythonLiteral(item, indent + 1)}`).join(",\n")}\n${pad}]`;
  }
  const entries = Object.entries(value as Record<string, unknown>).filter(([, item]) => item !== undefined);
  if (!entries.length) return "{}";
  return `{\n${entries
    .map(([key, item]) => `${nextPad}${JSON.stringify(key)}: ${toPythonLiteral(item, indent + 1)}`)
    .join(",\n")}\n${pad}}`;
}

async function copy(value: string) {
  if (!value) {
    ElMessage.warning("没有内容可复制");
    return;
  }
  await navigator.clipboard.writeText(value);
  ElMessage.success("已复制");
}

function downloadOutput() {
  if (!outputCode.value) {
    ElMessage.warning("请先生成转换结果");
    return;
  }
  const converter = activeConverter.value || converterOptions[0];
  const blob = new Blob([outputCode.value], { type: "text/plain;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `curl-${converter.value}.${converter.extension}`;
  link.click();
  URL.revokeObjectURL(url);
}

function clearAll() {
  curlInput.value = "";
  outputCode.value = "";
  requestInfo.value = undefined;
  parseError.value = "";
  warnings.value = [];
}

function useExample(value: string) {
  curlInput.value = value;
}

function loadBrowserExample() {
  curlInput.value = browserExample;
}

function scheduleConvert() {
  window.clearTimeout(convertTimer);
  if (!autoConvert.value) return;
  convertTimer = window.setTimeout(convert, 240);
}

watch([curlInput, target], scheduleConvert, { immediate: true });
watch(autoConvert, (enabled) => {
  if (enabled) convert();
});
watch(
  () => route.meta.curlTarget,
  (value) => {
    if (value === "feapder") target.value = "feapder";
    else if (value === "fetch") target.value = "fetch";
    else target.value = "python";
  },
  { immediate: true },
);
</script>

<style scoped>
.curl-lab {
  display: grid;
  grid-template-columns: minmax(420px, 1.05fr) 260px minmax(420px, 1.1fr);
  gap: 14px;
}

.panel {
  min-width: 0;
  border: 1px solid var(--theme-border);
  border-radius: 14px;
  background: var(--theme-card);
  box-shadow: var(--shadow-xs);
}

.input-panel,
.output-panel {
  display: grid;
  min-height: 640px;
  grid-template-rows: auto minmax(0, 1fr) auto auto;
}

.target-panel {
  display: grid;
  max-height: 640px;
  grid-template-rows: auto auto minmax(0, 1fr);
  padding: 14px;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  padding: 16px;
  border-bottom: 1px solid var(--theme-border);
}

.compact-head {
  padding: 0 0 12px;
  border-bottom: 0;
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

.head-actions,
.input-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}

.curl-input {
  min-height: 0;
}

.curl-input :deep(.el-textarea__inner) {
  height: 100%;
  min-height: 100% !important;
  border: 0;
  border-radius: 0;
  box-shadow: none;
  color: var(--theme-text);
  font-family: "JetBrains Mono", "Consolas", monospace;
  font-size: 14px;
  line-height: 1.75;
}

.input-actions {
  justify-content: flex-start;
  padding: 12px 16px;
  border-top: 1px solid var(--theme-border);
}

.example-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  padding: 0 16px 16px;
}

.example-card,
.target-item {
  border: 1px solid var(--theme-border);
  border-radius: 10px;
  background: var(--theme-hover);
  text-align: left;
  cursor: pointer;
}

.example-card {
  padding: 10px;
}

.example-card:hover,
.target-item:hover,
.target-item.active {
  border-color: var(--theme-primary);
  background: #ecfeff;
}

.example-card strong,
.example-card span,
.target-item span,
.target-item small,
.summary-item span,
.summary-item strong {
  display: block;
}

.example-card strong,
.target-item span {
  color: var(--theme-text);
  font-size: 13px;
  font-weight: 700; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;
}

.example-card span,
.target-item small,
.summary-item span {
  color: var(--theme-text-secondary);
  font-size: 12px;
}

.target-list {
  display: grid;
  gap: 8px;
  margin-top: 12px;
  overflow: auto;
  padding-right: 4px;
}

.target-item {
  padding: 10px 11px;
}

.target-item.active span {
  color: var(--theme-primary);
}

.alert,
.warning-box {
  margin: 12px 16px 0;
}

.warning-box {
  border: 1px solid rgba(245, 158, 11, 0.28);
  border-radius: 10px;
  padding: 10px 12px;
  background: var(--theme-hover);
  color: #92400e;
  font-size: 12px;
}

.warning-box ul {
  display: grid;
  gap: 5px;
  margin: 8px 0 0;
  padding: 0 0 0 18px;
}

.warning-box em {
  display: block;
  color: #b45309;
  font-style: normal;
  opacity: 0.82;
}

.code-block {
  min-height: 0;
  margin: 12px 16px 16px;
  overflow: auto;
  border-radius: 12px;
  padding: 16px;
  background: var(--theme-text);
  color: #dbeafe;
  font-family: "JetBrains Mono", "Consolas", monospace;
  font-size: 13px;
  line-height: 1.65;
  white-space: pre-wrap;
}

.summary-grid {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: 120px minmax(220px, 1fr) repeat(4, 120px);
  gap: 10px;
}

.summary-item {
  min-width: 0;
  border: 1px solid var(--theme-border);
  border-radius: 12px;
  padding: 12px;
  background: linear-gradient(135deg, var(--theme-card), var(--theme-background));
}

.summary-item strong {
  margin-top: 4px;
  color: var(--theme-text);
  font-size: 15px;
  word-break: break-all;
}

.browser-guide {
  grid-column: 1 / -1;
  margin-top: 18px;
  padding: 18px 22px 22px;
  border: 1px solid var(--theme-border);
  border-radius: 14px;
  background: var(--theme-card);
  box-shadow: var(--shadow-xs);
}

.browser-guide h2 {
  margin: 0;
  font-size: 16px;
}

.browser-guide > p {
  margin: 6px 0 14px;
  font-size: 13px;
  line-height: 1.6;
}

.browser-steps {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.browser-step h3 {
  margin: 0 0 8px;
  font-size: 14px;
  color: var(--theme-text);
}

.browser-step ol {
  margin: 0;
  padding: 0 0 0 18px;
  font-size: 13px;
  line-height: 1.8;
  color: var(--theme-text-secondary);
}

.browser-step li::marker {
  color: var(--theme-primary);
  font-weight: 700;
}

@media (max-width: 1280px) {
  .curl-lab {
    grid-template-columns: minmax(0, 1fr) 240px;
  }

  .output-panel {
    grid-column: 1 / -1;
  }

  .summary-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .curl-lab,
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .input-panel,
  .output-panel {
    min-height: 520px;
  }

  .example-grid {
    grid-template-columns: 1fr;
  }

  .browser-steps {
    grid-template-columns: 1fr;
  }
}
</style>
