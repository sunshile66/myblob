<template>
  <ToolPageShell
    title="加密与摘要工具"
    description="快速生成 MD5、SHA、SM3 摘要，并处理 RC4、Base64 编码解码，适合校验、签名和内容传输场景。"
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

        <el-tab-pane label="SM3" name="sm3">
          <div class="tool-content">
            <section class="panel">
              <h2>输入数据</h2>
              <el-input
                v-model="sm3Input"
                type="textarea"
                :rows="10"
                placeholder="请输入需要计算国密 SM3 摘要的内容"
              />
              <div class="option-row">
                <el-select v-model="sm3InputType" style="width: 150px">
                  <el-option label="UTF-8 文本" value="utf8" />
                  <el-option label="Hex 十六进制" value="hex" />
                  <el-option label="Base64" value="base64" />
                </el-select>
                <el-radio-group v-model="sm3OutputType">
                  <el-radio-button value="hex">Hex</el-radio-button>
                  <el-radio-button value="base64">Base64</el-radio-button>
                </el-radio-group>
              </div>
              <div class="actions">
                <el-button type="primary" @click="calculateSM3">计算 SM3</el-button>
                <el-button @click="loadSM3Sample">标准向量</el-button>
              </div>
            </section>

            <section class="panel">
              <h2>SM3 结果</h2>
              <el-input v-model="sm3Result" type="textarea" :rows="10" readonly />
              <div class="sub-actions">
                <el-button text @click="copyResult(sm3Result)">复制结果</el-button>
              </div>
            </section>
          </div>
        </el-tab-pane>

        <el-tab-pane label="RC4" name="rc4">
          <div class="tool-content">
            <section class="panel">
              <h2>输入文本</h2>
              <el-input
                v-model="rc4Input"
                type="textarea"
                :rows="10"
                placeholder="请输入需要 RC4 加密或解密的内容"
              />
              <el-input
                v-model="rc4Key"
                class="key-input"
                placeholder="请输入密钥"
                show-password
              />
              <div class="option-row">
                <el-radio-group v-model="rc4CipherFormat">
                  <el-radio-button value="base64">密文 Base64</el-radio-button>
                  <el-radio-button value="hex">密文 Hex</el-radio-button>
                </el-radio-group>
              </div>
              <div class="actions">
                <el-button type="primary" @click="encryptRC4">RC4 加密</el-button>
                <el-button @click="decryptRC4">RC4 解密</el-button>
              </div>
            </section>

            <section class="panel">
              <h2>RC4 结果</h2>
              <el-input v-model="rc4Result" type="textarea" :rows="10" readonly />
              <div class="sub-actions">
                <el-button text @click="copyResult(rc4Result)">复制结果</el-button>
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
import { computed, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import CryptoJS from "crypto-js";
import ToolPageShell from "@features/tools/ui/ToolPageShell.vue";
import { sm3Base64, sm3Hex } from "@/utils/sm3";

const activeTab = ref("md5");
const route = useRoute();
const inputText = ref("");
const md5Result = ref("");
const shaType = ref("SHA-256");
const shaResult = ref("");
const sm3Input = ref("");
const sm3InputType = ref<"utf8" | "hex" | "base64">("utf8");
const sm3OutputType = ref<"hex" | "base64">("hex");
const sm3Result = ref("");
const rc4Input = ref("");
const rc4Key = ref("");
const rc4CipherFormat = ref<"base64" | "hex">("base64");
const rc4Result = ref("");
const base64Input = ref("");
const base64Result = ref("");
const textEncoder = new TextEncoder();
const textDecoder = new TextDecoder();

const toolMeta = computed(() => [
  { label: "当前模式", value: activeTab.value.toUpperCase() },
  { label: "算法", value: activeAlgorithm.value },
  { label: "输出长度", value: `${getCurrentResult().length} 字符` },
]);

const activeAlgorithm = computed(() => {
  if (activeTab.value === "sha") return shaType.value;
  if (activeTab.value === "sm3") return "GM/T 0004-2012";
  if (activeTab.value === "rc4") return "RC4";
  if (activeTab.value === "base64") return "Base64";
  return "MD5";
});

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

const hexToBytes = (value: string) => {
  const clean = value.replace(/\s/g, "");
  if (!clean || clean.length % 2 !== 0 || /[^0-9a-f]/i.test(clean)) {
    throw new Error("Hex 输入必须是偶数长度的十六进制字符");
  }
  return Uint8Array.from(clean.match(/.{2}/g) || [], (chunk) => Number.parseInt(chunk, 16));
};

const base64ToBytes = (value: string) => {
  const binary = atob(value.replace(/\s/g, ""));
  return Uint8Array.from(binary, (item) => item.charCodeAt(0));
};

const bytesFromSM3Input = () => {
  if (sm3InputType.value === "hex") return hexToBytes(sm3Input.value);
  if (sm3InputType.value === "base64") return base64ToBytes(sm3Input.value);
  return textEncoder.encode(sm3Input.value);
};

const calculateSM3 = () => {
  if (!ensureInput(sm3Input.value)) {
    return;
  }

  try {
    const bytes = bytesFromSM3Input();
    sm3Result.value = sm3OutputType.value === "base64" ? sm3Base64(bytes) : sm3Hex(bytes);
    ElMessage.success("SM3 计算完成");
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "SM3 输入解析失败");
  }
};

