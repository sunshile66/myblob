<template>
  <ToolPageShell
    title="curl 转代码"
    description="解析 curl 命令，生成 Python requests、JavaScript fetch，并展示请求摘要。"
    eyebrow="开发调试"
    :meta="meta"
  >
    <div class="workbench">
      <section class="panel input-panel">
        <div class="panel-head">
          <div>
            <h2>curl 命令</h2>
            <p>支持常见 header、cookie、query、JSON body、表单和文件上传参数。</p>
          </div>
          <el-button text :icon="Delete" @click="clearAll">清空</el-button>
        </div>

        <el-input v-model="curlInput" type="textarea" :rows="16" :placeholder="placeholder" />

        <div class="example-grid">
          <button v-for="item in examples" :key="item.title" class="example-card" @click="useExample(item.curl)">
            <strong>{{ item.title }}</strong>
            <span>{{ item.description }}</span>
          </button>
        </div>
      </section>

      <section class="panel output-panel">
        <div class="panel-head">
          <div>
            <h2>输出代码</h2>
            <p>切换语言后会保留同一份解析结果。</p>
          </div>
          <div class="inline-actions">
            <el-segmented v-model="target" :options="targetOptions" />
            <el-button :icon="DocumentCopy" @click="copy(outputCode)">复制</el-button>
          </div>
        </div>

        <el-alert v-if="parseError" :title="parseError" type="error" show-icon :closable="false" class="alert" />
        <pre class="code-block"><code>{{ outputCode || "转换结果会显示在这里" }}</code></pre>
      </section>

      <section class="panel summary-panel">
        <div class="summary-item">
          <span>Method</span>
          <strong>{{ requestInfo.method || "-" }}</strong>
        </div>
        <div class="summary-item summary-item--url">
          <span>URL</span>
          <strong>{{ requestInfo.url || "-" }}</strong>
        </div>
        <div class="summary-item">
          <span>Headers</span>
          <strong>{{ Object.keys(requestInfo.headers).length }}</strong>
        </div>
        <div class="summary-item">
          <span>Body</span>
          <strong>{{ bodyLabel }}</strong>
        </div>
      </section>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { Delete, DocumentCopy } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import ToolPageShell from "@features/tools/ui/ToolPageShell.vue";

type Target = "python" | "fetch" | "feapder";

interface ParsedRequest {
  method: string;
  url: string;
  headers: Record<string, string>;
  cookies: Record<string, string>;
  data: string;
  forms: Record<string, string>;
  files: Record<string, string>;
  params: Record<string, string>;
}

const placeholder = `curl -X POST https://api.example.com/users \\
  -H "Authorization: Bearer token" \\
  -H "Content-Type: application/json" \\
  -d '{"name":"MyBlob"}'`;

const curlInput = ref(placeholder);
const target = ref<Target>("python");
const parseError = ref("");
const requestInfo = reactive<ParsedRequest>({
  method: "",
  url: "",
  headers: {},
  cookies: {},
  data: "",
  forms: {},
  files: {},
  params: {},
});

const targetOptions = [
  { label: "Python", value: "python" },
  { label: "fetch", value: "fetch" },
  { label: "feapder", value: "feapder" },
];

const examples = [
  {
    title: "JSON POST",
    description: "带认证头和 JSON body",
    curl: placeholder,
  },
  {
    title: "Query GET",
    description: "带 query 参数",
    curl: `curl 'https://api.example.com/search?q=myblob&page=1' -H 'Accept: application/json'`,
  },
  {
    title: "文件上传",
    description: "multipart/form-data",
    curl: `curl -X POST https://api.example.com/upload -F 'file=@/tmp/report.pdf' -F 'name=report'`,
  },
];

const hasBody = computed(() => Boolean(requestInfo.data || Object.keys(requestInfo.forms).length || Object.keys(requestInfo.files).length));
const bodyLabel = computed(() => {
  if (requestInfo.data) return "raw/json";
  if (Object.keys(requestInfo.files).length) return "multipart";
  if (Object.keys(requestInfo.forms).length) return "form";
  return "none";
});

const outputCode = computed(() => {
  if (parseError.value || !requestInfo.url) return "";
  if (target.value === "python") return toPython(requestInfo);
  if (target.value === "feapder") return toFeapder(requestInfo);
  return toFetch(requestInfo);
});

const meta = computed(() => [
  { label: "请求方法", value: requestInfo.method || "-" },
  { label: "Header 数", value: `${Object.keys(requestInfo.headers).length}` },
  { label: "Body 类型", value: bodyLabel.value },
]);

