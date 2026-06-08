/**
 * JSON 格式化 Web Worker
 * 将 JSON 解析和树构建移到后台线程，避免阻塞 UI
 */

export interface TreeRow {
  id: string
  parentId: string
  depth: number
  keyName: string
  path: string
  type: string
  preview: string
  meta: string
  expandable: boolean
  matched: boolean
}

export interface WorkerRequest {
  action: 'parse' | 'buildTree' | 'search' | 'minify' | 'sortKeys'
  payload: any
  requestId: string
}

export interface WorkerResponse {
  success: boolean
  data?: any
  error?: string
  requestId: string
}

// JSON 解析
function parseJson(input: string): any {
  const normalized = normalizeInput(input)
  return JSON.parse(normalized)
}

// 输入标准化
function normalizeInput(value: string): string {
  let text = value.trim()
  text = text.replace(/\t/gm, '    ')
  text = text.replace(/\bundefined\b/gm, 'null')
  text = text.replace(/ObjectId\((.*?)\)/gm, '$1')

  const firstObject = text.search(/[{[]/)
  const lastObject = Math.max(text.lastIndexOf('}'), text.lastIndexOf(']'))
  if (firstObject >= 0 && lastObject > firstObject) {
    text = text.slice(firstObject, lastObject + 1)
  }

  try {
    JSON.parse(text)
    return text
  } catch {
    return text
      .replace(/([{,]\s*)'([^']+)'\s*:/g, '$1"$2":')
      .replace(/:\s*'([^']*)'/g, ': "$1"')
      .replace(/\bTrue\b/g, 'true')
      .replace(/\bFalse\b/g, 'false')
      .replace(/\bNone\b/g, 'null')
  }
}

// 获取值类型
function getType(value: any): string {
  if (value === null) return 'null'
  if (Array.isArray(value)) return 'array'
  return typeof value
}

// 获取预览文本
function getPreview(value: any): string {
  if (value === null) return 'null'
  if (value === undefined) return 'undefined'
  if (typeof value === 'string') {
    return value.length > 100 ? value.slice(0, 100) + '...' : value
  }
  if (typeof value === 'number' || typeof value === 'boolean') return String(value)
  if (Array.isArray(value)) return `Array(${value.length})`
  if (typeof value === 'object') return `Object(${Object.keys(value).length})`
  return String(value)
}

// 获取元信息
function getMeta(value: any): string {
  if (Array.isArray(value)) return `${value.length} items`
  if (value && typeof value === 'object') return `${Object.keys(value).length} keys`
  return ''
}

// 生成子路径
function makeChildPath(parentPath: string, key: string, isArray: boolean): string {
  if (!parentPath) return key
  return isArray ? `${parentPath}[${key}]` : `${parentPath}.${key}`
}

// 获取可搜索文本
function getSearchText(value: any, keyName: string, path: string): string {
  const parts = [keyName, path]
  if (value !== null && value !== undefined) {
    if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
      parts.push(String(value))
    }
  }
  return parts.join(' ').toLowerCase()
}

// 构建树行
function buildTreeRows(data: any, keyword: string = '', maxRows: number = 3000): TreeRow[] {
  const rows: TreeRow[] = []

  const visit = (value: any, keyName: string, path: string, parentId: string, depth: number) => {
    if (rows.length >= maxRows) return

    const type = getType(value)
    const id = path || '__root__'
    const searchable = getSearchText(value, keyName, path)

    rows.push({
      id,
      parentId,
      depth,
      keyName,
      path,
      type,
      preview: getPreview(value),
      meta: getMeta(value),
      expandable: type === 'array' || type === 'object',
      matched: Boolean(keyword && searchable.includes(keyword))
    })

    if (Array.isArray(value)) {
      value.forEach((item, index) => {
        visit(item, '', makeChildPath(path, String(index), true), id, depth + 1)
      })
      return
    }

    if (value && typeof value === 'object') {
      Object.entries(value).forEach(([key, item]) => {
        visit(item, key, makeChildPath(path, key, false), id, depth + 1)
      })
    }
  }

  visit(data, '', '', '', 0)
  return rows
}

// 搜索树行
function searchTreeRows(rows: TreeRow[], keyword: string): TreeRow[] {
  const lowerKeyword = keyword.toLowerCase()
  return rows.filter(row => {
    const searchable = `${row.keyName} ${row.path} ${row.preview}`.toLowerCase()
    return searchable.includes(lowerKeyword)
  })
}

// 排序对象键
function sortObjectKeys(value: any): any {
  if (Array.isArray(value)) return value.map(sortObjectKeys)
  if (value && typeof value === 'object') {
    return Object.keys(value)
      .sort((a, b) => a.localeCompare(b))
      .reduce((result: any, key) => {
        result[key] = sortObjectKeys(value[key])
        return result
      }, {})
  }
  return value
}

// 压缩 JSON
function minifyJson(data: any): string {
  return JSON.stringify(data)
}

// 监听消息
self.onmessage = (e: MessageEvent<WorkerRequest>) => {
  const { action, payload, requestId } = e.data

  try {
    switch (action) {
      case 'parse': {
        const result = parseJson(payload)
        self.postMessage({ success: true, data: result, requestId })
        break
      }

      case 'buildTree': {
        const { data, keyword, maxRows } = payload
        const rows = buildTreeRows(data, keyword, maxRows)
        self.postMessage({ success: true, data: rows, requestId })
        break
      }

      case 'search': {
        const { rows, keyword } = payload
        const matches = searchTreeRows(rows, keyword)
        self.postMessage({ success: true, data: matches, requestId })
        break
      }

      case 'sortKeys': {
        const sorted = sortObjectKeys(payload)
        self.postMessage({ success: true, data: sorted, requestId })
        break
      }

      case 'minify': {
        const result = minifyJson(payload)
        self.postMessage({ success: true, data: result, requestId })
        break
      }

      default:
        self.postMessage({ success: false, error: `Unknown action: ${action}`, requestId })
    }
  } catch (error) {
    self.postMessage({
      success: false,
      error: error instanceof Error ? error.message : 'Unknown error',
      requestId
    })
  }
}
