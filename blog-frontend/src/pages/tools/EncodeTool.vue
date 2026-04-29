<template>
  <ToolPageShell
    title="编码解码工具"
    description="集中处理 URL、HTML 与 Base64 的常见转换，适合接口调试和内容清洗。"
    eyebrow="文本处理"
    :meta="toolMeta"
  >
    <div class="workspace">
      <el-tabs v-model="activeTab" class="tool-tabs">
        <el-tab-pane label="URL" name="url">
          <div class="tool-content">
            <section class="panel">
              <h2>输入文本</h2>
              <el-input
                v-model="urlInput"
                type="textarea"
                :rows="10"
                placeholder="请输入需要进行 URL 编码或解码的文本"
              />
              <div class="actions">
                <el-button type="primary" @click="encodeURL">URL 编码</el-button>
                <el-button @click="decodeURL">URL 解码</el-button>
              </div>
            </section>

            <section class="panel">
              <h2>结果</h2>
              <el-input v-model="urlResult" type="textarea" :rows="10" readonly />
              <div class="sub-actions">
                <el-button text @click="copyResult(urlResult)">复制结果</el-button>
              </div>
            </section>
          </div>
        </el-tab-pane>

        <el-tab-pane label="HTML" name="html">
          <div class="tool-content">
            <section class="panel">
              <h2>输入 HTML</h2>
              <el-input
                v-model="htmlInput"
                type="textarea"
                :rows="10"
                placeholder="请输入 HTML 片段或转义后的内容"
              />
              <div class="actions">
                <el-button type="primary" @click="encodeHTML">HTML 编码</el-button>
                <el-button @click="decodeHTML">HTML 解码</el-button>
              </div>
            </section>

            <section class="panel">
              <h2>结果</h2>
              <el-input v-model="htmlResult" type="textarea" :rows="10" readonly />
              <div class="sub-actions">
                <el-button text @click="copyResult(htmlResult)">复制结果</el-button>
              </div>
            </section>
          </div>
        </el-tab-pane>

        <el-tab-pane label="Base64" name="base64">
          <div class="tool-content">
            <section class="panel">
              <h2>输入文本</h2>
              <el-input
                v-model="base64Input"
                type="textarea"
                :rows="10"
                placeholder="支持中文和 Unicode 文本"
              />
              <div class="actions">
                <el-button type="primary" @click="encodeBase64">Base64 编码</el-button>
                <el-button @click="decodeBase64">Base64 解码</el-button>
              </div>
            </section>

            <section class="panel">
              <h2>结果</h2>
              <el-input
                v-model="base64Result"
                type="textarea"
                :rows="10"
                readonly
              />
              <div class="sub-actions">
                <el-button text @click="copyResult(base64Result)">复制结果</el-button>
              </div>
            </section>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { ElMessage } from "element-plus";
import ToolPageShell from "@features/tools/ui/ToolPageShell.vue";

const activeTab = ref("url");
const urlInput = ref("");
const urlResult = ref("");
const htmlInput = ref("");
const htmlResult = ref("");
const base64Input = ref("");
const base64Result = ref("");

const toolMeta = computed(() => [
  { label: "当前标签", value: activeTab.value.toUpperCase() },
  { label: "处理方式", value: "浏览器本地执行" },
  {
    label: "输出长度",
    value: `${getCurrentResult().length} 字符`,
  },
]);

const ensureInput = (value: string, label: string) => {
  if (!value) {
    ElMessage.warning(`请输入${label}`);
    return false;
  }

  return true;
};

const encodeURL = () => {
  if (!ensureInput(urlInput.value, "文本")) {
    return;
  }

  urlResult.value = encodeURIComponent(urlInput.value);
  ElMessage.success("URL 编码完成");
};

const decodeURL = () => {
  if (!ensureInput(urlInput.value, "文本")) {
    return;
  }

  try {
    urlResult.value = decodeURIComponent(urlInput.value);
    ElMessage.success("URL 解码完成");
  } catch {
    ElMessage.error("URL 解码失败，请检查输入格式");
  }
};

const encodeHTML = () => {
  if (!ensureInput(htmlInput.value, "HTML")) {
    return;
  }

  const div = document.createElement("div");
  div.textContent = htmlInput.value;
  htmlResult.value = div.innerHTML;
  ElMessage.success("HTML 编码完成");
};

const decodeHTML = () => {
  if (!ensureInput(htmlInput.value, "HTML")) {
    return;
  }

  const div = document.createElement("div");
  div.innerHTML = htmlInput.value;
  htmlResult.value = div.textContent || "";
  ElMessage.success("HTML 解码完成");
};

const utf8ToBase64 = (value: string) => {
  const bytes = new TextEncoder().encode(value);
  let binary = "";
  bytes.forEach((byte) => {
    binary += String.fromCharCode(byte);
  });
  return btoa(binary);
};

const base64ToUtf8 = (value: string) => {
  const binary = atob(value);
  const bytes = Uint8Array.from(binary, (item) => item.charCodeAt(0));
  return new TextDecoder().decode(bytes);
};

const encodeBase64 = () => {
  if (!ensureInput(base64Input.value, "文本")) {
    return;
  }

  base64Result.value = utf8ToBase64(base64Input.value);
  ElMessage.success("Base64 编码完成");
};

const decodeBase64 = () => {
  if (!ensureInput(base64Input.value, "文本")) {
    return;
  }

  try {
    base64Result.value = base64ToUtf8(base64Input.value);
    ElMessage.success("Base64 解码完成");
  } catch {
    ElMessage.error("Base64 解码失败，请检查输入格式");
  }
};

const copyResult = async (value: string) => {
  if (!value) {
    ElMessage.warning("当前没有结果可复制");
    return;
  }

  await navigator.clipboard.writeText(value);
  ElMessage.success("结果已复制");
};

const getCurrentResult = () => {
  if (activeTab.value === "url") {
    return urlResult.value;
  }

  if (activeTab.value === "html") {
    return htmlResult.value;
  }

  return base64Result.value;
};
</script>

<style scoped>
.tool-tabs {
  padding: 20px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(148, 163, 184, 0.12);
  box-shadow: 0 22px 38px rgba(15, 23, 42, 0.06);
}

.tool-content {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.panel {
  padding: 18px;
  border-radius: 20px;
  background: rgba(15, 23, 42, 0.03);
}

.panel h2 {
  margin: 0 0 14px;
  color: #0f172a;
  font-size: 20px;
}

.actions,
.sub-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}

.sub-actions {
  justify-content: flex-end;
}

@media (max-width: 960px) {
  .tool-content {
    grid-template-columns: 1fr;
  }

  .tool-tabs {
    padding: 16px;
  }
}
</style>
