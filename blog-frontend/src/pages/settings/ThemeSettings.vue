<template>
  <div class="theme-settings-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-title">
          <el-icon class="title-icon"><Brush /></el-icon>
          <h1>主题设置</h1>
        </div>
        <p class="header-subtitle">自定义您的界面外观，打造专属的视觉体验</p>
      </div>
    </div>

    <div class="page-content">
      <div class="content-row">
        <div class="main-panel">
          <div class="panel-section">
            <div class="section-header">
              <h2>选择主题</h2>
              <p>选择一个适合您的主题风格</p>
            </div>
            
            <div class="theme-selector">
              <div
                v-for="theme in availableThemes"
                :key="theme.id"
                class="theme-option"
                :class="{ active: currentTheme === theme.id }"
                @click="setTheme(theme.id)"
              >
                <div class="theme-visual">
                  <div class="color-bar" :style="{ background: theme.colors.gradientPrimary }"></div>
                  <div class="mini-preview">
                    <div class="preview-nav" :style="{ background: theme.colors.card }">
                      <div class="nav-item" :style="{ background: theme.colors.primary }"></div>
                      <div class="nav-item" :style="{ background: theme.colors.hover }"></div>
                      <div class="nav-item" :style="{ background: theme.colors.hover }"></div>
                    </div>
                    <div class="preview-body" :style="{ background: theme.colors.background }">
                      <div class="body-text" :style="{ background: theme.colors.card }"></div>
                      <div class="body-text-small" :style="{ background: theme.colors.hover }"></div>
                    </div>
                  </div>
                </div>
                <div class="theme-details">
                  <el-icon class="theme-icon" :style="{ color: theme.id === 'system' ? '#4F46E5' : theme.colors.primary }">
                    <Sunny v-if="theme.icon === 'sunny'" />
                    <Moon v-else-if="theme.icon === 'moon'" />
                    <Monitor v-else-if="theme.icon === 'monitor'" />
                    <MagicStick v-else />
                  </el-icon>
                  <div class="theme-meta">
                    <span class="theme-name">{{ theme.name }}</span>
                    <span class="theme-desc">{{ theme.description }}</span>
                  </div>
                  <el-icon v-if="currentTheme === theme.id" class="check-icon"><Check /></el-icon>
                </div>
              </div>
            </div>
          </div>

          <div class="panel-section">
            <div class="section-header">
              <h2>显示设置</h2>
              <p>调整亮度和对比度以获得最佳视觉体验</p>
            </div>

            <div class="settings-group">
              <div class="setting-item">
                <div class="setting-header">
                  <span class="setting-label">亮度</span>
                  <span class="setting-value">{{ brightness }}%</span>
                </div>
                <el-slider
                  v-model="brightness"
                  :min="50"
                  :max="150"
                  :step="1"
                  @change="(v: any) => onBrightnessChange(Number(v))"
                  class="setting-slider"
                />
                <div class="setting-hint">向左降低亮度，向右提高亮度</div>
              </div>

              <div class="setting-item">
                <div class="setting-header">
                  <span class="setting-label">对比度</span>
                  <span class="setting-value">{{ contrast }}%</span>
                </div>
                <el-slider
                  v-model="contrast"
                  :min="50"
                  :max="150"
                  :step="1"
                  @change="(v: any) => onContrastChange(Number(v))"
                  class="setting-slider"
                />
                <div class="setting-hint">向左降低对比度，向右提高对比度</div>
              </div>
            </div>
          </div>

          <div class="panel-section">
            <div class="section-header">
              <h2>操作</h2>
            </div>

            <div class="action-buttons">
              <el-button
                type="primary"
                size="large"
                @click="resetToDefault"
                class="action-btn"
              >
                <el-icon><Refresh /></el-icon>
                恢复默认设置
              </el-button>
              <el-button
                size="large"
                @click="exportSettings"
                class="action-btn secondary"
              >
                <el-icon><Download /></el-icon>
                导出配置
              </el-button>
            </div>
          </div>
        </div>

        <div class="preview-panel">
          <div class="panel-section preview-section">
            <div class="section-header">
              <h2>实时预览</h2>
              <span class="preview-label">当前主题：{{ currentThemeConfig.name }}</span>
            </div>

            <div class="live-preview" :style="previewStyle">
              <div class="preview-header" :style="{ background: currentThemeConfig.colors.gradientPrimary }">
                <div class="preview-logo">
                  <span class="logo-icon">M</span>
                  <span class="logo-text">MyBlob</span>
                </div>
                <div class="preview-nav-items">
                  <span class="nav-item active">首页</span>
                  <span class="nav-item">探索</span>
                  <span class="nav-item">工具</span>
                  <span class="nav-item">关于</span>
                </div>
              </div>

              <div class="preview-body">
                <div class="preview-card">
                  <div class="card-header">
                    <span class="card-badge" :style="{ background: currentThemeConfig.colors.primaryLight, color: currentThemeConfig.colors.primary }">精选</span>
                    <span class="card-date">2024年1月15日</span>
                  </div>
                  <h3 class="card-title">如何提升工作效率</h3>
                  <p class="card-text">分享一些实用的工作技巧和工具推荐...</p>
                  <div class="card-footer">
                    <div class="author-info">
                      <div class="author-avatar" :style="{ background: currentThemeConfig.colors.primary }"></div>
                      <span class="author-name">作者</span>
                    </div>
                    <div class="card-stats">
                      <span class="stat-item"><View /> 1.2k</span>
                      <span class="stat-item"><Message /> 45</span>
                      <span class="stat-item"><Star /> 234</span>
                    </div>
                  </div>
                </div>

                <div class="preview-cards-row">
                  <div class="small-card">
                    <div class="small-card-icon" :style="{ background: currentThemeConfig.colors.primaryLight }"><el-icon><Edit /></el-icon></div>
                    <span class="small-card-title">写文章</span>
                  </div>
                  <div class="small-card">
                    <div class="small-card-icon" :style="{ background: currentThemeConfig.colors.primaryLight }"><el-icon><Folder /></el-icon></div>
                    <span class="small-card-title">文件管理</span>
                  </div>
                  <div class="small-card">
                    <div class="small-card-icon" :style="{ background: currentThemeConfig.colors.primaryLight }"><el-icon><Setting /></el-icon></div>
                    <span class="small-card-title">设置</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useThemeStore, type ThemeType } from '@/store/theme'
