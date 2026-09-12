<template>
  <view class="page">
    <view v-if="!skus.length" class="card empty">暂无可兑换商品</view>
    <view v-for="item in skus" :key="item.id" class="card sku">
      <image v-if="item.imageUrl" class="cover" :src="item.imageUrl" mode="aspectFill" />
      <view class="body">
        <view class="name">{{ item.name }}</view>
        <view class="meta">{{ item.pointsPrice }} 积分 · 库存 {{ item.stock }}</view>
        <view v-if="item.description" class="desc">{{ item.description }}</view>
        <button
          class="btn-primary"
          size="mini"
          :loading="redeemingId === item.id"
          @click="onRedeem(item)"
        >
          兑换
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { ensureLogin } from '@/utils/auth'

const skus = ref([])
const redeemingId = ref(null)

async function loadSkus() {
  try {
    const data = await get('/redeem/skus', {}, { needAuth: false })
    skus.value = data?.records || data?.list || data || []
  } catch (e) {
    // ignore
  }
}

function onRedeem(item) {
  uni.showModal({
    title: '确认兑换',
    content: `确认兑换「${item.name}」？`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await ensureLogin()
        redeemingId.value = item.id
        await post('/redeem', { skuId: item.id, quantity: 1 })
        uni.showToast({ title: '兑换成功', icon: 'success' })
        loadSkus()
      } catch (e) {
        // toasted
      } finally {
        redeemingId.value = null
      }
    }
  })
}

onShow(() => {
  loadSkus()
})
</script>

<style scoped>
.sku {
  display: flex;
  gap: 20rpx;
}
.cover {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}
.body {
  flex: 1;
}
.name {
  font-size: 30rpx;
  font-weight: 600;
}
.meta,
.desc {
  color: #909399;
  margin: 8rpx 0;
}
.empty {
  text-align: center;
  color: #909399;
}
</style>
