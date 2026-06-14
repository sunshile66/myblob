<template>
  <ToolPageShell
    title="哈希生成器"
    description="为文本生成 MD5、SHA 系列摘要，支持 HMAC、大小写输出和文件校验。"
    eyebrow="安全与标识"
    :meta="meta"
  >
    <div class="workbench">
      <section class="panel input-panel">
        <div class="panel-head">
          <div>
            <h2>输入内容</h2>
            <p>可以粘贴文本，也可以选择本地文件计算校验值。</p>
          </div>
          <div class="inline-actions">
            <el-upload :auto-upload="false" :show-file-list="false" :on-change="loadFile">
              <el-button :icon="Upload">读取文件</el-button>
            </el-upload>
            <el-button text :icon="Delete" @click="clearAll">清空</el-button>
          </div>
        </div>

        <el-input v-model="inputText" type="textarea" :rows="14" placeholder="输入要计算摘要的内容" />

        <div class="settings-grid">
          <label>
            输出格式
            <el-segmented v-model="caseMode" :options="caseOptions" />
          </label>
          <label>
            HMAC 密钥
            <el-input v-model="hmacKey" placeholder="为空时生成普通哈希" clearable />
          </label>
        </div>
      </section>

      <section class="panel result-panel">
        <div class="panel-head">
          <div>
            <h2>摘要结果</h2>
            <p>点击任意结果即可复制。</p>
          </div>
          <el-button type="primary" :icon="Refresh" @click="generate">重新计算</el-button>
        </div>

        <button v-for="item in results" :key="item.name" class="result-row" @click="copy(item.value)">
          <span>{{ item.name }}</span>
          <code>{{ item.value || "等待输入" }}</code>
        </button>
      </section>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { Delete, Refresh, Upload } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import type { UploadFile } from "element-plus";
import CryptoJS from "crypto-js";
import ToolPageShell from "@features/tools/ui/ToolPageShell.vue";

type CaseMode = "lower" | "upper";

const inputText = ref("MyBlob");
const hmacKey = ref("");
const caseMode = ref<CaseMode>("lower");
const fileName = ref("");

const caseOptions = [
  { label: "小写", value: "lower" },
  { label: "大写", value: "upper" },
];

const algorithms = [
  { name: "MD5", hash: CryptoJS.MD5, hmac: CryptoJS.HmacMD5 },
  { name: "SHA-1", hash: CryptoJS.SHA1, hmac: CryptoJS.HmacSHA1 },
  { name: "SHA-224", hash: CryptoJS.SHA224, hmac: CryptoJS.HmacSHA224 },
  { name: "SHA-256", hash: CryptoJS.SHA256, hmac: CryptoJS.HmacSHA256 },
  { name: "SHA-384", hash: CryptoJS.SHA384, hmac: CryptoJS.HmacSHA384 },
  { name: "SHA-512", hash: CryptoJS.SHA512, hmac: CryptoJS.HmacSHA512 },
];

const normalizeCase = (value: string) => (caseMode.value === "upper" ? value.toUpperCase() : value.toLowerCase());

const results = computed(() =>
  algorithms.map((algorithm) => {
    if (!inputText.value) {
      return { name: algorithm.name, value: "" };
    }

    const digest = hmacKey.value
      ? algorithm.hmac(inputText.value, hmacKey.value).toString()
      : algorithm.hash(inputText.value).toString();
    return { name: hmacKey.value ? `HMAC-${algorithm.name}` : algorithm.name, value: normalizeCase(digest) };
  })
);

const meta = computed(() => [
  { label: "输入长度", value: `${inputText.value.length} 字符` },
  { label: "模式", value: hmacKey.value ? "HMAC" : "Hash" },
  { label: "文件", value: fileName.value || "未选择" },
]);

const generate = () => {
  if (!inputText.value) {
    ElMessage.warning("请先输入内容");
    return;
  }
  ElMessage.success("摘要已更新");
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
  inputText.value = "";
  hmacKey.value = "";
  fileName.value = "";
};

const loadFile = async (file: UploadFile) => {
  const raw = file.raw;
  if (!raw) return;
  fileName.value = raw.name;
  inputText.value = await raw.text();
  ElMessage.success("文件内容已读取");
};

// Results update reactively via computed(), no manual watcher needed
</script>

<style scoped>
.workbench {
  display: grid;
  grid-template-columns: minmax(0, 0.95fr) minmax(0, 1.05fr);
  gap: 16px;
}

.panel {
  padding: 16px;
  border: 1px solid var(--theme-border);
  border-radius: 8px;
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
  line-height: 1.6;
}

.inline-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.settings-grid {
  display: grid;
  grid-template-columns: 190px minmax(0, 1fr);
  gap: 12px;
  margin-top: 12px;
}

.settings-grid label {
  display: grid;
  gap: 8px;
  color: var(--theme-text-secondary);
  font-size: 13px;
  font-weight: 700;
}

.result-panel {
  display: grid;
  align-content: start;
  gap: 8px;
}

.result-row {
  display: grid;
  grid-template-columns: 110px minmax(0, 1fr);
  gap: 12px;
  width: 100%;
  padding: 11px 12px;
  border: 1px solid var(--theme-border);
  border-radius: 8px;
  background: var(--theme-hover);
  cursor: pointer;
  text-align: left;
}

.result-row:hover {
  border-color: var(--theme-primary);
  background: var(--theme-primary-light);
}

.result-row span {
  color: var(--theme-text-secondary);
  font-size: 13px;
  font-weight: 700;
}

.result-row code {
  color: var(--theme-text);
  font-size: 13px;
  word-break: break-all;
}

@media (max-width: 960px) {
  .workbench,
  .settings-grid {
    grid-template-columns: 1fr;
  }

  .panel-head,
  .result-row {
    grid-template-columns: 1fr;
    flex-direction: column;
  }
}
:deep(.el-textarea__inner) { font-family: var(--font-mono); font-size: 14px; line-height: 1.7; }
</style>