import { Brush, Check, Refresh, Download, View, Message, Star, Sunny, Moon, Monitor, MagicStick, Edit, Folder, Setting } from '@element-plus/icons-vue'

const themeStore = useThemeStore()

const currentTheme = ref<ThemeType>(themeStore.currentTheme)
const brightness = ref(themeStore.brightness)
const contrast = ref(themeStore.contrast)

const availableThemes = computed(() => themeStore.availableThemes)
const currentThemeConfig = computed(() => themeStore.currentThemeConfig)

const previewStyle = computed(() => ({
  '--preview-primary': currentThemeConfig.value.colors.primary,
  '--preview-background': currentThemeConfig.value.colors.background,
  '--preview-card': currentThemeConfig.value.colors.card,
  '--preview-text': currentThemeConfig.value.colors.text,
  '--preview-text-secondary': currentThemeConfig.value.colors.textSecondary,
  '--preview-border': currentThemeConfig.value.colors.border,
}))

const setTheme = (theme: ThemeType) => {
  themeStore.setTheme(theme)
  currentTheme.value = theme
}

const onBrightnessChange = (value: number) => {
  themeStore.setBrightness(value)
}

const onContrastChange = (value: number) => {
  themeStore.setContrast(value)
}

const resetToDefault = () => {
  themeStore.setTheme('system')
  themeStore.setBrightness(100)
  themeStore.setContrast(100)
  currentTheme.value = 'system'
  brightness.value = 100
  contrast.value = 100
}

