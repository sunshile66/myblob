<template>
  <SimpleLayout>
    <div class="color-picker-tool">
      <div class="tool-header">
        <el-button @click="router.push('/tools')" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回工具箱
        </el-button>
        <h1>🎨 颜色拾取器</h1>
      </div>

      <div class="tool-container">
        <div class="color-sections">
          <div class="picker-card">
            <div class="color-preview-section">
              <div
                class="color-preview-large"
                :style="{ backgroundColor: selectedColor }"
              >
                <div class="color-info">
                  <span>{{ selectedColor }}</span>
                </div>
              </div>
              <div class="picker-controls">
                <el-color-picker
                  v-model="selectedColor"
                  show-alpha
                  size="large"
                  @change="onColorChange"
                />
                <el-input
                  v-model="colorInput"
                  placeholder="#667eea"
                  class="color-input"
                  @change="updateColorFromInput"
                >
                  <template #prefix>
                    <span class="input-prefix">#</span>
                  </template>
                </el-input>
              </div>
            </div>

            <div class="color-sliders">
              <div class="slider-item">
                <label
                  >R <span>{{ rgba.r }}</span></label
                >
                <el-slider
                  v-model="rgba.r"
                  :min="0"
                  :max="255"
                  @input="updateFromRGB"
                />
              </div>
              <div class="slider-item">
                <label
                  >G <span>{{ rgba.g }}</span></label
                >
                <el-slider
                  v-model="rgba.g"
                  :min="0"
                  :max="255"
                  @input="updateFromRGB"
                />
              </div>
              <div class="slider-item">
                <label
                  >B <span>{{ rgba.b }}</span></label
                >
                <el-slider
                  v-model="rgba.b"
                  :min="0"
                  :max="255"
                  @input="updateFromRGB"
                />
              </div>
              <div class="slider-item">
                <label
                  >A <span>{{ rgba.a }}%</span></label
                >
                <el-slider
                  v-model="rgba.a"
                  :min="0"
                  :max="100"
                  @input="updateFromRGB"
                />
              </div>
            </div>
          </div>

          <div class="formats-card">
            <h3>📋 颜色格式</h3>
            <div class="format-list">
              <div
                class="format-item"
                v-for="format in colorFormats"
                :key="format.label"
              >
                <div class="format-label">{{ format.label }}</div>
                <div class="format-value" :title="format.value">
                  {{ format.value }}
                </div>
                <el-button size="small" circle @click="copyValue(format.value)">
                  <el-icon><DocumentCopy /></el-icon>
                </el-button>
              </div>
            </div>

            <div class="css-section">
              <h4>CSS 代码</h4>
              <div class="css-code">
                <div class="css-line">
                  <span class="css-prop">color:</span>
                  <span class="css-value">{{ selectedColor }};</span>
                  <el-button size="small" @click="copyCSS('color')"
                    >复制</el-button
                  >
                </div>
                <div class="css-line">
                  <span class="css-prop">background-color:</span>
                  <span class="css-value">{{ selectedColor }};</span>
                  <el-button size="small" @click="copyCSS('bg')"
                    >复制</el-button
                  >
                </div>
                <div class="css-line">
                  <span class="css-prop">border:</span>
                  <span class="css-value">1px solid {{ selectedColor }};</span>
                  <el-button size="small" @click="copyCSS('border')"
                    >复制</el-button
                  >
                </div>
                <div class="css-line">
                  <span class="css-prop">box-shadow:</span>
                  <span class="css-value"
                    >0 0 10px {{ hexToRgba(selectedColor, 0.5) }};</span
                  >
                  <el-button size="small" @click="copyCSS('shadow')"
                    >复制</el-button
                  >
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="presets-section">
          <h3>🎨 预设颜色</h3>
          <div class="preset-groups">
            <div
              class="preset-group"
              v-for="group in colorPresets"
              :key="group.name"
            >
              <h4>{{ group.name }}</h4>
              <div class="preset-colors">
                <div
                  v-for="color in group.colors"
                  :key="color"
                  class="preset-color"
                  :style="{ backgroundColor: color }"
                  @click="selectedColor = color"
                  :title="color"
                >
                  <el-icon
                    v-if="selectedColor === color"
                    :size="20"
                    color="white"
                  >
                    <Check />
                  </el-icon>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="gradients-section">
          <h3>🌈 渐变生成器</h3>
          <div class="gradient-examples">
            <div
              class="gradient-item"
              v-for="gradient in gradients"
              :key="gradient.css"
              :style="{ background: gradient.css }"
              @click="copyGradient(gradient.css)"
            >
              <span class="gradient-name">{{ gradient.name }}</span>
              <el-button
                size="small"
                circle
                @click.stop="copyGradient(gradient.css)"
              >
                <el-icon><DocumentCopy /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { ArrowLeft, DocumentCopy, Check } from "@element-plus/icons-vue";
