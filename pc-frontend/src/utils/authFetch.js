import { buildApiUrl } from '@/utils/api'

export function authFetch(url, options = {}) {
  const token = localStorage.getItem('token')
  const headers = new Headers(options.headers || {})

  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  return fetch(buildApiUrl(url), {
    ...options,
    headers
  })
}
