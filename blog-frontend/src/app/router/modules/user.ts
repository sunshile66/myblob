import type { RouteRecordRaw } from "vue-router";

export const userRoutes: RouteRecordRaw[] = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/pages/auth/AuthPage.vue"),
    meta: { title: "登录" },
  },
  {
    path: "/register",
    name: "Register",
    component: () => import("@/pages/auth/AuthPage.vue"),
    meta: { title: "注册" },
  },
  {
    path: "/forgot-password",
    name: "ForgotPassword",
    component: () => import("@/pages/auth/ForgotPasswordPage.vue"),
    meta: { title: "忘记密码" },
  },
  {
    path: "/reset-password",
    name: "ResetPassword",
    component: () => import("@/pages/auth/ResetPasswordPage.vue"),
    meta: { title: "重置密码" },
  },
  {
    path: "/profile",
    name: "Profile",
    component: () => import("@/pages/user/ProfilePage.vue"),
    meta: { title: "个人中心", requiresAuth: true },
  },
  {
    path: "/profile/edit",
    name: "ProfileEdit",
    component: () => import("@/pages/user/ProfileEditPage.vue"),
    meta: { title: "编辑个人资料", requiresAuth: true },
  },
  {
    path: "/my-posts",
    name: "MyPosts",
    component: () => import("@/pages/user/MyPostsPage.vue"),
    meta: { title: "我的笔记", requiresAuth: true },
  },
  {
    path: "/favorites",
    name: "Favorites",
    component: () => import("@/pages/user/FavoritesPage.vue"),
    meta: { title: "我的收藏", requiresAuth: true },
  },
  {
    path: "/settings/theme",
    name: "ThemeSettings",
    component: () => import("@/pages/settings/ThemeSettings.vue"),
    meta: { title: "主题设置", requiresAuth: true },
  },
  {
    path: "/settings",
    name: "Settings",
    component: () => import("@/pages/settings/ThemeSettings.vue"),
    meta: { title: "设置", requiresAuth: true },
  },
  {
    path: "/admin/users",
    name: "AdminUsers",
    component: () => import("@/pages/admin/UserListPage.vue"),
    meta: { title: "用户管理", requiresAuth: true, requiresAdmin: true },
  },
  {
    path: "/admin/site-config",
    name: "AdminSiteConfig",
    component: () => import("@/pages/admin/SiteConfigPage.vue"),
    meta: { title: "站点配置", requiresAuth: true, requiresAdmin: true },
  },
  {
    path: "/admin/ip-blocks",
    name: "AdminIPBlocks",
    component: () => import("@/pages/admin/SecurityIPBlocksPage.vue"),
    meta: { title: "IP 黑名单", requiresAuth: true, requiresAdmin: true },
  },
  {
    path: "/admin/request-logs",
    name: "AdminRequestLogs",
    component: () => import("@/pages/admin/SecurityRequestLogsPage.vue"),
    meta: { title: "请求日志", requiresAuth: true, requiresAdmin: true },
  },
  {
    path: "/api-keys",
    name: "ApiKeys",
    component: () => import("@/pages/apigateway/ApiKeyPage.vue"),
    meta: { title: "API 密钥", requiresAuth: true },
  },
  {
    path: "/media/upload",
    name: "MediaUpload",
    component: () => import("@/pages/media/MediaUploadPage.vue"),
    meta: { title: "媒体上传", requiresAuth: true },
  },
  {
    path: "/admin/news",
    name: "NewsDashboard",
    component: () => import("@/pages/admin/news/NewsDashboard.vue"),
    meta: { title: "新闻管理", requiresAuth: true, requiresAdmin: true },
  },
  {
    path: "/admin/news/sources",
    name: "NewsSourceManager",
    component: () => import("@/pages/admin/news/SourceManager.vue"),
    meta: { title: "新闻源管理", requiresAuth: true, requiresAdmin: true },
  },
  {
    path: "/admin/news/items",
    name: "NewsItemManager",
    component: () => import("@/pages/admin/news/ItemManager.vue"),
    meta: { title: "新闻内容管理", requiresAuth: true, requiresAdmin: true },
  },
];
