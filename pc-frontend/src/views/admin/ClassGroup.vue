<template>
  <div class="table-container">
    <el-card>
      <div class="card-header">
        <h3>班级管理</h3>
        <div class="header-actions">
          <el-button type="info" @click="handleDownloadTemplate">下载模板</el-button>
          <el-upload :show-file-list="false" accept=".xlsx,.xls" :before-upload="handleImport" :auto-upload="true">
            <el-button type="warning" :loading="importing">导入</el-button>
          </el-upload>
          <el-button type="success" @click="handleExport" :loading="exporting">导出</el-button>
          <el-button type="primary" @click="showAddDialog">新增班级</el-button>
          <el-button @click="fetchClassGroups">刷新</el-button>
        </div>
      </div>

      <div class="filter-section">
        <el-input
          v-model="searchText"
          placeholder="搜索班级名称或专业"
          style="width: 300px"
          clearable
          @input="handleSearch">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="gradeFilter"
          placeholder="按年级筛选"
          style="width: 200px; margin-left: 10px"
          clearable
          @change="handleFilter">
          <el-option
            v-for="grade in grades"
            :key="grade"
            :label="grade"
            :value="grade" />
        </el-select>
        <el-select
          v-model="majorFilter"
          placeholder="按专业筛选"
          style="width: 200px; margin-left: 10px"
          clearable
          @change="handleFilter">
          <el-option
            v-for="major in majorOptions"
            :key="major"
            :label="major"
            :value="major" />
        </el-select>
        <el-select v-model="pageSize" style="width: 110px; margin-left: auto" @change="currentPage = 1">
          <el-option v-for="s in [5, 10, 15, 20]" :key="s" :label="s + ' 条/页'" :value="s" />
        </el-select>
      </div>

      <el-table
        :data="paginatedData"
        style="width: 100%"
        table-layout="fixed"
        :cell-style="{ padding: '8px 12px' }"
        :header-cell-style="{ padding: '12px 12px', fontWeight: 'bold' }"
        stripe>
        <el-table-column type="index" label="序号" align="center" width="80" :index="indexMethod" />
        <el-table-column prop="name" label="班级名称" width="150" />
        <el-table-column prop="grade" label="年级" align="center" width="100" />
        <el-table-column prop="major" label="专业" width="200" />
        <el-table-column prop="studentCount" label="学生人数" align="center" width="120" />
        <el-table-column label="操作" align="center" width="280" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="primary" plain @click="openCourseDialog(scope.row)">分配课程</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :page-size="pageSize"
          :total="filteredClassGroups.length"
          v-model:current-page="currentPage">
        </el-pagination>
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false">
      <el-form
        ref="classGroupFormRef"
        :model="classGroupForm"
        :rules="formRules"
        label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="班级名称" prop="name">
              <el-input v-model="classGroupForm.name" placeholder="如：计科2301" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年级" prop="grade">
              <el-select v-model="classGroupForm.grade" placeholder="请选择年级" style="width: 100%">
                <el-option
                  v-for="grade in grades"
                  :key="grade"
                  :label="grade"
                  :value="grade" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="专业" prop="major">
              <el-select v-model="classGroupForm.major" placeholder="请选择专业" style="width: 100%" filterable clearable>
                <el-option
                  v-for="major in majorOptions"
                  :key="major"
                  :label="major"
                  :value="major" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学生人数" prop="studentCount">
              <el-input
                v-model="classGroupForm.studentCount"
                disabled
                placeholder="自动计算"
                style="width: 100%" />
              <div style="font-size: 12px; color: #999; margin-top: 4px;">
                根据该班级的学生数自动计算
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="courseDialogVisible"
      title="分配班级课程"
      width="860px"
      :close-on-click-modal="false">
      <div class="assignment-panel">
        <div class="assignment-toolbar">
          <div class="assignment-meta">
            <span>班级：{{ activeClassGroup?.name || '-' }}</span>
            <span>专业：{{ activeClassGroup?.major || '-' }}</span>
          </div>
          <el-select
            v-model="assignmentSemester"
            style="width: 180px"
            @change="loadAssignedCourses">
            <el-option
              v-for="semester in semesterOptions"
              :key="semester"
              :label="semester"
              :value="semester" />
          </el-select>
        </div>
        <div class="assignment-tip">左侧勾选课程后会立即加入右侧，右侧勾选课程后会立即移除。</div>
        <el-transfer
          v-model="selectedCourseIds"
          filterable
          filter-placeholder="搜索课程"
          :titles="['可选课程', '已分配课程']"
          :data="courseTransferData"
          @left-check-change="handleLeftCheckChange"
          @right-check-change="handleRightCheckChange" />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="courseDialogVisible = false">取消</el-button>
          <el-button :loading="assignmentLoading" @click="loadAssignedCourses">重新加载</el-button>
          <el-button type="primary" :loading="assignmentSaving" @click="handleSaveAssignedCourses">保存分配</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getClassGroupList, addClassGroup, updateClassGroup, deleteClassGroup, importClassGroups, exportClassGroups, downloadClassGroupTemplate, getAssignedCourseIds, assignClassGroupCourses } from '@/api/classgroup'
