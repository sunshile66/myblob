<template>
  <SimpleLayout>
    <div class="image-editor">
      <!-- 顶部栏 -->
      <header class="editor-topbar">
        <div class="topbar-left">
          <h1>图片编辑器</h1>
          <span v-if="currentFileName" class="file-name">{{ currentFileName }}</span>
        </div>
        <div class="topbar-actions">
          <el-button @click="triggerUpload" type="primary">
            <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16"><path d="M3 4.25A2.25 2.25 0 015.25 2h5.5A2.25 2.25 0 0113 4.25v2a.75.75 0 01-1.5 0v-2a.75.75 0 00-.75-.75h-5.5a.75.75 0 00-.75.75v11.5c0 .414.336.75.75.75h5.5a.75.75 0 00.75-.75v-2a.75.75 0 011.5 0v2A2.25 2.25 0 0110.75 18h-5.5A2.25 2.25 0 013 15.75V4.25z"/><path d="M6 10a.75.75 0 01.75-.75h9.546l-1.048-.943a.75.75 0 111.004-1.114l2.5 2.25a.75.75 0 010 1.114l-2.5 2.25a.75.75 0 11-1.004-1.114l1.048-.943H6.75A.75.75 0 016 10z"/></svg>
            打开图片
          </el-button>
          <el-dropdown @command="handleExport" :disabled="!currentImage">
            <el-button type="success">
              导出 <svg viewBox="0 0 20 20" fill="currentColor" width="14" height="14" style="margin-left:4px"><path fill-rule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.25 4.5a.75.75 0 01-1.08 0l-4.25-4.5a.75.75 0 01.02-1.06z" clip-rule="evenodd"/></svg>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="png">PNG</el-dropdown-item>
                <el-dropdown-item command="jpeg">JPEG</el-dropdown-item>
                <el-dropdown-item command="webp">WebP</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <div class="editor-body">
        <!-- 左侧工具栏 -->
        <aside class="tool-sidebar">
          <button v-for="t in tools" :key="t.id" class="sidebar-btn" :class="{ active: activeTool === t.id }" :title="t.label" @click="setTool(t.id)">
            <span v-html="t.icon"></span>
            <small>{{ t.label }}</small>
          </button>
        </aside>

        <!-- 中间画布 -->
        <main class="canvas-main">
          <!-- 工具选项条 -->
          <div v-if="activeTool === 'brush' || activeTool === 'erase'" class="option-bar">
            <div class="opt-item">
              <label>大小</label>
              <el-slider v-model="brushSize" :min="1" :max="80" :show-tooltip="true" style="width: 140px" />
              <span class="opt-val">{{ brushSize }}px</span>
            </div>
            <div class="opt-item" v-if="activeTool === 'brush'">
              <label>颜色</label>
              <el-color-picker v-model="brushColor" />
            </div>
            <div class="opt-item" v-if="activeTool === 'brush'">
              <label>透明度</label>
              <el-slider v-model="brushOpacity" :min="0" :max="100" style="width: 100px" />
            </div>
          </div>
          <div v-if="activeTool === 'text'" class="option-bar">
            <div class="opt-item">
              <label>字号</label>
              <el-input-number v-model="textFontSize" :min="10" :max="200" size="small" />
            </div>
            <div class="opt-item">
              <label>颜色</label>
              <el-color-picker v-model="textColor" />
            </div>
            <div class="opt-item">
              <label>粗体</label>
              <el-switch v-model="textBold" />
            </div>
            <el-button size="small" type="primary" @click="addTextToCanvas">添加文字</el-button>
          </div>
          <div v-if="activeTool === 'crop'" class="option-bar">
            <el-button size="small" type="primary" @click="applyCrop" :disabled="!cropRect">确认裁剪</el-button>
            <el-button size="small" @click="cancelCrop" :disabled="!cropRect">取消</el-button>
            <span class="opt-hint">拖动选区后点击确认</span>
          </div>

          <!-- 画布容器 -->
          <div class="canvas-wrap" ref="canvasContainer" @click="handleCanvasClick">
            <div v-if="!currentImage" class="empty-canvas" @click="triggerUpload">
              <div class="empty-icon">
                <svg viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="8" y="12" width="48" height="40" rx="4" stroke="currentColor" stroke-width="2.5"/>
                  <circle cx="22" cy="28" r="5" stroke="currentColor" stroke-width="2"/>
                  <path d="M8 44l14-12 10 8 12-10 12 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              <h3>点击或拖拽图片开始编辑</h3>
              <p>支持 JPG · PNG · GIF · WebP</p>
            </div>
            <canvas v-else ref="canvasEl"></canvas>
          </div>

          <!-- 底部状态栏 -->
          <div v-if="currentImage" class="status-bar">
            <span>{{ imageWidth }} × {{ imageHeight }} px</span>
            <span>缩放 {{ zoomLevel }}%</span>
            <div class="zoom-controls">
              <button @click="zoomOut" title="缩小">−</button>
              <button @click="fitToScreen" title="适应">⊡</button>
              <button @click="zoomIn" title="放大">+</button>
            </div>
          </div>
        </main>

        <!-- 右侧面板 -->
        <aside class="props-panel" v-if="currentImage">
          <div class="panel-section">
            <h3>撤销 / 重做</h3>
            <div class="btn-row">
              <el-button size="small" @click="undo" :disabled="historyIndex <= 0">撤销</el-button>
              <el-button size="small" @click="redo" :disabled="historyIndex >= stateStack.length - 1">重做</el-button>
            </div>
          </div>

          <div class="panel-section">
            <h3>图片滤镜</h3>
            <div class="filter-item">
              <label>亮度</label>
              <el-slider v-model="filters.brightness" :min="-100" :max="100" @change="applyFilters" />
            </div>
            <div class="filter-item">
              <label>对比度</label>
              <el-slider v-model="filters.contrast" :min="-100" :max="100" @change="applyFilters" />
            </div>
            <div class="filter-item">
              <label>饱和度</label>
              <el-slider v-model="filters.saturation" :min="-100" :max="100" @change="applyFilters" />
            </div>
            <div class="filter-item">
              <label>模糊</label>
              <el-slider v-model="filters.blur" :min="0" :max="20" @change="applyFilters" />
            </div>
            <div class="btn-row" style="margin-top:8px">
              <el-button size="small" @click="resetFilters">重置滤镜</el-button>
            </div>
          </div>

          <div class="panel-section">
            <h3>快捷预设</h3>
            <div class="preset-grid">
              <button @click="applyPreset('grayscale')">黑白</button>
              <button @click="applyPreset('sepia')">复古</button>
              <button @click="applyPreset('invert')">反色</button>
              <button @click="applyPreset('warm')">暖色</button>
              <button @click="applyPreset('cool')">冷色</button>
              <button @click="applyPreset('vintage')">胶片</button>
            </div>
          </div>

          <div class="panel-section" v-if="activeObject">
            <h3>对象属性</h3>
            <div class="filter-item">
              <label>透明度</label>
              <el-slider v-model="objOpacity" :min="0" :max="100" @input="updateObjOpacity" />
            </div>
            <div class="btn-row">
              <el-button size="small" type="danger" @click="deleteActiveObject">删除</el-button>
              <el-button size="small" @click="bringForward">上移</el-button>
              <el-button size="small" @click="sendBackward">下移</el-button>
            </div>
          </div>
        </aside>
      </div>

      <!-- 隐藏 input -->
      <input type="file" ref="fileInput" accept="image/*" style="display:none" @change="handleFileSelect" />
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as fabric from 'fabric'
import SimpleLayout from '@/layout/SimpleLayout.vue'

