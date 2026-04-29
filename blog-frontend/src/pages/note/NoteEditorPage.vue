<template>
  <div class="note-editor">
    <div class="editor-header">
      <div class="header-left">
        <el-button @click="goBack" text>
          <el-icon><ArrowLeft /></el-icon>
          取消
        </el-button>
      </div>
      <div class="header-center">
        <div class="mode-tabs">
          <div
            v-for="mode in publishModes"
            :key="mode.value"
            class="mode-tab"
            :class="{ active: publishMode === mode.value }"
            @click="publishMode = mode.value"
          >
            <el-icon><component :is="mode.icon" /></el-icon>
            <span>{{ mode.label }}</span>
          </div>
        </div>
      </div>
      <div class="header-right">
        <el-button @click="saveDraft" round size="small">存草稿</el-button>
        <el-button
          type="primary"
          @click="handleSave"
          :loading="saving"
          round
          size="small"
        >
          {{ isEdit ? "更新" : "发布" }}
        </el-button>
      </div>
    </div>

    <div class="editor-body">
      <div class="sidebar-left">
        <div v-if="publishMode !== 'video'" class="panel templates-panel">
          <div class="panel-title">📝 模板</div>
          <div class="templates-list">
            <div
              v-for="template in templates"
              :key="template.id"
              class="template-item"
              :class="{ active: selectedTemplate?.id === template.id }"
              @click="applyTemplate(template)"
            >
              <div class="template-icon">{{ template.icon }}</div>
              <div class="template-name">{{ template.name }}</div>
            </div>
          </div>
        </div>

        <div class="panel tools-panel">
          <div class="panel-title">🛠️ 工具</div>
          <div class="tools-list">
            <div
              v-if="publishMode !== 'video'"
              class="tool-item"
              @click="smartFormat"
            >
              <el-icon><MagicStick /></el-icon>
              <span>智能排版</span>
              <span class="shortcut">Tab</span>
            </div>
            <div
              v-if="publishMode !== 'video'"
              class="tool-item"
              @click="addEmoji"
            >
              <el-icon><ChatLineSquare /></el-icon>
              <span>添加表情</span>
              <span class="shortcut">Ctrl+E</span>
            </div>
            <div class="tool-item" @click="countWords">
              <el-icon><Document /></el-icon>
              <span>字数统计</span>
              <span class="shortcut">Ctrl+W</span>
            </div>
          </div>
        </div>

        <div class="panel settings-panel">
          <div class="panel-title">⚙️ 设置</div>
          <div class="setting-item">
            <span class="setting-label">谁可以看</span>
            <el-radio-group v-model="form.visibility" size="small">
              <el-radio-button value="public">公开</el-radio-button>
              <el-radio-button value="private">私密</el-radio-button>
            </el-radio-group>
          </div>
          <div class="setting-item">
            <span class="setting-label">允许评论</span>
            <el-switch v-model="allowComments" size="small" />
          </div>
        </div>
      </div>

      <div class="main-editor">
        <div class="edit-panel">
          <div v-if="publishMode === 'video'" class="video-section">
            <VideoUploader
              v-model="videoFile"
              @upload-success="handleVideoUpload"
            />
          </div>

          <div v-else-if="publishMode === 'note'" class="image-section">
            <div class="cover-upload" v-if="form.images.length === 0">
              <el-upload
                class="image-uploader"
                :show-file-list="false"
                :on-change="handleImageChange"
                :auto-upload="false"
                multiple
                accept="image/*"
              >
                <div class="upload-placeholder">
                  <el-icon class="upload-icon"><Plus /></el-icon>
                  <span>添加图片/封面</span>
                </div>
              </el-upload>
            </div>
            <div v-else class="images-grid">
              <div
                v-for="(img, index) in form.images"
                :key="index"
                class="image-item"
              >
                <img :src="img" class="preview-image" />
                <div class="image-overlay">
                  <el-button
                    type="danger"
                    size="small"
                    circle
                    @click="removeImage(index)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
              <div class="add-more-btn" @click="addMoreImages">
                <el-icon><Plus /></el-icon>
                <span>添加更多</span>
              </div>
            </div>
          </div>

          <div v-if="publishMode === 'article'" class="article-cover-section">
            <div class="section-title">🖼️ 封面图片（可选）</div>
            <div class="cover-upload" v-if="form.images.length === 0">
              <el-upload
                class="image-uploader"
                :show-file-list="false"
                :on-change="handleImageChange"
                :auto-upload="false"
                accept="image/*"
              >
                <div class="upload-placeholder">
                  <el-icon class="upload-icon"><Plus /></el-icon>
                  <span>添加封面图片（可选）</span>
                </div>
              </el-upload>
            </div>
            <div v-else class="single-cover">
              <div class="cover-item">
                <img :src="form.images[0]" class="preview-image" />
                <div class="image-overlay">
                  <el-button
                    type="danger"
                    size="small"
                    circle
                    @click="removeImage(0)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <div class="title-section">
            <el-input
              v-model="form.title"
              type="textarea"
              :rows="2"
              placeholder="填写标题，让更多人看到你的作品～"
              class="title-input"
              maxlength="100"
              show-word-limit
            />
          </div>

          <div v-if="publishMode !== 'video'" class="content-section">
            <div v-if="publishMode === 'article'" class="article-toolbar">
              <div class="toolbar-left">
                <span class="word-count">字数: {{ form.content.length }}</span>
              </div>
              <div class="toolbar-right">
                <el-button type="primary" link size="small" @click="addHeading">
                  <el-icon><EditPen /></el-icon>
                  标题
                </el-button>
                <el-button type="primary" link size="small" @click="addBold">
                  <el-icon><Edit /></el-icon>
                  加粗
                </el-button>
                <el-button type="primary" link size="small" @click="addQuote">
                  <el-icon><ChatDotRound /></el-icon>
                  引用
                </el-button>
                <el-button type="primary" link size="small" @click="addList">
                  <el-icon><List /></el-icon>
                  列表
                </el-button>
              </div>
            </div>
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="publishMode === 'article' ? 30 : 15"
              :placeholder="
                publishMode === 'article'
                  ? '开始创作你的长文... (支持 Markdown 格式)'
                  : '添加正文... (支持 Markdown)'
              "
              class="content-input"
            />
          </div>

          <div class="tags-section">
            <div class="section-title">🏷️ 添加话题</div>
            <div class="tags-input">
              <el-tag
                v-for="(tag, index) in form.tags"
                :key="index"
                closable
                @close="removeTag(index)"
                class="post-tag"
              >
                {{ tag }}
              </el-tag>
              <el-input
                v-if="showTagInput"
                ref="tagInputRef"
                v-model="newTag"
                class="tag-input-inline"
                size="small"
                @keyup.enter="addTag"
                @blur="addTag"
                placeholder="输入话题"
              />
              <el-button
                v-else
                type="primary"
                link
                size="small"
                @click="showTagInput = true"
              >
                + 添加话题
              </el-button>
            </div>
          </div>

          <div class="location-section">
            <div class="section-title">📍 添加地点</div>
            <el-input
              v-model="form.location"
              placeholder="搜索地点"
              :prefix-icon="Location"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from "vue";
