<template>
  <div class="detail-page">
    <div v-if="loading" class="center">加载中...</div>
    <div v-else-if="!item" class="center">新闻不存在</div>
    <div v-else class="detail">
      <div class="meta">
        <span class="src">{{ item.sourceName || item.sourcePlatform }}</span>
        <span class="cat">{{ item.category }}</span>
        <span class="lang-badge" :class="item.language==='CN'?'cn':'en'">{{ item.language==='CN'?'中':'EN' }}</span>
        <span class="time">{{ fmtTime(item.publishedAt) }}</span>
        <span class="score" :class="item.qualityScore>=60?'hi':item.qualityScore>=35?'mid':'lo'">{{ item.qualityScore }}分</span>
      </div>
      <h1>{{ item.title }}</h1>
      <h2 v-if="item.language==='EN' && item.translatedTitle" class="title-zh">{{ item.translatedTitle }}</h2>
      <div v-if="item.summary" class="summary">{{ item.summary }}</div>
      <div v-if="item.language==='EN' && item.translatedSummary" class="summary-zh">{{ item.translatedSummary }}</div>
      <div v-if="item.content" class="content" v-html="item.content"></div>
      <div class="actions">
        <a :href="item.sourceUrl" target="_blank" class="btn">查看原文</a>
        <button @click="$router.back()" class="btn-back">返回</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getNewsDetail } from '@/api/news'
import type { NewsItem } from '@/types'

const route = useRoute()
const item = ref<NewsItem|null>(null)
const loading = ref(true)

function fmtTime(t?:string){if(!t)return'';const d=new Date(t),n=new Date();const m=Math.floor((n.getTime()-d.getTime())/60000);if(m<1)return'刚刚';if(m<60)return m+'分钟前';const h=Math.floor(m/60);if(h<24)return h+'小时前';return d.toLocaleDateString()}

onMounted(async () => {
  try {
    const id = Number(route.params.id)
    item.value = await getNewsDetail(id) as any
  } catch(e) { console.error(e) }
  finally { loading.value = false }
})
</script>

<style scoped>
.detail-page{max-width:800px;margin:0 auto;padding:24px 16px}
.center{text-align:center;padding:60px 0;color:#999}
.meta{display:flex;gap:10px;align-items:center;margin-bottom:16px;flex-wrap:wrap}
.src{padding:2px 10px;background:#409eff;color:#fff;border-radius:10px;font-size:12px}
.cat{padding:2px 10px;background:#f0f0f0;border-radius:10px;font-size:12px}
.time{font-size:13px;color:#999}
.score{font-size:12px;padding:2px 8px;border-radius:10px;font-weight:700}.hi{color:#67c23a;background:#f0f9eb}.mid{color:#e6a23c;background:#fdf6ec}.lo{color:#f56c6c;background:#fef0f0}
.lang-badge{padding:1px 6px;border-radius:8px;font-size:11px}.lang-badge.cn{background:#fff0f0;color:#f56c6c}.lang-badge.en{background:#f0f0ff;color:#409eff}
h1{font-size:1.6rem;line-height:1.5;margin:0 0 8px}
.title-zh{font-size:1.1rem;color:#888;font-weight:400;margin:0 0 12px;line-height:1.5}
.summary{font-size:15px;color:#555;line-height:1.8;background:#f9f9f9;padding:16px;border-radius:8px;margin-bottom:8px}
.summary-zh{font-size:14px;color:#888;line-height:1.8;background:#f5f5ff;padding:12px 16px;border-radius:8px;margin-bottom:20px;border-left:3px solid #409eff}
.content{font-size:15px;line-height:1.8;color:#333}
.actions{display:flex;gap:12px;margin-top:32px;padding-top:20px;border-top:1px solid #eee}
.btn{padding:8px 20px;background:#409eff;color:#fff;border:none;border-radius:6px;text-decoration:none;font-size:14px;cursor:pointer;display:inline-block}
.btn-back{padding:8px 20px;background:#f0f0f0;border:none;border-radius:6px;cursor:pointer;font-size:14px}
</style>
