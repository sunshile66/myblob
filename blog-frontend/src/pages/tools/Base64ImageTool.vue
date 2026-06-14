<template>
  <ToolPageShell
    title="Base64 图片"
    description="图片与 Base64/DataURL 双向转换，支持预览、尺寸识别、体积估算和下载。"
    eyebrow="图片处理"
    :meta="meta"
  >
    <div class="image-base64">
      <section class="panel">
        <div class="panel-head">
          <div>
            <h2>图片转 Base64</h2>
            <p>选择本地图片后会在浏览器内完成转换，不上传服务器。</p>
          </div>
          <el-button text @click="clearImage">清空</el-button>
        </div>

        <label class="drop-zone" :class="{ 'drop-active': isDragging }"
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="isDragging = false; handleDrop($event)">
          <input type="file" accept="image/*" @change="handleFileChange" />
          <span>拖拽图片到此处或点击选择</span>
          <small>支持 PNG、JPEG、WebP、GIF、SVG 等浏览器可读格式</small>
        </label>

        <div v-if="previewUrl" class="preview-box">
          <img :src="previewUrl" alt="图片预览" />
        </div>

        <div class="output-actions">
          <el-switch v-model="dataUrlOnly" active-text="仅 Base64" inactive-text="DataURL" />
          <el-button :icon="DocumentCopy" @click="copyBase64">复制</el-button>
        </div>

        <pre class="text-output"><code>{{ encodedOutput || "图片 Base64 会显示在这里" }}</code></pre>
      </section>

      <section class="panel">
        <div class="panel-head">
          <div>
            <h2>Base64 转图片</h2>
            <p>粘贴 DataURL 或纯 Base64，会尝试识别 MIME 类型并预览。</p>
          </div>
          <el-button text @click="decodeInput = ''">清空</el-button>
        </div>

        <el-input
          v-model="decodeInput"
          type="textarea"
          :rows="12"
          placeholder="data:image/png;base64,... 或纯 Base64"
        />

        <el-alert
          v-if="decodeError"
          :title="decodeError"
          type="error"
          show-icon
          :closable="false"
          class="alert"
        />

        <div v-if="decodedDataUrl" class="preview-box">
          <img :src="decodedDataUrl" alt="Base64 预览" />
        </div>

        <div class="output-actions">
          <el-input v-model="downloadName" placeholder="文件名" />
          <el-button type="primary" @click="downloadDecoded">下载图片</el-button>
        </div>
      </section>

      <section class="panel info-panel">
        <article v-for="item in imageInfo" :key="item.label">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </article>
      </section>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { DocumentCopy } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import ToolPageShell from "@features/tools/ui/ToolPageShell.vue";

const isDragging = ref(false);
const fileName = ref("");
const fileType = ref("");
const fileSize = ref(0);
const width = ref(0);
const height = ref(0);
const encodedDataUrl = ref("");
const dataUrlOnly = ref(false);
const decodeInput = ref("");
const decodeError = ref("");
const downloadName = ref("image.png");

const previewUrl = computed(() => encodedDataUrl.value);
const encodedOutput = computed(() => {
  if (!encodedDataUrl.value) return "";
  if (!dataUrlOnly.value) return encodedDataUrl.value;
  return encodedDataUrl.value.split(",")[1] || encodedDataUrl.value;
});

const normalizedDecode = computed(() => decodeInput.value.trim().replace(/\s/g, ""));
const decodedDataUrl = computed(() => {
  const value = normalizedDecode.value;
  if (!value) return "";
  if (/^data:image\/[a-zA-Z0-9.+-]+;base64,/.test(value)) return value;
  return `data:image/png;base64,${value}`;
});

const estimateBytes = (value: string) => {
  const base64 = value.includes(",") ? value.split(",")[1] || "" : value;
  return Math.max(0, Math.floor((base64.length * 3) / 4) - (base64.endsWith("==") ? 2 : base64.endsWith("=") ? 1 : 0));
};

const formatBytes = (bytes: number) => {
  if (!bytes) return "0 B";
  const units = ["B", "KB", "MB", "GB"];
  const index = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1);
  return `${(bytes / 1024 ** index).toFixed(index === 0 ? 0 : 2)} ${units[index]}`;
};

const imageInfo = computed(() => [
  { label: "文件名", value: fileName.value || "未选择" },
  { label: "类型", value: fileType.value || "未知" },
  { label: "原始体积", value: formatBytes(fileSize.value) },
  { label: "Base64 体积", value: formatBytes(estimateBytes(encodedOutput.value || normalizedDecode.value)) },
  { label: "尺寸", value: width.value && height.value ? `${width.value} x ${height.value}` : "待识别" },
]);

const meta = computed(() => [
  { label: "原始体积", value: formatBytes(fileSize.value) },
  { label: "尺寸", value: width.value && height.value ? `${width.value}x${height.value}` : "未识别" },
  { label: "输出长度", value: `${encodedOutput.value.length}` },
]);

