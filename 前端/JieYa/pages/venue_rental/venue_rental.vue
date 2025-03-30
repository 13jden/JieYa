<template>
	<view class="rental-container">
		<view class="booking-card">
			<view class="card-header">
				<text class="header-title">预约确认</text>
			</view>
			
			<view class="venue-info">
				<view class="info-item">
					<text class="item-label">场地名称</text>
					<text class="item-value">{{ venueName }}</text>
				</view>
				
				<view class="info-item">
					<text class="item-label">预约时间</text>
					<text class="item-value">{{ formatDateTime(startTime) }} 至 {{ formatDateTime(endTime) }}</text>
				</view>
				
				<view class="info-item">
					<text class="item-label">预约时长</text>
					<text class="item-value">{{ calculateDuration() }}小时</text>
				</view>
				
				<view class="info-item">
					<text class="item-label">订单金额</text>
					<text class="item-value price">¥{{ price }}</text>
				</view>
			</view>
			
			<view class="notes-section">
				<text class="notes-title">预约须知</text>
				<view class="notes-content">
					<text class="note-item">• 请按时到达，迟到不补时</text>
					<text class="note-item">• 提前15分钟取消可全额退款</text>
					<text class="note-item">• 场馆设施损坏需赔偿</text>
					<text class="note-item">• 禁止携带宠物入内</text>
				</view>
			</view>
		</view>
		
		<view class="action-section">
			<view class="price-summary">
				<text class="total-label">实付金额</text>
				<text class="total-price">¥{{ price }}</text>
			</view>
			
			<button class="submit-btn" @tap="submitBooking" :disabled="submitting">
				{{ submitting ? '提交中...' : '提交订单' }}
			</button>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { createBooking } from '../../api/venue.js';

// 订单数据
const venueId = ref(null);
const venueName = ref('');
const startTime = ref('');
const endTime = ref('');
const price = ref(0);
const submitting = ref(false);

onLoad((options) => {
	venueId.value = options.venueId;
	venueName.value = decodeURIComponent(options.venueName || '');
	startTime.value = decodeURIComponent(options.startTime || '');
	endTime.value = decodeURIComponent(options.endTime || '');
	price.value = options.price || 0;
	
	console.log('预约信息:', {
		venueId: venueId.value,
		venueName: venueName.value,
		startTime: startTime.value,
		endTime: endTime.value,
		price: price.value
	});
});

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
	if (!dateTimeStr) return '';
	
	// 假设格式是 "YYYY-MM-DD HH:mm"
	const parts = dateTimeStr.split(' ');
	if (parts.length !== 2) return dateTimeStr;
	
	// 只显示月日和时间
	const dateParts = parts[0].split('-');
	if (dateParts.length !== 3) return dateTimeStr;
	
	return `${dateParts[1]}-${dateParts[2]} ${parts[1]}`;
};

// 计算预约时长
const calculateDuration = () => {
	if (!startTime.value || !endTime.value) return 0;
	
	try {
		const start = new Date(startTime.value);
		const end = new Date(endTime.value);
		const diffMs = end - start;
		const diffHours = diffMs / (1000 * 60 * 60);
		return diffHours.toFixed(1);
	} catch (e) {
		console.error('计算时长错误:', e);
		return 0;
	}
};

// 提交订单
const submitBooking = async () => {
	if (submitting.value) return;
	
	if (!venueId.value || !startTime.value || !endTime.value) {
		uni.showToast({
			title: '预约信息不完整',
			icon: 'none'
		});
		return;
	}
	
	submitting.value = true;
	
	try {
		const result = await createBooking(
			venueId.value, 
			startTime.value, 
			endTime.value
		);
		
		console.log('预约结果:', result);
		
		if (result.code === 1) {
			uni.showToast({
				title: '预约成功',
				icon: 'success'
			});
			
			// 跳转到订单页面
			setTimeout(() => {
				uni.redirectTo({
					url: '/pages/order/order?activeTab=venue'
				});
			}, 1500);
		} else {
			uni.showToast({
				title: result.msg || '预约失败',
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('预约错误:', error);
		uni.showToast({
			title: '预约失败，请重试',
			icon: 'none'
		});
	} finally {
		submitting.value = false;
	}
};
</script>

<style lang="scss" scoped>
.rental-container {
	display: flex;
	flex-direction: column;
	min-height: 100vh;
	background-color: #f5f7fa;
	padding-bottom: 180rpx; // 为底部按钮留出空间
}

.booking-card {
	margin: 30rpx;
	border-radius: 20rpx;
	background-color: #ffffff;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	overflow: hidden;
}

.card-header {
	padding: 30rpx;
	border-bottom: 2rpx solid #f2f2f2;
	
	.header-title {
		font-size: 36rpx;
		color: #333;
		font-weight: 600;
	}
}

.venue-info {
	padding: 30rpx;
	
	.info-item {
		display: flex;
		justify-content: space-between;
		margin-bottom: 30rpx;
		
		&:last-child {
			margin-bottom: 0;
		}
		
		.item-label {
			font-size: 28rpx;
			color: #666;
		}
		
		.item-value {
			font-size: 28rpx;
			color: #333;
			font-weight: 500;
			
			&.price {
				color: #ff6b81;
				font-weight: 600;
			}
		}
	}
}

.notes-section {
	padding: 30rpx;
	background-color: #f9f9f9;
	
	.notes-title {
		font-size: 30rpx;
		color: #333;
		font-weight: 500;
		margin-bottom: 20rpx;
	}
	
	.notes-content {
		.note-item {
			display: block;
			font-size: 26rpx;
			color: #666;
			line-height: 1.8;
		}
	}
}

.action-section {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	display: flex;
	align-items: center;
	padding: 20rpx 30rpx;
	background-color: #ffffff;
	box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
	
	.price-summary {
		flex: 1;
		
		.total-label {
			font-size: 26rpx;
			color: #666;
			margin-right: 10rpx;
		}
		
		.total-price {
			font-size: 38rpx;
			color: #ff6b81;
			font-weight: bold;
		}
	}
	
	.submit-btn {
		width: 300rpx;
		height: 80rpx;
		line-height: 80rpx;
		border-radius: 40rpx;
		background-color: #1a73e8;
		color: #ffffff;
		font-size: 30rpx;
		text-align: center;
		
		&:disabled {
			background-color: #cccccc;
		}
	}
}
</style>
