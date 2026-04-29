<template>
  <div class="register">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <span>注册</span>
        </div>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" type="email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称（可选）" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item v-if="securityConfig?.verification_code_enabled" label="验证码" prop="code">
          <el-input v-model="form.code" placeholder="请输入验证码" style="width: 200px; margin-right: 10px;" />
          <el-button 
            type="primary" 
            @click="handleSendCode" 
            :loading="sendingCode"
            :disabled="countdown > 0"
          >
            {{ countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading">
            注册
          </el-button>
          <router-link to="/login">已有账号？去登录</router-link>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { register, sendVerificationCode, getSecurityConfig, type SecurityConfig } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const securityConfig = ref<SecurityConfig | null>(null)

const form = reactive({
  username: '',
  email: '',
  nickname: '',
  password: '',
  phone_or_email: '',
  code: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const loadSecurityConfig = async () => {
  try {
    const config = await getSecurityConfig()
    securityConfig.value = config
  } catch (error) {
    console.error('Failed to load security config:', error)
  }
}

const handleSendCode = async () => {
  if (!form.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  
  sendingCode.value = true
  try {
    let codeType: 'sms' | 'email' | undefined = undefined
    if (securityConfig.value?.verification_code_type === 'email') {
      codeType = 'email'
    } else if (securityConfig.value?.verification_code_type === 'sms') {
      codeType = 'sms'
    }
    
    await sendVerificationCode({
      phone_or_email: form.email,
      type: codeType
    })
    
    form.phone_or_email = form.email
    ElMessage.success('验证码已发送，请查收')
    
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error('Failed to send verification code:', error)
  } finally {
    sendingCode.value = false
  }
}

const handleRegister = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await register(form)
        userStore.setToken(response.token)
        userStore.setUserInfo(response.user)
        ElMessage.success('注册成功')
        router.push('/')
      } catch (error) {
        console.error('Registration failed:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  loadSecurityConfig()
})
</script>

<style scoped>
.register {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.register-card {
  width: 100%;
  max-width: 500px;
}

.card-header {
  text-align: center;
  font-size: 20px;
  font-weight: bold;
}
</style>