const exportSettings = () => {
  const settings = {
    theme: themeStore.currentTheme,
    brightness: themeStore.brightness,
    contrast: themeStore.contrast,
  }
  const blob = new Blob([JSON.stringify(settings, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'theme-settings.json'
  a.click()
  URL.revokeObjectURL(url)
}

watch(currentTheme, (newTheme) => {
  themeStore.setTheme(newTheme)
})
</script>

<style scoped>
.theme-settings-page {
  min-height: 100vh;
  padding: 24px;
  background: var(--theme-background);
}

.page-header {
  margin-bottom: 24px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;

  .title-icon {
    font-size: 32px;
    color: var(--theme-primary);
  }

  h1 {
    font-size: 28px;
    font-weight: 700;
    color: var(--theme-text);
    margin: 0;
  }
}

.header-subtitle {
  font-size: 14px;
  color: var(--theme-text-secondary);
  margin: 0;
}

.page-content {
  max-width: 1400px;
  margin: 0 auto;
}

.content-row {
  display: grid;
  grid-template-columns: 1fr 420px;
  gap: 24px;
}

.main-panel {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.panel-section {
  background: var(--theme-card);
  border-radius: var(--radius-lg);
  padding: 24px;
  border: 1px solid var(--theme-border);
}

.section-header {
  margin-bottom: 20px;

  h2 {
    font-size: 18px;
    font-weight: 600;
    color: var(--theme-text);
    margin: 0 0 4px 0;
  }

  p {
    font-size: 13px;
    color: var(--theme-text-secondary);
    margin: 0;
  }

  .preview-label {
    font-size: 13px;
    color: var(--theme-primary);
    font-weight: 500;
  }
}

.theme-selector {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.theme-option {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: var(--radius-md);
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  background: var(--theme-background);

  &:hover {
    border-color: var(--theme-primary-light);
    transform: translateX(4px);
  }

  &.active {
    border-color: var(--theme-primary);
    background: var(--theme-primary-light);
  }
}

.theme-visual {
  flex-shrink: 0;
  width: 100px;

  .color-bar {
    height: 8px;
    border-radius: 4px 4px 0 0;
  }

  .mini-preview {
    background: var(--theme-background);
    border-radius: 0 0 4px 4px;
    padding: 8px;

    .preview-nav {
      display: flex;
      gap: 4px;
      margin-bottom: 8px;
      padding: 4px;
      border-radius: 4px;

      .nav-item {
        width: 20px;
        height: 20px;
        border-radius: 4px;
      }
    }

    .preview-body {
      border-radius: 4px;
      padding: 6px;

      .body-text {
        height: 10px;
        border-radius: 2px;
        margin-bottom: 4px;
      }

      .body-text-small {
        height: 6px;
        border-radius: 2px;
        width: 60%;
      }
    }
  }
}

.theme-details {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;

  .theme-icon {
    font-size: 24px;
    flex-shrink: 0;
  }

  .theme-meta {
    flex: 1;

    .theme-name {
      display: block;
      font-size: 15px;
      font-weight: 600;
      color: var(--theme-text);
      margin-bottom: 2px;
    }

    .theme-desc {
      display: block;
      font-size: 13px;
      color: var(--theme-text-secondary);
    }
  }

  .check-icon {
    color: var(--theme-primary);
    font-size: 20px;
  }
}

.settings-group {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.setting-item {
  .setting-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .setting-label {
      font-size: 14px;
      font-weight: 500;
      color: var(--theme-text);
    }

    .setting-value {
      font-size: 14px;
      font-weight: 600;
      color: var(--theme-primary);
    }
  }

  .setting-slider {
    margin-bottom: 8px;
  }

  .setting-hint {
    font-size: 12px;
    color: var(--theme-text-secondary);
  }
}

.action-buttons {
  display: flex;
  gap: 12px;

  .action-btn {
    flex: 1;
    height: 44px;
    font-weight: 500;

    &.secondary {
      background: var(--theme-card);
      border-color: var(--theme-border);
      color: var(--theme-text);

      &:hover {
        background: var(--theme-hover);
      }
    }
  }
}

.preview-panel {
  position: sticky;
  top: 24px;
  height: fit-content;
}

.preview-section {
  position: relative;
}

.live-preview {
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
  background: var(--theme-background);
}

.preview-header {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .preview-logo {
    display: flex;
    align-items: center;
    gap: 8px;

    .logo-icon {
      width: 32px;
      height: 32px;
      border-radius: 8px;
      background: rgba(255, 255, 255, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-weight: 700;
    }

    .logo-text {
      color: white;
      font-weight: 700;
      font-size: 16px;
    }
  }

  .preview-nav-items {
    display: flex;
    gap: 16px;

    .nav-item {
      font-size: 13px;
      color: rgba(255, 255, 255, 0.8);
      padding: 6px 12px;
      border-radius: 20px;
      transition: all 0.2s;

      &.active {
        background: rgba(255, 255, 255, 0.2);
        color: white;
      }
    }
  }
}

.preview-body {
  padding: 20px;
}

.preview-card {
  background: var(--theme-card);
  border-radius: var(--radius-md);
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid var(--theme-border);

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .card-badge {
      padding: 4px 10px;
      border-radius: 4px;
      font-size: 12px;
      font-weight: 500;
    }

    .card-date {
      font-size: 12px;
      color: var(--theme-text-secondary);
    }
  }

  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--theme-text);
    margin: 0 0 8px 0;
  }

  .card-text {
    font-size: 13px;
    color: var(--theme-text-secondary);
    margin: 0 0 12px 0;
    line-height: 1.5;
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .author-info {
      display: flex;
      align-items: center;
      gap: 8px;

      .author-avatar {
        width: 28px;
        height: 28px;
        border-radius: 50%;
      }

      .author-name {
        font-size: 13px;
        color: var(--theme-text-secondary);
      }
    }

    .card-stats {
      display: flex;
      gap: 16px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: var(--theme-text-secondary);
      }
    }
  }
}

.preview-cards-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;

  .small-card {
    background: var(--theme-card);
    border-radius: var(--radius-sm);
    padding: 16px;
    text-align: center;
    border: 1px solid var(--theme-border);

    .small-card-icon {
      width: 40px;
      height: 40px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      margin: 0 auto 8px;
    }

    .small-card-title {
      font-size: 13px;
      color: var(--theme-text);
      font-weight: 500;
    }
  }
}

@media (max-width: 1024px) {
  .content-row {
    grid-template-columns: 1fr;
  }

  .preview-panel {
    order: -1;
  }
}
</style>
