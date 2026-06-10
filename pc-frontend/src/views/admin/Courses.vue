<template>
  <div class="table-container">
    <el-card>
      <!-- 卡片头部 -->
      <div class="card-header">
        <h3>课程管理</h3>
        <div class="header-actions">
          <el-button type="info" @click="handleDownloadTemplate">下载模板</el-button>
          <el-upload :show-file-list="false" accept=".xlsx,.xls" :before-upload="handleImport" :auto-upload="true">
            <el-button type="warning" :loading="importing">导入</el-button>
          </el-upload>
          <el-button type="success" @click="handleExport" :loading="exporting">导出</el-button>
          <el-button type="primary" @click="showAddDialog">新增课程</el-button>
          <el-button @click="fetchCourses">刷新</el-button>
        </div>
      </div>

      <!-- 搜索和筛选 -->
      <div class="filter-section">
        <el-input
          v-model="searchText"
          placeholder="搜索课程名称或编号"
          style="width: 300px"
          clearable
          @input="handleSearch">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="teacherFilter"
          placeholder="按教师筛选"
          style="width: 200px; margin-left: 10px"
          clearable
          @change="handleFilter">
          <el-option
            v-for="teacher in teachers"
            :key="teacher.id"
            :label="teacher.name"
            :value="teacher.id" />
        </el-select>
        <el-select v-model="pageSize" style="width: 110px; margin-left: auto" @change="currentPage = 1">
          <el-option v-for="s in [5, 10, 15, 20]" :key="s" :label="s + ' 条/页'" :value="s" />
        </el-select>
      </div>

      <!-- 课程表格 -->
      <el-table
        :data="paginatedData"
        style="width: 100%"
        table-layout="fixed"
        :cell-style="{ padding: '8px 12px' }"
        :header-cell-style="{ padding: '12px 12px', fontWeight: 'bold' }"
        stripe>
        <el-table-column prop="code" label="课程编号" width="120" />
        <el-table-column prop="name" label="课程名称" width="200" />
        <el-table-column prop="teacherName" label="授课教师" width="120" />
        <el-table-column prop="credit" label="学分" align="center" width="80" />
        <!-- 操作列 -->
        <el-table-column label="操作" align="center" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :page-size="pageSize"
          :total="filteredCourses.length"
          v-model:current-page="currentPage">
        </el-pagination>
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false">
      <el-form
        ref="courseFormRef"
        :model="courseForm"
        :rules="formRules"
        label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程编号" prop="code">
              <el-input v-model="courseForm.code" placeholder="如：CS101" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程名称" prop="name">
              <el-input v-model="courseForm.name" placeholder="请输入课程名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="授课教师" prop="teacherId">
              <el-select v-model="courseForm.teacherId" placeholder="请选择教师" style="width: 100%" @change="updateTeacherName">
                <el-option
                  v-for="teacher in teachers"
                  :key="teacher.id"
                  :label="`${teacher.name} (${teacher.title})`"
                  :value="teacher.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学分" prop="credit">
              <el-input-number
                v-model="courseForm.credit"
                :min="0.5"
                :max="10"
                :step="0.5"
                controls-position="right"
                style="width: 100%" />
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onActivated, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getCourseList, addCourse, updateCourse, deleteCourse, importCourses, exportCourses, downloadCourseTemplate } from '@/api/course'
import { getTeacherList } from '@/api/teacher'
import { downloadBlob } from '@/utils/download'
import { formatDateTime } from '@/utils/formatters'

const courses = ref([])            // 全部课程数据
const teachers = ref([])           // 教师列表
const currentPage = ref(1)         // 当前页
const pageSize = ref(15)           // 每页显示数量
const searchText = ref('')         // 搜索文本
const teacherFilter = ref(null)    // 教师筛选

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增课程')
const submitLoading = ref(false)
const isEdit = ref(false)

// 缓存标志
const dataLoaded = ref(false)

const importing = ref(false)
const exporting = ref(false)

// 表单数据
const courseForm = reactive({
  id: null,
  code: '',
  name: '',
  teacherId: null,
  teacherName: '',
  credit: 3.0
})

