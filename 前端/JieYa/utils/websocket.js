class WebSocketService {
  constructor() {
    this.connected = false;
    this.url = null;
    this.token = null;
    this.socketTask = null;
    this.pingTimer = null;
    this.reconnectTimer = null;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectInterval = 5000;
    this.pingInterval = 15000;
    this.listeners = {
      open: [],
      message: [],
      error: [],
      close: [],
      reconnect_failed: []
    };
  }

  // 连接WebSocket
  connect(url, token) {
    if (this.connected) {
      console.log("WebSocket 已连接");
      return;
    }
    
    this.close();
    this.url = url.replace('http://', 'ws://').replace('https://', 'wss://');
    this.token = token;
    
    console.log("正在连接WebSocket...", this.url);
    
    try {
      // 创建WebSocket连接
      this.socketTask = uni.connectSocket({
        url: this.token ? `${this.url}?token=${this.token}` : this.url,
        header: {
          'Authorization': `Bearer ${this.token}`
        },
        success: () => {
          console.log("WebSocket连接请求已发送");
        },
        fail: (err) => {
          console.error("WebSocket连接请求失败:", err);
          this._triggerEvent("error", err);
          this._attemptReconnect();
        }
      });
      
      // 监听WebSocket事件
      this.socketTask.onOpen(this._onOpen.bind(this));
      this.socketTask.onMessage(this._onMessage.bind(this));
      this.socketTask.onClose(this._onClose.bind(this));
      this.socketTask.onError(this._onError.bind(this));
    } catch (e) {
      console.error("创建WebSocket连接失败:", e);
      this._attemptReconnect();
    }
  }
  
  // 连接成功回调
  _onOpen(res) {
    console.log("WebSocket连接已打开:", res);
    this.connected = true;
    this.reconnectAttempts = 0;
    this._startHeartbeat();
    this._triggerEvent("open", res);
  }
  
  // 接收消息回调
  _onMessage(res) {
    try {
      let data = res.data;
      if (typeof data === "string") {
        try {
          data = JSON.parse(data);
        } catch (e) {
          // 如果不是JSON格式，保持原样
        }
      }
      console.log("收到WebSocket消息:", data);
      this._triggerEvent("message", data);
    } catch (e) {
      console.error("处理WebSocket消息出错:", e);
    }
  }
  
  // 连接关闭回调
  _onClose(res) {
    console.log("WebSocket连接已关闭:", res);
    this.connected = false;
    this._stopHeartbeat();
    this._triggerEvent("close", res);
    this._attemptReconnect();
  }
  
  // 连接错误回调
  _onError(err) {
    console.error("WebSocket连接错误:", err);
    this.connected = false;
    this._triggerEvent("error", err);
  }
  
  // 发送消息
  send(data) {
    if (!this.connected || !this.socketTask) {
      console.error("WebSocket未连接，无法发送消息");
      return false;
    }
    
    try {
      const message = typeof data === "string" ? data : JSON.stringify(data);
      this.socketTask.send({
        data: message,
        success: () => {
          console.log("WebSocket消息发送成功");
        },
        fail: (err) => {
          console.error("WebSocket消息发送失败:", err);
          if (this.connected) {
            this.connected = false;
            this._attemptReconnect();
          }
        }
      });
      return true;
    } catch (e) {
      console.error("发送WebSocket消息出错:", e);
      return false;
    }
  }
  
  // 关闭连接
  close() {
    this._stopHeartbeat();
    clearTimeout(this.reconnectTimer);
    
    if (this.socketTask) {
      try {
        this.socketTask.close({
          code: 1000,
          reason: "用户主动关闭",
          success: () => {
            console.log("WebSocket连接已关闭");
          },
          fail: (err) => {
            console.error("关闭WebSocket连接失败:", err);
          },
          complete: () => {
            this.connected = false;
            this.socketTask = null;
          }
        });
      } catch (e) {
        console.error("关闭WebSocket出错:", e);
        this.connected = false;
        this.socketTask = null;
      }
    }
  }
  
  // 开始心跳
  _startHeartbeat() {
    this._stopHeartbeat();
    this.pingTimer = setInterval(() => {
      if (this.connected) {
        this.send({ type: "heartbeat", timestamp: Date.now() });
      }
    }, this.pingInterval);
  }
  
  // 停止心跳
  _stopHeartbeat() {
    if (this.pingTimer) {
      clearInterval(this.pingTimer);
      this.pingTimer = null;
    }
  }
  
  // 尝试重连
  _attemptReconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
    }
    
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
      
      this.reconnectTimer = setTimeout(() => {
        this.connect(this.url, this.token);
      }, this.reconnectInterval);
    } else {
      console.log("达到最大重连次数，放弃重连");
      this._triggerEvent("reconnect_failed");
    }
  }
  
  // 添加事件监听
  on(event, callback) {
    if (!this.listeners[event]) {
      this.listeners[event] = [];
    }
    this.listeners[event].push(callback);
    return this;
  }
  
  // 移除事件监听
  off(event, callback) {
    if (!this.listeners[event]) return this;
    
    if (!callback) {
      this.listeners[event] = [];
    } else {
      this.listeners[event] = this.listeners[event].filter(cb => cb !== callback);
    }
    return this;
  }
  
  // 触发事件
  _triggerEvent(event, data) {
    if (!this.listeners[event]) return;
    
    this.listeners[event].forEach(callback => {
      try {
        callback(data);
      } catch (e) {
        console.error(`事件 ${event} 处理错误:`, e);
      }
    });
  }
  
  // 检查是否已连接
  isConnected() {
    return this.connected;
  }
}

// 创建单例
const webSocketService = new WebSocketService();
export default webSocketService; 