// -- refs --
const canvasContainer = ref<HTMLDivElement | null>(null)
const canvasEl = ref<HTMLCanvasElement | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)
let fc: fabric.Canvas | null = null

// -- state --
const currentImage = ref<fabric.FabricImage | null>(null)
const currentFileName = ref('')
const imageWidth = ref(0)
const imageHeight = ref(0)
const zoomLevel = ref(100)
const activeTool = ref('select')
const activeObject = ref<fabric.FabricObject | null>(null)
const objOpacity = ref(100)

// tools
const brushSize = ref(12)
const brushColor = ref('#e74c3c')
const brushOpacity = ref(100)
const textFontSize = ref(32)
const textColor = ref('#1f2937')
const textBold = ref(false)

// crop
const cropRect = ref<fabric.Rect | null>(null)

// filters
const filters = reactive({ brightness: 0, contrast: 0, saturation: 0, blur: 0 })

// history
const stateStack = ref<string[]>([])
const historyIndex = ref(-1)

const tools = [
  { id: 'select', label: '选择', icon: '<svg viewBox="0 0 20 20" fill="currentColor" width="20" height="20"><path d="M6.5 2a.75.75 0 00-.728.544l-4 14a.75.75 0 001.154.758l4.72-3.305 2.634 3.161a.75.75 0 001.28-.577l-.765-4.614 5.453-.793a.75.75 0 00.31-1.354L6.842 1.45A.75.75 0 006.5 2z"/></svg>' },
  { id: 'crop', label: '裁剪', icon: '<svg viewBox="0 0 20 20" fill="currentColor" width="20" height="20"><path fill-rule="evenodd" d="M4 2.75A1.25 1.25 0 015.25 1.5h.5a.75.75 0 010 1.5h-.5v13.25h13.25v-.5a.75.75 0 011.5 0v.5A1.25 1.25 0 0118.75 17.5H5.25A1.25 1.25 0 014 16.25V2.75zM7.25 6A1.25 1.25 0 006 7.25v9.5c0 .69.56 1.25 1.25 1.25h9.5A1.25 1.25 0 0018 16.75v-9.5A1.25 1.25 0 0016.75 6h-9.5z" clip-rule="evenodd"/></svg>' },
  { id: 'brush', label: '画笔', icon: '<svg viewBox="0 0 20 20" fill="currentColor" width="20" height="20"><path d="M5.43 13.39a4.5 4.5 0 01-.93-1.197c-.376-.7-.5-1.394-.5-1.693 0-.993.389-1.988 1.22-2.82L10.22 2.68a2.25 2.25 0 013.182 0l3.918 3.918a2.25 2.25 0 010 3.182l-5 5a4.486 4.486 0 01-2.82 1.22c-.299 0-.993-.124-1.693-.5a4.5 4.5 0 01-1.197-.93 4.5 4.5 0 01-.18-.18z"/></svg>' },
  { id: 'erase', label: '橡皮', icon: '<svg viewBox="0 0 20 20" fill="currentColor" width="20" height="20"><path fill-rule="evenodd" d="M13.904 2.34a3 3 0 014.243 0l.513.513a3 3 0 010 4.243L8.19 17.565a3.75 3.75 0 01-2.652 1.099H2.75a.75.75 0 010-1.5h2.788a2.25 2.25 0 001.59-.659L17.13 6.51l-.513-.513a1.5 1.5 0 00-2.121 0l-6.5 6.5a.75.75 0 11-1.06-1.06l6.5-6.5z" clip-rule="evenodd"/></svg>' },
  { id: 'text', label: '文字', icon: '<svg viewBox="0 0 20 20" fill="currentColor" width="20" height="20"><path fill-rule="evenodd" d="M4.75 2a.75.75 0 01.75.75V4h9V2.75a.75.75 0 011.5 0v1.5h1.25a.75.75 0 010 1.5H16v11.5a.75.75 0 01-1.5 0V5.75h-9v11.5a.75.75 0 01-1.5 0V5.75H2.75a.75.75 0 010-1.5H4V2.75A.75.75 0 014.75 2z" clip-rule="evenodd"/></svg>' },
  { id: 'rect', label: '矩形', icon: '<svg viewBox="0 0 20 20" fill="currentColor" width="20" height="20"><path fill-rule="evenodd" d="M3.75 3A.75.75 0 003 3.75v12.5c0 .414.336.75.75.75h12.5a.75.75 0 00.75-.75V3.75a.75.75 0 00-.75-.75H3.75zM1.5 3.75A2.25 2.25 0 013.75 1.5h12.5A2.25 2.25 0 0118.5 3.75v12.5a2.25 2.25 0 01-2.25 2.25H3.75A2.25 2.25 0 011.5 16.25V3.75z" clip-rule="evenodd"/></svg>' },
  { id: 'circle', label: '圆形', icon: '<svg viewBox="0 0 20 20" fill="currentColor" width="20" height="20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM3.5 10a6.5 6.5 0 1113 0 6.5 6.5 0 01-13 0z" clip-rule="evenodd"/></svg>' },
  { id: 'line', label: '直线', icon: '<svg viewBox="0 0 20 20" fill="currentColor" width="20" height="20"><path fill-rule="evenodd" d="M17.75 2.25a.75.75 0 010 1.06L3.31 17.75a.75.75 0 01-1.06-1.06L16.69 2.25a.75.75 0 011.06 0z" clip-rule="evenodd"/></svg>' },
  { id: 'arrow', label: '箭头', icon: '<svg viewBox="0 0 20 20" fill="currentColor" width="20" height="20"><path fill-rule="evenodd" d="M2 10a.75.75 0 01.75-.75h12.59l-2.1-1.95a.75.75 0 111.02-1.1l3.5 3.25a.75.75 0 010 1.1l-3.5 3.25a.75.75 0 11-1.02-1.1l2.1-1.95H2.75A.75.75 0 012 10z" clip-rule="evenodd"/></svg>' },
]

