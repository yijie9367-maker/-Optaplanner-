// post-editor.js - 发布帖子
var app = null;
try { app = getApp(); } catch (e) {}

Page({
  data: {
    title: '',
    content: ''
  },

  onLoad: function() {
    // 恢复草稿
    var draft = wx.getStorageSync('postDraft');
    if (draft && (draft.title || draft.content)) {
      this.setData({ title: draft.title || '', content: draft.content || '' });
    }
  },

  onTitle: function(e) { this.setData({ title: e.detail.value }); },
  onContent: function(e) { this.setData({ content: e.detail.value }); },

  submit: function() {
    var title = this.data.title.trim();
    var content = this.data.content.trim();
    if (!title) { wx.showToast({ title: '请输入标题', icon: 'none' }); return; }
    if (!content) { wx.showToast({ title: '请输入正文', icon: 'none' }); return; }

    var userInfo = null;
    try { userInfo = wx.getStorageSync('userInfo'); } catch (e) {}
    if (!userInfo) { userInfo = { id: 1, name: '我', role: 'student' }; }

    var that = this;
    var a = app || getApp();

    if (a && a.request) {
      a.request({
        url: '/forum/posts', method: 'POST',
        data: {
          title: title, content: content,
          authorId: userInfo.id, authorName: userInfo.name, authorRole: userInfo.role || 'student'
        }
      }).then(function(res) {
        if (res && res.code === 200) {
          wx.removeStorageSync('postDraft');
          wx.showToast({ title: '发布成功', icon: 'success' });
          that.close();
        } else {
          wx.showToast({ title: '发布失败', icon: 'none' });
        }
      }).catch(function() {
        wx.showToast({ title: '发布失败', icon: 'none' });
      });
    } else {
      // 演示模式：返回上一页并刷新
      wx.showToast({ title: '发布成功（演示模式）', icon: 'success' });
      that.close();
    }
  },

  close: function() {
    wx.navigateBack({ delta: 1 });
  },

  cancel: function() {
    var hasContent = this.data.title.trim() || this.data.content.trim();
    var that = this;
    if (hasContent) {
      wx.showActionSheet({
        itemList: ['保存草稿', '放弃编辑'],
        success: function(res) {
          if (res.tapIndex === 0) {
            wx.setStorageSync('postDraft', {
              title: that.data.title,
              content: that.data.content
            });
            wx.showToast({ title: '已保存草稿', icon: 'success' });
            setTimeout(function() { that.close(); }, 800);
          } else {
            wx.removeStorageSync('postDraft');
            that.close();
          }
        }
      });
    } else {
      this.close();
    }
  }
});
