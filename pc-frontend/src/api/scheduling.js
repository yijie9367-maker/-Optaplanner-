import request from '@/utils/request'

const schedulingApi = {
  // 执行智能排课
  solve(timeTable) {
    return request({
      url: '/scheduling/solve',
      method: 'post',
      data: timeTable
    })
  },

  // 一键智能排课
  autoSchedule() {
    return request({
      url: '/scheduling/auto-schedule',
      method: 'post'
    })
  },

  // 获取排课统计
  getStats(timeTable) {
    return request({
      url: '/scheduling/stats',
      method: 'post',
      data: timeTable
    })
  },

  // 检查排课冲突
  checkConflicts(timeTable) {
    return request({
      url: '/scheduling/check-conflicts',
      method: 'post',
      data: timeTable
    })
  },

  // 获取排课建议
  getSuggestions(params) {
    return request({
      url: '/scheduling/suggestions',
      method: 'post',
      data: params
    })
  },

  // 导出排课结果
  exportSchedule(timeTable) {
    return request({
      url: '/scheduling/export',
      method: 'post',
      data: timeTable,
      responseType: 'blob' // 对于文件下载
    })
  },

  // 手动调整排课
  manualAdjust(adjustment) {
    return request({
      url: '/scheduling/manual-adjust',
      method: 'post',
      data: adjustment
    })
  },

  // 获取排课进度
  getProgress(problemId) {
    return request({
      url: `/scheduling/progress/${problemId}`,
      method: 'get'
    })
  },

  // 取消排课求解
  cancelSolving(problemId) {
    return request({
      url: `/scheduling/cancel/${problemId}`,
      method: 'post'
    })
  },

  // 保存排课结果到数据库
  saveSchedule(timeTable) {
    return request({
      url: '/scheduling/save',
      method: 'post',
      data: timeTable
    })
  }
}

export default schedulingApi