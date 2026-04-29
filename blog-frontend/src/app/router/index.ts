import { createRouter, createWebHistory } from "vue-router";
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

router.beforeEach((to, _from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - MyBlob` : "MyBlob";
  next();
});

router.afterEach((to) => {
  const match = to.path.match(/^\/tools\/([^/]+)$/);
  if (match?.[1]) {
    saveRecentTool(match[1]);
  }
});

export default router;
