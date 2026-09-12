<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="brand">Start Nine Plus</div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#1f2d3d"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">概览</el-menu-item>
        <el-menu-item index="/banners">Banner 管理</el-menu-item>
        <el-menu-item index="/blocks">内容块</el-menu-item>
        <el-menu-item index="/users">用户管理</el-menu-item>
        <el-menu-item index="/points">积分流水</el-menu-item>
        <el-menu-item index="/lottery">抽奖活动</el-menu-item>
        <el-menu-item index="/redeem-skus">兑换商品</el-menu-item>
        <el-menu-item index="/redeem-orders">兑换订单</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ route.meta.title || '管理后台' }}</span>
        <div class="header-right">
          <span class="user">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员' }}</span>
          <el-button link type="primary" @click="userStore.logout()">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
</script>

<style scoped>
.layout {
  height: 100%;
}
.aside {
  background: #1f2d3d;
  color: #fff;
}
.brand {
  height: 56px;
  line-height: 56px;
  text-align: center;
  font-weight: 600;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user {
  color: #606266;
}
.main {
  padding: 16px;
}
</style>
