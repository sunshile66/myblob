<template>
  <SimpleLayout>
    <div class="tool-page">
      <div class="tool-container">
        <div class="page-header">
          <h1>🔑 哈希生成器</h1>
          <p>MD5、SHA1、SHA256、SHA512</p>
        </div>

        <div class="tool-card">
          <div class="input-section">
            <label>输入文本</label>
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="4"
              placeholder="请输入要计算哈希的文本..."
            />
          </div>

          <el-button type="primary" size="large" @click="generateHash" class="generate-btn">
            生成哈希
          </el-button>

          <div class="results-section">
            <h3>哈希结果</h3>
            <div class="result-item">
              <div class="result-header">
                <span class="result-label">MD5</span>
                <el-button size="small" @click="copyResult('md5')" :icon="DocumentCopy">复制</el-button>
              </div>
              <el-input v-model="md5Hash" readonly />
            </div>
            <div class="result-item">
              <div class="result-header">
                <span class="result-label">SHA-1</span>
                <el-button size="small" @click="copyResult('sha1')" :icon="DocumentCopy">复制</el-button>
              </div>
              <el-input v-model="sha1Hash" readonly />
            </div>
            <div class="result-item">
              <div class="result-header">
                <span class="result-label">SHA-256</span>
                <el-button size="small" @click="copyResult('sha256')" :icon="DocumentCopy">复制</el-button>
              </div>
              <el-input v-model="sha256Hash" readonly />
            </div>
            <div class="result-item">
              <div class="result-header">
                <span class="result-label">SHA-512</span>
                <el-button size="small" @click="copyResult('sha512')" :icon="DocumentCopy">复制</el-button>
              </div>
              <el-input v-model="sha512Hash" readonly />
            </div>
          </div>
        </div>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { DocumentCopy } from '@element-plus/icons-vue'
import SimpleLayout from '@/layout/SimpleLayout.vue'
import { ElMessage } from 'element-plus'
import CryptoJS from 'crypto-js'

const inputText = ref('')
const md5Hash = ref('')
const sha1Hash = ref('')
const sha256Hash = ref('')
const sha512Hash = ref('')

const generateHash = () => {
  if (!inputText.value) {
    ElMessage.warning('请输入文本')
    return
  }

  md5Hash.value = CryptoJS.MD5(inputText.value).toString()
  sha1Hash.value = CryptoJS.SHA1(inputText.value).toString()
  sha256Hash.value = CryptoJS.SHA256(inputText.value).toString()
  sha512Hash.value = CryptoJS.SHA512(inputText.value).toString()
}

const copyResult = (type: string) => {
  let text = ''
  switch (type) {
    case 'md5':
      text = md5Hash.value
      break
    case 'sha1':
      text = sha1Hash.value
      break
    case 'sha256':
      text = sha256Hash.value
      break
    case 'sha512':
      text = sha512Hash.value
      break
  }

  if (text) {
    navigator.clipboard.writeText(text).then(() => {
      ElMessage.success('已复制到剪贴板')
    })
  }
}
</script>

<style scoped>
.tool-page {
  background: var(--theme-background);
  min-height: calc(100vh - 80px);
  padding: 40px 0;
}

.tool-container {
  max-width: 700px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 10px 0;
}

.page-header p {
  font-size: 15px;
  color: var(--theme-text-secondary);
  margin: 0;
}

.tool-card {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  padding: 32px;
  box-shadow: var(--glass-shadow);
}

.input-section {
  margin-bottom: 24px;
}

.input-section label {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: var(--theme-text);
  margin-bottom: 12px;
}

.generate-btn {
  width: 100%;
  font-weight: 600;
  margin-bottom: 24px;
}

.results-section h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0 0 16px 0;
}

.result-item {
  margin-bottom: 16px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.result-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--theme-text);
}

@media (max-width: 768px) {
  .tool-card {
    padding: 20px;
  }
}
</style>
