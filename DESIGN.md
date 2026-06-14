---
name: Myblob
description: 面向开发者的轻量级创作与效率工作台
colors:
  primary: "#4F46E5"
  primary-light: "rgba(79, 70, 229, 0.10)"
  secondary: "#6366F1"
  background: "#FAFAFB"
  card: "#FFFFFF"
  text: "#0F172A"
  text-secondary: "#64748B"
  text-tertiary: "#94A3B8"
  border: "#E5E7EB"
  border-strong: "#D1D5DB"
  hover: "#F1F5F9"
  muted: "#F8FAFC"
  success: "#10B981"
  warning: "#F59E0B"
  danger: "#EF4444"
  info: "#0EA5E9"
typography:
  display:
    fontFamily: "Inter, HarmonyOS Sans SC, -apple-system, BlinkMacSystemFont, Segoe UI, PingFang SC, Hiragino Sans GB, Microsoft YaHei, sans-serif"
    fontSize: "clamp(1.75rem, 4vw, 2.25rem)"
    fontWeight: 600
    lineHeight: 1.3
    letterSpacing: "-0.018em"
  body:
    fontFamily: "Inter, HarmonyOS Sans SC, -apple-system, BlinkMacSystemFont, Segoe UI, PingFang SC, Hiragino Sans GB, Microsoft YaHei, sans-serif"
    fontSize: "14px"
    fontWeight: 400
    lineHeight: 1.55
    letterSpacing: "-0.005em"
  mono:
    fontFamily: "JetBrains Mono, SF Mono, Menlo, Consolas, monospace"
    fontSize: "12.88px"
    fontWeight: 400
    lineHeight: 1.5
    letterSpacing: "normal"
rounded:
  xs: "4px"
  sm: "6px"
  md: "8px"
  lg: "12px"
  xl: "16px"
  2xl: "20px"
  full: "9999px"
spacing:
  1: "4px"
  2: "8px"
  3: "12px"
  4: "16px"
  5: "20px"
  6: "24px"
  8: "32px"
  10: "40px"
  12: "48px"
  16: "64px"
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "#FFFFFF"
    rounded: "{rounded.md}"
    padding: "10px 20px"
    size: "14px"
    height: "40px"
  button-primary-hover:
    backgroundColor: "color-mix(in srgb, {colors.primary} 88%, black)"
  button-default:
    backgroundColor: "{colors.card}"
    textColor: "{colors.text}"
    rounded: "{rounded.md}"
    padding: "10px 20px"
    size: "14px"
    height: "40px"
  card:
    backgroundColor: "{colors.card}"
    rounded: "{rounded.lg}"
    padding: "20px 24px"
  input:
    backgroundColor: "{colors.card}"
    rounded: "{rounded.md}"
    padding: "8px 12px"
  tag:
    backgroundColor: "{colors.primary-light}"
    textColor: "{colors.primary}"
    rounded: "{rounded.sm}"
    padding: "4px 8px"
---

# Design System: Myblob

## 1. Overview

**Creative North Star: "创意工作室"**

Myblob 的设计系统是一个精心整理的创意工作室——简洁、有序、专注。它为开发者提供一个没有干扰的工作空间，让创作和工具使用变得愉悦而高效。

这个系统明确拒绝 PRODUCT.md 中定义的反面案例：过于复杂的界面、过度设计、千篇一律和视觉混乱。相反，它追求一种平衡——既有足够的个性和趣味性，又保持简洁和专注。

**Key Characteristics:**
- **内容优先**：界面服务于内容，减少视觉噪音
- **渐进式复杂度**：基础功能简单直观，高级功能可发现但不干扰
- **一致性与个性并存**：统一的交互模式，通过细节展现平台个性
- **性能即体验**：快速加载和流畅交互是设计的基础

## 2. Colors: The Deep Indigo Palette

主色调采用深邃靛蓝（#4F46E5），传达专业、可靠和创意的气质。配色系统支持多主题切换，包括浅色、深色和多种彩色主题。

