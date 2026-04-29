<template>
  <div class="announcement-bar" v-if="announcements.length > 0">
    <div class="announcement-container">
      <el-icon class="announcement-icon"><Bell /></el-icon>
      <div class="announcement-scroll">
        <div class="announcement-content">
          <span
            v-for="(item, index) in announcements"
            :key="item.id"
            class="announcement-item"
          >
            <el-tag
              v-if="item.is_pinned"
              type="danger"
              size="small"
              class="pinned-tag"
              >置顶</el-tag
            >
            {{ item.title }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { Bell } from "@element-plus/icons-vue";
import { getAnnouncements } from "@/api/core";
import type { Announcement } from "@/types";

const announcements = ref<Announcement[]>([]);

const loadAnnouncements = async () => {
  try {
    const response = await getAnnouncements();
    announcements.value = (response.results || []).filter(
      (a: Announcement) => a.is_active && a.announcement_type === "bar"
    );
  } catch (error) {
    console.error("Failed to load announcements:", error);
  }
};

onMounted(() => {
  loadAnnouncements();
});
</script>

<style scoped>
.announcement-bar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 8px 0;
  color: white;
  font-size: 14px;
}

.announcement-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.announcement-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.announcement-scroll {
  flex: 1;
  overflow: hidden;
}

.announcement-content {
  display: flex;
  gap: 40px;
  animation: scroll 30s linear infinite;
  white-space: nowrap;
}

.announcement-content:hover {
  animation-play-state: paused;
}

@keyframes scroll {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50%);
  }
}

.announcement-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.pinned-tag {
  border: none;
}
</style>
