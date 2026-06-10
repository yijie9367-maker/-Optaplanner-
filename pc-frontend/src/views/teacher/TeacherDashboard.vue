<template>
  <el-container class="teacher-dashboard">
    <!-- 左侧边栏 -->
    <el-aside width="240px" class="teacher-sidebar">
      <div class="sidebar-brand">
        <div class="brand-title">教师中心</div>
        <div class="brand-subtitle">{{ currentSemester }}</div>
      </div>

      <el-menu :default-active="activeTab" class="teacher-menu" @select="handleMenuSelect">
        <el-menu-item index="schedule">
          <el-icon><Calendar /></el-icon>
          <span>我的课表</span>
        </el-menu-item>
        <el-menu-item index="classes">
          <el-icon><Collection /></el-icon>
          <span>课程班级</span>
        </el-menu-item>
        <el-menu-item index="colleagues">
          <el-icon><UserFilled /></el-icon>
          <span>同事列表</span>
        </el-menu-item>
        <el-menu-item index="exams">
          <el-icon><Document /></el-icon>
          <span>期末考试</span>
        </el-menu-item>
        <el-menu-item index="forum">
          <el-icon><ChatDotRound /></el-icon>
          <span>校园论坛</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-summary">
        <div class="summary-card" @click="activeTab = 'classes'">
          <div class="summary-value">{{ teachingCoursesCount }}</div>
          <div class="summary-label">执教课程</div>
        </div>
        <div class="summary-card" @click="activeTab = 'colleagues'">
          <div class="summary-value">{{ colleaguesCount }}</div>
          <div class="summary-label">同事人数</div>
        </div>
      </div>

      <el-button type="primary" class="logout-btn" @click="handleLogout">
        <el-icon><SwitchButton /></el-icon>
        退出登录
      </el-button>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="dashboard-main">
      <el-main class="center-panel">
        <el-card class="main-card" shadow="never">
          <template #header>
            <div class="main-card-header">
              <div>
                <div class="main-title">{{ panelTitle }}</div>
                <div class="main-subtitle">
                  {{ currentUser?.name }}
                  <span v-if="currentUser?.title"> · {{ currentUser.title }}</span>
                  <span v-if="currentUser?.department"> · {{ currentUser.department }}</span>
                </div>
              </div>
              <div class="quick-switch">
                <el-button size="small" @click="activeTab = 'schedule'">课表</el-button>
                <el-button size="small" @click="activeTab = 'classes'">班级</el-button>
                <el-button size="small" @click="activeTab = 'colleagues'">同事</el-button>
                <el-button size="small" @click="activeTab = 'exams'">考试</el-button>
              </div>
            </div>
          </template>

          <!-- 我的课表 -->
          <div v-if="activeTab === 'schedule'" class="panel-body no-padding">
            <ScheduleVisualization :schedules="teacherSchedules" view-mode="teacher" />
          </div>

          <!-- 课程班级 -->
          <div v-else-if="activeTab === 'classes'" class="panel-body scrollable">
            <TeachingClasses :schedules="teacherSchedules" />
          </div>

          <!-- 同事列表 -->
          <div v-else-if="activeTab === 'colleagues'" class="panel-body scrollable">
            <ColleaguesList :teachers="colleagues" :loading="colleaguesLoading" />
          </div>

          <!-- 期末考试 -->
          <div v-else-if="activeTab === 'exams'" class="panel-body scrollable">
            <ExamManagement :teacher-id="currentUser?.id" :teacher-schedules="teacherSchedules" />
          </div>

          <div v-else-if="activeTab === 'forum'" class="panel-body scrollable">
            <ForumPage />
          </div>
        </el-card>
      </el-main>

      <!-- 右侧面板 -->
      <el-aside width="320px" class="right-panel">
        <el-card class="profile-card">
          <template #header>
            <div class="section-title">我的信息</div>
          </template>
          <div class="profile-avatar">{{ currentUser?.name?.charAt(0) ?? '师' }}</div>
          <div class="profile-name">{{ currentUser?.name }}</div>
          <div class="profile-line">职称：{{ currentUser?.title ?? '—' }}</div>
          <div class="profile-line">院系：{{ currentUser?.department ?? '—' }}</div>
          <div class="profile-line">身份：教师</div>
          <div class="profile-line">执教课程：{{ teachingCoursesCount }} 门</div>
        </el-card>

        <el-card class="notice-card">
          <template #header>
            <div class="section-title">消息栏目</div>
          </template>
          <div class="notices-list">
            <div v-for="notice in notices" :key="notice.id" class="notice-item">
              <div class="notice-title">{{ notice.title }}</div>
              <div class="notice-desc">{{ notice.description }}</div>
              <div class="notice-time">{{ formatTime(notice.time) }}</div>
            </div>
          </div>
        </el-card>
      </el-aside>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Calendar, Collection, UserFilled, SwitchButton, Document, ChatDotRound
} from '@element-plus/icons-vue'
import ScheduleVisualization from '@/components/ScheduleVisualization.vue'
import TeachingClasses from '@/views/teacher/TeachingClasses.vue'
import ColleaguesList from '@/views/teacher/ColleaguesList.vue'
import ExamManagement from '@/views/teacher/ExamManagement.vue'
import { messageAPI } from '@/api/message'
import ForumPage from '@/views/forum/ForumPage.vue'
import { authFetch } from '@/utils/authFetch'

