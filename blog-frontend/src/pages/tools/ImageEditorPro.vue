<template>
  <SimpleLayout>
    <div class="simple-image-editor">
      <div class="editor-header">
        <h1>图片编辑器</h1>
        <p>功能完整的在线图片编辑工具</p>
      </div>

      <div class="editor-container">
        <!-- 主工具栏 -->
        <div class="main-toolbar">
          <div class="toolbar-section">
            <el-button @click="triggerUpload" type="primary" size="large">
              <el-icon><FolderOpened /></el-icon>
              打开图片
            </el-button>
            <el-button @click="saveImage" size="large">
              <el-icon><Download /></el-icon>
              保存
            </el-button>
            <el-dropdown @command="handleExport" size="large">
              <el-button>
                <el-icon><Download /></el-icon>
                导出 <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="png">PNG 格式</el-dropdown-item>
                  <el-dropdown-item command="jpeg">JPEG 格式</el-dropdown-item>
                  <el-dropdown-item command="webp">WebP 格式</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <div class="toolbar-section">
            <el-button-group>
              <el-tooltip content="撤销 (Ctrl+Z)" placement="bottom">
                <el-button @click="undo" :disabled="!canUndo" size="large">
                  <el-icon><RefreshLeft /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="重做 (Ctrl+Y)" placement="bottom">
                <el-button @click="redo" :disabled="!canRedo" size="large">
                  <el-icon><RefreshRight /></el-icon>
                </el-button>
              </el-tooltip>
            </el-button-group>
          </div>

          <div class="toolbar-section">
            <el-button-group>
              <el-tooltip content="选择工具 (V)" placement="bottom">
                <el-button
                  :type="activeTool === 'move' ? 'primary' : ''"
                  @click="setTool('move')"
                  size="large"
                >
                  <el-icon><Pointer /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="裁剪 (C)" placement="bottom">
                <el-button
                  :type="activeTool === 'crop' ? 'primary' : ''"
                  @click="setTool('crop')"
                  size="large"
                >
                  <el-icon><Crop /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="画笔 (B)" placement="bottom">
                <el-button
                  :type="activeTool === 'brush' ? 'primary' : ''"
                  @click="setTool('brush')"
                  size="large"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="橡皮擦 (E)" placement="bottom">
                <el-button
                  :type="activeTool === 'erase' ? 'primary' : ''"
                  @click="setTool('erase')"
                  size="large"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </el-button-group>
          </div>

          <div class="toolbar-section">
            <el-button-group>
              <el-tooltip content="文字 (T)" placement="bottom">
                <el-button @click="addText" size="large">
                  <span style="font-weight: bold">T</span>
                </el-button>
              </el-tooltip>
              <el-tooltip content="形状" placement="bottom">
                <el-button @click="showShapeMenu = !showShapeMenu" size="large">
                  <el-icon><Star /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="吸管 (I)" placement="bottom">
                <el-button @click="pickColor" size="large">
                  <el-icon><Aim /></el-icon>
                </el-button>
              </el-tooltip>
            </el-button-group>
          </div>

          <div class="toolbar-section">
            <el-button-group>
              <el-tooltip content="放大 (Ctrl+Plus)" placement="bottom">
                <el-button @click="zoomIn" size="large">
                  <el-icon><ZoomIn /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="缩小 (Ctrl+Minus)" placement="bottom">
                <el-button @click="zoomOut" size="large">
                  <el-icon><ZoomOut /></el-icon>
                </el-button>
              </el-tooltip>
              <el-button size="large"> {{ zoomLevel }}% </el-button>
              <el-tooltip content="适应屏幕" placement="bottom">
                <el-button @click="fitToScreen" size="large">
                  <el-icon><FullScreen /></el-icon>
                </el-button>
              </el-tooltip>
            </el-button-group>
          </div>
        </div>

        <!-- 形状菜单 -->
        <div v-if="showShapeMenu" class="shape-menu">
          <div class="shape-options">
            <el-button @click="addRectangle" size="small">矩形</el-button>
            <el-button @click="addCircle" size="small">圆形</el-button>
            <el-button @click="addTriangle" size="small">三角形</el-button>
            <el-button @click="addLine" size="small">直线</el-button>
          </div>
        </div>

        <!-- 工具选项 -->
        <div v-if="activeTool === 'brush'" class="tool-options">
          <div class="option-group">
            <label>画笔大小: {{ brushSize }}px</label>
            <el-slider v-model="brushSize" :min="1" :max="50" show-input />
          </div>
          <div class="option-group">
            <label>颜色</label>
            <el-color-picker v-model="foregroundColor" />
          </div>
        </div>

        <div v-if="activeTool === 'text'" class="tool-options">
          <div class="option-group">
            <label>字体大小: {{ textSize }}px</label>
            <el-slider v-model="textSize" :min="12" :max="72" show-input />
          </div>
          <div class="option-group">
            <label>颜色</label>
            <el-color-picker v-model="textColor" />
          </div>
        </div>

        <!-- 画布区域 -->
        <div class="canvas-area">
          <div class="canvas-container" ref="canvasContainer">
            <div v-if="!currentImage" class="empty-state">
              <el-icon :size="64"><Picture /></el-icon>
              <h3>请选择一张图片开始编辑</h3>
              <p>支持 JPG、PNG、GIF、WebP 等格式</p>
              <el-button @click="triggerUpload" type="primary" size="large">
                <el-icon><FolderOpened /></el-icon>
                选择图片
              </el-button>
            </div>
            <canvas v-else ref="canvasElement"></canvas>
          </div>
        </div>

        <!-- 状态信息 -->
        <div v-if="currentImage" class="status-info">
          <div class="status-item">
            <span>文件名: {{ currentFileName }}</span>
          </div>
          <div class="status-item">
            <span>尺寸: {{ imageWidth }} × {{ imageHeight }} px</span>
          </div>
          <div class="status-item">
            <span>缩放: {{ zoomLevel }}%</span>
          </div>
        </div>
      </div>

      <!-- 隐藏的文件输入 -->
      <input
        type="file"
        ref="fileInput"
        accept="image/*"
        style="display: none"
        @change="handleFileSelect"
      />
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from "vue";
import { ElMessage } from "element-plus";
import * as fabric from "fabric";
import {
  FolderOpened,
  Download,
  RefreshLeft,
  RefreshRight,
  Pointer,
  Crop,
  Edit,
  Delete,
  Star,
  Aim,
  ZoomIn,
  ZoomOut,
  FullScreen,
  ArrowDown,
  Picture,
} from "@element-plus/icons-vue";
import SimpleLayout from "@/layout/SimpleLayout.vue";

