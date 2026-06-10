const rawBaseUrl = (import.meta.env.VITE_API_BASE_URL || '/api').trim()

export const API_BASE_URL = rawBaseUrl.endsWith('/') && rawBaseUrl !== '/'
  ? rawBaseUrl.slice(0, -1)
  : rawBaseUrl

export function buildApiUrl(path = '') {
  if (!path) {
    return API_BASE_URL
  }

  if (/^https?:\/\//i.test(path)) {
    return path
  }

  const normalizedPath = path.startsWith('/') ? path : `/${path}`

  if (API_BASE_URL === '/') {
    return normalizedPath
  }

  if (normalizedPath === API_BASE_URL || normalizedPath.startsWith(`${API_BASE_URL}/`)) {
    return normalizedPath
  }

  return `${API_BASE_URL}${normalizedPath}`
}
