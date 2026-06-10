/**
 * 统一的表单对话框Composable
 * 消除showAddDialog、showEditDialog的重复代码
 * 用于：Students、Teachers、Courses、Classroom等CRUD页面
 */

import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export function useFormDialog(formRefName = 'formRef') {
  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const submitLoading = ref(false)
  const isEdit = ref(false)

  // 打开新增对话框
  const openAddDialog = (title, initialForm, formRef) => {
    dialogTitle.value = `新增${title}`
    isEdit.value = false
    // 重置表单为初始值
    Object.keys(initialForm).forEach(key => {
      initialForm[key] = null
    })
    dialogVisible.value = true
    formRef?.clearValidate?.()
  }

  // 打开编辑对话框
  const openEditDialog = (title, data, initialForm, formRef) => {
    dialogTitle.value = `编辑${title}`
    isEdit.value = true
    Object.assign(initialForm, { ...data })
    dialogVisible.value = true
    formRef?.clearValidate?.()
  }

  // 关闭对话框
  const closeDialog = () => {
    dialogVisible.value = false
  }

  return {
    dialogVisible,
    dialogTitle,
    submitLoading,
    isEdit,
    openAddDialog,
    openEditDialog,
    closeDialog
  }
}

/**
 * 统一的CRUD操作Composable
 * 消除handleSubmit、handleDelete的重复代码
 */
export function useCrudOperation() {
  const submitLoading = ref(false)

  // 提交表单（新增或更新）
  const handleSubmit = async (options) => {
    const {
      formRef,
      formData,
      isEdit,
      onAdd,
      onUpdate,
      resourceName,
      onSuccess
    } = options

    try {
      await formRef?.validate?.()
      submitLoading.value = true

      if (isEdit) {
        await onUpdate(formData)
        ElMessage.success(`更新${resourceName}成功`)
      } else {
        await onAdd(formData)
        ElMessage.success(`新增${resourceName}成功`)
      }

      onSuccess?.()
    } catch (error) {
      console.error('表单提交失败:', error)
      if (error.message && !error.message.includes('cancel')) {
        ElMessage.error(error.message || `操作${resourceName}失败`)
      }
    } finally {
      submitLoading.value = false
    }
  }

  // 删除记录
  const handleDelete = async (options) => {
    const {
      data,
      onDelete,
      resourceName,
      resourceDisplay,
      onSuccess
    } = options

    try {
      await ElMessageBox.confirm(
        `确定要删除${resourceName} "${resourceDisplay || ''}" 吗？此操作不可恢复。`,
        '确认删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )

      await onDelete(data.id)
      ElMessage.success(`删除${resourceName}成功`)
      onSuccess?.()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除失败:', error)
        ElMessage.error(`删除${resourceName}失败`)
      }
    }
  }

  return {
    submitLoading,
    handleSubmit,
    handleDelete
  }
}
