import request from '@/utils/request'

// 查询所有考试（管理员，含名称信息）
export const getExamList = () => request.get('/exam/list')

// 查询教师自己的考试
export const getTeacherExamList = (teacherId) => request.get('/exam/listByTeacherId', { params: { teacherId } })

// 新增考试
export const addExam = (exam) => request.post('/exam/add', exam)

// 更新考试
export const updateExam = (exam) => request.put('/exam/update', exam)

// 删除考试
export const deleteExam = (id) => request.delete(`/exam/delete/${id}`)

// 导入考试（Excel）
export const importExams = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/exam/import', formData)
}

// 导出考试（Excel）
export const exportExams = () =>
  request.get('/exam/export', { responseType: 'blob' })

// 下载考试导入模板
export const downloadExamTemplate = () =>
  request.get('/exam/template', { responseType: 'blob' })

// 导入成绩（Excel）
export const importGrades = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/exam-grade/import', formData)
}

// 导出成绩（Excel）
export const exportGrades = () =>
  request.get('/exam-grade/export', { responseType: 'blob' })

// 下载成绩导入模板
export const downloadGradeTemplate = () =>
  request.get('/exam-grade/template', { responseType: 'blob' })
