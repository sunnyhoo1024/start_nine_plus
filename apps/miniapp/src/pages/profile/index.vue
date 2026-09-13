<template>
  <view class="page profile">
    <view class="card neu-out">
      <view class="avatar-ring">
        <image class="avatar" :src="userInfo.avatarUrl || defaultAvatar" mode="aspectFill" />
      </view>
      <view class="info">
        <view class="name">{{ userInfo.nickname || '旅人尚未登录' }}</view>
        <view class="uid">ID · {{ userInfo.userId || '----' }}</view>
        <view class="bal neu-in">★ {{ userInfo.balance != null ? userInfo.balance : '-' }} PTS</view>
      </view>
    </view>

    <view class="panel neu-out">
      <view class="panel-title">软粘土宇宙通行证</view>
      <view class="panel-desc">登录后可同步积分、抽奖与兑换记录。灵感来自拟物质感与快餐式热情节奏。</view>
      <button v-if="!loggedIn" class="neu-btn neu-btn-accent" @click="onLogin">微信一键登录</button>
      <button v-else class="neu-btn" @click="onLogout">退出登录</button>
    </view>

    <view class="footer brand-mark">START NINE+</view>
    <SnpToast />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { showToast } from '@/utils/toast'
import { fetchProfile, isLoggedIn, login, logout } from '@/utils/auth'
import SnpToast from '@/components/SnpToast/SnpToast.vue'

const defaultAvatar = '/static/avatar/default.png'
const userInfo = ref({})
const loggedIn = ref(false)

function syncLocal() {
  loggedIn.value = isLoggedIn()
  userInfo.value = uni.getStorageSync('app_user') || {}
}

async function onLogin() {
  try {
    await login()
    try {
      userInfo.value = (await fetchProfile()) || uni.getStorageSync('app_user') || {}
    } catch (e) {
      syncLocal()
    }
    loggedIn.value = true
    uni.showToast({ title: '欢迎回来', icon: 'success' })
  } catch (e) {
    showToast({ title: '登录失败', type: 'error' })
  }
}

function onLogout() {
  logout()
  userInfo.value = {}
  loggedIn.value = false
}

onShow(() => {
  syncLocal()
  if (loggedIn.value) {
    fetchProfile()
      .then((data) => {
        userInfo.value = data || {}
      })
      .catch(() => {})
  }
})
</script>

<style lang="less" scoped>
@import '@/styles/mixins.less';

.profile {
  padding: 24rpx 28rpx 48rpx;
}
.card {
  padding: 36rpx 28rpx;
  display: flex;
  gap: 28rpx;
  align-items: center;
  margin-bottom: 24rpx;
}
.avatar-ring {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  padding: 6rpx;
  background: @neu-bg;
  box-shadow: 6rpx 6rpx 12rpx @neu-shadow, -6rpx -6rpx 12rpx @neu-light;
  flex-shrink: 0;
  overflow: hidden;
}
.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: #d5d5d5;
}
.info {
  flex: 1;
  min-width: 0;
}
.name {
  font-size: 36rpx;
  font-weight: 900;
}
.uid {
  margin-top: 8rpx;
  color: @neu-muted;
  font-size: 24rpx;
}
.bal {
  display: inline-block;
  margin-top: 16rpx;
  padding: 10rpx 18rpx;
  font-size: 22rpx;
  font-weight: 800;
  color: @neu-accent;
}
.panel {
  padding: 36rpx 32rpx;
}
.panel-title {
  font-size: 32rpx;
  font-weight: 900;
}
.panel-desc {
  margin: 14rpx 0 28rpx;
  color: @neu-muted;
  font-size: 24rpx;
  line-height: 1.6;
}
.neu-btn {
  height: 88rpx;
  line-height: 88rpx;
  font-size: 30rpx;
}
.footer {
  margin-top: 48rpx;
  text-align: center;
  color: @neu-accent;
  opacity: 0.7;
  font-size: 28rpx;
}
</style>
