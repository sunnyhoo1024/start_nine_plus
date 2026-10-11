<template>
  <view class="page home">
    <view class="blob blob-a" />
    <view class="blob blob-b" />

    <view class="hero">
      <view class="brand-row">
        <text class="brand-mark">START NINE+</text>
        <view class="live-pill neu-in">
          <view class="live-dot" />
          <text>今日营业中</text>
        </view>
      </view>
      <view class="hero-title">软乎乎的积分宇宙</view>
      <view class="hero-sub">抽一发好运 · 换一点小确幸</view>
    </view>

    <view class="banner-stage neu-out">
      <swiper
        class="banner-swiper"
        circular
        autoplay
        :interval="4200"
        :duration="700"
        @change="onBannerChange"
      >
        <swiper-item v-for="(item, idx) in displayBanners" :key="idx" @click="onBannerTap(item)">
          <view class="slide">
            <image
              v-if="item.imageUrl"
              class="banner-img kenburns"
              :src="item.imageUrl"
              mode="aspectFill"
            />
            <view v-else class="banner-fallback" :style="{ background: item.gradient }">
              <text class="fallback-emoji">{{ item.emoji }}</text>
              <text class="fallback-title">{{ item.title }}</text>
            </view>
            <view class="slide-shade" />
            <view class="slide-caption">
              <text class="cap-title">{{ item.title || '活动进行中' }}</text>
              <text class="cap-hint">轻触探索 →</text>
            </view>
          </view>
        </swiper-item>
      </swiper>
      <view class="dots">
        <view
          v-for="(_, i) in displayBanners"
          :key="i"
          class="dot"
          :class="{ on: i === bannerIndex }"
        />
      </view>
    </view>

    <view class="wallet neu-out">
      <view>
        <view class="wallet-label">口袋里的星星</view>
        <view class="wallet-num">
          <text class="num">{{ balance }}</text>
          <text class="unit">PTS</text>
        </view>
        <view class="wallet-desc">{{ introText }}</view>
      </view>
      <view class="coin float" @click="goPoints">★</view>
    </view>

    <view class="grid">
      <view class="tile neu-out" @click="goLottery">
        <view class="tile-icon pulse">🎰</view>
        <view class="tile-name">星光抽奖</view>
        <view class="tile-desc">转起来就有戏</view>
      </view>
      <view class="tile neu-out" @click="goRedeem">
        <view class="tile-icon">🎁</view>
        <view class="tile-name">积分兑换</view>
        <view class="tile-desc">把星星变成礼</view>
      </view>
      <view class="tile neu-out wide" @click="goPoints">
        <view class="tile-row">
          <view>
            <view class="tile-name">流水账本</view>
            <view class="tile-desc">每一笔星星去哪了</view>
          </view>
          <view class="tile-arrow">→</view>
        </view>
      </view>
    </view>
    <SnpToast />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow, onLoad } from '@dcloudio/uni-app'
import { get } from '@/utils/request'
import { ensureLogin } from '@/utils/auth'
import { staleWhileRevalidate } from '@/utils/cache'
import { showToast } from '@/utils/toast'
import SnpToast from '@/components/SnpToast/SnpToast.vue'

const banners = ref([])
const introText = ref('用积分抽奖、兑换好礼')
const balance = ref('--')
const bannerIndex = ref(0)

/** 首页固定跳转其它小程序体验版；CMS Banner 接在后面 */
const partnerBanners = [
  {
    title: 'STG星开心',
    emoji: '✨',
    gradient: 'linear-gradient(135deg, #ff7a66 0%, #ff4d3a 55%, #c93b2c 100%)',
    linkUrl: 'mp://wx1c0ae3adb97ad0e2?envVersion=trial'
  },
  {
    title: '星巴克',
    emoji: '☕',
    gradient: 'linear-gradient(135deg, #00704a 0%, #1e3932 55%, #006241 100%)',
    linkUrl: 'mp://wx3dcca19d0aa51755?envVersion=trial'
  }
]

const displayBanners = computed(() => {
  const cms = banners.value.map((b) => ({
    ...b,
    title: b.title || '活动进行中'
  }))
  return [...partnerBanners, ...cms]
})

function applyHome(data) {
  banners.value = data.banners || []
  const intro = data.intro || {}
  let text = '用积分抽奖、兑换好礼'
  const content = intro.contentJson
  if (content) {
    try {
      const obj = typeof content === 'string' ? JSON.parse(content) : content
      text = obj.text || text
    } catch (e) {
      // ignore bad json
    }
  }
  introText.value = text
  balance.value = data.balance != null ? data.balance : '--'
}

async function loadHome() {
  try {
    const { data } = await staleWhileRevalidate(
      'home',
      () => get('/home', {}, { needAuth: true }),
      45000,
      (fresh) => applyHome(fresh || {})
    )
    applyHome(data || {})
  } catch (e) {
    // ignore
  }
}

function onBannerChange(e) {
  bannerIndex.value = e.detail.current || 0
}

