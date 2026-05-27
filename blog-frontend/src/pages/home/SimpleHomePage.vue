<template>
  <SimpleLayout noFooter>
    <div class="simple-home">
      <AnnouncementBar />

      <!-- Hero: 网站简介，博客导向 -->
      <section class="hero-section">
        <div class="hero-card">
          <div class="hero-copy">
            <span class="hero-eyebrow">创意与效率工作台</span>
            <h1>把灵感和笔记变成内容，用工具链提效。</h1>
            <p>
              MyBlob 是一个以笔记/博客为中心的创作平台，集成了高频开发工具和内容管理能力。
              写笔记、浏览文章、处理 JSON 和正则，一个界面完成。
            </p>
            <div class="hero-actions">
              <el-button type="primary" size="large" round @click="scrollToFeed">
                开始浏览<el-icon class="ml-2"><ArrowRight /></el-icon>
              </el-button>
              <el-button size="large" round @click="router.push('/tools')">
                打开工具中心
              </el-button>
            </div>
          </div>
          <div class="hero-visual" aria-hidden="true">
            <div class="hero-graphic">
              <span class="hero-emoji">✍️</span>
              <div class="hero-badge hero-badge--1">📝 笔记</div>
              <div class="hero-badge hero-badge--2">🔧 工具</div>
              <div class="hero-badge hero-badge--3">📊 数据</div>
            </div>
          </div>
        </div>
      </section>

      <!-- 主内容区：博客内容流 + 侧栏 -->
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

          <div v-if="loading && posts.length === 0" class="notes-grid notes-grid--placeholder">
            <div v-for="item in 8" :key="item" class="placeholder-card">
              <div class="placeholder-image" />
              <div class="placeholder-body">
                <div class="placeholder-line placeholder-line--l" />
                <div class="placeholder-line placeholder-line--m" />
                <div class="placeholder-line placeholder-line--s" />
              </div>
            </div>
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
          <!-- 高频工具 -->
          <section class="sidebar-card">
            <div class="panel-head">
              <span class="panel-kicker">高频工具</span>
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
                  <span>{{ tool.tags.join(' \u00B7 ') }}</span>
                </div>
              </button>
            </div>
          </section>

          <!-- 热门标签 -->
          <section class="sidebar-card">
            <div class="panel-head">
              <span class="panel-kicker">热门标签</span>
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

          <!-- 分类导航 -->
          <section class="sidebar-card">
            <div class="panel-head">
              <span class="panel-kicker">分类导航</span>
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
              </button>
            </div>
          </section>
        </aside>
      </div>

      <AdBanner position="sidebar" />
    </div>
  </SimpleLayout>
</template>
<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { ArrowRight } from "@element-plus/icons-vue";
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

const categoryPreview = computed(() => categories.value.slice(0, 4));
const tagPreview = computed(() => tags.value.slice(0, 6));
const quickTools = computed(() => {
  const source =
    recentTools.value.length > 0 ? recentTools.value : FEATURED_TOOLS;
  return source.slice(0, 3);
});

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

onMounted(() => {
  void Promise.all([loadDiscoveryData(), fetchPosts(true)]);
});
</script>

<style scoped>
.simple-home {
  padding-top: 8px;
}

/* ===== Hero ===== */
.hero-section {
  margin-bottom: 16px;
}