// ---- functions ----
const triggerUpload = () => fileInput.value?.click()

const handleFileSelect = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file || !file.type.startsWith('image/')) { ElMessage.error('请选择图片文件'); return }
  currentFileName.value = file.name
  const reader = new FileReader()
  reader.onload = (ev) => {
    const img = new Image()
    img.onload = () => loadImageToCanvas(img)
    img.src = ev.target?.result as string
  }
  reader.readAsDataURL(file)
  ;(e.target as HTMLInputElement).value = ''
}

const loadImageToCanvas = (img: HTMLImageElement) => {
  if (!canvasContainer.value) return
  const MAX = { w: 1400, h: 900 }
  let w = img.naturalWidth, h = img.naturalHeight
  if (w > MAX.w || h > MAX.h) {
    const s = Math.min(MAX.w / w, MAX.h / h)
    w = Math.round(w * s); h = Math.round(h * s)
  }
  imageWidth.value = img.naturalWidth
  imageHeight.value = img.naturalHeight

  if (fc) fc.dispose()
  fc = new fabric.Canvas(canvasEl.value!, {
    width: canvasContainer.value.clientWidth,
    height: Math.max(canvasContainer.value.clientHeight - 40, 500),
    backgroundColor: '#f1f5f9',
  })

  const fImg = new fabric.FabricImage(img)
  fImg.scaleToWidth(w)
  fImg.scaleToHeight(h)
  fc.add(fImg)
  fc.centerObject(fImg)
  fImg.selectable = false
  fImg.evented = false
  currentImage.value = fImg

  zoomLevel.value = 100
  stateStack.value = []
  historyIndex.value = -1
  saveState()

  fc.on('selection:created', onSelectionChange)
  fc.on('selection:updated', onSelectionChange)
  fc.on('selection:cleared', () => { activeObject.value = null })
  fc.on('object:modified', () => saveState())
}

