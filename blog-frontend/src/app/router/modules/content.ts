import type { RouteRecordRaw } from "vue-router";

export const contentRoutes: RouteRecordRaw[] = [
  {
    path: "/",
    name: "Home",
    component: () => import("@features/home/pages/HomePage.vue"),
    meta: { title: "首页" },
  },
  {
    path: "/full",
    name: "FullHome",
    component: () => import("@/pages/home/FullHomePage.vue"),
    meta: { title: "完整首页" },
  },
  {
    path: "/note/:slug",
    name: "NoteDetail",
    component: () => import("@/pages/note/SimpleNoteDetailPage.vue"),
    meta: { title: "笔记详情" },
  },
  {
    path: "/category/:slug",
    name: "Category",
    component: () => import("@/pages/CategoryPage.vue"),
    meta: { title: "分类" },
  },
  {
    path: "/tag/:slug",
    name: "Tag",
    component: () => import("@/pages/TagPage.vue"),
    meta: { title: "标签" },
  },
  {
    path: "/search",
    name: "Search",
    component: () => import("@features/search/pages/SearchPage.vue"),
    meta: { title: "搜索" },
  },
  {
    path: "/photo-wall",
    name: "PhotoWall",
    component: () => import("@/pages/PhotoWallPage.vue"),
    meta: { title: "图片墙" },
  },
  {
    path: "/editor",
    name: "Editor",
    component: () => import("@/pages/note/NoteEditorPage.vue"),
    meta: { title: "发布笔记" },
  },
];
