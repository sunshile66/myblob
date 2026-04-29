<template>
  <div class="note-card" @click="goToDetail">
    <div class="note-image-wrapper">
      <el-image
        :src="post.cover || getRandomImage()"
        fit="cover"
        class="note-image"
        lazy
        :initial-index="post.id"
        :preview-src-list="[post.cover || getRandomImage()]"
      >
        <template #placeholder>
          <div class="image-placeholder">
            <el-icon class="placeholder-icon"><Picture /></el-icon>
          </div>
        </template>
        <template #error>
          <div class="image-error">
            <el-icon><Picture /></el-icon>
          </div>
        </template>
      </el-image>
      <div v-if="isVideo" class="video-badge">
        <el-icon><VideoCamera /></el-icon>
      </div>
    </div>

    <div class="note-content">
      <h3 class="note-title">{{ post.title }}</h3>
      <div class="note-footer">
        <div class="author-info">
          <el-avatar :size="18" :src="post.author.avatar">
            {{
              post.author.nickname?.charAt(0) || post.author.username.charAt(0)
            }}
          </el-avatar>
          <span class="author-name">{{
            post.author.nickname || post.author.username
          }}</span>
        </div>
        <div class="like-info" @click.stop="toggleLike">
          <el-icon :class="{ liked: isLiked }"><Star /></el-icon>
          <span>{{ formatNumber(post.like_count) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Picture, Star, VideoCamera } from "@element-plus/icons-vue";
import type { Post } from "@/types";

interface Props {
  post: Post;
}

const props = defineProps<Props>();
const router = useRouter();
const isLiked = ref(false);

const isVideo = computed(() => props.post.post_type === "video");

const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + "w";
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + "k";
  }
  return num.toString();
};

const getRandomImage = (): string => {
  const seed = props.post.id || Math.floor(Math.random() * 1000);
  const coverImages = [
    "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=260&h=320&fit=crop",
    "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=260&h=360&fit=crop",
    "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=260&h=280&fit=crop",
    "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=260&h=400&fit=crop",
    "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=260&h=300&fit=crop",
    "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=260&h=340&fit=crop",
    "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=260&h=380&fit=crop",
    "https://images.unsplash.com/photo-1485230919244-13f0247d8e02?w=260&h=260&fit=crop",
    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=260&h=320&fit=crop",
    "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=260&h=300&fit=crop"
  ];
  return coverImages[seed % coverImages.length];
};

const toggleLike = () => {
  isLiked.value = !isLiked.value;
};

const goToDetail = () => {
  router.push({ name: "NoteDetail", params: { slug: props.post.slug } });
};

onMounted(() => {
  if (props.post.cover) {
    const img = new Image();
    img.src = props.post.cover;
  }
});
</script>

<style scoped>
.note-card {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: var(--glass-shadow);
}

.note-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: var(--shadow-xl);
  border-color: var(--theme-primary);
}

.note-image-wrapper {
  position: relative;
  width: 100%;
  overflow: hidden;
  background: var(--gradient-primary);
}

.note-image {
  width: 100%;
  display: block;
  transition: transform var(--transition-slow);
}

.note-card:hover .note-image {
  transform: scale(1.1);
}

.image-placeholder {
  width: 100%;
  aspect-ratio: 3/4;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
}

.placeholder-icon {
  font-size: 32px;
  color: white;
}

.video-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--theme-primary);
  font-size: 16px;
  box-shadow: var(--shadow-md);
}

.image-error {
  width: 100%;
  aspect-ratio: 3/4;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-warning);
  color: white;
  font-size: 32px;
}

.note-content {
  padding: 12px 14px;
}

.note-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--theme-text);
  margin: 0 0 12px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  letter-spacing: -0.2px;
  transition: color var(--transition-fast);
}

.note-card:hover .note-title {
  color: var(--theme-primary);
}

.note-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-name {
  font-size: 12px;
  color: var(--theme-text-secondary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.like-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--theme-text-secondary);
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 20px;
  transition: all var(--transition-fast);
  background: transparent;
}

.like-info:hover {
  background: var(--theme-primary-light);
  color: var(--theme-primary);
}

.like-info .el-icon {
  color: var(--theme-text-secondary);
  font-size: 14px;
  transition: all var(--transition-fast);
}

.like-info:hover .el-icon {
  color: var(--theme-primary);
}

.like-info .el-icon.liked {
  color: var(--theme-primary);
  fill: var(--theme-primary);
}
</style>
