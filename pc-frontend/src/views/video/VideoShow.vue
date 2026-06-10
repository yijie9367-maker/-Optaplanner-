<template>
  <div class="detail" :class="{ dark: isDark }">
    <button class="theme-toggle" @click="isDark = !isDark">
      {{ isDark ? '☀️ 白' : '🌙 黑' }}
    </button>

    <a href="/video" class="back-link">← 返回影视中心</a>

    <div v-if="show" class="content">
      <!-- 左：封面 -->
      <div class="left">
        <img :src="show.cover" class="cover" :alt="show.title" />
      </div>

      <!-- 右：剧集列表 -->
      <div class="right">
        <h1 class="title">{{ show.title }}</h1>
        <p class="desc">{{ show.desc }}</p>

        <div class="ep-header">
          <span>共 {{ show.episodes.length }} 集</span>
        </div>

        <div class="ep-grid">
          <div
            v-for="ep in show.episodes"
            :key="ep.id"
            class="ep-item"
            @click="goPlay(ep.id)"
          >
            第{{ ep.id }}集
          </div>
        </div>
      </div>
    </div>

    <div v-else class="not-found">剧集不存在</div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { shows } from '@/data/videos'

const route = useRoute()
const router = useRouter()
const isDark = ref(false)

const showId = computed(() => Number(route.params.showId))
const show = computed(() => shows.find(s => s.id === showId.value))

function goPlay(epId: number) {
  router.push(`/video/${showId.value}/play/${epId}`)
}
</script>

<style scoped>
.detail {
  --bg: #f4f5f7;
  --card-bg: #fff;
  --text: #18191c;
  --text2: #9499a0;
  --accent: #00a1d6;
  --border: #e3e5e7;
  --hover: #f0f8ff;
  min-height: 100vh;
  background: var(--bg);
  color: var(--text);
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  transition: background .3s, color .3s;
}
.detail.dark {
  --bg: #141414;
  --card-bg: #1f1f1f;
  --text: #e8e8e8;
  --text2: #888;
  --border: #2a2a2a;
  --hover: #252525;
}

.theme-toggle {
  position: fixed;
  top: 16px; right: 16px;
  z-index: 100;
  padding: 8px 16px;
  border: 1px solid var(--border);
  border-radius: 20px;
  background: var(--card-bg);
  color: var(--text);
  font-size: 13px;
  cursor: pointer;
}
.theme-toggle:hover { border-color: var(--accent); color: var(--accent); }

.back-link {
  display: inline-block;
  padding: 24px 32px 0;
  color: var(--accent);
  text-decoration: none;
  font-size: 14px;
}
.back-link:hover { opacity: .8; }

.content {
  display: flex;
  gap: 40px;
  max-width: 900px;
  margin: 24px auto;
  padding: 0 32px 40px;
}

/* 左：封面 */
.left {
  flex-shrink: 0;
  width: 260px;
}
.cover {
  width: 100%;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,.15);
  aspect-ratio: 3/4;
  object-fit: cover;
}

/* 右 */
.right {
  flex: 1;
  min-width: 0;
}
.title {
  font-size: 24px;
  margin: 0 0 8px;
}
.desc {
  color: var(--text2);
  font-size: 14px;
  margin: 0 0 20px;
}

.ep-header {
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
}

.ep-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.ep-item {
  padding: 10px 0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  text-align: center;
  color: var(--text);
  background: transparent;
  border: 1px solid var(--border);
  transition: all .15s;
}
.ep-item:hover {
  background: var(--hover);
  border-color: var(--text2);
  color: var(--text);
}

.not-found {
  text-align: center;
  padding: 60px;
  color: var(--text2);
  font-size: 16px;
}
</style>
