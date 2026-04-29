<template>
  <ToolPageShell
    title="正则测试器"
    description="测试表达式、查看匹配结果，并即时预览替换后的内容。"
    eyebrow="开发调试"
    :meta="meta"
  >
    <div class="workspace">
      <section class="panel">
        <h2>表达式与选项</h2>
        <el-input v-model="pattern" placeholder="例如 ^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$" />

        <div class="flags">
          <el-checkbox v-model="flags.global">g 全局</el-checkbox>
          <el-checkbox v-model="flags.ignoreCase">i 忽略大小写</el-checkbox>
          <el-checkbox v-model="flags.multiline">m 多行</el-checkbox>
          <el-checkbox v-model="flags.dotAll">s 点号匹配换行</el-checkbox>
        </div>

        <h2>测试文本</h2>
        <el-input
          v-model="testText"
          type="textarea"
          :rows="12"
          placeholder="把需要测试的文本粘贴到这里"
        />

        <h2>替换模板</h2>
        <el-input v-model="replacement" placeholder="例如 [$1]" />
      </section>

      <section class="panel">
        <h2>结果</h2>
        <el-alert
          v-if="regexError"
          :title="regexError"
          type="error"
          show-icon
          :closable="false"
          class="alert"
        />

        <div class="summary">
          <article class="summary-card">
            <span>匹配数量</span>
            <strong>{{ matches.length }}</strong>
          </article>
          <article class="summary-card">
            <span>当前 flags</span>
            <strong>{{ activeFlags || "无" }}</strong>
          </article>
          <article class="summary-card">
            <span>替换预览</span>
            <strong>{{ replacedText ? "已生成" : "暂无" }}</strong>
          </article>
        </div>

        <div v-if="matches.length" class="match-list">
          <article v-for="match in matches" :key="match.key" class="match-card">
            <span>{{ match.range }}</span>
            <strong>{{ match.text }}</strong>
            <p v-if="match.groups.length">分组: {{ match.groups.join(" | ") }}</p>
          </article>
        </div>

        <el-empty v-else-if="!regexError" description="当前没有匹配结果" />

        <div class="replace-box">
          <div class="replace-box__head">
            <h3>替换预览</h3>
            <el-button text @click="copy(replacedText)">复制预览</el-button>
          </div>
          <pre>{{ replacedText || "替换结果会显示在这里" }}</pre>
        </div>
      </section>
    </div>
  </ToolPageShell>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { ElMessage } from "element-plus";
import ToolPageShell from "@features/tools/ui/ToolPageShell.vue";

const pattern = ref("");
const testText = ref("");
const replacement = ref("");
const flags = ref({
  global: true,
  ignoreCase: false,
  multiline: false,
  dotAll: false,
});

const activeFlags = computed(() =>
  [
    flags.value.global ? "g" : "",
    flags.value.ignoreCase ? "i" : "",
    flags.value.multiline ? "m" : "",
    flags.value.dotAll ? "s" : "",
  ].join("")
);

const regexState = computed(() => {
  if (!pattern.value) {
    return { regex: null as RegExp | null, error: "" };
  }

  try {
    return {
      regex: new RegExp(pattern.value, activeFlags.value),
      error: "",
    };
  } catch (error) {
    return {
      regex: null,
      error: error instanceof Error ? error.message : "正则表达式无效",
    };
  }
});

const regexError = computed(() => regexState.value.error);

const matches = computed(() => {
  const regex = regexState.value.regex;
  if (!regex || !testText.value) {
    return [];
  }

  const iterable = new RegExp(
    regex.source,
    regex.flags.includes("g") ? regex.flags : `${regex.flags}g`
  );

  return Array.from(testText.value.matchAll(iterable)).map((match, index) => ({
    key: `${index}-${match.index ?? 0}`,
    range: `#${index + 1} · ${match.index ?? 0}-${(match.index ?? 0) + match[0].length}`,
    text: match[0],
    groups: match.slice(1).filter(Boolean),
  }));
});

const replacedText = computed(() => {
  const regex = regexState.value.regex;
  if (!regex || !testText.value) {
    return "";
  }
  return testText.value.replace(regex, replacement.value);
});

const meta = computed(() => [
  { label: "匹配数量", value: `${matches.value.length}` },
  { label: "当前 flags", value: activeFlags.value || "无" },
  { label: "替换预览", value: replacedText.value ? "已生成" : "暂无" },
]);

const copy = async (value: string) => {
  if (!value) {
    ElMessage.warning("当前没有内容可复制");
    return;
  }

  await navigator.clipboard.writeText(value);
  ElMessage.success("已复制");
};
</script>

<style scoped>
.workspace {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.panel {
  padding: 20px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(148, 163, 184, 0.12);
  box-shadow: 0 22px 38px rgba(15, 23, 42, 0.06);
}

.panel h2,
.replace-box__head h3 {
  margin: 0 0 14px;
  color: #0f172a;
  font-size: 20px;
}

.flags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin: 14px 0 20px;
}

.alert {
  margin-bottom: 14px;
}

.summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.summary-card,
.match-card {
  padding: 16px;
  border-radius: 18px;
  background: rgba(15, 23, 42, 0.04);
}

.summary-card span,
.match-card span {
  display: block;
  margin-bottom: 8px;
  color: #64748b;
  font-size: 13px;
}

.summary-card strong,
.match-card strong {
  color: #0f172a;
  font-size: 16px;
  line-height: 1.6;
  word-break: break-word;
}

.match-card p {
  margin: 8px 0 0;
  color: #475569;
  font-size: 13px;
}

.match-list {
  display: grid;
  gap: 12px;
  margin-bottom: 16px;
}

.replace-box {
  margin-top: 14px;
  padding: 18px;
  border-radius: 20px;
  background: #0f172a;
}

.replace-box__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.replace-box pre {
  margin: 0;
  color: #e2e8f0;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 960px) {
  .workspace,
  .summary {
    grid-template-columns: 1fr;
  }

  .panel {
    padding: 18px;
  }
}
</style>
