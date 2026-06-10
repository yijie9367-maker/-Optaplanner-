/**
 * 将后端返回的 Blob 对象触发浏览器下载
 * @param {Blob} blob - 文件数据
 * @param {string} filename - 保存的文件名（含扩展名）
 */
export function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}
