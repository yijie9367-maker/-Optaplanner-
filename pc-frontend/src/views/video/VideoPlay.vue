<template>
  <div class="play-page" :class="{ dark: isDark }">
    <!-- 主题切换 -->
    <button class="theme-toggle" @click="isDark = !isDark">
      {{ isDark ? '☀️ 白' : '🌙 黑' }}
    </button>

    <!-- 顶栏 -->
    <div class="top-bar">
      <a :href="`/video/${showId}`" class="back-link">← 返回</a>
      <span class="ep-title">{{ show?.title }} · 第{{ currentId }}集</span>
    </div>

    <!-- 播放器 -->
    <div class="player-wrapper">
      <video
        ref="videoRef"
        class="video-js vjs-big-play-centered"
        controls
        autoplay
        playsinline
      ></video>
    </div>

    <!-- 切集 -->
    <div class="ep-controls">
      <button :disabled="!hasPrev" @click="goEpisode(currentId - 1)" class="ctrl-btn">
        ← 上一集
      </button>
      <select v-model="currentId" @change="goEpisode(currentId)" class="ctrl-select">
        <option v-for="ep in episodes" :key="ep.id" :value="ep.id">
          第{{ ep.id }}集
        </option>
      </select>
      <button :disabled="!hasNext" @click="goEpisode(currentId + 1)" class="ctrl-btn">
        下一集 →
      </button>
    </div>

    <!-- 快速选集 -->
    <div class="ep-quick">
      <button
        v-for="ep in episodes"
        :key="ep.id"
        class="ep-chip"
        :class="{ active: currentId === ep.id }"
        @click="goEpisode(ep.id)"
      >
        {{ ep.id }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import videojs from 'video.js'
import 'video.js/dist/video-js.css'
import { shows } from '@/data/videos'

const route = useRoute()
const router = useRouter()
const videoRef = ref<HTMLVideoElement | null>(null)
const isDark = ref(false)

let player: any = null

const showId = computed(() => Number(route.params.showId))
const show = computed(() => shows.find(s => s.id === showId.value))
const episodes = computed(() => show.value?.episodes || [])
const currentId = ref(Number(route.params.epId) || 1)
const episode = computed(() => episodes.value.find(e => e.id === currentId.value))
const hasPrev = computed(() => currentId.value > 1)
const hasNext = computed(() => currentId.value < episodes.value.length)

function initPlayer(url: string) {
  if (!videoRef.value) return
  if (player) { player.dispose(); player = null }
  player = videojs(videoRef.value, {
    controls: true, autoplay: true, preload: 'auto', fluid: true,
    playbackRates: [0.5, 0.75, 1, 1.25, 1.5, 2],
    sources: [{ src: url, type: 'application/x-mpegURL' }],
  })
}

function goEpisode(id: number) {
  if (id < 1 || id > episodes.value.length) return
  currentId.value = id
  router.replace(`/video/${showId.value}/play/${id}`)
  const ep = episodes.value.find(e => e.id === id)
  if (ep) initPlayer(ep.url)
}

onMounted(() => {
  const ep = episode.value
  if (ep) initPlayer(ep.url)
})

onUnmounted(() => {
  if (player) { player.dispose(); player = null }
})
</script>

<style scoped>
.play-page {
  --bg: #f4f5f7;
  --bar-bg: #fff;
  --text: #18191c;
  --accent: #00a1d6;
  --border: #e3e5e7;
  --chip-bg: #e8e8e8;
  min-height: 100vh;
  background: var(--bg);
  color: var(--text);
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  transition: background .3s, color .3s;
}
.play-page.dark {
  --bg: #141414;
  --bar-bg: #1f1f1f;
  --text: #e8e8e8;
  --border: #2a2a2a;
  --chip-bg: #2a2a2a;
}

.theme-toggle {
  position: fixed;
  top: 16px; right: 16px;
  z-index: 100;
  padding: 8px 16px;
  border: 1px solid var(--border);
  border-radius: 20px;
  background: var(--bar-bg);
  color: var(--text);
  font-size: 13px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,.08);
}
.theme-toggle:hover { border-color: var(--accent); color: var(--accent); }

.top-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  background: var(--bar-bg);
  border-bottom: 1px solid var(--border);
}
.back-link { color: var(--accent); text-decoration: none; font-size: 14px; }
.back-link:hover { opacity: .8; }
.ep-title { font-size: 17px; font-weight: 600; }

.player-wrapper {
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
}
:deep(.video-js) { width: 100%; height: 560px; }

.ep-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 20px;
}
.ctrl-btn {
  padding: 10px 24px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--bar-bg);
  color: var(--text);
  font-size: 14px;
  cursor: pointer;
}
.ctrl-btn:hover:not(:disabled) { border-color: var(--accent); color: var(--accent); }
.ctrl-btn:disabled { opacity: .3; cursor: not-allowed; }
.ctrl-select {
  padding: 10px 16px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--bar-bg);
  color: var(--text);
  font-size: 14px;
}

.ep-quick {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  padding: 0 24px 40px;
  max-width: 1000px;
  margin: 0 auto;
}
.ep-chip {
  width: 42px; height: 42px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--chip-bg);
  color: var(--text);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}
.ep-chip:hover { border-color: var(--accent); }
.ep-chip.active { background: var(--accent); border-color: var(--accent); color: #fff; }
</style>
