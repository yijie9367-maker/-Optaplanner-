import request from '@/utils/request'

export const getScheduleList = () => request.get('/schedule/list')
export const getSchedulePageList = (page = 0, size = 20, keyword = '') => request.get('/schedule/pageList', { params: { page, size, keyword: keyword || undefined } })
export const getScheduleById = (id) => request.get(`/schedule/${id}`)
export const addSchedule = (schedule) => request.post('/schedule/add', schedule)
export const updateSchedule = (schedule) => request.put('/schedule/update', schedule)
export const deleteSchedule = (id) => request.delete(`/schedule/delete/${id}`)
export const importSchedules = (file) => { const f = new FormData(); f.append('file', file); return request.post('/schedule/import', f) }
export const exportSchedules = () => request.get('/schedule/export', { responseType: 'blob' })
export const downloadScheduleTemplate = () => request.get('/schedule/template', { responseType: 'blob' })

