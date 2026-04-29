<template>
  <SimpleLayout>
    <div class="timestamp-converter">
      <div class="tool-header">
        <el-button @click="router.push('/tools')" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回工具箱
        </el-button>
        <h1>⏱️ 时间戳转换工具</h1>
      </div>

      <div class="tool-container">
        <div class="converter-tabs">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="时间戳转日期" name="timestamp">
              <div class="converter-card">
                <div class="section">
                  <h3>Unix 时间戳</h3>
                  <div class="input-group">
                    <el-input
                      v-model="timestamp"
                      placeholder="输入时间戳（秒或毫秒）"
                      clearable
                    >
                      <template #prefix>
                        <el-icon><Timer /></el-icon>
                      </template>
                    </el-input>
                    <div class="button-group">
                      <el-button type="primary" @click="convertTimestamp">
                        <el-icon><Search /></el-icon>
                        转换
                      </el-button>
                      <el-button @click="getCurrentTimestamp">
                        <el-icon><Refresh /></el-icon>
                        当前时间
                      </el-button>
                    </div>
                  </div>
                  <div class="timestamp-type">
                    <el-radio-group v-model="timestampType">
                      <el-radio label="seconds">秒 (10 位)</el-radio>
                      <el-radio label="milliseconds">毫秒 (13 位)</el-radio>
                    </el-radio-group>
                  </div>
                </div>

                <div class="results" v-if="timestampResults.length">
                  <h4>转换结果</h4>
                  <div class="result-grid">
                    <div
                      class="result-item"
                      v-for="(item, index) in timestampResults"
                      :key="index"
                    >
                      <div class="result-label">{{ item.label }}</div>
                      <div class="result-value">{{ item.value }}</div>
                      <el-button
                        size="small"
                        circle
                        @click="copyResult(item.value)"
                      >
                        <el-icon><DocumentCopy /></el-icon>
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="日期转时间戳" name="datetime">
              <div class="converter-card">
                <div class="section">
                  <h3>日期时间</h3>
                  <div class="input-group">
                    <el-date-picker
                      v-model="dateTime"
                      type="datetime"
                      placeholder="选择日期时间"
                      format="YYYY-MM-DD HH:mm:ss"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      style="width: 100%"
                    />
                    <el-button type="primary" @click="convertDateTime">
                      <el-icon><Search /></el-icon>
                      转换
                    </el-button>
                  </div>
                </div>

                <div class="results" v-if="datetimeResults.length">
                  <h4>转换结果</h4>
                  <div class="result-grid">
                    <div
                      class="result-item"
                      v-for="(item, index) in datetimeResults"
                      :key="index"
                    >
                      <div class="result-label">{{ item.label }}</div>
                      <div class="result-value">{{ item.value }}</div>
                      <el-button
                        size="small"
                        circle
                        @click="copyResult(item.value)"
                      >
                        <el-icon><DocumentCopy /></el-icon>
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="城市时间转换" name="city">
              <div class="converter-card">
                <div class="section">
                  <h3>城市时间转换</h3>
                  <div class="city-input-group">
                    <div class="city-input">
                      <label>源城市</label>
                      <el-select
                        v-model="sourceCity"
                        placeholder="选择城市"
                        filterable
                        style="width: 100%"
                      >
                        <el-option
                          v-for="city in cities"
                          :key="city.value"
                          :label="city.label"
                          :value="city.value"
                        >
                          <span>{{ city.emoji }} {{ city.label }}</span>
                        </el-option>
                      </el-select>
                    </div>
                    <div class="city-input">
                      <label>目标城市</label>
                      <el-select
                        v-model="targetCity"
                        placeholder="选择城市"
                        filterable
                        style="width: 100%"
                      >
                        <el-option
                          v-for="city in cities"
                          :key="city.value"
                          :label="city.label"
                          :value="city.value"
                        >
                          <span>{{ city.emoji }} {{ city.label }}</span>
                        </el-option>
                      </el-select>
                    </div>
                  </div>

                  <div class="time-input-section">
                    <label>输入时间</label>
                    <div class="time-input-group">
                      <el-date-picker
                        v-model="cityDateTime"
                        type="datetime"
                        placeholder="选择日期时间"
                        format="YYYY-MM-DD HH:mm:ss"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%"
                      />
                      <el-button type="primary" @click="convertCityTime">
                        <el-icon><Refresh /></el-icon>
                        转换
                      </el-button>
                    </div>
                  </div>
                </div>

                <div class="results" v-if="cityResults.length">
                  <h4>转换结果</h4>
                  <div class="result-grid">
                    <div
                      class="result-item"
                      v-for="(item, index) in cityResults"
                      :key="index"
                    >
                      <div class="result-label">{{ item.label }}</div>
                      <div class="result-value">{{ item.value }}</div>
                      <el-button
                        size="small"
                        circle
                        @click="copyResult(item.value)"
                      >
                        <el-icon><DocumentCopy /></el-icon>
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  ArrowLeft,
  DocumentCopy,
  Timer,
  Refresh,
  Search,
} from "@element-plus/icons-vue";
import SimpleLayout from "@/layout/SimpleLayout.vue";