import SimpleLayout from "@/layout/SimpleLayout.vue";

const router = useRouter();
const selectedColor = ref("#667eea");
const colorInput = ref("667eea");

const rgba = ref({ r: 102, g: 126, b: 234, a: 100 });

const colorFormats = computed(() => [
  { label: "HEX", value: selectedColor.value },
  {
    label: "HEXA",
    value:
      selectedColor.value +
      Math.round((rgba.a / 100) * 255)
        .toString(16)
        .padStart(2, "0")
        .toUpperCase(),
  },
  { label: "RGB", value: `rgb(${rgba.r}, ${rgba.g}, ${rgba.b})` },
  {
    label: "RGBA",
    value: `rgba(${rgba.r}, ${rgba.g}, ${rgba.b}, ${(rgba.a / 100).toFixed(
      2
    )})`,
  },
  { label: "HSL", value: rgbToHsl(rgba.r, rgba.g, rgba.b) },
  {
    label: "HSLA",
    value:
      rgbToHsl(rgba.r, rgba.g, rgba.b).replace("hsl", "hsla") +
      `, ${(rgba.a / 100).toFixed(2)})`,
  },
  { label: "CMYK", value: rgbToCmyk(rgba.r, rgba.g, rgba.b) },
  { label: "Decimal", value: `${(rgba.r << 16) + (rgba.g << 8) + rgba.b}` },
]);

const colorPresets = [
  {
    name: "红色系",
    colors: ["#ff4757", "#ff6348", "#ff7f50", "#ffa502", "#ff6b81", "#ff9ff3"],
  },
  {
    name: "绿色系",
    colors: ["#2ed573", "#7bed9f", "#26de81", "#20bf6b", "#0fb9b8", "#0d947d"],
  },
  {
    name: "蓝色系",
    colors: ["#1e90ff", "#3742fa", "#5352ed", "#5f27cd", "#34ace0", "#33d9b2"],
  },
  {
    name: "中性色",
    colors: ["#ffffff", "#f8f9fa", "#e9ecef", "#dee2e6", "#6c757d", "#000000"],
  },
  {
    name: "渐变色",
    colors: ["#667eea", "#764ba2", "#f093fb", "#f5576c", "#4facfe", "#43e97b"],
  },
];

const gradients = [
  { name: "Ocean", css: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)" },
  { name: "Sunset", css: "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)" },
  { name: "Sky", css: "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)" },
  { name: "Forest", css: "linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)" },
  { name: "Candy", css: "linear-gradient(135deg, #fa709a 0%, #fee140 100%)" },
  { name: "Mint", css: "linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)" },
];

const onColorChange = () => {
  updateRGBFromHex();
  colorInput.value = selectedColor.value.replace("#", "");
};

const updateFromRGB = () => {
  selectedColor.value = `#${componentToHex(rgba.value.r)}${componentToHex(
    rgba.value.g
  )}${componentToHex(rgba.value.b)}`;
  colorInput.value = selectedColor.value.replace("#", "");
};

const updateColorFromInput = () => {
  let hex = colorInput.value.replace("#", "");
  if (hex.length === 3) {
    hex = hex
      .split("")
      .map((c) => c + c)
      .join("");
  }
  if (/^[0-9A-Fa-f]{6}$/.test(hex)) {
    selectedColor.value = `#${hex}`;
    updateRGBFromHex();
  }
};

const updateRGBFromHex = () => {
  const hex = selectedColor.value.replace("#", "");
  rgba.value.r = parseInt(hex.substring(0, 2), 16);
  rgba.value.g = parseInt(hex.substring(2, 4), 16);
  rgba.value.b = parseInt(hex.substring(4, 6), 16);
};

const componentToHex = (c: number) => {
  const hex = c.toString(16);
  return hex.length === 1 ? "0" + hex : hex;
};

const rgbToHsl = (r: number, g: number, b: number) => {
  r /= 255;
  g /= 255;
  b /= 255;
  const max = Math.max(r, g, b),
    min = Math.min(r, g, b);
  let h = 0,
    s,
    l = (max + min) / 2;

  if (max === min) {
    h = s = 0;
  } else {
    const d = max - min;
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
    switch (max) {
      case r:
        h = ((g - b) / d + (g < b ? 6 : 0)) / 6;
        break;
      case g:
        h = ((b - r) / d + 2) / 6;
        break;
      case b:
        h = ((r - g) / d + 4) / 6;
        break;
    }
  }
  return `hsl(${Math.round(h * 360)}, ${Math.round(s * 100)}%, ${Math.round(
    l * 100
  )}%)`;
};

