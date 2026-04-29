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
    path: "/profile",
    name: "Profile",
    component: () => import("@/pages/user/ProfilePage.vue"),
    meta: { title: "个人中心" },
  },
  {
    path: "/profile/edit",
    name: "ProfileEdit",
    component: () => import("@/pages/user/ProfileEditPage.vue"),
    meta: { title: "编辑个人资料" },
  },
  {
    path: "/my-posts",
    name: "MyPosts",
    component: () => import("@/pages/user/MyPostsPage.vue"),
    meta: { title: "我的笔记" },
  },
  {
    path: "/favorites",
    name: "Favorites",
    component: () => import("@/pages/user/FavoritesPage.vue"),
    meta: { title: "我的收藏" },
  },
];