import { useRouter, useRoute } from "vue-router";
import {
  ArrowLeft,
  MagicStick,
  ChatLineSquare,
  Document,
  Plus,
  Delete,
  Location,
  VideoCamera,
  Picture,
  DocumentCopy,
  EditPen,
  Edit,
  ChatDotRound,
  List,
} from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import VideoUploader from "@/components/post/VideoUploader.vue";
import type { MediaAsset } from "@/types";

const router = useRouter();
const route = useRoute();

const publishModes = [
  { value: "video", label: "发布视频", icon: VideoCamera },
  { value: "note", label: "发布图文", icon: Picture },
  { value: "article", label: "写长文", icon: DocumentCopy },
];

const publishMode = ref("note");
const activeTab = ref("edit");
const saving = ref(false);
const isEdit = ref(false);
const allowComments = ref(true);
const showTagInput = ref(false);
const newTag = ref("");
const tagInputRef = ref();
const videoFile = ref<File | null>(null);
const selectedVideo = ref<MediaAsset | null>(null);

const form = reactive({
  title: "",
  content: "",
  images: [] as string[],
  tags: [] as string[],
  location: "",
  visibility: "public",
});

const templates = [
  { id: 1, name: "日常分享", icon: "🌞", content: "今天分享一下..." },
  { id: 2, name: "美食探店", icon: "🍜", content: "今天去了一家超棒的店..." },
  { id: 3, name: "旅行日记", icon: "✈️", content: "这次旅行真的太难忘了..." },
  { id: 4, name: "穿搭分享", icon: "👗", content: "今日份穿搭来啦..." },
  { id: 5, name: "学习笔记", icon: "📚", content: "今天学习到了..." },
];

