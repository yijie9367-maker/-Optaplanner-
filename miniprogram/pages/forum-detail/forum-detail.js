// forum-detail.js - 帖子详情页
Page({
  data: {
    postId: null,
    post: null,
    comments: [],
    commentText: '',
    isLoggedIn: false
  },

  onLoad: function(options) {
    if (options.id) {
      this.setData({ postId: options.id });
      this.checkLogin();
      this.loadDetail();
    }
  },

  onShow: function() {
    this.checkLogin();
  },

  // 检查登录
  checkLogin: function() {
    var token = null;
    var userInfo = null;
    try {
      token = wx.getStorageSync('token');
      userInfo = wx.getStorageSync('userInfo');
    } catch (e) {}
    this.setData({ isLoggedIn: !!(token && userInfo) });
  },

  // 获取请求实例
  _getRequest: function() {
    try {
      var app = getApp();
      if (app && app.request) return app.request;
    } catch (e) {}
    return null;
  },

  loadDetail: function() {
    var that = this;
    var postId = this.data.postId;
    var request = this._getRequest();

    if (request) {
      request({
        url: '/forum/posts/' + postId,
        method: 'GET',
        needAuth: false
      }).then(function(res) {
        if (res.code === 200) {
          var post = res.data;
          that.setData({ post: post, comments: post.comments || [] });
        }
      }).catch(function() {
        that.loadDemoData();
      });
    } else {
      this.loadDemoData();
    }
  },

  loadDemoData: function() {
    this.setData({
      post: {
        id: this.data.postId,
        title: '关于高等数学选课的建议',
        content: '请问各位学长学姐，张伟老师的高等数学怎么样？给分高吗？',
        authorName: '张三',
        createdAt: '2025-05-08 10:30',
        upvotes: 23,
        pinned: true,
        userVoted: false
      },
      comments: [
        { id: 1, authorName: '李四', content: '张伟老师讲得很好，给分也比较公平，推荐选他的课！', createdAt: '2025-05-08 11:00' },
        { id: 2, authorName: '王五', content: '我去年上过，每节课都点名。参考书推荐同济版《高等数学》。', createdAt: '2025-05-08 11:30' }
      ]
    });
  },

  // 点赞（需登录）
  toggleVote: function() {
    if (!this.data.isLoggedIn) {
      this.showLoginTip();
      return;
    }
    var post = this.data.post;
    if (!post) return;
    var newVoted = !post.userVoted;
    this.setData({
      'post.userVoted': newVoted,
      'post.upvotes': newVoted ? post.upvotes + 1 : post.upvotes - 1
    });

    var request = this._getRequest();
    if (request) {
      request({
        url: '/forum/posts/' + this.data.postId + '/vote',
        method: 'POST',
        data: { voteType: newVoted ? 'upvote' : 'cancel' }
      });
    }
  },

  // 评论输入
  onCommentInput: function(e) {
    this.setData({ commentText: e.detail.value });
  },

  // 提交评论（需登录）
  submitComment: function() {
    if (!this.data.isLoggedIn) {
      this.showLoginTip();
      return;
    }
    var text = this.data.commentText.trim();
    if (!text) {
      wx.showToast({ title: '请输入评论内容', icon: 'none' });
      return;
    }

    var userInfo = null;
    try { userInfo = wx.getStorageSync('userInfo'); } catch (e) {}
    if (!userInfo) userInfo = { id: 1, name: '我', role: 'student' };

    var that = this;
    var request = this._getRequest();
    if (request) {
      request({
        url: '/forum/posts/' + this.data.postId + '/comments',
        method: 'POST',
        data: {
          authorId: userInfo.id,
          authorName: userInfo.name,
          authorRole: userInfo.role || 'student',
          content: text
        }
      }).then(function(res) {
        if (res.code === 200) {
          that.addCommentLocally(userInfo.name, text);
        }
      }).catch(function() {
        that.addCommentLocally(userInfo.name, text);
      });
    } else {
      this.addCommentLocally(userInfo.name, text);
    }
  },

  addCommentLocally: function(authorName, content) {
    var comments = this.data.comments;
    comments.push({
      id: Date.now(),
      authorName: authorName,
      content: content,
      createdAt: '刚刚'
    });
    this.setData({ comments: comments, commentText: '' });
    wx.showToast({ title: '评论成功', icon: 'success' });
  },

  // 显示登录提示
  showLoginTip: function() {
    wx.showModal({
      title: '请先登录',
      content: '登录后才能进行此操作',
      confirmText: '去登录',
      success: function(res) {
        if (res.confirm) {
          wx.switchTab({ url: '/pages/mine/mine' });
        }
      }
    });
  }
});
