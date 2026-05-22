<template>
  <ToolPageShell
    title="密码生成器"
    description="生成随机密码、PIN 和可读口令，支持批量生成、排除易混字符与强度评估。"
    eyebrow="安全与标识"
    :meta="meta"
  >
    <div class="workbench">
      <section class="panel panel--main">
        <div class="section-head">
          <div>
            <h2>生成结果</h2>
            <p>优先使用浏览器加密随机数，生成后可直接复制或批量导出。</p>
          </div>
          <el-button type="primary" :icon="Refresh" @click="generatePasswords">重新生成</el-button>
        </div>

        <div class="primary-result">
          <el-input v-model="primaryPassword" readonly size="large" placeholder="生成结果会显示在这里">
            <template #append>
              <el-button :icon="DocumentCopy" @click="copy(primaryPassword)">复制</el-button>
            </template>
          </el-input>
          <div class="strength-row">
            <span>{{ strength.label }}</span>
            <el-progress :percentage="strength.score" :color="strength.color" :show-text="false" />
            <strong :style="{ color: strength.color }">{{ strengthText }}</strong>
          </div>
        </div>

        <div class="batch-list">
          <div class="batch-head">
            <h3>批量结果</h3>
            <div class="batch-actions">
              <el-button text :icon="DocumentCopy" @click="copy(batchPasswords.join('\n'))">复制全部</el-button>
              <el-button text :icon="Delete" @click="clearHistory">清空历史</el-button>
            </div>
          </div>

          <button
            v-for="password in batchPasswords"
            :key="password"
            class="password-row"
            @click="copy(password)"
          >
            <code>{{ password }}</code>
            <span>点击复制</span>
          </button>

          <el-empty v-if="!batchPasswords.length" description="暂无生成结果" />
        </div>
      </section>

      <aside class="panel panel--settings">
        <h2>生成设置</h2>

        <div class="setting-group">
          <label>生成模式</label>
          <el-segmented v-model="mode" :options="modeOptions" @change="generatePasswords" />
        </div>

        <div class="setting-group">
          <label>长度：{{ length }}</label>
          <el-slider v-model="length" :min="mode === 'pin' ? 4 : 8" :max="mode === 'passphrase' ? 8 : 96" @change="generatePasswords" />
        </div>

        <div class="setting-group">
          <label>批量数量：{{ count }}</label>
          <el-slider v-model="count" :min="1" :max="20" @change="generatePasswords" />
        </div>

        <div v-if="mode === 'password'" class="setting-group">
          <label>字符集</label>
          <div class="check-grid">
            <el-checkbox v-model="options.uppercase" @change="generatePasswords">大写字母</el-checkbox>
            <el-checkbox v-model="options.lowercase" @change="generatePasswords">小写字母</el-checkbox>
            <el-checkbox v-model="options.numbers" @change="generatePasswords">数字</el-checkbox>
            <el-checkbox v-model="options.symbols" @change="generatePasswords">符号</el-checkbox>
          </div>
        </div>

        <div class="setting-group">
          <label>可读性</label>
          <el-checkbox v-model="excludeAmbiguous" @change="generatePasswords">排除 0/O、1/l/I 等易混字符</el-checkbox>
          <el-checkbox v-if="mode === 'password'" v-model="mustIncludeEachType" @change="generatePasswords">确保每类字符至少出现一次</el-checkbox>
        </div>

        <div class="setting-group">
          <label>自定义符号</label>
          <el-input v-model="customSymbols" :disabled="mode !== 'password' || !options.symbols" @change="generatePasswords" />
        </div>
      </aside>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { Delete, DocumentCopy, Refresh } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import ToolPageShell from "@features/tools/ui/ToolPageShell.vue";

type Mode = "password" | "pin" | "passphrase";

const mode = ref<Mode>("password");
const length = ref(18);
const count = ref(5);
const excludeAmbiguous = ref(true);
const mustIncludeEachType = ref(true);
const customSymbols = ref("!@#$%^&*_-+=?");
const batchPasswords = ref<string[]>([]);

const options = reactive({
  uppercase: true,
  lowercase: true,
  numbers: true,
  symbols: true,
});

const modeOptions = [
  { label: "随机密码", value: "password" },
  { label: "数字 PIN", value: "pin" },
  { label: "可读口令", value: "passphrase" },
];

const WORDS = [
  "river", "stone", "forest", "cloud", "orbit", "harbor", "silver", "ember",
  "north", "canvas", "maple", "signal", "bright", "anchor", "summer", "planet",
  "copper", "garden", "velvet", "matrix", "rocket", "winter", "meadow", "summit",
];

const CHARSETS = {
  uppercase: "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
  lowercase: "abcdefghijklmnopqrstuvwxyz",
  numbers: "0123456789",
};

const ambiguousChars = /[0O1lI]/g;

const primaryPassword = computed(() => batchPasswords.value[0] || "");

const activePools = computed(() => {
  if (mode.value === "pin") {
    return ["0123456789"];
  }

  const pools: string[] = [];
  if (options.uppercase) pools.push(cleanCharset(CHARSETS.uppercase));
  if (options.lowercase) pools.push(cleanCharset(CHARSETS.lowercase));
  if (options.numbers) pools.push(cleanCharset(CHARSETS.numbers));
  if (options.symbols) pools.push(cleanCharset(customSymbols.value));
  return pools.filter(Boolean);
});

