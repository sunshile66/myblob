export type ToolCategoryKey =
  | 'text'
  | 'security'
  | 'developer'
  | 'crawler'
  | 'image'

export type ToolIconKey =
  | 'Document'
  | 'Key'
  | 'Lock'
  | 'Ticket'
  | 'DocumentCopy'
  | 'Edit'
  | 'Picture'
  | 'Connection'
  | 'Search'
  | 'Timer'
  | 'Operation'
  | 'Brush'

export interface ToolCategory {
  key: ToolCategoryKey
  label: string
  description: string
}

export interface ToolItem {
  slug: string
  name: string
  description: string
  category: ToolCategoryKey
  icon: ToolIconKey
  accent: string
  tags: string[]
  featured?: boolean
  isNew?: boolean
}

export const TOOL_CATEGORIES: ToolCategory[] = [
  {
    key: 'text',
    label: '文本处理',
    description: '编码、格式化、预览与文本差异处理。',
  },
  {
    key: 'security',
    label: '安全与标识',
    description: '加密哈希、随机密码和唯一标识生成。',
  },
  {
    key: 'developer',
    label: '开发调试',
    description: '正则、时间、请求和数值转换等高频操作。',
  },
  {
    key: 'crawler',
    label: '爬虫辅助',
    description: 'Header、Cookie、URL、HTML 与代码片段处理。',
  },
  {
    key: 'image',
    label: '图片处理',
    description: '编辑、压缩与色彩相关工具。',
  },
]

