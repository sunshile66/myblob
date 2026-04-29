<template>
  <div class="social-login">
    <div v-if="showTitle" class="divider">
      <span>其他登录方式</span>
    </div>
    <div class="social-buttons">
      <el-button
        v-for="app in apps"
        :key="app.id"
        class="social-btn"
        :type="getButtonType(app.provider)"
        circle
        @click="handleLogin(app)"
      >
        <el-icon :size="20">
          <component :is="getProviderIcon(app.provider)" />
        </el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  ChatDotRound,
  Promotion,
  Share,
  Iphone,
  Location,
  PhoneFilled,
} from "@element-plus/icons-vue";
import { getOAuthApps, oauthLogin, oauthCallback } from "@/api/social";
import { useUserStore } from "@/store/user";
import type { OAuthApp } from "@/types";

interface Props {
  showTitle?: boolean;
}

withDefaults(defineProps<Props>(), {
  showTitle: true,
});

const router = useRouter();
const userStore = useUserStore();
const apps = ref<OAuthApp[]>([]);
const loading = ref(false);

const getButtonType = (provider: string) => {
  const typeMap: Record<string, any> = {
    wechat: "success",
    qq: "primary",
    weibo: "danger",
    github: "",
    google: "warning",
    facebook: "primary",
    twitter: "primary",
  };
  return typeMap[provider] || "";
};

const getProviderIcon = (provider: string) => {
  const iconMap: Record<string, any> = {
    wechat: ChatDotRound,
    qq: Promotion,
    weibo: Share,
    github: Iphone,
    google: Location,
    facebook: PhoneFilled,
    twitter: Share,
  };
  return iconMap[provider] || ChatDotRound;
};

const loadApps = async () => {
  try {
    apps.value = await getOAuthApps();
  } catch (error) {
    console.error("Failed to load OAuth apps:", error);
  }
};

const handleLogin = async (app: OAuthApp) => {
  if (loading.value) return;

  loading.value = true;
  try {
    ElMessage.info(`正在跳转${app.provider_display}登录...`);

    const result = await oauthLogin(app.provider);
    console.log("OAuth login result:", result);

    const mockCode = "mock_code_" + Date.now();
    ElMessage.info(`模拟${app.provider_display}登录中...`);

    const userResult = await oauthCallback(app.provider, mockCode);

    userStore.setToken("oauth-token-" + Date.now());
    userStore.setUserInfo({
      id: userResult.user_id,
      username: userResult.username,
      nickname: userResult.nickname,
      avatar: userResult.avatar,
      email: userResult.email,
    });

    ElMessage.success(userResult.is_new ? "注册成功！" : "登录成功！");
    router.push("/");
  } catch (error: any) {
    console.error("OAuth login failed:", error);
    ElMessage.error(error.message || "登录失败，请重试");
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadApps();
});
</script>

<style scoped>
.social-login {
  width: 100%;
}

.divider {
  display: flex;
  align-items: center;
  margin: 24px 0;
  color: var(--theme-text-secondary);
  font-size: 14px;
}

.divider::before,
.divider::after {
  content: "";
  flex: 1;
  height: 1px;
  background: var(--theme-border);
}

.divider span {
  padding: 0 16px;
}

.social-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.social-btn {
  width: 48px;
  height: 48px;
}
</style>