// 画布相关
const canvasContainer = ref<HTMLDivElement | null>(null);
const canvasElement = ref<HTMLCanvasElement | null>(null);
const canvas = ref<fabric.Canvas | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);

// 状态管理
const currentImage = ref<fabric.Image | null>(null);
const imageWidth = ref(0);
const imageHeight = ref(0);
const zoomLevel = ref(100);
const activeTool = ref("move");
const currentFileName = ref("");
const showShapeMenu = ref(false);

// 工具选项
const brushSize = ref(10);
const textSize = ref(24);
const foregroundColor = ref("#000000");
const textColor = ref("#000000");

// 历史记录
const history = ref<Array<{ action: string; timestamp: string }>>([]);
const historyIndex = ref(-1);
const canUndo = ref(false);
const canRedo = ref(false);

// 初始化画布
const initCanvas = () => {
  if (!canvasElement.value || !canvasContainer.value) return;

  canvas.value = new fabric.Canvas(canvasElement.value, {
    width: canvasContainer.value.clientWidth,
    height: canvasContainer.value.clientHeight,
    backgroundColor: "#f8f9fa",
  });
};

// 工具函数
const setTool = (tool: string) => {
  activeTool.value = tool;
  if (canvas.value) {
    canvas.value.isDrawingMode = tool === "brush";
    canvas.value.selection = tool === "move";
  }
};

const triggerUpload = () => {
  fileInput.value?.click();
};

