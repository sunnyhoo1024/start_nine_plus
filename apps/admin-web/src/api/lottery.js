import request from '@/utils/request'

export function listActivities(params) {
  return request.get('/lottery/activities', { params })
}

export function createActivity(data) {
  return request.post('/lottery/activities', data)
}

export function updateActivity(id, data) {
  return request.put(`/lottery/activities/${id}`, data)
}

export function deleteActivity(id) {
  return request.delete(`/lottery/activities/${id}`)
}

export function listPrizes(activityId) {
  return request.get(`/lottery/activities/${activityId}/prizes`)
}

export function createPrize(activityId, data) {
  return request.post('/lottery/prizes', { ...data, activityId })
}

export function updatePrize(activityId, prizeId, data) {
  return request.put(`/lottery/prizes/${prizeId}`, { ...data, activityId })
}

export function deletePrize(activityId, prizeId) {
  return request.delete(`/lottery/prizes/${prizeId}`)
}
