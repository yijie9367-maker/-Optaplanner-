import request from '@/utils/request'

export const forumAPI = {
  // 获取帖子列表（搜索、排序、分页）
  getPosts(params) {
    return request.get('/forum/posts', { params })
  },

  // 获取帖子详情（含评论）
  getPostDetail(id, params) {
    return request.get(`/forum/posts/${id}`, { params })
  },

  // 发帖
  createPost(data) {
    return request.post('/forum/posts', data)
  },

  // 编辑帖子（管理员）
  updatePost(id, data) {
    return request.put(`/forum/posts/${id}`, data)
  },

  // 删除帖子（管理员或帖子作者）
  deletePost(id) {
    return request.delete(`/forum/posts/${id}`)
  },

  // 置顶/取消置顶（管理员）
  pinPost(id, pinned) {
    return request.put(`/forum/posts/${id}/pin`, { pinned })
  },

  // 点赞/踩（toggle）
  vote(id, data) {
    return request.post(`/forum/posts/${id}/vote`, data)
  },

  // 发评论
  addComment(id, data) {
    return request.post(`/forum/posts/${id}/comments`, data)
  },

  // 删除评论（管理员）
  deleteComment(id) {
    return request.delete(`/forum/comments/${id}`)
  },

  // ===== 违禁词管理（管理员） =====
  getSensitiveWords() {
    return request.get('/sensitive-words')
  },
  addSensitiveWords(words) {
    return request.post('/sensitive-words', { words })
  },
  deleteSensitiveWord(id) {
    return request.delete(`/sensitive-words/${id}`)
  }
}
