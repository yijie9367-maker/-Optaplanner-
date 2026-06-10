<template>
  <div class="login-wrapper">
    <!-- 左侧面板：固定不变 -->
    <div class="left-panel">
      <div class="logo-section">
        <div class="logo">🎓</div>
        <h1>教育教务系统</h1>
        <p>学生/老师统一服务平台</p>
        <a href="/video" class="video-entry">🎬 影视中心</a>
      </div>
      <div class="decoration">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>
    </div>

    <!-- 右侧面板：tab 切换学生/教师 -->
    <div class="right-panel">
      <div class="form-container">

        <!-- Tab 切换 -->
        <div class="role-switch">
          <span class="role-tab" :class="{ active: activeRole === 'student' }" @click="switchRole('student')">学生登录</span>
          <span class="role-tab" :class="{ active: activeRole === 'teacher' }" @click="switchRole('teacher')">教师登录</span>
        </div>

        <h2 class="form-title">欢迎登录教务系统</h2>

        <el-form
          ref="loginFormRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <!-- 姓名 -->
          <el-form-item prop="name">
            <el-input
              v-model="form.name"
              :placeholder="activeRole === 'student' ? '请输入学生姓名' : '请输入教师姓名'"
              clearable
              @keyup.enter="handleLogin"
            >
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>

          <!-- 密码 -->
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
              @keyup.enter="handleLogin"
            >
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>

          <!-- 验证码 -->
          <el-form-item prop="captcha">
            <el-row :gutter="10" style="width: 100%">
              <el-col :span="14">
                <el-input v-model="form.captcha" placeholder="请输入验证码" clearable>
                  <template #prefix><el-icon><Picture /></el-icon></template>
                </el-input>
              </el-col>
              <el-col :span="10">
                <div class="captcha-box" @click="refreshCaptcha">
                  <span class="captcha-text">{{ captchaCode }}</span>
                </div>
              </el-col>
            </el-row>
          </el-form-item>

          <!-- 登录按钮 -->
          <el-form-item>
            <el-button type="primary" class="login-button" size="large" :loading="loading" @click="handleLogin">
              立即登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-tips">
          <p v-if="activeRole === 'student'">提示：使用数据库中的学生姓名登录（默认密码：123456）</p>
          <p v-else>提示：使用数据库中的教师姓名登录（默认密码：123456）</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { User, Lock, Picture } from '@element-plus/icons-vue'
import { buildApiUrl } from '@/utils/api'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)
const activeRole = ref('student') // 'student' | 'teacher'

const form = reactive({ name: '', password: '', captcha: '' })
const captchaCode = ref('')
const actualCaptcha = ref('')

const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码长度为 4 个字符', trigger: 'blur' }
  ]
}

function refreshCaptcha() {
  const chars = 'abcdefghijklmnopqrstuvwxyz0123456789'
  let code = ''
  for (let i = 0; i < 4; i++) code += chars[Math.floor(Math.random() * chars.length)]
  actualCaptcha.value = code
  captchaCode.value = code
}

function switchRole(role) {
  activeRole.value = role
  form.name = ''
  form.password = ''
  form.captcha = ''
  refreshCaptcha()
  loginFormRef.value?.clearValidate()
}

async function handleLogin() {
  if (form.captcha !== '1111' && form.captcha.toLowerCase() !== actualCaptcha.value.toLowerCase()) {
    ElMessage.error('验证码错误')
    form.captcha = ''
    refreshCaptcha()
    return
  }

  try {
    await loginFormRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const url = activeRole.value === 'student'
      ? buildApiUrl('/login')
      : buildApiUrl('/teacher/login')

    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: form.name, password: form.password })
    })
    const data = await response.json()

    if (data.code === 200 && data.data) {
      if (activeRole.value === 'student') {
        userStore.setUser({
          id: data.data.id,
          name: data.data.name,
          email: data.data.email,
          major: data.data.major,
          classGroupId: data.data.classGroupId,
          className: data.data.className,
          isMuted: Boolean(data.data.isMuted),
          role: 'student',
          token: data.data.token
        })
        localStorage.setItem('token', data.data.token)
        ElMessage.success('登录成功')
        router.push('/student-dashboard')
      } else {
        userStore.setUser({
          id: data.data.id,
          name: data.data.name,
          title: data.data.title,
          department: data.data.department,
          isMuted: Boolean(data.data.isMuted),
          role: 'teacher',
          token: data.data.token
        })
        localStorage.setItem('token', data.data.token)
        ElMessage.success('登录成功')
        router.push('/teacher-dashboard')
      }
    } else {
      ElMessage.error(data.message || '登录失败，请检查姓名或密码')
    }
  } catch (error) {
    ElMessage.error('登录失败，请检查服务器是否在线')
    console.error(error)
  } finally {
    loading.value = false
  }
}

refreshCaptcha()
</script>

<style scoped>
.login-wrapper {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* ===== 左侧 ===== */
.left-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  position: relative;
  background: linear-gradient(135deg, rgb(103, 144, 190) 0%, #5a7fa3 100%);
}

.logo-section {
  text-align: center;
  z-index: 10;
}

.logo {
  font-size: 80px;
  margin-bottom: 20px;
  animation: float 3s ease-in-out infinite;
}

.logo-section h1 {
  font-size: 36px;
  margin: 0 0 10px;
  font-weight: bold;
  letter-spacing: 2px;
}

.logo-section p {
  font-size: 16px;
  opacity: 0.8;
  margin: 0;
}

.video-entry {
  display: inline-block;
  margin-top: 16px;
  padding: 8px 20px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 8px;
  color: #fff;
  text-decoration: none;
  font-size: 15px;
  transition: all 0.2s;
}
.video-entry:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: #fff;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-16px); }
}

.decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.shape {
  position: absolute;
  opacity: 0.1;
  border-radius: 50%;
  background: white;
}

.shape-1 { width: 300px; height: 300px; top: -50px; left: -50px; }
.shape-2 { width: 200px; height: 200px; bottom: 100px; right: -50px; }
.shape-3 { width: 150px; height: 150px; top: 50%; left: 50%; }

/* ===== 右侧 ===== */
.right-panel {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
}

.form-container {
  width: 100%;
  max-width: 420px;
  padding: 40px 44px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

/* ===== Tab 切换 ===== */
.role-switch {
  display: flex;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 24px;
}

.role-tab {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 14px;
  color: #909399;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: color 0.2s, border-color 0.2s;
}

.role-tab:hover { color: #1989fa; }

.role-tab.active {
  color: #1989fa;
  border-bottom-color: #1989fa;
  font-weight: 600;
}

/* ===== 标题 ===== */
.form-title {
  text-align: center;
  font-size: 22px;
  color: #1f2d3d;
  margin: 0 0 24px;
  font-weight: 700;
}

/* ===== 表单 ===== */
.login-form { margin-top: 4px; }

:deep(.el-form-item) { margin-bottom: 20px; }

:deep(.el-input__wrapper) {
  border-radius: 8px;
  height: 44px;
}

/* ===== 验证码 ===== */
.captcha-box {
  height: 44px;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  user-select: none;
}

.captcha-text {
  color: #303133;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 3px;
  font-family: monospace;
}

/* ===== 登录按钮 ===== */
.login-button {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 8px;
  letter-spacing: 1px;
}

/* ===== 底部提示 ===== */
.login-tips {
  margin-top: 16px;
  text-align: center;
  border-top: 1px solid #f0f2f5;
  padding-top: 14px;
}

.login-tips p {
  font-size: 12px;
  color: #909399;
  margin: 0;
  line-height: 1.6;
}
</style>
