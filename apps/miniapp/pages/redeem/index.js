const { get, post } = require('../../utils/request')
const { ensureLogin } = require('../../utils/auth')

Page({
  data: {
    skus: [],
    redeemingId: null
  },

  onShow() {
    this.loadSkus()
  },

  async loadSkus() {
    try {
      const data = await get('/redeem/skus', {}, { needAuth: false })
      this.setData({
        skus: data?.records || data?.list || data || []
      })
    } catch (e) {
      // ignore
    }
  },

  onRedeem(e) {
    const { id, name } = e.currentTarget.dataset
    wx.showModal({
      title: '确认兑换',
      content: `确认兑换「${name}」？`,
      success: async (res) => {
        if (!res.confirm) return
        try {
          await ensureLogin()
          this.setData({ redeemingId: id })
          await post('/redeem', { skuId: id, quantity: 1 })
          wx.showToast({ title: '兑换成功', icon: 'success' })
          this.loadSkus()
        } catch (err) {
          // toasted
        } finally {
          this.setData({ redeemingId: null })
        }
      }
    })
  }
})
