<template>
  <el-container class="dashboard-container">
    <!-- 左侧侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '200px'" class="sidebar">

      <!-- 收起/展开按钮 -->
      <div class="collapse-toggle" @click="isCollapsed = !isCollapsed">
        <el-icon :size="20">
          <Expand v-if="isCollapsed" />
          <Fold v-else />
        </el-icon>
      </div>

      <el-menu
        :default-active="$route.path"
        class="el-menu-vertical-demo"
        background-color="#79BAFF"
        text-color="#fff"
        active-text-color="#ffd700"
        :collapse="isCollapsed"
        router
      >
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>

          <!-- ===== 基础信息 ===== -->
          <el-sub-menu index="base-info">
            <template #title>
              <el-icon><Folder /></el-icon>
              <span>基础信息</span>
            </template>
            <el-menu-item index="/dashboard/admins">
              <el-icon><User /></el-icon>
              <span>管理员管理</span>
            </el-menu-item>
            <el-menu-item index="/dashboard/students">
              <el-icon><UserFilled /></el-icon>
              <span>学生管理</span>
            </el-menu-item>
            <el-menu-item index="/dashboard/teachers">
              <el-icon><Avatar /></el-icon>
              <span>教师管理</span>
            </el-menu-item>
            <el-menu-item index="/dashboard/courses">
              <el-icon><Document /></el-icon>
              <span>课程管理</span>
            </el-menu-item>
            <el-menu-item index="/dashboard/classroom">
              <el-icon><OfficeBuilding /></el-icon>
              <span>教室管理</span>
            </el-menu-item>
            <el-menu-item index="/dashboard/class-group">
              <el-icon><Collection /></el-icon>
              <span>班级管理</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- ===== 消息通知 ===== -->
          <el-sub-menu index="messages">
            <template #title>
              <el-icon><Bell /></el-icon>
              <span>消息通知</span>
            </template>
            <el-menu-item index="/dashboard/messages-admin">
              <el-icon><Bell /></el-icon>
              <span>管理员消息</span>
            </el-menu-item>
            <el-menu-item index="/dashboard/messages-teacher">
              <el-icon><ChatDotRound /></el-icon>
              <span>教师消息</span>
            </el-menu-item>
            <el-menu-item index="/dashboard/messages-student">
              <el-icon><ChatLineRound /></el-icon>
              <span>学生消息</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- ===== 论坛管理 ===== -->
          <el-sub-menu index="forum-group">
            <template #title>
              <el-icon><ChatDotRound /></el-icon>
              <span>论坛管理</span>
            </template>
            <el-menu-item index="/dashboard/forum">
              <el-icon><ChatDotRound /></el-icon>
              <span>校园论坛</span>
            </el-menu-item>
            <el-menu-item index="/dashboard/sensitive-words">
              <el-icon><WarningFilled /></el-icon>
              <span>违禁词管理</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- ===== 考试管理 ===== -->
          <el-sub-menu index="exam-group">
            <template #title>
              <el-icon><School /></el-icon>
              <span>考试管理</span>
            </template>
            <el-menu-item index="/dashboard/exams">
              <el-icon><Document /></el-icon>
              <span>期末考试</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- ===== 智能排课 ===== -->
          <el-menu-item index="/dashboard/scheduling">
            <el-icon><Calendar /></el-icon>
            <span>智能排课</span>
          </el-menu-item>
      </el-menu>

      <el-button
        v-show="!isCollapsed"
        type="primary"
        class="logout-btn"
        @click="logout"
        size="medium"
      >
          <span>退出登录</span>
      </el-button>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <el-header class="main-header">
        <h1 class="welcome">
          Welcome!
        </h1>
      </el-header>

      <el-main class="main-content">
        <el-row v-if="$route.path === '/dashboard'" :gutter="20" class="overview">
          <el-col :span="6">
            <el-card shadow="hover" class="card">
              <h3>学生总数</h3>
              <p>{{ stats.studentCount }}</p>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="card">
              <h3>教师总数</h3>
              <p>{{ stats.teacherCount }}</p>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="card">
              <h3>课程总数</h3>
              <p>{{ stats.courseCount }}</p>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="card">
              <h3>排课记录</h3>
              <p>{{ stats.scheduleCount }}</p>
            </el-card>
          </el-col>
        </el-row>

        <!-- 消息列表区域 - 仅在首页显示 -->
        <div v-if="$route.path === '/dashboard'" class="messages-section">
          <h3 class="section-title">📢 最新消息</h3>
          <div v-if="messages.length === 0" class="empty-messages">
            <el-empty description="暂无消息"></el-empty>
          </div>
          <el-card v-for="message in messages" :key="message.id" shadow="hover" class="message-item">
            <div class="message-header">
              <span class="message-type" :class="`type-${message.type}`">{{ getTypeLabel(message.type) }}</span>
              <span class="message-title">{{ message.title }}</span>
              <span class="message-time">{{ formatDate(message.publishTime) }}</span>
            </div>
            <div class="message-content">{{ truncateContent(message.content, 150) }}</div>
            <div class="message-footer">
              <span class="stat"><el-icon><View /></el-icon> {{ message.views }}</span>
              <span class="stat"><el-icon><Star /></el-icon> {{ message.likes }}</span>
              <el-button type="primary" text size="small" @click="viewMessage(message)">查看详情</el-button>
            </div>
          </el-card>
        </div>

        <!-- 消息详情对话框 -->
        <el-dialog
          v-if="$route.path === '/dashboard'"
          v-model="detailDialogVisible"
          :title="detailData ? detailData.title : '消息详情'"
          width="700px"
          @close="detailData = null"
        >
          <div v-if="detailData" class="message-detail">
            <div class="detail-header">
              <span class="detail-badge" :class="`type-${detailData.type}`">
                {{ getTypeLabel(detailData.type) }}
              </span>
              <span class="detail-meta">{{ detailData.publisherName }} · {{ formatDate(detailData.publishTime) }}</span>
            </div>
            <div class="detail-content">{{ detailData.content }}</div>
            <div class="detail-stats">
              浏览: <strong>{{ detailData.views }}</strong> | 点赞: <strong>{{ detailData.likes }}</strong>
            </div>
          </div>
          <template #footer>
            <el-button @click="detailDialogVisible = false">关闭</el-button>
          </template>
        </el-dialog>

        <KeepAlive>
          <router-view></router-view>
        </KeepAlive>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user.js'