const onSelectionChange = (opt: any) => {
  activeObject.value = opt.selected?.[0] || null
  if (activeObject.value) {
    objOpacity.value = Math.round((activeObject.value.opacity ?? 1) * 100)
  }
}

// ---- tools ----
const setTool = (id: string) => {
  activeTool.value = id
  if (!fc) return

  // reset drawing mode
  fc.isDrawingMode = false
  fc.selection = true
  fc.forEachObject(o => { o.selectable = true; o.evented = true })
  if (currentImage.value) { currentImage.value.selectable = false; currentImage.value.evented = false }
  removeCropRect()

  if (id === 'select') {
    fc.selection = true
  } else if (id === 'brush') {
    fc.isDrawingMode = true
    fc.freeDrawingBrush = new fabric.PencilBrush(fc)
    fc.freeDrawingBrush.color = brushColor.value
    fc.freeDrawingBrush.width = brushSize.value
  } else if (id === 'erase') {
    fc.isDrawingMode = true
    fc.freeDrawingBrush = new fabric.PencilBrush(fc)
    fc.freeDrawingBrush.color = '#f1f5f9'
    fc.freeDrawingBrush.width = brushSize.value
  } else if (id === 'crop') {
    fc.selection = false
    addCropOverlay()
  } else if (id === 'rect') {
    addShape('rect')
  } else if (id === 'circle') {
    addShape('circle')
  } else if (id === 'line') {
    addShape('line')
  } else if (id === 'arrow') {
    addShape('arrow')
  }
}

