<template>
	<view class="system-message-container">

	  
	  <scroll-view scroll-y class="message-list" refresher-enabled @refresherrefresh="onRefresh" :refresher-triggered="isRefreshing">
		<view v-for="(item, index) in messageList" :key="index" class="message-card">
		  <view class="message-header">
			<text class="message-title">{{ item.title || '系统通知' }}</text>
			<text class="message-time">{{ formatTime(item.time) }}</text>
		  </view>
		  <view class="message-content">
			<text>{{ item.content }}</text>
		  </view>
		  <view class="message-actions" v-if="item.actionType">
			<view class="action-btn" @tap="handleAction(item)">{{ getActionText(item.actionType) }}</view>
		  </view>
		</view>
		
		<!-- 分页加载 -->
		<view class="load-more" v-if="hasMore && !isLoading" @tap="loadMore">
		  加载更多
		</view>
		
		<view class="loading" v-if="isLoading">
		  <text>加载中...</text>
		</view>
		
		<!-- 空状态 -->
		<view v-if="messageList.length === 0 && !isLoading" class="empty-state">
		  <image src="/static/empty-notification.png" mode="aspectFit" class="empty-image"></image>
		  <text class="empty-text">暂无{{ pageTitle }}</text>
		</view>
	  </scroll-view>
	</view>
  </template>
  
  <script setup>
  import { ref, computed, onMounted } from 'vue';
  import { getSystemMessages, markSystemMessagesAsRead } from '@/api/message'; // 假设有这个API
  
  const messageList = ref([]);
  const isLoading = ref(true);
  const isRefreshing = ref(false);
  const hasMore = ref(true);
  const currentPage = ref(1);
  const pageSize = ref(20);
  const messageType = ref('SYSTEM'); // 默认为系统消息，也可以是'ADMIN'或'ORDER'
  
  // 根据消息类型计算页面标题
  const pageTitle = computed(() => {
	switch (messageType.value) {
	  case 'SYSTEM':
		return '系统消息';
	  case 'ADMIN':
		return '管理员消息';
	  case 'ORDER':
		return '订单消息';
	  default:
		return '系统消息';
	}
  });
  
  // 加载消息
  const loadMessages = async (refresh = false) => {
	if (refresh) {
	  currentPage.value = 1;
	}
	
	isLoading.value = true;
	try {
	  const res = await getSystemMessages(currentPage.value, pageSize.value);
	  if (res.code === 1) {
		if (refresh) {
		  messageList.value = res.data.records || [];
		  // 标记系统消息为已读
		  await markSystemMessagesAsRead();
		} else {
		  messageList.value = [...messageList.value, ...(res.data.records || [])];
		}
		
		hasMore.value = currentPage.value * pageSize.value < res.data.total;
	  }
	} catch (error) {
	  console.error('获取系统消息失败:', error);
	  uni.showToast({
		title: '获取消息失败',
		icon: 'none'
	  });
	} finally {
	  isLoading.value = false;
	  isRefreshing.value = false;
	}
  };
  
  // 下拉刷新
  const onRefresh = () => {
	isRefreshing.value = true;
	loadMessages(true);
  };
  
  // 加载更多
  const loadMore = () => {
	if (hasMore.value && !isLoading.value) {
	  currentPage.value++;
	  loadMessages();
	}
  };
  
  // 处理操作按钮点击
  const handleAction = (item) => {
	console.log('处理操作:', item);
	
	switch (item.actionType) {
	  case 'VIEW_NOTE':
		// 跳转到笔记详情
		uni.navigateTo({
		  url: `/pages/note/note?id=${item.targetId}`
		});
		break;
	  case 'VIEW_ORDER':
		// 跳转到订单详情
		uni.navigateTo({
		  url: `/pages/order/orderDetail?id=${item.targetId}`
		});
		break;
	  case 'VIEW_USER':
		// 跳转到用户详情
		uni.navigateTo({
		  url: `/pages/user/user?id=${item.targetId}`
		});
		break;
	  default:
		break;
	}
  };
  
  // 获取操作按钮文本
  const getActionText = (actionType) => {
	switch (actionType) {
	  case 'VIEW_NOTE':
		return '查看笔记';
	  case 'VIEW_ORDER':
		return '查看订单';
	  case 'VIEW_USER':
		return '查看用户';
	  default:
		return '查看详情';
	}
  };
  
  // 格式化时间
  const formatTime = (time) => {
	if (!time) return '';
	
	const date = new Date(time);
	const year = date.getFullYear();
	const month = (date.getMonth() + 1).toString().padStart(2, '0');
	const day = date.getDate().toString().padStart(2, '0');
	const hours = date.getHours().toString().padStart(2, '0');
	const minutes = date.getMinutes().toString().padStart(2, '0');
	
	return `${year}-${month}-${day} ${hours}:${minutes}`;
  };
  
  // 返回
  const goBack = () => {
	uni.navigateBack();
  };
  
  // 获取页面参数
  onMounted(() => {
	const pages = getCurrentPages();
	const page = pages[pages.length - 1];
	if (page && page.options && page.options.type) {
	  messageType.value = page.options.type.toUpperCase();
	}
	
	loadMessages(true);
  });
  </script>
  
  <style lang="scss" scoped>
  .system-message-container {
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
	justify-content: space-between;
	background-color: #ffffff;
	padding: 0 16px;
	border-bottom: 1px solid #f0f0f0;
  }
  
  .back-btn {
	width: 24px;
	height: 24px;
	display: flex;
	align-items: center;
	justify-content: center;
  }
  
  .back-icon {
	font-size: 20px;
	color: #333;
  }
  
  .title {
	font-size: 18px;
	font-weight: 600;
	color: #333;
  }
  
  .placeholder {
	width: 24px;
  }
  
  .message-list {
	flex: 1;
	padding: 16px;
  }
  
  .message-card {
	background-color: #ffffff;
	border-radius: 8px;
	padding: 16px;
	margin-bottom: 16px;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  }
  
  .message-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 10px;
  }
  
  .message-title {
	font-size: 16px;
	font-weight: 500;
	color: #333;
  }
  
  .message-time {
	font-size: 12px;
	color: #999;
  }
  
  .message-content {
	font-size: 14px;
	color: #666;
	line-height: 1.5;
	margin-bottom: 16px;
  }
  
  .message-actions {
	display: flex;
	justify-content: flex-end;
  }
  
  .action-btn {
	display: inline-block;
	padding: 6px 12px;
	background-color: #1aad19;
	color: #fff;
	font-size: 14px;
	border-radius: 4px;
  }
  
  .load-more {
	text-align: center;
	color: #666;
	font-size: 14px;
	padding: 16px 0;
  }
  
  .loading {
	text-align: center;
	color: #999;
	font-size: 14px;
	padding: 16px 0;
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
  </style>