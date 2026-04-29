import type { RouteRecordRaw } from "vue-router";

export const legalRoutes: RouteRecordRaw[] = [
  {
    path: "/user-agreement",
    name: "UserAgreement",
    component: () => import("@/pages/legal/UserAgreementPage.vue"),
    meta: { title: "用户协议" },
  },
  {
    path: "/privacy-policy",
    name: "PrivacyPolicy",
    component: () => import("@/pages/legal/PrivacyPolicyPage.vue"),
    meta: { title: "隐私政策" },
  },
];
