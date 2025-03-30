import websocketService from '@/utils/websocket'

// 登出函数
export function logout() {
  // 断开WebSocket连接
  websocketService.disconnect()
  // 清除token
  sessionStorage.removeItem('token')
  // 重定向到登录页
  window.location.href = '/login'
} 