const router = useRouter();
const activeTab = ref("timestamp");
const timestamp = ref("");
const timestampType = ref<"seconds" | "milliseconds">("seconds");
const dateTime = ref("");
const timestampResults = ref<Array<{ label: string; value: string }>>([]);
const datetimeResults = ref<Array<{ label: string; value: string }>>([]);

// 城市时间转换
const sourceCity = ref("Asia/Shanghai");
const targetCity = ref("America/New_York");
const cityDateTime = ref("");
const cityResults = ref<Array<{ label: string; value: string }>>([]);

const cities = [
  { value: "Asia/Shanghai", label: "上海/北京", emoji: "🇨🇳" },
  { value: "Asia/Tokyo", label: "东京", emoji: "🇯🇵" },
  { value: "Asia/Seoul", label: "首尔", emoji: "🇰🇷" },
  { value: "Asia/Singapore", label: "新加坡", emoji: "🇸🇬" },
  { value: "Asia/Dubai", label: "迪拜", emoji: "🇦🇪" },
  { value: "Europe/London", label: "伦敦", emoji: "🇬🇧" },
  { value: "Europe/Paris", label: "巴黎", emoji: "🇫🇷" },
  { value: "Europe/Berlin", label: "柏林", emoji: "🇩🇪" },
  { value: "Europe/Moscow", label: "莫斯科", emoji: "🇷🇺" },
  { value: "America/New_York", label: "纽约", emoji: "🇺🇸" },
  { value: "America/Los_Angeles", label: "洛杉矶", emoji: "🇺🇸" },
  { value: "America/Chicago", label: "芝加哥", emoji: "🇺🇸" },
  { value: "America/Toronto", label: "多伦多", emoji: "🇨🇦" },
  { value: "America/Sao_Paulo", label: "圣保罗", emoji: "🇧🇷" },
  { value: "Australia/Sydney", label: "悉尼", emoji: "🇦🇺" },
  { value: "Pacific/Auckland", label: "奥克兰", emoji: "🇳🇿" },
  { value: "Asia/Kolkata", label: "孟买", emoji: "🇮🇳" },
  { value: "Asia/Bangkok", label: "曼谷", emoji: "🇹🇭" },
  { value: "Asia/Ho_Chi_Minh", label: "胡志明市", emoji: "🇻🇳" },
  { value: "Asia/Jakarta", label: "雅加达", emoji: "🇮🇩" },
];

const getCurrentTimestamp = () => {
  timestamp.value =
    timestampType.value === "seconds"
      ? Math.floor(Date.now() / 1000).toString()
      : Date.now().toString();
  convertTimestamp();
};

const convertTimestamp = () => {
  if (!timestamp.value) return;
  try {
    let ts = parseInt(timestamp.value);
    if (timestampType.value === "seconds") {
      ts = ts * 1000;
    }
    const date = new Date(ts);
    timestampResults.value = [
      { label: "日期时间", value: formatDateTime(date) },
      { label: "毫秒时间戳", value: ts.toString() },
      { label: "秒时间戳", value: Math.floor(ts / 1000).toString() },
      { label: "ISO 格式", value: date.toISOString() },
      { label: "UTC 时间", value: date.toUTCString() },
      { label: "本地时间", value: date.toLocaleString("zh-CN") },
      { label: "星期", value: getWeekDay(date) },
    ];
    ElMessage.success("转换成功");
  } catch (e) {
    ElMessage.error("转换失败，请检查时间戳格式");
  }
};

const convertDateTime = () => {
  if (!dateTime.value) return;
  try {
    const date = new Date(dateTime.value);
    const ts = date.getTime();
    datetimeResults.value = [
      { label: "Unix 时间戳（秒）", value: Math.floor(ts / 1000).toString() },
      { label: "Unix 时间戳（毫秒）", value: ts.toString() },
      { label: "ISO 格式", value: date.toISOString() },
      { label: "UTC 时间", value: date.toUTCString() },
      { label: "本地时间", value: date.toLocaleString("zh-CN") },
      { label: "星期", value: getWeekDay(date) },
    ];
    ElMessage.success("转换成功");
  } catch (e) {
    ElMessage.error("转换失败");
  }
};

