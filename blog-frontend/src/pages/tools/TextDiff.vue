<template>
  <SimpleLayout>
    <div class="text-diff">
      <div class="tool-header">
        <el-button @click="router.push('/tools')" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回工具箱
        </el-button>
        <h1>📝 文本对比工具</h1>
      </div>

      <div class="tool-container">
        <div class="input-section">
          <div class="text-input-card">
            <div class="input-header">
              <h3>原文</h3>
              <div class="input-actions">
                <el-button size="small" @click="clearText1">
                  <el-icon><Delete /></el-icon>
                  清空
                </el-button>
                <el-button size="small" @click="pasteText1">
                  <el-icon><DocumentCopy /></el-icon>
                  粘贴
                </el-button>
              </div>
            </div>
            <el-input
              v-model="text1"
              type="textarea"
              :rows="12"
              placeholder="请输入原文..."
              class="text-input"
            />
            <div class="text-stats">
              <span>字符数：{{ text1.length }}</span>
              <span>行数：{{ text1Lines }}</span>
            </div>
          </div>

          <div class="text-input-card">
            <div class="input-header">
              <h3>修改后</h3>
              <div class="input-actions">
                <el-button size="small" @click="clearText2">
                  <el-icon><Delete /></el-icon>
                  清空
                </el-button>
                <el-button size="small" @click="pasteText2">
                  <el-icon><DocumentCopy /></el-icon>
                  粘贴
                </el-button>
              </div>
            </div>
            <el-input
              v-model="text2"
              type="textarea"
              :rows="12"
              placeholder="请输入修改后的文本..."
              class="text-input"
            />
            <div class="text-stats">
              <span>字符数：{{ text2.length }}</span>
              <span>行数：{{ text2Lines }}</span>
            </div>
          </div>
        </div>

        <div class="action-section">
          <el-button
            type="primary"
            size="large"
            @click="compareText"
            class="compare-btn"
          >
            <el-icon><Search /></el-icon>
            开始对比
          </el-button>
          <el-button @click="swapTexts" class="swap-btn">
            <el-icon><Sort /></el-icon>
            交换文本
          </el-button>
        </div>

        <div v-if="hasResult" class="result-section">
          <div class="result-header">
            <h3>📊 对比结果</h3>
            <div class="result-stats">
              <el-tag type="success">+{{ addedCount }} 新增</el-tag>
              <el-tag type="danger">-{{ removedCount }} 删除</el-tag>
              <el-tag>{{ unchangedCount }} 未变</el-tag>
            </div>
          </div>

          <div class="result-tabs">
            <el-tabs v-model="activeTab">
              <el-tab-pane label="并排对比" name="side">
                <div class="diff-view side-by-side">
                  <div class="diff-column">
                    <div class="diff-column-header">原文</div>
                    <div class="diff-content" v-html="originalDiff"></div>
                  </div>
                  <div class="diff-column">
                    <div class="diff-column-header">修改后</div>
                    <div class="diff-content" v-html="modifiedDiff"></div>
                  </div>
                </div>
              </el-tab-pane>
              <el-tab-pane label="统一视图" name="unified">
                <div class="diff-view unified" v-html="unifiedDiff"></div>
              </el-tab-pane>
              <el-tab-pane label="统计信息" name="stats">
                <div class="stats-card">
                  <div class="stat-item">
                    <div class="stat-value">{{ text1.length }}</div>
                    <div class="stat-label">原文字符数</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-value">{{ text2.length }}</div>
                    <div class="stat-label">修改后字符数</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-value">{{ diffPercent }}%</div>
                    <div class="stat-label">差异比例</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-value">{{ similarity }}%</div>
                    <div class="stat-label">相似度</div>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
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
  ArrowLeft,
  Delete,
  DocumentCopy,
  Search,
  Sort,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import SimpleLayout from "@/layout/SimpleLayout.vue";

const router = useRouter();

const text1 = ref("");
const text2 = ref("");
const hasResult = ref(false);
const activeTab = ref("side");

const text1Lines = computed(() => text1.value.split("\n").length);
const text2Lines = computed(() => text2.value.split("\n").length);

const addedCount = ref(0);
const removedCount = ref(0);
const unchangedCount = ref(0);

const originalDiff = ref("");
const modifiedDiff = ref("");
const unifiedDiff = ref("");

const diffPercent = computed(() => {
  const total = text1.value.length || 1;
  return Math.round(((addedCount.value + removedCount.value) / total) * 100);
});

const similarity = computed(() => {
  const total = text1.value.length || 1;
  return Math.round(
    (1 - (addedCount.value + removedCount.value) / total) * 100
  );
});

const compareText = () => {
  if (!text1.value.trim() && !text2.value.trim()) {
    ElMessage.warning("请至少输入一段文本");
    return;
  }

  const lines1 = text1.value.split("\n");
  const lines2 = text2.value.split("\n");

  const result = computeDiff(lines1, lines2);

  originalDiff.value = result.original
    .map((line: string) => {
      if (line.startsWith("-")) {
        return `<div class="diff-line removed"><span class="line-num">${result.lineNums.original.shift()}</span>${line.substring(
          1
        )}</div>`;
      }
      return `<div class="diff-line unchanged"><span class="line-num">${result.lineNums.original.shift()}</span>${line}</div>`;
    })
    .join("");

  modifiedDiff.value = result.modified
    .map((line: string) => {
      if (line.startsWith("+")) {
        return `<div class="diff-line added"><span class="line-num">${result.lineNums.modified.shift()}</span>${line.substring(
          1
        )}</div>`;
      }
      return `<div class="diff-line unchanged"><span class="line-num">${result.lineNums.modified.shift()}</span>${line}</div>`;
    })
    .join("");

  unifiedDiff.value = result.unified
    .map((line: string) => {
      if (line.startsWith("+")) {
        return `<div class="diff-line added">${line}</div>`;
      } else if (line.startsWith("-")) {
        return `<div class="diff-line removed">${line}</div>`;
      }
      return `<div class="diff-line unchanged">${line}</div>`;
    })
    .join("");

  addedCount.value = result.addedCount;
  removedCount.value = result.removedCount;
  unchangedCount.value = result.unchangedCount;
  hasResult.value = true;
};

