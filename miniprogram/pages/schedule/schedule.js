// schedule.js - 课表页面
var app = null;
try { app = getApp(); } catch (e) {}

Page({
  data: {
    currentWeek: 1,
    totalWeeks: 20,
    viewMode: 'week',
    weekDays: [],
    timeSlots: [],
    showModal: false,
    selectedCourse: {},
    isLoggedIn: false,
    showLoginTip: false
  },

  onLoad: function() {
    this.initWeekDays();
    this.checkLoginAndLoad();
  },

  onShow: function() {
    this.checkLoginAndLoad();
  },

  checkLoginAndLoad: function() {
    var studentInfo = this._getStudentInfo();
    var token = this._getToken();
    var isLoggedIn = !!(studentInfo && token);
    this.setData({ isLoggedIn: isLoggedIn });

    if (isLoggedIn) {
      this.loadSchedule();
    } else {
      this.loadEmptyGrid();
      if (!this._hasShownLoginTip) {
        this._hasShownLoginTip = true;
        this.setData({ showLoginTip: true });
      }
    }
  },

  _getToken: function() {
    try { return wx.getStorageSync('token') || null; }
    catch (e) { return null; }
  },

  _getStudentInfo: function() {
    try { return wx.getStorageSync('userInfo') || null; }
    catch (e) { return null; }
  },

  _request: function(options) {
    var a = app || getApp();
    if (a && a.request) return a.request(options);
    return Promise.reject(new Error('no app'));
  },

  initWeekDays: function() {
    var dayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
    var today = new Date();
    var currentDay = today.getDay();
    var monday = new Date(today);
    monday.setDate(today.getDate() - (currentDay === 0 ? 6 : currentDay - 1));
    
    var weekDays = [];
    for (var i = 0; i < 7; i++) {
      var date = new Date(monday);
      date.setDate(monday.getDate() + i);
      var dateStr = (date.getMonth() + 1) + '/' + date.getDate();
      weekDays.push({
        name: dayNames[i],
        date: dateStr,
        isToday: date.toDateString() === today.toDateString(),
        isWeekend: i >= 5,
        courses: []
      });
    }
    this.setData({ weekDays: weekDays });
  },

  loadSchedule: function() {
    var studentInfo = this._getStudentInfo();
    if (!studentInfo || !studentInfo.classGroupId) {
      this.loadEmptyGrid();
      return;
    }

    var that = this;
    this._request({
      url: '/schedule/listByClassGroupId?classGroupId=' + studentInfo.classGroupId,
      method: 'GET'
    }).then(function(res) {
      if (res && res.code === 200 && res.data && res.data.length > 0) {
        that.buildScheduleGrid(res.data);
      } else {
        that.loadEmptyGrid();
      }
    }).catch(function() {
      that.loadEmptyGrid();
      wx.showToast({ title: '课表加载失败', icon: 'none' });
    });
  },

  buildScheduleGrid: function(schedules) {
    if (!schedules || !schedules.length) {
      this.loadEmptyGrid();
      return;
    }

    var timeSlotConfig = [
      { bk: 1, id: 1, label: '1-2节', start: '08:00', end: '09:35' },
      { bk: 3, id: 2, label: '3-4节', start: '10:05', end: '11:40' },
      { bk: 5, id: 3, label: '5-6节', start: '13:30', end: '15:05' },
      { bk: 7, id: 4, label: '7-8节', start: '15:35', end: '17:10' },
      { bk: 9, id: 5, label: '9-10节', start: '18:30', end: '20:05' }
    ];

    var that = this;
    var currentWeek = this.data.currentWeek;
    var timeSlots = [];

    for (var si = 0; si < timeSlotConfig.length; si++) {
      var slot = timeSlotConfig[si];
      var cells = [];
      for (var day = 1; day <= 7; day++) {
        var found = null;
        for (var i = 0; i < schedules.length; i++) {
          if (schedules[i].weekDay === day &&
              schedules[i].timeSlot === slot.bk &&
              schedules[i].weekNumber === currentWeek) {
            found = schedules[i];
            break;
          }
        }
        if (found) {
          cells.push({
            dayIndex: day,
            hasCourse: true,
            course: {
              id: found.id,
              name: found.courseName || '课程',
              location: found.roomName || found.building || '待定',
              teacher: found.teacherName || '待定',
              credit: found.credit || 2,
              schedule: '第' + currentWeek + '周 周' + day + ' ' + slot.label
            }
          });
        } else {
          cells.push({ dayIndex: day, hasCourse: false, isEmpty: (day === 6 || day === 7), isWeekend: (day === 6 || day === 7) });
        }
      }
      timeSlots.push({ id: slot.id, label: slot.label, start: slot.start, end: slot.end, cells: cells });
    }

    this.setData({ timeSlots: timeSlots });
    this.buildListView(schedules);
  },

  buildListView: function(schedules) {
    var currentWeek = this.data.currentWeek;
    var weekDays = this.data.weekDays;
    var newWeekDays = [];
    for (var i = 0; i < weekDays.length; i++) {
      var day = weekDays[i];
      var dayIndex = i + 1;
      var courses = [];
      for (var j = 0; j < schedules.length; j++) {
        if (schedules[j].weekDay === dayIndex && schedules[j].weekNumber === currentWeek) {
          courses.push({
            id: schedules[j].id,
            name: schedules[j].courseName || '课程',
            location: schedules[j].roomName || schedules[j].building || '待定',
            teacher: schedules[j].teacherName || '待定',
            timeSlotLabel: this.getTimeSlotLabel(schedules[j].timeSlot)
          });
        }
      }
      newWeekDays.push({ name: day.name, date: day.date, isToday: day.isToday, courses: courses });
    }
    this.setData({ weekDays: newWeekDays });
  },

  getTimeSlotLabel: function(slot) {
    var labels = { 1: '1-2节 08:00-09:35', 3: '3-4节 10:05-11:40', 5: '5-6节 13:30-15:05', 7: '7-8节 15:35-17:10', 9: '9-10节 18:30-20:05' };
    return labels[slot] || '';
  },

  loadEmptyGrid: function() {
    var config = [
      { id: 1, label: '1-2节', start: '08:00', end: '09:35' },
      { id: 2, label: '3-4节', start: '10:05', end: '11:40' },
      { id: 3, label: '5-6节', start: '13:30', end: '15:05' },
      { id: 4, label: '7-8节', start: '15:35', end: '17:10' },
      { id: 5, label: '9-10节', start: '18:30', end: '20:05' }
    ];
    var timeSlots = [];
    for (var si = 0; si < config.length; si++) {
      var cells = [];
      for (var d = 1; d <= 7; d++) {
        cells.push({ dayIndex: d, hasCourse: false, isEmpty: (d === 6 || d === 7) });
      }
      timeSlots.push({ id: config[si].id, label: config[si].label, start: config[si].start, end: config[si].end, cells: cells });
    }
    this.setData({ timeSlots: timeSlots });
    var wd = this.data.weekDays;
    var ew = [];
    for (var i = 0; i < wd.length; i++) {
      ew.push({ name: wd[i].name, date: wd[i].date, isToday: wd[i].isToday, courses: [] });
    }
    this.setData({ weekDays: ew });
  },

  prevWeek: function() {
    if (this.data.currentWeek > 1) {
      this.setData({ currentWeek: this.data.currentWeek - 1 });
      this.loadSchedule();
    }
  },
  nextWeek: function() {
    if (this.data.currentWeek < this.data.totalWeeks) {
      this.setData({ currentWeek: this.data.currentWeek + 1 });
      this.loadSchedule();
    }
  },

  switchView: function(e) { this.setData({ viewMode: e.currentTarget.dataset.mode }); },

  showCourseDetail: function(e) {
    var c = e.currentTarget.dataset.course;
    if (c) this.setData({ selectedCourse: c, showModal: true });
  },
  closeModal: function() { this.setData({ showModal: false }); },
  noop: function() {},
  closeLoginTip: function() { this.setData({ showLoginTip: false }); },
  goToLogin: function() {
    this.setData({ showLoginTip: false });
    wx.switchTab({ url: '/pages/mine/mine' });
  },

  goExam: function() {
    wx.navigateTo({ url: '/pages/exam/exam' });
  }
});
