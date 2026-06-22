import type { RouteRecordRaw } from "vue-router";

export const contentRoutes: RouteRecordRaw[] = [
  {
    path: "/",
    name: "Home",
    component: () => import("@features/home/pages/HomePage.vue"),
    meta: { title: "首页" },
  },
  {
    path: "/note/:slug",
    name: "NoteDetail",
    component: () => import("@/pages/blog/SimpleNoteDetailPage.vue"),
    meta: { title: "笔记详情" },
  },
  {
    path: "/category/:slug",
    name: "Category",
    component: () => import("@/pages/blog/CategoryPage.vue"),
    meta: { title: "分类" },
  },
  {
    path: "/tag/:slug",
    name: "Tag",
    component: () => import("@/pages/blog/TagPage.vue"),
    meta: { title: "标签" },
  },
  {
    path: "/search",
    name: "Search",
    component: () => import("@/pages/search/SearchPage.vue"),
    meta: { title: "搜索" },
  },
  {
    path: "/photo-wall",
    name: "PhotoWall",
    component: () => import("@/pages/media/PhotoWallPage.vue"),
    meta: { title: "图片墙" },
  },
  {
    path: "/editor",
    name: "Editor",
    component: () => import("@/pages/blog/NoteEditorPage.vue"),
    meta: { title: "发布笔记" },
  },
  {
    path: "/post/create",
    name: "PostCreate",
    component: () => import("@/pages/blog/PostEditorPage.vue"),
    meta: { title: "写文章", requiresAuth: true },
  },
  {
    path: "/post/:slug/edit",
    name: "PostEdit",
    component: () => import("@/pages/blog/PostEditorPage.vue"),
    meta: { title: "编辑文章", requiresAuth: true },
  },
  {
    path: "/post/:slug",
    name: "PostDetail",
    component: () => import("@/pages/blog/PostDetailPage.vue"),
    meta: { title: "文章详情" },
  },
  {
    path: "/board",
    name: "Board",
    component: () => import("@/pages/interactions/BoardPage.vue"),
    meta: { title: "留言板" },
  },
  // 知识百科
  {
    path: "/knowledge",
    name: "KnowledgeHub",
    component: () => import("@/pages/knowledge/KnowledgeHub.vue"),
    meta: { title: "知识百科" },
  },
  {
    path: "/knowledge/vocabulary",
    name: "VocabularyList",
    component: () => import("@/pages/knowledge/VocabularyList.vue"),
    meta: { title: "英语词汇" },
  },
  {
    path: "/knowledge/vocabulary/quiz",
    name: "VocabularyQuiz",
    component: () => import("@/pages/knowledge/VocabularyQuiz.vue"),
    meta: { title: "词汇测验" },
  },
  {
    path: "/knowledge/vocabulary/:id",
    name: "VocabularyDetail",
    component: () => import("@/pages/knowledge/VocabularyDetail.vue"),
    meta: { title: "单词详情" },
  },
  {
    path: "/knowledge/articles",
    name: "EnglishArticles",
    component: () => import("@/pages/knowledge/EnglishArticles.vue"),
    meta: { title: "英语外刊" },
  },
  {
    path: "/knowledge/progress",
    name: "LearningProgress",
    component: () => import("@/pages/knowledge/LearningProgress.vue"),
    meta: { title: "学习进度", requiresAuth: true },
  },
  {
    path: "/knowledge/:category",
    name: "KnowledgeCards",
    component: () => import("@/pages/knowledge/KnowledgeCards.vue"),
    meta: { title: "知识卡片" },
  },
];
