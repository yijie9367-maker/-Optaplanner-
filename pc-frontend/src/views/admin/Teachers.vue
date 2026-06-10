<template>
  <div class="table-container">
    <el-card>
      <div class="card-header">
        <h3>教师管理</h3>
        <div class="header-actions">
          <el-button type="info" @click="handleDownloadTemplate">下载模板</el-button>
          <el-upload :show-file-list="false" accept=".xlsx,.xls" :before-upload="handleImport" :auto-upload="true">
            <el-button type="warning" :loading="importing">导入</el-button>
          </el-upload>
          <el-button type="success" @click="handleExport" :loading="exporting">导出</el-button>
          <el-button type="primary" @click="showAddDialog">新增教师</el-button>
          <el-button @click="fetchTeachers">刷新</el-button>
        </div>
      </div>

      <!-- 搜索和筛选 -->
      <div class="filter-section">
        <el-input
          v-model="searchText"
          placeholder="搜索教师姓名或职称"
          style="width: 300px"
          clearable
          @input="handleSearch">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="departmentFilter"
          placeholder="按院系筛选"
          style="width: 200px; margin-left: 10px"
          clearable
          @change="handleFilter">
          <el-option
            v-for="dept in departmentOptions"
            :key="dept"
            :label="dept"
            :value="dept" />
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
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="title" label="职称" width="120" />
        <el-table-column prop="department" label="院系" width="200" show-overflow-tooltip />
        <el-table-column label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.updatedAt) }}
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
          :total="filteredTeachers.length"
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
        ref="teacherFormRef"
        :model="teacherForm"
        :rules="formRules"
        label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="teacherForm.name" placeholder="请输入教师姓名" />
        </el-form-item>
        <el-form-item label="职称" prop="title">
          <el-select v-model="teacherForm.title" placeholder="请选择职称" style="width: 100%">
            <el-option label="教授" value="教授" />
            <el-option label="副教授" value="副教授" />
            <el-option label="讲师" value="讲师" />
            <el-option label="助教" value="助教" />
          </el-select>
        </el-form-item>
        <el-form-item label="院系" prop="department">
          <el-select
            v-model="teacherForm.department"
            placeholder="请选择院系"
            style="width: 100%"
            filterable
            clearable>
            <el-option
              v-for="department in departmentOptions"
              :key="department"
              :label="department"
              :value="department" />
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
import { getTeacherList, addTeacher, updateTeacher, deleteTeacher, importTeachers, exportTeachers, downloadTeacherTemplate, updateTeacherMuteStatus } from '@/api/teacher'
import { downloadBlob } from '@/utils/download'
import { formatDateTime } from '@/utils/formatters'

const teachers = ref([])           // 全部教师数据
const currentPage = ref(1)         // 当前页
const pageSize = ref(15)           // 每页显示数量
const searchText = ref('')         // 搜索文本
const departmentFilter = ref('')   // 院系筛选

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增教师')
const submitLoading = ref(false)
const isEdit = ref(false)

// 缓存标志
const dataLoaded = ref(false)

const importing = ref(false)
const exporting = ref(false)

// 表单数据
const teacherForm = reactive({
  id: null,
  name: '',
  title: '',
  department: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入教师姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请选择职称', trigger: 'change' }
  ],
  department: [
    { required: true, message: '请选择院系', trigger: 'change' }
  ]
}

const teacherFormRef = ref()

const departmentOptions = computed(() => {
  const departmentSet = new Set(
    teachers.value
      .map(teacher => teacher.department?.trim())
      .filter(Boolean)
  )

  if (teacherForm.department?.trim()) {
    departmentSet.add(teacherForm.department.trim())
  }

  return Array.from(departmentSet).sort((left, right) => left.localeCompare(right, 'zh-CN'))
})

