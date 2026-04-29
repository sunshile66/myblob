<template>
  <SimpleLayout>
    <div class="tool-page">
      <div class="tool-container">
        <div class="page-header">
          <h1>🔢 进制转换</h1>
          <p>二进制、八进制、十进制、十六进制转换</p>
        </div>

        <div class="tool-card">
          <div class="input-section">
            <div class="input-group">
              <label>输入值</label>
              <el-input v-model="inputValue" placeholder="请输入数值" />
            </div>
            <div class="input-group">
              <label>原进制</label>
              <el-select v-model="fromBase" placeholder="选择原进制">
                <el-option label="二进制 (2)" :value="2" />
                <el-option label="八进制 (8)" :value="8" />
                <el-option label="十进制 (10)" :value="10" />
                <el-option label="十六进制 (16)" :value="16" />
              </el-select>
            </div>
          </div>

          <el-button type="primary" size="large" @click="convert" class="convert-btn">
            转换
          </el-button>

          <div class="results-section">
            <h3>转换结果</h3>
            <div class="result-item">
              <span class="result-label">二进制</span>
              <el-input v-model="binaryResult" readonly />
            </div>
            <div class="result-item">
              <span class="result-label">八进制</span>
              <el-input v-model="octalResult" readonly />
            </div>
            <div class="result-item">
              <span class="result-label">十进制</span>
              <el-input v-model="decimalResult" readonly />
            </div>
            <div class="result-item">
              <span class="result-label">十六进制</span>
              <el-input v-model="hexResult" readonly />
            </div>
          </div>
        </div>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import SimpleLayout from '@/layout/SimpleLayout.vue'

const inputValue = ref('')
const fromBase = ref(10)
const binaryResult = ref('')
const octalResult = ref('')
const decimalResult = ref('')
const hexResult = ref('')

const convert = () => {
  try {
    const num = parseInt(inputValue.value, fromBase.value)
    binaryResult.value = num.toString(2)
    octalResult.value = num.toString(8)
    decimalResult.value = num.toString(10)
    hexResult.value = num.toString(16).toUpperCase()
  } catch (e) {
    binaryResult.value = ''
    octalResult.value = ''
    decimalResult.value = ''
    hexResult.value = ''
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
  max-width: 600px;
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
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}

.input-group label {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: var(--theme-text);
  margin-bottom: 8px;
}

.convert-btn {
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
  margin-bottom: 12px;
}

.result-label {
  display: block;
  font-size: 14px;
  color: var(--theme-text-secondary);
  margin-bottom: 6px;
}

@media (max-width: 768px) {
  .input-section {
    grid-template-columns: 1fr;
  }

  .tool-card {
    padding: 20px;
  }
}
</style>