const router = useRouter()
const userStore = useUserStore()

// 状态
const activeTab = ref('schedule')
const teacherSchedules = ref([])
const colleagues = ref([])
const colleaguesLoading = ref(false)

const notices = ref([])

const currentUser = computed(() => userStore.user)

const currentSemester = computed(() => {
  const now = new Date()
  const month = now.getMonth() + 1
  const year = now.getFullYear()
  if (month >= 9) return `${year}年秋季学期`
  if (month >= 2) return `${year}年春季学期`
  return `${year - 1}年冬季学期`
})

const teachingCoursesCount = computed(() => {
  return new Set(teacherSchedules.value.map(s => s.courseName)).size
})

const colleaguesCount = computed(() => colleagues.value.length)

const panelTitle = computed(() => {
  const map = {
    schedule: '我的课表',
    classes: '课程班级',
    colleagues: '同事列表',
    exams: '期末考试',
    forum: '校园论坛'
  }
  return map[activeTab.value] || '教师中心'
})

function handleMenuSelect(key) {
  activeTab.value = key
}

function formatTime(date) {
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) return `${Math.floor(diff / (1000 * 60))}分钟前`
    return `${hours}小时前`
  }
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

// 获取教师课表（ScheduleVO）
async function fetchSchedules() {
  try {
    const teacherId = currentUser.value?.id
    if (!teacherId) {
      console.warn('No teacherId available')
      return
    }

    const response = await authFetch(
      `/schedule/listByTeacherId?teacherId=${teacherId}`
    )
    const data = await response.json()
    const voList = data.data || []

    teacherSchedules.value = voList.map(vo => ({
      id: vo.id,
      courseId: vo.courseId,
      classGroupId: vo.classGroupId,
      courseName: vo.courseName || '未知课程',
      className: vo.className || '未知班级',
      teacherName: vo.teacherName || currentUser.value?.name || '',
      classroomName: vo.roomName || vo.building || 'TBA',
      weekDay: vo.weekDay,
      weekNumber: vo.weekNumber,
      timeSlot: vo.timeSlot,
      studentNum: vo.studentNum,
      duration: vo.duration || 2
    }))
  } catch (error) {
    console.error('获取课表失败:', error)
    ElMessage.error('获取课表失败，请检查服务器连接')
  }
}

// 获取同事列表（全部教师，过滤掉自己）
async function fetchColleagues() {
  colleaguesLoading.value = true
  try {
    const response = await authFetch('/teacher/list')
    const data = await response.json()
    const allTeachers = data.data || []
    // 排除自己
    colleagues.value = allTeachers.filter(t => t.id !== currentUser.value?.id)
  } catch (error) {
    console.error('获取同事列表失败:', error)
    ElMessage.error('获取同事列表失败')
  } finally {
    colleaguesLoading.value = false
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    router.push('/login')
  } catch {
    // 取消退出
  }
}

// 获取教师消息
async function fetchNotices() {
  try {
    const res = await messageAPI.getMessagesByTarget('teacher')
    const list = res.data || []
    notices.value = list.map(m => ({
      id: m.id,
      title: m.title,
      description: m.content && m.content.length > 60 ? m.content.substring(0, 60) + '...' : m.content,
      time: m.publishTime ? new Date(m.publishTime) : new Date(m.createdAt)
    }))
  } catch (e) {
    console.error('获取消息失败:', e)
  }
}

