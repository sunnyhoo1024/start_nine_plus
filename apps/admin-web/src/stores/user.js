import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, getMe } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('admin_user') || 'null'))

  async function login(username, password) {
    const data = await loginApi({ username, password })
    token.value = data.token
    userInfo.value = {
      userId: data.userId,
      username: data.username,
      nickname: data.nickname,
      roles: data.roles,
      permissions: data.permissions
    }
    localStorage.setItem('admin_token', token.value)
    localStorage.setItem('admin_user', JSON.stringify(userInfo.value))
    return data
  }

  async function fetchMe() {
    const data = await getMe()
    userInfo.value = data
    localStorage.setItem('admin_user', JSON.stringify(data))
    return data
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
    router.push('/login')
  }

  return { token, userInfo, login, fetchMe, logout }
})
