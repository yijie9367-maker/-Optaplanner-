// components/ai-chat/ai-chat.js
Component({
  data: {
    show: false,
    messages: [],
    input: '',
    loading: false,
    scrollTop: 0,
    userInitial: '我'
  },

  lifetimes: {
    attached() {
      try {
        var userInfo = wx.getStorageSync('userInfo');
        if (userInfo && userInfo.name) this.setData({ userInitial: userInfo.name[0] });
      } catch(e) {}
      this.setData({
        messages: [{
          role: 'ai',
          text: '你好！👋 我是AI智能排课助手，有什么可以帮你的？'
        }]
      });
    }
  },

  methods: {
    open()  { this.setData({ show: true }); },
    close() { this.setData({ show: false }); },
    stop()  {},

    onInput(e) { this.setData({ input: e.detail.value }); },

    copyReply(e) {
      var text = e.currentTarget.dataset.text;
      wx.setClipboardData({
        data: text,
        success: function() {
          wx.showToast({ title: '已复制', icon: 'success', duration: 1500 });
        }
      });
    },

    send() {
      var text = this.data.input.trim();
      if (!text || this.data.loading) return;

      var msgs = this.data.messages.concat({ role: 'user', text: text });
      this.setData({ messages: msgs, input: '', loading: true, scrollTop: 99999 });

      var that = this;
      wx.request({
        url: this._baseUrl() + '/chatbot/chat',
        method: 'POST',
        header: { 'Content-Type': 'application/json' },
        data: { message: text },
        timeout: 180000,
        success: function(res) {
          var reply = (res.data && res.data.data && res.data.data.reply) || 'AI 无响应';
          that.setData({
            messages: that.data.messages.concat({ role: 'ai', text: reply }),
            loading: false,
            scrollTop: 99999
          });
        },
        fail: function(err) {
          var msg = '⚠️ 网络异常，请确认后端已启动';
          if (err && err.errMsg && err.errMsg.indexOf('timeout') !== -1) {
            msg = '⚠️ AI 响应超时，请稍后重试';
          }
          that.setData({
            messages: that.data.messages.concat({ role: 'ai', text: msg }),
            loading: false,
            scrollTop: 99999
          });
        }
      });
    },

    _baseUrl: function() {
      try {
        var app = getApp();
        if (app && app.globalData && app.globalData.baseUrl) return app.globalData.baseUrl;
      } catch(e) {}
      return 'http://10.17.86.214:8084';
    }

  }
});
