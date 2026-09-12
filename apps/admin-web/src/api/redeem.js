import request from '@/utils/request'

export function listSkus(params) {
  return request.get('/redeem-skus', { params })
}

export function createSku(data) {
  return request.post('/redeem-skus', data)
}

export function updateSku(id, data) {
  return request.put(`/redeem-skus/${id}`, data)
}

export function deleteSku(id) {
  return request.delete(`/redeem-skus/${id}`)
}

export function listOrders(params) {
  return request.get('/redeem-orders', { params })
}

export function fulfillOrder(id) {
  return request.put(`/redeem-orders/${id}/status`, { status: 'FULFILLED' })
}

export function cancelOrder(id) {
  return request.put(`/redeem-orders/${id}/status`, { status: 'CANCELLED' })
}
