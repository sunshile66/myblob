<template>
  <div class="navbar">
    <div class="navbar-container">
      <div class="navbar-left">
        <router-link to="/" class="logo">
          <span class="logo-icon"><el-icon><Notebook /></el-icon></span>
          <span class="logo-text">小红书</span>
        </router-link>
      </div>
      <div class="navbar-center">
        <div class="nav-links">
          <router-link to="/knowledge" class="top-nav-link">知识百科</router-link>
          <router-link to="/news" class="top-nav-link">新闻</router-link>
          <router-link to="/tools" class="top-nav-link">工具</router-link>
        </div>
        <div class="search-box">
          <el-input
            v-model="searchQuery"
            placeholder="搜索你感兴趣的内容..."
            :prefix-icon="Search"
            @keyup.enter="handleSearch"
          />
        </div>
      </div>
      <div class="navbar-right">
        <template v-if="userStore.isLoggedIn">
          <router-link to="/write" class="publish-btn">
            <el-icon><Plus /></el-icon>
            <span>发布</span>
          </router-link>
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" :src="userStore.userInfo?.avatar">
                {{
                  userStore.userInfo?.nickname?.charAt(0) ||
                  userStore.userInfo?.username?.charAt(0)
                }}
              </el-avatar>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  我的主页
                </el-dropdown-item>
                <el-dropdown-item command="write">
                  <el-icon><Edit /></el-icon>
                  发布笔记
                </el-dropdown-item>
                <el-dropdown-item command="my-posts">
                  <el-icon><Document /></el-icon>
                  我的笔记
                </el-dropdown-item>
                <el-dropdown-item command="favorites">
                  <el-icon><Collection /></el-icon>
                  收藏
                </el-dropdown-item>
                <el-dropdown-item command="membership" divided>
                  <el-icon><GoldMedal /></el-icon>
                  会员中心
                </el-dropdown-item>
                <el-dropdown-item command="orders">
                  <el-icon><Tickets /></el-icon>
                  我的订单
                </el-dropdown-item>
                <el-dropdown-item command="wallet">
                  <el-icon><Wallet /></el-icon>
                  我的钱包
                </el-dropdown-item>
                <el-dropdown-item command="edit-profile" divided>
                  <el-icon><Setting /></el-icon>
                  编辑资料
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-link">登录</router-link>
          <router-link to="/register" class="nav-link register-link"
            >注册</router-link
          >
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/store/user";
import {
  Search,
  User,
  Edit,
  Document,
  Collection,
  Setting,
  SwitchButton,
  Plus,
  GoldMedal,
  Tickets,
  Wallet,
  Notebook,
} from "@element-plus/icons-vue";
import { ElMessageBox } from "element-plus";

const router = useRouter();
const userStore = useUserStore();
const searchQuery = ref("");

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ name: "Search", query: { q: searchQuery.value } });
  }
};

const handleCommand = async (command: string) => {
  switch (command) {
    case "profile":
      router.push("/profile");
      break;
    case "write":
      router.push("/write");
      break;
    case "my-posts":
      router.push("/my-posts");
      break;
    case "favorites":
      router.push("/favorites");
      break;
    case "membership":
      router.push("/membership");
      break;
    case "orders":
      router.push("/orders");
      break;
    case "wallet":
      router.push("/wallet");
      break;
    case "edit-profile":
      router.push("/profile/edit");
      break;
    case "logout":
      try {
        await ElMessageBox.confirm("确定要退出登录吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
        userStore.logout();
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
  background: var(--theme-card);
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid var(--theme-border);
}

.navbar-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 14px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.navbar-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  transition: opacity 0.2s ease;
}

.logo:hover {
  opacity: 0.8;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 22px;
  font-weight: 800;
  color: var(--theme-primary);
  letter-spacing: -0.02em;
}

.navbar-center {
  flex: 1;
  max-width: 520px;
  margin: 0 48px;
  display: flex;
  align-items: center;
  gap: 28px;
}

.nav-links {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.top-nav-link {
  color: var(--theme-text-secondary);
  text-decoration: none;
  font-weight: 500;
  font-size: 14px;
  padding: 7px 16px;
  border-radius: 8px;
  transition: color 0.2s cubic-bezier(0.4, 0, 0.2, 1), background 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  letter-spacing: -0.01em;
}

.top-nav-link:hover {
  color: var(--theme-primary);
  background: var(--theme-primary-light);
}

.top-nav-link.router-link-active {
  color: var(--theme-primary);
  background: var(--theme-primary-light);
  font-weight: 600;
}

.search-box {
  width: 100%;
  min-width: 0;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.nav-link {
  color: var(--theme-text-secondary);
  text-decoration: none;
  font-weight: 500;
  font-size: 14px;
  transition: color var(--transition-fast);
  letter-spacing: -0.01em;
}

.nav-link:hover {
  color: var(--theme-primary);
}

.register-link {
  color: var(--theme-primary);
  font-weight: 600;
}

.publish-btn {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 9px 22px;
  background: var(--theme-primary);
  color: #fff;
  border-radius: 10px;
  text-decoration: none;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: -0.01em;
  box-shadow: 0 1px 3px rgba(79, 70, 229, 0.2);
}

.publish-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
  background: color-mix(in srgb, var(--theme-primary) 90%, black);
}

.user-info {
  cursor: pointer;
  transition: transform var(--transition-fast);
}

.user-info:hover {
  transform: scale(1.05);
}

@media (max-width: 768px) {
  .navbar-container {
    padding: 12px 16px;
  }
  .navbar-center {
    margin: 0 16px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .user-info,
  .user-info:hover,
  .logo,
  .publish-btn {
    transition: none;
    transform: none;
  }
}
</style>
