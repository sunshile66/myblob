<template>
  <div class="news-page">
    <div class="news-header">
      <h1>新闻聚合</h1>
      <p>多平台实时新闻，智能过滤低质内容</p>
    </div>
    <div class="filter-bar">
      <div class="category-tabs">
        <button v-for="c in cats" :key="c.v" :class="['tab',{active:curCat===c.v}]" @click="curCat=c.v">{{c.l}}</button>
      </div>
      <div class="filter-right">
        <div class="lang-tabs">
          <button v-for="l in langs" :key="l.v" :class="['langtab',{active:curLang===l.v}]" @click="curLang=l.v">{{l.l}}</button>
        </div>
        <input v-model="search" placeholder="搜索新闻..." @keyup.enter="reload" class="search-inp" />
      </div>
    </div>
    <div v-if="loading" class="center">加载中...</div>
    <div v-else-if="!list.length" class="center"><p>暂无新闻</p><p class="sub">系统正在抓取新闻，请稍后再来</p></div>
    <div v-else class="grid">
      <div v-for="it in list" :key="it.id" class="card" @click="$router.push('/news/'+it.id)">
        <div class="card-head">
          <span class="src-tag">{{ it.sourceName||it.sourcePlatform }}</span>
          <span class="lang-badge" :class="it.language==='CN'?'cn':'en'">{{ it.language==='CN'?'中':'EN' }}</span>
          <span class="cat-badge">{{ it.category }}</span>
        </div>
        <h3>{{ it.title }}</h3>
        <h3 v-if="it.language==='EN' && it.translatedTitle" class="title-zh">{{ it.translatedTitle }}</h3>
        <p class="sum">{{ it.summary||'暂无摘要' }}</p>
        <p v-if="it.language==='EN' && it.translatedSummary" class="sum-zh">{{ it.translatedSummary }}</p>
        <div class="card-foot">
          <span>{{ fmtTime(it.publishedAt) }}</span>
          <span class="score" :class="it.qualityScore>=60?'hi':it.qualityScore>=35?'mid':'lo'">{{it.qualityScore}}</span>
        </div>
      </div>
    </div>
    <div v-if="hasMore&&!loading" class="more"><button @click="more" :disabled="loadingMore">{{loadingMore?'加载中':'加载更多'}}</button></div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { getNewsList } from '@/api/news'
import type { NewsItem } from '@/types'

const list=ref<NewsItem[]>([]),page=ref(0),loading=ref(false),loadingMore=ref(false),hasMore=ref(true)
const curCat=ref(''),curLang=ref(''),search=ref('')

const cats=[{l:'全部分类',v:''},{l:'官方媒体',v:'官方媒体'},{l:'社交媒体',v:'社交媒体'},{l:'科技财经',v:'科技财经'},{l:'科技媒体',v:'科技媒体'},{l:'开源开发者',v:'开源开发者'}]
const langs=[{l:'🌐 全部语言',v:''},{l:'🇨🇳 中文',v:'CN'},{l:'🌍 English',v:'EN'}]

function fmtTime(t?:string){if(!t)return'';const d=new Date(t),n=new Date();const m=Math.floor((n.getTime()-d.getTime())/60000);if(m<1)return'刚刚';if(m<60)return m+'分钟前';const h=Math.floor(m/60);if(h<24)return h+'小时前';const dd=Math.floor(h/24);if(dd<7)return dd+'天前';return d.toLocaleDateString()}

async function fetch(reset=false){
  if(loading.value)return;loading.value=true
  if(reset){page.value=0;list.value=[]}
  try{
    const res:any=await getNewsList({page:page.value,size:20,category:curCat.value||undefined,language:curLang.value||undefined,search:search.value||undefined})
    const items=res?.results||[]
    list.value=reset?items:[...list.value,...items];hasMore.value=items.length>=20;page.value++
  }catch(e){console.error(e)}finally{loading.value=false}
}
function reload(){fetch(true)}
function more(){if(loadingMore.value)return;loadingMore.value=true;fetch().finally(()=>loadingMore.value=false)}

watch([curCat,curLang],()=>reload())
onMounted(()=>reload())
</script>

<style scoped>
.news-page{max-width:1200px;margin:0 auto;padding:24px 16px}
.news-header{text-align:center;margin-bottom:24px}.news-header h1{font-size:2rem;margin:0 0 8px}.news-header p{color:#666;margin:0}
.filter-bar{display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:12px;margin-bottom:20px;padding:12px 16px;background:#f5f5f5;border-radius:8px}
.category-tabs{display:flex;gap:4px;flex-wrap:wrap}
.tab{padding:6px 14px;border:1px solid #ddd;border-radius:16px;background:#fff;cursor:pointer;font-size:13px}.tab:hover{border-color:#409eff;color:#409eff}.tab.active{background:#409eff;color:#fff;border-color:#409eff}
.filter-right{display:flex;gap:12px;align-items:center;flex-wrap:wrap}
.filter-right .lang-tabs{display:flex;gap:4px}
.langtab{padding:6px 12px;border:1px solid #ddd;border-radius:16px;background:#fff;cursor:pointer;font-size:12px;white-space:nowrap}.langtab:hover{border-color:#409eff;color:#409eff}.langtab.active{background:#409eff;color:#fff;border-color:#409eff}
.search-inp{padding:6px 10px;border:1px solid #ddd;border-radius:6px;width:180px;font-size:13px}
.center{text-align:center;padding:60px 0;color:#999}.sub{font-size:13px;color:#bbb}
.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:16px}
.card{background:#fff;border-radius:10px;padding:16px;box-shadow:0 1px 4px rgba(0,0,0,.08);cursor:pointer;transition:box-shadow .2s,transform .2s}.card:hover{box-shadow:0 3px 12px rgba(0,0,0,.12);transform:translateY(-2px)}
.card-head{display:flex;align-items:center;gap:6px;margin-bottom:8px;flex-wrap:wrap}.src-tag{padding:2px 8px;border-radius:10px;font-size:11px;background:#409eff;color:#fff}.lang-badge{padding:1px 6px;border-radius:8px;font-size:10px}.lang-badge.cn{background:#fff0f0;color:#f56c6c}.lang-badge.en{background:#f0f0ff;color:#409eff}.cat-badge{padding:1px 6px;border-radius:8px;font-size:10px;background:#f0f0f0;color:#888}
.card h3{font-size:15px;margin:0 0 4px;line-height:1.4;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden}
.title-zh{font-size:13px!important;color:#888!important;font-weight:400!important;-webkit-line-clamp:1!important;margin-bottom:6px!important}
.sum{font-size:13px;color:#666;line-height:1.5;display:-webkit-box;-webkit-line-clamp:3;-webkit-box-orient:vertical;overflow:hidden;margin:0 0 4px}
.sum-zh{font-size:12px;color:#aaa;line-height:1.5;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;margin:0 0 10px}
.card-foot{display:flex;justify-content:space-between;align-items:center;font-size:12px;color:#999}
.score{font-weight:700;font-size:11px;padding:2px 8px;border-radius:10px}.hi{color:#67c23a;background:#f0f9eb}.mid{color:#e6a23c;background:#fdf6ec}.lo{color:#f56c6c;background:#fef0f0}
.more{text-align:center;padding:24px}.more button{padding:8px 28px;background:#409eff;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:14px}
</style>
