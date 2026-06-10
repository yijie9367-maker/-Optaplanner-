<template>
  <div class="table-container">
    <el-card>
      <div class="card-header">
        <h3>期末考试管理</h3>
        <div class="header-actions">
          <el-button type="primary" @click="openAdd">新增考试</el-button>
          <el-dropdown>
            <el-button type="info">考试导入导出<el-icon class="el-icon--right"><arrow-down /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleDownloadExamTemplate">下载考试模板</el-dropdown-item>
                <el-dropdown-item>
                  <el-upload :show-file-list="false" accept=".xlsx,.xls" :before-upload="handleImportExam" :auto-upload="true" style="display:inline">
                    <span>导入考试</span>
                  </el-upload>
                </el-dropdown-item>
                <el-dropdown-item @click="handleExportExam">导出考试</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown>
            <el-button type="warning">成绩导入导出<el-icon class="el-icon--right"><arrow-down /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleDownloadGradeTemplate">下载成绩模板</el-dropdown-item>
                <el-dropdown-item>
                  <el-upload :show-file-list="false" accept=".xlsx,.xls" :before-upload="handleImportGrade" :auto-upload="true" style="display:inline">
                    <span>导入成绩</span>
                  </el-upload>
                </el-dropdown-item>
                <el-dropdown-item @click="handleExportGrade">导出成绩</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button @click="fetchExams">刷新</el-button>
        </div>
      </div>

      <!-- 搜索筛选 -->
      <div class="filter-section">
        <el-input
          v-model="searchText"
          placeholder="搜索课程、班级或地点"
          style="width: 280px"
          clearable
          @input="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select
          v-model="classFilter"
          placeholder="按班级筛选"
          style="width: 180px; margin-left: 10px"
          clearable
          @change="handleFilter">
          <el-option
            v-for="cls in classes"
            :key="cls.id"
            :label="cls.name"
            :value="cls.id" />
        </el-select>
        <el-select v-model="pageSize" style="width: 110px; margin-left: auto" @change="currentPage = 1">
          <el-option v-for="s in [5, 10, 15, 20]" :key="s" :label="s + ' 条/页'" :value="s" />
        </el-select>
      </div>

      <!-- 考试列表 -->
      <el-table
        :data="paginatedData"
        v-loading="loading"
        stripe
        style="width: 100%"
        :cell-style="{ padding: '8px 12px' }"
        :header-cell-style="{ padding: '12px 12px', fontWeight: 'bold' }">
        <el-table-column prop="courseName" label="课程" min-width="160" />
        <el-table-column prop="className" label="班级" width="130" />
        <el-table-column prop="teacherName" label="主考教师" width="110" />
        <el-table-column prop="examDate" label="考试日期" width="120" />
        <el-table-column label="考试时间" width="140">
          <template #default="{ row }">{{ row.startTime }} ~ {{ row.endTime }}</template>
        </el-table-column>
        <el-table-column prop="location" label="考试地点" min-width="130" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :page-size="pageSize"
          :total="filteredList.length"
          v-model:current-page="currentPage" />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑考试安排' : '新增考试安排'"
      width="480px"
      :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="班级" prop="classGroupId">
          <el-select
            v-model="form.classGroupId"
            placeholder="请选择班级"
            style="width: 100%"
            filterable
            @change="handleClassGroupChange">
            <el-option
              v-for="classItem in classes"
              :key="classItem.id"
              :label="classItem.name"
              :value="classItem.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程" prop="courseId">
          <el-select
            v-model="form.courseId"
            placeholder="请选择课程"
            style="width: 100%"
            filterable
            :loading="coursesLoading"
            @change="handleCourseChange">
            <el-option
              v-for="course in availableCourses"
              :key="course.id"
              :label="`${course.name}${course.code ? ` (${course.code})` : ''}`"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="主考教师">
          <el-input :value="selectedTeacherName" disabled />
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
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, ArrowDown } from '@element-plus/icons-vue'
import { getExamList, addExam, updateExam, deleteExam, importExams, exportExams, downloadExamTemplate, importGrades, exportGrades, downloadGradeTemplate } from '@/api/exam'
import { downloadBlob } from '@/utils/download'
import { authFetch } from '@/utils/authFetch'
import { buildApiUrl } from '@/utils/api'

