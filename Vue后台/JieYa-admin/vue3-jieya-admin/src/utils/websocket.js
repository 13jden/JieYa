import { useWebsocketStore } from '@/stores/websocket'

class WebSocketService {
  constructor() {
    this.socket = null
    this.connected = false
    this.reconnecting = false
    this.reconnectTimeout = null
    this.heartbeatInterval = null
    this.listeners = {}
  }

  // 初始化WebSocket连接
  connect() {
    // 获取保存在sessionStorage中的token
    const token = sessionStorage.getItem('token')
    if (!token) {
      console.error('未找到token，无法建立WebSocket连接')
      return
    }
    
    try {
      // 创建原生WebSocket连接，带上token
      const wsUrl = `ws://localhost:8082/websocket?token=${token}`
      this.socket = new WebSocket(wsUrl)
      
      // 设置事件处理器
      this.socket.onopen = this.onConnected.bind(this)
      this.socket.onmessage = this.onMessage.bind(this)
      this.socket.onerror = this.onError.bind(this)
      this.socket.onclose = this.onClose.bind(this)
    } catch (error) {
      console.error('WebSocket连接失败:', error)
      this.scheduleReconnect()
    }
  }

  // 连接成功回调
  onConnected() {
    this.connected = true
    this.reconnecting = false
    console.log('WebSocket连接成功')
    
    // 更新Pinia状态（尝试获取store实例）
    try {
      const websocketStore = useWebsocketStore()
      websocketStore.setConnected(true)
    } catch (error) {
      console.warn('无法更新WebSocket状态:', error)
    }
    
    // 设置心跳检测
    this.startHeartbeat()
    
    // 触发onConnect监听器
    this.triggerListeners('connect')
  }

  // 接收消息回调
  onMessage(event) {
    console.log('收到WebSocket消息:', event.data)
    
    try {
      // 尝试解析JSON消息
      const data = JSON.parse(event.data)
      
      // 根据消息类型处理不同的消息
      if (data.type != 'CONNECT' && data.type != 'heaer_beat') {
        
        // 处理其他类型消息
        console.log('收到消息:', data)
        this.triggerListeners('message', data)
      }
    } catch (error) {
      console.error('解析消息失败:', error)
      this.triggerListeners('rawMessage', event.data)
    }
  }

  // 错误处理回调
  onError(error) {
    console.error('WebSocket错误:', error)
    this.triggerListeners('error', error)
  }

  // 连接关闭回调
  onClose(event) {
    this.connected = false
    console.log(`WebSocket连接关闭: ${event.code} - ${event.reason}`)
    
    // 更新Pinia状态
    try {
      const websocketStore = useWebsocketStore()
      websocketStore.setConnected(false)
    } catch (error) {
      console.warn('无法更新WebSocket状态:', error)
    }
    
    // 停止心跳
    this.stopHeartbeat()
    
    // 触发断开连接事件
    this.triggerListeners('disconnect')
    
    // 如果不是正常关闭，则尝试重连
    if (event.code !== 1000) {
      this.scheduleReconnect()
    }
  }

  // 发送消息
  send(data) {
    if (!this.connected || !this.socket) {
      console.error('WebSocket未连接，无法发送消息')
      return false
    }
    
    try {
      const message = typeof data === 'string' ? data : JSON.stringify(data)
      this.socket.send(message)
      return true
    } catch (error) {
      console.error('发送消息失败:', error)
      return false
    }
  }

  // 断开连接
  disconnect() {
    if (this.socket) {
      if (this.connected) {
        try {
          this.socket.close(1000, 'Normal closure')
        } catch (error) {
          console.error('断开WebSocket连接失败:', error)
        }
      }
      this.socket = null
    }
    
    this.connected = false
    this.stopHeartbeat()
    
    // 更新Pinia状态
    try {
      const websocketStore = useWebsocketStore()
      websocketStore.setConnected(false)
    } catch (error) {
      console.warn('无法更新WebSocket状态:', error)
    }
  }

  // 开始心跳检测
  startHeartbeat() {
    this.stopHeartbeat()
    this.heartbeatInterval = setInterval(() => {
      if (this.connected) {
        // 发送心跳消息
        this.send({ type: 'heartbeat', timestamp: Date.now() })
      } else {
        this.stopHeartbeat()
      }
    }, 30000) // 每30秒发送一次心跳
  }

  // 停止心跳检测
  stopHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }
  }

  // 安排重新连接
  scheduleReconnect() {
    if (!this.reconnecting) {
      this.reconnecting = true
      console.log('准备重新连接WebSocket...')
      
      // 清除旧的重连计时器
      if (this.reconnectTimeout) {
        clearTimeout(this.reconnectTimeout)
      }
      
      // 设置5秒后重连
      this.reconnectTimeout = setTimeout(() => {
        console.log('尝试重新连接WebSocket...')
        this.connect()
      }, 5000) // 5秒后重连
    }
  }

  // 添加事件监听器
  addListener(event, callback) {
    if (!this.listeners[event]) {
      this.listeners[event] = []
    }
    this.listeners[event].push(callback)
  }

  // 移除事件监听器
  removeListener(event, callback) {
    if (this.listeners[event]) {
      this.listeners[event] = this.listeners[event].filter(cb => cb !== callback)
    }
  }

  // 触发监听器
  triggerListeners(event, data) {
    if (this.listeners[event]) {
      this.listeners[event].forEach(callback => callback(data))
    }
  }

  // 添加事件发射方法
  emit(event, data) {
    if (this.listeners[event]) {
      this.listeners[event].forEach(callback => {
        callback(data);
      });
    }
  }
}

// 创建单例
const websocketService = new WebSocketService()

export default websocketService 