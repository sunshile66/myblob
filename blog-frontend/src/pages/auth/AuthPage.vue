<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <div class="logo">
          <span class="logo-icon">📔</span>
          <span class="logo-text">小红书</span>
        </div>
        <p class="auth-subtitle">分享精彩生活</p>
      </div>

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
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
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
              />
            </el-form-item>

            <el-form-item label="验证码" prop="code">
              <div class="code-input-wrapper">
                <el-input
                  v-model="loginForm.code"
                  placeholder="请输入验证码"
                  size="large"
                  :prefix-icon="Message"
                  style="flex: 1"
                  @keyup.enter="handleLogin"
                />
                <el-button
                  size="large"
                  :disabled="loginCodeCountdown > 0"
                  @click="handleSendLoginCode"
                >
                  {{
                    loginCodeCountdown > 0
                      ? `${loginCodeCountdown}s`
                      : "获取验证码"
                  }}
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
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="registerForm.email"
                type="email"
                placeholder="请输入邮箱"
                size="large"
                :prefix-icon="Message"
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

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码（至少6位）"
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
                  :disabled="registerCodeCountdown > 0"
                  @click="handleSendRegisterCode"
                >
                  {{
                    registerCodeCountdown > 0
                      ? `${registerCodeCountdown}s`
                      : "获取验证码"
                  }}
                </el-button>
              </div>
            </el-form-item>

            <el-form-item prop="agree">
              <el-checkbox v-model="registerForm.agree" size="large">
                我已阅读并同意
                <router-link to="/user-agreement" class="auth-link-inline"
                  >用户协议</router-link
                >
                和
                <router-link to="/privacy-policy" class="auth-link-inline"
                  >隐私政策</router-link
                >
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
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <SocialLogin />

      <div class="auth-footer">
        <p>登录即表示同意</p>
        <div class="auth-links">
          <router-link to="/user-agreement" class="auth-link"
            >用户协议</router-link
          >
          <span class="auth-divider">·</span>
          <router-link to="/privacy-policy" class="auth-link"
            >隐私政策</router-link
          >
        </div>
      </div>
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
import type { User as UserType } from "@/types";
import SocialLogin from "@/components/common/SocialLogin.vue";
import { sendVerificationCode } from "@/api/core";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const activeTab = ref("login");

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
  loginCodeCountdown.value = 60;
  loginTimer = window.setInterval(() => {
    loginCodeCountdown.value--;
    if (loginCodeCountdown.value <= 0) {
      if (loginTimer) clearInterval(loginTimer);
    }
  }, 1000);
};

const startRegisterCountdown = () => {
  registerCodeCountdown.value = 60;
  registerTimer = window.setInterval(() => {
    registerCodeCountdown.value--;
    if (registerCodeCountdown.value <= 0) {
      if (registerTimer) clearInterval(registerTimer);
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
};

onUnmounted(() => {
  if (loginTimer) clearInterval(loginTimer);
  if (registerTimer) clearInterval(registerTimer);
});

const handleLogin = async () => {
  if (!loginFormRef.value) return;

  const rateLimitKey = `login_${loginForm.username}`;
  if (!SecurityUtils.checkRateLimit(rateLimitKey)) {
    ElMessage.error("登录尝试次数过多，请稍后再试");
    return;
  }

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loginForm.username = SecurityUtils.sanitizeHTML(loginForm.username);

      loginLoading.value = true;
      try {
        await new Promise((resolve) => setTimeout(resolve, 1000));

        const mockUser: UserType = {
          id: 1,
          username: loginForm.username,
          email: `${loginForm.username}@example.com`,
          nickname: loginForm.username,
          avatar: "",
        };

        userStore.setToken("mock-token-" + Date.now());
        userStore.setUserInfo(mockUser);

        ElMessage.success("登录成功！");
        router.push("/");
      } catch (error) {
        ElMessage.error("登录失败，请重试");
      } finally {
        loginLoading.value = false;
      }
    }
  });
};

const handleRegister = async () => {
  if (!registerFormRef.value) return;

  const usernameValidation = SecurityUtils.validateUsername(
    registerForm.username
  );
  if (!usernameValidation.valid) {
    ElMessage.warning(usernameValidation.message);
    return;
  }

  if (!SecurityUtils.validateEmail(registerForm.email)) {
    ElMessage.warning("请输入正确的邮箱格式");
    return;
  }

  const passwordValidation = SecurityUtils.validatePassword(
    registerForm.password
  );
  if (!passwordValidation.valid) {
    ElMessage.warning(passwordValidation.message);
    return;
  }

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      registerForm.username = SecurityUtils.sanitizeHTML(registerForm.username);
      registerForm.email = SecurityUtils.sanitizeHTML(registerForm.email);
      registerForm.nickname = SecurityUtils.sanitizeHTML(registerForm.nickname);

      registerLoading.value = true;
      try {
        await new Promise((resolve) => setTimeout(resolve, 1000));

        const mockUser: UserType = {
          id: Date.now(),
          username: registerForm.username,
          email: registerForm.email,
          nickname: registerForm.nickname,
          avatar: "",
        };

        userStore.setToken("mock-token-" + Date.now());
        userStore.setUserInfo(mockUser);

        ElMessage.success("注册成功！");
        router.push("/");
      } catch (error) {
        ElMessage.error("注册失败，请重试");
      } finally {
        registerLoading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  padding: 20px;
}

.auth-card {
  width: 100%;
  max-width: 420px;
  background: white;
  border-radius: 24px;
  padding: 40px 32px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 12px;
}

.logo-icon {
  font-size: 40px;
}

.logo-text {
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #ff2442 0%, #ff6a7f 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.auth-subtitle {
  font-size: 15px;
  color: #999;
  margin: 0;
}

.auth-tabs {
  margin-bottom: 8px;
}

.auth-tabs :deep(.el-tabs__header) {
  margin-bottom: 28px;
}

.auth-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.auth-tabs :deep(.el-tabs__item) {
  font-size: 18px;
  font-weight: 600;
  color: #999;
  padding: 0 24px;
}

.auth-tabs :deep(.el-tabs__item.is-active) {
  color: #ff2442;
}

.auth-tabs :deep(.el-tabs__active-bar) {
  background: #ff2442;
  height: 3px;
  border-radius: 2px;
}

.auth-form {
  margin-top: 24px;
}

.auth-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
  padding-bottom: 6px;
}

.auth-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #ff2442 0%, #ff6a7f 100%);
  border: none;
  border-radius: 24px;
  margin-top: 8px;
  transition: all 0.3s ease;
}

.auth-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 36, 66, 0.35);
}

.auth-button:active {
  transform: translateY(0);
}

.auth-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.auth-footer p {
  font-size: 13px;
  color: #999;
  margin: 0 0 8px 0;
}

.auth-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.auth-link {
  font-size: 13px;
  color: #ff2442;
  text-decoration: none;
  transition: color 0.2s ease;
}

.auth-link:hover {
  color: #ff6a7f;
}

.auth-divider {
  color: #ddd;
  font-size: 13px;
}

.code-input-wrapper {
  display: flex;
  gap: 12px;
}

.auth-link-inline {
  color: #ff2442;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s ease;
}

.auth-link-inline:hover {
  color: #ff6a7f;
}
</style>
