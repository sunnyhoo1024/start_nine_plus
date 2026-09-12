<template>
  <view class="page">
    <swiper v-if="banners.length" class="banner" circular autoplay indicator-dots>
      <swiper-item v-for="(item, idx) in banners" :key="idx" @click="onBannerTap(item)">
        <image class="banner-img" :src="item.imageUrl" mode="aspectFill" />
      </swiper-item>
    </swiper>

    <view class="card">
      <view class="title">{{ introTitle }}</view>
      <view class="desc">{{ introText }}</view>
      <view class="balance">积分余额：{{ balance }}</view>
    </view>

    <view class="card entries">
      <button class="btn-primary" @click="goLottery">去抽奖</button>
      <button class="btn-primary" @click="goRedeem">去兑换</button>
      <button class="btn-primary" @click="goPoints">积分明细</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { get } from '@/utils/request'
import { ensureLogin } from '@/utils/auth'

const banners = ref([])
const introTitle = ref('首页介绍')
const introText = ref('用积分抽奖、兑换好礼')
const balance = ref('--')

async function loadHome() {
  try {
    const data = await get('/home', {}, { needAuth: true })
    banners.value = data.banners || []
    const intro = data.intro || {}
    introTitle.value = intro.title || '首页介绍'
    let text = '用积分抽奖、兑换好礼'
    const content = intro.contentJson
    if (content) {
      const obj = typeof content === 'string' ? JSON.parse(content) : content
      text = obj.text || text
    }
    introText.value = text
    balance.value = data.balance != null ? data.balance : '--'
  } catch (e) {
    // ignore
  }
}

function onBannerTap(item) {
  const link = item && item.linkUrl
  if (!link) return
  if (link.startsWith('/pages/')) {
    uni.navigateTo({ url: link }).catch(() => {
      uni.switchTab({ url: link })
    })
  }
}

function goLottery() {
  uni.switchTab({ url: '/pages/lottery/index' })
}

function goRedeem() {
  uni.switchTab({ url: '/pages/redeem/index' })
}

async function goPoints() {
  try {
    await ensureLogin()
    uni.switchTab({ url: '/pages/points/index' })
  } catch (e) {
    uni.showToast({ title: '请先登录', icon: 'none' })
  }
}

onShow(() => {
  loadHome()
})
</script>

<style scoped>
.banner {
  height: 320rpx;
}
.banner-img {
  width: 100%;
  height: 320rpx;
}
.title {
  font-size: 34rpx;
  font-weight: 600;
  margin-bottom: 12rpx;
}
.desc {
  color: #606266;
  line-height: 1.6;
}
.balance {
  margin-top: 20rpx;
  color: #1f2d3d;
  font-weight: 600;
}
.entries button {
  margin-bottom: 16rpx;
}
</style>
