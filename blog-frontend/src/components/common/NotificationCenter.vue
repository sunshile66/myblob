<template>
  <div class="notification-center">
    <div class="notification-header">
      <h3>通知</h3>
      <el-button
        v-if="isLoggedIn"
        type="primary"
        link
        size="small"
        @click="markAllAsRead"
        >全部已读</el-button
      >
    </div>
    <div v-if="!isLoggedIn" class="login-prompt">
      <el-icon class="empty-icon"><Lock /></el-icon>
      <span>请登录后查看通知</span>
    </div>
    <div v-else-if="loading" class="notification-list">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else class="notification-list">
      <div
        v-for="item in notifications"
        :key="item.id"
        class="notification-item"
        :class="{ unread: !item.is_read }"
        @click="markAsRead(item)"
      >
        <div class="notification-content">
          <div class="notification-text">
            <span>{{ item.content }}</span>
          </div>
          <div class="notification-time">{{ formatTime(item.created_at) }}</div>
        </div>
      </div>
    </div>
    <div
      v-if="isLoggedIn && !loading && notifications.length === 0"
      class="empty-state"
    >
      <el-icon class="empty-icon"><Bell /></el-icon>
      <span>暂无通知</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import { ElMessage } from "element-plus";
import { Bell, Lock } from "@element-plus/icons-vue";
import {
  getNotifications,
  markNotificationAsRead,
  markAllNotificationsAsRead,
} from "@/api/notification";
import type { Notification } from "@/types";
import { useUserStore } from "@/store/user";

const userStore = useUserStore();
const notifications = ref<Notification[]>([]);
const loading = ref(false);

const isLoggedIn = computed(() => userStore.isLoggedIn);

const loadNotifications = async () => {
  if (!isLoggedIn.value) {
    notifications.value = [];
    return;
  }
  loading.value = true;
  try {
    const response = await getNotifications();
    notifications.value = response.results || [];
  } catch (error: any) {
    console.error("Failed to load notifications:", error);
    if (error.response?.status !== 401) {
      ElMessage.error("加载通知失败");
    }
  } finally {
    loading.value = false;
  }
};

watch(() => userStore.isLoggedIn, () => {
  loadNotifications();
});

const markAllAsRead = async () => {
  try {
    await markAllNotificationsAsRead();
    notifications.value.forEach((item) => {
      item.is_read = true;
    });
    ElMessage.success("已全部标记为已读");
  } catch (error) {
    console.error("Failed to mark all as read:", error);
    ElMessage.error("操作失败，请稍后重试");
  }
};

const markAsRead = async (item: Notification) => {
  if (item.is_read) return;
  try {
    await markNotificationAsRead(item.id);
    item.is_read = true;
  } catch (error) {
    console.error("Failed to mark as read:", error);
  }
};

const formatTime = (timeStr: string) => {
  const date = new Date(timeStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return "刚刚";
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  return date.toLocaleDateString();
};

onMounted(() => {
  loadNotifications();
});
</script>

<style scoped>
.notification-center {
  width: 360px;
  max-height: 480px;
  overflow-y: auto;
  background: var(--theme-card);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--theme-border);
  position: sticky;
  top: 0;
  background: var(--theme-card);
  z-index: 10;
}

.notification-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--theme-text);
}

.login-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  color: var(--theme-text-secondary);
}

.notification-list {
  padding: 8px 0;
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s ease;
  border-left: 3px solid transparent;
}

.notification-item:hover {
  background: var(--theme-hover);
}

.notification-item.unread {
  background: rgba(255, 36, 66, 0.05);
  border-left-color: var(--theme-primary);
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-text {
  font-size: 14px;
  color: var(--theme-text);
  line-height: 1.5;
}

.username {
  font-weight: 600;
}

.notification-time {
  font-size: 12px;
  color: var(--theme-text-secondary);
  margin-top: 4px;
}

.notification-image {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  color: var(--theme-text-secondary);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}
</style>
