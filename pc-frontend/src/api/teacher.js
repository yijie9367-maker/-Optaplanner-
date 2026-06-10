import request from '@/utils/request'

// 查询教师列表
export const getTeacherList = () => request.get('/teacher/list')

// 新增教师
export const addTeacher = (teacher) => request.post('/teacher/add', teacher)

// 更新教师
export const updateTeacher = (teacher) => request.put('/teacher/update', teacher)

// 删除教师
export const deleteTeacher = (id) => request.delete(`/teacher/delete/${id}`)

// 导入教师（Excel）
export const importTeachers = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/teacher/import', formData)
}

// 导出教师（Excel）
export const exportTeachers = () =>
  request.get('/teacher/export', { responseType: 'blob' })

// 下载导入模板
export const downloadTeacherTemplate = () =>
  request.get('/teacher/template', { responseType: 'blob' })

// 更新禁言状态
export const updateTeacherMuteStatus = (id, muted) =>
  request.put(`/teacher/${id}/mute`, { muted })