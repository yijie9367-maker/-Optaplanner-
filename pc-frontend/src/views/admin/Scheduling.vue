<template>
  <div class="scheduling-container">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span>智能排课系统</span>
          <div class="header-actions">
            <el-button type="success" @click="autoSchedule" :loading="loading.autoSchedule">
              一键智能排课
            </el-button>
            <el-button type="primary" plain @click="openConstraintDialog">
              约束设置
            </el-button>
            <el-button type="warning" @click="checkConflicts" :loading="loading.checkConflicts">
              检查冲突
            </el-button>
            <el-button type="info" @click="handleDownloadTemplate">下载模板</el-button>
            <el-upload :show-file-list="false" accept=".xlsx,.xls" :before-upload="handleImportSchedule" :auto-upload="true">
              <el-button type="warning" :loading="importing">导入课表</el-button>
            </el-upload>
            <el-button type="info" @click="handleExportSchedule" :loading="exporting">
              导出结果
            </el-button>
          </div>
        </div>
      </template>

      <div v-if="total > 0" class="stats-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="总课程数" :value="total" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="本页已安排" :value="scheduledCount" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="本页冲突数" :value="conflictedLessons.length">
              <template #suffix>
                <el-tag :type="conflictedLessons.length > 0 ? 'danger' : 'success'" size="small">
                  {{ conflictedLessons.length > 0 ? '有冲突' : '无冲突' }}
                </el-tag>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="当前页" :value="currentPage">
              <template #suffix>/ {{ totalPages }}</template>
            </el-statistic>
          </el-col>
        </el-row>
      </div>

      <div class="table-section">
        <div class="table-header">
          <h3>排课结果</h3>
          <el-input
            v-model="searchText"
            placeholder="搜索课程、教师或教室"
            style="width: 300px"
            clearable
            @input="onSearchInput"
            @clear="onSearchClear"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <el-table :data="lessonList" style="width: 100%" v-loading="loading.table" stripe border>
          <el-table-column type="index" label="序号" width="70" :index="indexMethod" />
          <el-table-column prop="courseCode" label="课程代码" width="120" />
          <el-table-column prop="courseName" label="课程名称" min-width="180" />
          <el-table-column prop="teacherName" label="授课教师" width="120" />
          <el-table-column prop="className" label="班级" width="120" />
          <el-table-column label="时间安排" width="180">
            <template #default="scope">
              <div v-if="scope.row.timeslot">
                {{ scope.row.timeslot.displayName }}
              </div>
              <div v-else class="unscheduled">
                <el-tag type="warning">未安排</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="教室" width="160">
            <template #default="scope">
              <div v-if="scope.row.room">
                {{ scope.row.room.building }} {{ scope.row.room.name }}
              </div>
              <div v-else class="unscheduled">
                <el-tag type="warning">未分配</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="studentCount" label="学生数" width="90" />
          <el-table-column label="状态" width="110">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row)" size="small">
                {{ getStatusText(scope.row) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="editSchedule(scope.row)" :disabled="!scope.row.timeslot">
                调整
              </el-button>
              <el-button size="small" type="danger" @click="removeSchedule(scope.row)">
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-bar">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 15, 20]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>

      <div v-if="conflictedLessons.length > 0" class="conflict-section">
        <el-alert title="检测到排课冲突" type="error" :closable="false" show-icon>
          <template #description>
            <div class="conflict-list">
              <div v-for="lesson in conflictedLessons" :key="lesson.id" class="conflict-item">
                <span class="conflict-course">{{ lesson.courseName }}</span>
                <span class="conflict-reason">{{ lesson.conflictReason || '存在冲突' }}</span>
              </div>
            </div>
          </template>
        </el-alert>
      </div>

      <el-dialog v-model="dialogVisible" title="手动调整排课" width="600px">
        <div v-if="selectedLesson">
          <el-form :model="adjustForm" label-width="100px">
            <el-form-item label="课程">
              <el-input :value="selectedLesson.courseName" disabled />
            </el-form-item>
            <el-form-item label="教师">
              <el-input :value="selectedLesson.teacherName" disabled />
            </el-form-item>
            <el-form-item label="班级">
              <el-input :value="selectedLesson.className" disabled />
            </el-form-item>
            <el-form-item label="时间槽">
              <el-select v-model="adjustForm.timeslotId" placeholder="选择时间" style="width: 100%">
                <el-option
                  v-for="timeslot in availableTimeslots"
                  :key="timeslot.id"
                  :label="timeslot.displayName"
                  :value="timeslot.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="教室">
              <el-select v-model="adjustForm.roomId" placeholder="选择教室" style="width: 100%">
                <el-option
                  v-for="room in suitableRooms"
                  :key="room.id"
                  :label="`${room.building} ${room.name}`"
                  :value="room.id"
                />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="saveAdjustment" :loading="loading.adjust">
              保存调整
            </el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>

  <!-- 约束设置弹窗 -->
  <el-dialog v-model="constraintDialogVisible" title="排课约束设置" width="700px" :close-on-click-modal="false">
    <div v-loading="constraintLoading">
      <el-alert type="info" :closable="false" style="margin-bottom: 16px">
        调整后点击保存，下次「一键智能排课」时生效。硬约束（冲突检测）不可关闭。
      </el-alert>

      <el-table :data="constraintList.filter(c => c.category === 'SOFT')" border size="small">
        <el-table-column prop="constraintName" label="约束名称" width="150" />
        <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
        <el-table-column label="启用" width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="markDirty(row)" />
          </template>
        </el-table-column>
        <el-table-column label="权重(1-100)" width="140" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.weight"
              :min="1"
              :max="100"
              :step="1"
              size="small"
              controls-position="right"
              style="width: 120px"
              :disabled="!row.enabled"
              @change="markDirty(row)"
            />
          </template>
        </el-table-column>
      </el-table>
    </div>
    <template #footer>
      <el-button @click="constraintDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="constraintSaving" @click="saveConstraints">
        保存设置
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onActivated, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import schedulingApi from '@/api/scheduling.js'
import { getSchedulePageList, importSchedules, exportSchedules, downloadScheduleTemplate } from '@/api/schedule'
import { downloadBlob } from '@/utils/download'
import { getConstraintConfigs, updateConstraintConfig } from '@/api/constraintConfig'

const timeTable = ref(null)
const lessonList = ref([])
const searchText = ref('')
const dialogVisible = ref(false)
const selectedLesson = ref(null)
const adjustForm = ref({
  timeslotId: null,
  roomId: null
})

const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const dataLoaded = ref(false)
const importing = ref(false)
const exporting = ref(false)

const handleDownloadTemplate = async () => {
  try {
    await ElMessageBox.confirm('确定要下载课表导入模板吗？下载后按照模板格式填写数据后再导入。', '下载模板', {
      confirmButtonText: '确定下载',
      cancelButtonText: '取消',
      type: 'info'
    })
  } catch {
    return
  }
  try { downloadBlob(await downloadScheduleTemplate(), '课表导入模板.xlsx') } catch { ElMessage.error('下载模板失败') }
}
const handleImportSchedule = async (file) => {
  importing.value = true
  try {
    const res = await importSchedules(file)
    const r = res.data
    r.failCount === 0 ? ElMessage.success(`导入成功 ${r.successCount} 条`) : ElMessage.warning(`成功 ${r.successCount} 条，失败 ${r.failCount} 条`)
    loadExistingSchedules()
  } catch { ElMessage.error('导入失败') } finally { importing.value = false }
  return false
}
const handleExportSchedule = async () => {
  try {
    await ElMessageBox.confirm('确定要导出当前课表列表吗？', '导出确认', {
      confirmButtonText: '确定导出', cancelButtonText: '取消', type: 'info'
    })
  } catch { return }
  exporting.value = true
  try { downloadBlob(await exportSchedules(), '课表列表.xlsx') } catch { ElMessage.error('导出失败') } finally { exporting.value = false }
}
let searchTimer = null

const loading = ref({
  autoSchedule: false,
  checkConflicts: false,
  table: false,
  adjust: false,
  save: false
})

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

const scheduledCount = computed(() =>
  lessonList.value.filter((lesson) => lesson.timeslot && lesson.room).length
)

const conflictedLessons = computed(() =>
  lessonList.value.filter((lesson) => lesson.hasConflict)
)

const availableTimeslots = computed(() => timeTable.value?.timeslotList || [])

const suitableRooms = computed(() => {
  if (!timeTable.value || !selectedLesson.value) return []
  return (timeTable.value.roomList || []).filter((room) => room.capacity >= selectedLesson.value.studentCount)
})

const indexMethod = (index) => (currentPage.value - 1) * pageSize.value + index + 1

const mapScheduleToLesson = (schedule, index, page) => ({
  id: `lesson-${page * pageSize.value + index}-${schedule.id || ''}`,
  scheduleId: schedule.id,
  semester: schedule.semester || '',
  weekNumber: schedule.weekNumber || null,
  courseId: schedule.courseId?.toString(),
  courseCode: schedule.courseCode || schedule.courseId?.toString() || '',
  courseName: schedule.courseName || '',
  teacherId: schedule.teacherId?.toString(),
  teacherName: schedule.teacherName || '',
  classGroupId: schedule.classGroupId?.toString(),
  className: schedule.className || '',
  studentCount: Number.parseInt(schedule.studentNum, 10) || 0,
  duration: schedule.duration || 2,
  weekDay: schedule.weekDay ? String(schedule.weekDay) : null,
  timeSlotIndex: schedule.timeSlot ? String(schedule.timeSlot) : null,
  building: schedule.building || '',
  roomName: schedule.roomName || '',
  roomId: schedule.classroomId?.toString(),
  timeslot: schedule.weekDay && schedule.timeSlot ? {
    id: `timeslot-${schedule.weekNumber || 'x'}-${schedule.weekDay}-${schedule.timeSlot}`,
    semester: schedule.semester || '',
    weekNumber: schedule.weekNumber || null,
    dayOfWeek: String(schedule.weekDay),
    displayName: formatScheduleDisplayTime(schedule)
  } : null,
  room: schedule.classroomId ? {
    id: schedule.classroomId.toString(),
    name: schedule.roomName || '',
    building: schedule.building || '',
    capacity: schedule.capacity || 0
  } : null,
  hasConflict: Boolean(schedule.isConflict),
  conflictReason: schedule.conflictReason || ''
})

const formatScheduleDisplayTime = (schedule) => {
  const weekNumber = Number(schedule.weekNumber)
  const weekDay = Number(schedule.weekDay)
  const timeSlot = Number(schedule.timeSlot)
  const dateText = getScheduleDateText(schedule.semester, weekNumber, weekDay)
  const dayLabelMap = {
    1: '周一',
    2: '周二',
    3: '周三',
    4: '周四',
    5: '周五',
    6: '周六',
    7: '周日'
  }
  const lessonLabelMap = {
    1: '第一节课',
    3: '第二节课',
    5: '第三节课',
    7: '第四节课',
    9: '第五节课'
  }
  const dayLabel = dayLabelMap[weekDay] || `周${schedule.weekDay}`
  const lessonLabel = lessonLabelMap[timeSlot] || `第${timeSlot}节课`
  const weekLabel = Number.isFinite(weekNumber) ? `第${weekNumber}周` : ''
  return [dateText, dayLabel, lessonLabel, weekLabel].filter(Boolean).join(' ')
}

const getScheduleDateText = (semester, weekNumber, weekDay) => {
  if (!semester || !Number.isFinite(weekNumber) || !Number.isFinite(weekDay)) {
    return ''
  }

  const [yearText, term] = semester.split('-')
  const year = Number(yearText)
  if (!Number.isFinite(year)) {
    return ''
  }

  const termStartMap = {
    spring: { month: 2, day: 2 },
    autumn: { month: 8, day: 1 }
  }
  const termStart = termStartMap[term]
  if (!termStart) {
    return ''
  }

  const firstMonday = getFirstMonday(year, termStart.month, termStart.day)
  const targetDate = new Date(firstMonday)
  targetDate.setDate(firstMonday.getDate() + (weekNumber - 1) * 7 + (weekDay - 1))

  const month = targetDate.getMonth() + 1
  const day = targetDate.getDate()
  return `${targetDate.getFullYear()}-${month}-${day}`
}

const getFirstMonday = (year, month, day) => {
  const date = new Date(year, month, day)
  while (date.getDay() !== 1) {
    date.setDate(date.getDate() + 1)
  }
  return date
}

const loadExistingSchedules = async (page = currentPage.value, keyword = searchText.value) => {
  loading.value.table = true
  try {
    const backendPage = page - 1
    const response = await getSchedulePageList(backendPage, pageSize.value, keyword)
    if (response.code === 200 && response.data) {
      const { total: totalCount = 0, records = [] } = response.data
      total.value = totalCount
      lessonList.value = records.map((schedule, index) => mapScheduleToLesson(schedule, index, backendPage))
      if (!timeTable.value) {
        timeTable.value = {
          lessonList: [],
          timeslotList: [],
          roomList: []
        }
      }
      timeTable.value.lessonList = lessonList.value
    }
  } catch (error) {
    console.error('加载排课数据失败:', error)
    ElMessage.error('加载排课数据失败')
  } finally {
    loading.value.table = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadExistingSchedules(page)
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadExistingSchedules(1)
}

const onSearchInput = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    loadExistingSchedules(1, searchText.value)
  }, 400)
}

