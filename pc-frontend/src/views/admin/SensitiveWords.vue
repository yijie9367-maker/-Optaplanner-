<template>
  <div class="table-container">
    <el-card>
      <div class="card-header">
        <h3>违禁词管理</h3>
        <div class="header-actions">
          <el-button type="primary" @click="showAddDialog">添加违禁词</el-button>
          <el-button @click="loadWords">刷新</el-button>
        </div>
      </div>

      <!-- 提示 -->
      <el-alert type="info" :closable="false" style="margin-bottom:16px" show-icon>
        <template #title>
          违禁词库中的词汇将对论坛发帖和评论生效，包含任一违禁词的内容将被拦截。
        </template>
      </el-alert>

      <!-- 标签展示 -->
      <div v-if="words.length > 0" class="tag-cloud">
        <el-tag
          v-for="item in words"
          :key="item.id"
          closable
          size="large"
          type="danger"
          class="word-tag"
          @close="handleDelete(item)"
        >
          {{ item.word }}
        </el-tag>
      </div>
      <el-empty v-else description="暂无违禁词" />

      <!-- 分页信息 -->
      <div v-if="words.length > 0" class="footer-info">
        共 {{ words.length }} 个违禁词
      </div>
    </el-card>

    <!-- 添加对话框 -->
    <el-dialog v-model="dialogVisible" title="添加违禁词" width="500px" @close="clearInput">
      <div class="dialog-body">
        <el-input
          v-model="inputWords"
          type="textarea"
          :rows="4"
          placeholder="输入违禁词，多个词请用英文逗号或换行分隔"
        />
        <div class="dialog-tip">支持批量添加，系统会自动去重</div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd" :loading="adding">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { forumAPI } from '@/api/forum'

const words = ref([])
const dialogVisible = ref(false)
const inputWords = ref('')
const adding = ref(false)

const loadWords = async () => {
  try {
    const res = await forumAPI.getSensitiveWords()
    words.value = res.data || []
  } catch (e) {
    console.error('获取违禁词失败', e)
  }
}

const showAddDialog = () => {
  dialogVisible.value = true
}

const clearInput = () => {
  inputWords.value = ''
}

const handleAdd = async () => {
  const raw = inputWords.value.trim()
  if (!raw) {
    ElMessage.warning('请输入违禁词')
    return
  }
  // 支持逗号、中文逗号、换行分隔
  const wordList = raw
    .split(/[,，\n]+/)
    .map(w => w.trim())
    .filter(w => w.length > 0)

  adding.value = true
  try {
    const res = await forumAPI.addSensitiveWords(wordList)
    const added = res.data || []
    if (added.length > 0) {
      ElMessage.success(`成功添加 ${added.length} 个违禁词`)
      loadWords()
      dialogVisible.value = false
    } else {
      ElMessage.info('所有词汇已存在，未新增')
      dialogVisible.value = false
    }
  } catch (e) {
    console.error('添加失败', e)
  } finally {
    adding.value = false
  }
}

const handleDelete = (item) => {
  ElMessageBox.confirm(`确定删除违禁词「${item.word}」吗？`, '确认删除', {
    type: 'warning'
  })
    .then(async () => {
      try {
        await forumAPI.deleteSensitiveWord(item.id)
        ElMessage.success('已删除')
        loadWords()
      } catch (e) {
        console.error('删除失败', e)
      }
    })
    .catch(() => {})
}

onMounted(() => {
  loadWords()
})
</script>

<style scoped>
.table-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 12px 0;
}

.word-tag {
  font-size: 15px;
  padding: 8px 16px;
}

.footer-info {
  margin-top: 16px;
  color: #999;
  font-size: 13px;
}

.dialog-body {
  padding: 10px 0;
}

.dialog-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}
</style>
