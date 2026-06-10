<template>
  <div class="admin-management">
    <h2>管理员管理</h2>
    
    <el-card class="table-card">
      <div style="margin-bottom: 20px; display: flex; gap: 10px; align-items: center;">
        <el-button type="info" @click="handleDownloadTemplate">下载模板</el-button>
        <el-upload :show-file-list="false" accept=".xlsx,.xls" :before-upload="handleImport" :auto-upload="true" v-if="isSuperAdmin">
          <el-button type="warning" :loading="importing">导入</el-button>
        </el-upload>
        <el-button type="success" @click="handleExport" :loading="exporting">导出</el-button>
        <el-button type="primary" @click="showAddDialog" v-if="isSuperAdmin">
          新增管理员
        </el-button>
        <el-select v-model="pageSize" style="width: 110px; margin-left: auto" @change="currentPage = 1">
          <el-option v-for="s in [5, 10, 15, 20]" :key="s" :label="s + ' 条/页'" :value="s" />
        </el-select>
      </div>
      <div v-if="!isSuperAdmin" class="permission-notice">
        <el-alert title="权限提示" type="info" show-icon :closable="false">
          只有超级管理员可以新增、编辑和删除管理员
        </el-alert>
      </div>
      
      <el-table :data="paginatedList" style="width: 100%" border>
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button v-if="isSuperAdmin" size="small" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button v-if="isSuperAdmin" size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
            <span v-if="!isSuperAdmin" class="permission-text">无权限编辑</span>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; text-align: center;">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :page-size="pageSize"
          :total="adminList.length"
          v-model:current-page="currentPage" />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogMode === 'add' ? '新增管理员' : '编辑管理员'"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" v-if="dialogMode === 'add'">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getAdminList, addAdmin, updateAdmin, deleteAdmin, importAdmins, exportAdmins, downloadAdminTemplate } from '@/api/admin'
import { downloadBlob } from '@/utils/download'

const userStore = useUserStore()

const adminList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return adminList.value.slice(start, start + pageSize.value)
})
const dialogVisible = ref(false)
const dialogMode = ref('add')
const form = ref({
  id: null,
  username: '',
  password: ''
})
const importing = ref(false)
const exporting = ref(false)

// ==================== 权限判断 ====================
// 检查是否为超级管理员（用户名为 admin）
const isSuperAdmin = computed(() => {
  return userStore.admin?.username === 'admin'
})

// 导入/导出/下载模板
const handleDownloadTemplate = async () => {
  try {
    await ElMessageBox.confirm('确定要下载管理员导入模板吗？下载后按照模板格式填写数据后再导入。', '下载模板', {
      confirmButtonText: '确定下载',
      cancelButtonText: '取消',
      type: 'info'
    })
  } catch {
    return
  }
  try {
    const blob = await downloadAdminTemplate()
    downloadBlob(blob, '管理员导入模板.xlsx')
  } catch {
    ElMessage.error('下载模板失败')
  }
}
const handleImport = async (file) => {
  importing.value = true
  try {
    const res = await importAdmins(file)
    const r = res.data
    if (r.failCount === 0) {
      ElMessage.success(`导入成功 ${r.successCount} 条`)
    } else {
      ElMessage.warning(`成功 ${r.successCount} 条，失败 ${r.failCount} 条：${r.errors.slice(0, 3).join('; ')}`)
    }
    await fetchAdminList()
  } catch {
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
  return false
}
const handleExport = async () => {
  try {
    await ElMessageBox.confirm('确定要导出当前管理员列表吗？', '导出确认', {
      confirmButtonText: '确定导出', cancelButtonText: '取消', type: 'info'
    })
  } catch { return }
  exporting.value = true
  try {
    const blob = await exportAdmins()
    downloadBlob(blob, '管理员列表.xlsx')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 获取管理员列表
const fetchAdminList = async () => {
  try {
    const res = await getAdminList()
    adminList.value = res.data || []
  } catch (error) {
    console.error('获取管理员列表失败', error)
    ElMessage.error('获取管理员列表失败')
  }
}

// 显示新增对话框
const showAddDialog = () => {
  if (!isSuperAdmin.value) {
    ElMessage.error('只有超级管理员才能新增管理员')
    return
  }
  dialogMode.value = 'add'
  form.value = {
    id: null,
    username: '',
    password: ''
  }
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  if (!isSuperAdmin.value) {
    ElMessage.error('只有超级管理员才能编辑管理员')
    return
  }
  dialogMode.value = 'edit'
  form.value = {
    id: row.id,
    username: row.username,
    password: ''
  }
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  try {
    if (dialogMode.value === 'add') {
      await addAdmin(form.value)
      ElMessage.success('添加成功')
    } else {
      await updateAdmin(form.value)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    fetchAdminList()
  } catch (error) {
    console.error('操作失败', error)
    ElMessage.error('操作失败')
  }
}

// 删除管理员
const handleDelete = async (id) => {
  if (!isSuperAdmin.value) {
    ElMessage.error('只有超级管理员才能删除管理员')
    return
  }
  try {
    await ElMessageBox.confirm('确定要删除该管理员吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAdmin(id)
    ElMessage.success('删除成功')
    fetchAdminList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchAdminList()
})
</script>

<style scoped>
.admin-management {
  padding: 20px;
}

.table-card {
  margin-top: 20px;
}

.permission-notice {
  margin-bottom: 20px;
}

.permission-text {
  color: #909399;
  font-size: 12px;
}
</style>