const handleFileSelect = (e: Event) => {
  const target = e.target as HTMLInputElement;
  const file = target.files?.[0];
  if (file) {
    if (!file.type.startsWith("image/")) {
      ElMessage.error("请选择图片文件");
      return;
    }

    currentFileName.value = file.name;
    const reader = new FileReader();

    reader.onload = (event) => {
      try {
        const imgObj = new Image();

        imgObj.onload = () => {
          try {
            if (canvas.value) {
              const imgInstance = new fabric.Image(imgObj, {
                left: 0,
                top: 0,
                originX: "left",
                originY: "top",
              });

              // 限制图片最大尺寸
              const MAX_WIDTH = 1200;
              const MAX_HEIGHT = 800;

              let width = imgInstance.getScaledWidth();
              let height = imgInstance.getScaledHeight();

              // 图片缩放处理
              if (width > MAX_WIDTH || height > MAX_HEIGHT) {
                const scale = Math.min(MAX_WIDTH / width, MAX_HEIGHT / height);
                imgInstance.scale(scale);
              }

              // 设置画布尺寸
              canvas.value.setDimensions({
                width: Math.max(imgInstance.getScaledWidth() + 100, 800),
                height: Math.max(imgInstance.getScaledHeight() + 100, 600),
              });

              // 添加图片并居中
              canvas.value.add(imgInstance);
              canvas.value.centerObject(imgInstance);
              canvas.value.setActiveObject(imgInstance);

              currentImage.value = imgInstance;
              imageWidth.value = Math.round(imgInstance.getScaledWidth());
              imageHeight.value = Math.round(imgInstance.getScaledHeight());

              saveHistory("打开图片");
              ElMessage.success("图片加载成功");
            }
          } catch (error: any) {
            console.error("创建图片实例失败:", error);
            ElMessage.error("图片加载失败：" + error.message);
          }
        };

        imgObj.src = event.target?.result as string;
      } catch (error: any) {
        console.error("读取图片失败:", error);
        ElMessage.error("图片格式不支持");
      }
    };

    reader.readAsDataURL(file);
  }
  target.value = "";
};

const saveImage = () => {
  if (!canvas.value) {
    ElMessage.warning("请先加载图片");
    return;
  }

  const dataURL = canvas.value.toDataURL({
    format: "png",
    quality: 1,
  });
  const link = document.createElement("a");
  link.download = `edited-${Date.now()}.png`;
  link.href = dataURL;
  link.click();
  ElMessage.success("图片已保存");
};

const handleExport = (format: string) => {
  if (!canvas.value) {
    ElMessage.warning("请先加载图片");
    return;
  }

  const dataURL = canvas.value.toDataURL({
    format: format.toLowerCase(),
    quality: 0.9,
  });
  const link = document.createElement("a");
  link.download = `edited-${Date.now()}.${format.toLowerCase()}`;
  link.href = dataURL;
  link.click();
  ElMessage.success(`图片已导出为 ${format.toUpperCase()} 格式`);
};

const zoomIn = () => {
  if (canvas.value && zoomLevel.value < 500) {
    zoomLevel.value += 10;
    canvas.value.setZoom(zoomLevel.value / 100);
  }
};

const zoomOut = () => {
  if (canvas.value && zoomLevel.value > 10) {
    zoomLevel.value -= 10;
    canvas.value.setZoom(zoomLevel.value / 100);
  }
};

const fitToScreen = () => {
  if (canvas.value && currentImage.value) {
    const containerWidth = canvasContainer.value?.clientWidth || 800;
    const containerHeight = canvasContainer.value?.clientHeight || 600;
    const scale = Math.min(
      (containerWidth - 100) / currentImage.value.getScaledWidth(),
      (containerHeight - 100) / currentImage.value.getScaledHeight()
    );
    zoomLevel.value = Math.round(scale * 100);
    canvas.value.setZoom(scale);
  }
};

const undo = () => {
  if (historyIndex.value > 0) {
    historyIndex.value--;
    canUndo.value = historyIndex.value > 0;
    canRedo.value = true;
  }
};

const redo = () => {
  if (historyIndex.value < history.value.length - 1) {
    historyIndex.value++;
    canUndo.value = true;
    canRedo.value = historyIndex.value < history.value.length - 1;
  }
};

const saveHistory = (action: string) => {
  const timestamp = new Date().toLocaleTimeString();
  history.value.push({ action, timestamp });
  historyIndex.value = history.value.length - 1;
  canUndo.value = true;
  canRedo.value = false;
};

// 文字工具
const addText = () => {
  if (!canvas.value) return;

  const text = new fabric.IText("双击编辑文字", {
    left: 100,
    top: 100,
    fontSize: textSize.value,
    fill: textColor.value,
    fontFamily: "Arial",
  });

  canvas.value.add(text);
  canvas.value.setActiveObject(text);
  saveHistory("添加文字");
};

// 形状工具
const addRectangle = () => {
  if (!canvas.value) return;

  const rect = new fabric.Rect({
    left: 100,
    top: 100,
    width: 100,
    height: 100,
    fill: foregroundColor.value,
    stroke: "#000000",
    strokeWidth: 2,
  });

  canvas.value.add(rect);
  canvas.value.setActiveObject(rect);
  saveHistory("添加矩形");
  showShapeMenu.value = false;
};

