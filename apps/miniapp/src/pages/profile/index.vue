<template>
  <view class="page">
    <view class="card profile">
      <image class="avatar" :src="userInfo.avatarUrl || defaultAvatar" mode="aspectFill" />
      <view>
        <view class="name">{{ userInfo.nickname || '未登录用户' }}</view>
        <view class="uid">ID：{{ userInfo.userId || '-' }}</view>
        <view class="bal">积分：{{ userInfo.balance != null ? userInfo.balance : '-' }}</view>
      </view>
    </view>

    <view class="card">
      <button v-if="!loggedIn" class="btn-primary" @click="onLogin">微信登录</button>
      <button v-else class="btn-primary" @click="onLogout">退出登录</button>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { fetchProfile, isLoggedIn, login, logout } from '@/utils/auth'

const defaultAvatar = '/static/logo.png'
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
    uni.showToast({ title: '登录成功', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: '登录失败', icon: 'none' })
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

<style scoped>
.profile {
  display: flex;
  gap: 24rpx;
  align-items: center;
}
.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  background: #ebeef5;
}
.name {
  font-size: 34rpx;
  font-weight: 600;
}
.uid,
.bal {
  color: #909399;
  margin-top: 6rpx;
}
</style>
