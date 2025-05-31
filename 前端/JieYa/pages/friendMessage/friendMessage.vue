<template>
  <view class="chat-container">
    <!-- 消息列表区域 -->
    <scroll-view 
      class="message-list" 
      scroll-y 
      @scrolltoupper="loadMoreMessages" 
      :scroll-into-view="'msg-' + scrollToMessageId"
      :scroll-with-animation="true"
      :refresher-enabled="!loading"
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
      ref="messageListRef">
      
      <!-- 加载更多指示器 -->
      <view v-if="loading" class="loading-more">
        <text>加载中...</text>
      </view>
      
      <!-- 消息项 -->
      <view 
        v-for="(msg, index) in messageList" 
        :key="msg.id" 
        class="message-item-container">

        <!-- 时间戳 -->
        <view v-if="shouldShowTimestamp(msg, index)" class="timestamp">
          {{ formatDateDivider(msg.time) }}
        </view>
        
        <!-- 消息主体 -->
        <view 
          :id="'msg-' + msg.id"
          class="message-item" 
          :class="{ 'self-message': isSelfMessage(msg) }">
          
          <!-- 头像 -->
          <image 
            class="avatar" 
            :src="isSelfMessage(msg) ? userAvatar : (targetUserInfo.avatarUrl || '/static/default-avatar.png')" 
            mode="aspectFill"></image>
          
          <!-- 消息内容容器 -->
          <view class="message-content">
            <!-- 图片消息，简化判断条件，只要有fileUrl就显示图片 -->
            <view v-if="msg.fileUrl" class="message-bubble image-bubble">
              <image 
                class="message-image" 
                :src="msg.fileUrl"  
                mode="aspectFill" 
                @tap="previewImage(msg.fileUrl)"></image>
            </view>
            
            <!-- 文本消息 -->
            <view v-else class="message-bubble">
              <text class="message-text">{{ msg.content }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 滚动到底部的占位符 (可选，有时用于确保滚动准确) -->
      <view id="bottom-anchor"></view>

      <!-- 没有消息时的提示 -->
      <view v-if="messageList.length === 0 && !loading" class="empty-message">
        <text>暂无消息</text>
      </view>
    </scroll-view>
    
    <!-- 输入区域 -->
    <view class="input-area">
      <!-- 输入框 -->
      <input 
        type="text" 
        v-model="inputMessage" 
        class="message-input" 
        placeholder=""
        confirm-type="send"
        @confirm="handleSendMessage" />
        
      <!-- 表情按钮 -->
      <view class="input-icon-wrapper" @tap="toggleEmojiPanel">
         <image src="/static/emoji.png" class="input-icon emoji-icon"></image>
      </view>
      
      <!-- 根据输入内容动态显示加号或发送按钮 -->
      <view class="input-icon-wrapper" @tap="inputMessage.trim() ? handleSendMessage() : handleAttachment()">
         <!-- 当输入框为空时显示加号 -->
         <image v-if="!inputMessage.trim()" src="/static/plus.png" class="input-icon attach-icon"></image>
         <!-- 当输入框有内容时显示发送按钮 -->
         <view v-else class="send-btn">发送</view>
      </view>
    </view>

    <!-- 表情面板 -->
    <view class="emoji-panel" v-if="showEmojiPanel">
      <scroll-view scroll-y class="emoji-container">
        <view class="emoji-list">
          <view 
            v-for="(emoji, index) in emojiList" 
            :key="index" 
            class="emoji-item"
            @tap="insertEmoji(emoji)">
            {{ emoji }}
          </view>
        </view>
      </scroll-view>
    </view>

  </view>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, onUnmounted, onBeforeUnmount } from 'vue';
import { getChatMessages, sendMessage as sendMessageApi, markAsRead } from '@/api/message';
import webSocketService from '@/utils/websocket';
import { uploadMessageImage } from '@/api/upload';

const messageList = ref([]);
const inputMessage = ref('');
const loading = ref(false);
const isRefreshing = ref(false);
const currentPage = ref(1);
const pageSize = ref(20);
const hasMore = ref(true);
const targetUserId = ref('');
const currentUserId = ref('');
const targetUserInfo = ref({});
const scrollToMessageId = ref('');
const userAvatar = ref('');
const showEmojiPanel = ref(false);
const emojiList = ref(['😀', '😂', '😊', '😍', '🤔', '😒', '😭', '😡', '👍', '👋', '❤️', '🎉', '🔥', '✅', '⭐', '🎁', '🙏', '🌹']);
const needRefreshFriendPage = ref(false);