const onSearchClear = () => {
  searchText.value = ''
  currentPage.value = 1
  loadExistingSchedules(1, '')
}

const getStatusType = (lesson) => {
  if (lesson.hasConflict) return 'danger'
  if (lesson.timeslot && lesson.room) return 'success'
  return 'warning'
}

const getStatusText = (lesson) => {
  if (lesson.hasConflict) return '冲突'
  if (lesson.timeslot && lesson.room) return '已安排'
  return '未安排'
}

const autoSchedule = async () => {
  loading.value.autoSchedule = true
  loading.value.table = true
  try {
    const response = await schedulingApi.autoSchedule()
    if (response.code === 200) {
      timeTable.value = response.data
      ElMessage.success('智能排课完成')
      await loadExistingSchedules(currentPage.value)
    } else {
      ElMessage.error(response.message || '排课失败')
    }
  } catch (error) {
    ElMessage.error(`请求失败: ${error.message}`)
  } finally {
    loading.value.autoSchedule = false
    loading.value.table = false
  }
}

const checkConflicts = async () => {
  if (!timeTable.value) {
    ElMessage.warning('请先生成排课数据')
    return
  }

  loading.value.checkConflicts = true
  try {
    const response = await schedulingApi.checkConflicts(timeTable.value)
    if (response.code === 200) {
      const conflictData = response.data || {}
      if (conflictData.conflictedLessons) {
        conflictData.conflictedLessons.forEach((conflictLesson) => {
          const lesson = lessonList.value.find((item) => item.id === conflictLesson.id)
          if (lesson) {
            lesson.hasConflict = true
            lesson.conflictReason = conflictLesson.conflictReason
          }
        })
      }
      ElMessage.success(`冲突检查完成，发现 ${conflictData.conflictCount || 0} 个冲突`)
    } else {
      ElMessage.error(response.message || '检查失败')
    }
  } catch (error) {
    ElMessage.error(`请求失败: ${error.message}`)
  } finally {
    loading.value.checkConflicts = false
  }
}

