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

<style>
page {
  background: #f5f7fa;
  color: #303133;
  font-size: 28rpx;
}
.card {
  background: #fff;
  border-radius: 16rpx;
  margin: 24rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}
.btn-primary {
  background: #1f2d3d;
  color: #fff;
  border-radius: 12rpx;
}
</style>