const addTextToCanvas = () => {
  if (!fc) return
  const text = new fabric.IText('双击编辑', {
    left: 100, top: 100,
    fontSize: textFontSize.value,
    fill: textColor.value,
    fontWeight: textBold.value ? 'bold' : 'normal',
    fontFamily: 'system-ui, sans-serif',
  })
  fc.add(text)
  fc.setActiveObject(text)
  saveState()
}

const addShape = (type: string) => {
  if (!fc) return
  let obj: fabric.FabricObject
  const fill = brushColor.value
  if (type === 'rect') {
    obj = new fabric.Rect({ left: 120, top: 120, width: 140, height: 100, fill, stroke: '#333', strokeWidth: 1 })
  } else if (type === 'circle') {
    obj = new fabric.Circle({ left: 120, top: 120, radius: 60, fill, stroke: '#333', strokeWidth: 1 })
  } else if (type === 'line') {
    obj = new fabric.Line([100, 150, 300, 150], { stroke: fill, strokeWidth: 3 })
  } else if (type === 'arrow') {
    const line = new fabric.Line([100, 150, 280, 150], { stroke: fill, strokeWidth: 3 })
    const head = new fabric.Triangle({ left: 265, top: 135, width: 30, height: 30, fill, angle: 90 })
    const group = new fabric.Group([line, head])
    fc.add(group)
    fc.setActiveObject(group)
    saveState()
    return
  } else return
  fc.add(obj)
  fc.setActiveObject(obj)
  saveState()
}

// ---- crop ----
const addCropOverlay = () => {
  if (!fc || !currentImage.value) return
  const bounds = currentImage.value.getBoundingRect()
  const rect = new fabric.Rect({
    left: bounds.left + 20, top: bounds.top + 20,
    width: bounds.width - 40, height: bounds.height - 40,
    fill: 'rgba(59,130,246,0.12)', stroke: '#3b82f6', strokeWidth: 2,
    strokeDashArray: [6, 4], cornerColor: '#3b82f6', cornerSize: 10,
    transparentCorners: false, hasRotatingPoint: false, lockRotation: true,
  })
  fc.add(rect)
  fc.setActiveObject(rect)
  cropRect.value = rect
}
const removeCropRect = () => {
  if (cropRect.value && fc) { (fc as any).remove(cropRect.value); cropRect.value = null }
}
const applyCrop = () => {
  if (!fc || !cropRect.value || !currentImage.value) return
  const r = cropRect.value
  const left = r.left!; const top = r.top!; const w = r.width! * r.scaleX!; const h = r.height! * r.scaleY!
  removeCropRect()

  // crop via canvas toDataURL
  const dataUrl = (fc as any).toDataURL({ left, top, width: w, height: h, format: 'png', multiplier: 1 })
  const img = new Image()
  img.onload = () => {
    fc!.clear()
    const fImg = new fabric.FabricImage(img)
    fc!.add(fImg)
    fc!.centerObject(fImg)
    fImg.selectable = false; fImg.evented = false
    currentImage.value = fImg
    imageWidth.value = Math.round(w); imageHeight.value = Math.round(h)
    saveState()
    setTool('select')
    ElMessage.success('裁剪完成')
  }
  img.src = dataUrl
}
const cancelCrop = () => { removeCropRect(); setTool('select') }

