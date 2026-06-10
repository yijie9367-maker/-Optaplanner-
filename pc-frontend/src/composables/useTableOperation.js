/**
 * 统一的表格操作Composable
 * 消除分页、搜索、过滤的重复代码
 * 用于：Students、Teachers、Courses、Classroom等表格页面
 */

import { ref } from 'vue'

export function useTableOperation(fetchFunc) {
  // 分页状态
  const currentPage = ref(1)
  const pageSize = ref(15)
  const searchText = ref('')
  const dataLoaded = ref(false)
  const loading = ref(false)

  // 第一次加载标志
  const initLoad = async () => {
    if (!dataLoaded.value) {
      await fetchFunc()
      dataLoaded.value = true
    }
  }

  // 分页索引方法
  const indexMethod = (index) => {
    return (currentPage.value - 1) * pageSize.value + index + 1
  }

  // 分页数据计算
  const getPaginatedData = (filteredData) => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredData.slice(start, end)
  }

  // 页大小改变
  const handleSizeChange = (newSize) => {
    pageSize.value = newSize
    currentPage.value = 1
  }

  // 页码改变
  const handlePageChange = (newPage) => {
    currentPage.value = newPage
  }

  // 搜索时重置分页
  const handleSearch = () => {
    currentPage.value = 1
  }

  // 清空搜索
  const clearSearch = () => {
    searchText.value = ''
    handleSearch()
  }

  return {
    currentPage,
    pageSize,
    searchText,
    dataLoaded,
    loading,
    initLoad,
    indexMethod,
    getPaginatedData,
    handleSizeChange,
    handlePageChange,
    handleSearch,
    clearSearch
  }
}

