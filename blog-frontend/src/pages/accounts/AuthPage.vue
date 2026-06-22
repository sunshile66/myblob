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
          <h2 class="brand-title">欢迎回来。</h2>
          <p class="brand-desc">
            一个极简、专注、可扩展的内容与创作平台。
          </p>
          <ul class="brand-points">
            <li><span class="dot"></span>极简现代的阅读体验</li>
            <li><span class="dot"></span>多主题、多设备适配</li>
            <li><span class="dot"></span>RBAC 权限体系保护账号安全</li>
          </ul>
        </div>

        <div class="brand-foot">
          <span>© {{ year }} MyBlob</span>
          <router-link to="/" class="brand-link">返回首页</router-link>
        </div>
      </aside>

      <main class="auth-panel">
        <div class="panel-inner">
          <header class="panel-header">
            <h1>{{ activeTab === 'login' ? '登录账号' : '创建账号' }}</h1>
            <p>{{ activeTab === 'login' ? '使用账号密码继续' : '几秒钟完成注册，开启你的创作之旅' }}</p>
          </header>

          <el-tabs
            v-model="activeTab"
            class="auth-tabs"
            @tab-change="handleTabChange"
          >
            <el-tab-pane label="登录" name="login">
              <el-form
                ref="loginFormRef"
                :model="loginForm"
                :rules="loginRules"
                class="auth-form"
                label-position="top"
              >
                <el-form-item label="用户名 / 邮箱" prop="username">
                  <el-input
                    v-model="loginForm.username"
                    placeholder="请输入用户名或邮箱"
                    size="large"
                    :prefix-icon="User"
                  />
                </el-form-item>

                <el-form-item label="密码" prop="password">
                  <el-input
                    v-model="loginForm.password"
                    type="password"
                    placeholder="请输入密码"
                    size="large"
                    :prefix-icon="Lock"
                    show-password
                    @keyup.enter="handleLogin"
                  />
                </el-form-item>

                <div class="form-options">
                  <router-link to="/forgot-password" class="forgot-link">忘记密码？</router-link>
                </div>

                <el-form-item label="验证码" prop="code">
                  <div class="code-input-wrapper">
                    <el-input
                      v-model="loginForm.code"
                      placeholder="请输入 6 位验证码"
                      size="large"
                      :prefix-icon="Message"
                      style="flex: 1"
                      @keyup.enter="handleLogin"
                    />
                    <el-button
                      size="large"
                      class="code-btn"
                      :disabled="loginCodeCountdown > 0"
                      @click="handleSendLoginCode"
                    >
                      {{ loginCodeCountdown > 0 ? `${loginCodeCountdown}s` : '获取验证码' }}
                    </el-button>
                  </div>
                </el-form-item>

                <el-form-item>
                  <el-button
                    type="primary"
                    size="large"
                    class="auth-button"
                    :loading="loginLoading"
                    @click="handleLogin"
                  >
                    登录
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="注册" name="register">
              <el-form
                ref="registerFormRef"
                :model="registerForm"
                :rules="registerRules"
                class="auth-form"
                label-position="top"
              >
                <div class="form-grid">
                  <el-form-item label="用户名" prop="username">
                    <el-input
                      v-model="registerForm.username"
                      placeholder="请输入用户名"
                      size="large"
                      :prefix-icon="User"
                    />
                  </el-form-item>

                  <el-form-item label="昵称" prop="nickname">
                    <el-input
                      v-model="registerForm.nickname"
                      placeholder="请输入昵称"
                      size="large"
                      :prefix-icon="Avatar"
                    />
                  </el-form-item>
                </div>

                <el-form-item label="邮箱" prop="email">
                  <el-input
                    v-model="registerForm.email"
                    type="email"
                    placeholder="请输入邮箱"
                    size="large"
                    :prefix-icon="Message"
                  />
                </el-form-item>

                <div class="form-grid">
                  <el-form-item label="密码" prop="password">
                    <el-input
                      v-model="registerForm.password"
                      type="password"
                      placeholder="至少 6 位"
                      size="large"
                      :prefix-icon="Lock"
                      show-password
                    />
                  </el-form-item>

                  <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input
                      v-model="registerForm.confirmPassword"
                      type="password"
                      placeholder="请再次输入密码"
                      size="large"
                      :prefix-icon="Lock"
                      show-password
                    />
                  </el-form-item>
                </div>

                <el-form-item label="验证码" prop="code">
                  <div class="code-input-wrapper">
                    <el-input
                      v-model="registerForm.code"
                      placeholder="请输入验证码"
                      size="large"
                      :prefix-icon="Message"
                      style="flex: 1"
                      @keyup.enter="handleRegister"
                    />
                    <el-button
                      size="large"
                      class="code-btn"
                      :disabled="registerCodeCountdown > 0"
                      @click="handleSendRegisterCode"
                    >
                      {{ registerCodeCountdown > 0 ? `${registerCodeCountdown}s` : '获取验证码' }}
                    </el-button>
                  </div>
                </el-form-item>

                <el-form-item prop="agree" class="agree-item">
                  <el-checkbox v-model="registerForm.agree">
                    我已阅读并同意
                    <router-link to="/user-agreement" class="auth-link-inline">《用户协议》</router-link>
                    与
                    <router-link to="/privacy-policy" class="auth-link-inline">《隐私政策》</router-link>
                  </el-checkbox>
                </el-form-item>

                <el-form-item>
                  <el-button
                    type="primary"
                    size="large"
                    class="auth-button"
                    :loading="registerLoading"
                    @click="handleRegister"
                  >
                    创建账号
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>

          <p class="panel-foot">
            <template v-if="activeTab === 'login'">
              还没有账号？
              <a class="auth-link" @click.prevent="switchTab('register')">立即注册</a>
            </template>
            <template v-else>
              已有账号？
              <a class="auth-link" @click.prevent="switchTab('login')">返回登录</a>
            </template>
          </p>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock, Message, Avatar } from "@element-plus/icons-vue";
