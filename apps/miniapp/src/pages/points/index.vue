<template>
  <view class="page points">
    <view class="hero neu-out">
      <view class="label">可用积分</view>
      <view class="balance-wrap">
        <text class="balance">{{ displayBalance }}</text>
        <text class="unit">PTS</text>
      </view>
      <view class="bar neu-in">
        <view class="bar-fill" :style="{ width: barWidth }" />
      </view>
      <button class="neu-btn refresh" @click="refresh">刷新口袋</button>
    </view>

    <view class="list neu-out">
      <view class="list-title">星星去向</view>
      <view v-if="!ledger.length" class="empty">还没有流水，去抽奖或兑换玩玩</view>
      <view v-for="item in ledger" :key="item.id" class="row">
        <view class="left">
          <view class="icon neu-in">{{ iconFor(item.bizType) }}</view>
          <view>
            <view class="biz">{{ labelFor(item.bizType) }}</view>
            <view class="time">{{ item.createdAt }}</view>
          </view>
        </view>
        <view :class="['amount', item.changeAmount > 0 ? 'plus' : 'minus']">
          {{ item.changeAmount > 0 ? '+' : '' }}{{ item.changeAmount }}
        </view>
      </view>
    </view>
    <SnpToast />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { get } from '@/utils/request'
import { ensureLogin } from '@/utils/auth'
import { staleWhileRevalidate } from '@/utils/cache'
import SnpToast from '@/components/SnpToast/SnpToast.vue'

const balance = ref(0)
const ledger = ref([])
const animBalance = ref(0)

const displayBalance = computed(() =>
  balance.value === '--' || balance.value == null ? '--' : animBalance.value
)

const barWidth = computed(() => {
  const n = Number(balance.value) || 0
  const pct = Math.min(100, Math.max(8, (n % 1000) / 10))
  return `${pct}%`
})

function iconFor(type) {
  const t = (type || '').toUpperCase()
  if (t.includes('LOTTERY')) return '🎰'
  if (t.includes('REDEEM')) return '🎁'
  if (t.includes('ADMIN')) return '🛠️'
  return '★'
}

function labelFor(type) {
  const map = {
    ADMIN_ADJUST: '后台调账',
    LOTTERY_COST: '抽奖消耗',
    LOTTERY_REWARD: '抽奖奖励',
    REDEEM: '兑换扣减',
    REDEEM_REFUND: '兑换退回'
  }
  return map[type] || type || '变动'
}

function animateTo(target) {
  const end = Number(target) || 0
  const start = Number(animBalance.value) || 0
  const steps = 18
  let i = 0
  const timer = setInterval(() => {
    i += 1
    animBalance.value = Math.round(start + ((end - start) * i) / steps)
    if (i >= steps) {
      clearInterval(timer)
      animBalance.value = end
    }
  }, 24)
}

async function refresh() {
  try {
    await ensureLogin()
    const [{ data: bal }, { data: page }] = await Promise.all([
      staleWhileRevalidate(
        'points-balance',
        () => get('/points/balance'),
        20000,
        (fresh) => {
          const n = typeof fresh === 'object' && fresh != null && fresh.balance != null ? fresh.balance : fresh
          balance.value = n
          animateTo(n)
        }
      ),
      staleWhileRevalidate(
        'points-ledger',
        () => get('/points/ledger', { page: 1, size: 50 }),
        20000,
        (fresh) => {
          ledger.value = fresh?.records || fresh?.list || fresh || []
        }
      )
    ])
    const next = typeof bal === 'object' && bal != null && bal.balance != null ? bal.balance : bal
    balance.value = next
    animateTo(next)
    ledger.value = page?.records || page?.list || page || []
  } catch (e) {
    // toasted
  }
}

onLoad(() => {
  refresh()
})

onShow(() => {
  refresh()
})
</script>

<style lang="less" scoped>
@import '@/styles/mixins.less';

.points {
  padding: 24rpx 28rpx 48rpx;
}
.hero {
  padding: 36rpx 32rpx;
  margin-bottom: 24rpx;
  text-align: center;
}
.label {
  color: @neu-muted;
  font-size: 24rpx;
}
.balance-wrap {
  display: flex;
  justify-content: center;
  align-items: baseline;
  gap: 12rpx;
  margin: 16rpx 0 24rpx;
}
.balance {
  font-size: 88rpx;
  font-weight: 900;
  color: @neu-accent;
  line-height: 1;
}
.unit {
  font-size: 24rpx;
  font-weight: 800;
  letter-spacing: 0.12em;
  color: @neu-muted;
}
.bar {
  height: 18rpx;
  border-radius: 999rpx;
  overflow: hidden;
  margin-bottom: 28rpx;
}
.bar-fill {
  height: 100%;
  border-radius: 999rpx;
  background: linear-gradient(90deg, #ffb347, #ff4d3a);
  transition: width 0.6s ease;
}
.refresh {
  height: 72rpx;
  line-height: 72rpx;
  font-size: 26rpx;
}
.list {
  padding: 28rpx;
}
.list-title {
  font-size: 30rpx;
  font-weight: 900;
  margin-bottom: 12rpx;
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 22rpx 0;
  border-bottom: 2rpx solid rgba(0, 0, 0, 0.04);
}
.left {
  display: flex;
  align-items: center;
  gap: 18rpx;
}
.icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
}
.biz {
  font-weight: 700;
  font-size: 28rpx;
}
.time {
  margin-top: 6rpx;
  color: @neu-muted;
  font-size: 22rpx;
}
.amount {
  font-size: 32rpx;
  font-weight: 900;
}
.plus {
  color: @neu-mint;
}
.minus {
  color: @neu-accent;
}
.empty {
  text-align: center;
  color: @neu-muted;
  padding: 40rpx 0;
}
</style>
