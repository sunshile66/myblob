<template>
  <div class="theme-switcher">
    <el-dropdown trigger="click" @command="handleThemeChange" popper-class="theme-dropdown">
      <div class="theme-trigger" :class="{ active: isOpen }">
        <el-icon :size="20">
          <Sunny v-if="!isDark" />
          <Moon v-else />
        </el-icon>
        <span class="theme-indicator" :style="{ background: currentThemeColor }"></span>
      </div>
      <template #dropdown>
        <div class="theme-panel">
          <div class="theme-panel-header">
            <span class="theme-panel-title">选择主题</span>
            <span class="theme-panel-subtitle">共 {{ availableThemes.length }} 个主题</span>
          </div>
          <div class="theme-grid">
            <button
              v-for="theme in availableThemes"
              :key="theme.id"
              class="theme-card"
              :class="{ active: currentTheme === theme.id }"
              @click.stop="handleThemeChange(theme.id)"
            >
              <div class="theme-preview">
                <div class="preview-header" :style="{ background: theme.colors.gradientPrimary }"></div>
                <div class="preview-content" :style="{ background: theme.colors.card }">
                  <div class="preview-title" :style="{ color: theme.colors.text }">标题</div>
                  <div class="preview-text" :style="{ color: theme.colors.textSecondary }">副标题文本</div>
                </div>
              </div>
              <div class="theme-info">
                <span class="theme-icon">{{ theme.icon }}</span>
                <span class="theme-name">{{ theme.name }}</span>
              </div>
            </button>
          </div>
          <div class="theme-panel-footer">
            <div class="theme-hint">
              <span class="hint-icon">💡</span>
              <span>系统主题会自动跟随操作系统设置</span>
            </div>
          </div>
        </div>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useThemeStore, type ThemeType } from '@/store/theme'
import { Sunny, Moon } from '@element-plus/icons-vue'

const themeStore = useThemeStore()
const isOpen = ref(false)

const currentTheme = computed(() => themeStore.currentTheme)
const isDark = computed(() => themeStore.isDark)
const availableThemes = computed(() => themeStore.availableThemes)

const currentThemeColor = computed(() => {
  const config = themeStore.currentThemeConfig
  return config?.colors.primary || '#ff2442'
})

const handleThemeChange = (theme: ThemeType) => {
  themeStore.setTheme(theme)
  isOpen.value = false
}
</script>

<style scoped>
.theme-switcher {
  .theme-trigger {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border-radius: 50%;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    color: var(--theme-text-secondary);
    background: var(--theme-card);
    border: 1px solid var(--theme-border);
    position: relative;

    &:hover {
      background: var(--theme-hover);
      transform: scale(1.05);
      box-shadow: var(--shadow-md);
    }

    &.active {
      background: var(--theme-primary-light);
      border-color: var(--theme-primary);
      color: var(--theme-primary);
    }
  }

  .theme-indicator {
    position: absolute;
    bottom: 2px;
    right: 2px;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    border: 2px solid var(--theme-card);
    transition: all 0.3s ease;
  }
}

.theme-dropdown {
  padding: 0;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  border: 1px solid var(--theme-border);
  background: var(--theme-card);
  min-width: 320px;
  max-width: 400px;
}

.theme-panel {
  padding: 16px;
}

.theme-panel-header {
  margin-bottom: 16px;

  .theme-panel-title {
    display: block;
    font-size: 16px;
    font-weight: 600;
    color: var(--theme-text);
    margin-bottom: 4px;
  }

  .theme-panel-subtitle {
    display: block;
    font-size: 12px;
    color: var(--theme-text-secondary);
  }
}

.theme-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.theme-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  border: 2px solid transparent;
  background: var(--theme-background);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
    border-color: var(--theme-primary-light);
  }

  &.active {
    border-color: var(--theme-primary);
    background: var(--theme-primary-light);
  }
}

.theme-preview {
  border-radius: var(--radius-sm);
  overflow: hidden;
  height: 60px;

  .preview-header {
    height: 20px;
  }

  .preview-content {
    height: 40px;
    padding: 6px 8px;

    .preview-title {
      font-size: 10px;
      font-weight: 600;
      margin-bottom: 2px;
    }

    .preview-text {
      font-size: 8px;
    }
  }
}

.theme-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;

  .theme-icon {
    font-size: 14px;
  }

  .theme-name {
    font-size: 12px;
    color: var(--theme-text);
    font-weight: 500;
  }
}

.theme-panel-footer {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--theme-border);
}

.theme-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--theme-text-secondary);

  .hint-icon {
    font-size: 14px;
  }
}
</style>
