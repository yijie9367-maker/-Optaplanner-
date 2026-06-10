<template>
  <div class="forum-page">
    <el-card class="forum-toolbar" shadow="never">
      <div class="toolbar-row">
        <div class="toolbar-left">
          <el-input
            v-model="keyword"
            placeholder="搜索帖子标题"
            clearable
            class="forum-search"
            @keyup.enter="fetchPosts"
            @clear="fetchPosts"
          >
            <template #append>
              <el-button @click="fetchPosts">搜索</el-button>
            </template>
          </el-input>
          <el-select v-model="sort" class="forum-sort" @change="fetchPosts">
            <el-option label="最新发布" value="newest" />
            <el-option label="最早发布" value="oldest" />
          </el-select>
        </div>
        <el-button type="primary" :disabled="isMuted" @click="openCreateDialog">发布帖子</el-button>
      </div>
      <el-alert
        v-if="isMuted"
        class="mute-alert"
        title="你已被管理员禁言，当前不能发布帖子或评论。"
        type="warning"
        :closable="false"
        show-icon
      />
    </el-card>

    <el-card class="forum-list" shadow="never">
      <div v-if="loading" class="forum-loading">
        <el-text>加载中...</el-text>
      </div>
      <el-empty v-else-if="posts.length === 0" description="暂无帖子" />
      <div v-else class="post-list">
        <div v-for="post in posts" :key="post.id" class="post-card">
          <div class="post-main" @click="openDetail(post)">
            <div class="post-header-row">
              <div class="post-title-wrap">
                <el-tag v-if="post.pinned" type="danger" size="small">置顶</el-tag>
                <h3 class="post-title">{{ post.title }}</h3>
              </div>
              <div class="post-meta">
                <span>{{ post.authorName }} · {{ roleLabel(post.authorRole) }}</span>
                <span>{{ formatDateTime(post.createdAt) }}</span>
              </div>
            </div>
            <div class="post-preview">{{ post.content }}</div>
            <div class="post-stats">
              <span>👍 {{ post.likeCount }}</span>
              <span>👎 {{ post.dislikeCount }}</span>
              <span>💬 {{ post.commentCount }}</span>
            </div>
          </div>

          <div class="post-actions">
            <el-button size="small" @click.stop="handleVote(post, 'like')" :type="post.myVote === 'like' ? 'success' : 'default'">点赞</el-button>
            <el-button size="small" @click.stop="handleVote(post, 'dislike')" :type="post.myVote === 'dislike' ? 'danger' : 'default'">踩</el-button>
            <el-button v-if="isAdmin" size="small" @click.stop="openEditDialog(post)">编辑</el-button>
            <el-button v-if="isAdmin" size="small" @click.stop="togglePin(post)">{{ post.pinned ? '取消置顶' : '置顶' }}</el-button>
            <el-button v-if="canDeletePost(post)" size="small" type="danger" @click.stop="removePost(post)">删除</el-button>
          </div>
        </div>
      </div>

      <div class="forum-pagination">
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="page + 1"
          :page-size="size"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="editorVisible" :title="editorMode === 'create' ? '发布帖子' : '编辑帖子'" width="680px">
      <el-form :model="editorForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editorForm.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="editorForm.content" type="textarea" :rows="8" maxlength="3000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPost">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="帖子详情" width="760px">
      <div v-if="currentPost" class="detail-wrapper">
        <div class="detail-title-row">
          <div class="detail-title-wrap">
            <el-tag v-if="currentPost.pinned" type="danger" size="small">置顶</el-tag>
            <h2 class="detail-title">{{ currentPost.title }}</h2>
          </div>
          <div class="detail-meta">{{ currentPost.authorName }} · {{ roleLabel(currentPost.authorRole) }} · {{ formatDateTime(currentPost.createdAt) }}</div>
        </div>

        <div class="detail-content">{{ currentPost.content }}</div>

        <div class="detail-vote-row">
          <el-button @click="handleDetailVote('like')" :type="currentPost.myVote === 'like' ? 'success' : 'default'">👍 点赞 {{ currentPost.likeCount }}</el-button>
          <el-button @click="handleDetailVote('dislike')" :type="currentPost.myVote === 'dislike' ? 'danger' : 'default'">👎 踩 {{ currentPost.dislikeCount }}</el-button>
        </div>

        <div class="comments-section">
          <div class="section-heading">评论区</div>
          <el-empty v-if="!currentPost.comments || currentPost.comments.length === 0" description="暂无评论" />
          <div v-else class="comment-list">
            <div v-for="comment in currentPost.comments" :key="comment.id" class="comment-item">
              <div class="comment-main">
                <div class="comment-author">{{ comment.authorName }} · {{ roleLabel(comment.authorRole) }}</div>
                <div class="comment-content">{{ comment.content }}</div>
                <div class="comment-time">{{ formatDateTime(comment.createdAt) }}</div>
              </div>
              <el-button v-if="isAdmin" type="danger" size="small" @click="removeComment(comment)">删除</el-button>
            </div>
          </div>
        </div>

        <div class="comment-editor">
          <div class="section-heading">发表评论</div>
          <el-input v-model="commentContent" :disabled="isMuted" type="textarea" :rows="4" maxlength="1000" show-word-limit />
          <div class="comment-submit">
            <el-button type="primary" :disabled="isMuted" @click="submitComment">发布评论</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { forumAPI } from '@/api/forum'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const currentUser = computed(() => userStore.user || userStore.admin || {})
