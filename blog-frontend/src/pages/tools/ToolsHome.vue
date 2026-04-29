<template>
  <SimpleLayout>
    <div class="tools-home">
      <section class="hero-section">
        <div class="hero-card">
          <div class="hero-copy">
            <span class="hero-eyebrow">Tool Hub</span>
            <h1>把开发里的高频小动作，压缩成一套随手可用的工具。</h1>
            <p>
              覆盖 JSON、正则、时间戳、图片处理、编码转换和接口调试，
              打开就能用，不需要跳转到一堆零散页面里。
            </p>

            <div class="hero-search">
              <el-input
                v-model="searchQuery"
                clearable
                placeholder="搜索工具名称、用途或关键词"
              />
            </div>

            <div class="hero-actions">
              <el-button
                type="primary"
                round
                size="large"
                @click="startWithFirstMatch"
              >
                立即开始
              </el-button>
              <el-button
                v-if="recentTools.length"
                round
                size="large"
                @click="openRecentTool"
              >
                继续最近工具
              </el-button>
            </div>
          </div>

          <div class="hero-metrics">
            <article
              v-for="item in metrics"
              :key="item.label"
              class="metric-card"
            >
              <strong>{{ item.value }}</strong>
              <span>{{ item.label }}</span>
            </article>
          </div>
        </div>
      </section>

      <section class="filter-section">
        <div class="filters">
          <button
            v-for="item in filterOptions"
            :key="item.key"
            class="filter-chip"
            :class="{ active: activeFilter === item.key }"
            @click="activeFilter = item.key"
          >
            {{ item.label }}
          </button>
        </div>

        <div class="filter-meta">
          <span>{{ filteredTools.length }} 个可用工具</span>
          <button v-if="searchQuery" class="clear-btn" @click="searchQuery = ''">
            清除搜索
          </button>
        </div>
      </section>

      <section v-if="recentTools.length" class="recent-section">
        <div class="section-head">
          <div>
            <span class="section-kicker">最近使用</span>
            <h2>继续你的上一次操作</h2>
          </div>
        </div>

        <div class="recent-grid">
          <button
            v-for="tool in recentTools.slice(0, 3)"
            :key="tool.slug"
            class="recent-card"
            @click="goToTool(tool.slug)"
          >
            <span class="recent-card__icon" :style="{ background: tool.accent }">
              <component :is="iconMap[tool.icon]" />
            </span>
            <div>
              <strong>{{ tool.name }}</strong>
              <span>{{ tool.description }}</span>
            </div>
          </button>
        </div>
      </section>

      <section v-if="featuredTools.length" class="featured-section">
        <div class="section-head">
          <div>
            <span class="section-kicker">精选入口</span>
            <h2>适合一打开就用的高频工具</h2>
          </div>
        </div>

        <div class="featured-grid">
          <button
            v-for="tool in featuredTools"
            :key="tool.slug"
            class="featured-card"
            @click="goToTool(tool.slug)"
          >
            <div class="featured-card__top">
              <span class="featured-card__icon" :style="{ background: tool.accent }">
                <component :is="iconMap[tool.icon]" />
              </span>
              <div class="featured-card__badges">
                <span v-if="tool.featured" class="badge badge--featured">精选</span>
                <span v-if="tool.isNew" class="badge badge--new">新增</span>
              </div>
            </div>
            <h3>{{ tool.name }}</h3>
            <p>{{ tool.description }}</p>
            <div class="tag-row">
              <span v-for="tag in tool.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>
          </button>
        </div>
      </section>

      <section
        v-for="section in visibleSections"
        :key="section.category.key"
        class="tools-section"
      >
        <div class="section-head">
          <div>
            <span class="section-kicker">{{ section.category.label }}</span>
            <h2>{{ section.category.description }}</h2>
          </div>
          <span class="section-count">{{ section.tools.length }} 个工具</span>
        </div>

        <div class="tools-grid">
          <button
            v-for="tool in section.tools"
            :key="tool.slug"
            class="tool-card"
            @click="goToTool(tool.slug)"
          >
            <div class="tool-card__top">
              <span class="tool-card__icon" :style="{ background: tool.accent }">
                <component :is="iconMap[tool.icon]" />
              </span>
              <span v-if="tool.isNew" class="badge badge--new">新</span>
            </div>
            <h3>{{ tool.name }}</h3>
            <p>{{ tool.description }}</p>
            <div class="tag-row">
              <span v-for="tag in tool.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>
          </button>
        </div>
      </section>

      <div v-if="visibleSections.length === 0" class="empty-wrap">
        <el-empty description="没有找到匹配的工具，换个关键词试试。" />
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { computed, ref, type Component } from "vue";
import { useRouter } from "vue-router";
import {
  Brush,
  Connection,
  Document,
  DocumentCopy,
  Edit,
  Key,
  Lock,
  Operation,
  Picture,
  Search,
  Ticket,
  Timer,
} from "@element-plus/icons-vue";
import {
  FEATURED_TOOLS,
  TOOL_CATALOG,
  TOOL_CATEGORIES,
  type ToolCategoryKey,
  type ToolIconKey,
} from "@features/tools/config/toolCatalog";
import SimpleLayout from "@/layout/SimpleLayout.vue";
import { getRecentTools } from "@features/tools/lib/recentTools";

type FilterKey = "all" | ToolCategoryKey;

const router = useRouter();
const searchQuery = ref("");
const activeFilter = ref<FilterKey>("all");
const recentTools = ref(getRecentTools());

const iconMap: Record<ToolIconKey, Component> = {
  Document,
  Key,
  Lock,
  Ticket,
  DocumentCopy,
  Edit,
  Picture,
  Connection,
  Search,
  Timer,
  Operation,
  Brush,
};

