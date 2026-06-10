<template>
  <el-container class="student-dashboard">
    <el-aside width="240px" class="student-sidebar">
      <div class="sidebar-brand">
        <div class="brand-title">学生中心</div>
        <div class="brand-subtitle">{{ currentSemester }}</div>
      </div>

      <el-menu :default-active="activeTab" class="student-menu" @select="handleMenuSelect">
        <el-menu-item index="schedule">
          <el-icon><Calendar /></el-icon>
          <span>查看课表</span>
        </el-menu-item>
        <el-menu-item index="courses">
          <el-icon><Collection /></el-icon>
          <span>查看课程</span>
        </el-menu-item>
        <el-menu-item index="classmates">
          <el-icon><User /></el-icon>
          <span>查看同学</span>
        </el-menu-item>
        <el-menu-item index="grades">
          <el-icon><Medal /></el-icon>
          <span>成绩查询</span>
        </el-menu-item>
        <el-menu-item index="exams">
          <el-icon><Document /></el-icon>
          <span>考试安排</span>
        </el-menu-item>
        <el-menu-item index="forum">
          <el-icon><ChatDotRound /></el-icon>
          <span>校园论坛</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-summary">
        <div class="summary-card" @click="showCourseDetail">
          <div class="summary-value">{{ totalCourses }}</div>
          <div class="summary-label">本学期课程</div>
        </div>
        <div class="summary-card" @click="showClassmatesDetail">
          <div class="summary-value">{{ classmatesCount }}</div>
          <div class="summary-label">班级同学</div>
        </div>
      </div>

      <el-button type="primary" class="logout-btn" @click="handleLogout">
        <el-icon><SwitchButton /></el-icon>
        退出登录
      </el-button>
    </el-aside>

    <el-container class="dashboard-main">
      <el-main class="center-panel">
        <el-card class="main-card" shadow="never">
          <template #header>
            <div class="main-card-header">
              <div>
                <div class="main-title">{{ panelTitle }}</div>
                <div class="main-subtitle">{{ currentUser?.name }} · {{ currentUser?.className || '未分配班级' }}</div>
              </div>
              <div class="quick-switch">
                <el-button size="small" @click="activeTab = 'schedule'">课表</el-button>
                <el-button size="small" @click="activeTab = 'courses'">课程</el-button>
                <el-button size="small" @click="activeTab = 'classmates'">同学</el-button>
              </div>
            </div>
          </template>

          <div v-if="activeTab === 'schedule'" class="panel-body no-padding">
            <ScheduleVisualization :schedules="studentSchedules" />
          </div>

          <div v-else-if="activeTab === 'courses'" class="panel-body scrollable">
            <CoursesList :courses="studentCourses" />
          </div>

          <div v-else-if="activeTab === 'classmates'" class="panel-body scrollable">
            <StudentsList :students="classmates" :loading="classmatesLoading" />
          </div>

          <div v-else-if="activeTab === 'grades'" class="panel-body scrollable">
            <div class="grades-section">
              <el-empty v-if="gradesList.length === 0" description="暂无成绩数据" />
              <el-table v-else :data="gradesList">
                <el-table-column prop="courseName" label="课程名称" min-width="180" />
                <el-table-column prop="credit" label="学分" width="90" />
                <el-table-column prop="score" label="成绩" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.score >= 60 ? 'success' : 'danger'">{{ row.score }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="gpa" label="绩点" width="90" />
              </el-table>
            </div>
          </div>

          <div v-else-if="activeTab === 'forum'" class="panel-body scrollable">
            <ForumPage />
          </div>

          <div v-else class="panel-body scrollable">
            <div v-if="examLoading" class="exam-loading"><el-text>加载中...</el-text></div>
            <el-empty v-else-if="examList.length === 0" description="暂无考试安排" />
            <div v-else class="exam-list">
              <div v-for="exam in examList" :key="exam.id" class="exam-card">
                <div class="exam-header">
                  <span class="exam-course">{{ exam.courseName }}</span>
                  <el-tag v-if="getMyScore(exam.id) !== null" :type="getMyScore(exam.id) >= 60 ? 'success' : 'danger'" size="small">
                    我的成绩: {{ getMyScore(exam.id) }}
                  </el-tag>
                  <el-tag v-else type="info" size="small">尚未公布成绩</el-tag>
                </div>
                <div class="exam-info">
                  <span>📅 {{ exam.examDate }}</span>
                  <span>⏰ {{ exam.startTime }} ~ {{ exam.endTime }}</span>
                  <span>📍 {{ exam.location }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-main>

      <el-aside width="320px" class="right-panel">
        <el-card class="profile-card">
          <template #header>
            <div class="section-title">我的信息</div>
          </template>
          <div class="profile-name">{{ currentUser?.name }}</div>
          <div class="profile-line">班级：{{ currentUser?.className || '未分配' }}</div>
          <div class="profile-line">角色：学生</div>
          <div class="profile-line">已修课程：{{ completedCourses }}</div>
        </el-card>

        <el-card class="notice-card">
          <template #header>
            <div class="section-title">消息栏目</div>
          </template>
          <el-empty v-if="notices.length === 0" description="暂无通知" />
          <div v-else class="notices-list">
            <div v-for="notice in notices" :key="notice.id" class="notice-item">
              <div class="notice-title">{{ notice.title }}</div>
              <div class="notice-desc">{{ notice.description }}</div>
              <div class="notice-time">{{ formatTime(notice.time) }}</div>
            </div>
          </div>
        </el-card>
      </el-aside>
    </el-container>

    <!-- 课程详情对话框 -->
    <el-dialog v-model="courseDialogVisible" title="本学期课程" width="70%">
      <div class="course-detail-grid">
        <div v-for="course in studentCourses" :key="course.id" class="course-card">
          <div class="card-header">
            <h4>{{ course.name }}</h4>
          </div>
          <div class="card-body">
            <p><strong>授课教师：</strong>{{ course.teacher }}</p>
            <p><strong>学分：</strong>{{ course.credits }} 学分</p>
            <p><strong>课程类型：</strong>{{ course.type }}</p>
            <p><strong>状态：</strong>
              <el-tag :type="course.status === 'studying' ? 'success' : 'info'">
                {{ course.status === 'studying' ? '学习中' : '已完成' }}
              </el-tag>
            </p>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 班级同学详情对话框 -->
    <el-dialog v-model="classmatesDialogVisible" title="班级同学" width="70%">
      <div class="classmates-grid">
        <div v-for="classmate in classmates" :key="classmate.id" class="classmate-card">
          <div class="avatar">👤</div>
          <div class="info">
            <h4>{{ classmate.name }}</h4>
            <p>学号：{{ classmate.studentId }}</p>
            <p>专业：{{ classmate.major }}</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Calendar, SwitchButton, Collection, User, Medal, Document, ChatDotRound
} from '@element-plus/icons-vue'
import ScheduleVisualization from '@/components/ScheduleVisualization.vue'
import StudentsList from '@/views/student/StudentsList.vue'
import { messageAPI } from '@/api/message'
import CoursesList from '@/views/student/CoursesList.vue'
import ForumPage from '@/views/forum/ForumPage.vue'
import { authFetch } from '@/utils/authFetch'

