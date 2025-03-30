<script>
import webSocketService from './utils/websocket';

export default {
  onLaunch() {
    console.log('App Launch');
    
    // 检查是否已登录
    const token = uni.getStorageSync('token');
    if (token) {
      // 连接到WebSocket服务器
      const wsUrl = 'ws://localhost:8082/websocket';
      
      setTimeout(() => {
        webSocketService.connect(wsUrl, token);
        
        // 监听全局消息
        webSocketService.on('message', (message) => {
          console.log('收到WebSocket消息:', message);
          // 根据消息类型处理不同的业务逻辑
          if (message.type === 'notification') {
            // 处理通知
            uni.showToast({
              title: message.content,
              icon: 'none'
            });
          } else if (message.type === 'chat') {
            // 处理聊天消息
            console.log('收到聊天消息:', message.content);
          }
        });
      }, 1000);
    }
  },
  
  onShow: function() {
    console.log('App Show');
    const token = uni.getStorageSync('token');
    if (token && !webSocketService.isConnected()) {
      webSocketService.connect('ws://localhost:8082/ws', token);
    }
  },
  
  onHide: function() {
    console.log('App Hide');
    // 可以选择在应用隐藏时断开连接，或保持连接
    // webSocketService.close();
  }
}
</script>