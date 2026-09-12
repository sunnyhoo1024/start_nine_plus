// app.js
// 生产环境必须将 baseUrl 改为已备案的 https 域名（微信要求）
App({
  globalData: {
    baseUrl: 'http://127.0.0.1:8082',
    token: '',
    userInfo: null
  },
  onLaunch() {
    const token = wx.getStorageSync('app_token') || ''
    const userInfo = wx.getStorageSync('app_user') || null
    this.globalData.token = token
    this.globalData.userInfo = userInfo
  }
})
