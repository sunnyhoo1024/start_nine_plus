<template>
  <view class="page lottery">
    <view class="stage neu-out">
      <view class="pointer" />
      <view class="wheel-wrap" :class="{ spinning: spinning }">
        <view class="wheel">
          <view v-for="(seg, i) in segments" :key="i" class="seg" :style="segStyle(i)">
            <text>{{ seg }}</text>
          </view>
          <view class="hub neu-out">
            <text class="hub-star">★</text>
          </view>
        </view>
      </view>
      <view class="hint">轻触下方按钮，让软粘土转盘疯狂转起来</view>
    </view>

    <view v-if="!activities.length" class="empty neu-in">暂无抽奖活动，稍后再来</view>

    <view v-for="item in activities" :key="item.id" class="act neu-out">
      <view class="act-top">
        <view>
          <view class="name">{{ item.name }}</view>
          <view class="meta">消耗 {{ item.costPoints || 0 }} 积分 / 次</view>
        </view>
        <view class="cost-badge neu-in">GO</view>
      </view>
      <view v-if="item.description" class="desc">{{ item.description }}</view>
      <button
        class="neu-btn neu-btn-accent draw-btn"
        :disabled="spinning || drawingId === item.id"
        @click="onDraw(item.id)"
      >
        {{ spinning ? '转盘狂热中…' : '立刻抽奖' }}
      </button>
    </view>

    <view v-if="showResult" class="overlay" @click="closeResult">
      <view class="confetti" v-for="n in 14" :key="n" :style="confettiStyle(n)" />
      <view class="result-card neu-out pop" @click.stop>
        <view class="result-emoji">{{ resultEmoji }}</view>
        <view class="result-title">抽中啦！</view>
        <view class="result-name">{{ result?.prizeName || result?.name || '谢谢参与' }}</view>
        <view v-if="result?.pointsValue" class="result-pts">+{{ result.pointsValue }} 积分</view>
        <button class="neu-btn neu-btn-accent" @click="closeResult">收下好运</button>
      </view>
    </view>
    <SnpToast />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { ensureLogin } from '@/utils/auth'
import { invalidateCache, staleWhileRevalidate } from '@/utils/cache'
import { showToast } from '@/utils/toast'
import SnpToast from '@/components/SnpToast/SnpToast.vue'

const activities = ref([])
const drawingId = ref(null)
const spinning = ref(false)
const result = ref(null)
const showResult = ref(false)

const segments = ['积分', '谢谢', '礼品', '星星', '再来', '惊喜', '彩蛋', '大奖']

const resultEmoji = computed(() => {
  const t = (result.value?.prizeType || '').toUpperCase()
  if (t === 'POINTS') return '✨'
  if (t === 'PHYSICAL') return '🎁'
  if (t === 'THANKS') return '🤝'
  return '🎉'
})

function segStyle(i) {
  const angle = i * 45
  return {
    transform: `rotate(${angle}deg) skewY(-45deg)`
  }
}

function confettiStyle(n) {
  const colors = ['#ff4d3a', '#ffd56a', '#3ecf8e', '#6aa8ff', '#ff8fab']
  return {
    left: `${(n * 7) % 100}%`,
    background: colors[n % colors.length],
    animationDelay: `${(n % 7) * 0.08}s`,
    animationDuration: `${1.6 + (n % 5) * 0.15}s`
  }
}

async function loadActivities() {
  try {
    const { data } = await staleWhileRevalidate(
      'lottery-activities',
      () => get('/lottery/activities', {}, { needAuth: false }),
      60000,
      (fresh) => {
        activities.value = fresh?.records || fresh?.list || fresh || []
      }
    )
    activities.value = data?.records || data?.list || data || []
  } catch (e) {
    // ignore
  }
}

async function onDraw(id) {
  if (spinning.value) return
  try {
    await ensureLogin()
    drawingId.value = id
    result.value = null
    showResult.value = false
    spinning.value = true
    const dataPromise = post('/lottery/draw', { activityId: id }, { silent: true })
    await new Promise((r) => setTimeout(r, 2200))
    result.value = await dataPromise
    spinning.value = false
    showResult.value = true
    invalidateCache('home')
    invalidateCache('points-balance')
    invalidateCache('points-ledger')
  } catch (e) {
    spinning.value = false
    const msg = (e && (e.errorMsg || e.message)) || '抽奖失败'
    showToast({ title: msg, type: 'error' })
  } finally {
    drawingId.value = null
  }
}

