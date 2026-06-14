<template>
  <div class="dashboard">
    <div class="dash-header">
      <h2>新闻聚合管理</h2>
      <div class="global-bar">
        <span class="status-dot" :class="{ on: globalEnabled }"></span>
        <span class="status-text">{{ globalEnabled ? '运行中' : '已暂停' }}</span>
        <el-switch v-model="globalEnabled" @change="toggleGlobalSwitch" />
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card blue">
        <div class="stat-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4 22h16a2 2 0 002-2V4a2 2 0 00-2-2H8a2 2 0 00-2 2v16a2 2 0 01-2 2z"/>
          </svg>
        </div>
        <div class="stat-body">
          <h3>{{ stats.totalItems || 0 }}</h3>
          <p>总新闻数</p>
        </div>
      </div>
      <div class="stat-card green">
        <div class="stat-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
          </svg>
        </div>
        <div class="stat-body">
          <h3>{{ stats.activeItems || 0 }}</h3>
          <p>活跃新闻</p>
        </div>
      </div>
      <div class="stat-card orange">
        <div class="stat-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"/>
          </svg>
        </div>
        <div class="stat-body">
          <h3>{{ stats.enabledSources || 0 }}<span class="stat-total">/{{ stats.totalSources || 0 }}</span></h3>
          <p>活跃源数</p>
        </div>
      </div>
      <div class="stat-card purple">
        <div class="stat-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
          </svg>
        </div>
        <div class="stat-body">
          <h3>{{ stats.lastFetchDurationMs || 0 }}<span class="stat-unit">ms</span></h3>
          <p>上次耗时</p>
        </div>
      </div>
    </div>

    <!-- 抓取控制 -->
    <div class="control-section">
      <div class="control-info">
        <div class="info-row">
          <span class="info-label">上次抓取</span>
          <span class="info-value">{{ stats.lastFetchTime ? new Date(stats.lastFetchTime).toLocaleString() : '未执行' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">抓取数量</span>
          <span class="info-value">{{ stats.lastFetchCount || 0 }} 条</span>
        </div>
        <div class="info-row">
          <span class="info-label">抓取状态</span>
          <span class="info-value" :class="stats.fetchRunning ? 'running' : 'idle'">
            {{ stats.fetchRunning ? '运行中...' : '空闲' }}
          </span>
        </div>
      </div>
      <div class="control-actions">
        <el-button type="primary" @click="doFetchNow" :loading="fetching" :disabled="!globalEnabled">
          立即全量抓取
        </el-button>
        <el-button @click="loadStats">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:4px">
            <polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 11-2.12-9.36L23 10"/>
          </svg>
          刷新
        </el-button>
      </div>
      <div v-if="fetchMsg" class="fetch-result">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#67c23a" stroke-width="2">
          <path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>
        </svg>
        {{ fetchMsg }}
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-links">
      <router-link to="/admin/news/sources" class="link-card">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/>
          <path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/>
        </svg>
        <span>新闻源管理</span>
        <span class="link-desc">管理 {{ stats.totalSources || 0 }} 个数据源</span>
      </router-link>
      <router-link to="/admin/news/items" class="link-card">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
          <polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/>
          <line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/>
        </svg>
        <span>内容管理</span>
        <span class="link-desc">管理 {{ stats.totalItems || 0 }} 条新闻</span>
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getNewsStats, getGlobalStatus, toggleGlobal, fetchNow } from '@/api/newsAdmin'
import { ElMessage } from 'element-plus'

const stats = ref<any>({})
const globalEnabled = ref(true)
const fetching = ref(false)
const fetchMsg = ref('')

async function loadStats() {
  try { stats.value = await getNewsStats() as any } catch(e){console.error(e)}
}
async function loadGlobal() {
  try { const r = await getGlobalStatus() as any; globalEnabled.value = r?.enabled ?? true } catch(e){console.error(e)}
}
async function toggleGlobalSwitch() {
  try {
    const r = await toggleGlobal() as any
    globalEnabled.value = r?.enabled ?? globalEnabled.value
    ElMessage.success(r?.enabled ? '已开启新闻聚合' : '已暂停新闻聚合')
  } catch(e){console.error(e)}
}
async function doFetchNow() {
  fetching.value = true; fetchMsg.value = ''
  try {
    fetchMsg.value = await fetchNow() as any
    ElMessage.success('抓取完成')
  } catch(e:any) {
    fetchMsg.value = e?.message || '抓取失败'
    ElMessage.error('抓取失败')
  } finally { fetching.value = false; loadStats() }
}

onMounted(() => { loadStats(); loadGlobal() })
</script>

<style scoped>
.dashboard { max-width: 1000px; margin: 0 auto; padding: 24px; }
.dash-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.dash-header h2 { margin: 0; font-size: 1.4rem; font-weight: 700; }
.global-bar { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.status-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--theme-text-tertiary, #94A3B8); }
.status-dot.on { background: #67c23a; box-shadow: 0 0 6px rgba(103,194,58,.4); }
.status-text { color: var(--theme-text-secondary, #64748B); }

/* Stats */
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
@media (max-width: 768px) { .stats-row { grid-template-columns: repeat(2, 1fr); } }
.stat-card { display: flex; align-items: center; gap: 14px; background: var(--theme-card, #fff); padding: 20px; border-radius: 12px;
  box-shadow: var(--shadow-xs, 0 1px 2px rgba(15,23,42,.04)); border: 1px solid var(--theme-border, #E5E7EB); transition: box-shadow .2s; }
.stat-card:hover { box-shadow: var(--shadow-md, 0 4px 12px rgba(15,23,42,.06)); }
.stat-icon { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-card.blue .stat-icon { background: rgba(64,158,255,.1); color: #409eff; }
.stat-card.green .stat-icon { background: rgba(103,194,58,.1); color: #67c23a; }
.stat-card.orange .stat-icon { background: rgba(230,162,60,.1); color: #e6a23c; }
.stat-card.purple .stat-icon { background: rgba(155,89,182,.1); color: #9b59b6; }
.stat-body h3 { font-size: 1.6rem; margin: 0; font-weight: 700; color: var(--theme-text, #0F172A); }
.stat-total { font-size: .9rem; color: var(--theme-text-secondary, #64748B); font-weight: 400; }
.stat-unit { font-size: .8rem; color: var(--theme-text-secondary, #64748B); font-weight: 400; margin-left: 2px; }
.stat-body p { margin: 2px 0 0; color: var(--theme-text-secondary, #64748B); font-size: 12px; }

/* Control */
.control-section { background: var(--theme-card, #fff); padding: 20px; border-radius: 12px; margin-bottom: 24px;
  box-shadow: var(--shadow-xs, 0 1px 2px rgba(15,23,42,.04)); border: 1px solid var(--theme-border, #E5E7EB); }
.control-info { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 16px; }
@media (max-width: 600px) { .control-info { grid-template-columns: 1fr; } }
.info-row { display: flex; flex-direction: column; gap: 4px; }
.info-label { font-size: 11px; color: var(--theme-text-secondary, #64748B); text-transform: uppercase; letter-spacing: .5px; font-weight: 600; }
.info-value { font-size: 14px; color: var(--theme-text, #0F172A); font-weight: 500; }
.info-value.running { color: #e6a23c; }
.info-value.idle { color: #67c23a; }
.control-actions { display: flex; gap: 10px; }
.fetch-result { margin-top: 12px; display: flex; align-items: center; gap: 6px; padding: 10px 14px;
  background: rgba(103,194,58,.06); border-radius: 8px; color: #67c23a; font-size: 13px; }

/* Quick links */
.quick-links { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
@media (max-width: 600px) { .quick-links { grid-template-columns: 1fr; } }
.link-card { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 28px 20px;
  background: var(--theme-card, #fff); border-radius: 12px; text-decoration: none; color: var(--theme-text, #0F172A);
  box-shadow: var(--shadow-xs, 0 1px 2px rgba(15,23,42,.04)); border: 1px solid var(--theme-border, #E5E7EB); transition: all .2s; }
.link-card:hover { box-shadow: var(--shadow-lg, 0 10px 24px rgba(15,23,42,.08)); border-color: #409eff; color: #409eff; transform: translateY(-2px); }
.link-card span:first-of-type { font-size: 15px; font-weight: 600; }
.link-desc { font-size: 12px; color: var(--theme-text-secondary, #64748B); font-weight: 400; }
.link-card:hover .link-desc { color: #66b1ff; }
</style>
