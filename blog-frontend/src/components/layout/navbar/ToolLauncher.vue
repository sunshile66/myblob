<template>
  <el-popover
    v-model:visible="isOpen"
    placement="bottom-end"
    :width="380"
    trigger="click"
    popper-class="tool-launcher-popover"
  >
    <template #reference>
      <button class="tool-launcher-btn" type="button">
        <el-icon><Operation /></el-icon>
        <span>工具面板</span>
      </button>
    </template>

    <div class="tool-launcher">
      <div class="tool-launcher__head">
        <strong>快速打开工具</strong>
        <router-link to="/tools" @click="isOpen = false">全部工具</router-link>
      </div>
      <el-input
        v-model="toolQuery"
        placeholder="搜索 JSON、正则、图片、密码..."
        :prefix-icon="Search"
        clearable
        class="tool-launcher__search"
      />
      <div class="tool-launcher__list">
        <button
          v-for="tool in quickTools"
          :key="tool.slug"
          class="tool-launcher__item"
          type="button"
          @click="openTool(tool.slug)"
        >
          <span>{{ tool.name }}</span>
          <small>{{ tool.tags.slice(0, 2).join(" / ") }}</small>
        </button>
      </div>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { Operation, Search } from "@element-plus/icons-vue";
import { FEATURED_TOOLS, TOOL_CATALOG } from "@/data/toolCatalog";

const router = useRouter();
const isOpen = ref(false);
const toolQuery = ref("");

const quickTools = computed(() => {
  const keyword = toolQuery.value.trim().toLowerCase();
  const source = keyword ? TOOL_CATALOG : FEATURED_TOOLS;
  return source
    .filter((tool) => {
      if (!keyword) return true;
      return (
        tool.name.toLowerCase().includes(keyword) ||
        tool.description.toLowerCase().includes(keyword) ||
        tool.tags.some((tag) => tag.toLowerCase().includes(keyword))
      );
    })
    .slice(0, 8);
});

const openTool = (slug: string) => {
  isOpen.value = false;
  toolQuery.value = "";
  router.push(`/tools/${slug}`);
};
</script>

<style scoped>
.tool-launcher-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0 12px;
  border: 1px solid var(--theme-border);
  border-radius: 8px;
  background: var(--theme-card);
  color: var(--theme-text);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tool-launcher-btn:hover {
  border-color: var(--theme-primary);
  color: var(--theme-primary);
  background: var(--theme-primary-light);
}

.tool-launcher {
  display: grid;
  gap: 12px;
}

.tool-launcher__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.tool-launcher__head strong {
  color: var(--theme-text);
  font-size: 15px;
}

.tool-launcher__head a {
  color: var(--theme-primary);
  font-size: 13px;
  font-weight: 700;
  text-decoration: none;
}

.tool-launcher__list {
  display: grid;
  gap: 8px;
  max-height: 360px;
  overflow: auto;
}

.tool-launcher__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--theme-border);
  border-radius: 8px;
  background: var(--theme-background);
  text-align: left;
  cursor: pointer;
}

.tool-launcher__item:hover {
  border-color: var(--theme-primary);
  background: var(--theme-primary-light);
}

.tool-launcher__item span {
  color: var(--theme-text);
  font-size: 13px;
  font-weight: 700;
}

.tool-launcher__item small {
  color: var(--theme-text-secondary);
  white-space: nowrap;
}

@media (max-width: 768px) {
  .tool-launcher-btn span {
    display: none;
  }
}
</style>