const resetRequest = () => {
  requestInfo.method = "GET";
  requestInfo.url = "";
  requestInfo.headers = {};
  requestInfo.cookies = {};
  requestInfo.data = "";
  requestInfo.forms = {};
  requestInfo.files = {};
  requestInfo.params = {};
};

const tokenize = (input: string) => {
  const tokens: string[] = [];
  let current = "";
  let quote = "";
  let escaped = false;
  const normalized = input.replace(/\\\r?\n/g, " ");

  for (const char of normalized) {
    if (escaped) {
      current += char;
      escaped = false;
      continue;
    }
    if (char === "\\") {
      escaped = true;
      continue;
    }
    if (quote) {
      if (char === quote) quote = "";
      else current += char;
      continue;
    }
    if (char === "'" || char === '"') {
      quote = char;
      continue;
    }
    if (/\s/.test(char)) {
      if (current) {
        tokens.push(current);
        current = "";
      }
      continue;
    }
    current += char;
  }

  if (current) tokens.push(current);
  return tokens;
};

const setUrl = (value: string) => {
  try {
    const parsed = new URL(value);
    parsed.searchParams.forEach((paramValue, key) => {
      requestInfo.params[key] = paramValue;
    });
    parsed.search = "";
    requestInfo.url = parsed.toString();
  } catch {
    requestInfo.url = value;
  }
};

const parseCurl = () => {
  parseError.value = "";
  resetRequest();

  const tokens = tokenize(curlInput.value.trim());
  if (!tokens.length || tokens[0] !== "curl") {
    parseError.value = "请输入以 curl 开头的命令";
    return;
  }

  for (let index = 1; index < tokens.length; index += 1) {
    const token = tokens[index];
    const next = tokens[index + 1];

    if ((token === "-X" || token === "--request") && next) {
      requestInfo.method = next.toUpperCase();
      index += 1;
    } else if ((token === "-H" || token === "--header") && next) {
      const [key, ...rest] = next.split(":");
      if (key && rest.length) requestInfo.headers[key.trim()] = rest.join(":").trim();
      index += 1;
    } else if ((token === "-b" || token === "--cookie") && next) {
      next.split(";").forEach((part) => {
        const [key, value] = part.split("=");
        if (key && value) requestInfo.cookies[key.trim()] = value.trim();
      });
      index += 1;
    } else if (["-d", "--data", "--data-raw", "--data-binary", "--data-urlencode"].includes(token) && next) {
      requestInfo.data = next;
      if (requestInfo.method === "GET") requestInfo.method = "POST";
      index += 1;
    } else if ((token === "-F" || token === "--form") && next) {
      const [key, value = ""] = next.split("=");
      if (value.startsWith("@")) requestInfo.files[key] = value.slice(1);
      else requestInfo.forms[key] = value;
      if (requestInfo.method === "GET") requestInfo.method = "POST";
      index += 1;
    } else if (token.startsWith("http")) {
      setUrl(token);
    }
  }

  if (!requestInfo.url) {
    parseError.value = "未识别到请求 URL";
  }
};

const quote = (value: string) => JSON.stringify(value);

const objectLiteral = (value: Record<string, string>, indent = "  ") => {
  const entries = Object.entries(value);
  if (!entries.length) return "{}";
  return `{\n${entries.map(([key, item]) => `${indent}${quote(key)}: ${quote(item)}`).join(",\n")}\n}`;
};