const charsetSize = computed(() => {
  if (mode.value === "passphrase") return WORDS.length;
  return new Set(activePools.value.join("").split("")).size;
});

const entropy = computed(() => {
  if (!primaryPassword.value || charsetSize.value <= 1) return 0;
  const units = mode.value === "passphrase" ? length.value : primaryPassword.value.length;
  return Math.round(units * Math.log2(charsetSize.value));
});

const strength = computed(() => {
  const bits = entropy.value;
  if (bits >= 90) return { label: "很强", score: 100, color: "#059669" };
  if (bits >= 70) return { label: "强", score: 82, color: "#0d9488" };
  if (bits >= 50) return { label: "中等", score: 62, color: "#d97706" };
  if (bits >= 30) return { label: "偏弱", score: 38, color: "#ea580c" };
  return { label: "弱", score: 18, color: "#dc2626" };
});

const strengthText = computed(() => `${strength.value.label} · 约 ${entropy.value} bits`);

const meta = computed(() => [
  { label: "生成模式", value: modeOptions.find((item) => item.value === mode.value)?.label || "随机密码" },
  { label: "熵估算", value: `${entropy.value} bits` },
  { label: "批量数量", value: `${batchPasswords.value.length}` },
]);

const cleanCharset = (value: string) => {
  const cleaned = excludeAmbiguous.value ? value.replace(ambiguousChars, "") : value;
  return Array.from(new Set(cleaned.split(""))).join("");
};

const randomIndex = (max: number) => {
  const array = new Uint32Array(1);
  crypto.getRandomValues(array);
  return array[0] % max;
};

const shuffle = (items: string[]) => {
  const result = [...items];
  for (let index = result.length - 1; index > 0; index -= 1) {
    const swapIndex = randomIndex(index + 1);
    [result[index], result[swapIndex]] = [result[swapIndex], result[index]];
  }
  return result;
};

const generatePassword = () => {
  const pools = activePools.value;
  if (!pools.length) {
    ElMessage.warning("请至少选择一种字符集");
    return "";
  }

  const chars: string[] = [];
  if (mode.value === "password" && mustIncludeEachType.value) {
    pools.forEach((pool) => chars.push(pool[randomIndex(pool.length)]));
  }

  const fullPool = pools.join("");
  while (chars.length < length.value) {
    chars.push(fullPool[randomIndex(fullPool.length)]);
  }

  return shuffle(chars).join("");
};

const generatePin = () => {
  return Array.from({ length: length.value }, () => String(randomIndex(10))).join("");
};

const generatePassphrase = () => {
  return Array.from({ length: length.value }, () => WORDS[randomIndex(WORDS.length)]).join("-");
};

const generateOne = () => {
  if (mode.value === "pin") return generatePin();
  if (mode.value === "passphrase") return generatePassphrase();
  return generatePassword();
};

const generatePasswords = () => {
  const results = new Set<string>();
  while (results.size < count.value) {
    const next = generateOne();
    if (!next) break;
    results.add(next);
  }
  batchPasswords.value = Array.from(results);
};

const copy = async (value: string) => {
  if (!value) {
    ElMessage.warning("没有内容可复制");
    return;
  }
  await navigator.clipboard.writeText(value);
  ElMessage.success("已复制到剪贴板");
};

const clearHistory = () => {
  batchPasswords.value = [];
};

watch(mode, (value) => {
  length.value = value === "pin" ? 6 : value === "passphrase" ? 4 : 18;
  generatePasswords();
});

generatePasswords();
</script>

<style scoped>
.workbench {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 16px;
}

.panel {
  padding: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.section-head,
.batch-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

h2,
h3 {
  margin: 0;
  color: #0f172a;
  letter-spacing: 0;
}

h2 {
  font-size: 18px;
}

h3 {
  font-size: 15px;
}

p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.primary-result {
  display: grid;
  gap: 12px;
  margin-bottom: 18px;
}

.strength-row {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr) 120px;
  align-items: center;
  gap: 10px;
  color: #64748b;
  font-size: 13px;
}

.batch-list {
  display: grid;
  gap: 8px;
}

.batch-actions {
  display: flex;
  gap: 6px;
}

.password-row {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
  background: #f8fafc;
  cursor: pointer;
  text-align: left;
}

.password-row:hover {
  border-color: #14b8a6;
  background: rgba(20, 184, 166, 0.08);
}

.password-row code {
  color: #0f172a;
  font-size: 13px;
  word-break: break-all;
}

.password-row span {
  flex-shrink: 0;
  color: #64748b;
  font-size: 12px;
}

.setting-group {
  display: grid;
  gap: 10px;
  margin-top: 16px;
}

.setting-group label {
  color: #334155;
  font-size: 13px;
  font-weight: 700;
}

.check-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 4px 10px;
}

@media (max-width: 960px) {
  .workbench {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .section-head,
  .batch-head,
  .password-row {
    flex-direction: column;
    align-items: stretch;
  }

  .strength-row {
    grid-template-columns: 1fr;
  }

  .check-grid {
    grid-template-columns: 1fr;
  }
}
</style>
