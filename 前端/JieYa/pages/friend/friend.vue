<template>
  <view class="friend-container">
    <scroll-view scroll-y class="message-list" refresher-enabled @refresherrefresh="onRefresh" :refresher-triggered="isRefreshing">
      <!-- 心理咨询固定在顶部 -->
      <friend-item
        username="心理咨询"
        userimage="/static/aiChat.png"
        message="点击开始与心理顾问对话"
        :isNew="false"
        @tap="navigateToAiChat"
        class="fixed"
      />
      
      <!-- 所有消息列表 -->
      <template v-if="messageList.length > 0">
        <friend-item
          v-for="(item, index) in messageList"
          :key="item.id || index"
          :username="item.type === 'USER-USER' ? item.userName : getNameByType(item.type)"
          :userimage="item.type === 'USER-USER' ? (item.avatarUrl || '/static/default-avatar.png') : getAvatarByType(item.type)"
          :message="item.lastContent"
          :date="formatTime(item.time)"
          :badgeCount="item.notReadCount"
          @tap="handleItemTap(item)"
        />
      </template>
      
      <!-- 空状态 -->
      <view v-if="messageList.length === 0 && !isLoading" class="empty-state">
        <image src="/static/empty-message.png" mode="aspectFit" class="empty-image"></image>
        <text class="empty-text">暂无消息</text>
      </view>
    </scroll-view>
    
    <!-- 加载中状态 -->
    <view class="loading-overlay" v-if="isLoading">
      <view class="loader"></view>
      <text>加载中...</text>
    </view>
  </view>
</template>

<script>
// 使用Options API处理页面生命周期
export default {
  onShow() {
    console.log('Friend页面显示 (Options API)');
    this.loadMessages && this.loadMessages();
  },
  
  onHide() {
    console.log('Friend页面隐藏 (Options API)');
  },
  
  onTabItemTap() {
    console.log('Tab点击刷新');
    this.loadMessages && this.loadMessages();
  }
}
</script>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { getUserMessagesList, markAsRead } from '@/api/message';
import friendItem from '@/components/friend_item/friend_item.vue'
import webSocketService from '@/utils/websocket';

// 数据
const messageList = ref([]);
const isLoading = ref(true);
const isRefreshing = ref(false);

// 区分系统消息和用户消息
const systemMessages = computed(() => {
  return messageList.value.filter(msg => 
    msg.type === 'SYSTEM-USER' || 
    msg.type === 'ADMIN-USER' || 
    msg.type === 'ORDER-USER'
  );
});

const userMessages = computed(() => {
  return messageList.value.filter(msg => msg.type === 'USER-USER');
});

// 添加一个变量跟踪上次加载时间
const lastLoadTime = ref(0);

// 加载消息列表，带有强制刷新选项
const loadMessages = async (forceRefresh = false) => {
  const now = Date.now();
  
  // 如果距离上次加载不超过2秒且不是强制刷新，则跳过
  if (!forceRefresh && now - lastLoadTime.value < 2000) {
    console.log('加载过于频繁，跳过');
    return;
  }
  
  // 更新加载时间
  lastLoadTime.value = now;
  
  console.log('正在刷新消息列表...');
  isLoading.value = true;
  try {
    const res = await getUserMessagesList(1, 50);
    console.log('获取到的消息数据:', res);
    if (res.code === 1) {
      messageList.value = res.data.records || [];
      
      // 直接计算总未读数
      let totalUnread = 0;
      messageList.value.forEach(msg => {
        totalUnread += (msg.notReadCount || 0);
      });
      
      // 直接更新底部导航栏
      const app = getApp();
      if (app) {
        app.globalData.unreadMessageCount = totalUnread;
        
        // 直接设置角标
        if (totalUnread > 0) {
          uni.setTabBarBadge({
            index: 3,
            text: totalUnread.toString()
          });
        } else {
          try {
            uni.removeTabBarBadge({
              index: 3
            });
          } catch (e) {
            console.error('移除TabBar角标失败:', e);
          }
        }
      }
    }
  } catch (error) {
    console.error('获取消息列表失败:', error);
  } finally {
    isLoading.value = false;
    isRefreshing.value = false;
  }
};

// 下拉刷新
const onRefresh = () => {
  isRefreshing.value = true;
  loadMessages();
};

// 根据消息类型获取头像
const getAvatarByType = (type) => {
  switch (type) {
    case 'SYSTEM-USER':
      return '/static/system-message.png';
    case 'ADMIN-USER':
      return '/static/admin_avatar.png';
    case 'ORDER-USER':
      return '/static/order-message.png';
    default:
      return '/static/default-avatar.png';
  }
};

// 根据消息类型获取名称
const getNameByType = (type) => {
  switch (type) {
    case 'SYSTEM-USER':
      return '系统消息';
    case 'ADMIN-USER':
      return '管理员消息';
    case 'ORDER-USER':
      return '订单消息';
    default:
      return '未知消息';
  }
};

// 格式化时间
const formatTime = (time) => {
  if (!time) return '';
  
  const msgDate = new Date(time);
  const now = new Date();
  const diff = now - msgDate;
  
  // 今天的消息显示时间
  if (diff < 24 * 60 * 60 * 1000 && msgDate.getDate() === now.getDate()) {
    const hours = msgDate.getHours().toString().padStart(2, '0');
    const mins = msgDate.getMinutes().toString().padStart(2, '0');
    return `${hours}:${mins}`;
  }
  
  // 一周内的消息显示星期
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    return weekdays[msgDate.getDay()];
  }
  
  // 更早的消息显示日期
  return `${msgDate.getMonth() + 1}月${msgDate.getDate()}日`;
};

