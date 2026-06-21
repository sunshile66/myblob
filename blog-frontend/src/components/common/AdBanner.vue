<template>
  <div :class="['ad-banner', `ad-banner--${position}`]" v-if="loading || ads.length > 0">
    <!-- 骨架屏 -->
    <div v-if="loading" class="ad-skeleton">
      <div class="skeleton-item" v-for="i in 2" :key="i">
        <div class="skeleton-image"></div>
      </div>
    </div>

    <!-- 广告列表 -->
    <div v-else class="ad-list">
      <div
        v-for="ad in ads"
        :key="ad.id"
        class="ad-item"
        @click="handleAdClick(ad)"
        role="link"
        :aria-label="ad.title"
      >
        <el-image
          v-if="ad.image"
          :src="ad.image"
          fit="cover"
          class="ad-image"
          loading="lazy"
        >
          <template #error>
            <div class="ad-image-fallback">
              <span>{{ ad.title }}</span>
            </div>
          </template>
        </el-image>
        <div v-else class="ad-text">
          <span class="ad-text-title">{{ ad.title }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { getAds, recordAdClick } from '@/api/core'
import type { Ad } from '@/types'

interface Props {
  position?: string
}

const props = withDefaults(defineProps<Props>(), {
  position: 'sidebar'
})

const ads = ref<Ad[]>([])
const loading = ref(true)

const loadAds = async () => {
  loading.value = true
  try {
    const response = await getAds(props.position)
    ads.value = response.results || []
  } catch (error) {
    console.error('Failed to load ads:', error)
  } finally {
    loading.value = false
  }
}

const handleAdClick = async (ad: Ad) => {
  try {
    await recordAdClick(ad.id)
  } catch (error) {
    console.error('Failed to record ad click:', error)
  }
  if (ad.link) {
    window.open(ad.link, '_blank')
  }
}

watch(() => props.position, () => {
  loadAds()
})

onMounted(() => {
  loadAds()
})
</script>

<style scoped>
.ad-banner {
  width: 100%;
}

/* 侧边栏样式（垂直堆叠） */
.ad-banner--sidebar .ad-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ad-banner--sidebar .ad-item {
  border-radius: var(--radius-lg, 12px);
  overflow: hidden;
  cursor: pointer;
  transition: transform var(--transition-fast, 0.12s), box-shadow var(--transition-fast, 0.12s);
  border: 1px solid var(--theme-border);
}

.ad-banner--sidebar .ad-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

/* 横幅样式（水平排列） */
.ad-banner--banner .ad-list {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
  padding: 4px 0;
}

.ad-banner--banner .ad-list::-webkit-scrollbar {
  display: none;
}

.ad-banner--banner .ad-item {
  flex-shrink: 0;
  width: 100%;
  border-radius: var(--radius-lg, 12px);
  overflow: hidden;
  cursor: pointer;
  transition: transform var(--transition-fast, 0.12s), box-shadow var(--transition-fast, 0.12s);
  border: 1px solid var(--theme-border);
}

.ad-banner--banner .ad-item:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

/* 通用图片 */
.ad-image {
  width: 100%;
  display: block;
  aspect-ratio: 16 / 9;
}

.ad-image-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--theme-muted);
  color: var(--theme-text-secondary);
  font-size: 14px;
  padding: 20px;
  text-align: center;
}

.ad-text {
  background: var(--gradient-primary, linear-gradient(135deg, #4F46E5 0%, #6366F1 100%));
  color: white;
  padding: 20px 24px;
  text-align: center;
}

.ad-text-title {
  font-size: 15px;
  font-weight: 600;
  letter-spacing: -0.01em;
}

/* 骨架屏 */
.ad-skeleton {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-item {
  border-radius: var(--radius-lg, 12px);
  overflow: hidden;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
}

.skeleton-image {
  width: 100%;
  height: 120px;
  background: linear-gradient(
    90deg,
    var(--theme-muted) 0%,
    var(--theme-hover) 50%,
    var(--theme-muted) 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.8s ease-in-out infinite;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

@media (prefers-reduced-motion: reduce) {
  .skeleton-image {
    animation: none;
  }

  .ad-item:hover {
    transform: none;
  }
}

@media (max-width: 768px) {
  .ad-banner--banner .ad-item {
    min-width: 280px;
  }
}
</style>