// ---- filters ----
const applyFilters = () => {
  if (!currentImage.value) return
  const f = [] as any[]
  if (filters.brightness !== 0) f.push(new fabric.filters.Brightness({ brightness: filters.brightness / 100 }))
  if (filters.contrast !== 0) f.push(new fabric.filters.Contrast({ contrast: filters.contrast / 100 }))
  if (filters.saturation !== 0) f.push(new fabric.filters.Saturation({ saturate: filters.saturation / 100 }))
  if (filters.blur > 0) f.push(new fabric.filters.Blur({ blur: filters.blur / 20 }))
  currentImage.value.filters = f
  currentImage.value.applyFilters()
  fc?.requestRenderAll()
  saveState()
}

const resetFilters = () => {
  filters.brightness = 0; filters.contrast = 0; filters.saturation = 0; filters.blur = 0
  applyFilters()
}

const applyPreset = (preset: string) => {
  resetFiltersSilent()
  switch (preset) {
    case 'grayscale': filters.saturation = -100; break
    case 'sepia': filters.saturation = -60; filters.brightness = 10; filters.contrast = 10; break
    case 'invert': /* need special filter */ break
    case 'warm': filters.brightness = 8; filters.saturation = 20; break
    case 'cool': filters.brightness = -5; filters.saturation = -20; break
    case 'vintage': filters.saturation = -40; filters.contrast = 15; filters.brightness = 5; break
  }
  applyFilters()
}
const resetFiltersSilent = () => {
  filters.brightness = 0; filters.contrast = 0; filters.saturation = 0; filters.blur = 0
}

// ---- object properties ----
const updateObjOpacity = (v: number | number[]) => {
  const val = Array.isArray(v) ? v[0] : v
  if (activeObject.value) { activeObject.value.opacity = val / 100; fc?.requestRenderAll() }
}
const deleteActiveObject = () => {
  if (activeObject.value && fc) { (fc as any).remove(activeObject.value); activeObject.value = null; saveState() }
}
const bringForward = () => {
  if (activeObject.value && fc) { (fc as any).bringObjectForward(activeObject.value); fc.requestRenderAll() }
}
const sendBackward = () => {
  if (activeObject.value && fc) { (fc as any).sendObjectBackwards(activeObject.value); fc.requestRenderAll() }
}

// ---- history ----
const saveState = () => {
  if (!fc) return
  const json = JSON.stringify(fc.toJSON())
  stateStack.value = stateStack.value.slice(0, historyIndex.value + 1)
  stateStack.value.push(json)
  // 限制30步内存内，超出后移除最旧的而非全部保留
  if (stateStack.value.length > 30) {
    const removed = stateStack.value.shift()
    if (historyIndex.value > 0) historyIndex.value--
    // 将旧状态压缩存入 IndexedDB（异步，不阻塞）
    saveToIndexedDB(removed!).catch(() => {})
  }
  historyIndex.value = stateStack.value.length - 1
}

// IndexedDB 兜底存储，避免大图编辑时内存爆炸
const DB_NAME = 'myblob-image-history'
const openDB = (): Promise<IDBDatabase> => new Promise((resolve, reject) => {
  const req = indexedDB.open(DB_NAME, 1)
  req.onupgradeneeded = () => { req.result.createObjectStore('states') }
  req.onsuccess = () => resolve(req.result)
  req.onerror = () => reject(req.error)
})
const saveToIndexedDB = async (json: string) => {
  const db = await openDB()
  const tx = db.transaction('states', 'readwrite')
  tx.objectStore('states').put({ id: Date.now(), data: json, ts: Date.now() })
}
const loadFromIndexedDB = async (): Promise<string[]> => {
  try {
    const db = await openDB()
    return new Promise((resolve) => {
      const tx = db.transaction('states', 'readonly')
      const req = tx.objectStore('states').getAll()
      req.onsuccess = () => {
        resolve(req.result.sort((a:any,b:any) => a.ts - b.ts).map((r:any) => r.data))
        // 使用完后清理
        const delTx = db.transaction('states', 'readwrite')
        delTx.objectStore('states').clear()
      }
    })
  } catch { return [] }
}

