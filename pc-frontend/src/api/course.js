import request from '@/utils/request'

export const getCourseList = () => request.get('/course/list')
export const getCourseById = (id) => request.get(`/course/${id}`)
export const addCourse = (course) => request.post('/course/add', course)
export const updateCourse = (course) => request.put('/course/update', course)
export const deleteCourse = (id) => request.delete(`/course/delete/${id}`)
export const importCourses = (file) => { const f = new FormData(); f.append('file', file); return request.post('/course/import', f) }
export const exportCourses = () => request.get('/course/export', { responseType: 'blob' })
export const downloadCourseTemplate = () => request.get('/course/template', { responseType: 'blob' })

