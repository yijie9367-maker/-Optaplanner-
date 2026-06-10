// store/user.js
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => {
    // 尝试从 localStorage 恢复用户数据
    const storedUser = localStorage.getItem('user')
    const storedToken = localStorage.getItem('token')
    
    return {
      user: storedUser ? JSON.parse(storedUser) : null,
      token: storedToken || '',
      // 保持向后兼容性
      admin: localStorage.getItem('admin') 
        ? JSON.parse(localStorage.getItem('admin')) 
        : null
    }
  },
  
  getters: {
    // 获取当前用户（支持 admin 和 student）
    currentUser: (state) => state.user || state.admin,
    isAdmin: (state) => state.user?.role === 'admin' || !!state.admin,
    isStudent: (state) => state.user?.role === 'student',
    isAuthenticated: (state) => !!state.token
  },
  
  actions: {
    // 设置用户（支持 admin 和 student）
    setUser(user) {
      this.user = user
      this.token = user.token || ''
      
      // 保存到 localStorage
      localStorage.setItem('user', JSON.stringify(user))
      localStorage.setItem('token', this.token)
      
      // 向后兼容性：如果是 admin，也存到 admin 字段
      if (user.role === 'admin') {
        this.admin = user
        localStorage.setItem('admin', JSON.stringify(user))
      } else {
        localStorage.removeItem('admin')
      }
    },
    
    // 登出
    logout() {
      this.user = null
      this.admin = null
      this.token = ''
      localStorage.removeItem('user')
      localStorage.removeItem('admin')
      localStorage.removeItem('token')
    },
    
    // 设置 token（支持已有用户的 token 更新）
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    }
  }
})

