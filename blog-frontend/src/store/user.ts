import { defineStore, acceptHMRUpdate } from "pinia";
import { ref, computed } from "vue";
import { getUserProfile } from "@/api/accounts";
import type { User } from "@/types";

export const useUserStore = defineStore("user", () => {
  const token = ref<string | null>(localStorage.getItem("token") || null);
  const userInfo = ref<User | null>(null);
  const isLoading = ref(false);
  const lastFetchTime = ref<number>(0);
  const CACHE_DURATION = 5 * 60 * 1000;

  const isLoggedIn = computed(() => !!token.value);
  const userId = computed(() => userInfo.value?.id);
  const username = computed(() => userInfo.value?.username || "");
  const nickname = computed(() => userInfo.value?.nickname || userInfo.value?.username || "");
  const avatar = computed(() => userInfo.value?.avatar || "");
  const role = computed(() => userInfo.value?.role || "USER");
  const isAdmin = computed(
    () => userInfo.value?.role === "ADMIN" || userInfo.value?.is_superuser === true
  );

  const setToken = (newToken: string) => {
    token.value = newToken;
    localStorage.setItem("token", newToken);
  };

  const setUserInfo = (info: User) => {
    userInfo.value = info;
    lastFetchTime.value = Date.now();
  };

  const fetchUserInfo = async (force = false) => {
    if (!token.value) return null;

    if (!force && userInfo.value && Date.now() - lastFetchTime.value < CACHE_DURATION) {
      return userInfo.value;
    }

    isLoading.value = true;
    try {
      const user = await getUserProfile();
      setUserInfo(user);
      return user;
    } catch (error) {
      console.error("获取用户信息失败:", error);
      if ((error as any)?.response?.status === 401) {
        logout();
      }
      return null;
    } finally {
      isLoading.value = false;
    }
  };

  const logout = () => {
    token.value = null;
    userInfo.value = null;
    lastFetchTime.value = 0;
    localStorage.removeItem("token");
  };

  const initUser = async () => {
    if (token.value) {
      await fetchUserInfo();
    }
  };

  const updateUserInfo = (updates: Partial<User>) => {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...updates };
    }
  };

  return {
    token,
    userInfo,
    isLoading,
    isLoggedIn,
    userId,
    username,
    nickname,
    avatar,
    role,
    isAdmin,
    setToken,
    setUserInfo,
    fetchUserInfo,
    logout,
    initUser,
    updateUserInfo,
  };
});

// HMR 支持
if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useUserStore, import.meta.hot))
}