onMounted(() => {
  // 从 storage 获取 userInfo
  const userInfoStorage = uni.getStorageSync('userInfo');
  console.log('Retrieved userInfo from storage:', userInfoStorage); // 调试日志
  if (userInfoStorage) {
    try {
      // 确保是字符串再解析
      const parsedUserInfo = typeof userInfoStorage === 'string' 
        ? JSON.parse(userInfoStorage) 
        : userInfoStorage; // 如果已经是对象，直接使用

      // 检查解析后的对象是否有效
      if (parsedUserInfo && typeof parsedUserInfo === 'object') {
        currentUserId.value = parsedUserInfo.id || ''; 
        userAvatar.value = parsedUserInfo.avatar || '/static/default-avatar.png';
      } else {
         console.warn('Parsed userInfo is not a valid object:', parsedUserInfo);
         // 设置备用值
         currentUserId.value = ''; 
         userAvatar.value = '/static/default-avatar.png';
      }
    } catch (e) {
      console.error('Failed to parse userInfo from storage:', e, 'Raw value:', userInfoStorage);
      // 设置备用值
      currentUserId.value = ''; 
      userAvatar.value = '/static/default-avatar.png';
    }
  } else {
    console.warn('未在 Storage 中找到 userInfo');
    // 设置备用值
    currentUserId.value = ''; 
    userAvatar.value = '/static/default-avatar.png';
  }
  
  // 获取路由参数中的目标用户ID
  const pages = getCurrentPages();
  const page = pages[pages.length - 1];
  if (page && page.options && page.options.userId) {
    targetUserId.value = page.options.userId;
    
    // 加载目标用户信息
    const friendInfoStorage = uni.getStorageSync('friendInfo_' + targetUserId.value);
    console.log(`Retrieved friendInfo for ${targetUserId.value}:`, friendInfoStorage); // 调试日志
    
    if (friendInfoStorage) {
       try {
         // 确保是字符串再解析
         const parsedFriendInfo = typeof friendInfoStorage === 'string'
           ? JSON.parse(friendInfoStorage)
           : friendInfoStorage; // 如果已经是对象，直接使用

         if (parsedFriendInfo && typeof parsedFriendInfo === 'object') {
            targetUserInfo.value = parsedFriendInfo;
         } else {
            console.warn(`Parsed friendInfo for ${targetUserId.value} is not a valid object:`, parsedFriendInfo);
            targetUserInfo.value = {}; // 设置空对象
         }
       } catch (e) {
          console.error(`Failed to parse friendInfo for ${targetUserId.value}:`, e, 'Raw value:', friendInfoStorage);
          targetUserInfo.value = {}; // 设置空对象
       }
    } else if (targetUserId.value === 'admin') {
		console.log("adminadmin");
       targetUserInfo.value = {
         userId: 'admin',
         username: '管理员',
         avatarUrl: '/static/admin_avatar.png'
       };
    } else {
        console.warn(`未在 Storage 中找到 friendInfo_${targetUserId.value}`);
        targetUserInfo.value = {}; // 设置空对象
    }
    
    // 加载聊天消息
    loadMessages();
    
    // 立即标记消息为已读 - 添加这段代码
    markMessagesAsReadAndUpdateBadge();
    
    // 监听WebSocket消息
    setupWebSocket();
  } else {
     console.error("未能获取到目标用户ID");
     // 可以添加返回上一页或其他错误处理逻辑
  }

  // 在 friendMessage.vue 中的 onMounted 末尾添加这些调试日志
  console.log('Current User ID:', currentUserId.value);
  console.log('Target User ID:', targetUserId.value);
  console.log('Current User Avatar:', userAvatar.value);
  console.log('Target User Info:', targetUserInfo.value);
});

