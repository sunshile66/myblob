<template>
  <ToolPageShell
    title="时间戳转换工具"
    description="在时间戳、日期时间和常用城市时区之间快速切换。"
    eyebrow="开发调试"
    :meta="meta"
  >
    <div class="shell">
      <el-tabs v-model="tab" class="tabs">
        <el-tab-pane label="时间戳" name="timestamp">
          <div class="panel">
            <div class="row">
              <el-input v-model="timestamp" placeholder="输入 10 位或 13 位时间戳" />
              <el-button type="primary" @click="convertTimestamp">转换</el-button>
              <el-button @click="useNow">当前时间</el-button>
            </div>
            <el-radio-group v-model="mode" class="radio">
              <el-radio label="auto">自动判断</el-radio>
              <el-radio label="seconds">秒</el-radio>
              <el-radio label="milliseconds">毫秒</el-radio>
            </el-radio-group>
            <div v-if="timestampResults.length" class="grid">
              <article v-for="item in timestampResults" :key="item.label" class="result">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
                <el-button text @click="copy(item.value)">复制</el-button>
              </article>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="日期时间" name="datetime">
          <div class="panel">
            <div class="row">
              <el-date-picker
                v-model="dateTime"
                type="datetime"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="选择日期时间"
                style="width: 100%"
              />
              <el-button type="primary" @click="convertDateTime">转换</el-button>
            </div>
            <div v-if="dateTimeResults.length" class="grid">
              <article v-for="item in dateTimeResults" :key="item.label" class="result">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
                <el-button text @click="copy(item.value)">复制</el-button>
              </article>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="时区换算" name="timezone">
          <div class="panel">
            <div class="city-grid">
              <el-select v-model="sourceCity" filterable placeholder="源城市">
                <el-option v-for="city in cities" :key="city.value" :label="city.label" :value="city.value" />
              </el-select>
              <el-select v-model="targetCity" filterable placeholder="目标城市">
                <el-option v-for="city in cities" :key="city.value" :label="city.label" :value="city.value" />
              </el-select>
            </div>
            <div class="row">
              <el-date-picker
                v-model="zonedDateTime"
                type="datetime"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="输入源城市当地时间"
                style="width: 100%"
              />
              <el-button type="primary" @click="convertZone">转换</el-button>
            </div>
            <div v-if="zoneResults.length" class="grid">
              <article v-for="item in zoneResults" :key="item.label" class="result">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
                <el-button text @click="copy(item.value)">复制</el-button>
              </article>
            </div>
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

type ResultItem = { label: string; value: string };
type TimestampMode = "auto" | "seconds" | "milliseconds";

const tab = ref("timestamp");
const timestamp = ref("");
const mode = ref<TimestampMode>("auto");
const dateTime = ref("");
const zonedDateTime = ref("");
const sourceCity = ref("Asia/Shanghai");
const targetCity = ref("America/New_York");
const timestampResults = ref<ResultItem[]>([]);
const dateTimeResults = ref<ResultItem[]>([]);
const zoneResults = ref<ResultItem[]>([]);

const cities = [
  { value: "Asia/Shanghai", label: "上海 / 北京" },
  { value: "Asia/Tokyo", label: "东京" },
  { value: "Asia/Singapore", label: "新加坡" },
  { value: "Europe/London", label: "伦敦" },
  { value: "Europe/Paris", label: "巴黎" },
  { value: "America/New_York", label: "纽约" },
  { value: "America/Los_Angeles", label: "洛杉矶" },
  { value: "Australia/Sydney", label: "悉尼" },
];

const meta = computed(() => [
  { label: "当前标签", value: tab.value },
  { label: "本地时区", value: Intl.DateTimeFormat().resolvedOptions().timeZone },
  { label: "结果条目", value: `${timestampResults.value.length + dateTimeResults.value.length + zoneResults.value.length}` },
]);

const formatDateTime = (date: Date) => {
  const pad = (value: number) => value.toString().padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
};

const formatInZone = (date: Date, timeZone: string) =>
  new Intl.DateTimeFormat("zh-CN", {
    timeZone,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  }).format(date);

const getZoneOffset = (date: Date, timeZone: string) => {
  const parts = new Intl.DateTimeFormat("en-CA", {
    timeZone,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  }).formatToParts(date);
  const map = Object.fromEntries(parts.filter((part) => part.type !== "literal").map((part) => [part.type, part.value]));
  const utc = Date.UTC(Number(map.year), Number(map.month) - 1, Number(map.day), Number(map.hour), Number(map.minute), Number(map.second));
  return (utc - date.getTime()) / 60000;
};

