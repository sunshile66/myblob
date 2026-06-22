<template>
  <SimpleLayout>
    <div class="bento-home">
      <AnnouncementBar />

      <BentoGrid :cols="3" :gap="16">
        <!-- Row 1: Brand Hero (2 cols) + Stats x2 -->
        <BrandHeroBlock
          eyebrow="创作与效率工作台"
          title="MyBlob — 灵感写作为主，工具提效为辅。"
          description="把笔记创作、内容管理和高频开发工具整合到一个轻量工作台。一键发布，随时查阅。"
          primary-label="开始浏览内容"
          secondary-label="打开工具中心"
          @primary="scrollToContent"
          @secondary="router.push('/tools')"
        />

        <StatsBlock
          label="笔记总数"
          :value="totalPosts"
          accent="linear-gradient(135deg, #4F46E5, #6366F1)"
          :icon="Document"
          @click="scrollToContent"
        />

        <StatsBlock
          label="在线工具"
          :value="toolCount"
          accent="linear-gradient(135deg, #F59E0B, #EF4444)"
          :icon="Operation"
          @click="router.push('/tools')"
        />

        <StatsBlock
          label="新闻聚合"
          :value="newsCount"
          accent="linear-gradient(135deg, #06B6D4, #0EA5E9)"
          :icon="ChatLineSquare"
          @click="router.push('/news')"
        />

        <StatsBlock
          label="知识百科"
          :value="knowledgeCats"
          accent="linear-gradient(135deg, #8B5CF6, #EC4899)"
          :icon="Reading"
          @click="router.push('/knowledge')"
        />

        <!-- Row 2: Featured Posts x2  + Category Cloud (row-span 2 on right) -->
        <FeaturedPostBlock
          v-if="featuredPosts[0]"
          :post="featuredPosts[0]"
          kicker="编辑推荐"
          @click="(p: Post) => viewNote(p.slug)"
        />
        <FeaturedPostBlock
          v-if="featuredPosts[1]"
          :post="featuredPosts[1]"
          kicker="热门内容"
          @click="(p: Post) => viewNote(p.slug)"
        />

        <CategoryCloudBlock
          :categories="categories"
          :tags="tags"
          @select-category="goToCategory"
          @select-tag="goToTag"
        />

        <!-- Row 3-4: Tool Cards x4 (2x2) -->
        <ToolCardBlock
          v-for="tool in displayTools"
          :key="tool.slug"
          :tool="tool"
          @click="goToTool"
        />

        <!-- Row 4: Ad Banner (full width, horizontal) -->
        <div class="ad-section">
          <AdBanner position="banner" />
        </div>

        <!-- Row 5: Content Stream (full width) -->
        <ContentStreamBlock
          :posts="posts"
          :loading="loading"
          :loading-more="loadingMore"
          :has-more="hasMore"
          :active-tab="activeTab"
          :tabs="tabs"
          @switch-tab="switchTab"
          @load-more="loadMorePosts"
          @view-note="viewNote"
        />
      </BentoGrid>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Document, Operation, Reading, ChatLineSquare } from "@element-plus/icons-vue";
import { getCategories, getPosts, getTags } from "@/api/blog";
import { getNewsList } from "@/api/news";
import AnnouncementBar from "@/components/common/AnnouncementBar.vue";
import AdBanner from "@/components/common/AdBanner.vue";
import BentoGrid from "@/components/common/BentoGrid.vue";
import BrandHeroBlock from "@/components/home/BrandHeroBlock.vue";
import StatsBlock from "@/components/home/StatsBlock.vue";
import FeaturedPostBlock from "@/components/home/FeaturedPostBlock.vue";
import ToolCardBlock from "@/components/home/ToolCardBlock.vue";
import CategoryCloudBlock from "@/components/home/CategoryCloudBlock.vue";
import ContentStreamBlock from "@/components/home/ContentStreamBlock.vue";
import SimpleLayout from "@/layout/SimpleLayout.vue";
import { FEATURED_TOOLS, TOOL_CATALOG } from "@/data/toolCatalog";
import type { ToolItem } from "@/data/toolCatalog";
import type { Category, Post, Tag } from "@/types";
import { getRecentTools } from "@features/tools/lib/recentTools";

const router = useRouter();
const contentRef = ref<HTMLElement | null>(null);
const categories = ref<Category[]>([]);
const tags = ref<Tag[]>([]);
const posts = ref<Post[]>([]);
const loading = ref(false);
const loadingMore = ref(false);
const hasMore = ref(false);
const totalPosts = ref(0);
const page = ref(1);
const newsCount = ref(0)
const knowledgeCats = ref(11)
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

