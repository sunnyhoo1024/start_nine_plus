<template>
  <view class="page">
    <view v-if="!activities.length" class="card empty">暂无抽奖活动</view>
    <view v-for="item in activities" :key="item.id" class="card">
      <view class="name">{{ item.name }}</view>
      <view class="meta">消耗 {{ item.costPoints || 0 }} 积分</view>
      <view v-if="item.description" class="desc">{{ item.description }}</view>
      <button
        class="btn-primary"
        :loading="drawingId === item.id"
        :disabled="drawingId === item.id"
        @click="onDraw(item.id)"
      >
        立即抽奖
      </button>
    </view>

    <view v-if="result" class="card result">
      <view class="name">抽奖结果</view>
      <view>奖品：{{ result.prizeName || result.name || '谢谢参与' }}</view>
      <view v-if="result.pointsValue">积分：{{ result.pointsValue }}</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { get, post } from '@/utils/request'
import { ensureLogin } from '@/utils/auth'

const activities = ref([])
const drawingId = ref(null)
const result = ref(null)

async function loadActivities() {
  try {
    const data = await get('/lottery/activities', {}, { needAuth: false })
    activities.value = data?.records || data?.list || data || []
  } catch (e) {
    // ignore
  }
}

async function onDraw(id) {
  try {
    await ensureLogin()
    drawingId.value = id
    result.value = null
    result.value = await post('/lottery/draw', { activityId: id })
    uni.showToast({ title: '抽奖成功', icon: 'success' })
  } catch (e) {
    // toasted
  } finally {
    drawingId.value = null
  }
}

onShow(() => {
  loadActivities()
})
</script>

<style scoped>
.name {
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 8rpx;
}
.meta,
.desc {
  color: #909399;
  margin-bottom: 12rpx;
}
.empty {
  text-align: center;
  color: #909399;
}
.result {
  border: 2rpx solid #1f2d3d;
}
</style>