const selectedTemplate = ref<(typeof templates)[0] | null>(null);

const goBack = () => {
  ElMessageBox.confirm("确定要离开吗？未保存的内容将会丢失。", "提示", {
    confirmButtonText: "确定离开",
    cancelButtonText: "继续编辑",
    type: "warning",
  })
    .then(() => {
      router.back();
    })
    .catch(() => {});
};

const saveDraft = () => {
  ElMessage.success("草稿已保存");
};

const handleSave = async () => {
  if (!form.title.trim()) {
    ElMessage.warning("请填写标题");
    return;
  }

  if (publishMode.value === "video" && !selectedVideo.value) {
    ElMessage.warning("请上传视频");
    return;
  }

  if (publishMode.value === "note" && form.images.length === 0) {
    ElMessage.warning("请添加至少一张图片");
    return;
  }

  saving.value = true;

  try {
    await new Promise((resolve) => setTimeout(resolve, 1500));
    ElMessage.success(isEdit.value ? "更新成功" : "发布成功");
    router.push("/");
  } catch (error) {
    ElMessage.error("保存失败，请重试");
  } finally {
    saving.value = false;
  }
};

const handleImageChange = (file: any) => {
  const reader = new FileReader();
  reader.onload = (e) => {
    form.images.push(e.target?.result as string);
  };
  reader.readAsDataURL(file.raw);
};

const removeImage = (index: number) => {
  form.images.splice(index, 1);
};

const addMoreImages = () => {
  const input = document.createElement("input");
  input.type = "file";
  input.accept = "image/*";
  input.multiple = true;
  input.onchange = (e: any) => {
    Array.from(e.target.files).forEach((file: any) => {
      const reader = new FileReader();
      reader.onload = (ev) => {
        form.images.push(ev.target?.result as string);
      };
      reader.readAsDataURL(file);
    });
  };
  input.click();
};

const handleVideoUpload = (mediaAsset: MediaAsset) => {
  selectedVideo.value = mediaAsset;
};

const applyTemplate = (template: (typeof templates)[0]) => {
  selectedTemplate.value = template;
  form.content = template.content + "\n\n";
};

const smartFormat = () => {
  ElMessage.success("智能排版已应用");
};

const addEmoji = () => {
  form.content += " 😊";
};

const countWords = () => {
  const count = form.content.length;
  ElMessage.info(`当前字数：${count} 字`);
};

const addTag = () => {
  if (newTag.value.trim()) {
    form.tags.push(newTag.value.trim());
    newTag.value = "";
  }
  showTagInput.value = false;
};

const removeTag = (index: number) => {
  form.tags.splice(index, 1);
};

const addHeading = () => {
  form.content += "\n## 标题\n";
};

const addBold = () => {
  form.content += "**粗体文字**";
};

const addQuote = () => {
  form.content += "\n> 引用文字\n";
};