const addCircle = () => {
  if (!canvas.value) return;

  const circle = new fabric.Circle({
    left: 100,
    top: 100,
    radius: 50,
    fill: foregroundColor.value,
    stroke: "#000000",
    strokeWidth: 2,
  });

  canvas.value.add(circle);
  canvas.value.setActiveObject(circle);
  saveHistory("添加圆形");
  showShapeMenu.value = false;
};

const addTriangle = () => {
  if (!canvas.value) return;

  const triangle = new fabric.Triangle({
    left: 100,
    top: 100,
    width: 100,
    height: 100,
    fill: foregroundColor.value,
    stroke: "#000000",
    strokeWidth: 2,
  });

  canvas.value.add(triangle);
  canvas.value.setActiveObject(triangle);
  saveHistory("添加三角形");
  showShapeMenu.value = false;
};

const addLine = () => {
  if (!canvas.value) return;

  const line = new fabric.Line([50, 100, 200, 100], {
    left: 100,
    top: 100,
    stroke: foregroundColor.value,
    strokeWidth: 3,
  });

  canvas.value.add(line);
  canvas.value.setActiveObject(line);
  saveHistory("添加直线");
  showShapeMenu.value = false;
};

// 吸管工具
const pickColor = () => {
  ElMessage.info("点击画布上的颜色进行拾取");
};

// 生命周期
onMounted(() => {
  nextTick(() => {
    initCanvas();
  });

  // 添加键盘快捷键
  const handleKeyDown = (e: KeyboardEvent) => {
    if (e.ctrlKey || e.metaKey) {
      switch (e.key) {
        case "z":
          e.preventDefault();
          undo();
          break;
        case "y":
          e.preventDefault();
          redo();
          break;
        case "s":
          e.preventDefault();
          saveImage();
          break;
        case "o":
          e.preventDefault();
          triggerUpload();
          break;
      }
    }

    // 工具快捷键
    switch (e.key.toLowerCase()) {
      case "v":
        setTool("move");
        break;
      case "c":
        setTool("crop");
        break;
      case "b":
        setTool("brush");
        break;
      case "e":
        setTool("erase");
        break;
      case "t":
        addText();
        break;
      case "i":
        pickColor();
        break;
    }
  };

  window.addEventListener("keydown", handleKeyDown);

  onUnmounted(() => {
    window.removeEventListener("keydown", handleKeyDown);
    if (canvas.value) {
      canvas.value.dispose();
    }
  });
});
</script>

<style scoped lang="scss">
.simple-image-editor {
  min-height: 100vh;
  background: #ffffff;
  padding: 24px;
}

.editor-header {
  text-align: center;
  margin-bottom: 32px;

  h1 {
    font-size: 32px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 8px;
  }

  p {
    font-size: 16px;
    color: #6b7280;
  }
}

.editor-container {
  max-width: 1200px;
  margin: 0 auto;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.main-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #f8f9fa;
  border-bottom: 1px solid #e5e7eb;
  flex-wrap: wrap;
  gap: 16px;
}

.toolbar-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.shape-menu {
  padding: 16px 24px;
  background: #f8f9fa;
  border-bottom: 1px solid #e5e7eb;
}

.shape-options {
  display: flex;
  gap: 8px;
}

.tool-options {
  padding: 16px 24px;
  background: #f8f9fa;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  gap: 24px;
}

.option-group {
  display: flex;
  flex-direction: column;
  gap: 8px;

  label {
    font-size: 14px;
    color: #6b7280;
    font-weight: 500;
  }
}

.canvas-area {
  padding: 24px;
  min-height: 500px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.canvas-container {
  width: 100%;
  height: 100%;
  min-height: 400px;
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f9fafb;

  canvas {
    border-radius: 4px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  }
}

.empty-state {
  text-align: center;
  color: #6b7280;

  h3 {
    font-size: 18px;
    font-weight: 600;
    margin: 16px 0 8px;
  }

  p {
    font-size: 14px;
    margin-bottom: 24px;
  }
}

.status-info {
  padding: 16px 24px;
  background: #f8f9fa;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #6b7280;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .simple-image-editor {
    padding: 16px;
  }

  .main-toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .toolbar-section {
    justify-content: center;
  }

  .tool-options {
    flex-direction: column;
    gap: 16px;
  }

  .status-info {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
}
</style>
