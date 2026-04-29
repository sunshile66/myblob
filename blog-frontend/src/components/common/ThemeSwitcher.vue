<template>
  <div class="theme-switcher">
    <el-dropdown trigger="click" @command="handleThemeChange">
      <div class="theme-trigger">
        <el-icon><Sunny /></el-icon>
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="light">
            <div class="theme-option">
              <span class="theme-icon" style="background: linear-gradient(135deg, #ff2442, #ff6b9d);"></span>
              <span>浅色主题</span>
            </div>
          </el-dropdown-item>
          <el-dropdown-item command="dark">
            <div class="theme-option">
              <span class="theme-icon" style="background: linear-gradient(135deg, #1a1a1a, #2d2d2d);"></span>
              <span>深色主题</span>
            </div>
          </el-dropdown-item>
          <el-dropdown-item command="pink">
            <div class="theme-option">
              <span class="theme-icon" style="background: linear-gradient(135deg, #ff6b9d, #f093fb);"></span>
              <span>粉色主题</span>
            </div>
          </el-dropdown-item>
          <el-dropdown-item command="blue">
            <div class="theme-option">
              <span class="theme-icon" style="background: linear-gradient(135deg, #3b82f6, #60a5fa);"></span>
              <span>蓝色主题</span>
            </div>
          </el-dropdown-item>
          <el-dropdown-item command="purple">
            <div class="theme-option">
              <span class="theme-icon" style="background: linear-gradient(135deg, #8b5cf6, #a78bfa);"></span>
              <span>紫色主题</span>
            </div>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { useThemeStore, type ThemeType } from '@/store/theme'
import { Sunny } from '@element-plus/icons-vue'

const themeStore = useThemeStore()

const handleThemeChange = (theme: ThemeType) => {
  themeStore.setTheme(theme)
}
</script>

<style scoped>
.theme-switcher {
  .theme-trigger {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    cursor: pointer;
    transition: all 0.3s;
    color: #666;
    font-size: 20px;
  }

  .theme-trigger:hover {
    background: rgba(0, 0, 0, 0.08);
    color: #333;
  }

  .theme-option {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .theme-icon {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    flex-shrink: 0;
  }
}
</style>