const addList = () => {
  form.content += "\n- 列表项1\n- 列表项2\n";
};

onMounted(() => {
  if (route.params.slug) {
    isEdit.value = true;
  }
});
</script>

<style scoped>
.note-editor {
  min-height: 100vh;
  background: #f8f8f8;
}

.editor-header {
  background: white;
  border-bottom: 1px solid #eee;
  padding: 12px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left,
.header-right {
  min-width: 120px;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.mode-tabs {
  display: flex;
  gap: 8px;
  background: #f5f5f5;
  padding: 4px;
  border-radius: 8px;
}

.mode-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  transition: all 0.2s;
}

.mode-tab:hover {
  background: rgba(0, 0, 0, 0.05);
}

.mode-tab.active {
  background: white;
  color: #ff2442;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.mode-tab .el-icon {
  font-size: 16px;
}

.editor-body {
  display: flex;
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  gap: 24px;
}

.sidebar-left {
  width: 240px;
  flex-shrink: 0;
}

.panel {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.templates-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.template-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.template-item:hover {
  background: #f5f5f5;
}

.template-item.active {
  background: #fff5f7;
  border: 1px solid #ffb6c1;
}

.template-icon {
  font-size: 20px;
}

.template-name {
  font-size: 13px;
  color: #333;
}

.tools-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tool-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.tool-item:hover {
  background: #f5f5f5;
}

.tool-item .el-icon {
  color: #666;
  font-size: 18px;
}

.tool-item span {
  font-size: 13px;
  color: #333;
  flex: 1;
}

.shortcut {
  font-size: 11px;
  color: #999;
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.setting-item:last-child {
  margin-bottom: 0;
}

.setting-label {
  font-size: 13px;
  color: #333;
}

.main-editor {
  flex: 1;
  background: white;
  border-radius: 12px;
  padding: 24px;
}

.video-section,
.image-section,
.article-cover-section {
  margin-bottom: 24px;
}

.article-cover-section {
  margin-bottom: 20px;
}

.article-cover-section .section-title {
  margin-bottom: 16px;
}

.single-cover {
  width: 100%;
}

.cover-item {
  position: relative;
  width: 100%;
  max-width: 400px;
  aspect-ratio: 16/9;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.cover-upload {
  width: 100%;
}

.upload-placeholder {
  border: 2px dashed #ddd;
  border-radius: 12px;
  padding: 60px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-placeholder:hover {
  border-color: #ff2442;
  background: #fff5f7;
}

.upload-icon {
  font-size: 48px;
  color: #ccc;
  margin-bottom: 12px;
  display: block;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px;
}

.image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.image-item:hover .image-overlay {
  opacity: 1;
}

.add-more-btn {
  border: 2px dashed #ddd;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  color: #999;
  font-size: 13px;
  transition: all 0.3s;
}

.add-more-btn:hover {
  border-color: #ff2442;
  color: #ff2442;
}

.add-more-btn .el-icon {
  font-size: 28px;
}

.title-section {
  margin-bottom: 20px;
}

.title-input :deep(textarea) {
  font-size: 20px;
  font-weight: 600;
  border: none;
  resize: none;
  padding: 0;
}

.title-input :deep(.el-textarea__inner:focus) {
  box-shadow: none;
}

.content-section {
  margin-bottom: 24px;
}

.article-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.toolbar-left {
  font-size: 13px;
  color: #999;
}

.word-count {
  font-weight: 500;
}

.toolbar-right {
  display: flex;
  gap: 4px;
}

.content-input :deep(textarea) {
  font-size: 15px;
  line-height: 1.8;
  border: none;
  resize: none;
  padding: 0;
}

.content-input :deep(.el-textarea__inner:focus) {
  box-shadow: none;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.tags-section,
.location-section {
  margin-bottom: 24px;
}

.tags-input {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.post-tag {
  background: #fff5f7;
  border-color: #ffb6c1;
  color: #ff2442;
}

.tag-input-inline {
  width: 120px;
}
</style>
