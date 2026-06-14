<template>
  <SimpleLayout>
    <div class="tool-desk">
      <aside class="desk-sidebar">
        <div class="desk-brand">
          <span>Toolbox</span>
          <strong>{{ TOOL_CATALOG.length }}</strong>
        </div>

        <button
          v-for="item in filterOptions"
          :key="item.key"
          class="nav-item"
          :class="{ active: activeFilter === item.key }"
          type="button"
          @click="activeFilter = item.key"
        >
          <span>{{ item.label }}</span>
          <strong>{{ item.count }}</strong>
        </button>
      </aside>

      <main class="desk-main">
        <section class="desk-toolbar">
          <div>
            <span class="toolbar-kicker">开发 / 爬虫 / 文本 / 图片</span>
            <h1>工具中心</h1>
            <p>用更短的路径完成格式化、转换、对比、生成和调试。</p>
          </div>

          <div class="toolbar-search">
            <el-input
              v-model="searchQuery"
              clearable
              size="large"
              placeholder="搜索 JSON、curl、Header、图片、SQL..."
              @keyup.enter="startWithFirstMatch"
            />
            <el-button type="primary" size="large" @click="startWithFirstMatch">
              打开
            </el-button>
          </div>
        </section>

        <section class="desk-stats">
          <article v-for="metric in metrics" :key="metric.label">
            <strong>{{ metric.value }}</strong>
            <span>{{ metric.label }}</span>
          </article>
        </section>

        <section v-if="recentTools.length" class="quick-row">
          <span>最近</span>
          <button
            v-for="tool in recentTools.slice(0, 5)"
            :key="tool.slug"
            type="button"
            @click="goToTool(tool.slug)"
          >
            {{ tool.name }}
          </button>
        </section>

        <section class="tool-grid" :class="{ 'is-empty': !filteredTools.length }">
          <button
            v-for="tool in filteredTools"
            :key="tool.slug"
            class="tool-card"
            type="button"
            @click="goToTool(tool.slug)"
          >
            <span class="tool-icon" :style="{ background: tool.accent }">
              <component :is="iconMap[tool.icon]" />
            </span>
            <span class="tool-copy">
              <strong>
                {{ tool.name }}
                <em v-if="tool.isNew">NEW</em>
              </strong>
              <small>{{ tool.description }}</small>
              <span class="tag-line">{{ tool.tags.slice(0, 3).join(" / ") }}</span>
            </span>
          </button>

          <el-empty
            v-if="!filteredTools.length"
            description="没有找到匹配的工具，换个关键词试试。"
          />
        </section>
      </main>
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

const categoryCounts = computed(() =>
  TOOL_CATEGORIES.reduce<Record<string, number>>((result, category) => {
    result[category.key] = TOOL_CATALOG.filter((tool) => tool.category === category.key).length;
    return result;
  }, {}),
);

const filterOptions = computed(() => [
  { key: "all" as const, label: "全部工具", count: TOOL_CATALOG.length },
  ...TOOL_CATEGORIES.map((category) => ({
    key: category.key,
    label: category.label,
    count: categoryCounts.value[category.key] || 0,
  })),
]);

const normalizedKeyword = computed(() => searchQuery.value.trim().toLowerCase());

const filteredTools = computed(() => {
  const keyword = normalizedKeyword.value;

  return TOOL_CATALOG.filter((tool) => {
    const matchFilter =
      activeFilter.value === "all" || tool.category === activeFilter.value;
    const searchable = [
      tool.name,
      tool.description,
      TOOL_CATEGORIES.find((category) => category.key === tool.category)?.label || "",
      ...tool.tags,
    ].join(" ").toLowerCase();

    return matchFilter && (!keyword || searchable.includes(keyword));
  });
});

const metrics = computed(() => [
  { label: "当前结果", value: `${filteredTools.value.length}` },
  { label: "精选工具", value: `${FEATURED_TOOLS.length}` },
  { label: "新增能力", value: `${TOOL_CATALOG.filter((tool) => tool.isNew).length}` },
  { label: "分类", value: `${TOOL_CATEGORIES.length}` },
]);

const goToTool = (tool: string) => {
  router.push(`/tools/${tool}`);
};

const startWithFirstMatch = () => {
  const target = filteredTools.value[0];
  if (target) goToTool(target.slug);
};
</script>

<style scoped>
.tool-desk {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 18px;
  height: calc(100vh - 92px);
  min-height: 620px;
  padding: 8px 0 24px;
  overflow: hidden;
}

.desk-sidebar,
.desk-toolbar,
.desk-stats article,
.quick-row,
.tool-card {
  border: 1px solid var(--theme-border);
  border-radius: 10px;
  background: var(--theme-card);
  box-shadow: none;
}

.desk-sidebar {
  position: sticky;
  top: 88px;
  display: grid;
  align-content: start;
  gap: 6px;
  height: fit-content;
  padding: 12px;
  border: 1px solid var(--theme-border);
  border-radius: 12px;
  background: var(--theme-card);
  box-shadow: none;
  transform: translateZ(0);
  will-change: transform;
}

