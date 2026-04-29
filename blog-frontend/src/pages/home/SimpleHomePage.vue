<template>
  <SimpleLayout>
    <div class="simple-home">
      <AnnouncementBar />

      <section class="hero-section">
        <div class="hero-card">
          <div class="hero-copy">
            <span class="hero-eyebrow">MyBlob Workbench</span>
            <h1>把灵感笔记和高频开发工具放进同一个首页。</h1>
            <p>
              从内容浏览、分类探索，到 JSON、正则、时间戳这些开发细节处理，
              都能在这一套界面里连续完成，不再来回切换。
            </p>

            <div class="hero-search">
              <el-input
                v-model="searchQuery"
                placeholder="搜索笔记、标签或主题..."
                clearable
                @keyup.enter="goSearch"
              >
                <template #append>
                  <el-button @click="goSearch">
                    <el-icon><Search /></el-icon>
                    搜索
                  </el-button>
                </template>
              </el-input>
            </div>

            <div class="hero-actions">
              <el-button type="primary" size="large" round @click="scrollToFeed">
                开始浏览
                <el-icon><ArrowRight /></el-icon>
              </el-button>
              <el-button size="large" round @click="router.push('/tools')">
                打开工具中心
              </el-button>
            </div>

            <div class="hero-stats">
              <article
                v-for="item in heroStats"
                :key="item.label"
                class="hero-stat"
              >
                <strong>{{ item.value }}</strong>
                <span>{{ item.label }}</span>
              </article>
            </div>

            <div v-if="categoryPreview.length" class="hero-categories">
              <button
                v-for="category in categoryPreview"
                :key="category.id"
                class="hero-chip"
                @click="goToCategory(category.slug)"
              >
                {{ category.name }}
              </button>
            </div>
          </div>

          <div class="hero-side">
            <section class="spotlight-card">
              <div class="panel-head">
                <div>
                  <span class="panel-kicker">高频工具</span>
                  <h2>从首页直达常用工具</h2>
                </div>
                <router-link to="/tools">查看全部</router-link>
              </div>

              <div class="tool-preview-grid">
                <button
                  v-for="tool in quickTools"
                  :key="tool.slug"
                  class="tool-preview"
                  @click="goToTool(tool.slug)"
                >
                  <span
                    class="tool-preview__icon"
                    :style="{ background: tool.accent }"
                  >
                    {{ tool.name.slice(0, 1) }}
                  </span>
                  <div>
                    <strong>{{ tool.name }}</strong>
                    <span>{{ tool.tags.join(" · ") }}</span>
                  </div>
                </button>
              </div>
            </section>

            <section class="spotlight-card">
              <div class="panel-head">
                <div>
                  <span class="panel-kicker">热门标签</span>
                  <h2>继续探索内容主题</h2>
                </div>
                <span class="panel-meta">{{ tagPreview.length }} 个标签</span>
              </div>

              <div class="tag-cloud">
                <button
                  v-for="tag in tagPreview"
                  :key="tag.id"
                  class="tag-cloud__item"
                  @click="goToTag(tag.slug)"
                >
                  # {{ tag.name }}
                </button>
              </div>
            </section>
          </div>
        </div>
      </section>

      <div class="content-shell">
        <section ref="feedSection" class="feed-section">
          <div class="section-head">
            <div>
              <span class="section-kicker">内容流</span>
              <h2>{{ activeTabMeta.label }}笔记</h2>
              <p>{{ activeTabMeta.description }}</p>
            </div>

            <div class="tabs">
              <button
                v-for="tab in tabs"
                :key="tab.value"
                class="tab-item"
                :class="{ active: activeTab === tab.value }"
                @click="switchTab(tab.value)"
              >
                {{ tab.label }}
              </button>
            </div>
          </div>

          <div
            v-if="loading && posts.length === 0"
            class="notes-grid notes-grid--placeholder"
          >
            <div v-for="item in 8" :key="item" class="placeholder-card" />
          </div>

          <div v-else-if="posts.length > 0" class="notes-grid">
            <NoteCard
              v-for="post in posts"
              :key="post.id"
              :post="post"
              @click="viewNote(post.slug)"
            />
          </div>

          <div v-else class="empty-state">
            <el-empty description="暂时还没有内容，晚点再来看看。" />
          </div>

          <div v-if="posts.length > 0 && hasMore" class="load-more">
            <el-button round :loading="loadingMore" @click="loadMorePosts">
              {{ loadingMore ? "加载中..." : "加载更多内容" }}
            </el-button>
          </div>
        </section>

        <aside class="home-sidebar">
          <section class="sidebar-card">
            <div class="panel-head">
              <div>
                <span class="panel-kicker">分类导航</span>
                <h2>快速切换主题</h2>
              </div>
              <span class="panel-meta">{{ categoryPreview.length }} 个分类</span>
            </div>

            <div class="category-list">
              <button
                v-for="category in categoryPreview"
                :key="category.id"
                class="category-item"
                @click="goToCategory(category.slug)"
              >
                <span>{{ category.name }}</span>
                <el-icon><ArrowRight /></el-icon>
              </button>
            </div>
          </section>

          <section class="sidebar-card">
            <div class="panel-head">
              <div>
                <span class="panel-kicker">工具捷径</span>
                <h2>最近常用</h2>
              </div>
              <router-link to="/tools">工具中心</router-link>
            </div>

            <div class="shortcut-list">
              <button
                v-for="tool in quickTools"
                :key="tool.slug"
                class="shortcut-item"
                @click="goToTool(tool.slug)"
              >
                <span class="shortcut-item__title">{{ tool.name }}</span>
                <span class="shortcut-item__desc">{{ tool.description }}</span>
              </button>
            </div>
          </section>

          <AdBanner position="sidebar" />
        </aside>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { ArrowRight, Search } from "@element-plus/icons-vue";
