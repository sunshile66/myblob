<template>
  <div class="app-container">
    <router-view v-slot="{ Component, route }">
      <keep-alive :max="8" :exclude="heavyComponents">
        <component :is="Component" :key="route.path" />
      </keep-alive>
    </router-view>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useThemeStore } from "@/store/theme";
import { useUserStore } from "@/store/user";

const themeStore = useThemeStore();
const userStore = useUserStore();

onMounted(() => {
  themeStore.initTheme();
  userStore.initUser();
});

// 排除内存占用大的组件，避免浏览器崩溃
const heavyComponents = ['ImageEditorPro', 'CrawlerToolkit', 'CurlConverter', 'ColorPicker', 'ImageCompressor']
</script>

<style>
/* 字体声明使用 link preload 在 index.html 中处理，此处仅做后备 */
@font-face {
  font-family: 'Inter';
  src: local('Inter');
  font-display: swap;
}
@font-face {
  font-family: 'JetBrains Mono';
  src: local('JetBrains Mono');
  font-display: swap;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

:root {
  /* 主色调 — 靖蓝 Indigo */
  --theme-primary: #4F46E5;
  --theme-primary-light: rgba(79, 70, 229, 0.10);
  --theme-secondary: #6366F1;

  /* 中性色阶 */
  --theme-background: #FAFAFB;
  --theme-card: #FFFFFF;
  --theme-text: #0F172A;
  --theme-text-secondary: #64748B;
  --theme-text-tertiary: #94A3B8;
  --theme-border: #E5E7EB;
  --theme-border-strong: #D1D5DB;
  --theme-hover: #F1F5F9;
  --theme-muted: #F8FAFC;

  /* 语义色 */
  --color-success: #10B981;
  --color-warning: #F59E0B;
  --color-danger: #EF4444;
  --color-info: #0EA5E9;

  /* 阴影 — 以中性色 + 低透明度 */
  --shadow-xs: 0 1px 2px rgba(15, 23, 42, 0.04);
  --shadow-sm: 0 1px 3px rgba(15, 23, 42, 0.06), 0 1px 2px rgba(15, 23, 42, 0.04);
  --shadow-md: 0 4px 12px rgba(15, 23, 42, 0.06), 0 2px 4px rgba(15, 23, 42, 0.04);
  --shadow-lg: 0 10px 24px rgba(15, 23, 42, 0.08), 0 4px 8px rgba(15, 23, 42, 0.04);
  --shadow-xl: 0 20px 40px rgba(15, 23, 42, 0.10), 0 8px 16px rgba(15, 23, 42, 0.04);

  /* 圆角 — 克制 */
  --radius-xs: 4px;
  --radius-sm: 6px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-xl: 16px;
  --radius-2xl: 20px;
  --radius-full: 9999px;

  /* 间距 */
  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-5: 20px;
  --space-6: 24px;
  --space-8: 32px;
  --space-10: 40px;
  --space-12: 48px;
  --space-16: 64px;

  /* 排版 */
  --font-sans: "Inter", "HarmonyOS Sans SC", -apple-system, BlinkMacSystemFont,
    "Segoe UI", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  --font-mono: "JetBrains Mono", "SF Mono", Menlo, Consolas, monospace;

  --text-xs: 12px;
  --text-sm: 13px;
  --text-base: 14px;
  --text-md: 15px;
  --text-lg: 16px;
  --text-xl: 18px;
  --text-2xl: 22px;
  --text-3xl: 28px;
  --text-4xl: 36px;

  --leading-tight: 1.3;
  --leading-normal: 1.55;
  --leading-relaxed: 1.7;

  /* 玻璃拟态 */
  --glass-bg: rgba(255, 255, 255, 0.72);
  --glass-border: rgba(15, 23, 42, 0.06);
  --glass-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  --glass-blur: 18px;

  /* 渐变 */
  --gradient-primary: linear-gradient(135deg, #4F46E5 0%, #6366F1 100%);
  --gradient-secondary: linear-gradient(135deg, #4338CA 0%, #4F46E5 100%);
  --gradient-success: linear-gradient(135deg, #10B981 0%, #059669 100%);
  --gradient-warning: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
  --gradient-info: linear-gradient(135deg, #0EA5E9 0%, #0284C7 100%);

  /* 缓动 */
  --transition-fast: 0.12s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-normal: 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-slow: 0.32s cubic-bezier(0.4, 0, 0.2, 1);

  --brightness: 100%;
  --contrast: 100%;
}

html {
  font-size: 16px;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
}

body {
  font-family: var(--font-sans);
  font-size: var(--text-base);
  line-height: var(--leading-normal);
  letter-spacing: -0.005em;
  background: var(--theme-background);
  color: var(--theme-text);
  min-height: 100vh;
  transition: background-color var(--transition-normal), color var(--transition-normal);
}

h1, h2, h3, h4, h5, h6 {
  font-weight: 600;
  letter-spacing: -0.018em;
  line-height: var(--leading-tight);
  color: var(--theme-text);
}

h1 { font-size: var(--text-3xl); }
h2 { font-size: var(--text-2xl); }
h3 { font-size: var(--text-xl); }
h4 { font-size: var(--text-lg); }

p {
  line-height: var(--leading-relaxed);
  color: var(--theme-text);
}

a {
  color: var(--theme-primary);
  text-decoration: none;
  transition: color var(--transition-fast);
}

a:hover {
  color: color-mix(in srgb, var(--theme-primary) 80%, black);
}

code, pre {
  font-family: var(--font-mono);
  font-size: 0.92em;
}

.app-container,
#app {
  min-height: 100vh;
  filter: brightness(var(--brightness)) contrast(var(--contrast));
}

/* 滚动条 — 细而内敛 */
::-webkit-scrollbar {
  width: 7px;
  height: 7px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: var(--theme-border-strong);
  border-radius: var(--radius-full);
  transition: background 0.2s ease;
}
::-webkit-scrollbar-thumb:hover {
  background: var(--theme-text-tertiary);
}

/* 选中高亮 */
::selection {
  background: var(--theme-primary-light);
  color: var(--theme-primary);
}

/* 聚焦样式 — 清晰可见 */
:focus-visible {
  outline: 2px solid var(--theme-primary);
  outline-offset: 2px;
  border-radius: 4px;
}

/* ========== Element Plus 组件细节优化 ========== */

.el-image {
  background-color: var(--theme-muted);
}

/* 卡片 — 去掉强边框，改用柔和阴影 */
.el-card {
  background: var(--theme-card) !important;
  border: 1px solid var(--theme-border) !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04) !important;
  transition: box-shadow var(--transition-normal), border-color var(--transition-normal), transform var(--transition-normal) !important;
}

.el-card.is-hover-shadow:hover,
.el-card.hover-lift:hover {
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06), 0 2px 4px rgba(15, 23, 42, 0.04) !important;
  border-color: var(--theme-border-strong) !important;
}

/* 按钮 — 清晰层级 */
.el-button {
  border-radius: var(--radius-md) !important;
  font-weight: 500 !important;
  transition: all var(--transition-fast) !important;
  letter-spacing: -0.01em;
}

.el-button--primary {
  background: var(--theme-primary) !important;
  border-color: var(--theme-primary) !important;
  box-shadow: 0 1px 3px rgba(79, 70, 229, 0.2) !important;
}

.el-button--primary:hover {
  background: color-mix(in srgb, var(--theme-primary) 90%, black) !important;
  border-color: color-mix(in srgb, var(--theme-primary) 90%, black) !important;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25) !important;
  transform: translateY(-1px);
}

.el-button--primary:active {
  transform: translateY(0);
  box-shadow: 0 1px 3px rgba(79, 70, 229, 0.2) !important;
}

.el-button--default {
  background: var(--theme-card) !important;
  border-color: var(--theme-border) !important;
  color: var(--theme-text) !important;
}

.el-button--default:hover {
  background: var(--theme-hover) !important;
  border-color: var(--theme-border-strong) !important;
  color: var(--theme-primary) !important;
}

/* Tabs */
.el-tabs__item {
  font-weight: 500 !important;
  color: var(--theme-text-secondary) !important;
  transition: color var(--transition-fast) !important;
}
.el-tabs__item.is-active {
  color: var(--theme-primary) !important;
}
.el-tabs__active-bar {
  background: var(--theme-primary) !important;
  height: 2px !important;
  border-radius: var(--radius-full) !important;
}
.el-tabs__nav-wrap::after {
  background-color: var(--theme-border) !important;
  height: 1px !important;
}

/* Tag — 柔和调 */
.el-tag {
  border-radius: var(--radius-sm) !important;
  font-weight: 500 !important;
  border: none !important;
  letter-spacing: -0.01em;
}
.el-tag--info {
  background: var(--theme-primary-light) !important;
  color: var(--theme-primary) !important;
}

/* 输入框 */
.el-input__wrapper {
  background: var(--theme-card) !important;
  border-radius: var(--radius-md) !important;
  box-shadow: 0 0 0 1px var(--theme-border) inset !important;
  transition: box-shadow var(--transition-fast), border-color var(--transition-fast) !important;
}
.el-input__wrapper:hover {
  box-shadow: 0 0 0 1px var(--theme-border-strong) inset !important;
}
.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 2px var(--theme-primary) inset !important;
  border-color: var(--theme-primary) !important;
}
.el-textarea__inner {
  border-radius: var(--radius-md) !important;
  box-shadow: 0 0 0 1px var(--theme-border) inset !important;
  transition: box-shadow var(--transition-fast), border-color var(--transition-fast) !important;
}
.el-textarea__inner:hover {
  box-shadow: 0 0 0 1px var(--theme-border-strong) inset !important;
}
.el-textarea__inner:focus {
  box-shadow: 0 0 0 2px var(--theme-primary) inset !important;
  border-color: var(--theme-primary) !important;
}

