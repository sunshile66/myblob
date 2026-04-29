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
        <router-link to="/" class="quick-link">
          <el-icon><Picture /></el-icon>
          <span>首页</span>
        </router-link>
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
      <el-menu class="category-menu">
        <el-menu-item index=""> 全部 </el-menu-item>
        <el-menu-item
          v-for="category in categories"
          :key="category.id"
          :index="category.slug || ''"
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
          :key="tag.id"
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
import { ref } from "vue";
import { useRouter } from "vue-router";
import { Search, Picture } from "@element-plus/icons-vue";
import type { Category, Tag } from "@/types";

const router = useRouter();

const searchQuery = ref("");
const categories = ref<Category[]>([
  { id: 1, name: '分享', slug: 'share', is_active: true, sort: 0 },
  { id: 2, name: '教程', slug: 'tutorial', is_active: true, sort: 1 },
  { id: 3, name: '生活', slug: 'life', is_active: true, sort: 2 }
])
const tags = ref<Tag[]>([
  { id: 1, name: 'Vue', slug: 'vue' },
  { id: 2, name: 'TypeScript', slug: 'typescript' },
  { id: 3, name: '生活', slug: 'life' },
  { id: 4, name: '美食', slug: 'food' },
  { id: 5, name: '旅行', slug: 'travel' },
  { id: 6, name: '摄影', slug: 'photography' }
])

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    console.log('搜索:', searchQuery.value)
  }
};
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