const router = useRouter()
const userStore = useUserStore()

// 状态
const activeTab = ref('schedule')
const studentSchedules = ref([])
const classmates = ref([])
const studentCourses = ref([])
const classmatesLoading = ref(false)
const courseDialogVisible = ref(false)
const classmatesDialogVisible = ref(false)
const gradesList = ref([])
const examList = ref([])
const examLoading = ref(false)
const myGradesMap = ref({}) // examId -> score
const notices = ref([])

// 当前用户信息
const currentUser = computed(() => userStore.user)

// 当前学期
const currentSemester = computed(() => {
  const now = new Date()
  const month = now.getMonth() + 1
  const year = now.getFullYear()
  
  if (month >= 9) {
    return `${year}年秋季学期`
  } else if (month >= 2) {
    return `${year}年春季学期`
  } else {
    return `${year - 1}年冬季学期`
  }
})

// 统计信息
const totalCourses = computed(() => studentCourses.value.length)
const completedCourses = computed(() => {
  return studentCourses.value.filter(c => c.status === 'completed').length
})
const classmatesCount = computed(() => classmates.value.length)
const panelTitle = computed(() => {
  const titleMap = {
    schedule: '我的课表',
    courses: '课程列表',
    classmates: '班级同学',
    grades: '成绩查询',
    exams: '考试安排',
    forum: '校园论坛'
  }
  return titleMap[activeTab.value] || '学生中心'
})