/* 下拉菜单 — 柔和阴影 + 圆角 */
.el-dropdown-menu,
.el-popper.is-light {
  background: var(--theme-card) !important;
  border: 1px solid var(--theme-border) !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08), 0 4px 8px rgba(15, 23, 42, 0.04) !important;
  padding: var(--space-1) !important;
}

.el-dropdown-menu__item {
  color: var(--theme-text) !important;
  border-radius: var(--radius-sm) !important;
  padding: var(--space-2) var(--space-3) !important;
  transition: background var(--transition-fast), color var(--transition-fast) !important;
  font-size: 14px;
  letter-spacing: -0.01em;
}
.el-dropdown-menu__item:hover,
.el-dropdown-menu__item:focus {
  background: var(--theme-hover) !important;
  color: var(--theme-primary) !important;
}

.el-popover {
  background: var(--theme-card) !important;
  border: 1px solid var(--theme-border) !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08), 0 4px 8px rgba(15, 23, 42, 0.04) !important;
}

/* 分隔线 */
.el-divider {
  background: var(--theme-border) !important;
}

/* Badge */
.el-badge__content {
  background: var(--theme-primary) !important;
  font-weight: 600 !important;
  letter-spacing: 0 !important;
}

/* 头像 */
.el-avatar {
  background: var(--theme-primary-light) !important;
  color: var(--theme-primary) !important;
  font-weight: 600 !important;
}

