<script>
import webSocketService from './utils/websocket';
import { getUnreadMessageCount } from '@/api/message';

export default {
  globalData: {
    unreadMessageCount: 0,
    shouldRefreshFriendPage: false,
    pendingBadgeCount: 0,
    hasNewMessage: false
  },
  
  onLaunch() {
    console.log('App Launch');
    
    // 检查是否已登录
    const token = uni.getStorageSync('token');
    if (token) {
      // 连接到WebSocket服务器
      const wsUrl = 'ws://localhost:8082/websocket';
      
      setTimeout(() => {
        webSocketService.connect(wsUrl, token);
        
        // 确保WebSocket只监听一次
        webSocketService.off('chat');
        
        // 添加消息监听
        webSocketService.on('chat', (message) => {
          console.log('App收到WebSocket消息:', message);
          
          // 处理消息通知和角标更新
          this.handleNewMessage(message);
          
          // 刷新当前页面
          this.refreshCurrentPage(message);
        });
        
        // 监听普通消息
        webSocketService.on('message', (message) => {
          console.log('收到WebSocket消息:', message);
          // 处理非聊天类型消息
          if (message.type !== 'heartbeat' && 
              message.type !== 'heartbeat_response' && 
              message.type !== 'HEARTBEAT' && 
              message.type !== 'pong') {
            this.handleNewMessage(message);
          }
        });
        
        // 获取未读消息总数
        this.updateUnreadMessageCount();
      }, 1000);
    }
    
    // 设置App引用到WebSocket服务中
    if (typeof webSocketService.setApp === 'function') {
      webSocketService.setApp(this);
    }
    
    // 监听TabBar切换
    uni.onTabBarMidButtonTap(() => {
      console.log('点击了TabBar');
      this.onTabChange();
    });
  },
  
  methods: {
    // 处理新消息
    handleNewMessage(message) {
      console.log('处理新消息:', message);
      
      // 显示正确的消息通知
      let title = '';
      let content = message.content || '';
      
      if (message.type === 'USER-USER') {
        title = message.userName || '好友消息';
      } else if (message.type === 'SYSTEM-USER') {
        title = '系统消息';
      } else if (message.type === 'ORDER-USER') {
        title = '订单消息';
      } else if (message.type === 'ADMIN-USER') {
        title = '管理员消息';
      } else {
        title = '新消息';
      }
      
      // 显示通知
      this.showMessageNotification(title, content);
      
      // 更新未读消息计数
      this.updateUnreadMessageCount();
      
      // 刷新消息页面 (如果当前正在消息页面)
      const pages = getCurrentPages();
      if (pages.length > 0) {
        const currentPage = pages[pages.length - 1];
        if (currentPage.route === 'pages/friend/friend') {
          // 如果当前在消息页面，刷新列表
          if (typeof currentPage.$vm.loadMessages === 'function') {
            currentPage.$vm.loadMessages();
          }
        }
      }
      
      // 设置标记，表示有新消息需要在返回TabBar时更新角标
      this.globalData.shouldRefreshFriendPage = true;
      this.globalData.hasNewMessage = true;
    },
    
    // 显示消息通知
    showMessageNotification(title, content) {
      console.log('显示消息通知:', title, content);
      
      // 使用统一的消息通知方式
      uni.showToast({
        title: `${title}: ${content}`,
        icon: 'none',
        duration: 3000,
        position: 'top'
      });
      
      // 也可以使用自定义组件或更高级的通知组件
      // 如果要使用自定义组件，需要在App.vue中引入并使用
    },
    
    // 如果friend页面可见，刷新它
    refreshFriendPageIfVisible() {
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      
      // 检查当前页面是否是friend页面
      if (currentPage && currentPage.route === 'pages/friend/friend') {
        console.log('当前在消息页面，刷新列表');
        // 调用页面的刷新方法
        if (currentPage.$vm && typeof currentPage.$vm.loadMessages === 'function') {
          currentPage.$vm.loadMessages();
          // 重置刷新标记
          this.globalData.shouldRefreshFriendPage = false;
        }
      }
    },
    
    // 更新未读消息计数
    updateUnreadMessageCount() {
      getUnreadMessageCount().then(res => {
        if (res.code === 1) {
          const count = res.data || 0;
          this.globalData.unreadMessageCount = count;
          
          // 设置全局变量，延迟到适当时机再设置角标
          this.globalData.pendingBadgeCount = count;
          
          // 只在TabBar页面上设置角标
          this.safeSetTabBarBadge();
        }
      }).catch(err => {
        console.error('获取未读消息数失败:', err);
      });
    },
    
    // 安全地设置TabBar角标的方法
    safeSetTabBarBadge() {
      // 检查当前是否在TabBar页面
      const pages = getCurrentPages();
      if (pages.length === 0) return; // 页面栈为空
      
      const currentPage = pages[pages.length - 1];
      const tabBarPages = [
        'pages/index/index',
        'pages/find/find',
        'pages/publish/publish',
        'pages/friend/friend',
        'pages/word/word'
      ];
      
      // 检查当前是否是TabBar页面
      const isTabBarPage = tabBarPages.some(path => currentPage.route === path);
      
      if (isTabBarPage) {
        const count = this.globalData.pendingBadgeCount || 0;
        
        if (count > 0) {
          // 有未读消息，设置角标
          uni.setTabBarBadge({
            index: 3, // 消息选项卡索引
            text: count.toString(),
            fail: (err) => {
              console.log('设置角标失败:', err);
            }
          });
          
          // 重置新消息标记
          this.globalData.hasNewMessage = false;
        } else {
          // 无未读消息，移除角标
          try {
            uni.hideTabBarRedDot({
              index: 3
            });
            
            uni.removeTabBarBadge({
              index: 3
            });
          } catch (e) {
            console.log('移除角标时出错:', e);
          }
        }
      } else if (this.globalData.hasNewMessage) {
        console.log('当前不在TabBar页面，但有新消息，延迟设置角标');
        // 不在TabBar页面但有新消息，记录状态以便返回TabBar时更新
      }
    },
    
    // 刷新当前页面
    refreshCurrentPage(message) {
      const pages = getCurrentPages();
      if (pages.length === 0) return;
      
      const currentPage = pages[pages.length - 1];
      
      // 如果当前在消息页面，刷新消息列表
      if (currentPage.route === 'pages/friend/friend') {
        console.log('当前在消息页面，刷新消息列表');
        if (currentPage.$vm && typeof currentPage.$vm.loadMessages === 'function') {
          currentPage.$vm.loadMessages();
        }
      }
      
      // 如果当前在聊天页面，可能需要添加新消息
      if (currentPage.route === 'pages/friendMessage/friendMessage') {
        const targetUserId = currentPage.options && currentPage.options.userId;
        // 检查消息是否来自当前聊天对象
        if (targetUserId && (message.user === targetUserId || message.userId === targetUserId || 
            (targetUserId === 'admin' && message.type === 'ADMIN-USER'))) {
          console.log('当前在聊天页面，收到来自当前对象的消息');
          // 页面会自己处理
        }
      }
    },
    
    // 处理TabBar切换
    onTabChange() {
      setTimeout(() => {
        const pages = getCurrentPages();
        if (pages.length > 0) {
          const currentPage = pages[pages.length - 1];
          if (currentPage.route === 'pages/friend/friend') {
            console.log('检测到切换到消息页面');
            if (currentPage.$vm && typeof currentPage.$vm.loadMessages === 'function') {
              currentPage.$vm.loadMessages(true); // 强制刷新
            }
          }
        }
      }, 100); // 延迟执行确保页面已加载
    }
  },
  
  onShow: function() {
    console.log('App Show');
    const token = uni.getStorageSync('token');
    if (token) {
      if (!webSocketService.isConnected()) {
        webSocketService.connect('ws://localhost:8082/websocket', token);
      }
      
      // 每次App显示时，获取最新未读消息数
      this.updateUnreadMessageCount();
      
      // 每次App显示时，尝试设置角标
      setTimeout(() => {
        this.safeSetTabBarBadge();
      }, 300); // 延迟确保TabBar已加载
    }
  },
  
  onHide: function() {
    console.log('App Hide');
    // 可以选择在应用隐藏时断开连接，或保持连接
    // webSocketService.close();
  }
}
</script>

<style>
/* 可以添加全局样式，比如通知样式 */
.message-notification {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 9999;
  background-color: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 10px 15px;
  font-size: 14px;
  text-align: center;
}

/* 消息通知样式 */
.uni-top-toast {
  /* 覆盖uni-app的默认样式 */
  z-index: 99999 !important; 
  /* 调整位置使其更明显 */
  top: var(--status-bar-height, 20px) !important;
  background-color: rgba(0, 0, 0, 0.7) !important;
  color: #fff !important;
  padding: 10px 15px !important;
  border-radius: 4px !important;
  font-size: 14px !important;
}
</style>