// 设置WebSocket消息监听
const setupWebSocket = () => {
  // 添加消息监听
  webSocketService.on('chat', (data) => {
    console.log('聊天页面收到消息:', data, '当前聊天对象:', targetUserId.value);
    
    // 检查消息是否来自当前聊天对象
    if (data.user === targetUserId.value || data.userId === targetUserId.value) {
      // 是当前聊天对象发来的消息，添加到列表并立即标记为已读
      messageList.value.push({
        id: data.id || Date.now(),
        user: data.user || data.userId,
        content: data.content,
        fileUrl: data.fileUrl,
        type: data.type || 'text',
        time: data.time || new Date(),
        status: data.status || 1
      });
      
      // 滚动到底部
      nextTick(() => {
        scrollToBottom();
      });
      
      // 立即标记为已读
      markMessagesAsReadAndUpdateBadge();
    } else {
      console.log('收到非当前聊天对象的消息，不处理');
      // 收到其他用户的消息，设置标记，退出时需要刷新friend页面
      needRefreshFriendPage.value = true;
    }
  });
};

// 移除WebSocket监听
onBeforeUnmount(() => {
  webSocketService.off('chat');
});

// 加载消息
const loadMessages = async () => {
  if (loading.value || !hasMore.value) return;
  
  loading.value = true;
  try {
    const res = await getChatMessages(targetUserId.value, currentPage.value, pageSize.value);
    if (res && res.code === 1) {
      const records = res.data.records || [];
      if (records.length === 0) {
        hasMore.value = false;
      } else {
        // 消息按时间升序排列
        const sortedMessages = records.sort((a, b) => new Date(a.time) - new Date(b.time));
        
        // 添加到消息列表前面
        messageList.value = [...sortedMessages, ...messageList.value];
        
        // 更新页码
        currentPage.value++;
      }
    }
  } catch (error) {
    console.error('加载消息失败:', error);
  } finally {
    loading.value = false;
    isRefreshing.value = false;
  }
};

// 加载更多消息
const loadMoreMessages = () => {
  if (hasMore.value && !loading.value) {
    loadMessages();
  }
};

// 下拉刷新
const onRefresh = () => {
  isRefreshing.value = true;
  loadMoreMessages();
};

// 滚动到底部
const scrollToBottom = () => {
  if (messageList.value.length > 0) {
    const lastMsg = messageList.value[messageList.value.length - 1];
    scrollToMessageId.value = lastMsg.id;
  }
};

// 返回按钮点击
const goBack = () => {
  uni.navigateBack();
};

// 发送消息
const handleSendMessage = async () => {
  if (!inputMessage.value.trim()) return;
  
  const messageContent = inputMessage.value.trim();
  inputMessage.value = ''; // 清空输入框
  
  // 构建消息对象 - 设置正确的type
  const messageData = {
    toUser: targetUserId.value,
    content: messageContent,
    type: targetUserId.value === 'admin' ? 'ADMIN-USER' : 'USER-USER'
  };
  
  // 本地添加消息（乐观更新）
  const tempId = 'temp-' + Date.now();
  const newMessage = {
    id: tempId,
    user: currentUserId.value,
    toUser: targetUserId.value,
    content: messageContent,
    type: messageData.type,
    time: new Date(),
    status: 0 // 发送中状态
  };
  
  messageList.value.push(newMessage);
  
  // 滚动到底部
  nextTick(() => {
    scrollToBottom();
  });
  
  try {
    const res = await sendMessageApi(messageData);
    if (res.code === 1) {
      // 更新消息ID和状态
      const index = messageList.value.findIndex(msg => msg.id === tempId);
      if (index !== -1) {
        messageList.value[index].id = res.data.id || tempId;
        messageList.value[index].status = 1; // 已发送状态
      }
    } else {
      // 更新为发送失败状态
      const index = messageList.value.findIndex(msg => msg.id === tempId);
      if (index !== -1) {
        messageList.value[index].status = 2; // 发送失败状态
      }
      
      uni.showToast({
        title: res.message || '发送失败',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('发送消息失败:', error);
    
    // 更新为发送失败状态
    const index = messageList.value.findIndex(msg => msg.id === tempId);
    if (index !== -1) {
      messageList.value[index].status = 2; // 发送失败状态
    }
    
    uni.showToast({
      title: '发送失败',
      icon: 'none'
    });
  }
};

// 修改附件上传功能
const handleAttachment = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      const filePath = res.tempFilePaths[0];
      
      uni.showLoading({
        title: '上传中...'
      });
      
      uploadMessageImage(filePath).then(uploadRes => {
        if (uploadRes.code === 1 && uploadRes.data) {
          const fileUrl = uploadRes.data;
          
          // 发送消息
          const messageData = {
            toUser: targetUserId.value,
            content: '[图片]',
            fileUrl: fileUrl,
            type: targetUserId.value === 'admin' ? 'ADMIN-USER' : 'USER-USER'
          };
          
          // 添加临时消息到列表
          const tempId = 'temp-' + Date.now();
          messageList.value.push({
            id: tempId,
            user: currentUserId.value,
            toUser: targetUserId.value,
            content: '[图片]',
            fileUrl: fileUrl,
            type: messageData.type,
            time: new Date()
          });
          
          // 强制刷新视图
          nextTick(() => {
            messageList.value = [...messageList.value];
            scrollToBottom();
          });
          
          // 发送消息到服务器
          sendMessageApi(messageData).then(res => {
            if (res.code === 1) {
              // 更新消息ID和状态
              const index = messageList.value.findIndex(msg => msg.id === tempId);
              if (index !== -1) {
                messageList.value[index].id = res.data.id || tempId;
                messageList.value[index].status = 1; // 已发送状态
              }
            } else {
              // 更新为发送失败状态
              const index = messageList.value.findIndex(msg => msg.id === tempId);
              if (index !== -1) {
                messageList.value[index].status = 2; // 发送失败状态
              }
              
              uni.showToast({
                title: res.message || '发送失败',
                icon: 'none'
              });
            }
          }).catch(error => {
            console.error('发送消息失败:', error);
            
            // 更新为发送失败状态
            const index = messageList.value.findIndex(msg => msg.id === tempId);
            if (index !== -1) {
              messageList.value[index].status = 2; // 发送失败状态
            }
            
            uni.showToast({
              title: '发送失败',
              icon: 'none'
            });
          });
        } else {
          uni.showToast({
            title: uploadRes.message || '上传失败',
            icon: 'none'
          });
        }
      }).catch(error => {
        console.error('上传失败:', error);
        uni.showToast({
          title: error.message || '上传失败',
          icon: 'none'
        });
      }).finally(() => {
        uni.hideLoading();
      });
    }
  });
};

