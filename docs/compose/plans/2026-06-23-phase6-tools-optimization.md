# Phase 6: 工具模块优化计划

## 目标
- 合并相关工具为工具包，减少页面数量
- 统一 UI 风格，提升交互体验
- 拆分大文件，提升可维护性
- 性能优化：懒加载、防抖、Web Worker

---

## 6.1 工具合并方案

### JSON Toolkit（合并 4 个工具）
**文件**: `pages/tools/JsonToolkit.vue`
**合并**: JsonFormatter + JsonDiff + JsonToSql + JsonToolbox
**结构**:
```
JsonToolkit/
├── JsonToolkit.vue        # 主页面（标签切换）
├── components/
│   ├── JsonFormatter.vue  # 格式化面板
│   ├── JsonDiff.vue       # 对比面板
│   ├── JsonToSql.vue      # SQL转换面板
│   └── JsonQuery.vue      # 查询面板
```

### Encoding Toolkit（合并 3 个工具）
**文件**: `pages/tools/EncodingToolkit.vue`
**合并**: EncodeTool + EncryptTool + HashGenerator
**结构**:
```
EncodingToolkit/
├── EncodingToolkit.vue    # 主页面
├── components/
│   ├── Encoder.vue        # URL/Base64/HTML 编码
│   ├── Encryptor.vue      # MD5/SHA/SM3/RC4
│   └── HashGenerator.vue  # 哈希生成
```

### Image Toolkit（合并 3 个工具）
**文件**: `pages/tools/ImageToolkit.vue`
**合并**: Base64ImageTool + ImageCompressor + ImageEditorPro
**结构**:
```
ImageToolkit/
├── ImageToolkit.vue       # 主页面
├── components/
│   ├── ImageCompressor.vue
│   ├── ImageEditor.vue
│   └── Base64Converter.vue
```

### Crawler Toolkit（保留，拆分组件）
**文件**: `pages/tools/CrawlerToolkit.vue` (837行)
**拆分**:
```
CrawlerToolkit/
├── CrawlerToolkit.vue     # 主页面（标签切换）
├── components/
│   ├── HeaderFormatter.vue
│   ├── CookieFormatter.vue
│   ├── DictFormatter.vue
│   ├── UrlParams.vue
│   ├── UrlCodec.vue
│   ├── HtmlRenderer.vue
│   └── TextDecoder.vue
```

---

## 6.2 大文件拆分

| 文件 | 行数 | 拆分方案 |
|------|------|----------|
| JsonFormatter.vue | 1217 | → JsonFormatter组件 + JsonTreeView组件 + SearchBar组件 |
| CurlConverter.vue | 908 | → CurlInput组件 + OutputPanel组件 + LanguageSelector |
| ColorPicker.vue | 732 | → ColorWheel组件 + ColorSliders组件 + ColorHistory |
| ImageEditorPro.vue | 711 | → Canvas组件 + Toolbar组件 + LayersPanel |
| ToolsHome.vue | 575 | → ToolCard组件 + CategoryFilter组件 + SearchBar |

---

## 6.3 UI 统一方案

### 通用组件（新建 `components/tools/`）
```
components/tools/
├── ToolLayout.vue         # 统一布局容器
├── IOPanel.vue            # 输入输出面板
├── CopyButton.vue         # 复制按钮（带反馈）
├── FormatSelector.vue     # 格式选择器
├── ActionToolbar.vue      # 操作工具栏
└── ResultDisplay.vue      # 结果展示
```

### 设计规范
- 统一使用 ToolLayout 作为页面容器
- 输入面板：左侧，可调整大小
- 输出面板：右侧，支持全屏
- 操作按钮：顶部工具栏
- 结果展示：支持树形/文本/表格切换

---

## 6.4 性能优化

### 懒加载
```typescript
// 重型依赖动态导入
const fabric = () => import('fabric')
const curlconverter = () => import('curlconverter')
```

### 防抖处理
```typescript
// 输入防抖 300ms
const debouncedFormat = useDebounceFn(formatJson, 300)
```

### Web Worker
```
workers/
├── jsonWorker.ts          # JSON 格式化/压缩
├── diffWorker.ts          # 文本对比
└── hashWorker.ts          # 哈希计算
```

---

## 执行顺序

1. **Task 1**: 创建通用工具组件 (`components/tools/`)
2. **Task 2**: 合并 JSON Toolkit
3. **Task 3**: 合并 Encoding Toolkit
4. **Task 4**: 合并 Image Toolkit
5. **Task 5**: 拆分 CrawlerToolkit
6. **Task 6**: 拆分其他大文件
7. **Task 7**: 性能优化（懒加载、防抖）
8. **Task 8**: 更新路由和目录
9. **Task 9**: 验证和测试

---

## 验证清单

- [ ] pnpm typecheck 通过
- [ ] pnpm build 成功
- [ ] 所有工具页面正常加载
- [ ] 工具切换流畅
- [ ] 复制功能正常
- [ ] 移动端响应式正常
