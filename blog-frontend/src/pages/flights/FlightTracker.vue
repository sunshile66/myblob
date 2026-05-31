<template>
  <div class="flight-page">
    <!-- Top bar -->
    <div class="top-bar">
      <router-link to="/" class="home-btn">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>
        </svg>
        首页
      </router-link>
      <router-link to="/news" class="news-btn">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M4 22h16a2 2 0 002-2V4a2 2 0 00-2-2H8a2 2 0 00-2 2v16a2 2 0 01-2 2zm0 0a2 2 0 01-2-2v-9c0-1.1.9-2 2-2h2"/>
        </svg>
        新闻聚合
      </router-link>
    </div>

    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <h1>航班追踪</h1>
        <span class="subtitle">国际航司实时航班动态 · 航线变更高亮</span>
      </div>
      <div class="header-right">
        <select v-model="airlineFilter" class="filter-select" @change="loadFlights(true)">
          <option value="">全部航司</option>
          <option v-for="a in airlines" :key="a" :value="a">{{ a }}</option>
        </select>
        <button class="filter-btn" :class="{ active: showChangesOnly }" @click="toggleChanges">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
            <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
          仅显示变更
        </button>
        <button class="refresh-btn" @click="loadFlights(true)" :disabled="loading">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
            :class="{ spinning: loading }">
            <polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/>
            <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
          </svg>
          刷新
        </button>
      </div>
    </div>

    <!-- Stats -->
    <div class="stats-bar">
      <div class="stat-chip">
        <span class="stat-dot" style="background: #4F46E5"></span>
        总航班 <strong>{{ totalFlights }}</strong>
      </div>
      <div class="stat-chip new">
        <span class="stat-dot" style="background: #10B981"></span>
        新增 <strong>{{ newCount }}</strong>
      </div>
      <div class="stat-chip cancelled">
        <span class="stat-dot" style="background: #EF4444"></span>
        取消 <strong>{{ cancelledCount }}</strong>
      </div>
      <div class="stat-chip delayed">
        <span class="stat-dot" style="background: #F59E0B"></span>
        延误 <strong>{{ delayedCount }}</strong>
      </div>
    </div>

    <!-- Flight table -->
    <div class="table-wrap">
      <div v-if="loading && !flights.length" class="loading-state">
        <div class="loading-spinner"></div>
        <p>正在获取航班数据...</p>
      </div>
      <div v-else-if="!flights.length" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#CBD5E1" stroke-width="1">
          <path d="M22 2L11 13"/><path d="M22 2l-7 20-4-9-9-4 20-7z"/>
        </svg>
        <p>暂无航班数据</p>
        <p class="empty-sub">数据每小时自动更新，或点击刷新手动获取</p>
      </div>
      <table v-else class="flight-table">
        <thead>
          <tr>
            <th>状态</th>
            <th>航班号</th>
            <th>航司</th>
            <th>出发</th>
            <th>到达</th>
            <th>高度(m)</th>
            <th>速度(m/s)</th>
            <th>国家</th>
            <th>最后更新</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="f in flights" :key="f.id"
            :class="{ 'row-new': f.changeType === 'NEW', 'row-cancelled': f.changeType === 'CANCELLED', 'row-delayed': f.changeType === 'DELAYED' }">
            <td>
              <span :class="['change-badge', f.changeType?.toLowerCase()]">
                {{ changeLabel(f.changeType) }}
              </span>
            </td>
            <td class="callsign">{{ f.callsign || f.flightNumber }}</td>
            <td>{{ f.airline }}</td>
            <td>{{ f.originAirport || '-' }}</td>
            <td>{{ f.destinationAirport || '-' }}</td>
            <td>{{ f.altitude ? Math.round(f.altitude) : '-' }}</td>
            <td>{{ f.velocity ? Math.round(f.velocity) : '-' }}</td>
            <td>{{ f.country || '-' }}</td>
            <td class="time-cell">{{ fmtTime(f.lastSeen) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="hasMore" class="pagination">
      <button class="page-btn" @click="loadFlights(false)" :disabled="loading">加载更多</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getFlights, getFlightAirlines, getFlightChanges, type FlightRoute } from '@/api/news'

const flights = ref<FlightRoute[]>([])
const airlines = ref<string[]>([])
const airlineFilter = ref('')
const showChangesOnly = ref(false)
const loading = ref(false)
const page = ref(0)
const totalFlights = ref(0)
const newCount = ref(0)
const cancelledCount = ref(0)
const delayedCount = ref(0)
const hasMore = ref(true)

function changeLabel(ct?: string): string {
  switch (ct) {
    case 'NEW': return '新增'
    case 'CANCELLED': return '取消'
    case 'DELAYED': return '延误'
    default: return '正常'
  }
}

function fmtTime(t?: string) {
  if (!t) return '-'
  const d = new Date(t), n = new Date()
  const m = Math.floor((n.getTime() - d.getTime()) / 60000)
  if (m < 1) return '刚刚'
  if (m < 60) return m + '分钟前'
  const h = Math.floor(m / 60)
  if (h < 24) return h + '小时前'
  return d.toLocaleDateString()
}

function toggleChanges() {
  showChangesOnly.value = !showChangesOnly.value
  loadFlights(true)
}

async function loadFlights(reset: boolean) {
  if (loading.value) return
  loading.value = true
  if (reset) { page.value = 0; flights.value = [] }

  try {
    const params: any = { page: page.value, size: 50 }
    if (airlineFilter.value) params.airline = airlineFilter.value
    if (showChangesOnly.value) params.changeType = 'changes'

    const res: any = await getFlights(params)
    const items = res?.results || []
    flights.value = reset ? items : [...flights.value, ...items]
    totalFlights.value = res?.count || flights.value.length
    hasMore.value = items.length >= 50
    page.value++

    // Compute stats
    newCount.value = flights.value.filter(f => f.changeType === 'NEW').length
    cancelledCount.value = flights.value.filter(f => f.changeType === 'CANCELLED').length
    delayedCount.value = flights.value.filter(f => f.changeType === 'DELAYED').length
  } catch (e) {
    console.error('Flight load error:', e)
  } finally {
    loading.value = false
  }
}

async function loadAirlines() {
  try {
    airlines.value = (await getFlightAirlines() as any) || []
  } catch (e) { /* ignore */ }
}

// Auto refresh every 5 minutes
let timer: ReturnType<typeof setInterval>
onMounted(() => {
  loadFlights(true)
  loadAirlines()
  timer = setInterval(() => loadFlights(true), 300000)
})
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.flight-page { max-width: 1200px; margin: 0 auto; padding: 16px 20px; font-family: 'Inter', 'HarmonyOS Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif; }

/* Top bar */
.top-bar { display: flex; gap: 8px; margin-bottom: 16px; }
.home-btn, .news-btn { display: inline-flex; align-items: center; gap: 6px; padding: 6px 14px; border-radius: 8px; text-decoration: none; font-size: 13px; font-weight: 500; color: var(--theme-text-secondary, #64748B); background: var(--theme-card, #fff); border: 1px solid var(--theme-border, #E5E7EB); transition: all .2s; }
.home-btn:hover, .news-btn:hover { color: var(--theme-primary, #4F46E5); border-color: var(--theme-primary, #4F46E5); }

/* Header */
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 12px; }
.header-left h1 { font-size: 1.5rem; margin: 0; font-weight: 700; color: var(--theme-text, #0F172A); }
.subtitle { font-size: 13px; color: var(--theme-text-secondary, #64748B); display: block; margin-top: 4px; }
.header-right { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.filter-select { padding: 7px 12px; border: 1px solid var(--theme-border, #E5E7EB); border-radius: 8px; font-size: 13px; background: var(--theme-card, #fff); color: var(--theme-text, #0F172A); outline: none; }
.filter-select:focus { border-color: var(--theme-primary, #4F46E5); }
.filter-btn, .refresh-btn { display: inline-flex; align-items: center; gap: 5px; padding: 7px 14px; border: 1px solid var(--theme-border, #E5E7EB); border-radius: 8px; background: var(--theme-card, #fff); cursor: pointer; font-size: 13px; color: var(--theme-text-secondary, #64748B); transition: all .2s; font-weight: 500; }
.filter-btn:hover, .refresh-btn:hover { border-color: var(--theme-primary, #4F46E5); color: var(--theme-primary, #4F46E5); }
.filter-btn.active { background: var(--theme-primary, #4F46E5); color: #fff; border-color: var(--theme-primary, #4F46E5); }
.refresh-btn:disabled { opacity: .5; cursor: not-allowed; }
.spinning { animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* Stats */
.stats-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.stat-chip { display: inline-flex; align-items: center; gap: 6px; padding: 6px 14px; background: var(--theme-card, #fff); border: 1px solid var(--theme-border, #E5E7EB); border-radius: 8px; font-size: 13px; color: var(--theme-text-secondary, #64748B); }
.stat-chip strong { color: var(--theme-text, #0F172A); }
.stat-dot { width: 8px; height: 8px; border-radius: 50%; }

/* Table */
.table-wrap { background: var(--theme-card, #fff); border: 1px solid var(--theme-border, #E5E7EB); border-radius: 12px; overflow: hidden; }
.flight-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.flight-table th { padding: 12px 14px; text-align: left; font-weight: 600; font-size: 12px; color: var(--theme-text-secondary, #64748B); background: var(--theme-hover, #F1F5F9); border-bottom: 1px solid var(--theme-border, #E5E7EB); text-transform: uppercase; letter-spacing: .5px; }
.flight-table td { padding: 10px 14px; border-bottom: 1px solid var(--theme-border, #E5E7EB); color: var(--theme-text, #0F172A); }
.flight-table tr:last-child td { border-bottom: none; }
.flight-table tr:hover { background: var(--theme-hover, #F1F5F9); }

/* Row highlighting */
.row-new { background: rgba(16, 185, 129, .06) !important; }
.row-cancelled { background: rgba(239, 68, 68, .06) !important; opacity: .7; }
.row-delayed { background: rgba(245, 158, 11, .06) !important; }

/* Change badges */
.change-badge { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 600; }
.change-badge.new { background: rgba(16, 185, 129, .15); color: #059669; }
.change-badge.cancelled { background: rgba(239, 68, 68, .15); color: #DC2626; }
.change-badge.delayed { background: rgba(245, 158, 11, .15); color: #D97706; }
.change-badge.normal { background: var(--theme-hover, #F1F5F9); color: var(--theme-text-secondary, #64748B); }

.callsign { font-weight: 600; font-family: 'JetBrains Mono', monospace; }
.time-cell { color: var(--theme-text-secondary, #64748B); font-size: 12px; }

/* Loading / Empty */
.loading-state, .empty-state { text-align: center; padding: 60px 0; color: #94A3B8; }
.loading-spinner { width: 24px; height: 24px; border: 3px solid #E5E7EB; border-top-color: var(--theme-primary, #4F46E5); border-radius: 50%; animation: spin .6s linear infinite; margin: 0 auto 12px; }
.empty-state svg { margin-bottom: 12px; }
.empty-state p { margin: 0; font-size: 15px; }
.empty-sub { font-size: 13px !important; color: #CBD5E1; margin-top: 6px !important; }

/* Pagination */
.pagination { text-align: center; padding: 20px 0; }
.page-btn { padding: 8px 24px; border: 1px solid var(--theme-border, #E5E7EB); border-radius: 8px; background: var(--theme-card, #fff); cursor: pointer; font-size: 13px; color: var(--theme-text-secondary, #64748B); transition: all .2s; font-weight: 500; }
.page-btn:hover { border-color: var(--theme-primary, #4F46E5); color: var(--theme-primary, #4F46E5); }
.page-btn:disabled { opacity: .5; cursor: not-allowed; }

@media (max-width: 768px) {
  .flight-table { font-size: 12px; }
  .flight-table th, .flight-table td { padding: 8px 10px; }
  .header-right { width: 100%; }
}
</style>
