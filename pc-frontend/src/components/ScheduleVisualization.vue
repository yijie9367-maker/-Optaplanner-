<template>
  <div class="schedule-container">
    <div class="schedule-header">
      <h3>我的课表</h3>
      <div class="week-selector">
          <el-button @click="previousWeek" size="small" plain>上一周</el-button>
          <el-button @click="goToCurrentWeek" size="small" type="primary">本周</el-button>
          <el-button @click="nextWeek" size="small" plain>下一周</el-button>
        <span class="week-display">{{ weekDisplay }}</span>
      </div>
    </div>

    <!-- 课表主体 -->
    <div class="schedule-table">
      <!-- 时间列 -->
      <div class="time-column">
        <div class="header-cell"></div>
        <div v-for="time in timeSlots" :key="time" class="time-cell">
          <span class="time-text">{{ time }}</span>
        </div>
      </div>

      <!-- 各天的课程 -->
      <div v-for="(day, dayIndex) in weekDays" :key="dayIndex" class="day-column">
        <!-- 日期头 -->
        <div class="day-header" :class="{ today: isToday(dayIndex) }">
          <div class="day-name">{{ day.name }}</div>
          <div class="day-date">{{ day.date }}</div>
        </div>

        <!-- 课程格子 -->
        <div v-for="(timeSlot, slotIndex) in timeSlots" :key="slotIndex" class="day-cell">
          <!-- 该时间段的课程 -->
          <div
            v-if="getScheduleByDayAndTime(dayIndex, slotIndex)"
            :key="`schedule-${dayIndex}-${slotIndex}`"
            class="schedule-item"
            :style="getScheduleStyle(dayIndex, slotIndex)"
          >
            <div class="course-name">{{ getScheduleByDayAndTime(dayIndex, slotIndex)?.courseName }}</div>
            <div class="course-teacher">
              <template v-if="viewMode === 'teacher'">
                👥 {{ getScheduleByDayAndTime(dayIndex, slotIndex)?.className }}
              </template>
              <template v-else>
                👨‍🏫 {{ getScheduleByDayAndTime(dayIndex, slotIndex)?.teacherName }}
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>


  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// Props
const props = defineProps({
  schedules: {
    type: Array,
    default: () => []
  },
  viewMode: {
    type: String,
    default: 'student' // 'student' | 'teacher'
  }
})

// 状态
const currentDate = ref(new Date())
const timeSlots = [
  '08:00-09:40', '10:00-11:40', '14:00-15:40', '16:00-17:40', '19:00-20:40'
]

// 时间段索引映射（后端 time_slot 值 -> 前端 UI 索引）
// 后端存储的 time_slot 值：1,3,5,7,9
// 前端显示的索引：0,1,2,3,4
const timeSlotMap = {
  1: 0, // 08:00-09:40
  3: 1, // 10:00-11:40
  5: 2, // 14:00-15:40
  7: 3, // 16:00-17:40
  9: 4  // 19:00-20:40
}

// 获取当前周是教学周期中的第几周
// 教学周期：3月2日（周一） - 6月1日（第1-12周）
// 支持任意日期导航，超出范围时显示空白
const currentWeekNumber = computed(() => {
  const now = currentDate.value
  const year = now.getFullYear()
  
  // 教学周期开始：3月2日（周一）作为参考点
  const semesterStart = new Date(year, 2, 2) // 3月2日（周一）
  
  // 获取当前周的周一
  const monday = getMonday(now)
  
  // 计算周一距离教学开始日期的天数
  const diff = monday - semesterStart
  const oneDay = 24 * 60 * 60 * 1000
  const daysSinceStart = Math.floor(diff / oneDay)
  
  // 计算周数（从 3 月 2 日开始算第 1 周，允许负数或超过 12）
  const weekNumber = Math.floor(daysSinceStart / 7) + 1
  
  return weekNumber
})

// 获取周的显示信息
const weekDisplay = computed(() => {
  const monday = getMonday(currentDate.value)
  const sunday = new Date(monday)
  sunday.setDate(sunday.getDate() + 6)
  
  return `${monday.getMonth() + 1}月${monday.getDate()}日 - ${sunday.getMonth() + 1}月${sunday.getDate()}日`
})

// 获取周的各天
const weekDays = computed(() => {
  const monday = getMonday(currentDate.value)
  const days = []
  const dayNames = ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日']
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(monday)
    date.setDate(date.getDate() + i)
    days.push({
      name: dayNames[i],
      date: `${date.getMonth() + 1}/${date.getDate()}`,
      fullDate: date
    })
  }
  
  return days
})

// 计算周一（支持任意日期）
function getMonday(date) {
  const d = new Date(date) // 复制一份，避免修改原日期
  const day = d.getDay()
  const diff = d.getDate() - day + (day === 0 ? -6 : 1)
  d.setDate(diff)
  
  return d
}

