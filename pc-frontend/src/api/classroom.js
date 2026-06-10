import request from '@/utils/request'

export const getClassroomList = () => request.get('/classroom/list')
export const getClassroomById = (id) => request.get(`/classroom/${id}`)
export const addClassroom = (classroom) => request.post('/classroom/add', classroom)
export const updateClassroom = (classroom) => request.put('/classroom/update', classroom)
export const deleteClassroom = (id) => request.delete(`/classroom/delete/${id}`)
export const importClassrooms = (file) => { const f = new FormData(); f.append('file', file); return request.post('/classroom/import', f) }
export const exportClassrooms = () => request.get('/classroom/export', { responseType: 'blob' })
export const downloadClassroomTemplate = () => request.get('/classroom/template', { responseType: 'blob' })