import { useUserStore } from "@/store/user";
import SecurityUtils from "@/utils/security";
import type { FormInstance, FormRules } from "element-plus";
import { login as loginApi, register as registerApi } from "@/api/accounts";
import { sendVerificationCode } from "@/api/core";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const activeTab = ref(route.path === "/register" ? "register" : "login");
const year = new Date().getFullYear();

onMounted(() => {
  activeTab.value = route.path === "/register" ? "register" : "login";
});

const switchTab = (tab: string) => {
  activeTab.value = tab;
  const target = tab === "register" ? "/register" : "/login";
  if (route.path !== target) {
    router.replace({ path: target, query: route.query });
  }
};

const loginLoading = ref(false);
const registerLoading = ref(false);
const loginCodeCountdown = ref(0);
const registerCodeCountdown = ref(0);
let loginTimer: number | null = null;
let registerTimer: number | null = null;

const loginFormRef = ref<FormInstance>();
const registerFormRef = ref<FormInstance>();

const loginForm = reactive({
  username: "",
  password: "",
  code: "",
});

const registerForm = reactive({
  username: "",
  email: "",
  nickname: "",
  password: "",
  confirmPassword: "",
  code: "",
  agree: false,
});

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === "") {
    callback(new Error("请再次输入密码"));
  } else if (value !== registerForm.password) {
    callback(new Error("两次输入密码不一致"));
  } else {
    callback();
  }
};

const loginRules: FormRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 2, max: 20, message: "长度在 2 到 20 个字符", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码长度不能少于 6 位", trigger: "blur" },
  ],
  code: [
    { required: true, message: "请输入验证码", trigger: "blur" },
    { len: 6, message: "验证码为6位数字", trigger: "blur" },
  ],
};

const registerRules: FormRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 2, max: 20, message: "长度在 2 到 20 个字符", trigger: "blur" },
  ],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" },
  ],
  nickname: [
    { required: true, message: "请输入昵称", trigger: "blur" },
    { min: 2, max: 20, message: "长度在 2 到 20 个字符", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码长度不能少于 6 位", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: "blur" },
  ],
  code: [
    { required: true, message: "请输入验证码", trigger: "blur" },
    { len: 6, message: "验证码为6位数字", trigger: "blur" },
  ],
  agree: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error("请先同意用户协议和隐私政策"));
        } else {
          callback();
        }
      },
      trigger: "change",
    },
  ],
};

