import request from '@/utils/request'

export function listBlocks(params) {
  return request.get('/blocks', { params })
}

export function createBlock(data) {
  return request.post('/blocks', data)
}

export function updateBlock(id, data) {
  return request.put(`/blocks/${id}`, data)
}

export function deleteBlock(id) {
  return request.delete(`/blocks/${id}`)
}