onMounted(() => {
  // 未登录保护
  if (!currentUser.value || currentUser.value.role !== 'teacher') {
    router.push('/login')
    return
  }
  fetchSchedules()
  fetchColleagues()
  fetchNotices()
})
</script>

<style scoped>
/* ===== 整体布局 ===== */
.teacher-dashboard {
  height: 100vh;
  overflow: hidden;
  background: #f0f2f5;
  display: flex;
}

/* ===== 左侧栏：白色背景，与学生端一致 ===== */
.teacher-sidebar {
  height: 100vh;
  background: #ffffff;
  border-right: 1px solid #e4e7ed;
  padding: 20px 12px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: hidden;
  flex-shrink: 0;
}

.sidebar-brand {
  padding: 8px 8px 16px;
  border-bottom: 1px solid #f0f2f5;
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
  color: #1989fa;
}

.brand-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.teacher-menu {
  border: none !important;
  background: transparent;
  flex: 1;
}

.teacher-menu :deep(.el-menu-item) {
  color: #606266;
  border-radius: 8px;
  margin-bottom: 4px;
  height: 44px;
  line-height: 44px;
}

.teacher-menu :deep(.el-menu-item:hover) {
  background: #ecf5ff;
  color: #1989fa;
}

.teacher-menu :deep(.el-menu-item.is-active) {
  background: #ecf5ff;
  color: #1989fa;
  font-weight: 600;
}

.teacher-menu :deep(.el-menu-item .el-icon) {
  color: inherit;
}

.sidebar-summary {
  display: grid;
  gap: 10px;
}

.summary-card {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  padding: 12px 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.summary-card:hover {
  background: #ecf5ff;
  border-color: #b3d8ff;
}

.summary-value {
  font-size: 22px;
  font-weight: 700;
  color: #1989fa;
}

.summary-label {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.logout-btn {
  width: 100%;
}

/* ===== 中右区域 ===== */
.dashboard-main {
  height: 100vh;
  overflow: hidden;
  flex: 1;
  display: flex;
  flex-direction: row;
  padding: 16px;
  gap: 16px;
  min-width: 0;
}

.center-panel {
  height: calc(100vh - 32px);
  overflow: hidden;
  padding: 0;
  flex: 1;
  min-width: 0;
}

.center-panel.el-main {
  padding: 0;
}

.main-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.main-card :deep(.el-card__body) {
  flex: 1;
  overflow: hidden;
  padding: 0;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.main-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.main-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2d3d;
}

.main-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

.quick-switch {
  display: flex;
  gap: 8px;
}

.panel-body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  padding: 12px;
}

.panel-body.no-padding {
  padding: 0;
}

.panel-body.scrollable {
  overflow-y: auto;
  padding: 16px;
}

/* ===== 右侧栏 ===== */
.right-panel {
  height: calc(100vh - 32px);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex-shrink: 0;
  padding: 0;
}

.profile-card,
.notice-card {
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.notice-card {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.notice-card :deep(.el-card__body) {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  padding: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 700;
  color: #1f2d3d;
}

.profile-avatar {
  width: 54px;
  height: 54px;
  border-radius: 50%;
  background: #ecf5ff;
  color: #1989fa;
  font-size: 22px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  border: 2px solid #d9ecff;
}

.profile-name {
  font-size: 20px;
  font-weight: 700;
  color: #1989fa;
  text-align: center;
  margin-bottom: 10px;
}

.profile-line {
  font-size: 13px;
  color: #4e5d6c;
  margin-bottom: 8px;
}

.notices-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  height: 100%;
}

.notice-item {
  padding: 10px 12px;
  background: #f4f9ff;
  border-radius: 8px;
  border-left: 3px solid #1989fa;
  flex-shrink: 0;
}

.notice-title {
  font-size: 13px;
  font-weight: 600;
  color: #1f2d3d;
  margin-bottom: 4px;
}

.notice-desc {
  font-size: 12px;
  color: #6a7b92;
  margin-bottom: 4px;
}

.notice-time {
  font-size: 11px;
  color: #bbb;
}
</style>
