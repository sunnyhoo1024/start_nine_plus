<script>
const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8082'

export default {
  globalData: {
    baseUrl: API_BASE,
    token: '',
    userInfo: null
  },
  onLaunch() {
    this.globalData.baseUrl = API_BASE
    this.globalData.token = uni.getStorageSync('app_token') || ''
    this.globalData.userInfo = uni.getStorageSync('app_user') || null
    if (import.meta.env.DEV) {
      console.log('[miniapp] API baseUrl =', this.globalData.baseUrl, 'mode =', import.meta.env.MODE)
    }
  }
}
</script>

<style lang="less">
@import './styles/neu.less';
</style>
