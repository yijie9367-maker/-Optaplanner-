// exam.js - 考试安排页面
Page({
  data: {
    allExams: [],
    todayExams: [],
    upcomingExams: [],
    pastExams: [],
    upcomingGrouped: [],
    todayDate: ''
  },

  onLoad: function() {
    this.loadExams();
  },

  onShow: function() {
    this.loadExams();
  },

  loadExams: function() {
    var userInfo = null;
    try { userInfo = wx.getStorageSync('userInfo'); } catch (e) {}

    if (userInfo && userInfo.classGroupId) {
      this._loadFromApi(userInfo.classGroupId);
    } else {
      this._loadDemo();
    }
  },

  _loadFromApi: function(classGroupId) {
    var that = this;
    var app = null;
    try { app = getApp(); } catch (e) {}

    if (app && app.request) {
      app.request({
        url: '/exam/listByClassGroupId?classGroupId=' + classGroupId,
        method: 'GET'
      }).then(function(res) {
        if (res && res.code === 200 && res.data && res.data.length > 0) {
          that.processExams(res.data);
        } else {
          // 后端无数据时用演示数据
          that._loadDemo();
        }
      }).catch(function() {
        that._loadDemo();
      });
    } else {
      this._loadDemo();
    }
  },

  _loadDemo: function() {
    var today = new Date();
    var y = today.getFullYear();
    var m = today.getMonth() + 1;
    var d = today.getDate();

    var formatDate = function(offset) {
      var dt = new Date(y, m - 1, d + offset);
      return dt.getFullYear() + '-' +
        String(dt.getMonth() + 1).padStart(2, '0') + '-' +
        String(dt.getDate()).padStart(2, '0');
    };

    var exams = [
      { id: 1, courseName: '高级人工智能导论', location: '理科楼 B座 402 机房', startTime: '08:30', endTime: '10:30', teacherName: '张晓明 教授', className: '计科2201班', examDate: formatDate(0), daysLeft: 0, examType: '期末考试' },
      { id: 2, courseName: '概率论与数理统计', location: '一教 A101 多媒体教室', startTime: '14:00', endTime: '16:00', teacherName: '李芳 讲师', className: '计科2201班', examDate: formatDate(3), daysLeft: 3, examType: '' },
      { id: 3, courseName: '大学英语 (IV)', location: '外语楼 302 语音室', startTime: '09:00', endTime: '11:00', teacherName: 'Emily Chen', className: '计科2201班', examDate: formatDate(5), daysLeft: 5, examType: '机考' },
      { id: 4, courseName: '数据库系统概论', location: '理科楼 A座 201', startTime: '14:00', endTime: '16:00', teacherName: '王刚 副教授', className: '计科2201班', examDate: formatDate(-5), daysLeft: -5, examType: '' }
    ];
    this.processExams(exams);
  },

  processExams: function(exams) {
    var today = new Date();
    var todayStr = today.getFullYear() + '-' +
      String(today.getMonth() + 1).padStart(2, '0') + '-' +
      String(today.getDate()).padStart(2, '0');

    var todayExams = [];
    var upcomingExams = [];
    var pastExams = [];

    for (var i = 0; i < exams.length; i++) {
      var exam = exams[i];
      if (exam.examDate === todayStr) {
        todayExams.push(exam);
      } else if (exam.examDate > todayStr) {
        upcomingExams.push(exam);
      } else {
        pastExams.push(exam);
      }
    }

    // 未来考试按日期分组
    upcomingExams.sort(function(a, b) { return a.examDate.localeCompare(b.examDate); });
    var grouped = [];
    var currentDate = '';
    for (var j = 0; j < upcomingExams.length; j++) {
      var ex = upcomingExams[j];
      if (ex.examDate !== currentDate) {
        currentDate = ex.examDate;
        grouped.push({ date: ex.examDate, exams: [ex] });
      } else {
        grouped[grouped.length - 1].exams.push(ex);
      }
    }

    // 格式化今日日期
    var m = today.getMonth() + 1;
    var d = today.getDate();
    var weekDays = ['日', '一', '二', '三', '四', '五', '六'];
    var todayDisplay = m + '月' + d + '日 周' + weekDays[today.getDay()];

    this.setData({
      allExams: exams,
      todayExams: todayExams,
      upcomingExams: upcomingExams,
      pastExams: pastExams,
      upcomingGrouped: grouped,
      todayDate: todayDisplay
    });
  }
});