const convertCityTime = () => {
  if (!cityDateTime.value) {
    ElMessage.warning("请选择日期时间");
    return;
  }

  try {
    const date = new Date(cityDateTime.value);

    // 获取源城市时间
    const sourceTimeStr = date.toLocaleString("en-US", {
      timeZone: sourceCity.value,
    });
    const sourceDate = new Date(sourceTimeStr);

    // 获取目标城市时间
    const targetTimeStr = sourceDate.toLocaleString("en-US", {
      timeZone: targetCity.value,
    });
    const targetDate = new Date(targetTimeStr);

    // 获取城市名称
    const sourceCityName =
      cities.find((c) => c.value === sourceCity.value)?.label ||
      sourceCity.value;
    const targetCityName =
      cities.find((c) => c.value === targetCity.value)?.label ||
      targetCity.value;

    cityResults.value = [
      { label: `${sourceCityName}时间`, value: formatDateTime(sourceDate) },
      { label: `${targetCityName}时间`, value: formatDateTime(targetDate) },
      { label: "时差", value: getTimeDiff(sourceDate, targetDate) },
      { label: "源城市时区", value: sourceCity.value },
      { label: "目标城市时区", value: targetCity.value },
      { label: "UTC 时间", value: sourceDate.toISOString() },
    ];
    ElMessage.success("转换成功");
  } catch (e) {
    ElMessage.error("转换失败");
  }
};

const getTimeDiff = (date1: Date, date2: Date) => {
  const diff = date2.getTime() - date1.getTime();
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const minutes = Math.abs(Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60)));

  if (hours === 0 && minutes === 0) {
    return "无时差";
  }

  const sign = hours > 0 ? "+" : "-";
  const absHours = Math.abs(hours);
  return `${sign}${absHours}小时${minutes > 0 ? minutes + "分钟" : ""}`;
};

const formatDateTime = (date: Date) => {
  const pad = (n: number) => n.toString().padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(
    date.getDate()
  )} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(
    date.getSeconds()
  )}`;
};

const getWeekDay = (date: Date) => {
  const weekDays = [
    "星期日",
    "星期一",
    "星期二",
    "星期三",
    "星期四",
    "星期五",
    "星期六",
  ];
  return weekDays[date.getDay()];
};

const copyResult = (value: string) => {
  navigator.clipboard.writeText(value);
  ElMessage.success("已复制");
};
</script>

<style scoped>
.timestamp-converter {
  background: var(--theme-background);
  min-height: calc(100vh - 80px);
  padding: 24px;
}

.tool-header {
  max-width: 1000px;
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
  max-width: 1000px;
  margin: 0 auto;
}

.converter-tabs {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  padding: 24px;
  box-shadow: var(--glass-shadow);
}

.converter-card {
  padding: 8px;
}

.section {
  margin-bottom: 32px;
}

.section h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0 0 16px 0;
}

.input-group {
  display: flex;
  gap: 12px;
  align-items: center;
}

.input-group .el-input {
  flex: 1;
}

.button-group {
  display: flex;
  gap: 8px;
}

.timestamp-type {
  margin-top: 12px;
}

.city-input-group {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.city-input label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--theme-text);
  margin-bottom: 8px;
}

.time-input-section {
  margin-top: 16px;
}

.time-input-section label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--theme-text);
  margin-bottom: 8px;
}

.time-input-group {
  display: flex;
  gap: 12px;
  align-items: center;
}

.time-input-group .el-date-picker {
  flex: 1;
}

.results {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--theme-border);
}

.results h4 {
  font-size: 16px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0 0 16px 0;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}

.result-item {
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-md);
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all var(--transition-fast);
}

.result-item:hover {
  border-color: var(--theme-primary);
  transform: translateY(-2px);
}

.result-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--theme-text-secondary);
  min-width: 100px;
}

.result-value {
  flex: 1;
  font-family: "Fira Code", monospace;
  font-size: 14px;
  color: var(--theme-text);
  word-break: break-all;
}

@media (max-width: 768px) {
  .timestamp-converter {
    padding: 12px;
  }

  .tool-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .tool-header h1 {
    font-size: 22px;
  }

  .city-input-group {
    grid-template-columns: 1fr;
  }

  .result-grid {
    grid-template-columns: 1fr;
  }
}
</style>
