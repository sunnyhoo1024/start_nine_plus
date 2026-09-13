import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/** 与后端 ErrorCode 对齐：成功码为 200 */
export const ApiCode = {
  OK: 200,
  UNAUTHORIZED: 401
}

const request = axios.create({
  baseURL: '/admin/api',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res && typeof res.code === 'number') {
      if (res.code === ApiCode.OK) {
        return res.data
      }
      const msg = res.message || res.data?.errorMsg || '请求失败'
      ElMessage.error(msg)
      if (res.code === ApiCode.UNAUTHORIZED) {
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_user')
        router.push('/login')
      }
      const err = new Error(msg)
      err.code = res.code
      err.data = res.data
      return Promise.reject(err)
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    const msg = error.response?.data?.message || error.message || '网络错误'
    if (status === 401) {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_user')
      router.push('/login')
    }
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
