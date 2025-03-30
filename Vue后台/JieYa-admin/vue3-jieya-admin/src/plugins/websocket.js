import websocketService from '@/utils/websocket'

export default {
  install(app) {
    // 将websocketService实例添加到全局属性中
    app.config.globalProperties.$websocket = websocketService
    
    // 同时也暴露给组合式API
    app.provide('websocket', websocketService)
  }
} 