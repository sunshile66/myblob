<template>
  <SimpleLayout>
    <div class="file-manager">
      <div class="fm-container">
        <div class="fm-header">
          <div class="breadcrumb">
            <span @click="goToFolder(null)" class="breadcrumb-item"
              >根目录</span
            >
            <span
              v-for="folder in breadcrumbs"
              :key="folder.id"
              class="breadcrumb-separator"
              >/</span
            >
            <span
              v-for="folder in breadcrumbs"
              :key="folder.id"
              @click="goToFolder(folder)"
              class="breadcrumb-item"
              >{{ folder.name }}</span
            >
          </div>
          <div class="header-actions">
            <el-button type="primary" @click="showCreateFolder = true">
              <el-icon><FolderAdd /></el-icon>
              新建文件夹
            </el-button>
            <el-upload
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleFileUpload"
              multiple
            >
              <el-button type="success">
                <el-icon><Upload /></el-icon>
                上传文件
              </el-button>
            </el-upload>
          </div>
        </div>

        <div class="fm-content">
          <div class="files-grid">
            <div
              v-for="folder in folders"
              :key="folder.id"
              class="item-card"
              @click="goToFolder(folder)"
            >
              <div class="item-icon folder-icon">
                <el-icon :size="48"><Folder /></el-icon>
              </div>
              <div class="item-name">{{ folder.name }}</div>
              <div class="item-actions">
                <el-button
                  size="small"
                  @click.stop="deleteFolder(folder)"
                  type="danger"
                  :icon="Delete"
                />
              </div>
            </div>

            <div v-for="file in files" :key="file.id" class="item-card">
              <div class="item-icon" :class="getFileIconClass(file.file_type)">
                <el-icon :size="48">
                  <component :is="getFileIcon(file.file_type)" />
                </el-icon>
              </div>
              <div class="item-name">{{ file.filename }}</div>
              <div class="item-size">{{ file.human_readable_size }}</div>
              <div class="item-actions">
                <el-button
                  size="small"
                  @click="downloadFile(file)"
                  :icon="Download"
                />
                <el-button
                  size="small"
                  @click="shareFile(file)"
                  :icon="Share"
                />
                <el-button
                  size="small"
                  @click="deleteFile(file)"
                  type="danger"
                  :icon="Delete"
                />
              </div>
            </div>

            <div
              v-if="folders.length === 0 && files.length === 0"
              class="empty-state"
            >
              <el-icon :size="80"><FolderOpened /></el-icon>
              <p>文件夹是空的</p>
            </div>
          </div>
        </div>
      </div>

      <el-dialog v-model="showCreateFolder" title="新建文件夹" width="400px">
        <el-input v-model="newFolderName" placeholder="请输入文件夹名称" />
        <template #footer>
          <el-button @click="showCreateFolder = false">取消</el-button>
          <el-button type="primary" @click="createFolder">确定</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="showShareDialog" title="分享文件" width="500px">
        <div v-if="currentShare" class="share-info">
          <p>分享链接:</p>
          <el-input v-model="shareLink" readonly>
            <template #append>
              <el-button @click="copyShareLink">复制</el-button>
            </template>
          </el-input>
          <p class="share-code">分享码: {{ currentShare.share_code }}</p>
        </div>
        <template #footer>
          <el-button @click="showShareDialog = false">关闭</el-button>
        </template>
      </el-dialog>
    </div>
  </SimpleLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  Folder,
  FolderAdd,
  Upload,
  Delete,
  Download,
  Share,
  FolderOpened,
  Document,
  Picture,
  Film,
  Headset,
  Files,
  Box,
} from "@element-plus/icons-vue";
import SimpleLayout from "@/layout/SimpleLayout.vue";
import request from "@/utils/request";

const router = useRouter();

const currentFolder = ref<any>(null);
const folders = ref<any[]>([]);
const files = ref<any[]>([]);
const breadcrumbs = ref<any[]>([]);
const showCreateFolder = ref(false);
const newFolderName = ref("");
const showShareDialog = ref(false);
const currentShare = ref<any>(null);
const shareLink = ref("");

const getFileIcon = (type: string) => {
  switch (type) {
    case "image":
      return Picture;
    case "document":
      return Document;
    case "video":
      return Film;
    case "audio":
      return Headset;
    case "archive":
      return Box;
    default:
      return Files;
  }
};

const getFileIconClass = (type: string) => {
  return `file-icon-${type}`;
};

const loadFiles = async () => {
  try {
    const [foldersRes, filesRes] = await Promise.all([
      request.get("/api/filemanager/folders/"),
      request.get("/api/filemanager/files/"),
    ]);

    if (currentFolder.value) {
      folders.value = foldersRes.data.filter(
        (f: any) => f.parent === currentFolder.value.id
      );
      files.value = filesRes.data.filter(
        (f: any) => f.folder === currentFolder.value.id
      );
    } else {
      folders.value = foldersRes.data.filter((f: any) => !f.parent);
      files.value = filesRes.data.filter((f: any) => !f.folder);
    }
  } catch (error) {
    ElMessage.error("加载文件失败");
  }
};