// 检查是否是今天
function isToday(dayIndex) {
  const today = new Date()
  const cellDate = weekDays.value[dayIndex].fullDate
  return (
    today.getDate() === cellDate.getDate() &&
    today.getMonth() === cellDate.getMonth() &&
    today.getFullYear() === cellDate.getFullYear()
  )
}

// 获取指定天和时间段的课程
function getScheduleByDayAndTime(dayIndex, timeSlotIndex) {
  // dayIndex: 0-6 表示周一到周日
  // timeSlotIndex: 0-5 表示各个时间段
  
  // 将dayIndex（0-6）转换为weekDay（1-7）
  const weekDay = dayIndex + 1
  
  // 找到该时间段和那一天的课程，并根据周号过滤
  return props.schedules.find(schedule => {
    // 根据实际数据映射查找
    const scheduleDayOfWeek = parseInt(schedule.weekDay) || 0
    const scheduleTimeSlot = parseInt(schedule.timeSlot) || 0
    const scheduleWeekNumber = parseInt(schedule.weekNumber) || 1
    
    // 判断是否匹配（包括周号）
    return (
      scheduleDayOfWeek === weekDay && 
      timeSlotMap[scheduleTimeSlot] === timeSlotIndex &&
      scheduleWeekNumber === currentWeekNumber.value
    )
  })
}

// 获取课程样式
function getScheduleStyle(dayIndex, timeSlotIndex) {
  const schedule = getScheduleByDayAndTime(dayIndex, timeSlotIndex)
  if (!schedule) return {}
  
  return {
    position: 'absolute',
    top: '0',
    left: '0',
    right: '0',
    bottom: '0'
  }
}

// 切换周
function previousWeek() {
  currentDate.value = new Date(currentDate.value.setDate(currentDate.value.getDate() - 7))
}

function nextWeek() {
  currentDate.value = new Date(currentDate.value.setDate(currentDate.value.getDate() + 7))
}

function goToCurrentWeek() {
  currentDate.value = new Date()
}

onMounted(() => {
  // 初始化为当前周的周一
  currentDate.value = getMonday(new Date())
})
</script>

<style scoped>
.schedule-container {
  background: #ffffff;
  border-radius: 8px;
  padding: 12px 14px 10px;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

/* ==================== 头部 ==================== */
.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.schedule-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
  font-weight: 600;
}

.week-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.week-selector :deep(.el-button) {
  margin: 0;
}

.week-display {
  color: #666;
  font-size: 13px;
  min-width: 120px;
  font-weight: 500;
}

/* ==================== 课表主体 ==================== */
.schedule-table {
  display: flex;
  gap: 4px;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

/* 时间列 */
.time-column {
  display: flex;
  flex-direction: column;
  width: 62px;
  flex-shrink: 0;
}

.header-cell {
  height: 54px;
  flex-shrink: 0;
  background: #f5f7fa;
  border-radius: 4px 4px 0 0;
  border-bottom: 1px solid #e4e7ed;
}

.time-cell {
  flex: 1;
  min-height: 0;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: #666;
  font-weight: 500;
  text-align: center;
  box-sizing: border-box;
}

.time-text {
  line-height: 1.3;
  padding: 3px;
  font-size: 10px;
}

/* 各天的列 */
.day-column {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.day-header {
  height: 54px;
  background: #f5f7fa;
  color: #303133;
  border-radius: 4px 4px 0 0;
  border-bottom: 2px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.day-header.today {
  background: #ecf5ff;
  border-bottom-color: #1989fa;
  color: #1989fa;
}

.day-name {
  font-size: 12px;
  margin-bottom: 2px;
}

.day-date {
  font-size: 11px;
  color: #909399;
}

.day-header.today .day-date {
  color: #1989fa;
}

/* 日期格子 */
.day-cell {
  flex: 1;
  min-height: 0;
  border: 1px solid #e4e7ed;
  position: relative;
  background: white;
  transition: background 0.2s ease;
}

.day-cell:hover {
  background: #f9f9f9;
}

/* 课程项 */
.schedule-item {
  padding: 5px 7px;
  color: #1f2d3d;
  font-size: 13px;
  border-radius: 4px;
  cursor: pointer;
  background: #ecf5ff;
  border-left: 3px solid #1989fa;
  margin: 2px;
  overflow: hidden;
  height: calc(100% - 4px);
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.schedule-item:hover {
  background: #d9ecff;
}

.course-name {
  font-weight: 600;
  margin-bottom: 2px;
  white-space: normal;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.2;
  font-size: 13px;
  color: #1989fa;
  text-align: center;
}

.course-teacher {
  font-size: 12px;
  color: #606266;
  margin-bottom: 1px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
  width: 100%;
}

.course-location {
  font-size: 12px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
  width: 100%;
}




</style>