const undo = () => {
  if (historyIndex.value <= 0 || !fc) return
  historyIndex.value--
  loadState(stateStack.value[historyIndex.value])
}
const redo = () => {
  if (historyIndex.value >= stateStack.value.length - 1 || !fc) return
  historyIndex.value++
  loadState(stateStack.value[historyIndex.value])
}
const loadState = (json: string) => {
  if (!fc) return
  fc.loadFromJSON(json).then(() => {
    fc!.requestRenderAll()
    // re-find background image
    const objs = fc!.getObjects()
    const bg = objs.find(o => !o.selectable && !o.evented) as fabric.FabricImage | undefined
    if (bg) currentImage.value = bg
  })
}

// ---- zoom ----
const zoomIn = () => { if (!fc) return; zoomLevel.value = Math.min(zoomLevel.value + 10, 400); fc.setZoom(zoomLevel.value / 100) }
const zoomOut = () => { if (!fc) return; zoomLevel.value = Math.max(zoomLevel.value - 10, 20); fc.setZoom(zoomLevel.value / 100) }
const fitToScreen = () => { if (!fc) return; zoomLevel.value = 100; fc.setZoom(1) }

// ---- export ----
const handleExport = (format: string) => {
  if (!fc) return
  const fmt = format as any
  const url = (fc as any).toDataURL({ format: fmt, quality: 0.92, multiplier: 1 })
  const a = document.createElement('a')
  a.download = `edited-${Date.now()}.${format}`
  a.href = url
  a.click()
  ElMessage.success(`已导出为 ${format.toUpperCase()}`)
}

// ---- canvas click (color picker) ----
const handleCanvasClick = (_e: MouseEvent) => {
  // no-op for now
}

// ---- keyboard ----
const onKey = (e: KeyboardEvent) => {
  if (e.ctrlKey || e.metaKey) {
    if (e.key === 'z') { e.preventDefault(); undo() }
    if (e.key === 'y') { e.preventDefault(); redo() }
    if (e.key === 's') { e.preventDefault(); handleExport('png') }
    if (e.key === 'o') { e.preventDefault(); triggerUpload() }
  }
  if (e.key === 'Delete' || e.key === 'Backspace') {
    if (activeObject.value && fc && !(activeObject.value instanceof fabric.IText && (activeObject.value as fabric.IText).isEditing)) {
      deleteActiveObject()
    }
  }
}

// ---- drag drop ----
const onDrop = (e: DragEvent) => {
  e.preventDefault()
  const file = e.dataTransfer?.files[0]
  if (file && file.type.startsWith('image/')) {
    currentFileName.value = file.name
    const reader = new FileReader()
    reader.onload = (ev) => {
      const img = new Image()
      img.onload = () => loadImageToCanvas(img)
      img.src = ev.target?.result as string
    }
    reader.readAsDataURL(file)
  }
}
const onDragOver = (e: DragEvent) => e.preventDefault()

onMounted(() => {
  nextTick(() => {
    canvasContainer.value?.addEventListener('drop', onDrop)
    canvasContainer.value?.addEventListener('dragover', onDragOver)
  })
  window.addEventListener('keydown', onKey)
})
onUnmounted(() => {
  window.removeEventListener('keydown', onKey)
  canvasContainer.value?.removeEventListener('drop', onDrop)
  canvasContainer.value?.removeEventListener('dragover', onDragOver)
  if (fc) fc.dispose()
})
</script>

<style scoped>
.image-editor {
  display: flex; flex-direction: column; height: calc(100vh - 64px);
  background: #0f172a; color: #e2e8f0; overflow: hidden;
}

