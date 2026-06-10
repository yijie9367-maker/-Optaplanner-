<template>
  <div class="login-wrapper">
    <!-- 左侧插画区 -->
    <div class="left-panel">
      <div class="left-content">
        <div class="illustration">
          <div class="illust-screen">
            <div class="screen-bar"><span></span><span></span><span></span></div>
            <div class="screen-body">
              <div class="line short"></div>
              <div class="line long"></div>
              <div class="line medium"></div>
              <div class="line long"></div>
              <div class="line short"></div>
            </div>
          </div>
          <div class="illust-person person-left">
            <div class="head"></div>
            <div class="body-shape"></div>
          </div>
          <div class="illust-person person-right">
            <div class="head"></div>
            <div class="body-shape"></div>
            <div class="magnifier"></div>
          </div>
        </div>
        <h2 class="left-title">教育教务管理系统</h2>
        <p class="left-subtitle">管理员后台 · 智能排课与教务管理</p>
      </div>
      <!-- 装饰元素 -->
      <div class="decor decor-1"></div>
      <div class="decor decor-2"></div>
      <div class="decor decor-3"></div>
    </div>

    <!-- 右侧表单区 -->
    <div class="right-panel">
      <div class="form-card">
        <h2 class="form-title">管理员登录</h2>

        <el-form
          ref="loginFormRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username" label="账号">
            <el-input
              v-model="form.username"
              placeholder="请输入管理员账号"
              clearable
              @keyup.enter="handleLogin"
            >
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password" label="密码">
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

          <el-form-item prop="captcha" label="验证码">
            <el-row :gutter="10" style="width: 100%">
              <el-col :span="14">
                <el-input
                  v-model="form.captcha"
                  placeholder="请输入验证码"
                  clearable
                  @keyup.enter="handleLogin"
                >
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

          <el-form-item>
            <el-button
              type="primary"
              class="login-btn"
              size="large"
              :loading="loading"
              @click="handleLogin"
            >
              确 定
            </el-button>
          </el-form-item>
        </el-form>
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
import { loginAdmin } from '@/api/admin'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '', captcha: '' })
const captchaCode = ref('')
const actualCaptcha = ref('')

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
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

const handleLogin = async () => {
  // 验证码校验（优先）—— 输入 1111 可跳过验证码
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
    const response = await loginAdmin(form.username, form.password)

    if (response.code === 200 && response.data) {
      const backendData = response.data
      const userData = {
        id: backendData.id,
        username: backendData.username,
        name: backendData.name || backendData.username || '管理员',
        role: backendData.role || 'admin',
        token: backendData.token || 'admin-token-' + Date.now()
      }

      userStore.setUser(userData)
      localStorage.setItem('token', userData.token)
      localStorage.setItem('admin', JSON.stringify(userData))

      ElMessage.success('登录成功')
      setTimeout(() => {
        router.push('/dashboard')
      }, 500)
    } else {
      ElMessage.error(response.message || '用户名或密码错误')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败：' + (error.message || '网络错误'))
  }
  loading.value = false
}

refreshCaptcha()
</script>

<style scoped>
.login-wrapper {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* ===== 左侧面板 ===== */
.left-panel {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  background: linear-gradient(135deg, #4facfe 0%, #3b82f6 50%, #2563eb 100%);
  overflow: hidden;
}

.left-content {
  text-align: center;
  z-index: 10;
  color: white;
}

.left-title {
  font-size: 28px;
  font-weight: 700;
  margin: 30px 0 8px;
  letter-spacing: 2px;
}

.left-subtitle {
  font-size: 14px;
  opacity: 0.8;
  margin: 0;
}

/* 装饰圆 */
.decor {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}
.decor-1 { width: 400px; height: 400px; top: -100px; left: -100px; }
.decor-2 { width: 250px; height: 250px; bottom: 60px; right: -60px; }
.decor-3 { width: 120px; height: 120px; top: 40%; left: 60%; background: rgba(255,255,255,0.05); }

/* ===== CSS 插画 ===== */
.illustration {
  position: relative;
  width: 320px;
  height: 220px;
  margin: 0 auto;
}

.illust-screen {
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  width: 200px;
  height: 140px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  overflow: hidden;
}

.screen-bar {
  display: flex;
  gap: 4px;
  padding: 6px 8px;
  background: rgba(255, 255, 255, 0.1);
}

.screen-bar span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
}

.screen-body {
  padding: 10px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.line {
  height: 6px;
  border-radius: 3px;
  background: rgba(255, 255, 255, 0.25);
}
.line.short { width: 40%; }
.line.medium { width: 70%; }
.line.long { width: 90%; }

.illust-person {
  position: absolute;
  bottom: 10px;
}

.person-left {
  left: 30px;
}

.person-right {
  right: 30px;
}

.illust-person .head {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  margin: 0 auto 4px;
}

.illust-person .body-shape {
  width: 32px;
  height: 40px;
  border-radius: 12px 12px 6px 6px;
  background: rgba(255, 255, 255, 0.3);
  margin: 0 auto;
}

.person-right .magnifier {
  position: absolute;
  top: 10px;
  right: -18px;
  width: 22px;
  height: 22px;
  border: 3px solid rgba(255, 255, 255, 0.5);
  border-radius: 50%;
}

.person-right .magnifier::after {
  content: '';
  position: absolute;
  bottom: -8px;
  right: -2px;
  width: 3px;
  height: 10px;
  background: rgba(255, 255, 255, 0.5);
  transform: rotate(-45deg);
  border-radius: 2px;
}

/* ===== 右侧面板 ===== */
.right-panel {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f0f4f8;
}

.form-card {
  width: 100%;
  max-width: 400px;
  padding: 48px 44px 36px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
}

.form-title {
  text-align: center;
  font-size: 22px;
  color: #1f2d3d;
  font-weight: 700;
  margin: 0 0 32px;
}

/* ===== 表单 ===== */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-input__wrapper) {
  border-radius: 6px;
  height: 42px;
}

/* ===== 验证码 ===== */
.captcha-box {
  height: 42px;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
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
.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 6px;
  letter-spacing: 4px;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .left-panel {
    display: none;
  }
  .right-panel {
    flex: 1;
  }
}
</style>
