<template>
  <div class="table-container">
    <el-card>
      <!-- 卡片头部 -->
      <div class="card-header">
        <h3>教师课程关联管理</h3>
        <el-button type="primary" @click="fetchTeacherCourses">刷新</el-button>
      </div>

      <!-- 教师课程表格 -->
      <el-table 
        :data="teacherCourses" 
        style="width: 100%"
        table-layout="fixed"
        :cell-style="{ padding: '8px 12px' }"
        :header-cell-style="{ padding: '12px 12px', fontWeight: 'bold' }">
        <el-table-column prop="teacherId" label="教师ID" width="80" align="center" />
        <el-table-column prop="teacherName" label="教师姓名" />
        <el-table-column prop="teacherTitle" label="职称" />
        <el-table-column prop="teacherDepartment" label="院系" />
        <el-table-column label="教授课程" width="300">
          <template #default="scope">
            <div class="courses-list">
              <el-tag 
                v-for="course in scope.row.courses" 
                :key="course.id"
                type="info"
                size="small"
                class="course-tag"
              >
                {{ course.code }} - {{ course.name }} ({{ course.credit }}学分)
              </el-tag>
              <div v-if="!scope.row.courses || scope.row.courses.length === 0" class="no-courses">
                暂无课程
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalCourses" label="课程数量" width="100" align="center" />
        <el-table-column prop="totalCredits" label="总学分" width="100" align="center" />
        <el-table-column prop="totalHours" label="总课时" width="100" align="center" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getTeacherList } from '@/api/teacher'
import { getCourseList } from '@/api/course'

const teachers = ref([])
const courses = ref([])

// 获取教师和课程数据
const fetchTeacherCourses = async () => {
  try {
    const [teachersRes, coursesRes] = await Promise.all([
      getTeacherList(),
      getCourseList()
    ])
    
    teachers.value = teachersRes
    courses.value = coursesRes
    
    console.log('教师数据:', teachers.value)
    console.log('课程数据:', courses.value)
  } catch (error) {
    console.error('获取数据失败', error)
    // 使用模拟数据
    teachers.value = [
      { id: 1, name: '张老师', title: '教授', department: '计算机学院' },
      { id: 2, name: '李老师', title: '副教授', department: '软件学院' },
      { id: 3, name: '王老师', title: '讲师', department: '通信学院' }
    ]
    
    courses.value = [
      { id: 1, code: 'CS101', name: '数据结构', teacherId: 1, credit: 3, maxHours: 48 },
      { id: 2, code: 'CS102', name: '算法设计', teacherId: 1, credit: 3, maxHours: 48 },
      { id: 3, code: 'SE101', name: '软件工程', teacherId: 2, credit: 4, maxHours: 64 },
      { id: 4, code: 'SE102', name: '数据库系统', teacherId: 2, credit: 3, maxHours: 48 },
      { id: 5, code: 'CE101', name: '通信原理', teacherId: 3, credit: 3, maxHours: 48 }
    ]
  }
}

// 计算教师课程关联数据
const teacherCourses = computed(() => {
  return teachers.value.map(teacher => {
    const teacherCourses = courses.value.filter(course => course.teacherId === teacher.id)
    
    return {
      teacherId: teacher.id,
      teacherName: teacher.name,
      teacherTitle: teacher.title,
      teacherDepartment: teacher.department,
      courses: teacherCourses,
      totalCourses: teacherCourses.length,
      totalCredits: teacherCourses.reduce((sum, course) => sum + (course.credit || 0), 0),
      totalHours: teacherCourses.reduce((sum, course) => sum + (course.maxHours || 0), 0)
    }
  })
})

// 页面加载时获取数据
onMounted(() => {
  fetchTeacherCourses()
})
</script>

<style scoped>
.table-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}
.card-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

/* 课程标签样式 */
.courses-list {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}
.course-tag {
  margin: 2px;
}
.no-courses {
  color: #999;
  font-style: italic;
}

/* 表格优化样式 */
:deep(.el-table) {
  font-size: 14px;
}
:deep(.el-table th) {
  background-color: #f5f7fa;
  color: #303133;
}
:deep(.el-table tr:hover) {
  background-color: #f5f7fa;
}
:deep(.el-table .cell) {
  line-height: 1.5;
}

/* 确保内容在单元格内正确显示 */
:deep(.el-table .cell) {
  white-space: normal;
  word-break: break-word;
}
</style>