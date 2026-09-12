const { post, get } = require('./request')

function login(extra = {}) {
  const app = getApp()
  return new Promise((resolve, reject) => {
    wx.login({
      success(loginRes) {
        if (!loginRes.code) {
          reject(new Error('wx.login 失败'))
          return
        }
        post(
          '/auth/login',
          {
            code: loginRes.code,
            nickname: extra.nickname,
            avatarUrl: extra.avatarUrl
          },
          { needAuth: false }
        )
          .then((data) => {
            app.globalData.token = data.token
            app.globalData.userInfo = {
              userId: data.userId,
              nickname: data.nickname,
              avatarUrl: data.avatarUrl,
              balance: data.balance
            }
            wx.setStorageSync('app_token', data.token)
            wx.setStorageSync('app_user', app.globalData.userInfo)
            resolve(data)
          })
          .catch(reject)
      },
      fail: reject
    })
  })
}

function logout() {
  const app = getApp()
  app.globalData.token = ''
  app.globalData.userInfo = null
  wx.removeStorageSync('app_token')
  wx.removeStorageSync('app_user')
}

function isLoggedIn() {
  const app = getApp()
  return !!(app.globalData.token || wx.getStorageSync('app_token'))
}

function ensureLogin() {
  if (isLoggedIn()) {
    return Promise.resolve()
  }
  return login()
}

function fetchProfile() {
  return get('/profile/me').then((data) => {
    const app = getApp()
    app.globalData.userInfo = data
    wx.setStorageSync('app_user', data)
    return data
  })
}

module.exports = {
  login,
  logout,
  isLoggedIn,
  ensureLogin,
  fetchProfile
}
