<template>
  <div class="search-box">
    <el-input
      v-model="searchQuery"
      placeholder="搜索笔记、标签或工具..."
      :prefix-icon="Search"
      clearable
      @keyup.enter="handleSearch"
      class="search-input"
    >
      <template #append>
        <el-button @click="handleSearch">搜索</el-button>
      </template>
    </el-input>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { Search } from "@element-plus/icons-vue";

const router = useRouter();
const searchQuery = ref("");

const handleSearch = () => {
  const keyword = searchQuery.value.trim();
  if (keyword) {
    router.push({ name: "Search", query: { q: keyword } });
    searchQuery.value = "";
  }
};
</script>

<style scoped>
.search-box {
  flex: 1;
  min-width: 220px;
  max-width: 520px;
}

.search-input {
  --el-input-bg-color: transparent;
  --el-input-border-color: transparent;
  --el-input-hover-border-color: transparent;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 24px 0 0 24px;
  padding: 8px 18px;
  background: rgba(248, 250, 252, 0.92);
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.search-input :deep(.el-input-group__append) {
  padding: 4px 6px 4px 0;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-left: none;
  border-radius: 0 24px 24px 0;
  background: rgba(248, 250, 252, 0.92);
  box-shadow: none;
}

.search-input :deep(.el-input-group__append .el-button) {
  margin: 0;
  border: none;
  border-radius: 18px;
  background: var(--gradient-primary);
  color: white;
  font-weight: 600;
}

@media (max-width: 768px) {
  .search-box {
    min-width: 0;
    max-width: 180px;
  }
}
</style>
