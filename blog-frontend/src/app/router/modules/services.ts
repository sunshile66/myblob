import type { RouteRecordRaw } from "vue-router";

export const serviceRoutes: RouteRecordRaw[] = [
  {
    path: "/filemanager",
    name: "FileManager",
    component: () => import("@/pages/filemanager/FileManager.vue"),
    meta: { title: "文件管理器" },
  },
  {
    path: "/apigateway",
    name: "APIGateway",
    component: () => import("@/pages/apigateway/APIGateway.vue"),
    meta: { title: "API服务平台" },
  },
  {
    path: "/membership",
    name: "MembershipCenter",
    component: () => import("@/pages/membership/MembershipCenter.vue"),
    meta: { title: "会员中心" },
  },
  {
    path: "/orders",
    name: "OrderList",
    component: () => import("@/pages/payments/OrderList.vue"),
    meta: { title: "我的订单" },
  },
  {
    path: "/wallet",
    name: "WalletCenter",
    component: () => import("@/pages/payments/WalletCenter.vue"),
    meta: { title: "我的钱包" },
  },
];
