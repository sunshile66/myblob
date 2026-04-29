<template>
  <ToolPageShell
    title="加密与摘要工具"
    description="快速生成 MD5、SHA 摘要，并处理 Base64 编码解码，适合校验、签名和内容传输场景。"
    eyebrow="安全与标识"
    :meta="toolMeta"
  >
    <div class="tool-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="MD5" name="md5">
          <div class="tool-content">
            <section class="panel">
              <h2>输入文本</h2>
              <el-input
                v-model="inputText"
                type="textarea"
                :rows="10"
                placeholder="请输入需要计算摘要的文本"
              />
              <div class="actions">
                <el-button type="primary" @click="encryptMD5">计算 MD5</el-button>
              </div>
            </section>

            <section class="panel">
              <h2>MD5 结果</h2>
              <el-input v-model="md5Result" type="textarea" :rows="10" readonly />
              <div class="sub-actions">
                <el-button text @click="copyResult(md5Result)">复制结果</el-button>
              </div>
            </section>
          </div>
        </el-tab-pane>

        <el-tab-pane label="SHA" name="sha">
          <div class="tool-content">
            <section class="panel">
              <h2>输入文本</h2>
              <el-input
                v-model="inputText"
                type="textarea"
                :rows="10"
                placeholder="请输入需要计算摘要的文本"
              />
              <el-radio-group v-model="shaType" class="radio-group">
                <el-radio-button value="SHA-1">SHA-1</el-radio-button>
                <el-radio-button value="SHA-256">SHA-256</el-radio-button>
                <el-radio-button value="SHA-384">SHA-384</el-radio-button>
                <el-radio-button value="SHA-512">SHA-512</el-radio-button>
              </el-radio-group>
              <div class="actions">
                <el-button type="primary" @click="encryptSHA">计算 SHA</el-button>
              </div>
            </section>

            <section class="panel">
              <h2>{{ shaType }} 结果</h2>
              <el-input v-model="shaResult" type="textarea" :rows="10" readonly />
              <div class="sub-actions">
                <el-button text @click="copyResult(shaResult)">复制结果</el-button>
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
import CryptoJS from "crypto-js";
import ToolPageShell from "@features/tools/ui/ToolPageShell.vue";

const activeTab = ref("md5");
const inputText = ref("");
const md5Result = ref("");
const shaType = ref("SHA-256");
const shaResult = ref("");
const base64Input = ref("");
const base64Result = ref("");

const toolMeta = computed(() => [
  { label: "当前模式", value: activeTab.value.toUpperCase() },
  { label: "SHA 算法", value: shaType.value },
  { label: "输出长度", value: `${getCurrentResult().length} 字符` },
]);

const ensureInput = (value: string) => {
  if (!value) {
    ElMessage.warning("请输入文本");
    return false;
  }

  return true;
};

const encryptMD5 = () => {
  if (!ensureInput(inputText.value)) {
    return;
  }

  md5Result.value = CryptoJS.MD5(inputText.value).toString();
  ElMessage.success("MD5 计算完成");
};

const encryptSHA = () => {
  if (!ensureInput(inputText.value)) {
    return;
  }

  switch (shaType.value) {
    case "SHA-1":
      shaResult.value = CryptoJS.SHA1(inputText.value).toString();
      break;
    case "SHA-256":
      shaResult.value = CryptoJS.SHA256(inputText.value).toString();
      break;
    case "SHA-384":
      shaResult.value = CryptoJS.SHA384(inputText.value).toString();
      break;
    case "SHA-512":
      shaResult.value = CryptoJS.SHA512(inputText.value).toString();
      break;
  }

  ElMessage.success("SHA 计算完成");
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
  if (!ensureInput(base64Input.value)) {
    return;
  }

  base64Result.value = utf8ToBase64(base64Input.value);
  ElMessage.success("Base64 编码完成");
};

const decodeBase64 = () => {
  if (!ensureInput(base64Input.value)) {
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
  if (activeTab.value === "md5") {
    return md5Result.value;
  }

  if (activeTab.value === "sha") {
    return shaResult.value;
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

.radio-group {
  margin: 14px 0 0;
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
