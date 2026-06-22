<template>
  <div class="encrypt-panel">
    <div class="panel-grid">
      <IOPanel title="输入文本" clearable @clear="inputText = ''">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="4"
          resize="none"
          placeholder="输入要加密/解密的文本"
        />
      </IOPanel>
      <IOPanel title="输出结果" :content="outputText" copyable>
        <el-input
          v-model="outputText"
          type="textarea"
          :rows="4"
          resize="none"
          readonly
          placeholder="结果会显示在这里"
        />
      </IOPanel>
    </div>
    <ActionToolbar>
      <template #center>
        <el-select v-model="encryptType" size="small" style="width: 120px">
          <el-option label="RC4" value="rc4" />
          <el-option label="AES" value="aes" />
          <el-option label="DES" value="des" />
        </el-select>
        <el-input v-model="secretKey" placeholder="密钥" size="small" style="width: 150px" />
        <el-button type="primary" size="small" @click="encrypt">加密</el-button>
        <el-button size="small" @click="decrypt">解密</el-button>
      </template>
    </ActionToolbar>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import CryptoJS from 'crypto-js'
import IOPanel from '@/components/tools/IOPanel.vue'
import ActionToolbar from '@/components/tools/ActionToolbar.vue'

const inputText = ref('')
const outputText = ref('')
const encryptType = ref('rc4')
const secretKey = ref('')

const encrypt = () => {
  if (!inputText.value) return
  if (!secretKey.value) {
    ElMessage.warning('请输入密钥')
    return
  }
  try {
    switch (encryptType.value) {
      case 'rc4':
        outputText.value = CryptoJS.RC4.encrypt(inputText.value, secretKey.value).toString()
        break
      case 'aes':
        outputText.value = CryptoJS.AES.encrypt(inputText.value, secretKey.value).toString()
        break
      case 'des':
        outputText.value = CryptoJS.DES.encrypt(inputText.value, secretKey.value).toString()
        break
    }
  } catch (e: any) {
    ElMessage.error('加密失败: ' + e.message)
  }
}

const decrypt = () => {
  if (!inputText.value) return
  if (!secretKey.value) {
    ElMessage.warning('请输入密钥')
    return
  }
  try {
    let result
    switch (encryptType.value) {
      case 'rc4':
        result = CryptoJS.RC4.decrypt(inputText.value, secretKey.value)
        break
      case 'aes':
        result = CryptoJS.AES.decrypt(inputText.value, secretKey.value)
        break
      case 'des':
        result = CryptoJS.DES.decrypt(inputText.value, secretKey.value)
        break
    }
    outputText.value = result?.toString(CryptoJS.enc.Utf8) || '解密失败'
  } catch (e: any) {
    ElMessage.error('解密失败: ' + e.message)
  }
}
</script>

<style scoped>
.encrypt-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.panel-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
@media (max-width: 1024px) {
  .panel-grid {
    grid-template-columns: 1fr;
  }
}
</style>
