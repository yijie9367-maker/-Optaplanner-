/**
 * API 工具 - 封装对后端的所有请求
 * 后端地址: http://localhost:8084
 */

// TODO: 修改为你的后端地址
const BASE_URL = 'http://localhost:8084';

/**
 * 通用请求
 */
function request(path, options = {}) {
  const { method = 'GET', data, needAuth = true } = options;

  const header = { 'Content-Type': 'application/json' };
  const token = wx.getStorageSync('token');
  if (needAuth && token) {
    header['Authorization'] = 'Bearer ' + token;
  }

  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE_URL + path,
      method,
      data,
      header,
      success(res) {
        if (res.statusCode === 200) {
          resolve(res.data);
        } else if (res.statusCode === 401) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          wx.showToast({ title: '请先登录', icon: 'none' });
          reject(res);
        } else {
          reject(res);
        }
      },
      fail(err) {
        reject(err);
      }
    });
  });
}

// ==================== 学生相关 ====================
const studentApi = {
  login(name, password) {
    return request('/student/login', { method: 'POST', data: { name, password }, needAuth: false });
  },
  getById(id) {
    return request(`/student/${id}`);
  },
  update(data) {
    return request('/student/update', { method: 'PUT', data });
  }
};

// ==================== 课表相关 ====================
const scheduleApi = {
  listByClassGroupId(classGroupId) {
    return request(`/schedule/listByClassGroupId?classGroupId=${classGroupId}`);
  }
};

// ==================== 论坛相关 ====================
const forumApi = {
  getPosts(params = {}) {
    const { keyword, sort = 'newest', page = 0, size = 10 } = params;
    const queryStr = `sort=${sort}&page=${page}&size=${size}${keyword ? `&keyword=${encodeURIComponent(keyword)}` : ''}`;
    return request(`/forum/posts?${queryStr}`, { needAuth: false });
  },
  getPostDetail(id) {
    return request(`/forum/posts/${id}`, { needAuth: false });
  },
  createPost(data) {
    return request('/forum/posts', { method: 'POST', data });
  },
  vote(postId, voteType) {
    return request(`/forum/posts/${postId}/vote`, { method: 'POST', data: { voteType } });
  },
  addComment(postId, data) {
    return request(`/forum/posts/${postId}/comments`, { method: 'POST', data });
  }
};

// ==================== AI 助手（聊天机器人） ====================
const chatbotApi = {
  /**
   * 方案二：获取用户会话 Token（需登录）
   * 返回 { integrateId, requestDomain, sessionToken }
   */
  getSessionToken() {
    return request('/chatbot/session-token');
  },
  /**
   * 方案一：获取匿名配置（无需登录）
   * 返回 { integrateId, requestDomain }
   */
  getAnonymousConfig() {
    return request('/chatbot/anonymous-config', { needAuth: false });
  }
};

module.exports = {
  BASE_URL,
  request,
  studentApi,
  scheduleApi,
  forumApi,
  chatbotApi
};