import { getCourseList } from '@/api/course'
import { downloadBlob } from '@/utils/download'

const classGroups = ref([])
const currentPage = ref(1)
const pageSize = ref(15)
const searchText = ref('')
const gradeFilter = ref('')
const majorFilter = ref('')

const dialogVisible = ref(false)
const dialogTitle = ref('新增班级')
const submitLoading = ref(false)
const isEdit = ref(false)

const courseDialogVisible = ref(false)
const assignmentLoading = ref(false)
const assignmentSaving = ref(false)
const courseOptions = ref([])
const selectedCourseIds = ref([])
const activeClassGroup = ref(null)
const assignmentSemester = ref(getCurrentSemester())

const grades = [
  '2020', '2021', '2022', '2023', '2024', '2025'
]

const classGroupForm = reactive({
  id: null,
  name: '',
  grade: '',
  major: '',
  studentCount: 42
})

const formRules = {
  name: [
    { required: true, message: '请输入班级名称', trigger: 'blur' },
    { min: 2, max: 20, message: '班级名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  grade: [
    { required: true, message: '请选择年级', trigger: 'change' }
  ],
  major: [
    { required: true, message: '请选择专业', trigger: 'change' }
  ]
}

const classGroupFormRef = ref()
const semesterOptions = buildSemesterOptions()

const majorOptions = computed(() => {
  const majorSet = new Set(
    classGroups.value
      .map(classGroup => classGroup.major?.trim())
      .filter(Boolean)
  )

  if (classGroupForm.major?.trim()) {
    majorSet.add(classGroupForm.major.trim())
  }

  return Array.from(majorSet).sort((left, right) => left.localeCompare(right, 'zh-CN'))
})

const courseTransferData = computed(() => courseOptions.value.map(course => ({
  key: course.id,
  label: `${course.name}${course.code ? ` (${course.code})` : ''}`
})))

const filteredClassGroups = computed(() => {
  let filtered = classGroups.value

  if (searchText.value) {
    filtered = filtered.filter(classGroup =>
      classGroup.name.toLowerCase().includes(searchText.value.toLowerCase()) ||
      classGroup.major.toLowerCase().includes(searchText.value.toLowerCase())
    )
  }

  if (gradeFilter.value) {
    filtered = filtered.filter(classGroup => classGroup.grade === gradeFilter.value)
  }

  if (majorFilter.value) {
    filtered = filtered.filter(classGroup => classGroup.major === majorFilter.value)
  }

  return filtered
})

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredClassGroups.value.slice(start, end)
})

const importing = ref(false)
const exporting = ref(false)

const fetchClassGroups = async () => {
  try {
    const response = await getClassGroupList()
    classGroups.value = response.data || []
  } catch (error) {
    console.error('获取班级列表失败', error)
    ElMessage.error('获取班级列表失败')
  }
}

async function ensureCourseOptionsLoaded() {
  if (courseOptions.value.length > 0) {
    return
  }
  const response = await getCourseList()
  courseOptions.value = response.data || []
}

const showAddDialog = () => {
  dialogTitle.value = '新增班级'
  isEdit.value = false
  Object.assign(classGroupForm, {
    id: null,
    name: '',
    grade: '',
    major: '',
    studentCount: 42
  })
  dialogVisible.value = true
  classGroupFormRef.value?.clearValidate()
}

const showEditDialog = (classGroup) => {
  dialogTitle.value = '编辑班级'
  isEdit.value = true
  Object.assign(classGroupForm, { ...classGroup })
  dialogVisible.value = true
  classGroupFormRef.value?.clearValidate()
}

const openCourseDialog = async (classGroup) => {
  activeClassGroup.value = classGroup
  assignmentSemester.value = getCurrentSemester()
  courseDialogVisible.value = true
  try {
    assignmentLoading.value = true
    await ensureCourseOptionsLoaded()
    await loadAssignedCourses()
  } catch (error) {
    console.error('加载班级课程分配失败', error)
    ElMessage.error('加载班级课程分配失败')
  } finally {
    assignmentLoading.value = false
  }
}

