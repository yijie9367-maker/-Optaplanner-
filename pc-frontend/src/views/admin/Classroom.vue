<template>
  <div class="table-container">
    <el-card>
      <div class="card-header">
        <h3>教室管理</h3>
        <div class="header-actions">
          <el-button type="info" @click="handleDownloadTemplate">下载模板</el-button>
          <el-upload :show-file-list="false" accept=".xlsx,.xls" :before-upload="handleImport" :auto-upload="true">
            <el-button type="warning" :loading="importing">导入</el-button>
          </el-upload>
          <el-button type="success" @click="handleExport" :loading="exporting">导出</el-button>
          <el-button type="primary" @click="showAddDialog">新增教室</el-button>
          <el-button @click="fetchClassrooms">刷新</el-button>
        </div>
      </div>

      <!-- 搜索和筛选 -->
      <div class="filter-section">
        <el-input
          v-model="searchText"
          placeholder="搜索教室名称或楼栋"
          style="width: 300px"
          clearable
          @input="handleSearch">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="buildingFilter"
          placeholder="按楼栋筛选"
          style="width: 200px; margin-left: 10px"
          clearable
          @change="handleFilter">
          <el-option
            v-for="building in buildings"
            :key="building"
            :label="building"
            :value="building" />
        </el-select>
        <el-select
          v-model="typeFilter"
          placeholder="按类型筛选"
          style="width: 200px; margin-left: 10px"
          clearable
          @change="handleFilter">
          <el-option
            v-for="type in roomTypes"
            :key="type"
            :label="type"
            :value="type" />
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
        <el-table-column prop="name" label="教室名称" width="120" />
        <el-table-column prop="building" label="楼栋" width="150" />
        <el-table-column prop="capacity" label="容量" align="center" width="100" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column label="操作" align="center" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="onDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :page-size="pageSize"
          :total="filteredClassrooms.length"
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
        ref="classroomFormRef"
        :model="classroomForm"
        :rules="formRules"
        label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="教室名称" prop="name">
              <el-input v-model="classroomForm.name" placeholder="如：A101" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="楼栋" prop="building">
              <el-select v-model="classroomForm.building" placeholder="请选择楼栋" style="width: 100%">
                <el-option
                  v-for="building in buildings"
                  :key="building"
                  :label="building"
                  :value="building" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="容量" prop="capacity">
              <el-input-number
                v-model="classroomForm.capacity"
                :min="10"
                :max="500"
                controls-position="right"
                style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-select v-model="classroomForm.type" placeholder="请选择教室类型" style="width: 100%">
                <el-option
                  v-for="type in roomTypes"
                  :key="type"
                  :label="type"
                  :value="type" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="onSubmit" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createCrudApi } from '@/utils/apiFactory'
import { useTableOperation } from '@/composables/useTableOperation'
import { useFormDialog, useCrudOperation } from '@/composables/useFormDialog'
import { importClassrooms, exportClassrooms, downloadClassroomTemplate } from '@/api/classroom'
import { downloadBlob } from '@/utils/download'

// ==================== API初始化 ====================
const classroomApi = createCrudApi('/classroom')

// ==================== 数据状态 ====================
const classrooms = ref([])
const { 
  currentPage, 
  pageSize, 
  searchText, 
  handleSearch, 
  indexMethod
} = useTableOperation(() =>
  classroomApi.getList().then(response => {
    classrooms.value = response.data || []
    return classrooms.value
  })
)

// ==================== 对话框和表单 ====================
const { dialogVisible, isEdit, openAddDialog, openEditDialog } = useFormDialog()
const { handleSubmit, handleDelete, submitLoading } = useCrudOperation()

const classroomFormRef = ref()
const classroomForm = reactive({
  id: null,
  name: '',
  building: '',
  capacity: 60,
  type: '',
  hasProjector: false,
  hasComputer: false
})

// ==================== 筛选状态 ====================
const buildingFilter = ref('')
const typeFilter = ref('')