const loadSM3Sample = () => {
  sm3Input.value = "abc";
  sm3InputType.value = "utf8";
  sm3OutputType.value = "hex";
  calculateSM3();
};

const encryptRC4 = () => {
  if (!ensureInput(rc4Input.value) || !ensureInput(rc4Key.value)) {
    return;
  }

  if (rc4CipherFormat.value === "hex") {
    const encrypted = CryptoJS.RC4.encrypt(rc4Input.value, CryptoJS.enc.Utf8.parse(rc4Key.value));
    rc4Result.value = encrypted.ciphertext.toString(CryptoJS.enc.Hex);
  } else {
    rc4Result.value = CryptoJS.RC4.encrypt(rc4Input.value, rc4Key.value).toString();
  }
  ElMessage.success("RC4 加密完成");
};

const decryptRC4 = () => {
  if (!ensureInput(rc4Input.value) || !ensureInput(rc4Key.value)) {
    return;
  }

  try {
    const decrypted = rc4CipherFormat.value === "hex"
      ? CryptoJS.RC4.decrypt(
          CryptoJS.lib.CipherParams.create({ ciphertext: CryptoJS.enc.Hex.parse(rc4Input.value.replace(/\s/g, "")) }),
          CryptoJS.enc.Utf8.parse(rc4Key.value),
        ).toString(CryptoJS.enc.Utf8)
      : CryptoJS.RC4.decrypt(rc4Input.value.replace(/\s/g, ""), rc4Key.value).toString(CryptoJS.enc.Utf8);
    if (!decrypted) {
      ElMessage.error("RC4 解密失败，请检查密钥或密文格式");
      return;
    }
    rc4Result.value = decrypted;
    ElMessage.success("RC4 解密完成");
  } catch {
    ElMessage.error("RC4 解密失败，请检查密钥或密文格式");
  }
};

const utf8ToBase64 = (value: string) => {
  const bytes = textEncoder.encode(value);
  let binary = "";
  bytes.forEach((byte) => {
    binary += String.fromCharCode(byte);
  });
  return btoa(binary);
};

const base64ToUtf8 = (value: string) => {
  const binary = atob(value.replace(/\s/g, ""));
  const bytes = Uint8Array.from(binary, (item) => item.charCodeAt(0));
  return textDecoder.decode(bytes);
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

  if (activeTab.value === "sm3") {
    return sm3Result.value;
  }

  if (activeTab.value === "rc4") {
    return rc4Result.value;
  }

  return base64Result.value;
};

watch(
  () => route.meta.encryptTab,
  (tab) => {
    if (tab === "rc4" || tab === "sm3" || tab === "sha" || tab === "base64" || tab === "md5") {
      activeTab.value = tab;
    }
  },
  { immediate: true },
);
</script>

<style scoped>
.tool-tabs {
  padding: 20px;
  border-radius: var(--radius-xl);
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  box-shadow: var(--shadow-sm);
}

.tool-content {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.panel {
  padding: 18px;
  border-radius: var(--radius-lg);
  background: var(--theme-background);
}

.panel h2 {
  margin: 0 0 14px;
  color: var(--theme-text);
  font-size: 20px;
}

.radio-group {
  margin: 14px 0 0;
}

.key-input {
  margin-top: 12px;
}

.actions,
.sub-actions,
.option-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
  align-items: center;
}

.sub-actions {
  justify-content: flex-end;
}

:deep(.el-textarea__inner) { font-family: var(--font-mono); font-size: 14px; line-height: 1.7; }

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

@media (max-width: 960px) {
  .tool-content { grid-template-columns: 1fr; }
  .tool-tabs { padding: 16px; }
}
</style>
