<template>
  <div class="announcement-bar" v-if="!dismissed && announcements.length > 0">
    <div class="announcement-container">
      <el-icon class="announcement-icon"><Bell /></el-icon>
      <div class="announcement-scroll">
        <div class="announcement-content">
          <template v-for="(item, index) in announcements" :key="'a-' + item.id">
            <span class="announcement-item">
              <el-tag v-if="item.is_pinned" type="danger" size="small" class="pinned-tag">置顶</el-tag>
              {{ item.title }}
            </span>
            <span v-if="index < announcements.length - 1" class="separator">·</span>
          </template>
          <template v-for="(item, index) in announcements" :key="'b-' + item.id">
            <span class="announcement-item">
              <el-tag v-if="item.is_pinned" type="danger" size="small" class="pinned-tag">置顶</el-tag>
              {{ item.title }}
            </span>
            <span v-if="index < announcements.length - 1" class="separator">·</span>
          </template>
        </div>
      </div>
      <button class="dismiss-btn" @click="dismiss" title="关闭公告栏">
        <el-icon><Close /></el-icon>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { Bell, Close } from "@element-plus/icons-vue";
import { getAnnouncements } from "@/api/core";
import type { Announcement } from "@/types";

const announcements = ref<Announcement[]>([]);
const dismissed = ref(false);

const DISMISSED_KEY = "announcement_bar_dismissed";

const loadAnnouncements = async () => {
  // 检查是否在当天已关闭
  const saved = localStorage.getItem(DISMISSED_KEY);
  if (saved) {
    const dismissedDate = new Date(saved);
    const today = new Date();
    if (dismissedDate.toDateString() === today.toDateString()) {
      dismissed.value = true;
      return;
    }
  }

  try {
    const response = await getAnnouncements();
    announcements.value = (response.results || []).filter(
      (a: Announcement) => a.is_active && a.announcement_type === "bar"
    );
  } catch (error) {
    console.error("Failed to load announcements:", error);
  }
};

const dismiss = () => {
  dismissed.value = true;
  localStorage.setItem(DISMISSED_KEY, new Date().toISOString());
};

onMounted(() => {
  loadAnnouncements();
});
</script>

<style scoped>
.announcement-bar {
  background: var(--gradient-primary, linear-gradient(135deg, #4F46E5 0%, #6366F1 100%));
  padding: 0;
  color: white;
  font-size: 14px;
  position: relative;
  overflow: hidden;
}

.announcement-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 8px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.announcement-icon {
  font-size: 16px;
  flex-shrink: 0;
  opacity: 0.9;
}

.announcement-scroll {
  flex: 1;
  overflow: hidden;
  mask-image: linear-gradient(to right, transparent, black 24px, black calc(100% - 24px), transparent);
  -webkit-mask-image: linear-gradient(to right, transparent, black 24px, black calc(100% - 24px), transparent);
}

.announcement-content {
  display: flex;
  gap: 12px;
  align-items: center;
  animation: scroll var(--scroll-duration, 30s) linear infinite;
  white-space: nowrap;
  width: max-content;
}

.announcement-content:hover {
  animation-play-state: paused;
}

@keyframes scroll {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

.announcement-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  letter-spacing: -0.01em;
}

.separator {
  opacity: 0.5;
  font-size: 16px;
  user-select: none;
}

.pinned-tag {
  border: none;
  font-size: 11px;
}

.dismiss-btn {
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.15);
  border: none;
  color: white;
  width: 24px;
  height: 24px;
  border-radius: var(--radius-full, 9999px);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background var(--transition-fast, 0.12s);
  font-size: 14px;
}

.dismiss-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

@media (max-width: 768px) {
  .announcement-container {
    padding: 6px 12px;
    gap: 8px;
  }

  .announcement-icon {
    display: none;
  }

  .announcement-item {
    font-size: 13px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .announcement-content {
    animation: none;
    white-space: normal;
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
