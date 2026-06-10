<template>
  <div class="table-container">
    <el-card>
      <div class="card-header">
        <h3>学生管理</h3>
        <div class="header-actions">
          <el-button type="info" @click="handleDownloadTemplate">下载模板</el-button>
          <el-upload :show-file-list="false" accept=".xlsx,.xls" :before-upload="handleImport" :auto-upload="true">
            <el-button type="warning" :loading="importing">导入</el-button>
          </el-upload>
          <el-button type="success" @click="handleExport" :loading="exporting">导出</el-button>
          <el-button type="primary" @click="showAddDialog">新增学生</el-button>
          <el-button @click="loadPageData">刷新</el-button>
        </div>
      </div>

      <!-- 搜索和筛选 -->
      <div class="filter-section">
        <el-input
          v-model="searchText"
          placeholder="搜索学生姓名或专业"
          style="width: 300px"
          clearable
          @input="handleSearch">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
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
        <el-table-column label="序号" align="center" width="80">
          <template #default="scope">
            {{ (currentPage - 1) * pageSize + scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="age" label="年龄" align="center" width="80" />
        <el-table-column prop="major" label="专业" width="200" />
        <el-table-column label="班级" width="180">
          <template #default="scope">
            {{ getClassGroupLabel(scope.row.classGroupId) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.isMuted ? 'danger' : 'success'">
              {{ scope.row.isMuted ? '已禁言' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="280" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button size="small" :type="scope.row.isMuted ? 'success' : 'warning'" @click="toggleMuteStatus(scope.row)">
              {{ scope.row.isMuted ? '解禁' : '禁言' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :page-size="pageSize"
          :total="filteredStudents.length"
          v-model:current-page="currentPage">
        </el-pagination>
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false">
      <el-form
        ref="studentFormRef"
        :model="studentForm"
        :rules="formRules"
        label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="studentForm.name" placeholder="请输入学生姓名" />
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number
            v-model="studentForm.age"
            :min="16"
            :max="30"
            controls-position="right" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-select
            v-model="studentForm.major"
            placeholder="请选择专业"
            style="width: 100%"
            filterable
            clearable
            :loading="classGroupsLoading">
            <el-option
              v-for="major in majorOptions"
              :key="major"
              :label="major"
              :value="major" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" prop="classGroupId">
          <el-select
            v-model="studentForm.classGroupId"
            placeholder="请选择班级"
            style="width: 100%"
            filterable
            clearable
            :loading="classGroupsLoading"
            @change="handleClassGroupChange">
            <el-option
              v-for="classGroup in classGroupOptions"
              :key="classGroup.id"
              :label="classGroup.name"
              :value="classGroup.id" />
          </el-select>
        </el-form-item>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onActivated, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getStudentList, addStudent, updateStudent, deleteStudent, importStudents, exportStudents, downloadStudentTemplate, updateStudentMuteStatus } from '@/api/student'
import { getClassGroupList } from '@/api/classgroup'
import { downloadBlob } from '@/utils/download'

const students = ref([])           // 全部学生数据
const classGroups = ref([])        // 全部班级数据
const currentPage = ref(1)         // 当前页
const pageSize = ref(15)           // 每页显示数量
const searchText = ref('')         // 搜索文本

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增学生')
const submitLoading = ref(false)
const isEdit = ref(false)

// 缓存标志 - 确保只第一次加载数据
const dataLoaded = ref(false)
const classGroupsLoading = ref(false)

const importing = ref(false)
const exporting = ref(false)

// 表单数据
const studentForm = reactive({
  id: null,
  name: '',
  age: 20,
  major: '',
  classGroupId: null
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入学生姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' }
  ],
  major: [
    { required: true, message: '请选择专业', trigger: 'change' }
  ],
  classGroupId: [
    { required: true, message: '请选择班级', trigger: 'change' }
  ]
}

const studentFormRef = ref()

const classGroupOptions = computed(() => {
  const options = [...classGroups.value]
  if (
    studentForm.classGroupId != null
    && !options.some(classGroup => classGroup.id === studentForm.classGroupId)
  ) {
    options.unshift({
      id: studentForm.classGroupId,
      name: `无效班级(ID: ${studentForm.classGroupId})`
    })
  }
  return options
})

const majorOptions = computed(() => {
  const majorSet = new Set(
    classGroups.value
      .map(classGroup => classGroup.major?.trim())
      .filter(Boolean)
  )

  if (studentForm.major?.trim()) {
    majorSet.add(studentForm.major.trim())
  }

  return Array.from(majorSet).sort((left, right) => left.localeCompare(right, 'zh-CN'))
})

// 过滤后的学生数据
const filteredStudents = computed(() => {
  if (!searchText.value) {
    return students.value
  }
  return students.value.filter(student =>
    student.name.toLowerCase().includes(searchText.value.toLowerCase()) ||
    student.major.toLowerCase().includes(searchText.value.toLowerCase())
  )
})

// 计算当前页的数据
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredStudents.value.slice(start, end)
})

const getClassGroupLabel = (classGroupId) => {
  if (classGroupId == null) {
    return '未分配班级'
  }
  const classGroup = classGroups.value.find(item => item.id === classGroupId)
  return classGroup ? classGroup.name : `班级ID: ${classGroupId}`
}

// 获取学生列表
const fetchStudents = async () => {
  try {
    const response = await getStudentList()
    students.value = response.data || []
  } catch (error) {
    console.error('获取学生列表失败', error)
    ElMessage.error('获取学生列表失败')
  }
}

const fetchClassGroups = async () => {
  try {
    classGroupsLoading.value = true
    const response = await getClassGroupList()
    classGroups.value = response.data || []
  } catch (error) {
    console.error('获取班级列表失败', error)
    ElMessage.error('获取班级列表失败')
  } finally {
    classGroupsLoading.value = false
  }
}

const loadPageData = async () => {
  await Promise.all([fetchStudents(), fetchClassGroups()])
}

const handleClassGroupChange = (classGroupId) => {
  const classGroup = classGroups.value.find(item => item.id === classGroupId)
  studentForm.major = classGroup?.major || ''
}

// 显示新增对话框
const showAddDialog = () => {
  dialogTitle.value = '新增学生'
  isEdit.value = false
  Object.assign(studentForm, {
    id: null,
    name: '',
    age: 20,
    major: '',
    classGroupId: null
  })
  dialogVisible.value = true
  studentFormRef.value?.clearValidate()
}

// 显示编辑对话框
const showEditDialog = (student) => {
  dialogTitle.value = '编辑学生'
  isEdit.value = true
  Object.assign(studentForm, { ...student })
  dialogVisible.value = true
  studentFormRef.value?.clearValidate()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await studentFormRef.value.validate()
    submitLoading.value = true

    if (isEdit.value) {
      await updateStudent(studentForm)
      ElMessage.success('更新学生成功')
    } else {
      await addStudent(studentForm)
      ElMessage.success('新增学生成功')
    }

    dialogVisible.value = false
    await fetchStudents()
  } catch (error) {
    console.error('操作失败', error)
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 导入/导出/下载模板
const handleDownloadTemplate = async () => {
  try {
    await ElMessageBox.confirm('确定要下载学生导入模板吗？下载后按照模板格式填写数据后再导入。', '下载模板', {
      confirmButtonText: '确定下载',
      cancelButtonText: '取消',
      type: 'info'
    })
  } catch {
    return
  }
  try {
    const blob = await downloadStudentTemplate()
    downloadBlob(blob, '学生导入模板.xlsx')
  } catch {
    ElMessage.error('下载模板失败')
  }
}
const handleImport = async (file) => {
  importing.value = true
  try {
    const res = await importStudents(file)
    const r = res.data
    if (r.failCount === 0) {
      ElMessage.success(`导入成功 ${r.successCount} 条`)
    } else {
      ElMessage.warning(`成功 ${r.successCount} 条，失败 ${r.failCount} 条：${r.errors.slice(0, 3).join('; ')}`)
    }
    await fetchStudents()
  } catch {
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
  return false
}
const handleExport = async () => {
  try {
    await ElMessageBox.confirm('确定要导出当前学生列表吗？', '导出确认', {
      confirmButtonText: '确定导出', cancelButtonText: '取消', type: 'info'
    })
  } catch { return }
  exporting.value = true
  try {
    const blob = await exportStudents()
    downloadBlob(blob, '学生列表.xlsx')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 删除学生
const handleDelete = async (student) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除学生 "${student.name}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    await deleteStudent(student.id)
    ElMessage.success('删除学生成功')
    await loadPageData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除学生失败')
    }
  }
}

const toggleMuteStatus = async (student) => {
  const nextMuted = !student.isMuted
  const actionText = nextMuted ? '禁言' : '解禁'
  try {
    await ElMessageBox.confirm(`确定要${actionText}学生 "${student.name}" 吗？`, `${actionText}确认`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await updateStudentMuteStatus(student.id, nextMuted)
    ElMessage.success(`${actionText}成功`)
    await loadPageData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${actionText}失败`)
    }
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1 // 搜索时重置到第一页
}

// 页大小改变
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
}

onMounted(() => {
  if (!dataLoaded.value) {
    loadPageData()
    dataLoaded.value = true
  }
})

onActivated(() => {
  // KeepAlive恢复时不需要重新加载
})
</script>

<style src="@/styles/crudPage.css"></style>
<style scoped>
</style>