// 预览图片
const previewImage = (url) => {
  // 收集所有图片URL - 修改过滤条件，只检查fileUrl存在即可
  const imageUrls = messageList.value
    .filter(msg => msg.fileUrl)
    .map(msg => msg.fileUrl);
  
  // 如果没有收集到图片，至少包含当前点击的图片
  if (imageUrls.length === 0) {
    imageUrls.push(url);
  }
  
  // 使用uni-app的预览图片API
  uni.previewImage({
    current: url,      // 当前显示图片的URL
    urls: imageUrls,   // 所有图片URL的数组
    longPressActions: {
      itemList: ['保存图片'],
      success: function(data) {
        // 长按菜单被点击
        if (data.tapIndex === 0) {
          // 保存图片到相册
          uni.saveImageToPhotosAlbum({
            filePath: imageUrls[data.index],
            success: function() {
              uni.showToast({
                title: '保存成功',
                icon: 'success'
              });
            },
            fail: function() {
              uni.showToast({
                title: '保存失败',
                icon: 'none'
              });
            }
          });
        }
      }
    }
  });
};

// 标记消息为已读
const markMessagesAsRead = async () => {
  try {
    await markAsRead(targetUserId.value);
  } catch (error) {
    console.error('标记消息已读失败:', error);
  }
};

// 添加一个新方法，同时处理标记已读和更新角标
const markMessagesAsReadAndUpdateBadge = async () => {
  try {
    // 标记消息为已读
    await markAsRead(targetUserId.value);
    
    // 立即更新消息列表(如果有缓存)
    const pages = getCurrentPages();
    for(let i = 0; i < pages.length; i++) {
      if(pages[i].route === 'pages/friend/friend') {
        // 如果friend页面在栈中，通知它更新角标
        const friendPage = pages[i];
        if(friendPage.$vm && typeof friendPage.$vm.updateMessageBadge === 'function') {
          friendPage.$vm.updateMessageBadge(targetUserId.value);
        }
        break;
      }
    }
    
    // 更新全局未读消息数
    const app = getApp();
    if(app && app.updateUnreadMessageCount) {
      app.updateUnreadMessageCount();
    }
  } catch (error) {
    console.error('标记消息已读失败:', error);
  }
};