function parseMiniProgramLink(link) {
  if (!link || typeof link !== 'string') return null
  let appId = ''
  let query = ''
  if (link.startsWith('mp://') || link.startsWith('miniprogram://')) {
    const rest = link.replace(/^miniprogram:\/\//, '').replace(/^mp:\/\//, '')
    const qIndex = rest.indexOf('?')
    appId = qIndex >= 0 ? rest.slice(0, qIndex) : rest
    query = qIndex >= 0 ? rest.slice(qIndex + 1) : ''
  } else if (/^wx[0-9a-fA-F]{16}$/.test(link.trim())) {
    appId = link.trim()
  } else {
    return null
  }
  const params = {}
  if (query) {
    query.split('&').forEach((pair) => {
      const eq = pair.indexOf('=')
      const k = eq >= 0 ? pair.slice(0, eq) : pair
      const v = eq >= 0 ? pair.slice(eq + 1) : ''
      if (k) params[decodeURIComponent(k)] = decodeURIComponent(v || '')
    })
  }
  return {
    appId,
    path: params.path || '',
    envVersion: params.envVersion || 'trial'
  }
}

function onBannerTap(item) {
  const link = item && item.linkUrl
  if (!link) {
    goLottery()
    return
  }
  if (link.startsWith('/pages/')) {
    uni.navigateTo({ url: link }).catch(() => {
      uni.switchTab({ url: link })
    })
    return
  }
  const mp = parseMiniProgramLink(link)
  if (mp) {
    uni.navigateToMiniProgram({
      appId: mp.appId,
      path: mp.path,
      envVersion: mp.envVersion,
      fail: () => {
        showToast({ title: '暂无法打开该小程序', type: 'error' })
      }
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
    showToast({ title: '请先登录', type: 'error' })
  }
}

onLoad(() => {
  loadHome()
})

onShow(() => {
  // Warm cache only — do not block tab switch with full await when fresh
  loadHome()
})
</script>

<style lang="less" scoped>
@import '@/styles/mixins.less';

.home {
  position: relative;
  overflow: hidden;
  padding: 8rpx 28rpx 40rpx;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(2rpx);
  opacity: 0.45;
  pointer-events: none;
}
.blob-a {
  width: 220rpx;
  height: 220rpx;
  background: #ffb3a8;
  top: -40rpx;
  right: -60rpx;
  animation: float-y 5s ease-in-out infinite;
}
.blob-b {
  width: 180rpx;
  height: 180rpx;
  background: #9aefc7;
  top: 360rpx;
  left: -70rpx;
  animation: float-y 6.5s ease-in-out infinite reverse;
}

.hero {
  position: relative;
  z-index: 1;
  padding: 12rpx 8rpx 28rpx;
}
.brand-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18rpx;
}
.brand-mark {
  font-size: 34rpx;
  color: @neu-accent;
}
.live-pill {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 10rpx 18rpx;
  font-size: 22rpx;
  color: @neu-muted;
}
.live-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: @neu-mint;
  box-shadow: 0 0 0 6rpx rgba(62, 207, 142, 0.25);
  animation: pulse-soft 1.6s ease-in-out infinite;
}
.hero-title {
  font-size: 48rpx;
  font-weight: 900;
  line-height: 1.25;
  letter-spacing: 0.02em;
}
.hero-sub {
  margin-top: 10rpx;
  color: @neu-muted;
  font-size: 26rpx;
}

.banner-stage {
  position: relative;
  z-index: 1;
  height: 340rpx;
  padding: 14rpx;
  margin-bottom: 28rpx;
}
.banner-swiper,
.slide {
  height: 100%;
  border-radius: 28rpx;
  overflow: hidden;
}
.slide {
  position: relative;
}
.banner-img {
  width: 100%;
  height: 100%;
  animation: kenburns 8s ease-in-out infinite alternate;
}
.banner-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}
.fallback-emoji {
  font-size: 72rpx;
  animation: float-y 3s ease-in-out infinite;
}
.fallback-title {
  color: #fff;
  font-size: 32rpx;
  font-weight: 800;
  text-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.2);
}
.slide-shade {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 40%, rgba(0, 0, 0, 0.45));
  pointer-events: none;
}
.slide-caption {
  position: absolute;
  left: 24rpx;
  right: 24rpx;
  bottom: 24rpx;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}
.cap-title {
  font-size: 30rpx;
  font-weight: 800;
  max-width: 70%;
}
.cap-hint {
  font-size: 22rpx;
  opacity: 0.85;
}
.dots {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 28rpx;
  display: flex;
  justify-content: center;
  gap: 10rpx;
  z-index: 2;
  pointer-events: none;
}
.dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.45);
}
.dot.on {
  width: 28rpx;
  border-radius: 999rpx;
  background: #fff;
}

.wallet {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  margin-bottom: 28rpx;
}
.wallet-label {
  font-size: 24rpx;
  color: @neu-muted;
}
.wallet-num {
  display: flex;
  align-items: baseline;
  gap: 10rpx;
  margin: 8rpx 0 12rpx;
}
.num {
  font-size: 64rpx;
  font-weight: 900;
  color: @neu-accent;
  line-height: 1;
}
.unit {
  font-size: 22rpx;
  font-weight: 800;
  letter-spacing: 0.12em;
  color: @neu-muted;
}
.wallet-desc {
  font-size: 24rpx;
  color: @neu-muted;
  max-width: 420rpx;
}
.coin {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, #ffd56a, #ff9f1a);
  color: #fff;
  font-size: 56rpx;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    10rpx 10rpx 20rpx #c9b89a,
    -8rpx -8rpx 18rpx #fff;
}
.float {
  animation: float-y 3.2s ease-in-out infinite;
}

.grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24rpx;
}
.tile {
  padding: 28rpx;
}
.tile.wide {
  grid-column: 1 / -1;
}
.tile-icon {
  font-size: 48rpx;
  margin-bottom: 12rpx;
}
.tile-name {
  font-size: 30rpx;
  font-weight: 800;
}
.tile-desc {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: @neu-muted;
}
.tile-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.tile-arrow {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: @neu-bg;
  box-shadow:
    6rpx 6rpx 12rpx @neu-shadow,
    -6rpx -6rpx 12rpx @neu-light;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  color: @neu-accent;
}
.pulse {
  animation: pulse-soft 2s ease-in-out infinite;
}
</style>