export const TOOL_CATALOG: ToolItem[] = [
  {
    slug: 'json-formatter',
    name: 'JSON 格式化',
    description: '格式化、压缩并快速检查 JSON 结构。',
    category: 'text',
    icon: 'Document',
    accent: 'linear-gradient(135deg, #ff7a59 0%, #ffb36b 100%)',
    tags: ['格式化', '压缩', '校验'],
    featured: true,
  },
  {
    slug: 'encode',
    name: '编码解码',
    description: '处理 URL、HTML 与 Base64 的常见转换。',
    category: 'text',
    icon: 'DocumentCopy',
    accent: 'linear-gradient(135deg, #22c55e 0%, #06b6d4 100%)',
    tags: ['URL', 'HTML', 'Base64'],
  },
  {
    slug: 'markdown-preview',
    name: 'Markdown 预览',
    description: '实时查看 Markdown 渲染效果，适合写文档和说明。',
    category: 'text',
    icon: 'Document',
    accent: 'linear-gradient(135deg, #60a5fa 0%, #2563eb 100%)',
    tags: ['Markdown', '预览', '文档'],
  },
  {
    slug: 'text-diff',
    name: '文本对比',
    description: '对比两段文本的差异，快速检查修改内容。',
    category: 'text',
    icon: 'DocumentCopy',
    accent: 'linear-gradient(135deg, #f97316 0%, #ef4444 100%)',
    tags: ['Diff', '比对', '文本'],
  },
  {
    slug: 'json-diff',
    name: 'JSON 对比',
    description: '按字段路径对比两个 JSON，识别新增、删除和修改项。',
    category: 'text',
    icon: 'DocumentCopy',
    accent: 'linear-gradient(135deg, #0ea5e9 0%, #2563eb 100%)',
    tags: ['JSON', 'Diff', '接口'],
    featured: true,
    isNew: true,
  },
  {
    slug: 'encrypt',
    name: '加密解密',
    description: '快速计算 MD5、SHA 系列摘要与 Base64 转换。',
    category: 'security',
    icon: 'Lock',
    accent: 'linear-gradient(135deg, #4f46e5 0%, #ec4899 100%)',
    tags: ['MD5', 'SHA', '摘要'],
    featured: true,
  },
  {
    slug: 'hash-generator',
    name: '哈希生成器',
    description: '为文本生成常用哈希值，适合校验和签名场景。',
    category: 'security',
    icon: 'Key',
    accent: 'linear-gradient(135deg, #6366f1 0%, #ec4899 100%)',
    tags: ['SHA256', 'SHA512', '哈希'],
  },
  {
    slug: 'password-generator',
    name: '密码生成器',
    description: '生成高强度随机密码，可按长度和复杂度定制。',
    category: 'security',
    icon: 'Lock',
    accent: 'linear-gradient(135deg, #0f766e 0%, #14b8a6 100%)',
    tags: ['随机', '强密码', '安全'],
  },
  {
    slug: 'uuid-generator',
    name: 'UUID 生成器',
    description: '快速生成唯一标识符，适用于测试和占位场景。',
    category: 'security',
    icon: 'Ticket',
    accent: 'linear-gradient(135deg, #4f46e5 0%, #6366f1 100%)',
    tags: ['UUID', 'ID', '唯一值'],
  },
  {
    slug: 'regex-tester',
    name: '正则测试器',
    description: '测试表达式、查看匹配结果并预览替换输出。',
    category: 'developer',
    icon: 'Search',
    accent: 'linear-gradient(135deg, #ef4444 0%, #f59e0b 100%)',
    tags: ['正则', '匹配', '替换'],
    featured: true,
    isNew: true,
  },
  {
    slug: 'timestamp-converter',
    name: '时间戳转换',
    description: '在时间戳、日期时间和跨城市时区之间快速切换。',
    category: 'developer',
    icon: 'Timer',
    accent: 'linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%)',
    tags: ['时间', '时区', 'Unix'],
    featured: true,
  },
  {
    slug: 'curl-converter',
    name: 'curl 转代码',
    description: '将 curl 命令转换成 requests、fetch 和 feapder 片段。',
    category: 'crawler',
    icon: 'Connection',
    accent: 'linear-gradient(135deg, #0891b2 0%, #1d4ed8 100%)',
    tags: ['curl', 'requests', 'feapder'],
    featured: true,
  },
  {
    slug: 'code-formatter',
    name: 'JS/HTML 格式化',
    description: '格式化或压缩 JavaScript、HTML 和 CSS 片段。',
    category: 'crawler',
    icon: 'Document',
    accent: 'linear-gradient(135deg, #334155 0%, #0f172a 100%)',
    tags: ['JS', 'HTML', 'CSS'],
    featured: true,
    isNew: true,
  },
  {
    slug: 'header-formatter',
    name: 'Header 格式化',
    description: '把浏览器请求头转换成 JSON、Python dict 和 requests headers。',
    category: 'crawler',
    icon: 'DocumentCopy',
    accent: 'linear-gradient(135deg, #0f766e 0%, #22c55e 100%)',
    tags: ['Header', 'requests', '爬虫'],
    featured: true,
    isNew: true,
  },
  {
    slug: 'cookie-formatter',
    name: 'Cookie 格式化',
    description: '解析 Cookie 字符串，生成 JSON、dict 和可复用 Cookie header。',
    category: 'crawler',
    icon: 'Key',
    accent: 'linear-gradient(135deg, #f59e0b 0%, #ef4444 100%)',
    tags: ['Cookie', '登录态', '解析'],
    isNew: true,
  },
  {
    slug: 'dict-formatter',
    name: 'Dict 转换',
    description: '在 JSON、Python dict、键值对和 query 参数之间互转。',
    category: 'crawler',
    icon: 'Operation',
    accent: 'linear-gradient(135deg, #14b8a6 0%, #3b82f6 100%)',
    tags: ['Dict', 'JSON', 'Query'],
    isNew: true,
  },
  {
    slug: 'url-params',
    name: 'URL 参数提取',
    description: '拆解 URL、提取 query 参数，并生成请求参数对象。',
    category: 'crawler',
    icon: 'Connection',
    accent: 'linear-gradient(135deg, #2563eb 0%, #06b6d4 100%)',
    tags: ['URL', 'Query', 'Params'],
  },
  {
    slug: 'url-codec',
    name: 'URL 编解码',
    description: '对 URL、组件、Base64 与 HTML 实体进行批量编码解码。',
    category: 'crawler',
    icon: 'DocumentCopy',
    accent: 'linear-gradient(135deg, #7c3aed 0%, #db2777 100%)',
    tags: ['URL', 'Decode', 'Encode'],
  },
  {
    slug: 'html-renderer',
    name: 'HTML 渲染',
    description: '预览 HTML 片段，提取链接、图片和纯文本。',
    category: 'crawler',
    icon: 'Document',
    accent: 'linear-gradient(135deg, #ea580c 0%, #f97316 100%)',
    tags: ['HTML', '预览', '提取'],
  },
  {
    slug: 'text-decoder',
    name: '文本解码',
    description: '处理 Unicode 转义、十六进制、Base64 与 HTML 实体。',
    category: 'crawler',
    icon: 'Search',
    accent: 'linear-gradient(135deg, #475569 0%, #0f172a 100%)',
    tags: ['Unicode', 'Base64', 'Hex'],
  },
  {
    slug: 'code-share',
    name: '代码分享',
    description: '把短代码片段生成本地分享链接，适合临时传递示例。',
    category: 'crawler',
    icon: 'Ticket',
    accent: 'linear-gradient(135deg, #059669 0%, #84cc16 100%)',
    tags: ['代码', '分享', '片段'],
  },
  {
    slug: 'json-to-sql',
    name: 'JSON 转 SQL',
    description: '将对象数组生成 INSERT、UPDATE、UPSERT 或 CREATE TABLE。',
    category: 'developer',
    icon: 'Operation',
    accent: 'linear-gradient(135deg, #7c2d12 0%, #ea580c 100%)',
    tags: ['JSON', 'SQL', '批量'],
    featured: true,
    isNew: true,
  },
  {
    slug: 'base-converter',
    name: '进制转换',
    description: '支持二、八、十、十六进制的互相转换。',
    category: 'developer',
    icon: 'Operation',
    accent: 'linear-gradient(135deg, #059669 0%, #84cc16 100%)',
    tags: ['二进制', '十六进制', '数值'],
  },
  {
    slug: 'image-editor',
    name: '图片编辑器',
    description: '裁剪、标注和轻量编辑都能直接在浏览器中完成。',
    category: 'image',
    icon: 'Edit',
    accent: 'linear-gradient(135deg, #4f46e5 0%, #ec4899 100%)',
    tags: ['裁剪', '标注', '导出'],
    featured: true,
  },
  {
    slug: 'image-compressor',
    name: '图片压缩',
    description: '压缩图片体积，适合上传前快速优化。',
    category: 'image',
    icon: 'Picture',
    accent: 'linear-gradient(135deg, #14b8a6 0%, #22c55e 100%)',
    tags: ['压缩', '上传', '优化'],
  },
  {
    slug: 'base64-image',
    name: 'Base64 图片',
    description: '图片与 Base64/DataURL 双向转换，支持预览和下载。',
    category: 'image',
    icon: 'Picture',
    accent: 'linear-gradient(135deg, #0891b2 0%, #14b8a6 100%)',
    tags: ['Base64', 'DataURL', '图片'],
    featured: true,
    isNew: true,
  },
  {
    slug: 'color-picker',
    name: '颜色拾取器',
    description: '获取颜色值并在不同格式之间转换。',
    category: 'image',
    icon: 'Brush',
    accent: 'linear-gradient(135deg, #f43f5e 0%, #f97316 100%)',
    tags: ['颜色', 'HEX', 'RGB'],
  },
]

export const FEATURED_TOOLS = TOOL_CATALOG.filter((tool) => tool.featured)

export const findToolBySlug = (slug: string) =>
  TOOL_CATALOG.find((tool) => tool.slug === slug)
