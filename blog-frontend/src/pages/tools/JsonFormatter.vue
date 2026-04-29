<template>
  <ToolPageShell
    title="JSON 格式化"
    description="格式化、压缩并快速检查 JSON 结构，输出结果可以直接复制或下载。"
    eyebrow="文本处理"
    :meta="toolMeta"
  >
    <div class="workspace">
      <section class="panel">
        <div class="panel-header">
          <div>
            <h2>输入 JSON</h2>
            <p>支持对象、数组和嵌套结构。</p>
          </div>
          <div class="actions">
            <el-button type="primary" @click="formatJson">格式化</el-button>
            <el-button @click="minifyJson">压缩</el-button>
            <el-button @click="validateJson">校验</el-button>
          </div>
        </div>

        <el-input
          v-model="inputJson"
          type="textarea"
          :rows="18"
          placeholder="请输入 JSON，例如：{&quot;name&quot;:&quot;MyBlob&quot;,&quot;enabled&quot;:true}"
        />

        <div class="sub-actions">
          <el-button text @click="clearAll">清空</el-button>
          <el-button text @click="copyText(inputJson, '输入内容已复制')">复制输入</el-button>
        </div>
      </section>

      <section class="panel panel--output">
        <div class="panel-header">
          <div>
            <h2>输出结果</h2>
            <p>处理后的 JSON 会保留缩进和换行。</p>
          </div>
          <div class="actions">
            <el-button @click="copyText(outputJson, '输出结果已复制')">复制</el-button>
            <el-button @click="downloadOutput">下载</el-button>
          </div>
        </div>

        <el-alert
          v-if="parseError"
          :title="parseError"
          type="error"
          show-icon
          :closable="false"
          class="error-alert"
        />

        <pre class="code-block">{{ outputJson || placeholder }}</pre>
      </section>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { ElMessage } from "element-plus";
import ToolPageShell from "@features/tools/ui/ToolPageShell.vue";

const inputJson = ref("");
const outputJson = ref("");
const parseError = ref("");
const placeholder = "处理结果会显示在这里。";

const outputSummary = computed(() => {
  if (!outputJson.value) {
    return "待处理";
  }

  try {
    const parsed = JSON.parse(outputJson.value);
    if (Array.isArray(parsed)) {
      return `数组 ${parsed.length} 项`;
    }

    return `对象 ${Object.keys(parsed).length} 个字段`;
  } catch {
    return "输出不可读";
  }
});

const outputLines = computed(() =>
  outputJson.value ? `${outputJson.value.split("\n").length} 行` : "0 行"
);

const toolMeta = computed(() => [
  { label: "当前状态", value: parseError.value ? "存在错误" : "可处理" },
  { label: "结果摘要", value: outputSummary.value },
  { label: "输出行数", value: outputLines.value },
]);

const parseInput = () => {
  parseError.value = "";

  try {
    return JSON.parse(inputJson.value);
  } catch {
    parseError.value = "JSON 格式错误，请检查逗号、引号或括号是否完整。";
    ElMessage.error(parseError.value);
    return null;
  }
};

const formatJson = () => {
  const parsed = parseInput();
  if (parsed === null) {
    return;
  }

  outputJson.value = JSON.stringify(parsed, null, 2);
  ElMessage.success("格式化完成");
};

const minifyJson = () => {
  const parsed = parseInput();
  if (parsed === null) {
    return;
  }

  outputJson.value = JSON.stringify(parsed);
  ElMessage.success("压缩完成");
};

const validateJson = () => {
  const parsed = parseInput();
  if (parsed === null) {
    return;
  }

  outputJson.value = JSON.stringify(parsed, null, 2);
  ElMessage.success("JSON 校验通过");
};

const clearAll = () => {
  inputJson.value = "";
  outputJson.value = "";
  parseError.value = "";
};

const copyText = async (text: string, successMessage: string) => {
  if (!text) {
    ElMessage.warning("当前没有内容可复制");
    return;
  }

  await navigator.clipboard.writeText(text);
  ElMessage.success(successMessage);
};

const downloadOutput = () => {
  if (!outputJson.value) {
    ElMessage.warning("请先生成结果再下载");
    return;
  }

  const blob = new Blob([outputJson.value], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = "formatted.json";
  link.click();
  URL.revokeObjectURL(url);
  ElMessage.success("下载成功");
};
</script>

<style scoped>
.workspace {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.panel {
  padding: 22px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(148, 163, 184, 0.12);
  box-shadow: 0 22px 38px rgba(15, 23, 42, 0.06);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.panel-header h2 {
  margin: 0 0 6px;
  color: #0f172a;
  font-size: 22px;
}

.panel-header p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.actions,
.sub-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.sub-actions {
  margin-top: 12px;
  justify-content: flex-end;
}

.error-alert {
  margin-bottom: 14px;
}

.code-block {
  min-height: 420px;
  margin: 0;
  padding: 18px;
  overflow: auto;
  border-radius: 18px;
  background: #0f172a;
  color: #e2e8f0;
  font-size: 14px;
  line-height: 1.7;
  font-family: "Consolas", "Monaco", monospace;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 960px) {
  .workspace {
    grid-template-columns: 1fr;
  }

  .panel {
    padding: 18px;
  }

  .panel-header {
    flex-direction: column;
  }
}
</style>