const computeDiff = (lines1: string[], lines2: string[]) => {
  const original: string[] = [];
  const modified: string[] = [];
  const unified: string[] = [];
  const lineNums = {
    original: [] as number[],
    modified: [] as number[],
  };

  let addedCount = 0;
  let removedCount = 0;
  let unchangedCount = 0;

  const maxLen = Math.max(lines1.length, lines2.length);

  for (let i = 0; i < maxLen; i++) {
    const line1 = lines1[i];
    const line2 = lines2[i];

    if (line1 === undefined) {
      original.push(` ${line1 || ""}`);
      modified.push(`+${line2}`);
      unified.push(`+${line2}`);
      lineNums.original.push(i + 1);
      lineNums.modified.push(i + 1);
      addedCount++;
    } else if (line2 === undefined) {
      original.push(`-${line1}`);
      modified.push(` ${line2 || ""}`);
      unified.push(`-${line1}`);
      lineNums.original.push(i + 1);
      lineNums.modified.push(i + 1);
      removedCount++;
    } else if (line1 === line2) {
      original.push(` ${line1}`);
      modified.push(` ${line2}`);
      unified.push(`  ${line1}`);
      lineNums.original.push(i + 1);
      lineNums.modified.push(i + 1);
      unchangedCount++;
    } else {
      original.push(`-${line1}`);
      modified.push(`+${line2}`);
      unified.push(`-${line1}`);
      unified.push(`+${line2}`);
      lineNums.original.push(i + 1, i + 1);
      lineNums.modified.push(i + 1, i + 1);
      removedCount++;
      addedCount++;
    }
  }

  return {
    original,
    modified,
    unified,
    lineNums,
    addedCount,
    removedCount,
    unchangedCount,
  };
};

const clearText1 = () => {
  text1.value = "";
  hasResult.value = false;
};

const clearText2 = () => {
  text2.value = "";
  hasResult.value = false;
};

const pasteText1 = async () => {
  try {
    text1.value = await navigator.clipboard.readText();
  } catch {
    ElMessage.error("粘贴失败");
  }
};

const pasteText2 = async () => {
  try {
    text2.value = await navigator.clipboard.readText();
  } catch {
    ElMessage.error("粘贴失败");
  }
};

const swapTexts = () => {
  const temp = text1.value;
  text1.value = text2.value;
  text2.value = temp;
  hasResult.value = false;
};
</script>

<style scoped>
.text-diff {
  background: var(--theme-background);
  min-height: calc(100vh - 80px);
  padding: 24px;
}

.tool-header {
  max-width: 1200px;
  margin: 0 auto 24px;
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

.tool-header h1 {
  font-size: 28px;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.tool-container {
  max-width: 1200px;
  margin: 0 auto;
}

.input-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.text-input-card {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  padding: 20px;
  box-shadow: var(--glass-shadow);
}

.input-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.input-header h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0;
}

.input-actions {
  display: flex;
  gap: 8px;
}

.text-input {
  margin-bottom: 12px;
}

.text-input :deep(.el-textarea__inner) {
  font-family: monospace;
  font-size: 13px;
  line-height: 1.6;
}

.text-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--theme-text-secondary);
}

.action-section {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.compare-btn,
.swap-btn {
  flex: 1;
  font-weight: 600;
}

.result-section {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  padding: 24px;
  box-shadow: var(--glass-shadow);
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.result-header h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0;
}

.result-stats {
  display: flex;
  gap: 8px;
}

.result-tabs {
  margin-top: 20px;
}

.diff-view {
  font-family: monospace;
  font-size: 13px;
  line-height: 1.6;
}

.side-by-side {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.diff-column {
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.diff-column-header {
  padding: 12px 16px;
  background: var(--theme-background);
  border-bottom: 1px solid var(--theme-border);
  font-weight: 600;
  color: var(--theme-text);
}

.diff-content {
  max-height: 400px;
  overflow-y: auto;
}

.diff-line {
  padding: 4px 16px;
  display: flex;
  gap: 12px;
}

.diff-line .line-num {
  min-width: 40px;
  text-align: right;
  color: var(--theme-text-secondary);
  font-size: 11px;
  padding-top: 2px;
}

.diff-line.removed {
  background: rgba(239, 68, 68, 0.1);
}

.diff-line.removed .line-num {
  color: #ef4444;
}

.diff-line.added {
  background: rgba(16, 185, 129, 0.1);
}

.diff-line.added .line-num {
  color: #10b981;
}

.diff-line.unchanged {
  background: transparent;
}

.unified {
  max-height: 500px;
  overflow-y: auto;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-md);
  padding: 16px;
}

.stats-card {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  padding: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-lg);
}

.stat-value {
  font-size: 32px;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 13px;
  color: var(--theme-text-secondary);
}

@media (max-width: 992px) {
  .input-section {
    grid-template-columns: 1fr;
  }

  .side-by-side {
    grid-template-columns: 1fr;
  }

  .stats-card {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}

@media (max-width: 768px) {
  .text-diff {
    padding: 12px;
  }

  .tool-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .tool-header h1 {
    font-size: 22px;
  }

  .action-section {
    flex-direction: column;
  }

  .stats-card {
    grid-template-columns: 1fr;
  }
}
</style>
