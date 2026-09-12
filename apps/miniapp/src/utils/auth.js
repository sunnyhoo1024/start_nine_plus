import { get, post } from './request'

export function login(extra = {}) {
  return new Promise((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success(loginRes) {
        if (!loginRes.code) {
          reject(new Error('uni.login 失败'))
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
            const app = getApp()
            const userInfo = {
              userId: data.userId,
              nickname: data.nickname,
              avatarUrl: data.avatarUrl,
              balance: data.balance
            }
            if (app && app.globalData) {
              app.globalData.token = data.token
              app.globalData.userInfo = userInfo
            }
            uni.setStorageSync('app_token', data.token)
            uni.setStorageSync('app_user', userInfo)
            resolve(data)
          })
          .catch(reject)
      },
      fail: reject
    })
  })
}

export function logout() {
  const app = getApp()
  if (app && app.globalData) {
    app.globalData.token = ''
    app.globalData.userInfo = null
  }
  uni.removeStorageSync('app_token')
  uni.removeStorageSync('app_user')
}

export function isLoggedIn() {
  const app = getApp()
  return !!(
    (app && app.globalData && app.globalData.token) ||
    uni.getStorageSync('app_token')
  )
}

export function ensureLogin() {
  if (isLoggedIn()) {
    return Promise.resolve()
  }
  return login()
}

export function fetchProfile() {
  return get('/profile/me').then((data) => {
    const app = getApp()
    if (app && app.globalData) {
      app.globalData.userInfo = data
    }
    uni.setStorageSync('app_user', data)
    return data
  })
}
