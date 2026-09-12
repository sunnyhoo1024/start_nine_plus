const { get, post } = require('../../utils/request')
const { ensureLogin } = require('../../utils/auth')

Page({
  data: {
    activities: [],
    drawingId: null,
    result: null
  },

  onShow() {
    this.loadActivities()
  },

  async loadActivities() {
    try {
      const data = await get('/lottery/activities', {}, { needAuth: false })
      this.setData({
        activities: data?.records || data?.list || data || []
      })
    } catch (e) {
      // ignore
    }
  },

  async onDraw(e) {
    const id = e.currentTarget.dataset.id
    try {
      await ensureLogin()
      this.setData({ drawingId: id, result: null })
      const result = await post('/lottery/draw', { activityId: id })
      this.setData({ result })
      wx.showToast({ title: '抽奖成功', icon: 'success' })
    } catch (err) {
      // toasted in request
    } finally {
      this.setData({ drawingId: null })
    }
  }
})
