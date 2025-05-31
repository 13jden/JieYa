"use strict";
const common_vendor = require("../common/vendor.js");
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
    this.reconnectInterval = 3e3;
    this.pingInterval = 1e4;
    this.lastHeartbeatResponse = Date.now();
    this.manualDisconnect = false;
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
    this.manualDisconnect = false;
    this.close();
    this.url = url.replace("http://", "ws://").replace("https://", "wss://");
    this.token = token;
    const wsUrl = `${this.url}?token=${this.token}`;
    console.log("正在连接WebSocket...", wsUrl);
    try {
      this.socketTask = common_vendor.index.connectSocket({
        url: wsUrl,
        success: () => {
          console.log("WebSocket连接请求已发送");
        },
        fail: (err) => {
          console.error("WebSocket连接请求失败:", err);
          this._triggerEvent("error", err);
          this._attemptReconnect();
        }
      });
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
    this.lastHeartbeatResponse = Date.now();
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
        }
      }
      console.log("WebSocket收到原始消息:", data);
      if (data.type === "heartbeat_response" || data.type === "HEARTBEAT" || data.type === "pong") {
        console.log("收到心跳响应");
        this.lastHeartbeatResponse = Date.now();
        return;
      }
      this.processReceivedMessage(data);
    } catch (e) {
      console.error("处理WebSocket消息出错:", e);
    }
  }
  // 连接关闭回调
  _onClose(res) {
    console.log("WebSocket连接已关闭, 状态码:", res.code, "原因:", res.reason);
    this.connected = false;
    this._stopHeartbeat();
    this._triggerEvent("close", res);
    if (!this.manualDisconnect) {
      console.log("非手动断开，尝试重连...");
      this._attemptReconnect();
    } else {
      console.log("手动断开连接，不进行重连");
      this.manualDisconnect = false;
    }
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
      console.log("发送WebSocket消息:", data);
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
  // 主动关闭连接
  close() {
    this.manualDisconnect = true;
    this._stopHeartbeat();
    clearTimeout(this.reconnectTimer);
    if (this.socketTask) {
      try {
        this.socketTask.close({
          code: 1e3,
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
        const now = Date.now();
        if (now - this.lastHeartbeatResponse > 45e3) {
          console.warn("超过45秒未收到心跳响应，主动重连...");
          this.close();
          this._attemptReconnect();
          return;
        }
        this.send({ type: "heartbeat" });
        console.log("已发送心跳消息");
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
    if (!this.listeners[event])
      return this;
    if (!callback) {
      this.listeners[event] = [];
    } else {
      this.listeners[event] = this.listeners[event].filter((cb) => cb !== callback);
    }
    return this;
  }
  // 触发事件
  _triggerEvent(event, data) {
    if (!this.listeners[event])
      return;
    this.listeners[event].forEach((callback) => {
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
  _processMessage(data) {
    if (data && (data.type === "chat" || data.type === "USER-USER" || data.type === "ADMIN-USER" || data.type === "SYSTEM-USER" || data.type === "ORDER-USER")) {
      console.log("收到聊天消息:", data);
      this._triggerEvent("chat", data);
      this._triggerEvent("message", data);
      common_vendor.index.showToast({
        title: `收到新消息: ${data.content || ""}`,
        icon: "none",
        duration: 3e3,
        position: "top"
      });
    } else {
      this._triggerEvent("message", data);
    }
  }
  // 在WebSocketService类中添加处理消息的公共方法
  processReceivedMessage(message) {
    console.log("WebSocketService处理消息:", message);
    if (message.type === "heartbeat_response" || message.type === "HEARTBEAT" || message.type === "pong") {
      return;
    }
    const shouldShowNotification = !this.isCurrentlyChatting(message);
    if (shouldShowNotification) {
      this.showSimpleNotification(message);
    }
    if (message.type === "USER-USER" || message.type === "ADMIN-USER" || message.type === "SYSTEM-USER" || message.type === "ORDER-USER" || message.type === "chat") {
      this._triggerEvent("chat", message);
    }
    this.updateBadgeIfNeeded();
  }
  // 添加一个方法检查是否当前正在与该用户聊天
  isCurrentlyChatting(message) {
    try {
      const pages = getCurrentPages();
      if (pages.length === 0)
        return false;
      const currentPage = pages[pages.length - 1];
      if (currentPage.route !== "pages/friendMessage/friendMessage") {
        return false;
      }
      const targetUserId = currentPage.options && currentPage.options.userId;
      if (!targetUserId)
        return false;
      const messageSenderId = message.user || message.userId;
      return targetUserId === messageSenderId || targetUserId === "admin" && message.type === "ADMIN-USER";
    } catch (e) {
      console.error("检查当前聊天状态出错:", e);
      return false;
    }
  }
  // 添加简单的通知方法
  showSimpleNotification(message) {
    let title = "";
    let content = message.content || "";
    if (message.type === "USER-USER") {
      title = message.userName || "好友消息";
    } else if (message.type === "SYSTEM-USER") {
      title = "系统消息";
    } else if (message.type === "ORDER-USER") {
      title = "订单消息";
    } else if (message.type === "ADMIN-USER") {
      title = "管理员消息";
    } else {
      title = "新消息";
    }
    setTimeout(() => {
      common_vendor.index.showToast({
        title: `${title}: ${content}`,
        icon: "none",
        duration: 3e3,
        position: "top"
      });
    }, 0);
  }
  // 角标更新备用方法
  updateBadgeIfNeeded() {
    common_vendor.index.request({
      url: "http://localhost:8081/message/unread/count",
      method: "GET",
      header: {
        "Authorization": common_vendor.index.getStorageSync("token")
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 1) {
          const count = res.data.data || 0;
          const pages = getCurrentPages();
          if (pages.length === 0)
            return;
          const currentPage = pages[pages.length - 1];
          const tabBarPages = [
            "pages/index/index",
            "pages/find/find",
            "pages/publish/publish",
            "pages/friend/friend",
            "pages/word/word"
          ];
          const isTabBarPage = tabBarPages.some((path) => currentPage.route === path);
          if (isTabBarPage) {
            if (count > 0) {
              common_vendor.index.setTabBarBadge({
                index: 3,
                text: count.toString()
              });
            } else {
              common_vendor.index.removeTabBarBadge({
                index: 3
              });
            }
          }
        }
      }
    });
  }
}
const webSocketService = new WebSocketService();
exports.webSocketService = webSocketService;