const isAdmin = computed(() => currentUser.value?.role === 'admin')
const isMuted = computed(() => Boolean(currentUser.value?.isMuted))
const mutedMessage = '你已被管理员禁言，当前不能发帖或评论'

const loading = ref(false)
const posts = ref([])
const total = ref(0)
const page = ref(0)
const size = ref(10)
const keyword = ref('')
const sort = ref('newest')
const detailVisible = ref(false)
const editorVisible = ref(false)
const editorMode = ref('create')
const currentPost = ref(null)
const commentContent = ref('')

const editorForm = reactive({
  id: null,
  title: '',
  content: ''
})

function getQueryUser() {
  return {
    userId: currentUser.value?.id,
    userRole: currentUser.value?.role
  }
}

function roleLabel(role) {
  const map = {
    admin: '管理员',
    student: '学生',
    teacher: '教师'
  }
  return map[role] || role || '未知身份'
}

function formatDateTime(value) {
  if (!value) return '未知时间'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${hh}:${mm}`
}

function canDeletePost(post) {
  return isAdmin.value || (post.authorId === currentUser.value?.id && post.authorRole === currentUser.value?.role)
}

async function fetchPosts() {
  loading.value = true
  try {
    const res = await forumAPI.getPosts({
      keyword: keyword.value,
      sort: sort.value,
      page: page.value,
      size: size.value,
      ...getQueryUser()
    })
    const data = res.data || {}
    posts.value = data.content || []
    total.value = data.totalElements || 0
  } finally {
    loading.value = false
  }
}

function handlePageChange(nextPage) {
  page.value = nextPage - 1
  fetchPosts()
}

function openCreateDialog() {
  if (isMuted.value) {
    ElMessage.warning(mutedMessage)
    return
  }
  editorMode.value = 'create'
  editorForm.id = null
  editorForm.title = ''
  editorForm.content = ''
  editorVisible.value = true
}

function openEditDialog(post) {
  editorMode.value = 'edit'
  editorForm.id = post.id
  editorForm.title = post.title
  editorForm.content = post.content
  editorVisible.value = true
}

async function submitPost() {
  if (isMuted.value) {
    ElMessage.warning(mutedMessage)
    return
  }
  if (!editorForm.title.trim()) {
    ElMessage.warning('标题不能为空')
    return
  }
  if (!editorForm.content.trim()) {
    ElMessage.warning('内容不能为空')
    return
  }

  try {
    if (editorMode.value === 'create') {
      await forumAPI.createPost({
        title: editorForm.title.trim(),
        content: editorForm.content.trim(),
        authorId: currentUser.value?.id,
        authorName: currentUser.value?.name || currentUser.value?.username,
        authorRole: currentUser.value?.role
      })
      ElMessage.success('发帖成功')
    } else {
      await forumAPI.updatePost(editorForm.id, {
        title: editorForm.title.trim(),
        content: editorForm.content.trim()
      })
      ElMessage.success('帖子已更新')
    }

    editorVisible.value = false
    fetchPosts()
    if (detailVisible.value && currentPost.value?.id === editorForm.id) {
      await openDetail({ id: editorForm.id })
    }
  } catch {
    return
  }
}

async function openDetail(post) {
  const res = await forumAPI.getPostDetail(post.id, getQueryUser())
  currentPost.value = res.data || null
  detailVisible.value = true
}

async function handleVote(post, voteType) {
  await forumAPI.vote(post.id, {
    userId: currentUser.value?.id,
    userRole: currentUser.value?.role,
    voteType
  })
  await fetchPosts()
  if (detailVisible.value && currentPost.value?.id === post.id) {
    await openDetail(post)
  }
}

async function handleDetailVote(voteType) {
  if (!currentPost.value) return
  await forumAPI.vote(currentPost.value.id, {
    userId: currentUser.value?.id,
    userRole: currentUser.value?.role,
    voteType
  })
  await openDetail(currentPost.value)
  await fetchPosts()
}

async function togglePin(post) {
  const nextPinned = !post.pinned
  await forumAPI.pinPost(post.id, nextPinned)
  ElMessage.success(nextPinned ? '已置顶' : '已取消置顶')
  fetchPosts()
  if (detailVisible.value && currentPost.value?.id === post.id) {
    await openDetail(post)
  }
}

async function removePost(post) {
  await ElMessageBox.confirm(`确认删除帖子“${post.title}”吗？`, '删除确认', {
    type: 'warning'
  })
  await forumAPI.deletePost(post.id)
  ElMessage.success('帖子已删除')
  if (detailVisible.value && currentPost.value?.id === post.id) {
    detailVisible.value = false
  }
  fetchPosts()
}

async function submitComment() {
  if (!currentPost.value) return
  if (isMuted.value) {
    ElMessage.warning(mutedMessage)
    return
  }
  if (!commentContent.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  try {
    await forumAPI.addComment(currentPost.value.id, {
      authorId: currentUser.value?.id,
      authorName: currentUser.value?.name || currentUser.value?.username,
      authorRole: currentUser.value?.role,
      content: commentContent.value.trim()
    })
    commentContent.value = ''
    ElMessage.success('评论成功')
    await openDetail(currentPost.value)
    await fetchPosts()
  } catch {
    return
  }
}

async function removeComment(comment) {
  await ElMessageBox.confirm('确认删除这条评论吗？', '删除确认', {
    type: 'warning'
  })
  await forumAPI.deleteComment(comment.id)
  ElMessage.success('评论已删除')
  await openDetail(currentPost.value)
  await fetchPosts()
}

onMounted(() => {
  fetchPosts()
})
</script>

<style scoped>
.forum-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.forum-toolbar,
.forum-list {
  border-radius: 18px;
}

.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  gap: 12px;
  flex: 1;
  flex-wrap: wrap;
}

.forum-search {
  width: min(420px, 100%);
}

.forum-sort {
  width: 140px;
}

.forum-loading {
  padding: 48px 0;
  text-align: center;
}

.mute-alert {
  margin-top: 14px;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  border: 1px solid #e5e7eb;
  border-radius: 18px;
  background: #fcfcfd;
  padding: 18px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.post-main {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.post-header-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.post-title-wrap,
.detail-title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.post-title,
.detail-title {
  margin: 0;
  font-size: 18px;
  color: #1f2937;
}

.post-meta,
.detail-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: #6b7280;
  font-size: 13px;
  text-align: right;
}

.post-preview,
.detail-content {
  color: #374151;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
}

.post-preview {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 12px;
}

.post-stats,
.detail-vote-row {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-top: 12px;
  color: #6b7280;
}

.post-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: flex-start;
}

.forum-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.detail-wrapper {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-heading {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  padding: 14px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.comment-author {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.comment-content {
  margin: 8px 0;
  color: #374151;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-time {
  color: #6b7280;
  font-size: 12px;
}

.comment-submit {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .post-card {
    flex-direction: column;
  }

  .post-header-row {
    flex-direction: column;
  }

  .post-meta {
    text-align: left;
  }

  .post-actions {
    flex-direction: row;
    flex-wrap: wrap;
  }
}
</style>
