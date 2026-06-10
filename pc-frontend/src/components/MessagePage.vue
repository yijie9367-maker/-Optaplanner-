<template>
  <div class="messages-container" :style="{ '--message-border-color': borderColor }">
    <!-- 顶部操作栏 -->
    <div class="header-bar">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="全部" name="all"></el-tab-pane>
        <el-tab-pane label="公告" name="announcement"></el-tab-pane>
        <el-tab-pane label="通知" name="notice"></el-tab-pane>
        <el-tab-pane label="活动" name="event"></el-tab-pane>
      </el-tabs>
      <el-button
        v-if="canEdit"
        type="primary"
        @click="openDialog('add')"
        class="add-btn"
      >
        + 发布消息
      </el-button>
    </div>

    <!-- 消息列表 -->
    <div class="messages-list">
      <div v-if="messages.length === 0" class="empty-state">
        <el-empty description="暂无消息"></el-empty>
      </div>

      <div
        v-for="message in messages"
        :key="message.id"
        class="message-card"
        @click="viewMessage(message)"
      >
        <div class="message-badge" :class="`badge-${message.type}`">
          {{ getTypeLabel(message.type) }}
        </div>

        <div class="message-content">
          <h3 class="message-title">{{ message.title }}</h3>
          <p class="message-excerpt">{{ truncateContent(message.content, 100) }}</p>

          <div class="message-stats">
            <span class="stat-item">
              <el-icon><View /></el-icon>
              {{ message.views }}
            </span>
            <span class="stat-item">
              <el-icon><Like /></el-icon>
              {{ message.likes }}
            </span>
            <span class="stat-item">
              <span>{{ formatDate(message.publishTime) }}</span>
            </span>
          </div>
        </div>

        <div v-if="canEdit" class="message-actions">
          <el-button type="primary" plain size="small" @click.stop="viewMessage(message)">查看</el-button>
          <el-button type="default" plain size="small" @click.stop="openDialog('edit', message)">编辑</el-button>
          <el-button type="danger" plain size="small" @click.stop="deleteMessage(message.id)">删除</el-button>
        </div>
      </div>
    </div>

    <!-- 删除确认对话框 -->
    <el-dialog v-model="deleteDialogVisible" title="确认删除" width="400px">
      <span>确定要删除该消息吗？</span>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDelete">删除</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑消息对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? `发布${dialogLabel}` : `编辑${dialogLabel}`"
      width="700px"
      @close="closeDialog"
    >
      <el-form :model="formData" ref="formRef" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入消息标题" maxlength="200" show-word-limit></el-input>
        </el-form-item>

        <el-form-item label="类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择消息类型">
            <el-option label="公告" value="announcement"></el-option>
            <el-option label="通知" value="notice"></el-option>
            <el-option label="活动" value="event"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            placeholder="请输入消息内容"
            :rows="8"
            maxlength="5000"
            show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态">
            <el-option label="已发布" value="published"></el-option>
            <el-option label="草稿" value="draft"></el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMessage">
          {{ dialogMode === 'add' ? '发布' : '保存' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 消息详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="detailData ? detailData.title : '消息详情'"
      width="800px"
      @close="detailData = null"
    >
      <div v-if="detailData" class="message-detail">
        <div class="detail-header">
          <span class="detail-badge" :class="`badge-${detailData.type}`">
            {{ getTypeLabel(detailData.type) }}
          </span>
          <span class="detail-meta">
            <span>{{ detailData.publisherName }}</span>
            <span class="divider">·</span>
            <span>{{ formatDate(detailData.publishTime) }}</span>
          </span>
        </div>

        <div class="detail-title">{{ detailData.title }}</div>
        <div class="detail-content">{{ detailData.content }}</div>

        <div class="detail-footer">
          <div class="detail-stats">
            <span class="stat">浏览: <strong>{{ detailData.views }}</strong></span>
            <span class="stat">点赞: <strong>{{ detailData.likes }}</strong></span>
          </div>
          <el-button type="success" @click="likeMessage(detailData.id)">
            <el-icon><Like /></el-icon>
            点赞
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { messageAPI } from '@/api/message'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const props = defineProps({
  target: { type: String, required: true },
  dialogLabel: { type: String, default: '消息' },
  requireSuperAdmin: { type: Boolean, default: false },
  borderColor: { type: String, default: '#409eff' }
})

const userStore = useUserStore()

const canEdit = computed(() => {
  if (!props.requireSuperAdmin) return true
  return userStore.user?.username === 'admin'
})

const activeTab = ref('all')
const allMessages = ref([])
const messages = computed(() => {
  if (activeTab.value === 'all') return allMessages.value
  return allMessages.value.filter(m => m.type === activeTab.value)
})

const dialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const dialogMode = ref('add')
const formRef = ref(null)
const messageToEdit = ref(null)
const messageToDelete = ref(null)
const detailData = ref(null)

const formData = reactive({
  title: '',
  content: '',
  type: 'announcement',
  status: 'published'
})

const rules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  content: [{ required: true, message: '内容不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '类型不能为空', trigger: 'change' }]
}

const getTypeLabel = (type) => {
  const map = { announcement: '公告', notice: '通知', event: '活动' }
  return map[type] || type
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const truncateContent = (content, length) => {
  if (!content) return ''
  return content.length > length ? content.substring(0, length) + '...' : content
}

const loadMessages = async () => {
  try {
    const res = await messageAPI.getAdminMessagesByTarget(props.target)
    allMessages.value = res.data || []
  } catch (error) {
    ElMessage.error('加载消息失败')
  }
}

const onTabChange = () => {}

const openDialog = (mode, message = null) => {
  dialogMode.value = mode
  if (mode === 'add') {
    formData.title = ''
    formData.content = ''
    formData.type = 'announcement'
    formData.status = 'published'
    messageToEdit.value = null
  } else if (mode === 'edit' && message) {
    formData.title = message.title
    formData.content = message.content
    formData.type = message.type
    formData.status = message.status
    messageToEdit.value = message
  }
  dialogVisible.value = true
}

const closeDialog = () => {
  dialogVisible.value = false
  formRef.value && formRef.value.resetFields()
}

const saveMessage = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  try {
    const data = {
      title: formData.title,
      content: formData.content,
      type: formData.type,
      status: formData.status,
      target: props.target,
      publisherName: userStore.user?.username || 'admin'
    }
    if (dialogMode.value === 'add') {
      await messageAPI.addMessage(data)
      ElMessage.success('消息发布成功')
    } else {
      data.id = messageToEdit.value.id
      await messageAPI.updateMessage(data)
      ElMessage.success('消息更新成功')
    }
    closeDialog()
    loadMessages()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const deleteMessage = (id) => {
  messageToDelete.value = id
  deleteDialogVisible.value = true
}

const confirmDelete = async () => {
  try {
    await messageAPI.deleteMessage(messageToDelete.value)
    ElMessage.success('删除成功')
    deleteDialogVisible.value = false
    loadMessages()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const viewMessage = async (message) => {
  try {
    const res = await messageAPI.getMessageDetail(message.id)
    detailData.value = res.data
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取消息详情失败')
  }
}

const likeMessage = async (id) => {
  try {
    await messageAPI.likeMessage(id)
    if (detailData.value && detailData.value.id === id) {
      detailData.value.likes += 1
    }
    ElMessage.success('点赞成功')
  } catch (error) {
    ElMessage.error('点赞失败')
  }
}

onMounted(() => {
  loadMessages()
})
</script>

<style src="@/styles/messageStyles.css"></style>
<style scoped>
</style>
