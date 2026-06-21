<template>
  <el-dialog
    v-model="visible"
    :title="announcement?.title"
    width="520px"
    :close-on-click-modal="canClose"
    :close-on-press-escape="canClose"
    :show-close="canClose"
    class="announcement-modal"
    destroy-on-close
  >
    <div class="announcement-body">
      <div class="content-text">{{ announcement?.content || '' }}</div>

      <div v-if="!canClose" class="countdown-bar">
        <div class="countdown-progress">
          <div class="countdown-fill" :style="{ width: countdownPercent + '%' }"></div>
        </div>
        <span class="countdown-text">{{ countdown }}秒后可关闭</span>
      </div>
    </div>

    <template #footer>
      <el-button
        type="primary"
        :disabled="!canClose"
        @click="handleClose"
        round
      >
        我已阅读
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getAnnouncements } from '@/api/core'
import type { Announcement } from '@/types'

const visible = ref(false)
const announcement = ref<Announcement | null>(null)
const countdown = ref(0)
const initialCountdown = ref(5)
let countdownTimer: number | null = null
let autoCloseTimer: number | null = null

const canClose = computed(() => countdown.value <= 0)
const countdownPercent = computed(() =>
  initialCountdown.value > 0 ? Math.round((countdown.value / initialCountdown.value) * 100) : 0
)

const DISMISSED_KEY = 'dismissed_announcements'
const MAX_DISMISSED = 50

const loadAnnouncements = async () => {
  try {
    const response = await getAnnouncements()
    const modalAnnouncements = (response.results || []).filter(
      (a: Announcement) => a.is_active && a.announcement_type === 'modal'
    )

    if (modalAnnouncements.length > 0) {
      const dismissedIds = getDismissedIds()
      const activeModal = modalAnnouncements.find(
        (a: Announcement) => !dismissedIds.includes(a.id)
      )

      if (activeModal) {
        announcement.value = activeModal
        const delay = activeModal.show_delay || 5
        initialCountdown.value = delay
        countdown.value = delay
        visible.value = true

        startCountdown()

        if (activeModal.auto_close && activeModal.auto_close_time) {
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

const getDismissedIds = (): number[] => {
  try {
    return JSON.parse(localStorage.getItem(DISMISSED_KEY) || '[]')
  } catch {
    return []
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
    const dismissedIds = getDismissedIds()
    if (!dismissedIds.includes(announcement.value.id)) {
      dismissedIds.push(announcement.value.id)
      // 只保留最近 50 条，防止 localStorage 膨胀
      const trimmed = dismissedIds.slice(-MAX_DISMISSED)
      localStorage.setItem(DISMISSED_KEY, JSON.stringify(trimmed))
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
  background: var(--gradient-primary, linear-gradient(135deg, #4F46E5 0%, #6366F1 100%));
  padding: 16px 24px;
  margin: 0;
}

.announcement-modal :deep(.el-dialog__title) {
  color: white;
  font-weight: 600;
  font-size: 16px;
}

.announcement-modal :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: rgba(255, 255, 255, 0.8);
}

.announcement-modal :deep(.el-dialog__headerbtn .el-dialog__close:hover) {
  color: white;
}

.announcement-body {
  padding: 20px 0 8px;
}

.content-text {
  line-height: 1.8;
  color: var(--theme-text);
  font-size: 15px;
  white-space: pre-line;
}

.countdown-bar {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.countdown-progress {
  width: 100%;
  height: 3px;
  background: var(--theme-border);
  border-radius: 2px;
  overflow: hidden;
}

.countdown-fill {
  height: 100%;
  background: var(--theme-primary);
  border-radius: 2px;
  transition: width 1s linear;
}

.countdown-text {
  font-size: 13px;
  color: var(--theme-text-tertiary);
  font-weight: 500;
}

.announcement-modal :deep(.el-dialog__footer) {
  padding: 12px 24px 20px;
}

@media (prefers-reduced-motion: reduce) {
  .countdown-fill {
    transition: none;
  }
}
</style>