/* Top bar */
.editor-topbar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 18px; background: #1e293b; border-bottom: 1px solid #334155;
  flex-shrink: 0;
}
.topbar-left { display: flex; align-items: center; gap: 14px; }
.topbar-left h1 { font-size: 16px; font-weight: 700; margin: 0; color: #f8fafc; }
.file-name {
  font-size: 12px; padding: 3px 10px; border-radius: var(--radius-lg);
  background: #334155; color: #94a3b8;
}
.topbar-actions { display: flex; gap: 8px; }

/* Body */
.editor-body { display: flex; flex: 1; min-height: 0; }

/* Sidebar */
.tool-sidebar {
  width: 72px; background: #1e293b; border-right: 1px solid #334155;
  display: flex; flex-direction: column; align-items: center;
  padding: 10px 0; gap: 4px; overflow-y: auto; flex-shrink: 0;
}
.sidebar-btn {
  width: 58px; padding: 8px 4px; border: none; border-radius: var(--radius-md);
  background: transparent; color: #94a3b8; cursor: pointer;
  display: flex; flex-direction: column; align-items: center; gap: 3px;
  transition: all var(--transition-fast); font-size: 11px;
}
.sidebar-btn:hover { background: #334155; color: #e2e8f0; }
.sidebar-btn.active { background: #3b82f6; color: #fff; }
.sidebar-btn small { font-weight: 600; }

/* Main canvas */
.canvas-main { flex: 1; display: flex; flex-direction: column; min-width: 0; }

.option-bar {
  display: flex; align-items: center; gap: 20px; padding: 8px 16px;
  background: #1e293b; border-bottom: 1px solid #334155; flex-shrink: 0;
}
.opt-item { display: flex; align-items: center; gap: 8px; }
.opt-item label { font-size: 12px; color: #94a3b8; white-space: nowrap; }
.opt-val { font-size: 12px; color: #94a3b8; min-width: 36px; }
.opt-hint { font-size: 12px; color: #64748b; }

.canvas-wrap {
  flex: 1; display: flex; justify-content: center; align-items: center;
  overflow: auto; padding: 16px; min-height: 0;
  background:
    radial-gradient(circle at center, #1e293b 0%, #0f172a 100%);
}
.canvas-wrap canvas { box-shadow: 0 8px 32px rgba(0,0,0,.4); border-radius: var(--radius-xs); }

.empty-canvas {
  text-align: center; cursor: pointer; padding: 60px 40px;
  border: 2px dashed #334155; border-radius: var(--radius-xl); transition: all var(--transition-fast);
}
.empty-canvas:hover { border-color: #3b82f6; background: rgba(59,130,246,.05); }
.empty-icon { width: 72px; height: 72px; margin: 0 auto 16px; color: #475569; }
.empty-canvas h3 { font-size: 17px; font-weight: 600; color: #94a3b8; margin: 0 0 6px; }
.empty-canvas p { font-size: 13px; color: #64748b; margin: 0; }

.status-bar {
  display: flex; align-items: center; gap: 16px; padding: 8px 16px;
  background: #1e293b; border-top: 1px solid #334155; font-size: 12px; color: #64748b;
  flex-shrink: 0;
}
.zoom-controls { margin-left: auto; display: flex; gap: 2px; }
.zoom-controls button {
  width: 28px; height: 28px; border: 1px solid #334155; border-radius: var(--radius-sm);
  background: #1e293b; color: #94a3b8; cursor: pointer; font-size: 15px;
  display: grid; place-items: center; transition: all var(--transition-fast);
}
.zoom-controls button:hover { background: #334155; color: #fff; }

/* Right panel */
.props-panel {
  width: 240px; background: #1e293b; border-left: 1px solid #334155;
  overflow-y: auto; flex-shrink: 0; padding: 12px;
}
.panel-section { margin-bottom: 16px; }
.panel-section h3 {
  font-size: 12px; font-weight: 700; text-transform: uppercase;
  letter-spacing: .05em; color: #64748b; margin: 0 0 10px; padding-bottom: 6px;
  border-bottom: 1px solid #334155;
}
.btn-row { display: flex; gap: 6px; flex-wrap: wrap; }
.filter-item { margin-bottom: 10px; }
.filter-item label { display: block; font-size: 11px; color: #94a3b8; margin-bottom: 4px; }

.preset-grid {
  display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 6px;
}
.preset-grid button {
  padding: 7px 4px; border: 1px solid #334155; border-radius: var(--radius-md);
  background: #0f172a; color: #94a3b8; font-size: 12px; font-weight: 600;
  cursor: pointer; transition: all var(--transition-fast);
}
.preset-grid button:hover { border-color: #3b82f6; color: #e2e8f0; background: #1e293b; }

/* Responsive */
@media (max-width: 900px) {
  .props-panel { display: none; }
  .tool-sidebar { width: 56px; }
  .sidebar-btn { width: 46px; padding: 6px 2px; }
  .sidebar-btn small { font-size: 9px; }
}

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