const importing = ref(false)

const handleDownloadExamTemplate = async () => {
  try {
    await ElMessageBox.confirm('确定要下载考试导入模板吗？下载后按照模板格式填写数据后再导入。', '下载模板', {
      confirmButtonText: '确定下载', cancelButtonText: '取消', type: 'info'
    })
  } catch { return }
  try { downloadBlob(await downloadExamTemplate(), '考试导入模板.xlsx') } catch { ElMessage.error('下载失败') }
}
const handleImportExam = async (file) => {
  importing.value = true
  try {
    const res = await importExams(file)
    const r = res.data
    r.failCount === 0 ? ElMessage.success(`导入成功 ${r.successCount} 条`) : ElMessage.warning(`成功 ${r.successCount} 条，失败 ${r.failCount} 条`)
    fetchExams()
  } catch { ElMessage.error('导入失败') } finally { importing.value = false }
  return false
}
const handleExportExam = async () => {
  try {
    await ElMessageBox.confirm('确定要导出考试列表吗？', '导出确认', {
      confirmButtonText: '确定导出', cancelButtonText: '取消', type: 'info'
    })
  } catch { return }
  try { downloadBlob(await exportExams(), '考试列表.xlsx') } catch { ElMessage.error('导出失败') }
}
const handleDownloadGradeTemplate = async () => {
  try {
    await ElMessageBox.confirm('确定要下载成绩导入模板吗？下载后按照模板格式填写数据后再导入。', '下载模板', {
      confirmButtonText: '确定下载', cancelButtonText: '取消', type: 'info'
    })
  } catch { return }
  try { downloadBlob(await downloadGradeTemplate(), '成绩导入模板.xlsx') } catch { ElMessage.error('下载失败') }
}
const handleImportGrade = async (file) => {
  importing.value = true
  try {
    const res = await importGrades(file)
    const r = res.data
    r.failCount === 0 ? ElMessage.success(`导入成功 ${r.successCount} 条`) : ElMessage.warning(`成功 ${r.successCount} 条，失败 ${r.failCount} 条`)
    fetchExams()
  } catch { ElMessage.error('导入失败') } finally { importing.value = false }
  return false
}
const handleExportGrade = async () => {
  try {
    await ElMessageBox.confirm('确定要导出成绩列表吗？', '导出确认', {
      confirmButtonText: '确定导出', cancelButtonText: '取消', type: 'info'
    })
  } catch { return }
  try { downloadBlob(await exportGrades(), '成绩列表.xlsx') } catch { ElMessage.error('导出失败') }
}

const BASE = buildApiUrl()

// 固定时间段选项
const timeOptions = []
for (let h = 8; h <= 22; h++) {
  timeOptions.push(`${String(h).padStart(2, '0')}:00`)
  if (h < 22) timeOptions.push(`${String(h).padStart(2, '0')}:30`)
}

const loading = ref(false)
const examList = ref([])
const classrooms = ref([])
const classroomsLoading = ref(false)
const classes = ref([])
const availableCourses = ref([])
const coursesLoading = ref(false)
const searchText = ref('')
const classFilter = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = ref({ id: null, courseId: null, classGroupId: null, teacherId: null, examDate: '', startTime: '', endTime: '', location: '' })
const validateEndTime = (rule, value, callback) => {
  if (!value) return callback(new Error('请选择结束时间'))
  if (form.value.startTime && value <= form.value.startTime) {
    callback(new Error('结束时间必须晚于开始时间'))
  } else {
    callback()
  }
}

