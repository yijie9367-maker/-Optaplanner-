/**
 * 通用数据加载Composable
 * 处理数据加载、缓存、错误处理的通用逻辑
 */

import { ref, onMounted, onActivated } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 通用数据加载Hook
 * @param {Function} fetchFunc - 数据获取函数
 * @param {Object} options - 配置选项
 * @returns {Object} - 返回数据和相关方法
 */
export function useDataLoading(fetchFunc, options = {}) {
  const {
    errorMessage = '加载数据失败',
    initialData = [],
    useCache = true,
    cacheKey = null
  } = options

  const data = ref(initialData)
  const loading = ref(false)
  const error = ref(null)
  const dataLoaded = ref(false)

  // 获取缓存数据
  const getCachedData = () => {
    if (!useCache || !cacheKey) return null
    try {
      const cached = sessionStorage.getItem(`cache_${cacheKey}`)
      return cached ? JSON.parse(cached) : null
    } catch {
      return null
    }
  }

  // 保存缓存数据
  const setCachedData = (value) => {
    if (!useCache || !cacheKey) return
    try {
      sessionStorage.setItem(`cache_${cacheKey}`, JSON.stringify(value))
    } catch {
      console.warn('缓存数据失败')
    }
  }

  // 加载数据
  const loadData = async (forceRefresh = false) => {
    // 如果已缓存且非强制刷新
    if (!forceRefresh && dataLoaded.value) {
      const cached = getCachedData()
      if (cached) {
        data.value = cached
        return cached
      }
    }

    loading.value = true
    error.value = null

    try {
      const result = await fetchFunc()
      data.value = result
      setCachedData(result)
      dataLoaded.value = true
      return result
    } catch (err) {
      error.value = err
      console.error(errorMessage, err)
      ElMessage.error(errorMessage)
      throw err
    } finally {
      loading.value = false
    }
  }

  // 刷新数据
  const refresh = () => loadData(true)

  // 清空数据和缓存
  const clear = () => {
    data.value = initialData
    dataLoaded.value = false
    error.value = null
    if (useCache && cacheKey) {
      sessionStorage.removeItem(`cache_${cacheKey}`)
    }
  }

  // 组件挂载时加载数据
  onMounted(() => {
    if (!dataLoaded.value) {
      loadData()
    }
  })

  // KeepAlive激活时处理
  onActivated(() => {
    // 可选：在激活时刷新数据
    // loadData(true)
  })

  return {
    data,
    loading,
    error,
    dataLoaded,
    loadData,
    refresh,
    clear
  }
}

