import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api', // 修改为相对路径，通过 vite 代理转发
  timeout: 5000 // 请求超时时间
})

// request 拦截器
service.interceptors.request.use(
  config => {
    // 从 sessionStorage 获取 token
    const token = sessionStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// response 拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 1) {
      ElMessage({
        message: res.message || '请求失败',
        type: 'error',
        duration: 5 * 1000
      })
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    console.error('响应错误:', error)
    if (error.response) {
      // 服务器返回了错误状态码
      const status = error.response.status
      const message = error.response.data?.message || '请求失败'
      
      switch (status) {
        case 400:
          ElMessage({
            message: '请求参数错误：' + message,
            type: 'error',
            duration: 5 * 1000
          })
          break
        case 401:
          ElMessage({
            message: '未授权，请重新登录',
            type: 'error',
            duration: 5 * 1000
          })
          // 清除 token 并跳转到登录页
          sessionStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 403:
          ElMessage({
            message: '拒绝访问',
            type: 'error',
            duration: 5 * 1000
          })
          break
        case 404:
          ElMessage({
            message: '请求的资源不存在',
            type: 'error',
            duration: 5 * 1000
          })
          break
        case 500:
          ElMessage({
            message: '服务器错误',
            type: 'error',
            duration: 5 * 1000
          })
          break
        default:
          ElMessage({
            message: message,
            type: 'error',
            duration: 5 * 1000
          })
      }
    } else if (error.request) {
      // 请求已经发出，但没有收到响应
      ElMessage({
        message: '网络错误，请检查网络连接',
        type: 'error',
        duration: 5 * 1000
      })
    } else {
      // 请求配置出错
      ElMessage({
        message: '请求配置错误：' + error.message,
        type: 'error',
        duration: 5 * 1000
      })
    }
    return Promise.reject(error)
  }
)

export default service 