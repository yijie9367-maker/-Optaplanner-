/**
 * API工厂函数
 * 消除重复的CRUD API定义
 * 用于：student、teacher、course、classroom等资源的API层
 */

import request from './request.js'

/**
 * 创建通用的CRUD API
 * @param {string} endpoint - API端点，如 '/student', '/teacher'
 * @returns {Object} - 包含 list, add, update, delete 等方法的对象
 */
export function createCrudApi(endpoint) {
  return {
    // 获取列表
    getList: () => request.get(`${endpoint}/list`),

    // 新增
    add: (data) => request.post(`${endpoint}/add`, data),

    // 更新
    update: (data) => request.put(`${endpoint}/update`, data),

    // 删除
    delete: (id) => request.delete(`${endpoint}/delete/${id}`),

    // 获取详情（如果后端支持）
    getDetail: (id) => request.get(`${endpoint}/${id}`),

    // 批量删除
    batchDelete: (ids) => request.post(`${endpoint}/batchDelete`, { ids }),

    // 导入数据（通用接口）
    import: (file) => {
      const formData = new FormData()
      formData.append('file', file)
      return request.post(`${endpoint}/import`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    },

    // 导出数据（通用接口）
    export: () => request.get(`${endpoint}/export`, { responseType: 'blob' })
  }
}

/**
 * 创建带搜索的CRUD API
 */
export function createSearchableApi(endpoint) {
  const crud = createCrudApi(endpoint)
  return {
    ...crud,
    // 搜索
    search: (query) => request.get(`${endpoint}/search`, { params: query })
  }
}

/**
 * 创建带分页的CRUD API
 */
export function createPaginatedApi(endpoint) {
  const crud = createCrudApi(endpoint)
  return {
    ...crud,
    // 分页查询
    getPage: (page, pageSize, query = {}) =>
      request.get(`${endpoint}/page`, {
        params: { page, pageSize, ...query }
      })
  }
}

/**
 * 为现有API添加缓存支持
 */
export function withCache(apiFunc, cacheKey, ttl = 5 * 60 * 1000) {
  const cache = new Map()

  return async (...args) => {
    const now = Date.now()
    const cached = cache.get(cacheKey)

    if (cached && now - cached.timestamp < ttl) {
      return cached.data
    }

    const data = await apiFunc(...args)
    cache.set(cacheKey, { data, timestamp: now })
    return data
  }
}

/**
 * 为API添加重试机制
 */
export function withRetry(apiFunc, maxRetries = 3, delay = 1000) {
  return async (...args) => {
    let lastError
    for (let i = 0; i < maxRetries; i++) {
      try {
        return await apiFunc(...args)
      } catch (error) {
        lastError = error
        if (i < maxRetries - 1) {
          await new Promise(resolve => setTimeout(resolve, delay * Math.pow(2, i)))
        }
      }
    }
    throw lastError
  }
}
