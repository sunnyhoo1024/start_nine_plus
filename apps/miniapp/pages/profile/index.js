const { login, logout, isLoggedIn, fetchProfile } = require('../../utils/auth')

Page({
  data: {
    loggedIn: false,
    loading: false,
    userInfo: {},
    defaultAvatar: 'https://via.placeholder.com/120x120?text=User'
  },

  onShow() {
    this.refresh()
  },

  async refresh() {
    const loggedIn = isLoggedIn()
    this.setData({ loggedIn })
    if (!loggedIn) {
      this.setData({ userInfo: {} })
      return
    }
    try {
      const userInfo = await fetchProfile()
      this.setData({ userInfo: userInfo || {} })
    } catch (e) {
      const cached = wx.getStorageSync('app_user') || {}
      this.setData({ userInfo: cached })
    }
  },

  async onLogin() {
    this.setData({ loading: true })
    try {
      await login()
      wx.showToast({ title: '登录成功', icon: 'success' })
      await this.refresh()
    } catch (e) {
      wx.showToast({ title: e.message || '登录失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  onLogout() {
    logout()
    this.setData({ loggedIn: false, userInfo: {} })
    wx.showToast({ title: '已退出', icon: 'none' })
  }
})