/* Dialog — 现代化 */
.el-dialog {
  border-radius: var(--radius-xl) !important;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.1), 0 8px 16px rgba(15, 23, 42, 0.04) !important;
  overflow: hidden;
  border: 1px solid var(--theme-border);
}
.el-dialog__header {
  padding: var(--space-5) var(--space-6) !important;
  border-bottom: 1px solid var(--theme-border);
}
.el-dialog__title {
  font-weight: 600 !important;
  font-size: var(--text-lg) !important;
  letter-spacing: -0.01em;
}
.el-dialog__body {
  padding: var(--space-6) !important;
}

/* Message / Notification */
.el-message,
.el-notification {
  border-radius: var(--radius-lg) !important;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08), 0 4px 8px rgba(15, 23, 42, 0.04) !important;
  border: 1px solid var(--theme-border) !important;
}

/* Switch */
.el-switch.is-checked .el-switch__core {
  background: var(--theme-primary) !important;
  border-color: var(--theme-primary) !important;
}

/* Pagination */
.el-pagination .el-pager li.is-active {
  background: var(--theme-primary) !important;
  color: #fff !important;
  border-radius: var(--radius-sm) !important;
  font-weight: 600;
}

.el-pagination .el-pager li {
  transition: all var(--transition-fast);
  border-radius: var(--radius-sm);
}

