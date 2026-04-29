<template>
  <SimpleLayout>
    <div class="profile-edit-container">
      <div class="edit-header">
        <h1>编辑个人资料</h1>
        <p>完善你的个人信息，让更多人认识你</p>
      </div>

      <el-card class="edit-card" shadow="never">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="edit-form"
        >
          <div class="avatar-section">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :on-change="handleAvatarChange"
              :auto-upload="false"
              accept="image/*"
            >
              <div class="avatar-wrapper">
                <el-avatar :size="120" :src="form.avatar" class="avatar-preview">
                  {{ form.nickname?.charAt(0) || form.username?.charAt(0) || "U" }}
                </el-avatar>
                <div class="avatar-overlay">
                  <el-icon><Camera /></el-icon>
                  <span>更换头像</span>
                </div>
              </div>
            </el-upload>
          </div>

          <el-divider content-position="left">基本信息</el-divider>

          <el-form-item label="用户名">
            <el-input v-model="form.username" disabled class="disabled-input" />
          </el-form-item>

          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="请输入昵称" size="large" />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="form.email"
              type="email"
              placeholder="请输入邮箱"
              size="large"
            />
          </el-form-item>

          <el-form-item label="个人简介">
            <el-input
              v-model="form.bio"
              type="textarea"
              :rows="4"
              placeholder="介绍一下自己吧～"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-divider content-position="left">详细资料</el-divider>

          <div class="form-row">
            <el-form-item label="所在地">
              <el-input v-model="form.location" placeholder="请输入所在城市" size="large" />
            </el-form-item>

            <el-form-item label="职业">
              <el-input v-model="form.profession" placeholder="请输入职业" size="large" />
            </el-form-item>
          </div>

          <div class="form-actions">
            <el-button @click="handleCancel" size="large">取消</el-button>
            <el-button type="primary" :loading="saving" size="large" @click="handleSave">
              保存修改
            </el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Camera } from "@element-plus/icons-vue";
import { useUserStore } from "@/store/user";
import SimpleLayout from "@/layout/SimpleLayout.vue";
import type { FormInstance, FormRules } from "element-plus";
import type { User } from "@/types";

const router = useRouter();
const userStore = useUserStore();
const formRef = ref<FormInstance>();
const saving = ref(false);

const form = reactive<User>({
  id: 0,
  username: "",
  email: "",
  nickname: "",
  avatar: "",
  bio: "",
  website: "",
  company: "",
  profession: "",
  location: "",
  phone: "",
  wechat: "",
  qq: "",
  weibo: "",
});

const rules: FormRules = {
  nickname: [{ required: true, message: "请输入昵称", trigger: "blur" }],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" },
  ],
};

const handleAvatarChange = (file: any) => {
  if (!file.raw) return;
  
  const reader = new FileReader();
  reader.readAsDataURL(file.raw);
  reader.onload = (e) => {
    form.avatar = e.target?.result as string;
    if (userStore.userInfo) {
      userStore.setUserInfo({ ...userStore.userInfo, avatar: form.avatar });
    }
    ElMessage.success("头像已更新");
  };
};

const handleSave = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true;
      try {
        await new Promise((resolve) => setTimeout(resolve, 1000));
        
        if (userStore.userInfo) {
          userStore.setUserInfo({ ...userStore.userInfo, ...form });
        }
        
        ElMessage.success("保存成功");
        router.push("/profile");
      } catch (error) {
        ElMessage.error("保存失败");
      } finally {
        saving.value = false;
      }
    }
  });
};

const handleCancel = () => {
  router.push("/profile");
};

onMounted(() => {
  if (userStore.userInfo) {
    Object.assign(form, userStore.userInfo);
  }
});
</script>

<style scoped>
.profile-edit-container {
  max-width: 700px;
  margin: 0 auto;
  padding: 40px 20px;
}

.edit-header {
  text-align: center;
  margin-bottom: 40px;
}

.edit-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
}

.edit-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.edit-card {
  border-radius: 16px;
  border: none;
}

.edit-form {
  padding: 20px;
}

.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 40px;
}

.avatar-uploader .avatar-wrapper {
  position: relative;
  cursor: pointer;
}

.avatar-uploader .avatar-preview {
  border: 4px solid #f0f0f0;
  transition: all 0.3s ease;
}

.avatar-uploader .avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-uploader .avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-uploader .avatar-overlay .el-icon {
  font-size: 28px;
  margin-bottom: 4px;
}

.avatar-uploader .avatar-overlay span {
  font-size: 12px;
}

.disabled-input :deep(.el-input__wrapper) {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

:deep(.el-divider__text) {
  font-weight: 600;
  color: #666;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}
</style>