// 导航到AI聊天页面
const navigateToAiChat = () => {
  uni.navigateTo({
    url: '/pages/message/message'
  });
};

// 处理消息点击
const handleItemTap = (item) => {
  console.log('点击消息项:', item);
  
  // 更新本地UI显示
  const index = messageList.value.findIndex(msg => 
    (msg.type === item.type && 
     ((item.type === 'ADMIN-USER' && msg.type === 'ADMIN-USER') || 
      (item.type === 'USER-USER' && msg.userId === item.userId) ||
      (item.type === 'SYSTEM-USER' && msg.type === 'SYSTEM-USER') ||
      (item.type === 'ORDER-USER' && msg.type === 'ORDER-USER'))
    )
  );
  
  if (index !== -1) {
    // 保存原来的未读数，用于更新全局计数
    const oldNotReadCount = messageList.value[index].notReadCount || 0;
    
    // 修改本地未读数
    messageList.value[index].notReadCount = 0;
    
    // 强制刷新列表
    messageList.value = [...messageList.value];
    
    // 更新底部导航栏角标（重要：直接减少全局计数）
    const app = getApp();
    if (app && app.globalData) {
      // 直接减少相应数量
      if (app.globalData.unreadMessageCount >= oldNotReadCount) {
        app.globalData.unreadMessageCount -= oldNotReadCount;
      } else {
        app.globalData.unreadMessageCount = 0;
      }
      
      // 直接设置角标
      if (app.globalData.unreadMessageCount > 0) {
        uni.setTabBarBadge({
          index: 3, // 消息Tab的索引
          text: app.globalData.unreadMessageCount.toString()
        });
      } else {
        // 没有未读消息，尝试移除角标
        try {
          uni.removeTabBarBadge({
            index: 3
          });
        } catch (e) {
          console.error('移除TabBar角标失败:', e);
        }
      }
    }
  }
  
  // 调用API同步到服务器
  if (item.type === 'ADMIN-USER') {
    markAsRead('admin').catch(err => console.error('标记管理员消息已读失败:', err));
  } else if (item.type === 'USER-USER' && item.userId) {
    markAsRead(item.userId).catch(err => console.error('标记用户消息已读失败:', err));
  } else if (item.type === 'SYSTEM-USER') {
    markAsRead('system').catch(err => console.error('标记系统消息已读失败:', err));
  } else if (item.type === 'ORDER-USER') {
    markAsRead('order').catch(err => console.error('标记订单消息已读失败:', err));
  }
  
  // 现有跳转逻辑保持不变...
  if (item.type === 'USER-USER') {
    // 跳转到用户聊天页面
    uni.navigateTo({
      url: `/pages/friendMessage/friendMessage?userId=${item.userId}`
    });
  } else if (item.type === 'ADMIN-USER') {
    // 跳转到管理员聊天页面
    uni.navigateTo({
      url: '/pages/friendMessage/friendMessage?userId=admin'
    });
  } else if (item.type === 'SYSTEM-USER') {
    // 跳转到系统消息页面
    uni.navigateTo({
      url: '/pages/systemMessage/systemMessage'
    });
  } else if (item.type === 'ORDER-USER') {
    // 跳转到订单消息页面
    uni.navigateTo({
      url: '/pages/orderMessage/orderMessage'
    });
  }
};

// 生命周期钩子
onMounted(() => {
  console.log('Friend页面挂载');
  loadMessages();
  setupWebSocketListener();
});

onBeforeUnmount(() => {
  console.log('Friend页面卸载前');
  removeWebSocketListener();
});

// WebSocket监听
const setupWebSocketListener = () => {
  webSocketService.on('chat', (message) => {
    console.log('Friend页面收到消息:', message);
    loadMessages();
  });
};

const removeWebSocketListener = () => {
  webSocketService.off('chat');
};

// 页面被激活时要执行的函数
const onTabActivated = () => {
  console.log('Tab页面被激活');
  // 强制刷新
  loadMessages(true);
};

// 暴露方法
defineExpose({
  loadMessages,
  onTabActivated
});
</script>

<style lang="scss" scoped>
.friend-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f8f8;
  display: flex;
  flex-direction: column;
}

.status-bar {
  height: var(--status-bar-height, 25px);
  background-color: #ffffff;
}

.header {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  position: relative;
  border-bottom: 1px solid #f0f0f0;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.message-list {
  flex: 1;
  padding: 10px 0;
  box-sizing: border-box;
}

.section-title {
  padding: 12px 16px 8px;
  font-size: 14px;
  color: #999;
  background-color: #f8f8f8;
}

.message-card {
  display: flex;
  padding: 16px;
  background-color: #ffffff;
  position: relative;
  
  &.fixed {
    background-color: rgba(26, 173, 25, 0.05);
    border-left: 3px solid #1aad19;
  }
  
  &:active {
    background-color: #f5f5f5;
  }
  
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 68px;
    right: 0;
    height: 1px;
    background-color: #f0f0f0;
  }
  
  &:last-child::after {
    display: none;
  }
}

.left {
  margin-right: 12px;
}

.avatar {
  width: 52px;
  height: 52px;
  border-radius: 4px;
  background-color: #f0f0f0;
}

.content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.time {
  font-size: 12px;
  color: #999;
}

.bottom-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 6px;
}

.message {
  font-size: 14px;
  color: #666;
  max-width: 85%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.badge {
  min-width: 16px;
  height: 16px;
  border-radius: 8px;
  background-color: #ff4d4f;
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 0;
}

.empty-image {
  width: 120px;
  height: 120px;
  margin-bottom: 16px;
}

.empty-text {
  color: #999;
  font-size: 14px;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loader {
  width: 36px;
  height: 36px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #1aad19;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