const exportSchedule = () => {
  handleExportSchedule()
}

const editSchedule = (lesson) => {
  selectedLesson.value = lesson
  adjustForm.value = {
    timeslotId: lesson.timeslot?.id || null,
    roomId: lesson.room?.id || null
  }
  dialogVisible.value = true
}

const removeSchedule = async (lesson) => {
  try {
    await ElMessageBox.confirm(
      `确定要移除课程“${lesson.courseName}”的排课安排吗？`,
      '确认移除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    lesson.timeslot = null
    lesson.room = null
    lesson.hasConflict = false
    lesson.conflictReason = null
    ElMessage.success('排课安排已移除')
  } catch {
  }
}

const saveAdjustment = async () => {
  if (!adjustForm.value.timeslotId || !adjustForm.value.roomId) {
    ElMessage.warning('请选择时间和教室')
    return
  }

  loading.value.adjust = true
  try {
    const timeslot = availableTimeslots.value.find((item) => item.id === adjustForm.value.timeslotId)
    const room = suitableRooms.value.find((item) => item.id === adjustForm.value.roomId)
    if (timeslot && room && selectedLesson.value) {
      selectedLesson.value.timeslot = timeslot
      selectedLesson.value.room = room
      await checkConflicts()
      ElMessage.success('排课调整已保存')
      dialogVisible.value = false
    }
  } catch (error) {
    ElMessage.error(`调整失败: ${error.message}`)
  } finally {
    loading.value.adjust = false
  }
}

onMounted(() => {
  if (!dataLoaded.value) {
    loadExistingSchedules(1)
    dataLoaded.value = true
  }
})

onActivated(() => {
  console.log('Scheduling 页面从缓存恢复')
})

onBeforeUnmount(() => {
  if (searchTimer) clearTimeout(searchTimer)
})

// ===== 约束配置弹窗 =====
const constraintDialogVisible = ref(false)
const constraintLoading = ref(false)
const constraintSaving = ref(false)
const constraintList = ref([])
const dirtyKeys = new Set()

function markDirty(row) {
  dirtyKeys.add(row.constraintKey)
}

async function openConstraintDialog() {
  constraintDialogVisible.value = true
  constraintLoading.value = true
  dirtyKeys.clear()
  try {
    const res = await getConstraintConfigs()
    if (res.code === 200 && res.data) {
      constraintList.value = res.data.map(c => ({ ...c }))
    }
  } catch {
    ElMessage.error('加载约束配置失败')
  } finally {
    constraintLoading.value = false
  }
}

async function saveConstraints() {
  constraintSaving.value = true
  try {
    const toSave = constraintList.value.filter(c => dirtyKeys.has(c.constraintKey))
    await Promise.all(toSave.map(c =>
      updateConstraintConfig(c.constraintKey, { enabled: c.enabled, weight: c.weight })
    ))
    dirtyKeys.clear()
    ElMessage.success('约束设置已保存，下次排课时生效')
    constraintDialogVisible.value = false
  } catch {
    ElMessage.error('保存失败，请重试')
  } finally {
    constraintSaving.value = false
  }
}
</script>

<style scoped>
.scheduling-container {
  padding: 20px;
}

.main-card {
  min-height: calc(100vh - 100px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.stats-section {
  margin-bottom: 30px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.table-section {
  margin-top: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-header h3 {
  margin: 0;
  color: #303133;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.conflict-section {
  margin-top: 20px;
}

.conflict-list {
  margin-top: 10px;
}

.conflict-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 6px 0;
  border-bottom: 1px solid #fde2e2;
}

.conflict-course {
  font-weight: 600;
  color: #f56c6c;
}

.conflict-reason {
  color: #606266;
  font-size: 13px;
}

.unscheduled {
  color: #909399;
}
</style>
