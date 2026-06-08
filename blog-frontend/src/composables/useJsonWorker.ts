import { ref, shallowRef } from 'vue'
import type { TreeRow } from '@/workers/jsonWorker'

/**
 * JSON Worker composable
 * 将 JSON 处理任务移到 Web Worker 中执行
 */
export function useJsonWorker() {
  const worker = shallowRef<Worker | null>(null)
  const isProcessing = ref(false)
  const requestId = ref(0)

  // 初始化 Worker
  const initWorker = () => {
    if (worker.value) return

    worker.value = new Worker(
      new URL('@/workers/jsonWorker.ts', import.meta.url),
      { type: 'module' }
    )
  }

  // 发送消息到 Worker
  const postMessage = <T>(action: string, payload: any): Promise<T> => {
    return new Promise((resolve, reject) => {
      if (!worker.value) {
        initWorker()
      }

      const currentRequestId = String(++requestId.value)
      isProcessing.value = true

      const handler = (e: MessageEvent) => {
        if (e.data.requestId === currentRequestId) {
          worker.value?.removeEventListener('message', handler)
          isProcessing.value = false

          if (e.data.success) {
            resolve(e.data.data)
          } else {
            reject(new Error(e.data.error))
          }
        }
      }

      worker.value!.addEventListener('message', handler)
      worker.value!.postMessage({ action, payload, requestId: currentRequestId })
    })
  }

  // 解析 JSON
  const parseJson = async (input: string): Promise<any> => {
    return postMessage('parse', input)
  }

  // 构建树行
  const buildTreeRows = async (data: any, keyword: string = '', maxRows: number = 3000): Promise<TreeRow[]> => {
    return postMessage('buildTree', { data, keyword, maxRows })
  }

  // 搜索树行
  const searchTreeRows = async (rows: TreeRow[], keyword: string): Promise<TreeRow[]> => {
    return postMessage('search', { rows, keyword })
  }

  // 排序对象键
  const sortObjectKeys = async (data: any): Promise<any> => {
    return postMessage('sortKeys', data)
  }

  // 压缩 JSON
  const minifyJson = async (data: any): Promise<string> => {
    return postMessage('minify', data)
  }

  // 销毁 Worker
  const destroyWorker = () => {
    if (worker.value) {
      worker.value.terminate()
      worker.value = null
    }
  }

  return {
    isProcessing,
    parseJson,
    buildTreeRows,
    searchTreeRows,
    sortObjectKeys,
    minifyJson,
    destroyWorker
  }
}
