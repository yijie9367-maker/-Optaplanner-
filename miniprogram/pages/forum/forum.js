// forum.js - 论坛页面
let app;
try { app = getApp(); } catch (e) { app = null; }
function getAppSafe() { try { return getApp(); } catch (e) { return app; } }

Page({
  data: {
    keyword: '',
    sort: 'newest',
    page: 0,
    size: 10,
    posts: [],
    loading: false,
    hasMore: true,
    isLoggedIn: false
  },

  onLoad() {
    this.checkLogin();
    this.loadPosts();
  },

  onShow: function() {
    this.checkLogin();
    this.loadPosts(true);
  },

  // 检查登录状态
  checkLogin: function() {
    var token = null;
    var userInfo = null;
    try {
      token = wx.getStorageSync('token');
      userInfo = wx.getStorageSync('userInfo');
    } catch (e) {}
    this.setData({ isLoggedIn: !!(token && userInfo) });
  },

  // 要求登录
  requireLogin: function() {
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
  },

  // 加载帖子列表
  loadPosts: function(reset) {
    if (reset === undefined) reset = true;
    if (this.data.loading) return;
    if (!this.data.hasMore && !reset) return;

    var that = this;
    var page = reset ? 0 : this.data.page;
    this.setData({ loading: true });

    var params = { sort: this.data.sort, page: page, size: this.data.size };
    if (this.data.keyword) params.keyword = this.data.keyword;

    var queryStr = '';
    var keys = Object.keys(params);
    for (var i = 0; i < keys.length; i++) {
      var k = keys[i];
      if (i > 0) queryStr += '&';
      queryStr += k + '=' + encodeURIComponent(params[k]);
    }

    var appInstance = getAppSafe();
    if (appInstance && appInstance.request) {
      appInstance.request({
        url: '/forum/posts?' + queryStr,
        method: 'GET',
        needAuth: false
      }).then(function(res) {
        if (res.code === 200) {
          var newPosts = (res.data && res.data.content) || res.data || [];
          // 恢复本地点赞状态：后端 myVote === 'like' 表示已点赞
          var likedIds = that._getLikedIds();
          for (var i = 0; i < newPosts.length; i++) {
            if (newPosts[i].myVote === 'like') {
              newPosts[i].liked = true;
              // 同步到本地存储
              if (likedIds.indexOf(newPosts[i].id) < 0) likedIds.push(newPosts[i].id);
            } else if (likedIds.indexOf(newPosts[i].id) >= 0) {
              newPosts[i].liked = true;
            } else {
              newPosts[i].liked = false;
            }
          }
          wx.setStorageSync('likedPostIds', likedIds);
          var posts = reset ? newPosts : that.data.posts.concat(newPosts);
          that.setData({
            posts: posts,
            page: page + 1,
            hasMore: newPosts.length >= that.data.size
          });
        }
      }).catch(function() {
        // API 失败不再兜底演示数据
      }).finally(function() {
        that.setData({ loading: false });
        wx.stopPullDownRefresh();
      });
    } else {
      if (reset) this.loadDemoPosts();
      this.setData({ loading: false });
      wx.stopPullDownRefresh();
    }
  },

  // 演示数据
  loadDemoPosts: function() {
    var demoPosts = [
      { id: 1, title: '关于高等数学选课的建议', content: '请问各位学长学姐，张伟老师的高等数学怎么样？给分高吗？', authorName: '张三', createdAt: '2025-05-08 10:30', upvotes: 23, commentCount: 8, pinned: true },
      { id: 2, title: '数据结构实验报告模板分享', content: '分享一份我整理的实验报告模板，希望能帮到大家~', authorName: '李四', createdAt: '2025-05-07 15:20', upvotes: 45, commentCount: 12, pinned: false },
      { id: 3, title: '求组队参加程序设计竞赛', content: '有没有同学想一起组队参加下个月的程序设计竞赛？', authorName: '王五', createdAt: '2025-05-07 09:15', upvotes: 17, commentCount: 25, pinned: false },
      { id: 4, title: '图书馆自习座位好难抢', content: '最近期末了，图书馆一位难求，大家有没有好的自习地点推荐？', authorName: '赵六', createdAt: '2025-05-06 21:00', upvotes: 56, commentCount: 34, pinned: false },
      { id: 5, title: '线性代数考前重点整理', content: '根据周华老师上课划的重点整理了这份复习笔记，祝大家考试顺利！', authorName: '孙七', createdAt: '2025-05-06 16:45', upvotes: 89, commentCount: 15, pinned: false }
    ];
    // 按排序排列
    if (this.data.sort === 'hot') {
      demoPosts.sort(function(a, b) { return (b.upvotes || 0) - (a.upvotes || 0); });
    } else {
      demoPosts.sort(function(a, b) { return b.createdAt.localeCompare(a.createdAt); });
    }
    // 置顶始终最前
    demoPosts.sort(function(a, b) { return (b.pinned ? 1 : 0) - (a.pinned ? 1 : 0); });
    this.setData({ posts: demoPosts, hasMore: false });
  },

  // 搜索
  onSearchInput(e) { this.setData({ keyword: e.detail.value }); },
  onSearch() { this.loadPosts(true); },
  clearSearch() { this.setData({ keyword: '' }); this.loadPosts(true); },

  // 排序切换
  switchSort(e) { this.setData({ sort: e.currentTarget.dataset.sort }); this.loadPosts(true); },

  // 上拉加载更多
  onReachBottom() { this.loadPosts(false); },

  // 下拉刷新
  onPullDownRefresh: function() {
    this.loadPosts(true);
  },

  // 跳转帖子详情
  goDetail(e) {
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/forum-detail/forum-detail?id=' + id });
  },

  // 点赞
  toggleLike: function(e) {
    if (!this.data.isLoggedIn) { this.requireLogin(); return; }
    var index = e.currentTarget.dataset.index;
    var posts = this.data.posts;
    var post = posts[index];
    if (!post) return;
    
    var liked = !post.liked;
    // 先更新本地 UI
    posts[index].liked = liked;
    posts[index].likeCount = liked ? (post.likeCount || 0) + 1 : Math.max(0, (post.likeCount || 0) - 1);
    this.setData({ posts: posts });
    this._saveLiked(post.id, liked);

    // 调后端接口
    var that = this;
    var appInstance = getAppSafe();
    if (appInstance && appInstance.request) {
      var userInfo = null;
      try { userInfo = wx.getStorageSync('userInfo'); } catch (e) {}
      appInstance.request({
        url: '/forum/posts/' + post.id + '/vote',
        method: 'POST',
        data: {
          userId: userInfo ? userInfo.id : 1,
          userRole: 'student',
          voteType: liked ? 'upvote' : 'cancel'
        }
      }).then(function(res) {
        if (res && res.code === 200 && res.data) {
          // 以后端返回的点赞数为准
          var newPost = res.data;
          var allPosts = that.data.posts;
          allPosts[index].likeCount = newPost.likeCount || allPosts[index].likeCount;
          that.setData({ posts: allPosts });
        }
      }).catch(function() {});
    }
  },

  // 获取已点赞的帖子 ID 列表
  _getLikedIds: function() {
    try {
      var ids = wx.getStorageSync('likedPostIds');
      return ids || [];
    } catch (e) { return []; }
  },

  // 保存/移除点赞
  _saveLiked: function(postId, liked) {
    var ids = this._getLikedIds();
    var idx = ids.indexOf(postId);
    if (liked && idx < 0) {
      ids.push(postId);
    } else if (!liked && idx >= 0) {
      ids.splice(idx, 1);
    }
    wx.setStorageSync('likedPostIds', ids);
  },

  // 发帖 → 跳转到发帖页面
  goCreate() {
    if (!this.data.isLoggedIn) { this.requireLogin(); return; }
    wx.navigateTo({ url: '/pages/post-editor/post-editor' });
  },

  noop: function() {}
});
