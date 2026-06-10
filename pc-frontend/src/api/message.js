import request from '@/utils/request'

export const messageAPI = {
  // 获取所有已发布的消息
  getPublishedMessages() {
    return request.get('/message/list')
  },

  // 根据类型获取消息
  getMessagesByType(type) {
    return request.get(`/message/list/${type}`)
  },

  // 获取热门消息
  getHotMessages(limit = 5) {
    return request.get('/message/hotlist', { params: { limit } })
  },

  // 获取消息详情
  getMessageDetail(id) {
    return request.get(`/message/${id}`)
  },

  // 新增消息
  addMessage(data) {
    return request.post('/message/add', data)
  },

  // 更新消息
  updateMessage(data) {
    return request.put('/message/update', data)
  },

  // 删除消息
  deleteMessage(id) {
    return request.delete(`/message/delete/${id}`)
  },

  // 点赞消息
  likeMessage(id) {
    return request.post(`/message/like/${id}`)
  },

  // 获取所有消息（管理后台）
  getAllMessages() {
    return request.get('/message/all')
  },

  // 按目标受众获取已发布消息（学生/教师端使用）
  getMessagesByTarget(target) {
    return request.get(`/message/byTarget/${target}`)
  },

  // 按目标受众获取所有消息（含草稿，管理后台使用）
  getAdminMessagesByTarget(target) {
    return request.get(`/message/adminList/${target}`)
  },

  // 发布草稿
  publishMessage(id) {
    return request.post(`/message/publish/${id}`)
  },

  // 获取消息统计
  getMessageStats() {
    return request.get('/message/stats')
  }
}
