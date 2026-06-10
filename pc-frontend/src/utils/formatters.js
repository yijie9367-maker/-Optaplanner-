/**
 * 统一的格式化工具库
 * 消除重复的格式化代码，集中管理所有格式化逻辑
 */

/**
 * 格式化日期时间
 * @param {string|Date} dateTime - 日期时间值
 * @returns {string} - 格式化后的日期时间字符串
 */
export const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  try {
    const date = new Date(dateTime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (error) {
    console.error('日期格式化失败:', error)
    return '-'
  }
}

/**
 * 格式化日期（只显示年月日）
 */
export const formatDate = (dateTime) => {
  if (!dateTime) return '-'
  try {
    const date = new Date(dateTime)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  } catch (error) {
    return '-'
  }
}

/**
 * 格式化时间（只显示时分秒）
 */
export const formatTime = (dateTime) => {
  if (!dateTime) return '-'
  try {
    const date = new Date(dateTime)
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (error) {
    return '-'
  }
}

