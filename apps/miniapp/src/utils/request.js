import { showToast } from './toast'

const DEFAULT_BASE = 'http://127.0.0.1:8082'

/** 与后端 ErrorCode 对齐 */
export const ApiCode = {
  OK: 200,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  INTERNAL: 500,
  BIZ_ERROR: 1000,
  POINTS_INSUFFICIENT: 2001,
  STOCK_INSUFFICIENT: 2002,
  LOTTERY_NO_PRIZE: 2003,
  LOTTERY_LIMIT: 2004,
  ACTIVITY_UNAVAILABLE: 2005,
  REDEEM_UNAVAILABLE: 2006,
  AUTH_FAILED: 2007
}

export class ApiError extends Error {
  constructor(code, message, data) {
    super(message || '请求失败')
    this.name = 'ApiError'
    this.code = code
    this.data = data
    this.errorMsg =
      (data && (data.errorMsg || data.message)) || message || '请求失败'
  }
}

function getBaseUrl() {
  const app = getApp()
  return (app && app.globalData && app.globalData.baseUrl) || DEFAULT_BASE
}

function getToken() {
  const app = getApp()
  return (app && app.globalData && app.globalData.token) || uni.getStorageSync('app_token') || ''
}

function clearAuth() {
  const app = getApp()
  if (app && app.globalData) {
    app.globalData.token = ''
    app.globalData.userInfo = null
  }
  uni.removeStorageSync('app_token')
  uni.removeStorageSync('app_user')
}

/**
 * 统一响应：{ code, message, data }
 * - code === 200：resolve(data)
 * - 其他：reject(ApiError)，默认 toast message / data.errorMsg
 */
export function request({ url, method = 'GET', data, needAuth = true, silent = false }) {
  const token = getToken()
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${getBaseUrl()}/app/api${url}`,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...(needAuth && token ? { Authorization: `Bearer ${token}` } : {})
      },
      success(res) {
        const body = res.data
        if (res.statusCode === 401 || (body && body.code === ApiCode.UNAUTHORIZED)) {
          clearAuth()
          const msg =
            (body && (body.message || (body.data && body.data.errorMsg))) || '未登录'
          if (!silent) {
            showToast({ title: msg, type: 'error' })
          }
          reject(new ApiError(ApiCode.UNAUTHORIZED, msg, body && body.data))
          return
        }
        if (body && typeof body.code === 'number') {
          if (body.code === ApiCode.OK) {
            resolve(body.data)
            return
          }
          const msg =
            body.message ||
            (body.data && body.data.errorMsg) ||
            '请求失败'
          if (!silent) {
            showToast({ title: msg, type: 'error' })
          }
          reject(new ApiError(body.code, msg, body.data))
          return
        }
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(body)
        } else {
          if (!silent) {
            showToast({ title: '网络错误', type: 'error' })
          }
          reject(new ApiError(res.statusCode, '网络错误'))
        }
      },
      fail(err) {
        if (!silent) {
          showToast({ title: '网络异常', type: 'error' })
        }
        reject(err)
      }
    })
  })
}

export const get = (url, data, opts = {}) => request({ url, method: 'GET', data, ...opts })
export const post = (url, data, opts = {}) => request({ url, method: 'POST', data, ...opts })
export const put = (url, data, opts = {}) => request({ url, method: 'PUT', data, ...opts })
