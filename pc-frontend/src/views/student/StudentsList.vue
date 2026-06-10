<template>
  <div class="students-list-container">
    <!-- 顶部搜索 -->
    <div class="header-section">
      <el-input
        v-model="searchQuery"
        placeholder="🔍 搜索同学名字或学号..."
        clearable
        class="search-input"
        @keyup.enter="currentPage = 1"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-statistic title="班级人数" :value="filteredStudents.length" />
    </div>

    <!-- 空状态 -->
    <el-empty v-if="filteredStudents.length === 0" description="暂无数据" />

    <!-- 学生卡片网格 -->
    <div v-else class="students-grid">
      <el-card
        v-for="student in paginatedStudents"
        :key="student.id"
        class="student-card"
      >
        <!-- 卡片头部（头像+姓名） -->
        <template #header>
          <div class="card-header">
            <div class="student-avatar">
              {{ student.name.charAt(0).toUpperCase() }}
            </div>
            <div class="student-info">
              <div class="student-name">{{ student.name }}</div>
              <div class="student-id">{{ student.studentId }}</div>
            </div>
          </div>
        </template>

        <!-- 卡片主体（详情信息） -->
        <div class="card-body">
          <div class="info-block">
            <div class="info-label">
              <el-icon><Message /></el-icon>
              邮箱
            </div>
            <div class="info-value">{{ student.email || '未提供' }}</div>
          </div>

          <div class="info-block">
            <div class="info-label">
              <el-icon><Briefcase /></el-icon>
              专业
            </div>
            <div class="info-value">{{ student.major || '未提供' }}</div>
          </div>
        </div>

        <!-- 卡片底部（按钮） -->
        <template #footer>
          <div class="card-footer">
            <el-button type="primary" link size="small">查看完整信息</el-button>
          </div>
        </template>
      </el-card>
    </div>

    <!-- 分页 -->
    <div v-if="filteredStudents.length > pageSize" class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="filteredStudents.length"
        layout="prev, pager, next, total"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Search, Message, Briefcase } from '@element-plus/icons-vue'

const props = defineProps({
  students: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = 9

const filteredStudents = computed(() => {
  if (!searchQuery.value) return props.students
  
  const query = searchQuery.value.toLowerCase()
  return props.students.filter(student => 
    student.name.toLowerCase().includes(query) ||
    student.studentId.toLowerCase().includes(query)
  )
})

const paginatedStudents = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredStudents.value.slice(start, end)
})

function handlePageChange() {
  // 分页时滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<style scoped>
.students-list-container {
  padding: 0;
}

/* ==================== 头部区域 ==================== */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 30px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 250px;
  max-width: 400px;
}

.search-input :deep(.el-input__wrapper) {
  box-shadow: 0 2px 8px rgba(25, 137, 250, 0.1);
}

.header-section :deep(.el-statistic) {
  flex-shrink: 0;
}

/* ==================== 学生卡片网格 ==================== */
.students-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.student-card {
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.student-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 24px rgba(25, 137, 250, 0.15);
}

.student-card :deep(.el-card__header) {
  padding: 16px;
  border-bottom: 2px solid #f0f0f0;
  background: linear-gradient(135deg, #f0f8ff 0%, #e3f2fd 100%);
}

.student-card :deep(.el-card__body) {
  padding: 16px;
}

.student-card :deep(.el-card__footer) {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  background: #f9f9f9;
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.student-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1989fa 0%, #1565c0 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(25, 137, 250, 0.2);
}

.student-info {
  flex: 1;
  min-width: 0;
}

.student-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 3px;
}

.student-id {
  font-size: 11px;
  color: #999;
  letter-spacing: 0.5px;
}

/* 卡片主体 */
.card-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-block {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px;
  background: #f9f9f9;
  border-radius: 4px;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
  color: #1989fa;
  font-size: 12px;
  flex-shrink: 0;
  white-space: nowrap;
}

.info-label :deep(.el-icon) {
  font-size: 14px;
}

.info-value {
  font-size: 12px;
  color: #666;
  word-break: break-all;
  line-height: 1.4;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  justify-content: flex-end;
}

.card-footer :deep(.el-button) {
  font-weight: 500;
}

/* ==================== 分页 ==================== */
.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px 0;
  border-top: 1px solid #e4e7ed;
}

.pagination-container :deep(.el-pagination) {
  padding: 0;
}

.pagination-container :deep(.el-pagination .btn-prev),
.pagination-container :deep(.el-pagination .btn-next),
.pagination-container :deep(.el-pager li.active) {
  background-color: #1989fa;
  border-color: #1989fa;
}

.pagination-container :deep(.el-pagination .active.more::before),
.pagination-container :deep(.el-pagination .more::before) {
  background-color: #1989fa;
}

/* ==================== 响应式 ==================== */
@media (max-width: 1200px) {
  .students-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  }
}

@media (max-width: 768px) {
  .header-section {
    flex-direction: column;
    align-items: stretch;
    gap: 20px;
  }

  .search-input {
    max-width: none;
  }

  .students-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 15px;
  }

  .student-avatar {
    width: 44px;
    height: 44px;
    font-size: 16px;
  }

  .student-name {
    font-size: 14px;
  }

  .info-value {
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .students-grid {
    grid-template-columns: 1fr;
  }
}
</style>
