<template>
  <div class="video-player">
    <video
      ref="videoElement"
      class="video"
      :poster="poster"
      @click="togglePlay"
      @play="onPlay"
      @pause="onPause"
      @timeupdate="onTimeUpdate"
      @loadedmetadata="onLoadedMetadata"
    >
      <source :src="src" :type="type" />
      您的浏览器不支持视频播放
    </video>

    <div v-if="!playing" class="play-overlay" @click="togglePlay">
      <div class="play-button">
        <el-icon><VideoPlay /></el-icon>
      </div>
    </div>

    <div
      class="controls"
      :class="{ 'controls-visible': controlsVisible || !playing }"
      @mouseenter="showControls"
      @mouseleave="hideControls"
    >
      <div class="progress-bar" @click="seekVideo">
        <div class="progress-filled" :style="{ width: `${progress}%` }"></div>
        <div class="progress-handle" :style="{ left: `${progress}%` }"></div>
      </div>

      <div class="controls-bottom">
        <button class="control-btn" @click="togglePlay">
          <el-icon v-if="!playing"><VideoPlay /></el-icon>
          <el-icon v-else><VideoPause /></el-icon>
        </button>

        <span class="time-display"
          >{{ formatTime(currentTime) }} / {{ formatTime(duration) }}</span
        >

        <div class="controls-right">
          <button class="control-btn" @click="toggleMute">
            <el-icon v-if="!muted"><Volume /></el-icon>
            <el-icon v-else><VolumeMute /></el-icon>
          </button>

          <div class="volume-slider">
            <el-slider
              v-model="volume"
              :min="0"
              :max="1"
              :step="0.01"
              :show-tooltip="false"
              @change="onVolumeChange"
            />
          </div>

          <button class="control-btn" @click="toggleFullscreen">
            <el-icon><FullScreen /></el-icon>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";
import {
  VideoPlay,
  VideoPause,
  Volume,
  VolumeMute,
  FullScreen,
} from "@element-plus/icons-vue";

interface Props {
  src: string;
  poster?: string;
  type?: string;
  autoplay?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  type: "video/mp4",
  autoplay: false,
});

const videoElement = ref<HTMLVideoElement>();
const playing = ref(false);
const currentTime = ref(0);
const duration = ref(0);
const volume = ref(1);
const muted = ref(false);
const controlsVisible = ref(false);
let hideControlsTimeout: number | null = null;

const progress = computed(() => {
  if (duration.value === 0) return 0;
  return (currentTime.value / duration.value) * 100;
});

const togglePlay = () => {
  if (!videoElement.value) return;
  if (playing.value) {
    videoElement.value.pause();
  } else {
    videoElement.value.play();
  }
};

const onPlay = () => {
  playing.value = true;
};

const onPause = () => {
  playing.value = false;
};

const onTimeUpdate = () => {
  if (videoElement.value) {
    currentTime.value = videoElement.value.currentTime;
  }
};

const onLoadedMetadata = () => {
  if (videoElement.value) {
    duration.value = videoElement.value.duration;
  }
};

const seekVideo = (event: MouseEvent) => {
  if (!videoElement.value) return;
  const rect = (event.currentTarget as HTMLElement).getBoundingClientRect();
  const percent = (event.clientX - rect.left) / rect.width;
  videoElement.value.currentTime = percent * duration.value;
};

const toggleMute = () => {
  muted.value = !muted.value;
  if (videoElement.value) {
    videoElement.value.muted = muted.value;
  }
};

const onVolumeChange = (value: number) => {
  if (videoElement.value) {
    videoElement.value.volume = value;
    muted.value = value === 0;
  }
};

const toggleFullscreen = () => {
  if (!videoElement.value) return;
  if (!document.fullscreenElement) {
    videoElement.value.requestFullscreen();
  } else {
    document.exitFullscreen();
  }
};

const showControls = () => {
  controlsVisible.value = true;
  if (hideControlsTimeout) {
    clearTimeout(hideControlsTimeout);
  }
};

const hideControls = () => {
  if (playing.value) {
    hideControlsTimeout = window.setTimeout(() => {
      controlsVisible.value = false;
    }, 3000);
  }
};

const formatTime = (seconds: number): string => {
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins.toString().padStart(2, "0")}:${secs
    .toString()
    .padStart(2, "0")}`;
};

onMounted(() => {
  if (props.autoplay && videoElement.value) {
    videoElement.value.play();
  }
});

onUnmounted(() => {
  if (hideControlsTimeout) {
    clearTimeout(hideControlsTimeout);
  }
});
</script>

<style scoped>
.video-player {
  position: relative;
  width: 100%;
  background: #000;
  border-radius: 12px;
  overflow: hidden;
}

.video {
  width: 100%;
  display: block;
  cursor: pointer;
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  cursor: pointer;
}

.play-button {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 36, 66, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 36px;
  transition: transform 0.3s;
}

.play-button:hover {
  transform: scale(1.1);
}

.controls {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
  padding: 20px 16px 16px;
  opacity: 0;
  transition: opacity 0.3s;
}

.controls-visible {
  opacity: 1;
}

.progress-bar {
  position: relative;
  height: 4px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
  cursor: pointer;
  margin-bottom: 12px;
}

.progress-filled {
  height: 100%;
  background: #ff2442;
  border-radius: 2px;
}

.progress-handle {
  position: absolute;
  top: 50%;
  width: 12px;
  height: 12px;
  background: #ff2442;
  border-radius: 50%;
  transform: translate(-50%, -50%);
}

.controls-bottom {
  display: flex;
  align-items: center;
  gap: 16px;
}

.control-btn {
  background: none;
  border: none;
  color: white;
  font-size: 20px;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.control-btn:hover {
  color: #ff2442;
}

.time-display {
  color: white;
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}

.controls-right {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}

.volume-slider {
  width: 80px;
}

.volume-slider :deep(.el-slider__runway) {
  background: rgba(255, 255, 255, 0.3);
}

.volume-slider :deep(.el-slider__bar) {
  background: #ff2442;
}

.volume-slider :deep(.el-slider__button) {
  width: 12px;
  height: 12px;
  border: none;
}
</style>