.hero-card {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 24px 28px;
  border-radius: 18px;
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.74)),
    linear-gradient(135deg, rgba(99, 102, 241, 0.08), rgba(15, 23, 42, 0.02));
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.hero-copy {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-visual {
  flex-shrink: 0;
  display: none;
}

@media (min-width: 768px) {
  .hero-visual {
    display: block;
  }
}

.hero-graphic {
  position: relative;
  width: 180px;
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-emoji {
  font-size: 64px;
  opacity: 0.15;
  position: absolute;
}

.hero-badge {
  position: absolute;
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  color: var(--theme-text);
  font-size: 13px;
  font-weight: 600;
  box-shadow: var(--shadow-sm);
  white-space: nowrap;
}

.hero-badge--1 { top: 4px; left: 8px; }
.hero-badge--2 { top: 56px; right: 0; }
.hero-badge--3 { bottom: 8px; left: 24px; }

.hero-eyebrow,
.section-kicker,
.panel-kicker {
  display: inline-flex;
  width: fit-content;
  padding: 4px 12px;
  border-radius: 999px;
  background: var(--theme-primary-light);
  color: var(--theme-primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.hero-copy h1 {
  margin: 0;
  max-width: 600px;
  font-size: clamp(22px, 2.8vw, 30px);
  line-height: 1.3;
  letter-spacing: -0.02em;
  font-weight: 700;
  color: var(--theme-text);
}

.hero-copy p {
  margin: 0;
  max-width: 560px;
  color: var(--theme-text-secondary);
  font-size: 15px;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.ml-2 {
  margin-left: 4px;
}

/* ===== Sidebar panels ===== */
.tool-preview-grid {
  display: grid;
  gap: 8px;
}

.tool-preview {
  width: 100%;
  border: none;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  background: var(--theme-hover);
  text-align: left;
  cursor: pointer;
  transition: background var(--transition-fast), box-shadow var(--transition-fast);
  font-family: inherit;
  font-size: inherit;
}

.tool-preview:hover {
  background: rgba(79, 70, 229, 0.08);
}

.tool-preview:focus-visible {
  outline: 2px solid var(--theme-primary);
  outline-offset: 2px;
}

.tool-preview__icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  font-weight: 800;
  flex-shrink: 0;
}

.tool-preview strong {
  display: block;
  color: var(--theme-text);
  font-size: 13px;
  font-weight: 700;
}

.tool-preview span:last-child {
  color: var(--theme-text-secondary);
  font-size: 12px;
  line-height: 1.3;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-cloud__item {
  border: 1px solid var(--theme-border);
  border-radius: 999px;
  padding: 6px 12px;
  background: var(--theme-card);
  color: var(--theme-text);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background var(--transition-fast), border-color var(--transition-fast), color var(--transition-fast);
  font-family: inherit;
}

.tag-cloud__item:hover {
  background: var(--theme-primary-light);
  border-color: var(--theme-primary);
  color: var(--theme-primary);
}

.tag-cloud__item:focus-visible {
  outline: 2px solid var(--theme-primary);
  outline-offset: 2px;
}

/* ===== Content shell ===== */
.content-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 260px;
  gap: 24px;
  align-items: start;
}

.feed-section {
  min-width: 0;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 16px;
}

.section-head h2 {
  margin: 4px 0 2px;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.2;
  color: var(--theme-text);
}

.section-head p {
  margin: 0;
  color: var(--theme-text-secondary);
  font-size: 14px;
  line-height: 1.4;
}

.tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tab-item {
  border: none;
  border-radius: 999px;
  padding: 8px 20px;
  background: rgba(15, 23, 42, 0.06);
  color: var(--theme-text-secondary);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background var(--transition-fast), color var(--transition-fast);
  font-family: inherit;
}

.tab-item:hover,
.tab-item.active {
  background: var(--gradient-primary);
  color: #fff;
}

.tab-item:focus-visible {
  outline: 2px solid var(--theme-primary);
  outline-offset: 2px;
}

/* ===== Notes grid (CSS Grid, left-to-right top-to-bottom) ===== */
.notes-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.notes-grid--placeholder {
  grid-template-columns: repeat(3, 1fr);
}

/* Placeholder cards with skeleton structure */
.placeholder-card {
  border-radius: 14px;
  overflow: hidden;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
}

.placeholder-image {
  width: 100%;
  aspect-ratio: 3 / 4;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.6s ease-in-out infinite;
}

.placeholder-body {
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.placeholder-line {
  height: 12px;
  border-radius: 6px;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.6s ease-in-out infinite;
}

.placeholder-line--l { width: 80%; }
.placeholder-line--m { width: 55%; }
.placeholder-line--s { width: 35%; }

.empty-state {
  padding: 24px 16px;
  border-radius: 14px;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
}

.empty-state :deep(.el-empty) {
  padding: 8px 0;
}

.empty-state :deep(.el-empty__image) {
  width: 80px;
  height: 80px;
}

.empty-state :deep(.el-empty__description) {
  margin-top: 8px;
  font-size: 14px;
}

.load-more {
  margin-top: 28px;
  display: flex;
  justify-content: center;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ===== Sidebar ===== */
.home-sidebar {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sidebar-card {
  padding: 14px;
  border-radius: 12px;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  box-shadow: var(--shadow-sm);
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
}

.panel-head h2 {
  margin: 2px 0 0;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.3;
  color: var(--theme-text);
}

.panel-head a,
.panel-meta {
  color: var(--theme-text-secondary);
  font-size: 13px;
  text-decoration: none;
  white-space: nowrap;
}

.panel-head a:hover {
  color: var(--theme-primary);
}

.category-list {
  display: grid;
  gap: 6px;
}

.category-item {
  width: 100%;
  border: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  background: var(--theme-hover);
  color: var(--theme-text);
  font-size: 13px;
  font-weight: 600;
  text-align: left;
  cursor: pointer;
  transition: background var(--transition-fast), color var(--transition-fast);
  font-family: inherit;
}

.category-item:hover {
  background: var(--theme-primary-light);
  color: var(--theme-primary);
}

.category-item:focus-visible {
  outline: 2px solid var(--theme-primary);
  outline-offset: 2px;
}

/* ===== Responsive ===== */
@media (max-width: 1280px) {
  .notes-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 14px;
  }

  .notes-grid--placeholder {
    grid-template-columns: repeat(2, 1fr);
  }

  .content-shell {
    grid-template-columns: minmax(0, 1fr) 240px;
    gap: 20px;
  }
}

@media (max-width: 1024px) {
  .content-shell {
    grid-template-columns: 1fr;
  }

  .notes-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .home-sidebar {
    display: none;
  }

  .hero-card {
    padding: 20px;
  }
}

@media (max-width: 768px) {
  .hero-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 18px;
    border-radius: 14px;
  }

  .hero-visual {
    display: none;
  }

  .hero-copy h1 {
    font-size: 20px;
    line-height: 1.3;
  }

  .notes-grid,
  .notes-grid--placeholder {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .sidebar-card {
    padding: 12px;
    border-radius: 10px;
  }

  .section-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}

@media (max-width: 520px) {
  .notes-grid,
  .notes-grid--placeholder {
    grid-template-columns: 1fr;
  }

  .hero-actions {
    flex-direction: column;
    width: 100%;
  }

  .hero-actions .el-button {
    width: 100%;
  }
}

/* ===== Accessibility: Reduced motion ===== */
@media (prefers-reduced-motion: reduce) {
  .placeholder-image,
  .placeholder-line {
    animation: none;
    background: var(--theme-hover);
  }

  .tool-preview,
  .category-item,
  .tab-item,
  .tag-cloud__item {
    transition: none;
  }
}
</style>