import { getCategories, getPosts, getTags } from "@/api/post";
import AnnouncementBar from "@/components/common/AnnouncementBar.vue";
import AdBanner from "@/components/common/AdBanner.vue";
import NoteCard from "@/components/post/NoteCard.vue";
import { FEATURED_TOOLS } from "@features/tools/config/toolCatalog";
import SimpleLayout from "@/layout/SimpleLayout.vue";
import type { Category, Post, Tag } from "@/types";
import { getRecentTools } from "@features/tools/lib/recentTools";

const router = useRouter();
const feedSection = ref<HTMLElement | null>(null);
const searchQuery = ref("");
const categories = ref<Category[]>([]);
const tags = ref<Tag[]>([]);
const posts = ref<Post[]>([]);
const loading = ref(false);
const loadingMore = ref(false);
const hasMore = ref(false);
const totalPosts = ref(0);
const page = ref(1);
const recentTools = ref(getRecentTools());

const tabs = [
  {
    label: "推荐",
    value: "recommend",
    ordering: "-published_at",
    description: "优先展示最近发布、适合开始浏览的内容。",
  },
  {
    label: "最新",
    value: "latest",
    ordering: "-created_at",
    description: "按创建时间排序，适合追踪最新更新。",
  },
  {
    label: "热门",
    value: "hot",
    ordering: "-view_count",
    description: "根据浏览热度排序，快速看到被反复访问的内容。",
  },
] as const;

type TabValue = (typeof tabs)[number]["value"];

const activeTab = ref<TabValue>("recommend");

const activeTabMeta = computed(
  () => tabs.find((tab) => tab.value === activeTab.value) ?? tabs[0]
);

const categoryPreview = computed(() => categories.value.slice(0, 6));
const tagPreview = computed(() => tags.value.slice(0, 10));
const quickTools = computed(() => {
  const source =
    recentTools.value.length > 0 ? recentTools.value : FEATURED_TOOLS;
  return source.slice(0, 4);
});

