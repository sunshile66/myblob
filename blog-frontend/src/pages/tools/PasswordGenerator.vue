<template>
  <SimpleLayout>
    <div class="tool-page">
      <div class="tool-container">
        <div class="page-header">
          <div class="header-with-back">
            <el-button @click="router.push('/tools')" class="back-btn">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <div class="header-text">
              <h1>🔐 密码生成器</h1>
              <p>安全、随机的强密码生成</p>
            </div>
          </div>
        </div>

        <div class="tool-card">
          <div class="result-section">
            <div class="password-display">
              <el-input
                v-model="generatedPassword"
                readonly
                placeholder="点击生成密码"
                size="large"
              >
                <template #append>
                  <el-button @click="copyPassword" :icon="DocumentCopy"
                    >复制</el-button
                  >
                </template>
              </el-input>
            </div>
            <div class="strength-indicator">
              <span>密码强度:</span>
              <div class="strength-bar">
                <div
                  class="strength-fill"
                  :style="{
                    width: strengthPercent + '%',
                    background: strengthColor,
                  }"
                ></div>
              </div>
              <span class="strength-text" :style="{ color: strengthColor }">{{
                strengthText
              }}</span>
            </div>
          </div>

          <div class="options-section">
            <div class="option-group">
              <label>密码长度</label>
              <div class="length-control">
                <el-slider
                  v-model="passwordLength"
                  :min="4"
                  :max="64"
                  show-input
                />
              </div>
            </div>

            <div class="option-group">
              <label>字符类型</label>
              <div class="checkbox-group">
                <el-checkbox v-model="includeUppercase"
                  >大写字母 (A-Z)</el-checkbox
                >
                <el-checkbox v-model="includeLowercase"
                  >小写字母 (a-z)</el-checkbox
                >
                <el-checkbox v-model="includeNumbers">数字 (0-9)</el-checkbox>
                <el-checkbox v-model="includeSymbols"
                  >特殊符号 (!@#$%^&*)</el-checkbox
                >
              </div>
            </div>

            <el-button
              type="primary"
              size="large"
              @click="generatePassword"
              class="generate-btn"
            >
              <el-icon><Refresh /></el-icon>
              生成密码
            </el-button>
          </div>

          <div class="history-section">
            <h3>历史记录</h3>
            <div class="history-list">
              <div
                v-for="(pwd, index) in passwordHistory"
                :key="index"
                class="history-item"
              >
                <span class="history-password">{{ pwd }}</span>
                <div class="history-actions">
                  <el-button
                    size="small"
                    @click="copyToClipboard(pwd)"
                    :icon="DocumentCopy"
                  />
                  <el-button
                    size="small"
                    type="danger"
                    @click="removeHistory(index)"
                    :icon="Delete"
                  />
                </div>
              </div>
              <div v-if="passwordHistory.length === 0" class="empty-history">
                暂无历史记录
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import {
  DocumentCopy,
  Refresh,
  Delete,
  ArrowLeft,
} from "@element-plus/icons-vue";
import SimpleLayout from "@/layout/SimpleLayout.vue";
import { ElMessage } from "element-plus";

const router = useRouter();

const generatedPassword = ref("");
const passwordLength = ref(16);
const includeUppercase = ref(true);
const includeLowercase = ref(true);
const includeNumbers = ref(true);
const includeSymbols = ref(true);
const passwordHistory = ref<string[]>([]);

const UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
const LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
const NUMBERS = "0123456789";
const SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

const getCharset = () => {
  let charset = "";
  if (includeUppercase.value) charset += UPPERCASE;
  if (includeLowercase.value) charset += LOWERCASE;
  if (includeNumbers.value) charset += NUMBERS;
  if (includeSymbols.value) charset += SYMBOLS;
  return charset;
};

const generatePassword = () => {
  const charset = getCharset();
  if (charset === "") {
    ElMessage.warning("请至少选择一种字符类型");
    return;
  }

  let password = "";
  const array = new Uint32Array(passwordLength.value);
  crypto.getRandomValues(array);

  for (let i = 0; i < passwordLength.value; i++) {
    password += charset[array[i] % charset.length];
  }

  generatedPassword.value = password;

  if (password && !passwordHistory.value.includes(password)) {
    passwordHistory.value.unshift(password);
    if (passwordHistory.value.length > 10) {
      passwordHistory.value.pop();
    }
  }
};

const copyPassword = () => {
  if (generatedPassword.value) {
    copyToClipboard(generatedPassword.value);
  }
};

const copyToClipboard = (text: string) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success("已复制到剪贴板");
  });
};

const removeHistory = (index: number) => {
  passwordHistory.value.splice(index, 1);
};

const strengthPercent = computed(() => {
  let score = 0;
  if (passwordLength.value >= 8) score += 20;
  if (passwordLength.value >= 12) score += 20;
  if (passwordLength.value >= 16) score += 20;
  if (includeUppercase.value) score += 10;
  if (includeLowercase.value) score += 10;
  if (includeNumbers.value) score += 10;
  if (includeSymbols.value) score += 10;
  return Math.min(score, 100);
});

const strengthColor = computed(() => {
  if (strengthPercent.value < 40) return "#ef4444";
  if (strengthPercent.value < 70) return "#f59e0b";
  return "#10b981";
});

const strengthText = computed(() => {
  if (strengthPercent.value < 40) return "弱";
  if (strengthPercent.value < 70) return "中等";
  return "强";
});

generatePassword();
</script>

<style scoped>
.tool-page {
  background: var(--theme-background);
  min-height: calc(100vh - 80px);
  padding: 40px 0;
}

.tool-container {
  max-width: 700px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  margin-bottom: 40px;
}

.header-with-back {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  color: var(--theme-text);
}

.back-btn:hover {
  border-color: var(--theme-primary);
  color: var(--theme-primary);
}

.header-text {
  flex: 1;
  text-align: center;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 10px 0;
}

.page-header p {
  font-size: 15px;
  color: var(--theme-text-secondary);
  margin: 0;
}

.tool-card {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  padding: 32px;
  box-shadow: var(--glass-shadow);
}

.result-section {
  margin-bottom: 32px;
}

.password-display {
  margin-bottom: 20px;
}

.strength-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: var(--theme-text-secondary);
}

.strength-bar {
  flex: 1;
  height: 8px;
  background: var(--theme-border);
  border-radius: 4px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  transition: all var(--transition-normal);
}

.strength-text {
  font-weight: 600;
  min-width: 40px;
}

.options-section {
  margin-bottom: 32px;
}

.option-group {
  margin-bottom: 24px;
}

.option-group label {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: var(--theme-text);
  margin-bottom: 12px;
}

.length-control {
  padding: 0 10px;
}

.checkbox-group {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.generate-btn {
  width: 100%;
  font-weight: 600;
}

.history-section h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0 0 16px 0;
}

.history-list {
  max-height: 200px;
  overflow-y: auto;
}

.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-md);
  margin-bottom: 8px;
}

.history-password {
  font-family: monospace;
  font-size: 14px;
  color: var(--theme-text);
}

.empty-history {
  text-align: center;
  color: var(--theme-text-secondary);
  padding: 32px;
}

@media (max-width: 768px) {
  .tool-card {
    padding: 20px;
  }

  .checkbox-group {
    grid-template-columns: 1fr;
  }
}
</style>
