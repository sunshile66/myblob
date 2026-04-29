export type ToolCategoryKey =
  | 'text'
  | 'security'
  | 'developer'
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
    slug: 'encrypt',
    name: '加密解密',
    description: '快速计算 MD5、SHA 系列摘要与 Base64 转换。',
    category: 'security',
    icon: 'Lock',
    accent: 'linear-gradient(135deg, #7c3aed 0%, #db2777 100%)',
    tags: ['MD5', 'SHA', '摘要'],
    featured: true,
  },
  {
    slug: 'hash-generator',
    name: '哈希生成器',
    description: '为文本生成常用哈希值，适合校验和签名场景。',
    category: 'security',
    icon: 'Key',
    accent: 'linear-gradient(135deg, #8b5cf6 0%, #ec4899 100%)',
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
    accent: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
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
    name: 'curl 转 requests',
    description: '将 curl 命令转换成 Python requests 代码片段。',
    category: 'developer',
    icon: 'Connection',
    accent: 'linear-gradient(135deg, #0891b2 0%, #1d4ed8 100%)',
    tags: ['curl', 'Python', 'API'],
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
    accent: 'linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%)',
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