const heroStats = computed(() => [
  { label: "内容条目", value: formatCount(totalPosts.value || posts.value.length) },
  { label: "分类主题", value: `${categories.value.length}` },
  { label: "常用工具", value: `${FEATURED_TOOLS.length}+` },
]);

const loadDiscoveryData = async () => {
  try {
    const [categoryData, tagData] = await Promise.all([
      getCategories(),
      getTags(),
    ]);
    categories.value = Array.isArray(categoryData) ? categoryData : [];
    tags.value = Array.isArray(tagData) ? tagData : [];
  } catch (error) {
    console.error("Failed to load discovery data:", error);
  }
};

const fetchPosts = async (reset = true) => {
  try {
    if (reset) {
      loading.value = true;
    } else {
      loadingMore.value = true;
    }

    const response = await getPosts({
      page: page.value,
      page_size: 15,
      status: "published",
      ordering: activeTabMeta.value.ordering,
    });

    totalPosts.value = response.count;
    hasMore.value = Boolean(response.next);

    if (reset) {
      posts.value = response.results ?? [];
    } else {
      posts.value = [...posts.value, ...(response.results ?? [])];
    }
  } catch (error: any) {
    console.error("Failed to load posts:", error);
    if (error.response?.status !== 401) {
      ElMessage.error("内容加载失败，请稍后重试");
    }
  } finally {
    loading.value = false;
    loadingMore.value = false;
  }
};

const switchTab = (tab: TabValue) => {
  if (activeTab.value === tab) {
    return;
  }

  activeTab.value = tab;
  page.value = 1;
  void fetchPosts(true);
};

const loadMorePosts = () => {
  if (!hasMore.value || loadingMore.value) {
    return;
  }

  page.value += 1;
  void fetchPosts(false);
};

const scrollToFeed = () => {
  feedSection.value?.scrollIntoView({ behavior: "smooth", block: "start" });
};

const goSearch = () => {
  const keyword = searchQuery.value.trim();
  if (!keyword) {
    return;
  }

  router.push({ name: "Search", query: { q: keyword } });
};

const goToCategory = (slug: string) => {
  router.push({ name: "Category", params: { slug } });
};

const goToTag = (slug: string) => {
  router.push({ name: "Tag", params: { slug } });
};

const goToTool = (slug: string) => {
  router.push(`/tools/${slug}`);
};

const viewNote = (slug: string) => {
  router.push(`/note/${slug}`);
};