// 是否应该显示日期分割线
const shouldShowDateDivider = (msg, index) => {
  if (index === 0) return true;
  
  const prevMsg = messageList.value[index - 1];
  const msgDate = new Date(msg.time);
  const prevMsgDate = new Date(prevMsg.time);
  
  // 日期不同则显示分割线
  return msgDate.toDateString() !== prevMsgDate.toDateString();
};

// 格式化日期分割线文本
const formatDateDivider = (time) => {
  if (!time) return '';
  
  const date = new Date(time);
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  return `${hours}:${minutes}`; // 直接返回 时:分
};

// 格式化消息时间 (显示 小时:分钟)
const formatMessageTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  return `${hours}:${minutes}`;
};

// 是否显示时间戳（模拟图片中的独立时间戳）
// 简单的实现：如果两条消息时间间隔超过5分钟，则显示时间戳
const shouldShowTimestamp = (msg, index) => {
  if (index === 0) return true; // 第一条消息显示时间戳
  
  const prevMsg = messageList.value[index - 1];
  const msgDate = new Date(msg.time);
  const prevMsgDate = new Date(prevMsg.time);
  
  // 时间间隔超过 5 分钟 (300000 毫秒)
  return msgDate.getTime() - prevMsgDate.getTime() > 5 * 60 * 1000;
};

// 切换表情面板
const toggleEmojiPanel = () => {
  showEmojiPanel.value = !showEmojiPanel.value;
};

// 插入表情到输入框
const insertEmoji = (emoji) => {
  inputMessage.value += emoji;
};

// 判断是否为视频文件
const isVideoFile = (url) => {
  if (!url) return false;
  
  try {
    const lowerUrl = url.toLowerCase();
    const videoExtensions = ['.mp4', '.mov', '.avi', '.wmv', '.flv', '.mkv', '.webm'];
    return videoExtensions.some(ext => lowerUrl.endsWith(ext));
  } catch (e) {
    console.error('检查视频文件类型错误:', e);
    return false;
  }
};

// 判断是否为自己发送的消息
const isSelfMessage = (msg) => {
  // 主要判断：如果msg.user是自己的ID，这是自己发出的消息
  if (msg.user === currentUserId.value) {
    return true;
  }
  
  // 备用判断：如果msg.toUser是对方的ID，同样表示这是自己发出的消息
  if (msg.toUser === targetUserId.value) {
    return true;
  }
  
  // 如果两个条件都不满足，说明这是收到的消息
  return false;
};

// 判断 fileUrl 是否有效
const isValidFileUrl = (fileUrl) => {
  try {
    // 检查 fileUrl 是否存在、非空字符串、不是 undefined 或 null、不是字符串"undefined"
    return fileUrl && 
           typeof fileUrl === 'string' && 
           fileUrl.trim() !== '' && 
           fileUrl !== 'undefined' &&
           fileUrl !== 'null';
  } catch (e) {
    console.error('验证fileUrl错误:', e);
    return false;
  }
};

// 添加图片加载成功处理
const onImageLoad = (msg) => {
  console.log('图片加载成功:', msg.fileUrl);
};

// 添加图片加载错误处理
const onImageError = (msg) => {
  console.error('图片加载失败:', msg.fileUrl);
};

// 在页面卸载时，检查是否需要刷新friend页面
onUnmounted(() => {
  // 如果收到过非当前聊天对象的消息，需要刷新friend页面
  if (needRefreshFriendPage.value) {
    const pages = getCurrentPages();
    for(let i = 0; i < pages.length; i++) {
      if(pages[i].route === 'pages/friend/friend') {
        const friendPage = pages[i];
        if(friendPage.$vm && typeof friendPage.$vm.loadMessages === 'function') {
          friendPage.$vm.loadMessages();
        }
        break;
      }
    }
  }
});
</script>

<style lang="scss" scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #ededed;
  position: relative;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 15rpx 25rpx;
  padding-top: var(--status-bar-height);
  background-color: #f7f7f7;
  border-bottom: 1rpx solid #e5e5e5;
  height: 88rpx;
  box-sizing: content-box;
}

.back-icon, .more-icon {
  width: 60rpx;
  height: 88rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 44rpx;
  color: #333;
}
.more-icon{
  font-size: 36rpx;
  font-weight: bold;
}

