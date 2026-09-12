const { get } = require('../../utils/request')
const { ensureLogin } = require('../../utils/auth')

Page({
  data: {
    banners: [],
    introTitle: '首页介绍',
    introText: '用积分抽奖、兑换好礼',
    balance: '--'
  },

  onShow() {
    this.loadHome()
  },

  async loadHome() {
    try {
      // home 可匿名；有 token 时尽量带上
      const data = await get('/home', {}, { needAuth: true })
      const banners = data.banners || []
      const intro = data.intro || data.block || {}
      let introText = '用积分抽奖、兑换好礼'
      const content = intro.contentJson
      if (content) {
        const obj = typeof content === 'string' ? JSON.parse(content) : content
        introText = obj.text || introText
      }
      this.setData({
        banners,
        introTitle: intro.title || '首页介绍',
        introText,
        balance: data.balance != null ? data.balance : '--'
      })
    } catch (e) {
      // ignore
    }
  },

  onBannerTap(e) {
    const link = e.currentTarget.dataset.link
    if (!link) return
    if (link.startsWith('/pages/')) {
      wx.navigateTo({ url: link }).catch(() => {
        wx.switchTab({ url: link })
      })
    }
  },

  goLottery() {
    wx.switchTab({ url: '/pages/lottery/index' })
  },

  goRedeem() {
    wx.switchTab({ url: '/pages/redeem/index' })
  },

  async goPoints() {
    try {
      await ensureLogin()
      wx.switchTab({ url: '/pages/points/index' })
    } catch (e) {
      wx.showToast({ title: '请先登录', icon: 'none' })
    }
  }
})
