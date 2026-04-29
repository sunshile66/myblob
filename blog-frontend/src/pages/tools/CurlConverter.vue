<template>
  <SimpleLayout>
    <div class="curl-converter">
      <div class="tool-container">
        <div class="page-header">
          <el-button type="primary" link @click="router.back()" class="back-btn">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h1>curl转requests</h1>
        </div>

        <div class="tool-content">
          <div class="input-section">
            <h3>输入curl命令</h3>
            <el-input
              v-model="curlInput"
              type="textarea"
              :rows="10"
              placeholder="请输入curl命令，例如：
curl -X POST https://api.example.com/data \
  -H 'Content-Type: application/json' \
  -d '{\"key\": \"value\"}'"
            />
            <div class="button-group">
              <el-button type="primary" @click="convertCurl" class="action-btn">
                <el-icon><RefreshRight /></el-icon>
                转换
              </el-button>
              <el-button @click="clearInput" class="action-btn">
                清空
              </el-button>
            </div>
          </div>

          <div class="output-section">
            <h3>Python requests代码</h3>
            <div class="result-box">
              <div class="code-header">
                <span>python</span>
                <el-button @click="copyResult" class="copy-btn" size="small">
                  <el-icon><DocumentCopy /></el-icon>
                  复制代码
                </el-button>
              </div>
              <div class="code-wrapper">
                <pre><code>{{ pythonCode }}</code></pre>
              </div>
            </div>
          </div>
        </div>

        <div class="examples-section" v-if="examples.length > 0">
          <h3>示例</h3>
          <div class="examples-grid">
            <div v-for="(example, index) in examples" :key="index" class="example-card" @click="useExample(example)">
              <h4>{{ example.title }}</h4>
              <p>{{ example.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, DocumentCopy, RefreshRight } from '@element-plus/icons-vue'
import SimpleLayout from '@/layout/SimpleLayout.vue'

const router = useRouter()
const curlInput = ref('')
const pythonCode = ref('')

const examples = ref([
  {
    title: 'GET请求',
    description: '简单的GET请求示例',
    curl: `curl https://api.example.com/users`
  },
  {
    title: 'POST请求',
    description: '带JSON数据的POST请求',
    curl: `curl -X POST https://api.example.com/users \\
  -H 'Content-Type: application/json' \\
  -d '{"name": "John", "email": "john@example.com"}'`
  },
  {
    title: '带认证',
    description: '带Authorization头的请求',
    curl: `curl -X GET https://api.example.com/protected \\
  -H 'Authorization: Bearer your-token-here'`
  },
  {
    title: '文件上传',
    description: 'multipart/form-data文件上传',
    curl: `curl -X POST https://api.example.com/upload \\
  -F 'file=@/path/to/file.txt' \\
  -F 'description=My file'`
  }
])

const convertCurl = () => {
  if (!curlInput.value.trim()) {
    ElMessage.warning('请输入curl命令')
    return
  }

  try {
    pythonCode.value = parseCurl(curlInput.value)
    ElMessage.success('转换成功！')
  } catch (error) {
    ElMessage.error('转换失败，请检查curl命令格式')
    pythonCode.value = '# 转换失败，请检查curl命令格式'
  }
}

const parseCurl = (curlCmd: string): string => {
  let url = ''
  let method = 'GET'
  const headers: { [key: string]: string } = {}
  let data = ''
  let dataType = ''
  const files: { [key: string]: string } = {}

  const lines = curlCmd.replace(/\\\r?\n/g, ' ').split(/\s+(?=(?:[^"]*"[^"]*")*[^"]*$)/)

  let i = 0
  while (i < lines.length) {
    const token = lines[i].trim()
    if (!token || token === 'curl') {
      i++
      continue
    }

    if (token === '-X' || token === '--request') {
      method = lines[i + 1].toUpperCase()
      i += 2
    } else if (token === '-H' || token === '--header') {
      const header = lines[i + 1].replace(/^['"]|['"]$/g, '')
      const [key, value] = header.split(/:\s*/, 2)
      if (key && value) {
        headers[key] = value
      }
      i += 2
    } else if (token === '-d' || token === '--data' || token === '--data-raw' || token === '--data-binary') {
      data = lines[i + 1].replace(/^['"]|['"]$/g, '')
      dataType = 'json'
      i += 2
    } else if (token === '-F' || token === '--form') {
      const form = lines[i + 1].replace(/^['"]|['"]$/g, '')
      const [key, value] = form.split('=', 2)
      if (value && value.startsWith('@')) {
        files[key] = value.slice(1)
      }
      i += 2
    } else if (token.startsWith('http')) {
      url = token
      i++
    } else if (token.startsWith('-')) {
      i += 2
    } else {
      i++
    }
  }

  let code = 'import requests\n\n'

  if (Object.keys(headers).length > 0) {
    code += 'headers = {\n'
    for (const [key, value] of Object.entries(headers)) {
      code += `    '${key}': '${value}',\n`
    }
    code += '}\n\n'
  }

  if (data) {
    if (dataType === 'json') {
      code += `data = ${data}\n\n`
    } else {
      code += `data = '${data}'\n\n`
    }
  }

  if (Object.keys(files).length > 0) {
    code += 'files = {\n'
    for (const [key, value] of Object.entries(files)) {
      code += `    '${key}': open('${value}', 'rb'),\n`
    }
    code += '}\n\n'
  }

  code += `response = requests.${method.toLowerCase()}('${url}'`

  if (Object.keys(headers).length > 0) {
    code += ', headers=headers'
  }

  if (data && dataType === 'json') {
    code += ', json=data'
  } else if (data) {
    code += ', data=data'
  }

  if (Object.keys(files).length > 0) {
    code += ', files=files'
  }

  code += ')\n\n'
  code += 'print(response.status_code)\n'
  code += 'print(response.text)\n'

  return code
}

const clearInput = () => {
  curlInput.value = ''
  pythonCode.value = ''
}

const useExample = (example: any) => {
  curlInput.value = example.curl
  convertCurl()
}

const copyResult = () => {
  if (!pythonCode.value) {
    ElMessage.warning('没有内容可复制')
    return
  }
  navigator.clipboard.writeText(pythonCode.value)
  ElMessage.success('已复制到剪贴板')
}
</script>

<style scoped>
.curl-converter {
  background: var(--theme-background);
  min-height: calc(100vh - 80px);
  padding: 24px 0;
}

.tool-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
}

.back-btn {
  padding: 0;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 800;
  color: var(--theme-text);
  margin: 0;
}

.tool-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 32px;
}

.input-section,
.output-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.input-section h3,
.output-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--theme-text);
  margin: 0;
}

.button-group {
  display: flex;
  gap: 12px;
}

.action-btn {
  align-self: flex-start;
}

.result-box {
  background: #1e1e1e;
  border-radius: 12px;
  overflow: hidden;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #2d2d2d;
  border-bottom: 1px solid #404040;
}

.code-header span {
  color: #888;
  font-size: 12px;
  font-weight: 500;
}

.code-wrapper {
  flex: 1;
  overflow: auto;
  padding: 16px;
}

.code-wrapper pre {
  margin: 0;
}

.code-wrapper code {
  color: #d4d4d4;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

.examples-section h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--theme-text);
  margin: 0 0 16px 0;
}

.examples-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.example-card {
  background: var(--theme-card);
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.example-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  border-color: var(--theme-primary);
}

.example-card h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--theme-text);
  margin: 0 0 8px 0;
}

.example-card p {
  font-size: 14px;
  color: var(--theme-text-secondary);
  margin: 0;
}
</style>
