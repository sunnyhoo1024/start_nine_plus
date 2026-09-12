const DEFAULT_BASE = 'http://127.0.0.1:8082'

function getBaseUrl() {
  const app = getApp()
  return (app && app.globalData && app.globalData.baseUrl) || DEFAULT_BASE
}

function getToken() {
  const app = getApp()
  return (app && app.globalData && app.globalData.token) || uni.getStorageSync('app_token') || ''
}

export function request({ url, method = 'GET', data, needAuth = true }) {
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
        if (res.statusCode === 401 || (body && body.code === 401)) {
          const app = getApp()
          if (app && app.globalData) {
            app.globalData.token = ''
            app.globalData.userInfo = null
          }
          uni.removeStorageSync('app_token')
          uni.removeStorageSync('app_user')
          reject(new Error((body && body.message) || '未登录'))
          return
        }
        if (body && typeof body.code === 'number') {
          if (body.code === 0) {
            resolve(body.data)
          } else {
            uni.showToast({ title: body.message || '请求失败', icon: 'none' })
            reject(new Error(body.message || '请求失败'))
          }
          return
        }
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(body)
        } else {
          uni.showToast({ title: '网络错误', icon: 'none' })
          reject(new Error('网络错误'))
        }
      },
      fail(err) {
        uni.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      }
    })
  })
}

export const get = (url, data, opts = {}) => request({ url, method: 'GET', data, ...opts })
export const post = (url, data, opts = {}) => request({ url, method: 'POST', data, ...opts })
export const put = (url, data, opts = {}) => request({ url, method: 'PUT', data, ...opts })