.el-pagination .el-pager li:hover {
  color: var(--theme-primary);
}

/* ========== 通用工具类 ========== */

/* 玻璃拟态 — 仅按需使用，非默认样式 */
.glass-surface {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
  border-radius: var(--radius-lg);
}

.gradient-border {
  position: relative;
  background: var(--theme-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.gradient-border::before {
  content: "";
  position: absolute;
  inset: -1px;
  background: var(--gradient-primary);
  border-radius: inherit;
  z-index: -1;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.gradient-border:hover::before {
  opacity: 1;
}

.hover-lift {
  transition: transform var(--transition-normal), box-shadow var(--transition-normal), border-color var(--transition-normal);
}
.hover-lift:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06), 0 2px 4px rgba(15, 23, 42, 0.04);
  border-color: var(--theme-border-strong);
}

.hover-scale {
  transition: transform var(--transition-fast);
}
.hover-scale:hover {
  transform: scale(1.02);
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
  background-image: radial-gradient(var(--theme-border-strong) 1px, transparent 1px);
  background-size: 20px 20px;
}

/* 动画 — 保留但幅度收敛 */
.animate-float {
  animation: float 6s cubic-bezier(0.4, 0, 0.2, 1) infinite;
}
.animate-float-delay-1 { animation: float 6s cubic-bezier(0.4, 0, 0.2, 1) 1s infinite; }
.animate-float-delay-2 { animation: float 6s cubic-bezier(0.4, 0, 0.2, 1) 2s infinite; }

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-8px); }
}

.animate-pulse-glow {
  animation: pulse-glow 2.4s cubic-bezier(0.4, 0, 0.2, 1) infinite;
}

@keyframes pulse-glow {
  0%, 100% { box-shadow: 0 0 0 0 rgba(79, 70, 229, 0.32); }
  50% { box-shadow: 0 0 0 12px rgba(79, 70, 229, 0); }
}

.animate-shimmer {
  background: linear-gradient(
    90deg,
    var(--theme-card) 0%,
    var(--theme-hover) 50%,
    var(--theme-card) 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.8s cubic-bezier(0.4, 0, 0.2, 1) infinite;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }

  .hover-lift:hover,
  .hover-scale:hover {
    transform: none;
  }
}

@media (max-width: 768px) {
  :root {
    --text-3xl: 24px;
    --text-4xl: 30px;
    --radius-lg: 10px;
    --radius-xl: 14px;
  }
  .hide-mobile { display: none !important; }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .hide-tablet { display: none !important; }
}

@media (min-width: 1025px) {
  .hide-desktop { display: none !important; }
}

/* 优化移动端触摸体验 */
@media (hover: none) and (pointer: coarse) {
  .hover-lift:hover,
  .hover-scale:hover {
    transform: none;
  }
  .hover-lift:active,
  .hover-scale:active {
    transform: scale(0.98);
  }
}

.scroll-smooth { scroll-behavior: smooth; }
.scrollbar-hide::-webkit-scrollbar { display: none; }
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* 平滑滚动 — 仅在用户未设置减少动画时生效 */
@media (prefers-reduced-motion: no-preference) {
  html {
    scroll-behavior: smooth;
  }
}

.theme-transition {
  transition: background-color var(--transition-normal),
    color var(--transition-normal),
    border-color var(--transition-normal),
    box-shadow var(--transition-normal);
}
</style>