const loadAssignedCourses = async () => {
  if (!activeClassGroup.value) {
    return
  }
  assignmentLoading.value = true
  try {
    const response = await getAssignedCourseIds(activeClassGroup.value.id, assignmentSemester.value)
    selectedCourseIds.value = response.data || []
  } catch (error) {
    console.error('获取已分配课程失败', error)
    ElMessage.error('获取已分配课程失败')
  } finally {
    assignmentLoading.value = false
  }
}

const handleSaveAssignedCourses = async () => {
  if (!activeClassGroup.value) {
    return
  }
  assignmentSaving.value = true
  try {
    await assignClassGroupCourses(activeClassGroup.value.id, {
      semester: assignmentSemester.value,
      courseIds: selectedCourseIds.value
    })
    ElMessage.success('班级课程分配已保存')
    courseDialogVisible.value = false
  } catch (error) {
    console.error('保存班级课程分配失败', error)
    ElMessage.error('保存班级课程分配失败')
  } finally {
    assignmentSaving.value = false
  }
}

const handleLeftCheckChange = (checkedKeys) => {
  if (!checkedKeys.length) {
    return
  }
  const merged = new Set(selectedCourseIds.value)
  checkedKeys.forEach(key => merged.add(key))
  selectedCourseIds.value = Array.from(merged)
}

const handleRightCheckChange = (checkedKeys) => {
  if (!checkedKeys.length) {
    return
  }
  const checkedKeySet = new Set(checkedKeys)
  selectedCourseIds.value = selectedCourseIds.value.filter(key => !checkedKeySet.has(key))
}

const handleSubmit = async () => {
  try {
    await classGroupFormRef.value.validate()
    submitLoading.value = true

    if (isEdit.value) {
      await updateClassGroup(classGroupForm)
      ElMessage.success('更新班级成功')
    } else {
      await addClassGroup(classGroupForm)
      ElMessage.success('新增班级成功')
    }

    dialogVisible.value = false
    await fetchClassGroups()
  } catch (error) {
    console.error('操作失败', error)
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDownloadTemplate = async () => {
  try {
    await ElMessageBox.confirm('确定要下载班级导入模板吗？下载后按照模板格式填写数据后再导入。', '下载模板', {
      confirmButtonText: '确定下载',
      cancelButtonText: '取消',
      type: 'info'
    })
  } catch {
    return
  }
  try {
    const blob = await downloadClassGroupTemplate()
    downloadBlob(blob, '班级导入模板.xlsx')
  } catch {
    ElMessage.error('下载模板失败')
  }
}

const handleImport = async (file) => {
  importing.value = true
  try {
    const res = await importClassGroups(file)
    const r = res.data
    if (r.failCount === 0) {
      ElMessage.success(`导入成功 ${r.successCount} 条`)
    } else {
      ElMessage.warning(`成功 ${r.successCount} 条，失败 ${r.failCount} 条：${r.errors.slice(0, 3).join('; ')}`)
    }
    await fetchClassGroups()
  } catch {
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
  return false
}

const handleExport = async () => {
  try {
    await ElMessageBox.confirm('确定要导出当前班级列表吗？', '导出确认', {
      confirmButtonText: '确定导出',
      cancelButtonText: '取消',
      type: 'info'
    })
  } catch {
    return
  }
  exporting.value = true
  try {
    const blob = await exportClassGroups()
    downloadBlob(blob, '班级列表.xlsx')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

const handleDelete = async (classGroup) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除班级 "${classGroup.name}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteClassGroup(classGroup.id)
    ElMessage.success('删除班级成功')
    await fetchClassGroups()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除班级失败')
    }
  }
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleFilter = () => {
  currentPage.value = 1
}

const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

function getCurrentSemester() {
  const now = new Date()
  const year = now.getFullYear()
  return now.getMonth() <= 6 ? `${year}-spring` : `${year}-autumn`
}

function buildSemesterOptions() {
  const currentYear = new Date().getFullYear()
  return [
    `${currentYear - 1}-autumn`,
    `${currentYear}-spring`,
    `${currentYear}-autumn`,
    `${currentYear + 1}-spring`
  ]
}

onMounted(() => {
  fetchClassGroups()
})
</script>

<style src="@/styles/crudPage.css"></style>
<style scoped>
.assignment-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.assignment-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.assignment-meta {
  display: flex;
  gap: 16px;
  color: #606266;
  font-size: 14px;
  flex-wrap: wrap;
}

.assignment-tip {
  color: #909399;
  font-size: 13px;
}
</style>
