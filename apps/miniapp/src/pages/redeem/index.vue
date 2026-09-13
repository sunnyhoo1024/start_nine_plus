<template>
  <view class="page redeem">
    <view class="intro neu-out">
      <view class="intro-title">兑换橱窗</view>
      <view class="intro-sub">像点餐一样挑一件小确幸，用星星买单</view>
    </view>

    <view v-if="!skus.length" class="empty neu-in">货架空空，晚点再逛</view>

    <view v-for="item in skus" :key="item.id" class="sku neu-out">
      <view class="cover-wrap neu-in">
        <image v-if="item.imageUrl" class="cover" :src="item.imageUrl" mode="aspectFill" />
        <view v-else class="cover-fallback">🎁</view>
      </view>
      <view class="body">
        <view class="name">{{ item.name }}</view>
        <view class="meta">库存 {{ item.stock }}</view>
        <view v-if="item.description" class="desc">{{ item.description }}</view>
        <view class="row">
          <view class="price">
            <text class="pts">{{ item.pointsPrice }}</text>
            <text class="unit">PTS</text>
          </view>
          <button
            class="neu-btn neu-btn-accent mini"
            :disabled="redeemingId === item.id"
            @click="onRedeem(item)"
          >
            {{ redeemingId === item.id ? '…' : '兑换' }}
          </button>
        </view>
      </view>
    </view>
    <SnpToast />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { ensureLogin } from '@/utils/auth'
import { invalidateCache, staleWhileRevalidate } from '@/utils/cache'
import SnpToast from '@/components/SnpToast/SnpToast.vue'

const skus = ref([])
const redeemingId = ref(null)

async function loadSkus() {
  try {
    const { data } = await staleWhileRevalidate(
      'redeem-skus',
      () => get('/redeem/skus', {}, { needAuth: false }),
      60000,
      (fresh) => {
        skus.value = fresh?.records || fresh?.list || fresh || []
      }
    )
    skus.value = data?.records || data?.list || data || []
  } catch (e) {
    // ignore
  }
}

function onRedeem(item) {
  uni.showModal({
    title: '确认兑换',
    content: `用 ${item.pointsPrice} 积分兑换「${item.name}」？`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await ensureLogin()
        redeemingId.value = item.id
        await post('/redeem', { skuId: item.id, quantity: 1 })
        uni.showToast({ title: '兑换成功', icon: 'success' })
        invalidateCache('redeem-skus')
        invalidateCache('home')
        invalidateCache('points-balance')
        invalidateCache('points-ledger')
        loadSkus()
      } catch (e) {
        // toasted
      } finally {
        redeemingId.value = null
      }
    }
  })
}

onLoad(() => {
  loadSkus()
})

onShow(() => {
  loadSkus()
})
</script>

<style lang="less" scoped>
@import '@/styles/mixins.less';

.redeem {
  padding: 24rpx 28rpx 48rpx;
}
.intro {
  padding: 32rpx;
  margin-bottom: 24rpx;
}
.intro-title {
  font-size: 40rpx;
  font-weight: 900;
}
.intro-sub {
  margin-top: 10rpx;
  color: @neu-muted;
  font-size: 24rpx;
}
.sku {
  display: flex;
  gap: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}
.cover-wrap {
  width: 168rpx;
  height: 168rpx;
  border-radius: 28rpx;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cover {
  width: 100%;
  height: 100%;
}
.cover-fallback {
  font-size: 64rpx;
}
.body {
  flex: 1;
  min-width: 0;
}
.name {
  font-size: 30rpx;
  font-weight: 800;
}
.meta,
.desc {
  color: @neu-muted;
  font-size: 22rpx;
  margin-top: 8rpx;
}
.row {
  margin-top: 18rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.price {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}
.pts {
  font-size: 40rpx;
  font-weight: 900;
  color: @neu-accent;
}
.unit {
  font-size: 20rpx;
  font-weight: 800;
  color: @neu-muted;
}
.mini {
  min-width: 140rpx;
  height: 64rpx;
  line-height: 64rpx;
  font-size: 26rpx;
  padding: 0 28rpx;
}
.empty {
  padding: 48rpx;
  text-align: center;
  color: @neu-muted;
}
</style>
