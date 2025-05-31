<template>
	<view class="order-message-container">

		
		<scroll-view scroll-y class="message-list" refresher-enabled @refresherrefresh="onRefresh" :refresher-triggered="isRefreshing">
			<view v-for="(item, index) in orderMessages" :key="index" class="order-card" @tap="viewOrderDetail(item)">
				<view class="order-header">
					<view class="order-status" :class="getStatusClass(item.status)">{{ getStatusText(item.status) }}</view>
					<text class="order-time">{{ formatTime(item.time) }}</text>
				</view>
				
				<view class="order-info">
					<view class="order-title">{{ item.title || '订单通知' }}</view>
					<view class="order-desc">{{ item.content }}</view>
				</view>
				
				<view class="order-footer">
					<text class="order-id">订单号: {{ item.orderNo || '未知' }}</text>
					<view class="order-btn">查看详情</view>
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
			<view v-if="orderMessages.length === 0 && !isLoading" class="empty-state">
				<image src="/static/empty-order.png" mode="aspectFit" class="empty-image"></image>
				<text class="empty-text">暂无订单消息</text>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getOrderMessages, markOrderMessagesAsRead } from '@/api/message'; // 假设有这个API

const orderMessages = ref([]);
const isLoading = ref(true);
const isRefreshing = ref(false);
const hasMore = ref(true);
const currentPage = ref(1);
const pageSize = ref(20);

// 加载订单消息
const loadOrderMessages = async (refresh = false) => {
	if (refresh) {
		currentPage.value = 1;
	}
	
	isLoading.value = true;
	try {
		const res = await getOrderMessages(currentPage.value, pageSize.value);
		if (res.code === 1) {
			if (refresh) {
				orderMessages.value = res.data.records || [];
				// 标记订单消息为已读
				await markOrderMessagesAsRead();
			} else {
				orderMessages.value = [...orderMessages.value, ...(res.data.records || [])];
			}
			
			hasMore.value = currentPage.value * pageSize.value < res.data.total;
		}
	} catch (error) {
		console.error('获取订单消息失败:', error);
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
	loadOrderMessages(true);
};

// 加载更多
const loadMore = () => {
	if (hasMore.value && !isLoading.value) {
		currentPage.value++;
		loadOrderMessages();
	}
};

// 查看订单详情
const viewOrderDetail = (item) => {
	uni.navigateTo({
		url: `/pages/order/orderDetail?id=${item.orderId}`
	});
};

// 获取订单状态文本
const getStatusText = (status) => {
	const statusMap = {
		'PENDING': '待支付',
		'PAID': '已支付',
		'PROCESSING': '处理中',
		'COMPLETED': '已完成',
		'CANCELLED': '已取消',
		'REFUNDING': '退款中',
		'REFUNDED': '已退款'
	};
	
	return statusMap[status] || '未知状态';
};

// 获取订单状态样式类
const getStatusClass = (status) => {
	const classMap = {
		'PENDING': 'status-pending',
		'PAID': 'status-paid',
		'PROCESSING': 'status-processing',
		'COMPLETED': 'status-completed',
		'CANCELLED': 'status-cancelled',
		'REFUNDING': 'status-refunding',
		'REFUNDED': 'status-refunded'
	};
	
	return classMap[status] || '';
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

// 生命周期
onMounted(() => {
	loadOrderMessages(true);
});
</script>

<style lang="scss" scoped>
.order-message-container {
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

.order-card {
	background-color: #ffffff;
	border-radius: 8px;
	padding: 16px;
	margin-bottom: 16px;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.order-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 12px;
}

.order-status {
	font-size: 14px;
	font-weight: 500;
	padding: 2px 8px;
	border-radius: 4px;
	
	&.status-pending {
		color: #faad14;
		background-color: #fffbe6;
	}
	
	&.status-paid {
		color: #52c41a;
		background-color: #f6ffed;
	}
	
	&.status-processing {
		color: #1890ff;
		background-color: #e6f7ff;
	}
	
	&.status-completed {
		color: #52c41a;
		background-color: #f6ffed;
	}
	
	&.status-cancelled {
		color: #ff4d4f;
		background-color: #fff2f0;
	}
	
	&.status-refunding {
		color: #722ed1;
		background-color: #f9f0ff;
	}
	
	&.status-refunded {
		color: #13c2c2;
		background-color: #e6fffb;
	}
}

.order-time {
	font-size: 12px;
	color: #999;
}

.order-info {
	margin-bottom: 12px;
	
	.order-title {
		font-size: 16px;
		font-weight: 500;
		color: #333;
		margin-bottom: 8px;
	}
	
	.order-desc {
		font-size: 14px;
		color: #666;
		line-height: 1.5;
	}
}

.order-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-top: 12px;
	border-top: 1px solid #f0f0f0;
	
	.order-id {
		font-size: 12px;
		color: #999;
	}
	
	.order-btn {
		padding: 6px 12px;
		background-color: #1aad19;
		color: #fff;
		font-size: 14px;
		border-radius: 4px;
		
		&:active {
			opacity: 0.8;
		}
	}
}

.load-more {
	text-align: center;
	padding: 16px;
	color: #666;
	font-size: 14px;
	
	&:active {
		opacity: 0.7;
	}
}

.loading {
	text-align: center;
	padding: 16px;
	color: #999;
	font-size: 14px;
}

.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 100px 0;
	
	.empty-image {
		width: 120px;
		height: 120px;
		margin-bottom: 16px;
	}
	
	.empty-text {
		color: #999;
		font-size: 14px;
	}
}
</style>
