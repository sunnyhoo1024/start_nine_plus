import request from '@/utils/request'

export function listUsers(params) {
  return request.get('/app-users', { params })
}
