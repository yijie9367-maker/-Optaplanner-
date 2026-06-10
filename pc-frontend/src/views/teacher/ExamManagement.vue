<template>
  <div class="exam-management">
    <!-- 操作栏 -->
    <div class="action-bar">
      <span class="section-label">考试安排管理</span>
      <el-button type="primary" :icon="Plus" @click="openAddDialog">新增考试</el-button>
    </div>

    <!-- 考试列表 -->
    <el-table :data="examList" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="courseName" label="课程" min-width="140" />
      <el-table-column prop="className" label="班级" min-width="120" />
      <el-table-column prop="examDate" label="考试日期" width="120" />
      <el-table-column label="考试时间" width="140">
        <template #default="{ row }">{{ row.startTime }} ~ {{ row.endTime }}</template>
      </el-table-column>
      <el-table-column prop="location" label="考试地点" min-width="120" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openGradeDrawer(row)">录入成绩</el-button>
          <el-button size="small" type="primary" plain @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑考试' : '新增考试'"
      width="500px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="课程班级" prop="classKey">
          <el-select v-model="form.classKey" placeholder="请选择课程和班级" style="width: 100%" @change="onClassKeyChange">
            <el-option
              v-for="item in courseClassOptions"
              :key="item.key"
              :label="item.label"
              :value="item.key"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="考试日期" prop="examDate">
          <el-date-picker
            v-model="form.examDate"
            type="date"
            placeholder="选择考试日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-select v-model="form.startTime" placeholder="请选择开始时间" style="width: 100%" @change="onStartTimeChange">
            <el-option v-for="t in timeOptions" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-select v-model="form.endTime" placeholder="请选择结束时间" style="width: 100%">
            <el-option v-for="t in timeOptions" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试地点" prop="location">
          <el-select
            v-model="form.location"
            placeholder="请选择考试教室"
            style="width: 100%"
            filterable
            :loading="classroomsLoading"
          >
            <el-option
              v-for="room in classrooms"
              :key="room.id"
              :label="`${room.building}·${room.name}`"
              :value="`${room.building}·${room.name}`"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 成绩录入 Drawer -->
    <el-drawer
      v-model="gradeDrawerVisible"
      :title="`录入成绩 · ${currentExam?.courseName} · ${currentExam?.className}`"
      size="480px"
      direction="rtl"
    >
      <div v-loading="gradeLoading" class="grade-content">
        <div class="grade-tip">共 {{ gradeItems.length }} 名学生</div>
        <el-table :data="gradeItems" border size="small">
          <el-table-column prop="studentName" label="姓名" min-width="100" />
          <el-table-column label="成绩" width="160">
            <template #default="{ row }">
              <el-input-number
                v-model="row.score"
                :min="0"
                :max="100"
                :precision="1"
                size="small"
                placeholder="未录入"
                controls-position="right"
                style="width: 120px"
              />
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag
                v-if="row.score !== null && row.score !== undefined"
                :type="row.score >= 60 ? 'success' : 'danger'"
                size="small"
              >{{ row.score >= 60 ? '及格' : '不及格' }}</el-tag>
              <el-tag v-else type="info" size="small">未录入</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <div class="grade-actions">
          <el-button type="primary" :loading="gradeSaving" @click="saveGrades">保存成绩</el-button>
          <el-button @click="gradeDrawerVisible = false">关闭</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getTeacherExamList, addExam, updateExam, deleteExam } from '@/api/exam'
import { authFetch } from '@/utils/authFetch'
import { buildApiUrl } from '@/utils/api'

const props = defineProps({
  teacherId: {
    type: Number,
    required: true
  },
  teacherSchedules: {
    type: Array,
    default: () => []
  }
})

const BASE = buildApiUrl()

// 固定时间段选项（每30分钟，08:00-22:00）
const timeOptions = []
for (let h = 8; h <= 22; h++) {
  timeOptions.push(`${String(h).padStart(2, '0')}:00`)
  if (h < 22) timeOptions.push(`${String(h).padStart(2, '0')}:30`)
}

// 考试列表
const examList = ref([])
const loading = ref(false)

// 教室列表（从数据库获取）
const classrooms = ref([])
const classroomsLoading = ref(false)

async function fetchClassrooms() {
  classroomsLoading.value = true
  try {
    const res = await authFetch(`${BASE}/classroom/list`)
    const data = await res.json()
    classrooms.value = data.data || []
  } catch (e) {
    console.error('获取教室列表失败', e)
  } finally {
    classroomsLoading.value = false
  }
}

// 课程-班级选项（从课表推导）
const courseClassOptions = computed(() => {
  const seen = new Set()
  const opts = []
  for (const s of props.teacherSchedules) {
    const key = `${s.courseId || s.courseName}__${s.classGroupId || s.className}`
    if (!seen.has(key)) {
      seen.add(key)
      opts.push({
        key,
        label: `${s.courseName} · ${s.className}`,
        courseId: s.courseId,
        classGroupId: s.classGroupId,
        courseName: s.courseName,
        className: s.className
      })
    }
  }
  return opts
})

