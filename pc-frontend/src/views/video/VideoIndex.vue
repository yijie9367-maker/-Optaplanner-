<template>
  <div class="home" :class="{ dark: isDark }">
    <a href="/login" class="back-btn">← 返回</a>
    <button class="theme-toggle" @click="isDark = !isDark">
      {{ isDark ? '☀️ 白' : '🌙 黑' }}
    </button>

    <h1 class="page-title">🎬 影视中心</h1>

    <!-- 剧集卡片网格 -->
    <div class="show-grid">
      <div
        v-for="show in pagedShows"
        :key="show.id"
        class="show-card"
        @click="goShow(show.id)"
      >
        <img :src="show.cover" class="show-cover" :alt="show.title" />
        <div class="show-info">
          <div class="show-name">{{ show.title }}</div>
          <div class="show-desc">{{ show.desc }} · {{ show.episodes.length }}集</div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="pagination">
      <button :disabled="page === 1" @click="page--">‹</button>
      <button
        v-for="p in totalPages"
        :key="p"
        :class="{ active: page === p }"
        @click="page = p"
      >{{ p }}</button>
      <button :disabled="page === totalPages" @click="page++">›</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { shows } from '@/data/videos'

const router = useRouter()
const isDark = ref(false)

const PAGE_SIZE = 15
const page = ref(1)
const totalPages = computed(() => Math.ceil(shows.length / PAGE_SIZE))
const pagedShows = computed(() => {
  const start = (page.value - 1) * PAGE_SIZE
  return shows.slice(start, start + PAGE_SIZE)
})

function goShow(id: number) {
  router.push(`/video/${id}`)
}
</script>

<style scoped>
.home {
  --bg: #f4f5f7;
  --card-bg: #fff;
  --text: #18191c;
  --text2: #9499a0;
  --accent: #00a1d6;
  --border: #e3e5e7;
  --shadow: 0 2px 8px rgba(0,0,0,.06);
  min-height: 100vh;
  background: var(--bg);
  color: var(--text);
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  transition: background .3s, color .3s;
}
.home.dark {
  --bg: #141414;
  --card-bg: #1f1f1f;
  --text: #e8e8e8;
  --text2: #888;
  --border: #2a2a2a;
  --shadow: 0 2px 8px rgba(0,0,0,.4);
}

.back-btn {
  position: fixed;
  top: 16px; left: 16px;
  z-index: 100;
  padding: 8px 16px;
  border: 1px solid var(--border);
  border-radius: 20px;
  background: var(--card-bg);
  color: var(--text);
  font-size: 13px;
  text-decoration: none;
  box-shadow: var(--shadow);
  transition: all .2s;
}
.back-btn:hover { border-color: var(--accent); color: var(--accent); }

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
  box-shadow: var(--shadow);
}
.theme-toggle:hover { border-color: var(--accent); color: var(--accent); }

.page-title {
  text-align: center;
  font-size: 28px;
  padding: 40px 0 24px;
  margin: 0;
}

.show-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 24px;
}

.show-card {
  background: var(--card-bg);
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: var(--shadow);
  transition: transform .15s, box-shadow .15s;
}
.show-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0,161,214,.18);
}

.show-cover {
  width: 100%;
  aspect-ratio: 3/4;
  object-fit: cover;
  display: block;
}

.show-info {
  padding: 10px 12px;
}

.show-name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.show-desc {
  font-size: 12px;
  color: var(--text2);
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  gap: 6px;
  padding: 32px 0;
}
.pagination button {
  min-width: 36px; height: 36px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--card-bg);
  color: var(--text);
  font-size: 14px;
  cursor: pointer;
}
.pagination button:hover:not(:disabled):not(.active) { border-color: var(--accent); color: var(--accent); }
.pagination button.active { background: var(--accent); border-color: var(--accent); color: #fff; }
.pagination button:disabled { opacity: .3; cursor: not-allowed; }
</style>