.chat-title {
  flex: 1;
  text-align: center;
  font-size: 34rpx;
  font-weight: 500;
  color: #000;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-list {
  flex: 1;
  padding: 20rpx 30rpx;
  overflow-y: auto;
}

.loading-more {
  text-align: center;
  padding: 20rpx 0;
  font-size: 24rpx;
  color: #aaa;
}

.message-item-container {
  display: flex;
  flex-direction: column;
  margin-bottom: 10rpx;
}

.timestamp {
  text-align: center;
  font-size: 24rpx;
  color: #aaa;
  padding: 20rpx 0;
}

.message-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 30rpx;
}

.self-message {
  flex-direction: row-reverse;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 8rpx;
  margin: 0 20rpx;
  flex-shrink: 0;
}

.message-content {
  position: relative;
  display: flex;
  align-items: center;
}

.message-bubble {
  max-width: calc(100vw - 240rpx);
  border-radius: 8rpx;
  padding: 18rpx 24rpx;
  background-color: #ffffff;
  word-break: break-all;
  position: relative;
  min-height: 80rpx;
  display: flex;
  align-items: center;

  &::before {
    content: '';
    position: absolute;
    top: 20rpx;
    left: -14rpx;
    width: 0;
    height: 0;
    border: 8rpx solid transparent;
    border-right-color: #ffffff;
  }
}

.self-message .message-bubble {
  background-color: #95ec69;

  &::before {
    display: none;
  }
  &::after {
    content: '';
    position: absolute;
    top: 20rpx;
    right: -14rpx;
    width: 0;
    height: 0;
    border: 8rpx solid transparent;
    border-left-color: #95ec69;
  }
}

.message-text {
  font-size: 30rpx;
  line-height: 1.6;
  color: #000;
}

.image-bubble {
  padding: 0;
  background-color: transparent;
  overflow: visible;
  border-radius: 8rpx;
  width: 200rpx;
  height: 200rpx;
  
  &::before, &::after {
    display: none;
  }
}

.message-image {
  width: 200rpx;
  height: 200rpx;
  border-radius: 8rpx;
  object-fit: cover;
}

.empty-message {
  text-align: center;
  padding: 200rpx 0;
  color: #aaa;
  font-size: 28rpx;
}

.input-area {
  padding: 15rpx 20rpx;
  background-color: #f7f7f7;
  border-top: 1rpx solid #e5e5e5;
  display: flex;
  align-items: center;
  min-height: 100rpx;
}

.input-icon-wrapper {
  min-width: 80rpx;
  height: 80rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
}

.input-icon {
   width: 56rpx;
   height: 56rpx;
}

.message-input {
  flex: 1;
  height: 70rpx;
  background-color: #ffffff;
  border-radius: 8rpx;
  padding: 0 20rpx;
  font-size: 30rpx;
  margin: 0 10rpx;
  border: 1rpx solid #ddd;
}

.emoji-panel {
  position: absolute;
  bottom: 100rpx;
  left: 0;
  right: 0;
  background-color: #f8f8f8;
  border-top: 1rpx solid #e5e5e5;
  z-index: 10;
  height: 400rpx;
}

.emoji-container {
  height: 100%;
  width: 100%;
}

.emoji-list {
  display: flex;
  flex-wrap: wrap;
  padding: 20rpx;
}

.emoji-item {
  width: 80rpx;
  height: 80rpx;
  font-size: 50rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 10rpx;
  cursor: pointer;
  
  &:active {
    background-color: #e0e0e0;
    border-radius: 8rpx;
  }
}

// 视频样式
.message-video {
  max-width: 40vw;
  max-height: 40vw;
  border-radius: 8rpx;
}

// 添加发送按钮样式
.send-btn {
  width: 100%;
  height: 100%;
  background-color: #07C160;
  color: #fff;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  padding: 0 16rpx; // 增加一点内边距让按钮更宽
}

// 优化图标包装器样式以适应发送按钮
.input-icon-wrapper {
  min-width: 80rpx;
  height: 80rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
}

// 确保输入区域内所有元素垂直居中
.input-area {
  padding: 15rpx 20rpx;
  background-color: #f7f7f7;
  border-top: 1rpx solid #e5e5e5;
  display: flex;
  align-items: center;
  min-height: 100rpx;
}
</style>