// ==================== 数据选项 ====================
const buildings = [
  '一号教学楼', '二号教学楼', '三号教学楼', '四号教学楼',
  '五号教学楼', '图书馆', '实验楼', '体育馆'
]

const roomTypes = [
  '普通教室', '多媒体教室', '实验室', '阶梯教室',
  '报告厅', '会议室', '体育馆'
]

// ==================== 表单验证规则 ====================
const formRules = {
  name: [
    { required: true, message: '请输入教室名称', trigger: 'blur' },
    { min: 2, max: 20, message: '教室名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  building: [
    { required: true, message: '请选择楼栋', trigger: 'change' }
  ],
  capacity: [
    { required: true, message: '请输入容量', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择教室类型', trigger: 'change' }
  ]
}

// ==================== 计算属性 ====================
const filteredClassrooms = computed(() => {
  let filtered = classrooms.value

  if (searchText.value) {
    filtered = filtered.filter(classroom =>
      classroom.name.toLowerCase().includes(searchText.value.toLowerCase()) ||
      classroom.building.toLowerCase().includes(searchText.value.toLowerCase())
    )
  }

  if (buildingFilter.value) {
    filtered = filtered.filter(classroom => classroom.building === buildingFilter.value)
  }

  if (typeFilter.value) {
    filtered = filtered.filter(classroom => classroom.type === typeFilter.value)
  }

  return filtered
})

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredClassrooms.value.slice(start, end)
})

const dialogTitle = computed(() => {
  return isEdit.value ? '编辑教室' : '新增教室'
})

// ==================== 方法 ====================
const showAddDialog = () => {
  openAddDialog('教室', classroomForm, classroomFormRef.value)
}

const showEditDialog = (row) => {
  openEditDialog('教室', row, classroomForm, classroomFormRef.value)
}

const fetchClassrooms = async () => {
  const response = await classroomApi.getList()
  classrooms.value = response.data || []
}

const importing = ref(false)
const exporting = ref(false)

const handleDownloadTemplate = async () => {
  try {
    await ElMessageBox.confirm('确定要下载教室导入模板吗？下载后按照模板格式填写数据后再导入。', '下载模板', {
      confirmButtonText: '确定下载',
      cancelButtonText: '取消',
      type: 'info'
    })
  } catch {
    return
  }
  try {
    const blob = await downloadClassroomTemplate()
    downloadBlob(blob, '教室导入模板.xlsx')
  } catch {
    ElMessage.error('下载模板失败')
  }
}
const handleImport = async (file) => {
  importing.value = true
  try {
    const res = await importClassrooms(file)
    const r = res.data
    if (r.failCount === 0) {
      ElMessage.success(`导入成功 ${r.successCount} 条`)
    } else {
      ElMessage.warning(`成功 ${r.successCount} 条，失败 ${r.failCount} 条：${r.errors.slice(0, 3).join('; ')}`)
    }
    await fetchClassrooms()
  } catch {
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
  return false
}
const handleExport = async () => {
  try {
    await ElMessageBox.confirm('确定要导出当前教室列表吗？', '导出确认', {
      confirmButtonText: '确定导出', cancelButtonText: '取消', type: 'info'
    })
  } catch { return }
  exporting.value = true
  try {
    const blob = await exportClassrooms()
    downloadBlob(blob, '教室列表.xlsx')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

const onSubmit = async () => {
  await handleSubmit({
    formRef: classroomFormRef.value,
    formData: classroomForm,
    isEdit: isEdit.value,
    onAdd: classroomApi.add,
    onUpdate: classroomApi.update,
    resourceName: '教室',
    onSuccess: () => {
      dialogVisible.value = false
      fetchClassrooms()
    }
  })
}

const onDelete = async (row) => {
  await handleDelete({
    data: row,
    onDelete: classroomApi.delete,
    resourceName: '教室',
    resourceDisplay: row.name,
    onSuccess: fetchClassrooms
  })
}

const handleFilter = () => {
  currentPage.value = 1
}

const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
}

// 初始化加载
fetchClassrooms()
</script>

<style src="@/styles/crudPage.css"></style>
<style scoped>
</style>
