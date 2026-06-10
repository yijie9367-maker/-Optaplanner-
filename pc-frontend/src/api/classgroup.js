import request from '@/utils/request'

export const getClassGroupList = () => request.get('/classgroup/list')
export const getClassGroupById = (id) => request.get(`/classgroup/${id}`)
export const addClassGroup = (classGroup) => request.post('/classgroup/add', classGroup)
export const updateClassGroup = (classGroup) => request.put('/classgroup/update', classGroup)
export const deleteClassGroup = (id) => request.delete(`/classgroup/delete/${id}`)
export const importClassGroups = (file) => { const f = new FormData(); f.append('file', file); return request.post('/classgroup/import', f) }
export const exportClassGroups = () => request.get('/classgroup/export', { responseType: 'blob' })
export const downloadClassGroupTemplate = () => request.get('/classgroup/template', { responseType: 'blob' })
export const getAssignedCourseIds = (id, semester) => request.get(`/classgroup/${id}/courses`, { params: { semester } })
export const assignClassGroupCourses = (id, payload) => request.put(`/classgroup/${id}/courses`, payload)

