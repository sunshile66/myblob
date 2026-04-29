<template>
  <div class="sidebar">
    <el-card class="search-card">
      <el-input
        v-model="searchQuery"
        placeholder="搜索文章..."
        :prefix-icon="Search"
        @keyup.enter="handleSearch"
      />
    </el-card>

    <el-card class="quick-links-card">
      <template #header>
        <div class="card-header">
          <span>快速链接</span>
        </div>
      </template>
      <div class="quick-links">
        <router-link to="/photo-wall" class="quick-link">
          <el-icon><Picture /></el-icon>
          <span>图片墙</span>
        </router-link>
      </div>
    </el-card>

    <el-card class="category-card">
      <template #header>
        <div class="card-header">
          <span>分类</span>
        </div>
      </template>
      <el-menu :default-active="activeCategory" class="category-menu">
        <el-menu-item index="" @click="goToCategory('')"> 全部 </el-menu-item>
        <el-menu-item
          v-for="category in categories"
          v-if="category && category.id"
          :key="category.id"
          :index="category.slug || ''"
          @click="goToCategory(category.slug || '')"
        >
          {{ category.name }}
        </el-menu-item>
      </el-menu>
    </el-card>

    <el-card class="tag-card">
      <template #header>
        <div class="card-header">
          <span>标签云</span>
        </div>
      </template>
      <div class="tag-cloud">
        <el-tag
          v-for="tag in tags"
          v-if="tag && tag.id"
          :key="tag.id"
          @click="goToTag(tag.slug || '')"
          class="tag-item"
          effect="plain"
          style="cursor: pointer; margin: 4px"
        >
          {{ tag.name }}
        </el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { Search, Picture } from "@element-plus/icons-vue";
import { getCategories, getTags } from "@/api/post";
import type { Category, Tag } from "@/types";

const router = useRouter();
const route = useRoute();

const searchQuery = ref("");
const categories = ref<Category[]>([]);
const tags = ref<Tag[]>([]);

const activeCategory = ref((route.params.slug as string) || "");

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ name: "Search", query: { q: searchQuery.value } });
  }
};

const goToCategory = (slug: string) => {
  activeCategory.value = slug;
  if (slug) {
    router.push({ name: "Category", params: { slug } });
  } else {
    router.push({ name: "Home" });
  }
};

const goToTag = (slug: string) => {
  router.push({ name: "Tag", params: { slug } });
};

/**
 * 加载侧边栏数据（分类和标签）
 */
const loadData = async () => {
  try {
    const [catData, tagData] = await Promise.all([getCategories(), getTags()]);
    categories.value = (catData || []).filter(
      (cat): cat is Category => cat !== null && cat !== undefined
    );
    tags.value = (tagData || []).filter(
      (tag): tag is Tag => tag !== null && tag !== undefined
    );
  } catch (error) {
    console.error("Failed to load sidebar data:", error);
    categories.value = [];
    tags.value = [];
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.quick-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 10px;
  color: #475569;
  text-decoration: none;
  transition: all 0.25s ease;
  font-weight: 500;
}

.quick-link:hover {
  background: #dbeafe;
  color: #2563eb;
}

.category-menu {
  border: none;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item:hover {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  color: #fff !important;
}
</style>