const rgbToCmyk = (r: number, g: number, b: number) => {
  r /= 255;
  g /= 255;
  b /= 255;
  const k = 1 - Math.max(r, g, b);
  if (k === 1) return "cmyk(0%, 0%, 0%, 100%)";
  const c = (1 - r - k) / (1 - k);
  const m = (1 - g - k) / (1 - k);
  const y = (1 - b - k) / (1 - k);
  return `cmyk(${Math.round(c * 100)}%, ${Math.round(m * 100)}%, ${Math.round(
    y * 100
  )}%, ${Math.round(k * 100)}%)`;
};

const hexToRgba = (hex: string, alpha: number = 1) => {
  const r = parseInt(hex.slice(1, 3), 16);
  const g = parseInt(hex.slice(3, 5), 16);
  const b = parseInt(hex.slice(5, 7), 16);
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
};

const copyValue = (value: string) => {
  navigator.clipboard.writeText(value);
  ElMessage.success("已复制");
};

const copyCSS = (type: string) => {
  let css = "";
  switch (type) {
    case "color":
      css = `color: ${selectedColor.value};`;
      break;
    case "bg":
      css = `background-color: ${selectedColor.value};`;
      break;
    case "border":
      css = `border: 1px solid ${selectedColor.value};`;
      break;
    case "shadow":
      css = `box-shadow: 0 0 10px ${hexToRgba(selectedColor.value, 0.5)};`;
      break;
  }
  navigator.clipboard.writeText(css);
  ElMessage.success("CSS 已复制");
};

const copyGradient = (css: string) => {
  navigator.clipboard.writeText(css);
  ElMessage.success("渐变 CSS 已复制");
};

watch(selectedColor, () => {
  updateRGBFromHex();
});
</script>

<style scoped>
.color-picker-tool {
  background: var(--theme-background);
  min-height: calc(100vh - 80px);
  padding: 24px;
}

.tool-header {
  max-width: 1200px;
  margin: 0 auto 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  color: var(--theme-text);
}

.back-btn:hover {
  border-color: var(--theme-primary);
  color: var(--theme-primary);
}

.tool-header h1 {
  font-size: 28px;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.tool-container {
  max-width: 1200px;
  margin: 0 auto;
}

.color-sections {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.picker-card,
.formats-card {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  padding: 24px;
  box-shadow: var(--glass-shadow);
}

.color-preview-section {
  text-align: center;
  margin-bottom: 24px;
}

.color-preview-large {
  width: 100%;
  height: 200px;
  border-radius: var(--radius-lg);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  border: 2px solid var(--glass-border);
}

.color-info span {
  background: rgba(255, 255, 255, 0.9);
  padding: 8px 16px;
  border-radius: var(--radius-md);
  font-family: monospace;
  font-size: 16px;
  font-weight: 700;
  color: var(--theme-text);
}

.picker-controls {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: center;
}

.color-input {
  flex: 1;
  max-width: 200px;
}

.input-prefix {
  color: var(--theme-text-secondary);
  font-weight: 600;
}

.color-sliders {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.slider-item label {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 600;
  color: var(--theme-text);
  margin-bottom: 8px;
}

.slider-item label span {
  color: var(--theme-primary);
}

.formats-card h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0 0 20px 0;
}

.format-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.format-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-md);
}

.format-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--theme-text-secondary);
  min-width: 60px;
}

.format-value {
  flex: 1;
  font-family: monospace;
  font-size: 14px;
  color: var(--theme-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.css-section {
  margin-top: 20px;
}

.css-section h4 {
  font-size: 16px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0 0 12px 0;
}

.css-code {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.css-line {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-md);
  font-family: monospace;
  font-size: 13px;
}

.css-prop {
  color: var(--theme-primary);
  font-weight: 600;
}

.css-value {
  flex: 1;
  color: var(--theme-text);
}

.presets-section,
.gradients-section {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  padding: 24px;
  margin-bottom: 24px;
}

.presets-section h3,
.gradients-section h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--theme-text);
  margin: 0 0 20px 0;
}

.preset-groups {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.preset-group h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--theme-text-secondary);
  margin-bottom: 12px;
}

.preset-colors {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(56px, 1fr));
  gap: 12px;
}

.preset-color {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid transparent;
}

.preset-color:hover {
  transform: scale(1.1);
}

.preset-color:active {
  border-color: white;
}

.gradient-examples {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.gradient-item {
  height: 100px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.gradient-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.gradient-name {
  font-size: 14px;
  font-weight: 700;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

@media (max-width: 992px) {
  .color-sections {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .color-picker-tool {
    padding: 12px;
  }

  .tool-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .tool-header h1 {
    font-size: 22px;
  }

  .preset-colors {
    grid-template-columns: repeat(auto-fill, minmax(48px, 1fr));
  }

  .gradient-examples {
    grid-template-columns: 1fr;
  }
}
</style>