const zonedToUtc = (value: string, timeZone: string) => {
  const [datePart, timePart = "00:00:00"] = value.split(" ");
  const [year, month, day] = datePart.split("-").map(Number);
  const [hour, minute, second] = timePart.split(":").map(Number);
  const guess = new Date(Date.UTC(year, month - 1, day, hour, minute, second));
  const offset = getZoneOffset(guess, timeZone);
  return new Date(guess.getTime() - offset * 60000);
};

const zoneLabel = (date: Date, timeZone: string) => {
  const offset = getZoneOffset(date, timeZone);
  const sign = offset >= 0 ? "+" : "-";
  const abs = Math.abs(offset);
  const hours = String(Math.floor(abs / 60)).padStart(2, "0");
  const minutes = String(Math.floor(abs % 60)).padStart(2, "0");
  return `UTC${sign}${hours}:${minutes}`;
};

const ensure = (value: string, label: string) => {
  if (!value) {
    ElMessage.warning(`请输入${label}`);
    return false;
  }
  return true;
};

const useNow = () => {
  timestamp.value = mode.value === "seconds" ? `${Math.floor(Date.now() / 1000)}` : `${Date.now()}`;
  convertTimestamp();
};

const convertTimestamp = () => {
  if (!ensure(timestamp.value, "时间戳")) return;
  if (!/^-?\d+$/.test(timestamp.value.trim())) {
    ElMessage.error("时间戳只能包含数字");
    return;
  }
  const detected = mode.value === "auto" ? (timestamp.value.trim().length >= 13 ? "milliseconds" : "seconds") : mode.value;
  const raw = Number(timestamp.value);
  const milliseconds = detected === "seconds" ? raw * 1000 : raw;
  const date = new Date(milliseconds);
  timestampResults.value = [
    { label: "日期时间", value: formatDateTime(date) },
    { label: "秒时间戳", value: `${Math.floor(milliseconds / 1000)}` },
    { label: "毫秒时间戳", value: `${milliseconds}` },
    { label: "ISO", value: date.toISOString() },
    { label: "UTC", value: date.toUTCString() },
  ];
};

const convertDateTime = () => {
  if (!ensure(dateTime.value, "日期时间")) return;
  const date = new Date(dateTime.value);
  const milliseconds = date.getTime();
  dateTimeResults.value = [
    { label: "秒时间戳", value: `${Math.floor(milliseconds / 1000)}` },
    { label: "毫秒时间戳", value: `${milliseconds}` },
    { label: "ISO", value: date.toISOString() },
    { label: "本地时间", value: date.toLocaleString("zh-CN") },
  ];
};

const convertZone = () => {
  if (!ensure(zonedDateTime.value, "日期时间")) return;
  const utcDate = zonedToUtc(zonedDateTime.value, sourceCity.value);
  const sourceName = cities.find((city) => city.value === sourceCity.value)?.label || sourceCity.value;
  const targetName = cities.find((city) => city.value === targetCity.value)?.label || targetCity.value;
  zoneResults.value = [
    { label: `${sourceName}时间`, value: formatInZone(utcDate, sourceCity.value) },
    { label: `${targetName}时间`, value: formatInZone(utcDate, targetCity.value) },
    { label: "源时区", value: zoneLabel(utcDate, sourceCity.value) },
    { label: "目标时区", value: zoneLabel(utcDate, targetCity.value) },
    { label: "UTC", value: utcDate.toISOString() },
  ];
};

const copy = async (value: string) => {
  await navigator.clipboard.writeText(value);
  ElMessage.success("已复制");
};
</script>

<style scoped>
.tabs {
  padding: 20px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(148, 163, 184, 0.12);
  box-shadow: 0 22px 38px rgba(15, 23, 42, 0.06);
}

.panel {
  padding: 18px;
  border-radius: 20px;
  background: rgba(15, 23, 42, 0.03);
}

.row,
.city-grid {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 12px;
}

.city-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-bottom: 12px;
}

.radio {
  margin-top: 12px;
}

.grid {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}

.result {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.result span {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  color: #64748b;
}

.result strong {
  display: block;
  font-size: 15px;
  line-height: 1.7;
  color: #0f172a;
  word-break: break-word;
}

@media (max-width: 960px) {
  .row,
  .city-grid {
    grid-template-columns: 1fr;
  }

  .tabs {
    padding: 16px;
  }
}
</style>