.desk-brand,
.nav-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.desk-brand {
  padding: 14px 16px;
  border-radius: 10px;
  background: var(--gradient-primary);
  color: #fff;
}

.desk-brand span {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  opacity: 0.85;
}

.desk-brand strong {
  font-size: 24px;
  line-height: 1;
  font-weight: 700;
}

.nav-item {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid transparent;
  border-radius: 10px;
  background: transparent;
  color: var(--theme-text-secondary);
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  text-align: left;
  transition: all var(--transition-fast);
}

.nav-item strong {
  color: var(--theme-text-tertiary);
  font-size: 11px;
  font-weight: 500;
  padding: 2px 6px;
  border-radius: 4px;
  background: var(--theme-hover);
}

.nav-item:hover,
.nav-item.active {
  border-color: var(--theme-primary);
  background: var(--theme-primary-light);
  color: var(--theme-primary);
}
.nav-item.active strong { background: var(--theme-primary-light); color: var(--theme-primary); }

.desk-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
}

.desk-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(340px, 0.78fr);
  gap: 18px;
  align-items: end;
  padding: 18px;
}

.toolbar-kicker {
  display: inline-flex;
  margin-bottom: 8px;
  color: var(--theme-primary);
}

.desk-toolbar h1 {
  margin: 0;
  color: var(--theme-text);
  font-size: 28px;
  line-height: 1.2;
}

.desk-toolbar p {
  margin: 8px 0 0;
  color: var(--theme-text-secondary);
  font-size: 14px;
}

.toolbar-search {
  display: flex;
  gap: 10px;
}

.desk-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.desk-stats article {
  padding: 14px 16px;
  border: 1px solid var(--theme-border);
  border-radius: 12px;
  background: var(--theme-card);
  box-shadow: none;
}

.desk-stats strong,
.desk-stats span {
  display: block;
}

.desk-stats strong {
  color: var(--theme-primary);
  font-size: 24px;
  line-height: 1.2;
  font-weight: 700;
}

.desk-stats span {
  margin-top: 4px;
  color: var(--theme-text-secondary);
  font-size: 12px;
  font-weight: 500;
}

.quick-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 10px;
  overflow-x: auto;
}

.quick-row span {
  flex: 0 0 auto;
  color: var(--theme-text-secondary);
  font-size: 12px;
  font-weight: 600;
}

.quick-row button {
  flex: 0 0 auto;
  border: 0;
  border-radius: 999px;
  padding: 7px 12px;
  background: var(--theme-hover);
  color: var(--theme-text);
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
}

.tool-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  align-content: start;
  gap: 12px;
  flex: 1;
  margin-top: 12px;
  padding-right: 4px;
  overflow-y: auto;
}

.tool-grid.is-empty {
  display: block;
  padding: 40px 0;
  border-radius: 10px;
  background: var(--theme-card);
}

.tool-card {
  min-height: 96px;
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition: all var(--transition-normal);
  display: flex;
  align-items: flex-start;
  gap: 14px;
  border: 1px solid var(--theme-border);
  border-radius: 12px;
  background: var(--theme-card);
  box-shadow: none;
}
.tool-card:hover {
  transform: translateY(-3px);
  border-color: var(--theme-primary);
  box-shadow: 0 8px 24px rgba(79, 70, 229, 0.12);
}

.tool-icon {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  color: #fff;
  font-size: 22px;
}

.tool-copy {
  display: grid;
  min-width: 0;
  gap: 6px;
  flex: 1;
}

.tool-copy strong {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--theme-text);
  font-size: 15px;
  line-height: 1.25;
}

.tool-copy em {
  border-radius: 999px;
  padding: 2px 6px;
  background: var(--theme-primary-light);
  color: var(--theme-primary);
  font-size: 10px;
  font-style: normal;
  font-weight: 600;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.tool-copy small {
  display: -webkit-box;
  overflow: hidden;
  color: var(--theme-text-secondary);
  font-size: 12px;
  line-height: 1.45;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.tag-line {
  overflow: hidden;
  color: var(--theme-text-tertiary);
  font-size: 12px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 1180px) {
  .tool-desk {
    grid-template-columns: 1fr;
    grid-template-rows: auto minmax(0, 1fr);
  }

  .desk-sidebar {
    position: static;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .desk-brand {
    grid-column: 1 / -1;
  }

  .tool-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    overflow-y: auto;
  }
}

@media (max-width: 760px) {
  .desk-toolbar,
  .tool-grid {
    grid-template-columns: 1fr;
  }

  .desk-toolbar {
    gap: 12px;
    padding: 12px;
  }

  .desk-toolbar h1 {
    font-size: 24px;
  }

  .desk-toolbar p,
  .desk-stats,
  .quick-row {
    display: none;
  }

  .desk-sidebar {
    display: flex;
    overflow-x: auto;
  }

  .desk-brand,
  .nav-item {
    min-width: 138px;
  }

  .toolbar-search {
    flex-direction: column;
  }

  .tool-card {
    min-height: 82px;
  }
}
</style>