const goToFolder = (folder: any) => {
  currentFolder.value = folder;
  if (folder) {
    const idx = breadcrumbs.value.findIndex((f: any) => f.id === folder.id);
    if (idx >= 0) {
      breadcrumbs.value = breadcrumbs.value.slice(0, idx + 1);
    } else {
      breadcrumbs.value.push(folder);
    }
  } else {
    breadcrumbs.value = [];
  }
  loadFiles();
};

const createFolder = async () => {
  if (!newFolderName.value.trim()) {
    ElMessage.warning("请输入文件夹名称");
    return;
  }

  try {
    await request.post("/api/filemanager/folders/", {
      name: newFolderName.value,
      parent: currentFolder.value?.id || null,
    });
    ElMessage.success("文件夹创建成功");
    showCreateFolder.value = false;
    newFolderName.value = "";
    loadFiles();
  } catch (error) {
    ElMessage.error("创建文件夹失败");
  }
};

const deleteFolder = async (folder: any) => {
  try {
    await request.delete(`/api/filemanager/folders/${folder.id}/`);
    ElMessage.success("文件夹删除成功");
    loadFiles();
  } catch (error) {
    ElMessage.error("删除文件夹失败");
  }
};

const handleFileUpload = async (file: any) => {
  const formData = new FormData();
  formData.append("file", file.raw);
  if (currentFolder.value) {
    formData.append("folder", currentFolder.value.id);
  }

  try {
    await request.post("/api/filemanager/files/", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    ElMessage.success("文件上传成功");
    loadFiles();
  } catch (error) {
    ElMessage.error("文件上传失败");
  }
};

const downloadFile = (file: any) => {
  window.open(`/api/filemanager/files/${file.id}/download/`, "_blank");
};

const shareFile = async (file: any) => {
  try {
    const res = await request.post("/api/filemanager/shares/", {
      file: file.id,
    });
    currentShare.value = res.data;
    shareLink.value = `${window.location.origin}/share/${res.data.share_code}`;
    showShareDialog.value = true;
  } catch (error) {
    ElMessage.error("创建分享失败");
  }
};

const copyShareLink = () => {
  navigator.clipboard.writeText(shareLink.value).then(() => {
    ElMessage.success("链接已复制");
  });
};

const deleteFile = async (file: any) => {
  try {
    await request.delete(`/api/filemanager/files/${file.id}/`);
    ElMessage.success("文件删除成功");
    loadFiles();
  } catch (error) {
    ElMessage.error("删除文件失败");
  }
};

onMounted(() => {
  loadFiles();
});
</script>

<style scoped>
.file-manager {
  background: var(--theme-background);
  min-height: calc(100vh - 80px);
  padding: 24px;
}

.fm-container {
  max-width: 1400px;
  margin: 0 auto;
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--glass-shadow);
  overflow: hidden;
}

.fm-header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--glass-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.breadcrumb-item {
  color: var(--theme-primary);
  cursor: pointer;
  font-weight: 500;
}

.breadcrumb-item:hover {
  text-decoration: underline;
}

.breadcrumb-separator {
  color: var(--theme-text-secondary);
}

.header-actions {
  display: flex;
  gap: 12px;
}

.fm-content {
  padding: 24px;
}

.files-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
}

.item-card {
  background: var(--theme-card);
  border: 1px solid var(--theme-border);
  border-radius: var(--radius-lg);
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-normal);
  position: relative;
}

.item-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--theme-primary);
}

.item-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--theme-primary);
}

.folder-icon {
  color: #f59e0b;
}

.file-icon-image {
  color: #ef4444;
}

.file-icon-document {
  color: #3b82f6;
}

.file-icon-video {
  color: #8b5cf6;
}

.file-icon-audio {
  color: #10b981;
}

.file-icon-archive {
  color: #f97316;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--theme-text);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-size {
  font-size: 12px;
  color: var(--theme-text-secondary);
  margin-bottom: 12px;
}

.item-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.item-card:hover .item-actions {
  opacity: 1;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  color: var(--theme-text-secondary);
}

.empty-state .el-icon {
  margin-bottom: 16px;
  opacity: 0.5;
}

.share-info p {
  margin-bottom: 12px;
  color: var(--theme-text);
}

.share-code {
  margin-top: 12px;
  font-family: monospace;
  color: var(--theme-primary);
}

@media (max-width: 768px) {
  .file-manager {
    padding: 12px;
  }

  .fm-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .files-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 12px;
  }

  .item-card {
    padding: 16px 12px;
  }

  .item-icon {
    width: 60px;
    height: 60px;
  }

  .item-icon .el-icon {
    font-size: 36px;
  }
}
</style>