### Primary
- **深邃靛蓝** (#4F46E5): 主要交互元素、按钮、链接、选中状态。用于引导用户注意力和执行关键操作。
- **靛蓝浅色** (rgba(79, 70, 229, 0.10)): 背景高亮、选中项背景、头像背景。用于轻微强调而不喧宾夺主。
- **靛蓝次级** (#6366F1): 渐变终点、次要强调。与主色形成和谐的色彩层次。

### Neutral
- **雪白背景** (#FAFAFB): 页面主背景，提供明亮清爽的基底。
- **纯白卡片** (#FFFFFF): 卡片、面板、模态框背景，与页面背景形成层次。
- **深灰文字** (#0F172A): 主要文字，确保高可读性。
- **中灰次要文字** (#64748B): 次要信息、描述文字、标签。
- **浅灰辅助文字** (#94A3B8): 占位符、禁用状态、最次要信息。
- **柔和边框** (#E5E7EB): 分隔线、卡片边框、输入框边框。
- **强调边框** (#D1D5DB): 悬停状态边框、需要更多视觉重量的边框。
- **悬停背景** (#F1F5F9): 交互元素悬停状态、下拉菜单项悬停。
- **静音背景** (#F8FAFC): 图片占位、代码块背景、需要极轻微区分的区域。

### Semantic
- **成功绿** (#10B981): 成功状态、完成操作、正面反馈。
- **警告橙** (#F59E0B): 警告状态、需要注意的信息。
- **危险红** (#EF4444): 错误状态、删除操作、 destructive 操作。
- **信息蓝** (#0EA5E9): 信息提示、帮助文本、中性通知。

### Named Rules

**The 10% Accent Rule.** 主色调仅用于 ≤10% 的屏幕面积。它的稀缺性正是其价值所在——过多使用会削弱其引导作用。

**The Theme Harmony Rule.** 所有主题（浅色、深色、彩色）必须保持相同的色彩角色和层次结构，确保用户在切换主题时不会迷失。

## 3. Typography

**Display Font:** Inter (with HarmonyOS Sans SC, -apple-system, BlinkMacSystemFont, Segoe UI, PingFang SC, Hiragino Sans GB, Microsoft YaHei, sans-serif fallback)
**Body Font:** Inter (同上)
**Mono Font:** JetBrains Mono (with SF Mono, Menlo, Consolas, monospace fallback)

**Character:** Inter 字体家族提供清晰、现代、中性的气质，适合开发者工具和内容平台。中文字体回退确保在各种操作系统上都有良好的显示效果。

### Hierarchy
- **Display** (600, clamp(1.75rem, 4vw, 2.25rem), 1.3, -0.018em): 页面主标题、英雄区域标题。用于最重要的标题，吸引用户注意力。
- **Headline** (600, 22px, 1.3, -0.018em): 区域标题、卡片标题。用于二级标题，组织内容结构。
- **Title** (600, 18px, 1.3, -0.018em): 子区域标题、列表项标题。用于三级标题，细化内容层次。
- **Body** (400, 14px, 1.55, -0.005em): 正文内容、描述文字。最大行宽 65-75ch，确保舒适的阅读体验。
- **Label** (500, 13px, normal, normal): 标签、按钮文字、表单标签。用于次要信息和交互元素。

### Named Rules

**The Hierarchy Contrast Rule.** 标题层级通过字号 + 字重对比实现（≥1.25 的比例）。避免扁平的字号层级——层次感是可读性的基础。

**The Mono Restraint Rule.** 等宽字体仅用于代码、技术数据和需要精确对齐的场景。不要用于普通文本——它会降低阅读速度。

## 4. Elevation

设计系统采用轻微分层的高程策略。默认状态下，卡片和面板有微妙的阴影（shadow-xs），在交互时（hover、focus）阴影加深，营造轻微的深度感。这种分层帮助用户理解界面元素的层次关系，同时保持整体的简洁感。

### Shadow Vocabulary
- **xs** (`box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04)`): 卡片默认状态、静态元素。提供最轻微的深度感。
- **sm** (`box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06), 0 1px 2px rgba(15, 23, 42, 0.04)`): 输入框、按钮。比 xs 稍重，用于交互元素。
- **md** (`box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06), 0 2px 4px rgba(15, 23, 42, 0.04)`): 卡片悬停状态、浮动面板。用于强调交互反馈。
- **lg** (`box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08), 0 4px 8px rgba(15, 23, 42, 0.04)`): 下拉菜单、弹出框、模态框。用于脱离主内容流的元素。
- **xl** (`box-shadow: 0 20px 40px rgba(15, 23, 42, 0.10), 0 8px 16px rgba(15, 23, 42, 0.04)`): 对话框、重要模态。用于最高层级的浮层。

### Glass Effect
- **glass-bg** (`background: rgba(255, 255, 255, 0.72); backdrop-filter: blur(18px)`): 玻璃拟态效果，用于需要穿透背景的浮层。谨慎使用——过度使用会显得花哨。

### Named Rules

**The Interactive Depth Rule.** 静态元素使用 shadow-xs，交互元素在 hover 时升级到 shadow-md。深度变化是交互反馈的一部分，不是装饰。

**The Glass Restraint Rule.** 玻璃拟态仅用于需要穿透背景的场景（如浮动工具栏、搜索覆盖层）。不要用于普通卡片——它会降低可读性。

## 5. Components

### Buttons
- **Shape:** 中等圆角 (8px radius)
- **Primary:** 深邃靛蓝背景 (#4F46E5)，白色文字，10px 20px 内边距。用于主要操作（提交、确认、执行）。
- **Primary Hover:** 背景色加深 12%（color-mix），轻微上移 (translateY(-1px))，阴影加深。提供明确的交互反馈。
- **Default:** 白色背景，深灰文字，柔和边框。用于次要操作（取消、返回、辅助功能）。
- **Default Hover:** 背景变为悬停色 (#F1F5F9)，边框加深，文字变为主色。提供轻微的交互反馈。

### Cards / Containers
- **Corner Style:** 大圆角 (12px radius)
- **Background:** 纯白 (#FFFFFF)
- **Shadow Strategy:** 默认 shadow-xs，hover 时升级到 shadow-md
- **Border:** 1px 柔和边框 (#E5E7EB)
- **Internal Padding:** 20px 24px
- **Variants:** solid（默认）、gradient（渐变背景）、glass（玻璃拟态）、border（虚线边框）

### Inputs / Fields
- **Style:** 白色背景，柔和边框，中等圆角 (8px radius)
- **Focus:** 边框变为主色 (2px inset)，移除默认阴影。提供清晰的焦点状态。
- **Hover:** 边框颜色加深 (#D1D5DB)。提供轻微的交互提示。

### Tags / Chips
- **Style:** 主色浅背景 (#4F46E5 10% 透明度)，主色文字，小圆角 (6px radius)
- **Padding:** 4px 8px
- **Usage:** 分类标签、状态标记、筛选条件

### Navigation
- **Style:** 简洁清晰，使用主色表示当前页面
- **Typography:** 500 字重，14px 字号
- **States:** 默认使用次要文字色，hover 时变为主色，active 时有底部指示条
- **Mobile:** 响应式设计，在小屏幕上折叠为汉堡菜单

### Dropdown / Popover
- **Style:** 白色背景，柔和边框，大圆角 (12px radius)
- **Shadow:** lg 级别阴影，营造浮层感
- **Padding:** 4px 内边距，为菜单项提供呼吸空间
- **Items:** 小圆角 (6px radius)，hover 时背景变为悬停色，文字变为主色

## 6. Do's and Don'ts

### Do:
- **Do** 使用深邃靛蓝 (#4F46E5) 作为主要交互色，但仅用于 ≤10% 的屏幕面积
- **Do** 保持主题切换时的色彩角色一致性——主色永远是主色，无论在浅色还是深色主题
- **Do** 使用 Inter 字体家族的完整回退栈，确保跨平台显示一致性
- **Do** 为所有交互元素提供明确的 hover 和 focus 状态
- **Do** 尊重 `prefers-reduced-motion` 系统设置，为动画提供替代方案
- **Do** 使用 65-75ch 的最大行宽，确保舒适的阅读体验
- **Do** 通过字号 + 字重对比（≥1.25 比例）建立清晰的视觉层次
- **Do** 使用 Bento Grid 布局系统保持页面结构的一致性

### Don't:
- **Don't** 使用过于复杂的界面——功能堆砌、学习曲线陡峭、让用户迷失方向（PRODUCT.md 反面案例）
- **Don't** 过度设计——华而不实的动画、装饰性元素、影响性能和可用性（PRODUCT.md 反面案例）
- **Don't** 千篇一律——缺乏个性、没有特色、与其他平台雷同（PRODUCT.md 反面案例）
- **Don't** 视觉混乱——配色不一致、排版混乱、信息层级不清（PRODUCT.md 反面案例）
- **Don't** 在普通卡片上使用玻璃拟态效果——它会降低可读性
- **Don't** 使用等宽字体显示普通文本——它会降低阅读速度
- **Don't** 创造过于扁平的字号层级——层次感是可读性的基础
- **Don't** 忽略中文字体回退——确保在各种操作系统上都有良好的显示效果
- **Don't** 使用过于鲜艳或饱和的颜色作为大面积背景——它会造成视觉疲劳
- **Don't** 在没有明确用途的情况下添加动画——每个动画都应该有其存在的理由
