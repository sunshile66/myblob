import request from "@/utils/request";
import type { Notification, ApiResponse } from "@/types";

export const getNotifications = () => {
  return request.get<ApiResponse<Notification>>("/interactions/notifications/");
};

export const markNotificationAsRead = (id: number) => {
  return request.patch(`/interactions/notifications/${id}/`, { is_read: true });
};

export const markAllNotificationsAsRead = () => {
  return request.post("/interactions/notifications/mark_all_read/");
};