function closeResult() {
  showResult.value = false
}

onLoad(() => {
  loadActivities()
})

onShow(() => {
  loadActivities()
})
</script>

<style lang="less" scoped>
@import '@/styles/mixins.less';

.lottery {
  padding: 24rpx 28rpx 48rpx;
}

.stage {
  position: relative;
  padding: 40rpx 24rpx 32rpx;
  margin-bottom: 28rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.pointer {
  width: 0;
  height: 0;
  border-left: 18rpx solid transparent;
  border-right: 18rpx solid transparent;
  border-top: 36rpx solid @neu-accent;
  margin-bottom: 8rpx;
  filter: drop-shadow(0 4rpx 4rpx rgba(0, 0, 0, 0.12));
  z-index: 2;
}
.wheel-wrap {
  width: 460rpx;
  height: 460rpx;
  border-radius: 50%;
  padding: 18rpx;
  background: @neu-bg;
  box-shadow: inset 10rpx 10rpx 20rpx @neu-shadow, inset -10rpx -10rpx 20rpx @neu-light;
}
.wheel {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  overflow: hidden;
  background: conic-gradient(
    #ff6b57 0 45deg,
    #ffd56a 45deg 90deg,
    #6ee7b7 90deg 135deg,
    #6aa8ff 135deg 180deg,
    #ff8fab 180deg 225deg,
    #c4b5fd 225deg 270deg,
    #fbbf24 270deg 315deg,
    #fb7185 315deg 360deg
  );
  box-shadow: 8rpx 8rpx 18rpx @neu-shadow;
}
.spinning .wheel {
  animation: spin-wheel 0.45s linear infinite;
}
.seg {
  position: absolute;
  width: 50%;
  height: 50%;
  left: 50%;
  top: 0;
  transform-origin: 0% 100%;
  display: none;
}
.hub {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 110rpx;
  height: 110rpx;
  margin: -55rpx 0 0 -55rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.hub-star {
  color: @neu-accent;
  font-size: 44rpx;
  font-weight: 900;
}
.hint {
  margin-top: 24rpx;
  font-size: 24rpx;
  color: @neu-muted;
  text-align: center;
}

.act {
  padding: 28rpx;
  margin-bottom: 24rpx;
}
.act-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.name {
  font-size: 34rpx;
  font-weight: 900;
}
.meta {
  margin-top: 8rpx;
  color: @neu-muted;
  font-size: 24rpx;
}
.desc {
  margin: 16rpx 0;
  color: @neu-muted;
  font-size: 24rpx;
  line-height: 1.5;
}
.cost-badge {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  color: @neu-accent;
  font-size: 22rpx;
}
.draw-btn {
  margin-top: 8rpx;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 30rpx;
}
.empty {
  padding: 48rpx;
  text-align: center;
  color: @neu-muted;
  margin-bottom: 24rpx;
}

.overlay {
  position: fixed;
  inset: 0;
  background: rgba(42, 42, 42, 0.35);
  z-index: 99;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.confetti {
  position: absolute;
  top: 8%;
  width: 16rpx;
  height: 28rpx;
  border-radius: 6rpx;
  animation: confetti-fall 1.8s ease-in forwards;
}
.result-card {
  width: 78%;
  padding: 48rpx 36rpx;
  text-align: center;
  position: relative;
  z-index: 2;
}
.pop {
  animation: pop-in 0.45s cubic-bezier(0.2, 0.9, 0.3, 1.2);
}
.result-emoji {
  font-size: 80rpx;
  margin-bottom: 8rpx;
}
.result-title {
  font-size: 28rpx;
  color: @neu-muted;
}
.result-name {
  margin: 12rpx 0 8rpx;
  font-size: 40rpx;
  font-weight: 900;
}
.result-pts {
  color: @neu-accent;
  font-weight: 800;
  margin-bottom: 24rpx;
}
</style>
