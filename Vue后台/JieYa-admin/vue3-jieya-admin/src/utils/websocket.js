import { useWebsocketStore } from '@/stores/websocket'

class WebSocketService {
  constructor() {
    this.socket = null
    this.connected = false
    this.reconnecting = false
    this.reconnectTimeout = null
    this.heartbeatInterval = null
    this.listeners = {}
    this.lastHeartbeatResponse = Date.now() // 记录最后一次心跳响应时间
    this.manualDisconnect = false // 用于标记是否是用户主动断开
  }

  // 初始化连接
  connect() {
    if (this.socket) {
      console.log('WebSocket已连接，无需重新连接')
      return
    }

    // 重置手动断开标志
    this.manualDisconnect = false

    // 获取token
    const token = sessionStorage.getItem('token')
    if (!token) {
      console.error('未找到token，无法建立WebSocket连接')
      // 重定向到登录页面
      window.location.href = '/login'
      return
    }

    try {
      const wsUrl = `ws://localhost:8082/websocket?token=${token}`
      this.socket = new WebSocket(wsUrl)
      
      this.socket.onopen = this.onOpen.bind(this)
      this.socket.onmessage = this.onMessage.bind(this)
      this.socket.onclose = this.onClose.bind(this)
      this.socket.onerror = this.onError.bind(this)
    } catch (error) {
      console.error('初始化WebSocket失败:', error)
    }
  }

  // 连接打开回调
  onOpen() {
    this.connected = true
    this.reconnecting = false
    console.log('WebSocket连接已建立')
    
    // 更新Pinia状态
    try {
      const websocketStore = useWebsocketStore()
      websocketStore.setConnected(true)
    } catch (error) {
      console.warn('无法更新WebSocket状态:', error)
    }
    
    // 启动心跳检测
    this.startHeartbeat()
    
    // 触发连接事件
    this.triggerListeners('connect')
  }

  // 接收消息回调
  onMessage(event) {
    // 记录原始消息
    console.log('收到WebSocket消息:', event.data)
    
    // 检查消息是否为空
    if (!event.data || event.data.trim() === '') {
      console.log('收到空消息，忽略处理')
      return // 空消息直接返回，不触发任何事件
    }
    
    try {
      // 尝试解析JSON消息
      const data = JSON.parse(event.data)
      
      // 心跳响应处理 - 增加对pong的支持
      if (data && (data.type === 'heartbeat_response' || data.type === 'HEARTBEAT' || 
          data.type === 'pong' || data.type === 'PONG')) {
        console.log('收到心跳响应')
        this.lastHeartbeatResponse = Date.now() // 更新最后响应时间
        return // 不再传递心跳消息给业务层
      } 
      
      // 业务消息处理
      this.triggerListeners('message', data)
    } catch (error) {
      console.warn('解析消息失败，但保持连接:', error)
      // 即使解析失败也不影响连接
      if (event.data && event.data.trim() !== '') {
        // 只有非空消息才触发rawMessage事件
        this.triggerListeners('rawMessage', event.data)
      }
    }
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
    
    // 只有在非手动断开的情况下才尝试重连，无论关闭代码和原因是什么
    if (!this.manualDisconnect) {
      console.log('非手动断开，尝试重新连接...')
      this.scheduleReconnect()
    } else {
      console.log('手动断开连接，不进行重连')
      this.manualDisconnect = false // 重置标志
    }
  }

  // 连接错误回调
  onError(error) {
    console.error('WebSocket连接错误:', error)
    this.triggerListeners('error', error)
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

  // 断开连接 - 用户主动调用
  disconnect() {
    // 设置手动断开标志
    this.manualDisconnect = true
    
    if (this.socket) {
      if (this.connected) {
        try {
          this.socket.close(1000, 'User logout')
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
    
    // 设置初始最后响应时间
    this.lastHeartbeatResponse = Date.now()
    
    this.heartbeatInterval = setInterval(() => {
      if (this.connected) {
        const now = Date.now()
        // 如果超过45秒没有收到心跳响应，主动重连
        if (now - this.lastHeartbeatResponse > 45000) {
          console.warn('超过45秒未收到心跳响应，主动重连...')
          this.reconnect()
          return
        }
        
        // 使用大写HEARTBEAT与服务器匹配，移除可能引起问题的时间戳
        this.send({ type: 'heartbeat' })
        console.log('已发送心跳消息')
      } else {
        this.stopHeartbeat()
      }
    }, 10000)
  }

  // 停止心跳检测
  stopHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }
  }

  // 主动重连
  reconnect() {
    if (this.socket) {
      try {
        this.socket.close()
      } catch (error) {
        console.error('关闭现有连接失败:', error)
      }
      this.socket = null
    }
    this.connected = false
    this.stopHeartbeat()
    
    // 立即尝试重连
    this.connect()
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
      
      // 设置3秒后重连
      this.reconnectTimeout = setTimeout(() => {
        console.log('尝试重新连接WebSocket...')
        this.connect()
      }, 3000) // 缩短为3秒后重连
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

  // 触发事件
  triggerListeners(event, data) {
    if (this.listeners[event]) {
      this.listeners[event].forEach(callback => {
        try {
          callback(data)
        } catch (error) {
          console.error(`执行${event}事件监听器出错:`, error)
        }
      })
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

// 单例模式
const websocketService = new WebSocketService()
export default websocketService 