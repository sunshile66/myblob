<template>
  <div class="navbar">
    <div class="navbar-container">
      <div class="navbar-left">
        <router-link to="/" class="logo">
          <span class="logo-badge">M</span>
          <div class="logo-copy">
            <span class="logo-text">MyBlob</span>
            <span class="logo-subtitle">创作与效率工作台</span>
          </div>
        </router-link>

        <div class="nav-links">
          <router-link
            v-for="item in navLinks"
            :key="item.to"
            :to="item.to"
            class="nav-link-item"
            :class="{ active: isNavActive(item.to) }"
          >
            {{ item.label }}
          </router-link>
        </div>

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
      </div>

      <div class="navbar-right">
        <ThemeSwitcher />

        <template v-if="userStore.isLoggedIn">
          <el-popover
            placement="bottom"
            :width="360"
            trigger="click"
            popper-class="notification-popover"
          >
            <template #reference>
              <div class="notification-btn">
                <el-badge :value="3" class="notification-badge">
                  <el-icon class="notification-icon"><Bell /></el-icon>
                </el-badge>
              </div>
            </template>
            <NotificationCenter />
          </el-popover>

          <router-link to="/editor" class="publish-btn">
            <el-icon><Plus /></el-icon>
            <span>发布</span>
          </router-link>
        </template>

        <template v-if="!userStore.isLoggedIn">
          <router-link to="/login" class="nav-link">登录</router-link>
          <router-link to="/register" class="nav-link register-link">
            注册
          </router-link>
        </template>

        <template v-else>
          <div class="user-menu">
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-info">
                <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                  {{
                    userStore.userInfo?.nickname?.charAt(0) ||
                    userStore.userInfo?.username?.charAt(0) ||
                    "U"
                  }}
                </el-avatar>
                <span class="user-name">
                  {{
                    userStore.userInfo?.nickname || userStore.userInfo?.username
                  }}
                </span>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </div>

              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="my-posts">
                    <el-icon><Document /></el-icon>
                    我的笔记
                  </el-dropdown-item>
                  <el-dropdown-item command="favorites">
                    <el-icon><Star /></el-icon>
                    我的收藏
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  ArrowDown,
  Bell,
  Document,
  Plus,
  Search,
  Star,
  SwitchButton,
  User,
} from "@element-plus/icons-vue";
import ThemeSwitcher from "@/components/common/ThemeSwitcher.vue";
import NotificationCenter from "@/components/common/NotificationCenter.vue";
import { useUserStore } from "@/store/user";

const userStore = useUserStore();
const router = useRouter();
const route = useRoute();
const searchQuery = ref("");

const navLinks = [
  { label: "首页", to: "/" },
  { label: "工具", to: "/tools" },
  { label: "文件", to: "/filemanager" },
  { label: "API", to: "/apigateway" },
];

const handleSearch = () => {
  const keyword = searchQuery.value.trim();
  if (keyword) {
    router.push({ name: "Search", query: { q: keyword } });
    searchQuery.value = "";
  }
};

const isNavActive = (to: string) => {
  if (to === "/") {
    return route.path === "/";
  }
  return route.path.startsWith(to);
};

const handleCommand = async (command: string) => {
  switch (command) {
    case "profile":
      router.push("/profile");
      break;
    case "my-posts":
      router.push("/my-posts");
      break;
    case "favorites":
      router.push("/favorites");
      break;
    case "logout":
      try {
        await ElMessageBox.confirm("确定要退出登录吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
        userStore.logout();
        ElMessage.success("已退出登录");
        router.push("/");
      } catch {
        // 用户取消
      }
      break;
  }
};
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
  background: rgba(255, 255, 255, 0.74);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
}

.navbar-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 28px;
  flex: 1;
  min-width: 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  flex-shrink: 0;
}

.logo-badge {
  width: 42px;
  height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: linear-gradient(135deg, #0f172a 0%, #1d4ed8 100%);
  color: white;
  font-size: 18px;
  font-weight: 800;
  box-shadow: 0 16px 28px rgba(29, 78, 216, 0.18);
}

.logo-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.logo-text {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.logo-subtitle {
  font-size: 11px;
  color: #64748b;
  letter-spacing: 0.02em;
}

.nav-links {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.nav-link-item {
  position: relative;
  padding: 8px 12px;
  border-radius: 999px;
  color: #334155;
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

.navbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.notification-btn {
  cursor: pointer;
  padding: 10px;
  border-radius: 50%;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
}

.notification-btn:hover {
  background: var(--theme-primary-light);
  transform: scale(1.08);
}

.notification-icon {
  font-size: 22px;
  color: var(--theme-text);
}

.notification-btn:hover .notification-icon {
  color: var(--theme-primary);
}

.notification-badge :deep(.el-badge__content) {
  background: var(--theme-primary);
  border: 2px solid rgba(255, 255, 255, 0.84);
}

.publish-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: var(--gradient-primary);
  color: white;
  text-decoration: none;
  border-radius: 999px;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 14px 24px rgba(255, 36, 66, 0.2);
  transition: all var(--transition-normal);
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 28px rgba(255, 36, 66, 0.28);
}

.nav-link {
  color: var(--theme-text);
  text-decoration: none;
  font-weight: 600;
  font-size: 14px;
  transition: all var(--transition-fast);
  padding: 8px 14px;
  border-radius: 999px;
}

.nav-link:hover {
  color: var(--theme-primary);
  background: var(--theme-primary-light);
}

.register-link {
  color: white;
  background: linear-gradient(135deg, #0f172a 0%, #1d4ed8 100%);
}

.register-link:hover {
  color: white;
  background: linear-gradient(135deg, #111827 0%, #2563eb 100%);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 14px 6px 8px;
  border-radius: 999px;
  transition: all var(--transition-fast);
  border: 1px solid transparent;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.78);
  border-color: rgba(148, 163, 184, 0.18);
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.06);
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--theme-text);
}

.dropdown-icon {
  color: var(--theme-text-secondary);
  font-size: 14px;
}

@media (max-width: 960px) {
  .navbar-left {
    gap: 14px;
  }

  .nav-links {
    display: none;
  }
}

@media (max-width: 768px) {
  .navbar-container {
    padding: 10px 12px;
  }

  .search-box {
    min-width: 0;
    max-width: 180px;
  }

  .logo-text {
    font-size: 18px;
  }

  .logo-subtitle,
  .publish-btn span,
  .user-name {
    display: none;
  }
}
</style>
