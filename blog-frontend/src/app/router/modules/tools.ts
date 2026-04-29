import type { RouteRecordRaw } from "vue-router";

export const toolRoutes: RouteRecordRaw[] = [
  {
    path: "/tools",
    name: "ToolsHome",
    component: () => import("@features/tools/pages/ToolsHomePage.vue"),
    meta: { title: "开发工具" },
  },
  {
    path: "/tools/image-editor",
    name: "ImageEditor",
    component: () => import("@/pages/tools/ImageEditorPro.vue"),
    meta: { title: "图片编辑器" },
  },
  {
    path: "/tools/encrypt",
    name: "EncryptTool",
    component: () => import("@features/tools/pages/EncryptToolPage.vue"),
    meta: { title: "加密解密" },
  },
  {
    path: "/tools/encode",
    name: "EncodeTool",
    component: () => import("@features/tools/pages/EncodeToolPage.vue"),
    meta: { title: "编码解码" },
  },
  {
    path: "/tools/json-formatter",
    name: "JsonFormatter",
    component: () => import("@features/tools/pages/JsonFormatterPage.vue"),
    meta: { title: "JSON格式化" },
  },
  {
    path: "/tools/uuid-generator",
    name: "UuidGenerator",
    component: () => import("@/pages/tools/UuidGenerator.vue"),
    meta: { title: "UUID生成器" },
  },
  {
    path: "/tools/timestamp-converter",
    name: "TimestampConverter",
    component: () => import("@features/tools/pages/TimestampConverterPage.vue"),
    meta: { title: "时间戳转换" },
  },
  {
    path: "/tools/color-picker",
    name: "ColorPicker",
    component: () => import("@/pages/tools/ColorPicker.vue"),
    meta: { title: "颜色拾取器" },
  },
  {
    path: "/tools/markdown-preview",
    name: "MarkdownPreview",
    component: () => import("@/pages/tools/MarkdownPreview.vue"),
    meta: { title: "Markdown 预览" },
  },
  {
    path: "/tools/password-generator",
    name: "PasswordGenerator",
    component: () => import("@/pages/tools/PasswordGenerator.vue"),
    meta: { title: "密码生成器" },
  },
  {
    path: "/tools/text-diff",
    name: "TextDiff",
    component: () => import("@/pages/tools/TextDiff.vue"),
    meta: { title: "文本对比" },
  },
  {
    path: "/tools/image-compressor",
    name: "ImageCompressor",
    component: () => import("@/pages/tools/ImageCompressor.vue"),
    meta: { title: "图片压缩" },
  },
  {
    path: "/tools/base-converter",
    name: "BaseConverter",
    component: () => import("@/pages/tools/BaseConverter.vue"),
    meta: { title: "进制转换" },
  },
  {
    path: "/tools/regex-tester",
    name: "RegexTester",
    component: () => import("@features/tools/pages/RegexTesterPage.vue"),
    meta: { title: "正则测试器" },
  },
  {
    path: "/tools/hash-generator",
    name: "HashGenerator",
    component: () => import("@/pages/tools/HashGenerator.vue"),
    meta: { title: "哈希生成器" },
  },
  {
    path: "/tools/curl-converter",
    name: "CurlConverter",
    component: () => import("@/pages/tools/CurlConverter.vue"),
    meta: { title: "curl 转 requests" },
  },
];