// 过滤后的教师数据
const filteredTeachers = computed(() => {
  let filtered = teachers.value

  // 搜索过滤
  if (searchText.value) {
    filtered = filtered.filter(teacher =>
      teacher.name.toLowerCase().includes(searchText.value.toLowerCase()) ||
      teacher.title.toLowerCase().includes(searchText.value.toLowerCase())
    )
  }

  // 院系过滤
  if (departmentFilter.value) {
    filtered = filtered.filter(teacher => teacher.department === departmentFilter.value)
  }

  return filtered
})

// 计算当前页的数据
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredTeachers.value.slice(start, end)
})

// 获取教师列表
const fetchTeachers = async () => {
  try {
    const response = await getTeacherList()
    teachers.value = response.data || []
  } catch (error) {
    console.error('获取教师列表失败', error)
    ElMessage.error('获取教师列表失败')
  }
}

// 显示新增对话框
const showAddDialog = () => {
  dialogTitle.value = '新增教师'
  isEdit.value = false
  Object.assign(teacherForm, {
    id: null,
    name: '',
    title: '',
    department: ''
  })
  dialogVisible.value = true
  teacherFormRef.value?.clearValidate()
}

// 显示编辑对话框
const showEditDialog = (teacher) => {
  dialogTitle.value = '编辑教师'
  isEdit.value = true
  Object.assign(teacherForm, { ...teacher })
  dialogVisible.value = true
  teacherFormRef.value?.clearValidate()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await teacherFormRef.value.validate()
    submitLoading.value = true

    if (isEdit.value) {
      await updateTeacher(teacherForm)
      ElMessage.success('更新教师成功')
    } else {
      await addTeacher(teacherForm)
      ElMessage.success('新增教师成功')
    }

    dialogVisible.value = false
    await fetchTeachers()
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
    await ElMessageBox.confirm('确定要下载教师导入模板吗？下载后按照模板格式填写数据后再导入。', '下载模板', {
      confirmButtonText: '确定下载',
      cancelButtonText: '取消',
      type: 'info'
    })
  } catch {
    return
  }
  try {
    const blob = await downloadTeacherTemplate()
    downloadBlob(blob, '教师导入模板.xlsx')
  } catch {
    ElMessage.error('下载模板失败')
  }
}
const handleImport = async (file) => {
  importing.value = true
  try {
    const res = await importTeachers(file)
    const r = res.data
    if (r.failCount === 0) {
      ElMessage.success(`导入成功 ${r.successCount} 条`)
    } else {
      ElMessage.warning(`成功 ${r.successCount} 条，失败 ${r.failCount} 条：${r.errors.slice(0, 3).join('; ')}`)
    }
    await fetchTeachers()
  } catch {
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
  return false
}
const handleExport = async () => {
  try {
    await ElMessageBox.confirm('确定要导出当前教师列表吗？', '导出确认', {
      confirmButtonText: '确定导出', cancelButtonText: '取消', type: 'info'
    })
  } catch { return }
  exporting.value = true
  try {
    const blob = await exportTeachers()
    downloadBlob(blob, '教师列表.xlsx')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 删除教师
const handleDelete = async (teacher) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除教师 "${teacher.name}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    await deleteTeacher(teacher.id)
    ElMessage.success('删除教师成功')
    await fetchTeachers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除教师失败')
    }
  }
}

const toggleMuteStatus = async (teacher) => {
  const nextMuted = !teacher.isMuted
  const actionText = nextMuted ? '禁言' : '解禁'
  try {
    await ElMessageBox.confirm(`确定要${actionText}教师 "${teacher.name}" 吗？`, `${actionText}确认`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await updateTeacherMuteStatus(teacher.id, nextMuted)
    ElMessage.success(`${actionText}成功`)
    await fetchTeachers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${actionText}失败`)
    }
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
}

// 筛选处理
const handleFilter = () => {
  currentPage.value = 1
}

// 页大小改变
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
}

// 格式化时间
// 序号方法（用于表格序号列）
const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

onMounted(() => {
  if (!dataLoaded.value) {
    fetchTeachers()
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
