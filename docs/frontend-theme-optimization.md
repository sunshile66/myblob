# 前端主题样式优化计划

## 问题诊断

### 1. 硬编码颜色（435+ 处）
- 组件中直接使用 `#333`、`#666`、`#999` 等硬编码颜色
- 未使用 CSS 变量系统
- 深色主题无法正常工作

### 2. 不一致的样式值
- 相同用途使用不同颜色值
- 缺乏统一的设计令牌系统

### 3. 缺少响应式设计检查
- 部分组件未测试移动端

---

## 优化方案

### Phase 1: 扩展 CSS 变量系统

在 `critical.css` 中添加完整的语义化变量：

```css
:root {
  /* 主色 */
  --theme-primary: #4F46E5;
  --theme-primary-light: rgba(79, 70, 229, 0.10);
  --theme-primary-hover: #4338CA;
  
  /* 中性色 */
  --theme-text: #0F172A;
  --theme-text-secondary: #64748B;
  --theme-text-tertiary: #94A3B8;
  --theme-text-inverse: #FFFFFF;
  
  /* 背景色 */
  --theme-background: #FAFAFB;
  --theme-card: #FFFFFF;
  --theme-hover: #F1F5F9;
  --theme-muted: #F8FAFC;
  
  /* 边框 */
  --theme-border: #E5E7EB;
  --theme-border-strong: #D1D5DB;
  
  /* 语义色 */
  --theme-success: #10B981;
  --theme-success-light: rgba(16, 185, 129, 0.10);
  --theme-warning: #F59E0B;
  --theme-warning-light: rgba(245, 158, 11, 0.10);
  --theme-danger: #EF4444;
  --theme-danger-light: rgba(239, 68, 68, 0.10);
  --theme-info: #0EA5E9;
  --theme-info-light: rgba(14, 165, 233, 0.10);
  
  /* 差异对比色 */
  --diff-added-bg: #dcfce7;
  --diff-added-text: #166534;
  --diff-removed-bg: #fee2e2;
  --diff-removed-text: #991b1b;
  --diff-modified-bg: #fdf6ec;
  --diff-modified-text: #92400e;
}
```

### Phase 2: 替换硬编码颜色

将所有硬编码颜色替换为 CSS 变量：

| 硬编码值 | 替换为 | 用途 |
|----------|--------|------|
| `#333` | `var(--theme-text)` | 主要文字 |
| `#666` | `var(--theme-text-secondary)` | 次要文字 |
| `#999` | `var(--theme-text-tertiary)` | 辅助文字 |
| `#f5f5f5` | `var(--theme-hover)` | 悬停背景 |
| `#f0f0f0` | `var(--theme-border)` | 边框 |
| `#fff` | `var(--theme-card)` | 卡片背景 |
| `#4F46E5` | `var(--theme-primary)` | 主色 |
| `#10B981` | `var(--theme-success)` | 成功状态 |
| `#EF4444` | `var(--theme-danger)` | 错误状态 |
| `#F59E0B` | `var(--theme-warning)` | 警告状态 |

### Phase 3: 添加深色主题变量

```css
.dark {
  --theme-text: #F1F5F9;
  --theme-text-secondary: #94A3B8;
  --theme-text-tertiary: #64748B;
  --theme-background: #0B0F19;
  --theme-card: #111827;
  --theme-hover: #1A2233;
  --theme-border: #1F2937;
  --theme-border-strong: #374151;
}
```

---

## 执行顺序

1. 扩展 critical.css 变量系统
2. 修复高优先级组件（Layout、Navigation、Cards）
3. 修复工具页面样式
4. 测试深色主题

---

## 验证清单

- [ ] 所有颜色使用 CSS 变量
- [ ] 深色主题正常工作
- [ ] 无硬编码颜色
- [ ] 响应式布局正常
