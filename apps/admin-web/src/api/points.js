import request from '@/utils/request'

export function adjustPoints(data) {
  return request.post('/points/adjust', data)
}

export function listLedger(params) {
  return request.get('/points/ledger', { params })
}
