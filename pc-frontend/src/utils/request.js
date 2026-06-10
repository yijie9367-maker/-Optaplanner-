import axios from 'axios'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { API_BASE_URL } from '@/utils/api'

const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 2000000
})

// 请求拦截器 - 附带 token
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    if (response.data instanceof Blob) return response.data

    const { code, message } = response.data
    if (code === 200) {
      return response.data
    } else {
      ElMessage({ type: 'error', message: message || '接口请求失败' })
      return Promise.reject(new Error(message || 'Error'))
    }
  },
  async (error) => {
    const userStore = useUserStore()
    const status = error.response?.status
    if (status === 401) {
      ElMessage({ type: 'warning', message: '登录已过期，请重新登录' })
      await userStore.logout()
      router.push('/login/admin')
      return Promise.reject(error)
    }
    ElMessage({ type: 'error', message: error.message || '网络错误' })
    return Promise.reject(error)
  }
)

export default request
