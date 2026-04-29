<template>
  <div class="ad-banner" v-if="ads.length > 0">
    <div v-for="ad in ads" :key="ad.id" class="ad-item" @click="handleAdClick(ad)">
      <el-image v-if="ad.image" :src="ad.image" fit="cover" class="ad-image" />
      <div v-else class="ad-text">
        <span>{{ ad.title }}</span>
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

const loadAds = async () => {
  try {
    const response = await getAds(props.position)
    ads.value = response.results || []
  } catch (error) {
    console.error('Failed to load ads:', error)
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

.ad-item {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  margin-bottom: 16px;
}

.ad-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.ad-image {
  width: 100%;
  display: block;
}

.ad-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 24px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
}
</style>