import {
  HomeFilled,
  User,
  UserFilled,
  Avatar,
  Document,
  OfficeBuilding,
  Collection,
  Calendar,
  View,
  Star,
  Bell,
  ChatDotRound,
  ChatLineRound,
  WarningFilled,
  Folder,
  School,
  Fold,
  Expand
} from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api/admin'
import { messageAPI } from '@/api/message'

const router = useRouter()
const userStore = useUserStore()

// 侧边栏折叠
const isCollapsed = ref(false)

// 统计数据
const stats = ref({
  studentCount: 0,
  teacherCount: 0,
  courseCount: 0,
  scheduleCount: 0
})

// 消息数据
const messages = ref([])
const detailDialogVisible = ref(false)
const detailData = ref(null)

// 获取统计数据
const fetchStats = async () => {
  try {
    const res = await getDashboardStats()
    stats.value = {
      studentCount: res.data?.studentCount || 0,
      teacherCount: res.data?.teacherCount || 0,
      courseCount: res.data?.courseCount || 0,
      scheduleCount: res.data?.scheduleCount || 0
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

// 获取消息列表（只显示 target=admin 的消息）
const fetchMessages = async () => {
  try {
    const res = await messageAPI.getAdminMessagesByTarget('admin')
    messages.value = res.data || []
  } catch (error) {
    console.error('获取消息失败', error)
  }
}

// 消息类型标签
const getTypeLabel = (type) => {
  const typeMap = {
    'announcement': '公告',
    'notice': '通知',
    'event': '活动'
  }
  return typeMap[type] || type
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 截断内容
const truncateContent = (content, length) => {
  if (!content) return ''
  return content.length > length ? content.substring(0, length) + '...' : content
}

// 查看消息详情
const viewMessage = async (message) => {
  try {
    const res = await messageAPI.getMessageDetail(message.id)
    detailData.value = res.data
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取消息详情失败', error)
  }
}

// 退出登录
const logout = () => {
  userStore.logout()
  router.push('/login/admin')
}

onMounted(() => {
  fetchStats()
  fetchMessages()
})
</script>

<style scoped>
.dashboard-container {
  height: 100vh;
  font-family: "Helvetica Neue", Arial, sans-serif;
}

/* 侧边栏 */
.sidebar {
  display: flex;
  flex-direction: column;
  background-color: #79BAFF;
  color: #fff;
  padding: 0;
  transition: width 0.3s;
}

/* 收起/展开按钮 */
.collapse-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 44px;
  cursor: pointer;
  color: #fff;
  transition: background 0.2s;
}
.collapse-toggle:hover {
  background-color: rgba(255,255,255,0.15);
}

.logout-btn {
  margin: auto 10px 10px 10px;
  width: calc(100% - 20px);
  padding: 12px 0;
  font-weight: bold;
  background-color: #ff4d4f;
  color: #fff;
  border-radius: 4px;
}

/* 侧边栏子菜单样式 */
:deep(.el-sub-menu__title) {
  color: #fff !important;
}
:deep(.el-sub-menu__title:hover) {
  background-color: rgba(255,255,255,0.15) !important;
}

/* 主内容区 */
.main-header {
  background-color: #f5faff;
  padding: 20px;
  font-size: 20px;
  border-bottom: 1px solid #d0dfff;
}
.welcome {
  margin: 0;
  color: #333;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 0.5px;
}
.main-content {
  padding: 20px;
  background-color: #f0f4ff;
  min-height: calc(100vh - 64px);
}

/* 首页概览卡片 */
.overview {
  margin-bottom: 30px;
}
.card {
  text-align: center;
  padding: 20px;
}
.card h3 {
  color: #555;
  margin-bottom: 10px;
}
.card p {
  font-size: 28px;
  font-weight: bold;
  color: #1890ff;
}

/* 消息列表区域 */
.messages-section {
  margin-top: 30px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #40B0FF;
}

.empty-messages {
  background: white;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
}

.message-item {
  margin-bottom: 15px;
  border-left: 4px solid #40B0FF;
  transition: all 0.3s ease;
}

.message-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1) !important;
  transform: translateY(-2px);
}

.message-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.message-type {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.type-announcement {
  background-color: #e6f7ff;
  color: #0050b3;
}

.type-notice {
  background-color: #f6ffed;
  color: #274e2b;
}

.type-event {
  background-color: #fff7e6;
  color: #974806;
}

.message-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  flex: 1;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 10px;
}

.message-footer {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 12px;
  color: #999;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
}

.message-detail {
  padding: 20px 0;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.detail-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #999;
}

.detail-content {
  font-size: 14px;
  color: #555;
  line-height: 1.8;
  white-space: pre-wrap;
  word-wrap: break-word;
  margin-bottom: 15px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.detail-stats {
  font-size: 12px;
  color: #666;
  padding-top: 10px;
  border-top: 1px solid #eee;
}
</style>