const filterOptions: Array<{ key: FilterKey; label: string }> = [
  { key: "all", label: "全部" },
  ...TOOL_CATEGORIES.map((category) => ({
    key: category.key,
    label: category.label,
  })),
];

const filteredTools = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase();

  return TOOL_CATALOG.filter((tool) => {
    const matchFilter =
      activeFilter.value === "all" || tool.category === activeFilter.value;

    const matchKeyword =
      !keyword ||
      tool.name.toLowerCase().includes(keyword) ||
      tool.description.toLowerCase().includes(keyword) ||
      tool.tags.some((tag) => tag.toLowerCase().includes(keyword));

    return matchFilter && matchKeyword;
  });
});

const featuredTools = computed(() =>
  filteredTools.value.filter((tool) => tool.featured).slice(0, 4)
);

const visibleSections = computed(() =>
  TOOL_CATEGORIES.map((category) => ({
    category,
    tools: filteredTools.value.filter((tool) => tool.category === category.key),
  })).filter((section) => section.tools.length > 0)
);

const metrics = computed(() => [
  { label: "工具总数", value: `${TOOL_CATALOG.length}` },
  { label: "精选推荐", value: `${FEATURED_TOOLS.length}` },
  { label: "最近使用", value: `${recentTools.value.length}` },
  { label: "工具分类", value: `${TOOL_CATEGORIES.length}` },
]);

const goToTool = (tool: string) => {
  router.push(`/tools/${tool}`);
};

const startWithFirstMatch = () => {
  const target = filteredTools.value[0];
  if (target) {
    goToTool(target.slug);
  }
};

const openRecentTool = () => {
  const target = recentTools.value[0];
  if (target) {
    goToTool(target.slug);
  }
};
</script>

<style scoped>
.tools-home {
  min-height: calc(100vh - 80px);
  padding: 12px 0 24px;
}

.hero-section,
.filter-section,
.recent-section,
.featured-section,
.tools-section {
  margin-bottom: 28px;
}

.hero-card {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(320px, 0.8fr);
  gap: 20px;
  padding: 28px;
  border-radius: 32px;
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.78)),
    linear-gradient(135deg, rgba(255, 255, 255, 0.4), rgba(15, 23, 42, 0.04));
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.08);
}

.hero-copy {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.hero-eyebrow,
.section-kicker {
  display: inline-flex;
  width: fit-content;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.06);
  color: var(--theme-primary);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-copy h1 {
  margin: 0;
  font-size: clamp(34px, 4.4vw, 54px);
  line-height: 1.04;
  color: #0f172a;
  letter-spacing: -0.04em;
}

.hero-copy p,
.tool-card p,
.featured-card p,
.recent-card span {
  margin: 0;
  color: #475569;
  font-size: 15px;
  line-height: 1.75;
}

.hero-actions,
.filters,
.tag-row,
.filter-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.metric-card {
  padding: 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.metric-card strong {
  display: block;
  margin-bottom: 6px;
  color: #0f172a;
  font-size: 24px;
}

.metric-card span,
.section-count,
.filter-meta span {
  color: #64748b;
  font-size: 13px;
}

.filter-section {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.filter-chip,
.clear-btn {
  border: none;
  border-radius: 999px;
  padding: 10px 14px;
  background: rgba(15, 23, 42, 0.05);
  color: #334155;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.filter-chip:hover,
.filter-chip.active,
.clear-btn:hover {
  background: linear-gradient(135deg, #0f172a 0%, #1d4ed8 100%);
  color: white;
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-end;
  margin-bottom: 18px;
}

.section-head h2 {
  margin: 10px 0 0;
  font-size: 28px;
  line-height: 1.2;
  color: #0f172a;
}

.recent-grid,
.featured-grid,
.tools-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.recent-card,
.featured-card,
.tool-card {
  width: 100%;
  border: none;
  padding: 20px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(148, 163, 184, 0.12);
  box-shadow: 0 18px 30px rgba(15, 23, 42, 0.06);
  text-align: left;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.recent-card:hover,
.featured-card:hover,
.tool-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 22px 36px rgba(15, 23, 42, 0.1);
}

.recent-card {
  display: flex;
  align-items: center;
  gap: 16px;
}

.recent-card__icon,
.featured-card__icon,
.tool-card__icon {
  width: 52px;
  height: 52px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 18px;
  color: white;
  font-size: 22px;
  flex-shrink: 0;
}

.featured-card__top,
.tool-card__top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 18px;
}

.featured-card__badges {
  display: flex;
  gap: 8px;
}

.featured-card h3,
.tool-card h3 {
  margin: 0 0 10px;
  color: #0f172a;
  font-size: 20px;
  line-height: 1.3;
}

.tag {
  display: inline-flex;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.06);
  color: #475569;
  font-size: 12px;
  font-weight: 700;
}

.badge {
  display: inline-flex;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}

.badge--featured {
  background: rgba(255, 36, 66, 0.12);
  color: var(--theme-primary);
}

.badge--new {
  background: rgba(15, 23, 42, 0.08);
  color: #0f172a;
}

.empty-wrap {
  padding: 42px 0;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.78);
}

@media (max-width: 1200px) {
  .hero-card {
    grid-template-columns: 1fr;
  }

  .recent-grid,
  .featured-grid,
  .tools-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .hero-card,
  .recent-card,
  .featured-card,
  .tool-card {
    padding: 18px;
  }

  .hero-metrics,
  .recent-grid,
  .featured-grid,
  .tools-grid {
    grid-template-columns: 1fr;
  }

  .section-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
