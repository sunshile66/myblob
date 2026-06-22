<template>
  <div class="auth-container">
    <div class="auth-shell">
      <aside class="auth-brand">
        <div class="brand-top">
          <div class="brand-logo">
            <span class="brand-mark">M</span>
            <span class="brand-name">MyBlob</span>
          </div>
          <p class="brand-tag">记录思考 · 分享创作</p>
        </div>
        <div class="brand-illu">
          <h2 class="brand-title">重置密码</h2>
          <p class="brand-desc">
            输入注册邮箱，我们将发送密码重置链接。
          </p>
        </div>
        <div class="brand-foot">
          <span>&copy; {{ year }} MyBlob</span>
          <router-link to="/" class="brand-link">返回首页</router-link>
        </div>
      </aside>

      <main class="auth-panel">
        <div class="panel-inner">
          <header class="panel-header">
            <h1>忘记密码</h1>
            <p>输入邮箱地址，获取重置链接</p>
          </header>

          <div v-if="!sent" class="form-wrapper">
            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              class="auth-form"
              label-position="top"
              @submit.prevent="handleSubmit"
            >
              <el-form-item label="注册邮箱" prop="email">
                <el-input
                  v-model="form.email"
                  placeholder="请输入注册时使用的邮箱"
                  size="large"
                  :prefix-icon="Message"
                  type="email"
                />
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  class="auth-button"
                  :loading="loading"
                  @click="handleSubmit"
                >
                  发送重置链接
                </el-button>
              </el-form-item>
            </el-form>

            <p class="panel-foot">
              <router-link to="/login" class="auth-link">返回登录</router-link>
            </p>
          </div>

          <div v-else class="success-state">
            <div class="success-icon">
              <el-icon :size="48" style="color: var(--theme-primary)"><Promotion /></el-icon>
            </div>
            <h2 class="success-title">邮件已发送</h2>
            <p class="success-desc">
              如果该邮箱已注册，您将收到一封包含密码重置链接的邮件。<br>
              请检查收件箱（以及垃圾邮件文件夹）。
            </p>
            <el-button round class="success-btn" @click="router.push('/login')">
              返回登录
            </el-button>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Message, Promotion } from "@element-plus/icons-vue";
import { forgotPassword } from "@/api/accounts";
import type { FormInstance, FormRules } from "element-plus";

const router = useRouter();
const year = new Date().getFullYear();
const formRef = ref<FormInstance>();
const loading = ref(false);
const sent = ref(false);

const form = reactive({
  email: "",
});

const rules: FormRules = {
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" },
  ],
};

const handleSubmit = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid) => {
    if (!valid) return;

    loading.value = true;
    try {
      await forgotPassword(form.email);
      sent.value = true;
    } catch (error: any) {
      ElMessage.error(error.message || "发送失败，请重试");
    } finally {
      loading.value = false;
    }
  });
};
</script>

<style scoped>
/* Reuse same styles as AuthPage */
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(1200px 600px at -10% -20%, color-mix(in srgb, var(--theme-secondary) 18%, transparent), transparent 60%),
    radial-gradient(900px 500px at 110% 110%, color-mix(in srgb, var(--theme-primary) 16%, transparent), transparent 60%),
    linear-gradient(180deg, var(--theme-background) 0%, color-mix(in srgb, var(--theme-primary) 6%, var(--theme-background)) 100%);
}

.auth-shell {
  width: 100%;
  max-width: 840px;
  min-height: 500px;
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-xl);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.08);
  overflow: hidden;
}

.auth-brand {
  position: relative;
  padding: 40px 44px;
  color: #ffffff;
  background:
    radial-gradient(600px 300px at 100% 0%, rgba(255, 255, 255, 0.18), transparent 60%),
    radial-gradient(500px 260px at 0% 100%, rgba(255, 255, 255, 0.12), transparent 60%),
    linear-gradient(160deg, color-mix(in srgb, var(--theme-primary) 85%, black) 0%, var(--theme-primary) 45%, color-mix(in srgb, var(--theme-primary) 70%, var(--theme-secondary)) 100%);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.auth-brand::after {
  content: "";
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.06) 1px, transparent 1px);
  background-size: 28px 28px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.5), transparent 70%);
  pointer-events: none;
}

.brand-top,
.brand-illu,
.brand-foot {
  position: relative;
  z-index: 1;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-mark {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
  color: #ffffff;
  backdrop-filter: blur(6px);
}

.brand-name {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.4px;
}

.brand-tag {
  margin: 14px 0 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.78);
}

.brand-title {
  margin: 0 0 14px;
  font-size: 28px;
  line-height: 1.25;
  font-weight: 700;
}

.brand-desc {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.82);
}

.brand-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.brand-link {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  border-bottom: 1px solid rgba(255, 255, 255, 0.4);
  padding-bottom: 1px;
  transition: opacity 0.2s ease;
}

.brand-link:hover {
  opacity: 0.85;
}

.auth-panel {
  padding: 44px 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.panel-inner {
  width: 100%;
  max-width: 380px;
}

.panel-header h1 {
  font-size: 24px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0 0 6px;
  letter-spacing: 0.2px;
}

.panel-header p {
  margin: 0 0 22px;
  font-size: 13px;
  color: var(--theme-text-secondary);
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.auth-form :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 500;
  color: var(--theme-text);
  padding-bottom: 6px;
  line-height: 1.4;
}

.auth-form :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  box-shadow: 0 0 0 1px var(--theme-border) inset;
  transition: box-shadow 0.18s ease;
}

.auth-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--theme-border-strong) inset;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--theme-primary) inset, 0 0 0 4px var(--theme-primary-light);
}

.auth-button {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  background: var(--theme-primary);
  border: none;
  border-radius: var(--radius-md);
  margin-top: 4px;
  transition: transform 0.18s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.auth-button:hover {
  background: color-mix(in srgb, var(--theme-primary) 85%, black);
  box-shadow: 0 8px 18px color-mix(in srgb, var(--theme-primary) 25%, transparent);
  transform: translateY(-1px);
}

.auth-button:active {
  transform: translateY(0);
}

.panel-foot {
  margin: 18px 0 0;
  text-align: center;
  font-size: 13px;
  color: var(--theme-text-secondary);
}

.auth-link {
  color: var(--theme-primary);
  cursor: pointer;
  font-weight: 500;
  text-decoration: none;
  transition: color 0.2s ease;
}

.auth-link:hover {
  color: color-mix(in srgb, var(--theme-primary) 80%, black);
  text-decoration: underline;
}

/* Success state */
.success-state {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  margin-bottom: 16px;
}

.success-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0 0 10px;
}

.success-desc {
  font-size: 13px;
  color: var(--theme-text-secondary);
  line-height: 1.6;
  margin: 0 0 24px;
}

.success-btn {
  font-size: 14px;
  font-weight: 500;
}

@media (max-width: 720px) {
  .auth-shell {
    grid-template-columns: 1fr;
    max-width: 460px;
    min-height: auto;
  }
  .auth-brand {
    padding: 32px;
  }
  .brand-illu {
    margin-top: 22px;
  }
  .brand-title {
    font-size: 26px;
  }
  .auth-panel {
    padding: 32px 28px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .auth-button,
  .brand-link {
    transition: none;
  }
  .auth-button:hover {
    transform: none;
  }
}
</style>
