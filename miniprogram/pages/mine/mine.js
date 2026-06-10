// mine.js - 我的页面
var app = null;
try { app = getApp(); } catch (e) {}

Page({
  data: {
    isLoggedIn: false,
    userInfo: {},
    loginName: '',
    loginPwd: ''
  },

  onLoad: function() { this.checkLogin(); },
  onShow: function() { this.checkLogin(); },

  _request: function(options) {
    var a = app || getApp();
    if (a && a.request) return a.request(options);
    return Promise.reject(new Error('no app'));
  },

  checkLogin: function() {
    var token = null, userInfo = null;
    try {
      token = wx.getStorageSync('token');
      userInfo = wx.getStorageSync('userInfo');
    } catch (e) {}
    if (userInfo && token) {
      this.setData({ isLoggedIn: true, userInfo: userInfo });
    } else {
      this.setData({ isLoggedIn: false, userInfo: {} });
    }
  },

  onLoginName: function(e) { this.setData({ loginName: e.detail.value }); },
  onLoginPwd: function(e) { this.setData({ loginPwd: e.detail.value }); },

  doLogin: function() {
    var loginName = this.data.loginName.trim();
    var loginPwd = this.data.loginPwd.trim();
    if (!loginName || !loginPwd) {
      wx.showToast({ title: '请输入姓名和密码', icon: 'none' });
      return;
    }
    var that = this;
    this._request({
      url: '/student/login', method: 'POST',
      data: { name: loginName, password: loginPwd }, needAuth: false
    }).then(function(res) {
      if (res && res.code === 200 && res.data) {
        var data = res.data;
        var token = data.token;
        delete data.token;
        that._saveLogin(token, data);
      } else {
        wx.showToast({ title: (res && res.message) || '登录失败', icon: 'none' });
      }
    }).catch(function() {
      wx.showToast({ title: '登录失败，请检查网络', icon: 'none' });
    });
  },

  _saveLogin: function(token, userInfo) {
    var a = app || getApp();
    if (a && a.globalData) {
      a.globalData.token = token;
      a.globalData.userInfo = userInfo;
      a.globalData.studentId = userInfo.id || null;
    }
    wx.setStorageSync('token', token);
    wx.setStorageSync('userInfo', userInfo);
    this.setData({ isLoggedIn: true, userInfo: userInfo, loginName: '', loginPwd: '' });
    wx.showToast({ title: '登录成功', icon: 'success' });
  },

  doLogout: function() {
    var that = this;
    wx.showModal({
      title: '确认退出', content: '退出后需要重新登录',
      success: function(res) {
        if (res.confirm) {
          var a = app || getApp();
          if (a && a.globalData) {
            a.globalData.token = '';
            a.globalData.userInfo = null;
            a.globalData.studentId = null;
          }
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          that.setData({ isLoggedIn: false, userInfo: {} });
        }
      }
    });
  }
});
