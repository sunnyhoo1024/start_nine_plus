<script>
export default {
  globalData: {
    baseUrl: 'http://127.0.0.1:8082',
    token: '',
    userInfo: null
  },
  onLaunch() {
    this.globalData.token = uni.getStorageSync('app_token') || ''
    this.globalData.userInfo = uni.getStorageSync('app_user') || null
  }
}
</script>

<style lang="less">
@import './styles/neu.less';
</style>