// 表单验证规则
const formRules = {
  code: [
    { required: true, message: '请输入课程编号', trigger: 'blur' },
    { pattern: /^[A-Z]{2,4}\d{3}$/, message: '课程编号格式应为如：CS101', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, max: 50, message: '课程名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  teacherId: [
    { required: true, message: '请选择授课教师', trigger: 'change' }
  ],
  credit: [
    { required: true, message: '请输入学分', trigger: 'blur' }
  ]
}

const courseFormRef = ref()

// 过滤后的课程数据
const filteredCourses = computed(() => {
  let filtered = courses.value

  // 搜索过滤
  if (searchText.value) {
    filtered = filtered.filter(course =>
      course.name.toLowerCase().includes(searchText.value.toLowerCase()) ||
      course.code.toLowerCase().includes(searchText.value.toLowerCase())
    )
  }

  // 教师过滤
  if (teacherFilter.value) {
    filtered = filtered.filter(course => course.teacherId === teacherFilter.value)
  }

  return filtered
})

// 计算当前页的数据
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredCourses.value.slice(start, end)
})

// 获取课程列表
const fetchCourses = async () => {
  try {
    const response = await getCourseList()
    const courseData = response.data || []
    // 关联教师名称
    courses.value = courseData.map(course => ({
      ...course,
      teacherName: teachers.value.find(t => t.id === course.teacherId)?.name || '未知'
    }))
  } catch (error) {
    console.error('获取课程列表失败', error)
    ElMessage.error('获取课程列表失败')
  }
}

// 获取教师列表
const fetchTeachers = async () => {
  try {
    const response = await getTeacherList()
    teachers.value = response.data || []
    // 教师列表加载完成后，重新加载课程以关联教师名称
    if (courses.value.length === 0) {
      fetchCourses()
    } else {
      // 如果课程已加载，更新教师名称
      courses.value = courses.value.map(course => ({
        ...course,
        teacherName: teachers.value.find(t => t.id === course.teacherId)?.name || '未知'
      }))
    }
  } catch (error) {
    console.error('获取教师列表失败', error)
  }
}

// 更新教师姓名
const updateTeacherName = () => {
  const teacher = teachers.value.find(t => t.id === courseForm.teacherId)
  if (teacher) {
    courseForm.teacherName = teacher.name
  }
}

// 显示新增对话框
const showAddDialog = () => {
  dialogTitle.value = '新增课程'
  isEdit.value = false
  Object.assign(courseForm, {
    id: null,
    code: '',
    name: '',
    teacherId: null,
    teacherName: '',
    credit: 3.0
  })
  dialogVisible.value = true
  courseFormRef.value?.clearValidate()
}

// 显示编辑对话框
const showEditDialog = (course) => {
  dialogTitle.value = '编辑课程'
  isEdit.value = true
  Object.assign(courseForm, { 
    ...course,
    teacherName: teachers.value.find(t => t.id === course.teacherId)?.name || ''
  })
  dialogVisible.value = true
  courseFormRef.value?.clearValidate()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await courseFormRef.value.validate()
    submitLoading.value = true

    if (isEdit.value) {
      await updateCourse(courseForm)
      ElMessage.success('更新课程成功')
    } else {
      await addCourse(courseForm)
      ElMessage.success('新增课程成功')
    }

    dialogVisible.value = false
    await fetchCourses()
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
    await ElMessageBox.confirm('确定要下载课程导入模板吗？下载后按照模板格式填写数据后再导入。', '下载模板', {
      confirmButtonText: '确定下载',
      cancelButtonText: '取消',
      type: 'info'
    })
  } catch {
    return
  }
  try {
    const blob = await downloadCourseTemplate()
    downloadBlob(blob, '课程导入模板.xlsx')
  } catch {
    ElMessage.error('下载模板失败')
  }
}
const handleImport = async (file) => {
  importing.value = true
  try {
    const res = await importCourses(file)
    const r = res.data
    if (r.failCount === 0) {
      ElMessage.success(`导入成功 ${r.successCount} 条`)
    } else {
      ElMessage.warning(`成功 ${r.successCount} 条，失败 ${r.failCount} 条：${r.errors.slice(0, 3).join('; ')}`)
    }
    await fetchCourses()
  } catch {
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
  return false
}
const handleExport = async () => {
  try {
    await ElMessageBox.confirm('确定要导出当前课程列表吗？', '导出确认', {
      confirmButtonText: '确定导出', cancelButtonText: '取消', type: 'info'
    })
  } catch { return }
  exporting.value = true
  try {
    const blob = await exportCourses()
    downloadBlob(blob, '课程列表.xlsx')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 删除课程
const handleDelete = async (course) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除课程 "${course.name}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    await deleteCourse(course.id)
    ElMessage.success('删除课程成功')
    await fetchCourses()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除课程失败')
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
