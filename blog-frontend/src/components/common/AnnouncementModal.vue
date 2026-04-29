<template>
  <el-dialog
    v-model="visible"
    :title="announcement?.title"
    width="500px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="canClose"
    class="announcement-modal"
  >
    <div class="announcement-content">
      <div class="content-text" v-html="formattedContent"></div>
      
      <div v-if="!canClose" class="countdown">
        <span>{{ countdown }}秒后可以关闭</span>
      </div>
    </div>

    <template #footer>
      <el-button 
        type="primary" 
        :disabled="!canClose"
        @click="handleClose"
      >
        我已阅读
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { getAnnouncements } from '@/api/core'
import type { Announcement } from '@/types'

const visible = ref(false)
const announcement = ref<Announcement | null>(null)
const countdown = ref(0)
let countdownTimer: number | null = null
let autoCloseTimer: number | null = null

const canClose = computed(() => countdown.value <= 0)
const formattedContent = computed(() => {
  return announcement?.content?.replace(/\n/g, '<br>') || ''
})

const loadAnnouncements = async () => {
  try {
    const response = await getAnnouncements()
    const modalAnnouncements = (response.results || []).filter(
      (a: Announcement) => a.is_active && a.announcement_type === 'modal'
    )
    
    if (modalAnnouncements.length > 0) {
      const dismissedIds = JSON.parse(localStorage.getItem('dismissed_announcements') || '[]')
      const activeModal = modalAnnouncements.find(
        (a: Announcement) => !dismissedIds.includes(a.id)
      )
      
      if (activeModal) {
        announcement.value = activeModal
        countdown.value = activeModal.show_delay
        visible.value = true
        
        startCountdown()
        
        if (activeModal.auto_close) {
          autoCloseTimer = window.setTimeout(() => {
            handleClose()
          }, activeModal.auto_close_time * 1000)
        }
      }
    }
  } catch (error) {
    console.error('Failed to load announcements:', error)
  }
}

const startCountdown = () => {
  if (countdown.value > 0) {
    countdownTimer = window.setInterval(() => {
      countdown.value--
      if (countdown.value <= 0 && countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }, 1000)
  }
}

const handleClose = () => {
  if (announcement.value) {
    const dismissedIds = JSON.parse(localStorage.getItem('dismissed_announcements') || '[]')
    if (!dismissedIds.includes(announcement.value.id)) {
      dismissedIds.push(announcement.value.id)
      localStorage.setItem('dismissed_announcements', JSON.stringify(dismissedIds))
    }
  }
  visible.value = false
  cleanup()
}

const cleanup = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  if (autoCloseTimer) {
    clearTimeout(autoCloseTimer)
    autoCloseTimer = null
  }
}

onMounted(() => {
  loadAnnouncements()
})

onUnmounted(() => {
  cleanup()
})
</script>

<style scoped>
.announcement-modal :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.announcement-modal :deep(.el-dialog__title) {
  color: white;
}

.announcement-modal :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
}

.announcement-content {
  padding: 16px 0;
}

.content-text {
  line-height: 1.8;
  color: var(--theme-text);
  font-size: 15px;
}

.countdown {
  margin-top: 20px;
  text-align: center;
  color: var(--theme-text-secondary);
  font-size: 14px;
}

.countdown span {
  display: inline-block;
  background: var(--theme-hover);
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 600;
}
</style>
