import request from '@/utils/request'

// 查询学生列表
export const getStudentList = () => request.get('/student/list')

// 新增学生
export const addStudent = (student) => request.post('/student/add', student)

// 更新学生
export const updateStudent = (student) => request.put('/student/update', student)

// 删除学生
export const deleteStudent = (id) => request.delete(`/student/delete/${id}`)

// 导入学生（Excel）
export const importStudents = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/student/import', formData)
}

// 导出学生（Excel）
export const exportStudents = () =>
  request.get('/student/export', { responseType: 'blob' })

// 下载导入模板
export const downloadStudentTemplate = () =>
  request.get('/student/template', { responseType: 'blob' })

// 更新禁言状态
export const updateStudentMuteStatus = (id, muted) =>
  request.put(`/student/${id}/mute`, { muted })