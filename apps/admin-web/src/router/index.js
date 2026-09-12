import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '概览' }
      },
      {
        path: 'banners',
        name: 'Banners',
        component: () => import('@/views/cms/Banners.vue'),
        meta: { title: 'Banner 管理' }
      },
      {
        path: 'blocks',
        name: 'Blocks',
        component: () => import('@/views/cms/Blocks.vue'),
        meta: { title: '内容块' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/user/Users.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'points',
        name: 'Points',
        component: () => import('@/views/points/Ledger.vue'),
        meta: { title: '积分流水' }
      },
      {
        path: 'lottery',
        name: 'Lottery',
        component: () => import('@/views/lottery/Activities.vue'),
        meta: { title: '抽奖活动' }
      },
      {
        path: 'redeem-skus',
        name: 'RedeemSkus',
        component: () => import('@/views/redeem/Skus.vue'),
        meta: { title: '兑换商品' }
      },
      {
        path: 'redeem-orders',
        name: 'RedeemOrders',
        component: () => import('@/views/redeem/Orders.vue'),
        meta: { title: '兑换订单' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (!to.meta.public && !userStore.token) {
    next('/login')
  } else if (to.path === '/login' && userStore.token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
