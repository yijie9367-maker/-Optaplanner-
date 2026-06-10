// app.js
App({
  globalData: {
    // 后端 API 地址（本地）
    baseUrl: 'http://10.89.253.214:8084',
    // 用户登录信息
    token: '',
    userInfo: null,
    studentId: null
  },

  onLaunch() {
    // 尝试从本地存储恢复登录态
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (token) {
      this.globalData.token = token;
      this.globalData.userInfo = userInfo;
      this.globalData.studentId = userInfo ? userInfo.id : null;
    }
  },

  // 封装请求方法
  request(options) {
    const { url, method = 'GET', data, needAuth = true } = options;
    const header = { 'Content-Type': 'application/json' };
    if (needAuth && this.globalData.token) {
      header['Authorization'] = 'Bearer ' + this.globalData.token;
    }
    return new Promise((resolve, reject) => {
      wx.request({
        url: this.globalData.baseUrl + url,
        method,
        data,
        header,
        success(res) {
          if (res.statusCode === 200) {
            resolve(res.data);
          } else if (res.statusCode === 401) {
            wx.removeStorageSync('token');
            wx.removeStorageSync('userInfo');
            wx.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
            reject(res);
          } else {
            wx.showToast({ title: res.data?.message || '请求失败', icon: 'none' });
            reject(res);
          }
        },
        fail(err) {
          wx.showToast({ title: '网络错误', icon: 'none' });
          reject(err);
        }
      });
    });
  }
});
