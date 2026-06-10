import request from '@/utils/request'

export function getConstraintConfigs() {
  return request.get('/api/constraint-config')
}

export function updateConstraintConfig(key, data) {
  return request.put(`/api/constraint-config/${key}`, data)
}