const toolCount = TOOL_CATALOG.length;

const displayTools = computed(() => {
  const source = recentTools.value.length > 0 ? recentTools.value : FEATURED_TOOLS;
  return source.slice(0, 4) as ToolItem[];
});

const featuredPosts = computed(() => {
  return posts.value.slice(0, 2);
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

    const activeTabMeta = tabs.find((t) => t.value === activeTab.value) ?? tabs[0];
    const response = await getPosts({
      page: page.value - 1,
      page_size: 15,
      status: "published",
      ordering: activeTabMeta.ordering,
    });

    totalPosts.value = response.count;
    hasMore.value = response.results?.length >= 15;

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

const switchTab = (tab: string) => {
  if (activeTab.value === tab) return;
  activeTab.value = tab as TabValue;
  page.value = 1;
  void fetchPosts(true);
};

const loadMorePosts = () => {
  if (!hasMore.value || loadingMore.value) return;
  page.value += 1;
  void fetchPosts(false);
};

const scrollToContent = () => {
  window.scrollTo({ top: 400, behavior: "smooth" });
};

const goToCategory = (slug: string) => {
  router.push({ name: "Category", params: { slug } });
};

const goToTag = (slug: string) => {
  router.push({ name: "Tag", params: { slug } });
};

const goToTool = (tool: ToolItem) => {
  router.push(`/tools/${tool.slug}`);
};

const viewNote = (slug: string) => {
  router.push(`/note/${slug}`);
};

onMounted(async () => {
  void Promise.all([loadDiscoveryData(), fetchPosts(true)])
  // 异步加载新闻数和知识分类数（不阻塞首页）
  getNewsList({ page: 0, size: 1 }).then((r: any) => { newsCount.value = r?.count || 0 }).catch(() => {})
  import('@/api/knowledge').then(m => m.getKnowledgeCategories().then(cats => { if (Array.isArray(cats)) knowledgeCats.value = cats.length }).catch(() => {}))
});
</script>

<style scoped>
.bento-home {
  padding-top: 0;
}

.ad-section {
  grid-column: 1 / -1;
  padding: 4px 0 8px;
}

/* ===== Responsive ===== */
@media (max-width: 1280px) {
  .bento-home :deep(.bento-grid) {
    grid-template-columns: repeat(2, 1fr) !important;
  }

  /* All col-span-3 blocks become col-span-2 on 2-col layout */
  .bento-home :deep(.bento-card--col-3) {
    grid-column: span 2;
  }
}

@media (max-width: 768px) {
  .bento-home :deep(.bento-grid) {
    grid-template-columns: 1fr !important;
    gap: 12px !important;
  }

  /* All blocks span full width on mobile */
  .bento-home :deep(.bento-card--col-1),
  .bento-home :deep(.bento-card--col-2),
  .bento-home :deep(.bento-card--col-3) {
    grid-column: span 1;
  }

  /* Category cloud doesn't need extra row height on mobile */
  .bento-home :deep(.bento-card--row-2) {
    grid-row: span 1;
  }
}

/* ===== Animations ===== */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.bento-home :deep(.bento-card) {
  animation: fadeInUp 0.5s cubic-bezier(0.4, 0, 0.2, 1) both;
}

.bento-home :deep(.bento-card:nth-child(1)) { animation-delay: 0.06s; }
.bento-home :deep(.bento-card:nth-child(2)) { animation-delay: 0.12s; }
.bento-home :deep(.bento-card:nth-child(3)) { animation-delay: 0.18s; }
.bento-home :deep(.bento-card:nth-child(4)) { animation-delay: 0.24s; }
.bento-home :deep(.bento-card:nth-child(5)) { animation-delay: 0.24s; }
.bento-home :deep(.bento-card:nth-child(6)) { animation-delay: 0.3s; }
.bento-home :deep(.bento-card:nth-child(7)) { animation-delay: 0.3s; }
.bento-home :deep(.bento-card:nth-child(8)) { animation-delay: 0.36s; }
.bento-home :deep(.bento-card:nth-child(9)) { animation-delay: 0.36s; }
.bento-home :deep(.bento-card:nth-child(10)) { animation-delay: 0.42s; }
.bento-home :deep(.bento-card:nth-child(11)) { animation-delay: 0.42s; }
.bento-home :deep(.bento-card:nth-child(12)) { animation-delay: 0.48s; }

@media (prefers-reduced-motion: reduce) {
  .bento-home :deep(.bento-card) {
    animation: none;
  }
}
</style>
