<template>
  <div class="app-container">
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useThemeStore } from "@/store/theme";

const themeStore = useThemeStore();

onMounted(() => {
  themeStore.initTheme();
});
</script>

<style>
@import url("https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@300;400;500;700&family=Inter:wght@300;400;500;600;700&display=swap");

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

:root {
  --theme-primary: #ff2442;
  --theme-primary-light: rgba(255, 36, 66, 0.1);
  --theme-secondary: #667eea;
  --theme-background: #f5f5f5;
  --theme-card: #ffffff;
  --theme-text: #333333;
  --theme-text-secondary: #666666;
  --theme-border: #e5e5e5;
  --theme-hover: #f5f5f5;

  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.06);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.12);
  --shadow-xl: 0 16px 48px rgba(0, 0, 0, 0.15);

  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-xl: 20px;
  --radius-2xl: 28px;

  --glass-bg: rgba(255, 255, 255, 0.7);
  --glass-border: rgba(255, 255, 255, 0.3);
  --glass-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  --glass-blur: 20px;

  --gradient-primary: linear-gradient(135deg, #ff2442 0%, #ff6b9d 100%);
  --gradient-secondary: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --gradient-success: linear-gradient(135deg, #10b981 0%, #059669 100%);
  --gradient-warning: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  --gradient-info: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);

  --transition-fast: 0.15s ease;
  --transition-normal: 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-slow: 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

html {
  font-size: 16px;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

body {
  font-family: "Noto Sans SC", "Inter", -apple-system, BlinkMacSystemFont,
    "Segoe UI", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  background: var(--theme-background);
  color: var(--theme-text);
  min-height: 100vh;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.app-container,
#app {
  min-height: 100vh;
}

.el-image {
  background-color: var(--theme-hover);
}

.el-card {
  background: var(--theme-card) !important;
  border: 1px solid var(--theme-border) !important;
}

.el-button--primary {
  background: var(--theme-primary) !important;
  border-color: var(--theme-primary) !important;
}

.el-button--primary:hover {
  background: color-mix(in srgb, var(--theme-primary) 85%, black) !important;
  border-color: color-mix(in srgb, var(--theme-primary) 85%, black) !important;
}

.el-tabs__item.is-active {
  color: var(--theme-primary) !important;
}

.el-tabs__active-bar {
  background: var(--theme-primary) !important;
}

.el-tag--info {
  background: var(--theme-primary-light) !important;
  color: var(--theme-primary) !important;
  border: 1px solid var(--theme-primary) !important;
}

.el-input__wrapper {
  background: var(--theme-card) !important;
  box-shadow: 0 0 0 1px var(--theme-border) inset !important;
}

.el-input__wrapper:hover,
.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 1px var(--theme-primary) inset !important;
}

.glass-card {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
  border-radius: var(--radius-lg);
  transition: all var(--transition-normal);
}

.glass-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-xl);
}

.gradient-text {
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.gradient-border {
  position: relative;
  background: var(--theme-card);
  border-radius: var(--radius-lg);
}

.gradient-border::before {
  content: "";
  position: absolute;
  inset: -2px;
  background: var(--gradient-primary);
  border-radius: inherit;
  z-index: -1;
}

.animate-float {
  animation: float 6s ease-in-out infinite;
}

.animate-float-delay-1 {
  animation: float 6s ease-in-out 1s infinite;
}

.animate-float-delay-2 {
  animation: float 6s ease-in-out 2s infinite;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

.animate-pulse-glow {
  animation: pulse-glow 2s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%,
  100% {
    box-shadow: 0 0 0 0 rgba(255, 36, 66, 0.4);
  }
  50% {
    box-shadow: 0 0 0 20px rgba(255, 36, 66, 0);
  }
}

.animate-shimmer {
  background: linear-gradient(
    90deg,
    var(--theme-card) 0%,
    var(--theme-hover) 50%,
    var(--theme-card) 100%
  );
  background-size: 200% 100%;
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

.hover-lift {
  transition: transform var(--transition-normal),
    box-shadow var(--transition-normal);
}

.hover-lift:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-xl);
}

.hover-scale {
  transition: transform var(--transition-fast);
}

.hover-scale:hover {
  transform: scale(1.05);
}

.bg-gradient {
  background: linear-gradient(
    135deg,
    var(--theme-background) 0%,
    var(--theme-card) 100%
  );
}

.bg-pattern {
  background-color: var(--theme-background);
  background-image: radial-gradient(var(--theme-primary) 1px, transparent 1px);
  background-size: 20px 20px;
}

@media (max-width: 768px) {
  :root {
    --radius-lg: 12px;
    --radius-xl: 16px;
    --radius-2xl: 20px;
  }

  .hide-mobile {
    display: none !important;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .hide-tablet {
    display: none !important;
  }
}

@media (min-width: 1025px) {
  .hide-desktop {
    display: none !important;
  }
}

.scroll-smooth {
  scroll-behavior: smooth;
}

.scrollbar-hide::-webkit-scrollbar {
  display: none;
}

.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
