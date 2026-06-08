<template>
  <div class="nav-links">
    <router-link
      v-for="item in navLinks"
      :key="item.to"
      :to="item.to"
      class="nav-link-item"
      :class="{ active: isActive(item.to) }"
    >
      {{ item.label }}
    </router-link>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from "vue-router";

const route = useRoute();

const navLinks = [
  { label: "首页", to: "/" },
  { label: "新闻", to: "/news" },
  { label: "工具", to: "/tools" },
  { label: "文件", to: "/filemanager" },
  { label: "知识", to: "/knowledge" },
  { label: "API", to: "/apigateway" },
];

const isActive = (to: string) => {
  if (to === "/") {
    return route.path === "/";
  }
  return route.path.startsWith(to);
};
</script>

<style scoped>
.nav-links {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.nav-link-item {
  position: relative;
  padding: 8px 12px;
  border-radius: 999px;
  color: var(--theme-text);
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  transition: all var(--transition-fast);
}

.nav-link-item:hover,
.nav-link-item.active {
  color: var(--theme-primary);
  background: var(--theme-primary-light);
}

.nav-link-item::after {
  content: "";
  position: absolute;
  left: 12px;
  right: 12px;
  bottom: 4px;
  height: 2px;
  border-radius: 999px;
  background: var(--gradient-primary);
  transform: scaleX(0);
  transform-origin: center;
  transition: transform var(--transition-normal);
}

.nav-link-item:hover::after,
.nav-link-item.active::after {
  transform: scaleX(1);
}

@media (max-width: 960px) {
  .nav-links {
    display: none;
  }
}
</style>