const formatCount = (count: number) => {
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}w`;
  }

  if (count >= 1000) {
    return `${(count / 1000).toFixed(1)}k`;
  }

  return `${count}`;
};

onMounted(() => {
  void Promise.all([loadDiscoveryData(), fetchPosts(true)]);
});
</script>

<style scoped>
.simple-home {
  min-height: 100vh;
  padding-top: 10px;
}

.hero-section {
  margin-bottom: 28px;
}

.hero-card {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.9fr);
  gap: 24px;
  padding: 28px;
  border-radius: 32px;
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.72)),
    linear-gradient(135deg, rgba(14, 165, 233, 0.1), rgba(15, 23, 42, 0.02));
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.08);
}

.hero-copy {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.hero-eyebrow,
.section-kicker,
.panel-kicker {
  display: inline-flex;
  width: fit-content;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  color: var(--theme-primary);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-copy h1 {
  margin: 0;
  max-width: 760px;
  font-size: clamp(34px, 4.6vw, 56px);
  line-height: 1.04;
  letter-spacing: -0.04em;
  color: #0f172a;
}

.hero-copy p,
.section-head p {
  margin: 0;
  max-width: 720px;
  color: #475569;
  font-size: 16px;
  line-height: 1.8;
}

.hero-search {
  max-width: 540px;
}

.hero-search :deep(.el-input-group__append .el-button) {
  border: none;
  color: white;
  background: linear-gradient(135deg, #0f172a 0%, #1d4ed8 100%);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  max-width: 560px;
}

.hero-stat {
  padding: 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.hero-stat strong {
  display: block;
  margin-bottom: 6px;
  font-size: 22px;
  color: #0f172a;
}

.hero-stat span {
  font-size: 13px;
  color: #64748b;
}

.hero-categories,
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-chip,
.tag-cloud__item {
  border: none;
  border-radius: 999px;
  padding: 10px 14px;
  background: rgba(15, 23, 42, 0.05);
  color: #0f172a;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.hero-chip:hover,
.tag-cloud__item:hover {
  background: rgba(255, 36, 66, 0.12);
  color: var(--theme-primary);
}

.hero-side,
.home-sidebar {
  display: grid;
  gap: 18px;
}

.spotlight-card,
.sidebar-card {
  padding: 20px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(148, 163, 184, 0.12);
  box-shadow: 0 20px 36px rgba(15, 23, 42, 0.06);
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 18px;
}

.panel-head h2,
.section-head h2 {
  margin: 10px 0 6px;
  font-size: 28px;
  color: #0f172a;
  line-height: 1.15;
}

.panel-head a,
.panel-meta {
  color: #64748b;
  font-size: 13px;
  text-decoration: none;
}

.panel-head a:hover {
  color: var(--theme-primary);
}

.tool-preview-grid,
.shortcut-list,
.category-list {
  display: grid;
  gap: 12px;
}

.tool-preview,
.shortcut-item,
.category-item {
  width: 100%;
  border: none;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  border-radius: 18px;
  background: rgba(15, 23, 42, 0.04);
  text-align: left;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tool-preview:hover,
.shortcut-item:hover,
.category-item:hover {
  transform: translateY(-2px);
  background: rgba(255, 36, 66, 0.08);
}

.tool-preview__icon {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: 800;
  flex-shrink: 0;
}

.tool-preview strong,
.shortcut-item__title {
  display: block;
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
}

.tool-preview span:last-child,
.shortcut-item__desc,
.category-item span {
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.category-item {
  justify-content: space-between;
}

.content-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 24px;
}

.feed-section {
  min-width: 0;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 20px;
  margin-bottom: 20px;
}

.tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tab-item {
  border: none;
  border-radius: 999px;
  padding: 10px 16px;
  background: rgba(15, 23, 42, 0.05);
  color: #475569;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tab-item:hover,
.tab-item.active {
  background: linear-gradient(135deg, #0f172a 0%, #1d4ed8 100%);
  color: white;
}

.notes-grid {
  column-count: 4;
  column-gap: 14px;
}

.notes-grid > * {
  break-inside: avoid;
  margin-bottom: 14px;
}

.notes-grid--placeholder {
  column-count: 3;
}

.placeholder-card {
  height: 320px;
  border-radius: 24px;
  margin-bottom: 14px;
  background:
    linear-gradient(90deg, rgba(226, 232, 240, 0.8), rgba(248, 250, 252, 0.95), rgba(226, 232, 240, 0.8));
  background-size: 200% 100%;
  animation: shimmer 1.8s linear infinite;
}

.empty-state {
  padding: 48px 0;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.82);
}

.load-more {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }

  100% {
    background-position: -200% 0;
  }
}

@media (max-width: 1200px) {
  .hero-card,
  .content-shell {
    grid-template-columns: 1fr;
  }

  .notes-grid {
    column-count: 3;
  }
}

@media (max-width: 768px) {
  .hero-card,
  .spotlight-card,
  .sidebar-card {
    padding: 18px;
    border-radius: 24px;
  }

  .hero-stats {
    grid-template-columns: 1fr;
  }

  .section-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .notes-grid,
  .notes-grid--placeholder {
    column-count: 2;
  }
}

@media (max-width: 520px) {
  .notes-grid,
  .notes-grid--placeholder {
    column-count: 1;
  }

  .hero-copy h1 {
    font-size: 34px;
  }
}
</style>