const startLoginCountdown = () => {
  loginCodeCountdown.value = 5;
  loginTimer = window.setInterval(() => {
    loginCodeCountdown.value--;
    if (loginCodeCountdown.value <= 0) {
      if (loginTimer) clearInterval(loginTimer);
      // 开发模式：5s后自动填入万能验证码
      loginForm.code = "000000";
      ElMessage.success("🔓 开发模式：验证码已自动填入");
    }
  }, 1000);
};

const startRegisterCountdown = () => {
  registerCodeCountdown.value = 5;
  registerTimer = window.setInterval(() => {
    registerCodeCountdown.value--;
    if (registerCodeCountdown.value <= 0) {
      if (registerTimer) clearInterval(registerTimer);
      // 开发模式：5s后自动填入万能验证码
      registerForm.code = "000000";
      ElMessage.success("🔓 开发模式：验证码已自动填入");
    }
  }, 1000);
};

const handleSendLoginCode = async () => {
  if (!loginForm.username) {
    ElMessage.warning("请先输入用户名或邮箱");
    return;
  }

  try {
    await sendVerificationCode(loginForm.username, "email");
    ElMessage.success("验证码已发送！");
    startLoginCountdown();
  } catch (error) {
    ElMessage.error("发送失败，请重试");
  }
};

const handleSendRegisterCode = async () => {
  if (!registerForm.email) {
    ElMessage.warning("请先输入邮箱");
    return;
  }

  try {
    await sendVerificationCode(registerForm.email, "email");
    ElMessage.success("验证码已发送！");
    startRegisterCountdown();
  } catch (error) {
    ElMessage.error("发送失败，请重试");
  }
};

const handleTabChange = (tab: string) => {
  loginForm.username = "";
  loginForm.password = "";
  loginForm.code = "";
  registerForm.username = "";
  registerForm.email = "";
  registerForm.nickname = "";
  registerForm.password = "";
  registerForm.confirmPassword = "";
  registerForm.code = "";
  registerForm.agree = false;
  const target = tab === "register" ? "/register" : "/login";
  if (route.path !== target) {
    router.replace({ path: target, query: route.query });
  }
};

onUnmounted(() => {
  if (loginTimer) clearInterval(loginTimer);
  if (registerTimer) clearInterval(registerTimer);
});

const handleLogin = async () => {
  if (!loginFormRef.value) return;

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loginForm.username = SecurityUtils.sanitizeHTML(loginForm.username);

      loginLoading.value = true;
      try {
        const response = await loginApi({
          username: loginForm.username,
          password: loginForm.password,
          code: loginForm.code || undefined,
        });

        userStore.setToken(response.token);
        userStore.setUserInfo(response.user);

        ElMessage.success("登录成功！");
        const redirect = route.query.redirect as string;
        router.push(redirect || "/");
      } catch (error: any) {
        ElMessage.error(error.message || "登录失败，请重试");
      } finally {
        loginLoading.value = false;
      }
    }
  });
};

const handleRegister = async () => {
  if (!registerFormRef.value) return;

  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.error("两次输入的密码不一致");
    return;
  }

  if (registerForm.password.length < 6) {
    ElMessage.error("密码长度不能少于6位");
    return;
  }

  registerForm.username = SecurityUtils.sanitizeHTML(registerForm.username);

  registerLoading.value = true;
  try {
    await registerApi({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password,
      code: registerForm.code || undefined,
    });

    ElMessage.success("注册成功！请登录");
    activeTab.value = "login";
  } catch (error: any) {
    ElMessage.error(error.message || "注册失败，请重试");
  } finally {
    registerLoading.value = false;
  }
};
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(1200px 600px at -10% -20%, color-mix(in srgb, var(--theme-secondary) 18%, transparent), transparent 60%),
    radial-gradient(900px 500px at 110% 110%, color-mix(in srgb, var(--theme-primary) 16%, transparent), transparent 60%),
    linear-gradient(180deg, var(--theme-background) 0%, var(--theme-hover) 100%);
}