const readImageSize = (dataUrl: string) => {
  width.value = 0;
  height.value = 0;
  const image = new Image();
  image.onload = () => {
    width.value = image.naturalWidth;
    height.value = image.naturalHeight;
  };
  image.src = dataUrl;
};

const handleFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  const reader = new FileReader();
  reader.onload = () => {
    encodedDataUrl.value = String(reader.result || "");
    fileName.value = file.name;
    fileType.value = file.type || "image/*";
    fileSize.value = file.size;
    readImageSize(encodedDataUrl.value);
  };
  reader.readAsDataURL(file);
};

const handleDrop = (event: DragEvent) => {
  const file = event.dataTransfer?.files?.[0];
  if (!file || !file.type.startsWith("image/")) return;
  const reader = new FileReader();
  reader.onload = () => {
    encodedDataUrl.value = String(reader.result || "");
    fileName.value = file.name;
    fileType.value = file.type || "image/*";
    fileSize.value = file.size;
    readImageSize(encodedDataUrl.value);
  };
  reader.readAsDataURL(file);
};

const clearImage = () => {
  fileName.value = "";
  fileType.value = "";
  fileSize.value = 0;
  width.value = 0;
  height.value = 0;
  encodedDataUrl.value = "";
};

const copyBase64 = async () => {
  if (!encodedOutput.value) {
    ElMessage.warning("没有可复制的 Base64");
    return;
  }
  await navigator.clipboard.writeText(encodedOutput.value);
  ElMessage.success("已复制");
};

const downloadDecoded = () => {
  if (!decodedDataUrl.value || decodeError.value) {
    ElMessage.warning("请先输入有效的 Base64 图片");
    return;
  }
  const link = document.createElement("a");
  link.href = decodedDataUrl.value;
  link.download = downloadName.value.trim() || "image.png";
  link.click();
};

watch(decodedDataUrl, (value) => {
  decodeError.value = "";
  if (!value || !normalizedDecode.value) return;

  const image = new Image();
  image.onload = () => {
    width.value = image.naturalWidth;
    height.value = image.naturalHeight;
  };
  image.onerror = () => {
    decodeError.value = "无法识别为有效图片，请检查 Base64 内容";
  };
  image.src = value;
});
</script>

<style scoped>
.image-base64 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.panel {
  padding: 16px;
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-md);
  background: var(--theme-card);
  box-shadow: var(--shadow-xs);
}

.info-panel {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
}

.info-panel article {
  padding: 12px;
  border-radius: var(--radius-md);
  background: var(--theme-hover);
}

.info-panel span,
.info-panel strong {
  display: block;
}

.info-panel span {
  color: var(--theme-text-secondary);
  font-size: 12px;
}

.info-panel strong {
  margin-top: 5px;
  overflow: hidden;
  color: var(--theme-text);
  font-size: 15px;
  text-overflow: ellipsis;
  white-space: nowrap;
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
}

.drop-zone {
  display: grid;
  place-items: center;
  min-height: 130px;
  margin-bottom: 12px;
  border: 1px dashed var(--theme-primary);
  border-radius: var(--radius-md);
  background: var(--theme-primary-light);
  color: var(--theme-primary);
  cursor: pointer;
  text-align: center;
}

.drop-zone.drop-active {
  border-color: var(--theme-primary);
  background: var(--theme-primary-light);
  transform: scale(1.01);
}
.drop-zone input {
  display: none;
}

.drop-zone span {
  font-size: 16px;
  font-weight: 700; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;
}

.drop-zone small {
  color: var(--theme-text-secondary);
  font-size: 12px;
}

.preview-box {
  display: grid;
  place-items: center;
  min-height: 220px;
  margin-bottom: 12px;
  overflow: hidden;
  border-radius: var(--radius-md);
  background:
    linear-gradient(45deg, var(--theme-border) 25%, transparent 25%),
    linear-gradient(-45deg, var(--theme-border) 25%, transparent 25%),
    linear-gradient(45deg, transparent 75%, var(--theme-border) 75%),
    linear-gradient(-45deg, transparent 75%, var(--theme-border) 75%);
  background-color: var(--theme-card);
  background-position: 0 0, 0 10px, 10px -10px, -10px 0;
  background-size: 20px 20px;
}

.preview-box img {
  display: block;
  max-width: 100%;
  max-height: 360px;
  object-fit: contain;
}

.output-actions {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.text-output {
  max-height: 260px;
  min-height: 180px;
  margin: 0;
  padding: 14px;
  overflow: auto;
  border-radius: var(--radius-md);
  background: var(--theme-text);
  color: var(--theme-background);
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

.alert {
  margin: 12px 0;
}

@media (max-width: 1024px) {
  .image-base64,
  .info-panel {
    grid-template-columns: 1fr;
  }

  .panel-head,
  .output-actions {
    flex-direction: column;
    align-items: stretch;
  }
}

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