// 表单
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = ref({
  id: null,
  classKey: '',
  courseId: null,
  classGroupId: null,
  examDate: '',
  startTime: '',
  endTime: '',
  location: ''
})
const validateEndTime = (rule, value, callback) => {
  if (!value) return callback(new Error('请选择结束时间'))
  if (form.value.startTime && value <= form.value.startTime) {
    callback(new Error('结束时间必须晚于开始时间'))
  } else {
    callback()
  }
}

const rules = {
  classKey: [{ required: true, message: '请选择课程班级', trigger: 'change' }],
  examDate: [{ required: true, message: '请选择考试日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, validator: validateEndTime, trigger: 'change' }],
  location: [{ required: true, message: '请选择考试地点', trigger: 'change' }]
}

function onStartTimeChange() {
  // re-validate endTime whenever startTime changes
  if (form.value.endTime) {
    formRef.value?.validateField('endTime')
  }
}

// 成绩 Drawer
const gradeDrawerVisible = ref(false)
const gradeLoading = ref(false)
const gradeSaving = ref(false)
const currentExam = ref(null)
const gradeItems = ref([])

// -------------------- 数据加载 --------------------
async function fetchExams() {
  loading.value = true
  try {
    const response = await getTeacherExamList(props.teacherId)
    examList.value = response.data || []
  } catch (e) {
    ElMessage.error('获取考试列表失败')
  } finally {
    loading.value = false
  }
}

// -------------------- 新增/编辑 --------------------
function openAddDialog() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  // 找到匹配的 key
  const opt = courseClassOptions.value.find(
    o => o.courseId === row.courseId && o.classGroupId === row.classGroupId
  )
  form.value = {
    id: row.id,
    classKey: opt ? opt.key : '',
    courseId: row.courseId,
    classGroupId: row.classGroupId,
    examDate: row.examDate,
    startTime: row.startTime,
    endTime: row.endTime,
    location: row.location
  }
  dialogVisible.value = true
}

function onClassKeyChange(key) {
  const opt = courseClassOptions.value.find(o => o.key === key)
  if (opt) {
    form.value.courseId = opt.courseId
    form.value.classGroupId = opt.classGroupId
  }
}

function resetForm() {
  form.value = {
    id: null, classKey: '', courseId: null, classGroupId: null,
    examDate: '', startTime: '', endTime: '', location: ''
  }
  formRef.value?.resetFields()
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    const payload = {
      id: form.value.id,
      courseId: form.value.courseId,
      classGroupId: form.value.classGroupId,
      teacherId: props.teacherId,
      examDate: form.value.examDate,
      startTime: form.value.startTime,
      endTime: form.value.endTime,
      location: form.value.location
    }
    if (isEdit.value) {
      await updateExam(payload)
    } else {
      await addExam(payload)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false
    fetchExams()
  } catch (e) {
    ElMessage.error(e.message || '操作失败，请检查网络')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确认删除该考试安排？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await deleteExam(id)
    ElMessage.success('删除成功')
    fetchExams()
  } catch (e) {
    // 取消或失败
  }
}

// -------------------- 成绩录入 --------------------
async function openGradeDrawer(exam) {
  currentExam.value = exam
  gradeDrawerVisible.value = true
  gradeLoading.value = true
  gradeItems.value = []
  try {
    const res = await authFetch(
      `${BASE}/exam-grade/listByExamAndClass?examId=${exam.id}&classGroupId=${exam.classGroupId}`
    )
    const data = await res.json()
    gradeItems.value = (data.data || []).map(item => ({
      studentId: item.studentId,
      studentName: item.studentName,
      score: item.score !== null && item.score !== undefined ? Number(item.score) : null
    }))
  } catch (e) {
    ElMessage.error('加载学生成绩失败')
  } finally {
    gradeLoading.value = false
  }
}

async function saveGrades() {
  gradeSaving.value = true
  try {
    const grades = gradeItems.value.map(item => ({
      studentId: item.studentId,
      studentName: item.studentName,
      score: item.score
    }))
    const res = await authFetch(`${BASE}/exam-grade/saveAll`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ examId: currentExam.value.id, grades })
    })
    const data = await res.json()
    if (data.code === 200) {
      ElMessage.success('成绩保存成功')
    } else {
      ElMessage.error(data.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败，请检查网络')
  } finally {
    gradeSaving.value = false
  }
}

onMounted(() => {
  fetchClassrooms()
  if (props.teacherId) {
    fetchExams()
  }
})

watch(() => props.teacherId, (val) => {
  if (val) fetchExams()
})
</script>

<style scoped>
.exam-management {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-label {
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
}

.grade-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 8px 0;
}

.grade-tip {
  font-size: 13px;
  color: #909399;
}

.grade-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 8px;
}
</style>
