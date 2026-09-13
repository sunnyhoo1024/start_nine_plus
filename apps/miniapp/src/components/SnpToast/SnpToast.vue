<template>
  <view v-if="visible" class="snp-toast" @click="onClose" @touchmove.stop.prevent="noop">
    <view class="card" :class="'is-' + type" @click.stop>
      <text class="deco">注意</text>
      <view class="art">
        <image class="art-img" src="/static/toast/jojo-bg.png" mode="aspectFill" />
        <view class="art-fade" />
      </view>
      <view class="body">
        <text class="eyebrow">{{ eyebrow }}</text>
        <text class="title">{{ title }}</text>
        <text class="subtitle">{{ subtitle }}</text>
        <view class="bar" />
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { hideToast, TOAST_EVENT } from '@/utils/toast'

const visible = ref(false)
const title = ref('')
const subtitle = ref('')
const type = ref('error')

const eyebrow = computed(() => {
  if (type.value === 'success') return '成功'
  if (type.value === 'info') return '提示'
  return '出错了'
})

let hideTimer = null

function noop() {}

function onPayload(payload) {
  if (!payload || payload.visible === false) {
    visible.value = false
    return
  }
  title.value = payload.title || ''
  subtitle.value = payload.subtitle || ''
  type.value = payload.type || 'error'
  visible.value = true

  if (hideTimer) clearTimeout(hideTimer)
  hideTimer = setTimeout(() => {
    visible.value = false
    hideTimer = null
  }, payload.duration || 3000)
}

function onClose() {
  visible.value = false
  hideToast()
}

onMounted(() => {
  uni.$on(TOAST_EVENT, onPayload)
})

onUnmounted(() => {
  uni.$off(TOAST_EVENT, onPayload)
  if (hideTimer) clearTimeout(hideTimer)
})
</script>

<style lang="less" scoped>
.snp-toast {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 100000;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 160rpx 32rpx 0;
  background: rgba(8, 12, 28, 0.45);
}

.card {
  position: relative;
  width: 100%;
  max-width: 680rpx;
  min-height: 220rpx;
  display: flex;
  overflow: hidden;
  border-radius: 12rpx;
  border: 6rpx solid #0b1230;
  background-color: #16356f;
  box-shadow:
    0 0 0 4rpx #f4e7b0,
    12rpx 18rpx 0 rgba(11, 18, 48, 0.55);
  animation: toast-in 0.35s ease-out;
}

.card.is-success {
  background-color: #14523a;
  box-shadow:
    0 0 0 4rpx #9dffc8,
    12rpx 18rpx 0 rgba(8, 40, 28, 0.5);
}

.card.is-info {
  background-color: #1a3a7a;
}

.deco {
  position: absolute;
  right: 20rpx;
  top: 12rpx;
  z-index: 3;
  font-size: 22rpx;
  letter-spacing: 4rpx;
  color: rgba(244, 231, 176, 0.7);
  font-weight: 900;
  transform: rotate(8deg);
}

.art {
  position: relative;
  width: 210rpx;
  flex-shrink: 0;
  overflow: hidden;
  background: #13284f;
}

.art-img {
  width: 210rpx;
  height: 220rpx;
  display: block;
}

.art-fade {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 80rpx;
  background: linear-gradient(90deg, transparent, #16356f);
}

.body {
  flex: 1;
  min-width: 0;
  padding: 28rpx 28rpx 28rpx 12rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.eyebrow {
  font-size: 22rpx;
  font-weight: 900;
  letter-spacing: 4rpx;
  color: #f4e7b0;
  margin-bottom: 10rpx;
}

.title {
  font-size: 34rpx;
  line-height: 1.4;
  font-weight: 900;
  color: #ffffff;
}

.subtitle {
  margin-top: 10rpx;
  font-size: 22rpx;
  color: rgba(200, 220, 255, 0.9);
}

.bar {
  margin-top: 18rpx;
  width: 120rpx;
  height: 8rpx;
  background: #ff4d3a;
}

@keyframes toast-in {
  from {
    transform: translateY(-24rpx) scale(0.94);
  }
  to {
    transform: translateY(0) scale(1);
  }
}
</style>