.auth-shell {
  width: 100%;
  max-width: 1040px;
  min-height: 620px;
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
}

/* 左侧品牌区 */
.auth-brand {
  position: relative;
  padding: 40px 44px;
  color: #ffffff;
  background:
    radial-gradient(600px 300px at 100% 0%, rgba(255, 255, 255, 0.18), transparent 60%),
    radial-gradient(500px 260px at 0% 100%, rgba(255, 255, 255, 0.12), transparent 60%),
    linear-gradient(160deg, color-mix(in srgb, var(--theme-primary) 90%, black) 0%, var(--theme-primary) 45%, var(--theme-secondary) 100%);
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
  font-size: 32px;
  line-height: 1.25;
  font-weight: 700;
  letter-spacing: 0.2px;
}

.brand-desc {
  margin: 0 0 22px;
  font-size: 14px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.82);
}

.brand-points {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.brand-points li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.18);
  flex-shrink: 0;
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

/* 右侧表单区 */
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
  color: #0f172a;
  margin: 0 0 6px;
  letter-spacing: 0.2px;
}

.panel-header p {
  margin: 0 0 22px;
  font-size: 13px;
  color: #64748b;
}

/* Tabs */
.auth-tabs :deep(.el-tabs__header) {
  margin-bottom: 18px;
}

.auth-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: var(--theme-border);
}

.auth-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 600;
  color: var(--theme-text-tertiary);
  padding: 0 18px;
  height: 38px;
  line-height: 38px;
}

.auth-tabs :deep(.el-tabs__item.is-active) {
  color: var(--theme-primary);
}

.auth-tabs :deep(.el-tabs__active-bar) {
  background: var(--theme-primary);
  height: 2px;
  border-radius: 2px;
}

/* Form */
.auth-form :deep(.el-form-item) {
  margin-bottom: 16px;
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

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.code-input-wrapper {
  display: flex;
  gap: 10px;
}

.code-btn {
  min-width: 116px;
  border-radius: var(--radius-md);
  font-weight: 500;
  border: 1px solid var(--theme-border);
  background: var(--theme-card);
  color: var(--theme-primary);
}

.code-btn:hover:not(:disabled) {
  border-color: color-mix(in srgb, var(--theme-primary) 40%, var(--theme-border));
  background: var(--theme-primary-light);
  color: color-mix(in srgb, var(--theme-primary) 80%, black);
}

.code-btn:disabled {
  color: var(--theme-text-tertiary);
  background: var(--theme-background);
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

.agree-item :deep(.el-checkbox__label) {
  font-size: 13px;
  color: #64748b;
}

.agree-item :deep(.el-checkbox__inner) {
  border-radius: 4px;
}

.agree-item :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: var(--theme-primary);
  border-color: var(--theme-primary);
}

/* Foot */
.panel-foot {
  margin: 18px 0 0;
  text-align: center;
  font-size: 13px;
  color: #64748b;
}

.form-options {
  display: flex;
  justify-content: flex-end;
  margin: -8px 0 6px;
}

.forgot-link {
  color: var(--theme-primary);
  font-size: 12px;
  font-weight: 500;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.2s ease;
}

.forgot-link:hover {
  color: color-mix(in srgb, var(--theme-primary) 80%, black);
  text-decoration: underline;
}

.auth-link {
  color: var(--theme-primary);
  cursor: pointer;
  font-weight: 500;
  text-decoration: none;
  margin-left: 4px;
  transition: color 0.2s ease;
}

.auth-link:hover {
  color: color-mix(in srgb, var(--theme-primary) 80%, black);
  text-decoration: underline;
}

.auth-link-inline {
  color: var(--theme-primary);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s ease;
}

.auth-link-inline:hover {
  color: color-mix(in srgb, var(--theme-primary) 80%, black);
  text-decoration: underline;
}

/* Responsive */
@media (max-width: 880px) {
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
  .form-grid {
    grid-template-columns: 1fr;
    gap: 0;
  }
}
</style>
