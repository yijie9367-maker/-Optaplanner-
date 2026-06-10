import request from '@/utils/request'

export const loginAdmin = (username, password) => request.post('/admin/login', { username, password })
export const getAdminList = () => request.get('/admin/list')
export const getDashboardStats = () => request.get('/admin/dashboard-stats')
export const addAdmin = (adminUser) => request.post('/admin/add', adminUser)
export const updateAdmin = (adminUser) => request.put('/admin/update', adminUser)
export const deleteAdmin = (id) => request.delete(`/admin/delete/${id}`)
export const importAdmins = (file) => { const f = new FormData(); f.append('file', file); return request.post('/admin/import', f) }
export const exportAdmins = () => request.get('/admin/export', { responseType: 'blob' })
export const downloadAdminTemplate = () => request.get('/admin/template', { responseType: 'blob' })

