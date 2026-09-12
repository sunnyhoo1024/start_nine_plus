const { get } = require('../../utils/request')
const { ensureLogin } = require('../../utils/auth')

Page({
  data: {
    balance: '--',
    ledger: []
  },

  onShow() {
    this.refresh()
  },

  async refresh() {
    try {
      await ensureLogin()
      const [balance, ledgerPage] = await Promise.all([
        get('/points/balance'),
        get('/points/ledger', { page: 1, size: 50 })
      ])
      const balanceVal = typeof balance === 'object' ? balance.balance ?? balance : balance
      this.setData({
        balance: balanceVal,
        ledger: ledgerPage?.records || ledgerPage?.list || ledgerPage || []
      })
    } catch (e) {
      wx.showToast({ title: '请先登录', icon: 'none' })
    }
  }
})
