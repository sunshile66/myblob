<template>
  <DefaultLayout>
    <div class="photo-wall">
      <el-card class="wall-header">
        <div class="header-content">
          <h2 class="title">图片墙</h2>
          <p class="description">分享精彩瞬间</p>
        </div>
        <el-button type="primary" size="large" :icon="Plus">上传图片</el-button>
      </el-card>

      <div class="photo-grid">
        <div
          v-for="(photo, index) in photos"
          :key="index"
          class="photo-item"
          @click="viewPhoto(photo)"
        >
          <el-image
            :src="photo.url"
            fit="cover"
            :preview-src-list="photoUrls"
            :initial-index="index"
          >
            <template #error>
              <div class="image-slot">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="photo-overlay">
            <div class="photo-info">
              <span class="photo-title">{{ photo.title }}</span>
              <span class="photo-date">{{ photo.date }}</span>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="photos.length === 0" description="还没有图片" />
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { Plus, Picture } from "@element-plus/icons-vue";
import DefaultLayout from "@/layout/DefaultLayout.vue";

interface Photo {
  url: string;
  title: string;
  date: string;
}

const photos = ref<Photo[]>([
  {
    url: "https://picsum.photos/400/300?random=1",
    title: "美丽的风景",
    date: "2026-03-10",
  },
  {
    url: "https://picsum.photos/400/300?random=2",
    title: "城市夜景",
    date: "2026-03-09",
  },
  {
    url: "https://picsum.photos/400/300?random=3",
    title: "海边日落",
    date: "2026-03-08",
  },
  {
    url: "https://picsum.photos/400/300?random=4",
    title: "山间小径",
    date: "2026-03-07",
  },
  {
    url: "https://picsum.photos/400/300?random=5",
    title: "花开满园",
    date: "2026-03-06",
  },
  {
    url: "https://picsum.photos/400/300?random=6",
    title: "咖啡时光",
    date: "2026-03-05",
  },
]);

const photoUrls = computed(() => photos.value.map((p) => p.url));

const viewPhoto = (photo: Photo) => {
  console.log("Viewing photo:", photo);
};
</script>

<style scoped>
.photo-wall {
  max-width: 1200px;
  margin: 0 auto;
}

.wall-header {
  margin-bottom: 24px;
  border-radius: 16px;
}

.header-content {
  flex: 1;
}

.header-content .title {
  font-size: 28px;
  font-weight: 800;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.header-content .description {
  font-size: 16px;
  color: #64748b;
  margin: 0;
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.photo-item {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  aspect-ratio: 4/3;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.photo-item:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.photo-item :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f1f5f9;
  color: #94a3b8;
  font-size: 48px;
}

.photo-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8) 0%, transparent 100%);
  padding: 40px 20px 20px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.photo-item:hover .photo-overlay {
  opacity: 1;
}

.photo-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.photo-title {
  font-size: 18px;
  font-weight: 600;
  color: white;
}

.photo-date {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
