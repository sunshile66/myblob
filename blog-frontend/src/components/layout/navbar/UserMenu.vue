<template>
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
            <el-dropdown-item v-if="userStore.userInfo?.role === 'ADMIN' || userStore.userInfo?.is_superuser" command="admin-news">
              <el-icon><Reading /></el-icon>
              新闻管理
            </el-dropdown-item>
            <el-dropdown-item v-if="userStore.userInfo?.role === 'ADMIN' || userStore.userInfo?.is_superuser" divided command="admin-users">
              <el-icon><Setting /></el-icon>
              用户管理
            </el-dropdown-item>
            <el-dropdown-item v-if="userStore.userInfo?.role === 'ADMIN' || userStore.userInfo?.is_superuser" command="admin-ip-blocks">
              <el-icon><Lock /></el-icon>
              IP 黑名单
            </el-dropdown-item>
            <el-dropdown-item v-if="userStore.userInfo?.role === 'ADMIN' || userStore.userInfo?.is_superuser" command="admin-request-logs">
              <el-icon><Monitor /></el-icon>
              请求日志
            </el-dropdown-item>
            <el-dropdown-item v-if="userStore.userInfo?.role === 'ADMIN' || userStore.userInfo?.is_superuser" command="admin-site-config">
              <el-icon><Tools /></el-icon>
              站点配置
            </el-dropdown-item>
            <el-dropdown-item command="api-keys">
              <el-icon><Key /></el-icon>
              API 密钥
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
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  ArrowDown,
  Setting,
  Lock,
  Monitor,
  Tools,
  Key,
  Bell,
  Document,
  Plus,
  Reading,
  Star,
  SwitchButton,
  User,
} from "@element-plus/icons-vue";
import NotificationCenter from "@/components/common/NotificationCenter.vue";
import { useUserStore } from "@/store/user";

const userStore = useUserStore();
const router = useRouter();

const handleCommand = async (command: string) => {
  switch (command) {
    case "profile":
      router.push("/profile");
      break;
    case "my-posts":
      router.push("/my-posts");
      break;
    case "admin-news":
      router.push("/admin/news");
      break;
    case "admin-users":
      router.push("/admin/users");
      break;
    case "admin-ip-blocks":
      router.push("/admin/ip-blocks");
      break;
    case "admin-request-logs":
      router.push("/admin/request-logs");
      break;
    case "admin-site-config":
      router.push("/admin/site-config");
      break;
    case "api-keys":
      router.push("/api-keys");
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
  box-shadow: 0 8px 16px rgba(79, 70, 229, 0.22);
  transition: all var(--transition-normal);
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 22px rgba(79, 70, 229, 0.3);
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
  background: var(--gradient-primary);
  box-shadow: 0 8px 16px rgba(79, 70, 229, 0.22);
}

.register-link:hover {
  color: white;
  background: var(--gradient-primary);
  filter: brightness(1.05);
  box-shadow: 0 12px 22px rgba(79, 70, 229, 0.32);
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

@media (max-width: 768px) {
  .publish-btn span,
  .user-name {
    display: none;
  }
}
</style>