const rules = {
  classGroupId: [{ required: true, message: '请选择班级', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  examDate: [{ required: true, message: '请选择考试日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, validator: validateEndTime, trigger: 'change' }],
  location: [{ required: true, message: '请选择考试地点', trigger: 'change' }]
}

const selectedCourse = computed(() => availableCourses.value.find(course => course.id === form.value.courseId) || null)
const selectedTeacherName = computed(() => selectedCourse.value?.teacherName || '将根据课程自动带出')

function onStartTimeChange() {
  if (form.value.endTime) {
    formRef.value?.validateField('endTime')
  }
}

const filteredList = computed(() => {
  return examList.value.filter(e => {
    const matchText = !searchText.value ||
      (e.courseName || '').includes(searchText.value) ||
      (e.className || '').includes(searchText.value) ||
      (e.location || '').includes(searchText.value)
    const matchClass = !classFilter.value || e.classGroupId === classFilter.value
    return matchText && matchClass
  })
})

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

function handleSearch() { currentPage.value = 1 }
function handleFilter() { currentPage.value = 1 }

async function fetchExams() {
  loading.value = true
  try {
    const response = await getExamList()
    examList.value = response.data || []
  } catch (e) {
    ElMessage.error('获取考试列表失败')
  } finally {
    loading.value = false
  }
}

async function fetchClasses() {
  try {
    const res = await authFetch(`${BASE}/classgroup/list`)
    const data = await res.json()
    classes.value = data.data || []
  } catch {}
}

async function fetchClassrooms() {
  classroomsLoading.value = true
  try {
    const res = await authFetch(`${BASE}/classroom/list`)
    const data = await res.json()
    classrooms.value = data.data || []
  } catch {}
  finally { classroomsLoading.value = false }
}

async function fetchCoursesByClassGroupId(classGroupId) {
  if (!classGroupId) {
    availableCourses.value = []
    return
  }

  coursesLoading.value = true
  try {
    const res = await authFetch(`${BASE}/course/listByClassGroupId?classGroupId=${classGroupId}`)
    const data = await res.json()
    availableCourses.value = data.data || []
  } catch {
    availableCourses.value = []
    ElMessage.error('获取班级课程失败')
  } finally {
    coursesLoading.value = false
  }
}

function resetForm() {
  form.value = {
    id: null,
    courseId: null,
    classGroupId: null,
    teacherId: null,
    examDate: '',
    startTime: '',
    endTime: '',
    location: ''
  }
  availableCourses.value = []
  formRef.value?.resetFields()
}

function openAdd() {
  resetForm()
  dialogVisible.value = true
}

async function handleClassGroupChange(classGroupId) {
  form.value.courseId = null
  form.value.teacherId = null
  await fetchCoursesByClassGroupId(classGroupId)
}

function handleCourseChange(courseId) {
  const course = availableCourses.value.find(item => item.id === courseId)
  form.value.teacherId = course?.teacherId || null
}

async function openEdit(row) {
  form.value = {
    id: row.id,
    courseId: row.courseId,
    classGroupId: row.classGroupId,
    teacherId: row.teacherId,
    examDate: row.examDate,
    startTime: row.startTime,
    endTime: row.endTime,
    location: row.location
  }
  await fetchCoursesByClassGroupId(row.classGroupId)
  handleCourseChange(row.courseId)
  dialogVisible.value = true
}

async function handleSubmit() {
  try { await formRef.value.validate() } catch { return }
  submitting.value = true
  try {
    const payload = {
      ...form.value,
      teacherId: selectedCourse.value?.teacherId || form.value.teacherId
    }
    if (form.value.id) {
      await updateExam(payload)
    } else {
      await addExam(payload)
    }
    ElMessage.success(form.value.id ? '保存成功' : '新增成功')
    dialogVisible.value = false
    resetForm()
    fetchExams()
  } catch {
    ElMessage.error('保存失败，请检查网络')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确认删除该考试安排及其所有成绩记录？', '提示', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await deleteExam(id)
    ElMessage.success('删除成功')
    fetchExams()
  } catch {}
}

onMounted(() => {
  fetchExams()
  fetchClasses()
  fetchClassrooms()
})
</script>

<style scoped>
.table-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  color: #1f2d3d;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-section {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 8px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
