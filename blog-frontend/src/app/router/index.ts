import { createRouter, createWebHistory } from "vue-router";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/store/user";
import { saveRecentTool } from "@features/tools/lib/recentTools";
import { contentRoutes } from "./modules/content";
import { legalRoutes } from "./modules/legal";
import { serviceRoutes } from "./modules/services";
import { toolRoutes } from "./modules/tools";
import { userRoutes } from "./modules/user";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    ...contentRoutes,
    ...userRoutes,
    ...toolRoutes,
    ...serviceRoutes,
    ...legalRoutes,
  ],
});

router.beforeEach(async (to, _from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - MyBlob` : "MyBlob";

  const userStore = useUserStore();
  const requiresAuth = to.matched.some((r) => r.meta?.requiresAuth);
  const requiresAdmin = to.matched.some((r) => r.meta?.requiresAdmin);

  if (!requiresAuth && !requiresAdmin) {
    return next();
  }

  if (!userStore.isLoggedIn) {
    ElMessage.warning("请先登录");
    return next({ path: "/login", query: { redirect: to.fullPath } });
  }

  if (requiresAdmin) {
    if (!userStore.userInfo) {
      await userStore.fetchUserInfo();
    }
    if (!userStore.isAdmin) {
      ElMessage.error("无管理员权限");
      return next({ path: "/" });
    }
  }

  next();
});

router.afterEach((to) => {
  const match = to.path.match(/^\/tools\/([^/]+)$/);
  if (match?.[1]) {
    saveRecentTool(match[1]);
  }
});

export default router;