// 格式化时间
function formatTime(date) {
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60))
      return `${minutes}分钟前`
    }
    return `${hours}小时前`
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

// 获取按班级的考试列表
async function fetchExams() {
  examLoading.value = true
  try {
    const classGroupId = currentUser.value?.classGroupId
    if (!classGroupId) return
    const res = await authFetch(`/exam/listByClassGroupId?classGroupId=${classGroupId}`)
    const data = await res.json()
    examList.value = data.data || []
  } catch (e) {
    console.error('获取考试列表失败:', e)
  } finally {
    examLoading.value = false
  }
}

// 获取自己的所有考试成绩
async function fetchMyGrades() {
  try {
    const studentId = currentUser.value?.id
    if (!studentId) return
    const res = await authFetch(`/exam-grade/listByStudentId?studentId=${studentId}`)
    const data = await res.json()
    const grades = data.data || []
    const map = {}
    grades.forEach(g => { map[g.examId] = g.score })
    myGradesMap.value = map
  } catch (e) {
    console.error('获取成绩失败:', e)
  }
}

// 获取学生消息
async function fetchNotices() {
  try {
    const res = await messageAPI.getMessagesByTarget('student')
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

function getMyScore(examId) {
  const s = myGradesMap.value[examId]
  return s !== undefined ? Number(s) : null
}

// 获取学生课表
async function fetchSchedules() {
  try {
    const classGroupId = currentUser.value?.classGroupId
    if (!classGroupId) {
      console.warn('No classGroupId available')
      return
    }
    
    const scheduleResponse = await authFetch(
      `/schedule/listByClassGroupId?classGroupId=${classGroupId}`
    )
    const scheduleData = await scheduleResponse.json()
    const schedules = scheduleData.data || []
    
    const coursesResponse = await authFetch(
      `/course/listByClassGroupId?classGroupId=${classGroupId}`
    )
    const coursesData = await coursesResponse.json()
    const courses = coursesData.data || []
    
    const courseMap = new Map()
    courses.forEach(course => {
      courseMap.set(course.id, course)
    })
    
    studentSchedules.value = schedules.map(schedule => {
      const course = courseMap.get(schedule.courseId)
      return {
        id: schedule.id,
        courseName: course?.name || 'Unknown Course',
        teacherName: course?.teacherName || 'Unknown',
        classroomName: schedule.classroom || 'TBA',
        weekDay: schedule.weekDay,
        weekNumber: schedule.weekNumber,
        timeSlot: schedule.timeSlot,
        type: 'required',
        duration: 2
      }
    })

    studentCourses.value = courses.map(course => ({
      id: course.id,
      name: course.name,
      teacher: course.teacherName || '待分配教师',
      credits: Number(course.credit) || 0,
      type: course.courseType === '选修课' ? '选修' : '必修',
      status: 'studying'
    }))
  } catch (error) {
    console.error('获取课表失败:', error)
    ElMessage.error('获取课表失败')
  }
}

// 获取班级同学
async function fetchClassmates() {
  classmatesLoading.value = true
  try {
    const classGroupId = currentUser.value?.classGroupId
    if (!classGroupId) {
      console.warn('No classGroupId available')
      return
    }
    
    const response = await authFetch(
      `/student/listByClassGroupId?classGroupId=${classGroupId}`
    )
    const responseData = await response.json()
    const students = responseData.data || []
    
    classmates.value = students
      .filter(s => s.id !== currentUser.value?.id)
      .map(s => ({
        id: s.id,
        studentId: s.studentNumber || s.id,
        name: s.name,
        email: s.email,
        major: s.major || '计算机科学与技术',
        classGroupId: s.classGroupId
      }))
  } catch (error) {
    console.error('获取班级同学失败:', error)
    ElMessage.error('获取班级同学失败')
  } finally {
    classmatesLoading.value = false
  }
}

// 显示课程详情
function showCourseDetail() {
  courseDialogVisible.value = true
}

// 显示班级同学详情
function showClassmatesDetail() {
  classmatesDialogVisible.value = true
}

function handleMenuSelect(index) {
  activeTab.value = index
}

// 登出处理
async function handleLogout() {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/student-login')
  } catch {
    // 用户取消
  }
}

// 初始化
onMounted(() => {
  if (!currentUser.value || currentUser.value.role !== 'student') {
    router.push('/student-login')
    return
  }

  fetchSchedules()
  fetchClassmates()
  fetchExams()
  fetchMyGrades()
  fetchNotices()
})
</script>

<style scoped>
/* ===== 整体布局：锁定在视口内，不产生页面滚动 ===== */
.student-dashboard {
  height: 100vh;
  overflow: hidden;
  background: #f0f2f5;
  display: flex;
}

/* ===== 左侧栏：白色背景 ===== */
.student-sidebar {
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
  padding: 8px 8px 4px;
  border-bottom: 1px solid #f0f2f5;
  padding-bottom: 16px;
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

.student-menu {
  border: none !important;
  background: transparent;
  flex: 1;
}

.student-menu :deep(.el-menu-item) {
  color: #606266;
  border-radius: 8px;
  margin-bottom: 4px;
  height: 44px;
  line-height: 44px;
}

.student-menu :deep(.el-menu-item:hover) {
  background: #ecf5ff;
  color: #1989fa;
}

.student-menu :deep(.el-menu-item.is-active) {
  background: #ecf5ff;
  color: #1989fa;
  font-weight: 600;
}

.student-menu :deep(.el-menu-item .el-icon) {
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

/* ===== 中右区域：固定高度，不溢出 ===== */
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

/* el-main 默认有内边距，覆盖掉 */
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

/* ===== 右侧栏：固定高度，两卡片撑满 ===== */
.right-panel {
  height: calc(100vh - 32px);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex-shrink: 0;
  padding: 0;
}

.profile-card {
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
}

.notice-card {
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
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

.profile-name {
  font-size: 20px;
  font-weight: 700;
  color: #1989fa;
  margin-bottom: 10px;
}

.profile-line {
  font-size: 13px;
  color: #4e5d6c;
  margin-bottom: 8px;
}

.grades-section {
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.grades-info {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.info-item {
  padding: 14px;
  background: #f4f9ff;
  border-radius: 10px;
  border: 1px solid #d8eaff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-item .label {
  color: #6a7b92;
  font-size: 13px;
}

.info-item .value {
  color: #1989fa;
  font-size: 16px;
  font-weight: 700;
}

.course-detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  padding: 4px;
}

.course-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
  transition: box-shadow 0.2s;
}

.course-card:hover {
  box-shadow: 0 4px 16px rgba(25, 137, 250, 0.15);
}

.card-header {
  background: #ecf5ff;
  color: #1989fa;
  padding: 12px 14px;
  border-bottom: 1px solid #d9ecff;
}

.card-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.card-body {
  padding: 12px 14px;
}

.card-body p {
  margin: 8px 0;
  font-size: 13px;
  color: #555;
  line-height: 1.5;
}

.card-body strong {
  color: #333;
}

.classmates-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
  padding: 4px;
}

.classmate-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  border: 1px solid #e4e7ed;
  transition: box-shadow 0.2s;
}

.classmate-card:hover {
  box-shadow: 0 4px 16px rgba(25, 137, 250, 0.12);
}

.avatar {
  font-size: 40px;
  margin: 6px 0;
}

.classmate-card h4 {
  margin: 8px 0;
  color: #333;
  font-weight: 600;
  font-size: 14px;
}

.classmate-card p {
  margin: 6px 0;
  font-size: 12px;
  color: #666;
}

.notices-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notice-item {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  border-left: 3px solid #1989fa;
}

.notice-title {
  font-size: 13px;
  font-weight: 600;
  color: #213547;
  margin-bottom: 4px;
}

.notice-desc {
  font-size: 12px;
  color: #6a7b92;
  line-height: 1.5;
}

.notice-time {
  margin-top: 6px;
  font-size: 11px;
  color: #9aa9bd;
}

/* ===== 考试安排 ===== */
.exam-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.exam-card {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e4e7ed;
  padding: 16px;
  transition: box-shadow 0.2s;
}

.exam-card:hover {
  box-shadow: 0 4px 16px rgba(25, 137, 250, 0.12);
}

.exam-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.exam-course {
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
  flex: 1;
}

.exam-info {
  display: flex;
  gap: 18px;
  font-size: 13px;
  color: #606266;
  flex-wrap: wrap;
}

.exam-loading {
  padding: 40px;
  text-align: center;
}
</style>
