<template>
  <div class="dashboard">
    <h2>新闻管理仪表盘</h2>
    <div class="global-bar">
      <span>全局状态：</span>
      <el-switch v-model="globalEnabled" @change="toggleGlobalSwitch" active-text="开启" inactive-text="关闭" />
      <span v-if="!globalEnabled" class="paused-banner">新闻自动更新已暂停</span>
    </div>
    <div class="stats-row">
      <div class="stat-card"><h3>{{ stats.totalItems || 0 }}</h3><p>总新闻数</p></div>
      <div class="stat-card"><h3>{{ stats.activeItems || 0 }}</h3><p>活跃新闻</p></div>
      <div class="stat-card"><h3>{{ stats.enabledSources || 0 }}/{{ stats.totalSources || 0 }}</h3><p>活跃源数</p></div>
      <div class="stat-card"><h3>{{ stats.fetchRunning ? '运行中' : '空闲' }}</h3><p>抓取状态</p></div>
    </div>
    <div class="fetch-info">
      <p>上次抓取：{{ stats.lastFetchTime ? new Date(stats.lastFetchTime).toLocaleString() : '未执行' }}</p>
      <p>抓取数量：{{ stats.lastFetchCount || 0 }} 条 | 耗时：{{ stats.lastFetchDurationMs || 0 }}ms</p>
      <button @click="doFetchNow" :disabled="!globalEnabled||fetching" class="btn-primary">
        {{ fetching ? '抓取中...' : '立即抓取' }}
      </button>
      <span v-if="fetchMsg" class="fetch-msg">{{ fetchMsg }}</span>
    </div>
    <div class="links">
      <router-link to="/admin/news/sources" class="link-card">新闻源管理</router-link>
      <router-link to="/admin/news/items" class="link-card">内容管理</router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getNewsStats, getGlobalStatus, toggleGlobal, fetchNow } from '@/api/newsAdmin'

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
  try { const r = await toggleGlobal() as any; globalEnabled.value = r?.enabled ?? globalEnabled.value } catch(e){console.error(e)}
}
async function doFetchNow() {
  fetching.value = true; fetchMsg.value = ''
  try { fetchMsg.value = await fetchNow() as any } catch(e:any) { fetchMsg.value = e?.message || '抓取失败' }
  finally { fetching.value = false; loadStats() }
}

onMounted(() => { loadStats(); loadGlobal() })
</script>

<style scoped>
.dashboard{max-width:1000px;margin:0 auto;padding:24px}
h2{margin:0 0 20px}
.global-bar{display:flex;align-items:center;gap:12px;padding:12px 16px;background:#f5f5f5;border-radius:8px;margin-bottom:20px}
.paused-banner{color:#f56c6c;font-weight:700}
.stats-row{display:grid;grid-template-columns:repeat(4,1fr);gap:16px;margin-bottom:20px}
.stat-card{background:#fff;padding:20px;border-radius:8px;text-align:center;box-shadow:0 1px 4px rgba(0,0,0,.06)}.stat-card h3{font-size:1.8rem;margin:0 0 4px;color:#409eff}.stat-card p{margin:0;color:#666;font-size:13px}
.fetch-info{background:#fff;padding:16px;border-radius:8px;margin-bottom:20px;box-shadow:0 1px 4px rgba(0,0,0,.06)}.fetch-info p{margin:0 0 8px;color:#666;font-size:13px}
.fetch-msg{margin-left:12px;color:#67c23a;font-size:13px}
.btn-primary{padding:8px 20px;background:#409eff;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:14px;margin-top:8px}.btn-primary:disabled{opacity:.5;cursor:not-allowed}
.links{display:grid;grid-template-columns:1fr 1fr;gap:16px}
.link-card{display:block;padding:24px;background:#fff;border-radius:8px;text-align:center;text-decoration:none;color:#333;font-size:16px;box-shadow:0 1px 4px rgba(0,0,0,.06);transition:box-shadow .2s}.link-card:hover{box-shadow:0 3px 12px rgba(0,0,0,.12);color:#409eff}
</style>
