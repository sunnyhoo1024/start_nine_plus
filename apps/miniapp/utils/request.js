const app = getApp()

function request(options) {
  const { url, method = 'GET', data, header = {}, needAuth = true } = options
  const baseUrl = app.globalData.baseUrl || ''
  const token = app.globalData.token || wx.getStorageSync('app_token') || ''

  return new Promise((resolve, reject) => {
    wx.request({
      url: `${baseUrl}/app/api${url}`,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...(needAuth && token ? { Authorization: `Bearer ${token}` } : {}),
        ...header
      },
      success(res) {
        const body = res.data
        if (res.statusCode === 401 || (body && body.code === 401)) {
          app.globalData.token = ''
          app.globalData.userInfo = null
          wx.removeStorageSync('app_token')
          wx.removeStorageSync('app_user')
          reject(new Error(body?.message || '未登录'))
          return
        }
        if (body && typeof body.code === 'number') {
          if (body.code === 0) {
            resolve(body.data)
          } else {
            wx.showToast({ title: body.message || '请求失败', icon: 'none' })
            reject(new Error(body.message || '请求失败'))
          }
          return
        }
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(body)
        } else {
          wx.showToast({ title: '网络错误', icon: 'none' })
          reject(new Error('网络错误'))
        }
      },
      fail(err) {
        wx.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      }
    })
  })
}

module.exports = {
  request,
  get: (url, data, opts = {}) => request({ url, method: 'GET', data, ...opts }),
  post: (url, data, opts = {}) => request({ url, method: 'POST', data, ...opts }),
  put: (url, data, opts = {}) => request({ url, method: 'PUT', data, ...opts })
}
