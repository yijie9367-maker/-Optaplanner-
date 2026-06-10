// 影视数据配置
const COS_BASE = "https://video-self-1361115130.cos.ap-guangzhou.myqcloud.com"

export interface Episode {
  id: number
  title: string
  url: string
}

export interface Show {
  id: number
  title: string
  cover: string          // 封面图路径（assets 里）
  desc: string
  episodes: Episode[]
}

// ===== 剧集数据 =====

const xianwangCover = new URL('@/assets/404_images/xianwangderichangshenghuo.png', import.meta.url).href

const xianwangEpisodes: Episode[] = [
  { id: 1,  title: "第1集",  url: `${COS_BASE}/hls/20260506-153726-6e844e70/1.m3u8` },
  { id: 2,  title: "第2集",  url: `${COS_BASE}/hls/20260506-153726-764ba59b/2.m3u8` },
  { id: 3,  title: "第3集",  url: `${COS_BASE}/hls/20260506-153725-0310e198/3.m3u8` },
  { id: 4,  title: "第4集",  url: `${COS_BASE}/hls/20260506-153726-20a40a24/4.m3u8` },
  { id: 5,  title: "第5集",  url: `${COS_BASE}/hls/20260506-153726-ebfdc4ba/5.m3u8` },
  { id: 6,  title: "第6集",  url: `${COS_BASE}/hls/6_20260506-165413-aa466cb3/6.m3u8` },
  { id: 7,  title: "第7集",  url: `${COS_BASE}/hls/7_20260507-093816-cf5da717/7.m3u8` },
  { id: 8,  title: "第8集",  url: `${COS_BASE}/hls/8_20260507-093816-f0cad7ff/8.m3u8` },
  { id: 9,  title: "第9集",  url: `${COS_BASE}/hls/9_20260507-093816-6973011c/9.m3u8` },
  { id: 10, title: "第10集", url: `${COS_BASE}/hls/10_20260507-093816-1d69eee1/10.m3u8` },
  { id: 11, title: "第11集", url: `${COS_BASE}/hls/20260423-112119-33ee7ba2/11.m3u8` },
  { id: 12, title: "第12集", url: `${COS_BASE}/hls/12_20260507-093817-68b46917/12.m3u8` },
]

export const shows: Show[] = [
  {
    id: 1,
    title: "仙王的日常生活",
    cover: xianwangCover,
    desc: "奇幻·搞笑·日常",
    episodes: xianwangEpisodes,
  },
]

// 辅助函数
export function getShow(id: number): Show | undefined {
  return shows.find(s => s.id === id)
}
