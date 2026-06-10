<template>
  <div class="colleagues-list">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索姓名、职称或院系..."
        clearable
        :prefix-icon="Search"
        style="max-width: 320px"
      />
      <span class="count-tip">共 {{ filteredTeachers.length }} 位同事</span>
    </div>

    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-else-if="filteredTeachers.length === 0" class="empty-wrap">
      <el-empty :description="searchQuery ? '没有匹配的同事' : '暂无同事数据'" />
    </div>

    <div v-else class="colleagues-grid">
      <div
        v-for="teacher in pagedTeachers"
        :key="teacher.id"
        class="colleague-card"
      >
        <div class="avatar-circle">{{ teacher.name?.charAt(0) ?? '师' }}</div>
        <div class="colleague-info">
          <div class="colleague-name">{{ teacher.name }}</div>
          <div class="colleague-title">
            <el-tag size="small" type="info">{{ teacher.title ?? '教师' }}</el-tag>
          </div>
          <div class="colleague-dept">
            <el-icon><OfficeBuilding /></el-icon>
            {{ teacher.department ?? '—' }}
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="filteredTeachers.length > pageSize" class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="filteredTeachers.length"
        layout="prev, pager, next"
        background
        small
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Search, OfficeBuilding } from '@element-plus/icons-vue'

const props = defineProps({
  teachers: {
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
const pageSize = 12

const filteredTeachers = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return props.teachers
  return props.teachers.filter(
    t =>
      t.name?.toLowerCase().includes(q) ||
      t.title?.toLowerCase().includes(q) ||
      t.department?.toLowerCase().includes(q)
  )
})

const pagedTeachers = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredTeachers.value.slice(start, start + pageSize)
})
</script>

<style scoped>
.colleagues-list {
  padding: 8px 4px;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 18px;
}

.count-tip {
  font-size: 13px;
  color: #909399;
}

.loading-wrap,
.empty-wrap {
  padding: 40px 0;
}

.colleagues-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.colleague-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  background: #fff;
  transition: box-shadow 0.2s, transform 0.2s;
}

.colleague-card:hover {
  box-shadow: 0 4px 16px rgba(25, 137, 250, 0.12);
  transform: translateY(-2px);
}

.avatar-circle {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: #ecf5ff;
  color: #1989fa;
  font-size: 20px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 2px solid #d9ecff;
}

.colleague-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.colleague-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
}

.colleague-dept {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.colleague-dept .el-icon {
  flex-shrink: 0;
  color: #1989fa;
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
