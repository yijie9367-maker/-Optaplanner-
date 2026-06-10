<template>
  <div class="teaching-classes">
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-value">{{ uniqueCourses }}</div>
        <div class="stat-label">执教课程</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ uniqueClasses }}</div>
        <div class="stat-label">执教班级</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ totalLessons }}</div>
        <div class="stat-label">本周课时</div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索课程名或班级名..."
        clearable
        :prefix-icon="Search"
        style="max-width: 320px"
      />
    </div>

    <!-- 课程-班级卡片列表 -->
    <div v-if="filteredList.length === 0" class="empty-wrap">
      <el-empty :description="searchQuery ? '没有匹配的课程' : '暂无课程安排'" />
    </div>

    <div v-else class="class-cards-grid">
      <div
        v-for="item in filteredList"
        :key="`${item.courseName}-${item.className}`"
        class="class-card"
      >
        <div class="card-top">
          <div class="course-badge">{{ item.courseType }}</div>
          <div class="course-title">{{ item.courseName }}</div>
        </div>
        <div class="card-body">
          <div class="info-row">
            <el-icon><UserFilled /></el-icon>
            <span>班级：{{ item.className }}</span>
          </div>
          <div class="info-row">
            <el-icon><Location /></el-icon>
            <span>教室：{{ item.classroomName }}</span>
          </div>
          <div class="info-row">
            <el-icon><Calendar /></el-icon>
            <span>{{ formatScheduleTime(item) }}</span>
          </div>
          <div class="info-row">
            <el-icon><Avatar /></el-icon>
            <span>学生人数：{{ item.studentNum ?? '—' }} 人</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Search, UserFilled, Location, Calendar, Avatar } from '@element-plus/icons-vue'

const props = defineProps({
  schedules: {
    type: Array,
    default: () => []
  }
})

const searchQuery = ref('')

// 从 ScheduleVO 数组推导唯一课程-班级列表（按课程名+班级名去重）
const classCourseList = computed(() => {
  const map = new Map()
  props.schedules.forEach(s => {
    const key = `${s.courseName}__${s.className}`
    if (!map.has(key)) {
      map.set(key, {
        courseName: s.courseName || '未知课程',
        className: s.className || '未知班级',
        classroomName: s.classroomName || s.roomName || '—',
        studentNum: s.studentNum,
        courseType: '必修',
        weekDay: s.weekDay,
        timeSlot: s.timeSlot
      })
    }
  })
  return Array.from(map.values())
})

const filteredList = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return classCourseList.value
  return classCourseList.value.filter(
    item =>
      item.courseName.toLowerCase().includes(q) ||
      item.className.toLowerCase().includes(q)
  )
})

const uniqueCourses = computed(() => new Set(classCourseList.value.map(i => i.courseName)).size)
const uniqueClasses = computed(() => new Set(classCourseList.value.map(i => i.className)).size)
const totalLessons = computed(() => props.schedules.length)

const dayNames = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
const timeSlotNames = {
  1: '08:00-09:40',
  3: '10:00-11:40',
  5: '14:00-15:40',
  7: '16:00-17:40',
  9: '19:00-20:40'
}

function formatScheduleTime(item) {
  const day = dayNames[item.weekDay] || `第${item.weekDay}天`
  const slot = timeSlotNames[item.timeSlot] || `第${item.timeSlot}节`
  return `${day} ${slot}`
}
</script>

<style scoped>
.teaching-classes {
  padding: 8px 4px;
}

.stat-cards {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
}

.stat-card {
  flex: 1;
  background: #f4f9ff;
  border: 1px solid #d9ecff;
  border-radius: 10px;
  padding: 16px;
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1989fa;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.search-bar {
  margin-bottom: 18px;
}

.empty-wrap {
  padding: 40px 0;
}

.class-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.class-card {
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
  transition: box-shadow 0.2s, transform 0.2s;
}

.class-card:hover {
  box-shadow: 0 4px 16px rgba(25, 137, 250, 0.15);
  transform: translateY(-2px);
}

.card-top {
  background: #ecf5ff;
  color: #1989fa;
  padding: 14px 16px 12px;
  border-bottom: 1px solid #d9ecff;
}

.course-badge {
  display: inline-block;
  font-size: 11px;
  background: #d9ecff;
  color: #1989fa;
  border-radius: 20px;
  padding: 2px 10px;
  margin-bottom: 6px;
}

.course-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
}

.card-body {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #4e5d6c;
}

.info-row .el-icon {
  color: #1989fa;
  flex-shrink: 0;
}
</style>
