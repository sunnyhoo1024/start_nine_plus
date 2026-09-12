<template>
  <view class="page">
    <view class="card">
      <view class="label">当前积分</view>
      <view class="balance">{{ balance }}</view>
      <button class="btn-primary" size="mini" @click="refresh">刷新</button>
    </view>

    <view class="card">
      <view class="title">积分流水</view>
      <view v-if="!ledger.length" class="empty">暂无流水</view>
      <view v-for="item in ledger" :key="item.id" class="row">
        <view>
          <view class="biz">{{ item.bizType }}</view>
          <view class="time">{{ item.createdAt }}</view>
        </view>
        <view :class="['amount', item.changeAmount > 0 ? 'plus' : 'minus']">
          {{ item.changeAmount > 0 ? '+' : '' }}{{ item.changeAmount }}
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { get } from '@/utils/request'
import { ensureLogin } from '@/utils/auth'

const balance = ref('--')
const ledger = ref([])

async function refresh() {
  try {
    await ensureLogin()
    const bal = await get('/points/balance')
    balance.value = typeof bal === 'object' && bal != null && bal.balance != null ? bal.balance : bal
    const page = await get('/points/ledger', { page: 1, size: 50 })
    ledger.value = page?.records || page?.list || page || []
  } catch (e) {
    // toasted
  }
}

onShow(() => {
  refresh()
})
</script>

<style scoped>
.label {
  color: #909399;
}
.balance {
  font-size: 56rpx;
  font-weight: 700;
  margin: 12rpx 0 20rpx;
  color: #1f2d3d;
}
.title {
  font-weight: 600;
  margin-bottom: 16rpx;
}
.row {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #ebeef5;
}
.biz {
  font-weight: 500;
}
.time {
  color: #c0c4cc;
  font-size: 22rpx;
  margin-top: 4rpx;
}
.plus {
  color: #67c23a;
}
.minus {
  color: #f56c6c;
}
.empty {
  color: #909399;
  text-align: center;
  padding: 24rpx 0;
}
</style>
