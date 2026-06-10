<template>
  <div class="courses-list-container">
    <!-- 顶部搜索和统计 -->
    <div class="header-section">
      <div class="search-section">
        <el-input
          v-model="searchQuery"
          placeholder="🔍 搜索课程名称或教师..."
          clearable
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header-title">
              <el-icon style="color: #1989fa;"><Collection /></el-icon>
              总课程数
            </div>
          </template>
          <div class="stat-number">{{ filteredCourses.length }}</div>
          <div class="stat-desc">个课程</div>
        </el-card>

        <el-card class="stat-card">
          <template #header>
            <div class="card-header-title">
              <el-icon style="color: #1989fa;"><DocumentCopy /></el-icon>
              已完成
            </div>
          </template>
          <div class="stat-number">{{ completedCount }}</div>
          <div class="stat-desc">个课程</div>
        </el-card>

        <el-card class="stat-card">
          <template #header>
            <div class="card-header-title">
              <el-icon style="color: #1989fa;"><Medal /></el-icon>
              学分进度
            </div>
          </template>
          <el-progress :percentage="progressPercentage" color="#1989fa"></el-progress>
          <div class="progress-detail">{{ completedCredits }}/{{ totalCredits }} 分</div>
        </el-card>
      </div>
    </div>

    <!-- 表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header-title">课程列表</div>
      </template>

      <el-empty v-if="filteredCourses.length === 0" description="暂无课程数据" />

      <el-table
        v-else
        :data="filteredCourses"
        stripe
        style="width: 100%"
        row-class-name="course-row"
      >
        <!-- 课程名称列 -->
        <el-table-column prop="name" label="课程名称" width="280">
          <template #default="{ row }">
            <div class="course-name-cell">
              <div class="course-badge" :style="{ background: getCourseColor(row.id) }"></div>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- 授课教师列 -->
        <el-table-column prop="teacher" label="授课教师" width="160" />

        <!-- 课程类型列 -->
        <el-table-column prop="type" label="课程类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === '必修' ? 'success' : 'info'" size="small">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 学分列 -->
        <el-table-column prop="credits" label="学分" width="100" align="center">
          <template #default="{ row }">
            <el-badge :value="row.credits" class="credit-badge" />
          </template>
        </el-table-column>

        <!-- 修学状态列 -->
        <el-table-column prop="status" label="修学状态" width="140" align="center">
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.status)"
              effect="light"
              size="small"
            >
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Collection, DocumentCopy, Medal, Search } from '@element-plus/icons-vue'

const props = defineProps({
  courses: {
    type: Array,
    default: () => []
  }
})

const searchQuery = ref('')

const colors = [
  '#667eea', '#764ba2', '#f093fb', '#f5576c',
  '#4facfe', '#00f2fe', '#43e97b', '#38f9d7',
  '#fa709a', '#fee140'
]

const filteredCourses = computed(() => {
  if (!searchQuery.value) return props.courses
  
  const query = searchQuery.value.toLowerCase()
  return props.courses.filter(course => 
    course.name.toLowerCase().includes(query) ||
    course.teacher.toLowerCase().includes(query)
  )
})

const completedCount = computed(() => {
  return props.courses.filter(c => c.status === 'completed').length
})

const totalCredits = computed(() => {
  return props.courses.reduce((sum, c) => sum + (c.credits || 0), 0)
})

const completedCredits = computed(() => {
  return props.courses
    .filter(c => c.status === 'completed')
    .reduce((sum, c) => sum + (c.credits || 0), 0)
})

const progressPercentage = computed(() => {
  if (totalCredits.value === 0) return 0
  return Math.round((completedCredits.value / totalCredits.value) * 100)
})

function getCourseColor(courseId) {
  return colors[(courseId - 1) % colors.length]
}

function getStatusType(status) {
  const types = {
    'studying': 'warning',
    'completed': 'success',
    'failed': 'danger',
    'suspended': 'info'
  }
  return types[status] || 'info'
}

function getStatusLabel(status) {
  const labels = {
    'studying': '修学中',
    'completed': '已完成',
    'failed': '未通过',
    'suspended': '已暂停'
  }
  return labels[status] || '未知'
}
</script>

<style scoped>
.courses-list-container {
  padding: 0;
}

/* ==================== 顶部区域 ==================== */
.header-section {
  margin-bottom: 30px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.search-section {
  flex: 1;
}

.search-input {
  width: 100%;
  max-width: 400px;
}

.search-input :deep(.el-input__wrapper) {
  box-shadow: 0 2px 8px rgba(25, 137, 250, 0.1);
}

/* ==================== 统计卡片 ==================== */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.stat-card :deep(.el-card__header) {
  padding: 16px;
  border-bottom: 2px solid #f0f0f0;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(25, 137, 250, 0.12);
}

.card-header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #1989fa;
  margin: 12px 0 6px 0;
  text-align: center;
}

.stat-desc {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
  text-align: center;
}

.progress-detail {
  font-size: 12px;
  color: #666;
  margin-top: 8px;
  text-align: center;
}

/* ==================== 表格卡片 ==================== */
.table-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.table-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 2px solid #f0f0f0;
}

.table-header-title {
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

/* ==================== 表格样式 ==================== */
.courses-list-container :deep(.el-table) {
  border-radius: 0;
}

.courses-list-container :deep(.el-table__header) {
  background: linear-gradient(135deg, #f0f8ff 0%, #e3f2fd 100%);
}

.courses-list-container :deep(.el-table__header th) {
  background: transparent;
  font-weight: 600;
  color: #1989fa;
  border-bottom: 2px solid #1989fa;
}

.course-row {
  transition: all 0.2s ease;
}

.course-row:hover > td {
  background-color: #f0f8ff !important;
}

.course-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.course-badge {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.credit-badge {
  font-weight: 600;
  color: #1989fa;
}

.courses-list-container :deep(.el-table__row td) {
  border-color: #f0f0f0;
  padding: 16px;
}

/* ==================== 响应式 ==================== */
@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
}

@media (max-width: 768px) {
  .header-section {
    flex-direction: column;
  }

  .search-input {
    width: 100%;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }
}
</style>