const isJsonBody = (info: ParsedRequest) => {
  return (info.headers["Content-Type"] || info.headers["content-type"] || "").includes("json") || /^[\[{]/.test(info.data.trim());
};

const toPython = (info: ParsedRequest) => {
  const lines = ["import requests", ""];
  lines.push(`url = ${quote(info.url)}`);
  if (Object.keys(info.params).length) lines.push(`params = ${objectLiteral(info.params, "    ")}`);
  if (Object.keys(info.headers).length) lines.push(`headers = ${objectLiteral(info.headers, "    ")}`);
  if (Object.keys(info.cookies).length) lines.push(`cookies = ${objectLiteral(info.cookies, "    ")}`);
  if (info.data) {
    if (isJsonBody(info)) {
      try {
        lines.push(`json_data = ${JSON.stringify(JSON.parse(info.data), null, 4)}`);
      } catch {
        lines.push(`data = ${quote(info.data)}`);
      }
    } else {
      lines.push(`data = ${quote(info.data)}`);
    }
  }
  if (Object.keys(info.forms).length) lines.push(`data = ${objectLiteral(info.forms, "    ")}`);
  if (Object.keys(info.files).length) {
    lines.push("files = {");
    Object.entries(info.files).forEach(([key, path]) => lines.push(`    ${quote(key)}: open(${quote(path)}, "rb"),`));
    lines.push("}");
  }

  const args = ["url"];
  if (Object.keys(info.params).length) args.push("params=params");
  if (Object.keys(info.headers).length) args.push("headers=headers");
  if (Object.keys(info.cookies).length) args.push("cookies=cookies");
  if (info.data && isJsonBody(info)) args.push("json=json_data");
  else if (info.data || Object.keys(info.forms).length) args.push("data=data");
  if (Object.keys(info.files).length) args.push("files=files");
  lines.push("");
  lines.push(`response = requests.${info.method.toLowerCase()}(${args.join(", ")})`);
  lines.push("response.raise_for_status()");
  lines.push("print(response.text)");
  return lines.join("\n");
};

const toFetch = (info: ParsedRequest) => {
  const headers = { ...info.headers };
  let body = "";

  if (info.data) {
    body = isJsonBody(info) ? `JSON.stringify(${JSON.stringify(JSON.parse(info.data), null, 2)})` : quote(info.data);
  } else if (Object.keys(info.forms).length) {
    body = `new URLSearchParams(${objectLiteral(info.forms)})`;
  }

  const url = Object.keys(info.params).length
    ? `${info.url}?${new URLSearchParams(info.params).toString()}`
    : info.url;

  const options = [`method: ${quote(info.method)}`];
  if (Object.keys(headers).length) options.push(`headers: ${objectLiteral(headers)}`);
  if (body) options.push(`body: ${body}`);

  return `const response = await fetch(${quote(url)}, {\n  ${options.join(",\n  ")}\n});\n\nif (!response.ok) {\n  throw new Error(\`HTTP \${response.status}\`);\n}\n\nconst data = await response.text();\nconsole.log(data);`;
};

const toFeapder = (info: ParsedRequest) => {
  const lines = ["import feapder", "", "class DemoSpider(feapder.AirSpider):", "    def start_requests(self):"];
  const args = [`${quote(info.url)}`];
  args.push(`method=${quote(info.method)}`);
  if (Object.keys(info.params).length) args.push(`params=${objectLiteral(info.params, "            ")}`);
  if (Object.keys(info.headers).length) args.push(`headers=${objectLiteral(info.headers, "            ")}`);
  if (Object.keys(info.cookies).length) args.push(`cookies=${objectLiteral(info.cookies, "            ")}`);
  if (info.data && isJsonBody(info)) {
    try {
      args.push(`json=${JSON.stringify(JSON.parse(info.data), null, 12).replace(/\n/g, "\n        ")}`);
    } catch {
      args.push(`data=${quote(info.data)}`);
    }
  } else if (info.data) {
    args.push(`data=${quote(info.data)}`);
  }
  lines.push(`        yield feapder.Request(${args.join(", ")})`);
  lines.push("");
  lines.push("    def parse(self, request, response):");
  lines.push("        print(response.status_code)");
  lines.push("        print(response.text)");
  lines.push("");
  lines.push("if __name__ == \"__main__\":");
  lines.push("    DemoSpider().start()");
  return lines.join("\n");
};

const copy = async (value: string) => {
  if (!value) {
    ElMessage.warning("没有内容可复制");
    return;
  }
  await navigator.clipboard.writeText(value);
  ElMessage.success("已复制");
};

const clearAll = () => {
  curlInput.value = "";
  resetRequest();
};

const useExample = (value: string) => {
  curlInput.value = value;
};

watch(curlInput, parseCurl, { immediate: true });
</script>

<style scoped>
.workbench {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
}

.panel {
  padding: 16px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.summary-panel {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr) 120px 120px;
  gap: 12px;
}

.panel-head {
  display: flex;
  align-items: flex-start;
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

.inline-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

.example-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-top: 12px;
}

.example-card {
  padding: 10px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
  background: #f8fafc;
  text-align: left;
  cursor: pointer;
}

.example-card:hover {
  border-color: #14b8a6;
}

.example-card strong,
.example-card span,
.summary-item span,
.summary-item strong {
  display: block;
}

.example-card strong {
  color: #0f172a;
  font-size: 13px;
}

.example-card span,
.summary-item span {
  color: #64748b;
  font-size: 12px;
}

.code-block {
  min-height: 410px;
  margin: 0;
  padding: 14px;
  overflow: auto;
  border-radius: 8px;
  background: #111827;
  color: #e5e7eb;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.alert {
  margin-bottom: 12px;
}

.summary-item {
  padding: 12px;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid rgba(15, 23, 42, 0.08);
}

.summary-item strong {
  margin-top: 4px;
  color: #0f172a;
  font-size: 15px;
  word-break: break-all;
}

@media (max-width: 980px) {
  .workbench,
  .summary-panel {
    grid-template-columns: 1fr;
  }

  .example-grid {
    grid-template-columns: 1fr;
  }
}
</style>
