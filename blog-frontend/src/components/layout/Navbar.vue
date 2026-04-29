<template>
  <div class="navbar">
    <div class="navbar-container">
      <div class="navbar-left">
        <router-link to="/" class="logo">
          <span class="logo-icon">📔</span>
          <span class="logo-text">小红书</span>
        </router-link>
      </div>
      <div class="navbar-center">
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
        <!-- <ThemeSwitcher /> -->
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
                  <el-icon><Crown /></el-icon>
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
  Crown,
  Tickets,
  Wallet,
} from "@element-plus/icons-vue";
import { ElMessageBox } from "element-plus";
// import ThemeSwitcher from "./ThemeSwitcher.vue";

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
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.navbar-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 12px 20px;
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
  gap: 6px;
  text-decoration: none;
}

.logo-icon {
  font-size: 26px;
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #ff2442 0%, #ff6a7f 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.navbar-center {
  flex: 1;
  max-width: 500px;
  margin: 0 40px;
}

.search-box {
  width: 100%;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-link {
  color: #333;
  text-decoration: none;
  font-weight: 500;
  font-size: 15px;
  transition: color 0.2s ease;
}

.nav-link:hover {
  color: #ff2442;
}

.register-link {
  color: #ff2442;
}

.publish-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  background: linear-gradient(135deg, #ff2442 0%, #ff6a7f 100%);
  color: white;
  border-radius: 24px;
  text-decoration: none;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.2s ease;
}

.publish-btn:hover {
  transform: scale(1.03);
  box-shadow: 0 4px 14px rgba(255, 36, 66, 0.35);
}

.user-info {
  cursor: pointer;
  transition: transform 0.2s ease;
}

.user-info:hover {
  transform: scale(1.05);
}
</style>
