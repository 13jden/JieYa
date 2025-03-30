<template>
	<view class="order-container">
		<!-- 选项卡切换 -->
		<view class="tabs">
			<view 
				class="tab-item" 
				:class="{ active: activeTab === 'venue' }"
				@tap="switchTab('venue')"
			>
				场地预约
			</view>
			<view 
				class="tab-item" 
				:class="{ active: activeTab === 'prop' }"
				@tap="switchTab('prop')"
			>
				道具租借
			</view>
		</view>
		
		<!-- 场地预约列表 -->
		<view v-if="activeTab === 'venue'" class="order-list">
			<view v-if="venueOrders.length === 0" class="empty-state">
				<image src="/static/images/empty.png" mode="aspectFit" class="empty-image"></image>
				<text class="empty-text">暂无场地预约记录</text>
			</view>
			
			<view v-else v-for="(order, index) in venueOrders" :key="index" class="order-item" @tap="showVenueDetail(order)">
				<view class="order-status-bar" :class="getStatusClass(order.status)">
					<text>{{ getStatusText(order.status) }}</text>
				</view>
				
				<view class="venue-image-wrapper" v-if="order.venueCoverImage">
					<image :src="order.venueCoverImage" mode="aspectFill" class="venue-cover"></image>
				</view>
				
				<view class="order-main-content">
					<view class="venue-title-row">
						<text class="venue-name">{{ order.venueName }}</text>
						<text class="order-price">¥{{ order.totalPrice.toFixed(2) }}</text>
					</view>
					
					<view class="venue-time-info">
						<view class="time-item">
							<text class="time-icon">🕒</text>
							<text class="time-text">{{ order.startTimeStr || '' }} - {{ order.endTimeStr || '' }}</text>
						</view>
					</view>
					
					<view class="order-id-row">
						<text class="order-id">订单号：{{ order.id }}</text>
					</view>
				</view>
				
				<view class="order-footer">
					<button 
						class="order-btn cancel" 
						v-if="order.status === 'PENDING' || order.status === '待支付'"
						@tap.stop="cancelOrder(order.id)"
					>
						取消预约
					</button>
					
					<button 
						class="order-btn pay" 
						v-if="order.status === 'PENDING' || order.status === '待支付'"
						@tap.stop="payOrder(order.id)"
					>
						立即支付
					</button>
				</view>
			</view>
		</view>
		
		<!-- 道具租借列表 -->
		<view v-if="activeTab === 'prop'" class="rental-list">
			<view v-if="propRentals.length === 0" class="empty-state">
				<image src="/static/images/empty.png" mode="aspectFit" class="empty-image"></image>
				<text class="empty-text">暂无道具租借记录</text>
			</view>
			
			<view v-else v-for="(rental, index) in propRentals" :key="index" class="rental-item" @tap="showPropDetail(rental)">
				<view class="rental-header">
					<text class="rental-id">订单号：{{ rental.id }}</text>
					<text class="rental-status" :class="getRentalStatusClass(rental.status)">
						{{ rental.status }}
					</text>
				</view>
				
				<view class="rental-content">
					<image v-if="rental.propCoverImage" 
						   :src="rental.propCoverImage" 
						   mode="aspectFill" 
						   class="prop-image"></image>
					
					<view class="prop-info">
						<view class="prop-name">{{ rental.propName }}</view>
						<view class="rental-time">
							<text class="time-label">开始时间：</text>
							<text class="time-value">{{ formatTime(rental.startDate) }}</text>
						</view>
						<view class="rental-time" v-if="rental.endDate">
							<text class="time-label">归还时间：</text>
							<text class="time-value">{{ formatTime(rental.endDate) }}</text>
						</view>
						<view class="rental-duration" v-if="rental.status === '租借中'">
							<text class="duration-label">已租借：</text>
							<text class="duration-value">{{ calculateDuration(rental.startDate) }}</text>
						</view>
					</view>
				</view>
				
				<view class="rental-footer">
					<view class="rental-price-container">
						<text class="price-label">租借费用：</text>
						<text class="rental-price" v-if="rental.totalPrice">¥{{ rental.totalPrice.toFixed(2) }}</text>
						<text class="rental-price estimated" v-else>{{ estimatePrice(rental) }}</text>
					</view>
					
					<button 
						class="rental-btn return" 
						v-if="rental.status === '租借中'"
						@tap="returnRental(rental.id)"
					>
						归还道具
					</button>
				</view>
			</view>
		</view>
		
		<!-- 归还确认弹窗 -->
		<view class="return-modal" v-if="showReturnModal">
			<view class="modal-content">
				<view class="modal-title">确认归还</view>
				<view class="modal-body">
					<text>道具：{{ currentRental.propName }}</text>
					<text>租借时间：{{ formatTime(currentRental.startDate) }}</text>
					<text>租借时长：{{ calculateDuration(currentRental.startDate) }}</text>
					<text class="price-estimate">预计费用：{{ estimatePrice(currentRental) }}</text>
				</view>
				<view class="modal-footer">
					<button class="modal-btn cancel" @tap="showReturnModal = false">取消</button>
					<button class="modal-btn confirm" @tap="confirmReturn">确认归还</button>
				</view>
			</view>
		</view>
		
		<!-- 场地详情弹窗 -->
		<view class="detail-modal" v-if="showVenueDetailModal">
			<view class="modal-content">
				<view class="modal-header">
					<text class="modal-title">场地预约详情</text>
					<text class="modal-close" @tap="showVenueDetailModal = false">×</text>
				</view>
				<view class="modal-body">
				<view class="detail-item" v-if="currentVenue.venueCoverImage">
						<text class="detail-label">场馆图片</text>
						<image :src="currentVenue.venueCoverImage" mode="aspectFill" class="venue-cover-image"></image>
					</view>
					<view class="detail-item">
						<text class="detail-label">场馆名称</text>
						<text class="detail-value">{{ currentVenue.venueName }}</text>
					</view>
					<view class="detail-item">
						<text class="detail-label">场馆地址</text>
						<text class="detail-value">{{ currentVenue.venueLocation || currentVenue.location }}</text>
					</view>
					<view class="detail-item">
						<text class="detail-label">预约时间</text>
						<text class="detail-value">{{ currentVenue.startTimeStr || '' }} - {{ currentVenue.endTimeStr || '' }}</text>
					</view>
					<view class="detail-item">
						<text class="detail-label">预约状态</text>
						<text class="detail-value status" :class="getStatusClass(currentVenue.status)">{{ getStatusText(currentVenue.status) }}</text>
					</view>
					<view class="detail-item">
						<text class="detail-label">订单金额</text>
						<text class="detail-value price">¥{{ currentVenue.totalPrice ? currentVenue.totalPrice.toFixed(2) : '0.00' }}</text>
					</view>
					<view class="detail-item">
						<text class="detail-label">订单号</text>
						<text class="detail-value">{{ currentVenue.id }}</text>
					</view>
					<view class="detail-item">
						<text class="detail-label">创建时间</text>
						<text class="detail-value">{{ currentVenue.createdAtStr || '' }}</text>
					</view>
					<view class="detail-item" v-if="currentVenue.venueContent">
						<text class="detail-label">场馆介绍</text>
						<text class="detail-value">{{ currentVenue.venueContent }}</text>
					</view>
					
				</view>
				<view class="modal-footer">
					<button class="modal-btn close" @tap="showVenueDetailModal = false">关闭</button>
					<button 
						class="modal-btn pay" 
						v-if="currentVenue.status === 'PENDING' || currentVenue.status === '待支付'"
						@tap="payOrder(currentVenue.id)"
					>
						去支付
					</button>
				</view>
			</view>
		</view>
		
		<!-- 道具详情弹窗 -->
		<view class="detail-modal" v-if="showPropDetailModal">
			<view class="modal-content">
				<view class="modal-header">
					<text class="modal-title">道具租借详情</text>
					<text class="modal-close" @tap="showPropDetailModal = false">×</text>
				</view>
				<view class="modal-body">
					<view class="detail-item" v-if="currentProp.propCoverImage">
						<image :src="currentProp.propCoverImage" mode="aspectFill" class="prop-cover-image"></image>
					</view>
					<view class="detail-item">
						<text class="detail-label">道具名称</text>
						<text class="detail-value">{{ currentProp.propName }}</text>
					</view>
					<view class="detail-item">
						<text class="detail-label">租借状态</text>
						<text class="detail-value status" :class="getRentalStatusClass(currentProp.status)">{{ currentProp.status }}</text>
					</view>
					<view class="detail-item">
						<text class="detail-label">开始时间</text>
						<text class="detail-value">{{ formatTime(currentProp.startDate) }}</text>
					</view>
					<view class="detail-item" v-if="currentProp.endDate">
						<text class="detail-label">归还时间</text>
						<text class="detail-value">{{ formatTime(currentProp.endDate) }}</text>
					</view>
					<view class="detail-item" v-if="currentProp.status === '租借中'">
						<text class="detail-label">已租借时长</text>
						<text class="detail-value duration">{{ calculateDuration(currentProp.startDate) }}</text>
					</view>
					<view class="detail-item">
						<text class="detail-label">租借费用</text>
						<text class="detail-value price" v-if="currentProp.totalPrice">¥{{ currentProp.totalPrice.toFixed(2) }}</text>
						<text class="detail-value price estimated" v-else>{{ estimatePrice(currentProp) }}</text>
					</view>
					<view class="detail-item">
						<text class="detail-label">订单号</text>
						<text class="detail-value">{{ currentProp.id }}</text>
					</view>
				</view>
				<view class="modal-footer">
					<button class="modal-btn close" @tap="showPropDetailModal = false">关闭</button>
					<button 
						class="modal-btn return" 
						v-if="currentProp.status === '租借中'"
						@tap="returnRental(currentProp.id)"
					>
						归还道具
					</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { getBookingHistory, cancelBooking, payForBooking } from '../../api/venue.js';
import { getRentalHistory, returnProp } from '../../api/prop.js';

// 选项卡控制
const activeTab = ref('venue'); // 默认显示场地预约
const venueOrders = ref([]);
const propRentals = ref([]);
const loading = ref(false);
const showReturnModal = ref(false);
const currentRental = ref({});
const showVenueDetailModal = ref(false);
const currentVenue = ref({});
const showPropDetailModal = ref(false);
const currentProp = ref({});

// 切换选项卡
const switchTab = (tab) => {
	activeTab.value = tab;
	
	// 切换到对应选项卡时刷新数据
	if (tab === 'venue') {
		fetchVenueOrders();
	} else if (tab === 'prop') {
		fetchPropRentals();
	}
};

// 获取URL参数，默认展示哪个tab
onLoad((options) => {
	console.log('订单页面参数:', options);
	
	if (options.activeTab) {
		// 设置当前选项卡
		activeTab.value = options.activeTab;
	}
	
	// 初始加载数据
	if (activeTab.value === 'venue') {
		fetchVenueOrders();
	} else {
		fetchPropRentals();
	}
});

// 每次页面显示时刷新数据
onShow(() => {
	if (activeTab.value === 'venue') {
		fetchVenueOrders();
	} else {
		fetchPropRentals();
	}
});

// 获取场地预约历史
const fetchVenueOrders = async () => {
	loading.value = true;
	try {
		const result = await getBookingHistory();
		
		if (result.code === 1 && result.data) {
			venueOrders.value = result.data;
		} else {
			venueOrders.value = [];
		}
	} catch (error) {
		console.error('获取场地预约历史失败:', error);
		uni.showToast({
			title: '获取预约历史失败',
			icon: 'none'
		});
	} finally {
		loading.value = false;
	}
};

// 获取道具租借历史
const fetchPropRentals = async () => {
	loading.value = true;
	try {
		const result = await getRentalHistory();
		
		if (result.code === 1 && result.data) {
			propRentals.value = result.data;
		} else {
			propRentals.value = [];
		}
	} catch (error) {
		console.error('获取道具租借历史失败:', error);
		uni.showToast({
			title: '获取租借历史失败',
			icon: 'none'
		});
	} finally {
		loading.value = false;
	}
};

// 获取场地订单状态文本
const getStatusText = (status) => {
	const statusMap = {
		'PENDING': '待支付',
		'PAID': '已支付',
		'COMPLETED': '已完成',
		'CANCELLED': '已取消'
	};
	return statusMap[status] || status;
};

// 获取状态对应的样式类
const getStatusClass = (status) => {
	if (!status) return '';
	
	switch (status) {
		case 'PENDING':
		case '待支付':
			return 'status-waiting-payment';
		case 'CONFIRMED':
		case '已确认':
			return 'status-confirmed';
		case 'COMPLETED':
		case '已完成':
			return 'status-completed';
		case 'CANCELLED':
		case '已取消':
			return 'status-cancelled';
		default:
			return '';
	}
};

// 获取道具租借状态文本
const getRentalStatusText = (status) => {
	const statusMap = {
		'ACTIVE': '租借中',
		'RETURNED': '已归还',
		'OVERDUE': '已逾期'
	};
	return statusMap[status] || status;
};

// 获取道具租借状态样式类
const getRentalStatusClass = (status) => {
	const classMap = {
		'ACTIVE': 'status-active',
		'RETURNED': 'status-returned',
		'OVERDUE': 'status-overdue'
	};
	return classMap[status] || '';
};

// 格式化时间
const formatTime = (timeStr) => {
	if (!timeStr) return '未知时间';
	
	// 简单的时间格式化，可以根据需要扩展
	const date = new Date(timeStr);
	const year = date.getFullYear();
	const month = String(date.getMonth() + 1).padStart(2, '0');
	const day = String(date.getDate()).padStart(2, '0');
	const hours = String(date.getHours()).padStart(2, '0');
	const minutes = String(date.getMinutes()).padStart(2, '0');
	
	return `${year}-${month}-${day} ${hours}:${minutes}`;
};

// 计算租借时长
const calculateDuration = (startTimeStr) => {
	if (!startTimeStr) return '未知';
	
	const startTime = new Date(startTimeStr);
	const now = new Date();
	const diffMs = now - startTime;
	
	// 小于10分钟
	if (diffMs < 10 * 60 * 1000) {
		return '不足10分钟';
	}
	
	const diffMinutes = Math.floor(diffMs / (60 * 1000));
	const diffHours = Math.floor(diffMs / (60 * 60 * 1000));
	const diffDays = Math.floor(diffMs / (24 * 60 * 60 * 1000));
	
	if (diffDays > 0) {
		return `${diffDays}天${diffHours % 24}小时`;
	} else if (diffHours > 0) {
		return `${diffHours}小时${diffMinutes % 60}分钟`;
	} else {
		return `${diffMinutes}分钟`;
	}
};

// 估算道具租借费用
const estimatePrice = (rental) => {
	if (!rental || !rental.startDate || !rental.propPrice) {
		return '¥0.00';
	}
	
	const startTime = new Date(rental.startDate);
	const now = new Date();
	const diffMs = now - startTime;
	
	// 小于10分钟不收费
	if (diffMs < 10 * 60 * 1000) {
		return '¥0.00';
	}
	
	// 计算天数，不足24小时按1天计算
	const diffDays = Math.ceil(diffMs / (24 * 60 * 60 * 1000));
	const price = diffDays * rental.propPrice;
	
	return `¥${price.toFixed(2)}`;
};

// 归还道具
const returnRental = (rentalId) => {
	// 查找对应的租借记录
	const rental = propRentals.value.find(item => item.id === rentalId);
	if (!rental) return;
	
	currentRental.value = rental;
	showReturnModal.value = true;
};

// 确认归还
const confirmReturn = async () => {
	try {
		uni.showLoading({ title: '处理中...' });
		
		const result = await returnProp(currentRental.value.id);
		
		if (result.code === 1 && result.data) {
			uni.showToast({
				title: '归还成功',
				icon: 'success'
			});
			
			// 关闭弹窗并刷新数据
			showReturnModal.value = false;
			fetchPropRentals();
			
			// 可以考虑跳转到支付页面或显示费用详情
			// ...
		} else {
			uni.showToast({
				title: result.msg || '归还失败',
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('归还道具失败:', error);
		uni.showToast({
			title: '归还道具失败',
			icon: 'none'
		});
	} finally {
		uni.hideLoading();
	}
};

// 取消场地预约
const cancelOrder = async (orderId) => {
	try {
		uni.showLoading({ title: '处理中...' });
		
		const result = await cancelBooking(orderId);
		
		if (result.code === 1) {
			uni.showToast({
				title: '取消成功',
				icon: 'success'
			});
			fetchVenueOrders();
		} else {
			uni.showToast({
				title: result.msg || '取消失败',
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('取消预约失败:', error);
		uni.showToast({
			title: '取消预约失败',
			icon: 'none'
		});
	} finally {
		uni.hideLoading();
	}
};

// 支付场地预约
const payOrder = (orderId) => {
	uni.navigateTo({
		url: `/pages/payment/payment?orderId=${orderId}`
	});
};

// 跳转到我的预约（场地）
const goToMyVenueBookings = () => {
	uni.navigateTo({
		url: '/pages/order/order?activeTab=venue'
	});
};

// 跳转到我的租借（道具）
const goToMyPropRentals = () => {
	uni.navigateTo({
		url: '/pages/order/order?activeTab=prop'
	});
};

// 显示场地订单详情
const showVenueDetail = (order) => {
	currentVenue.value = order;
	showVenueDetailModal.value = true;
};

// 显示道具详情
const showPropDetail = (rental) => {
	currentProp.value = rental;
	showPropDetailModal.value = true;
};
</script>

<style lang="scss">
.order-container {
	background-color: #f5f5f5;
	min-height: 100vh;
	padding-bottom: 30rpx;
}

/* 选项卡样式 */
.tabs {
	display: flex;
	background-color: #ffffff;
	padding: 20rpx 0;
	margin-bottom: 20rpx;
	position: sticky;
	top: 0;
	z-index: 10;
}

.tab-item {
	flex: 1;
	text-align: center;
	font-size: 30rpx;
	color: #666;
	padding: 16rpx 0;
	position: relative;
	
	&.active {
		color: #3f8cff;
		font-weight: 500;
		
		&::after {
			content: '';
			position: absolute;
			bottom: -10rpx;
			left: 50%;
			transform: translateX(-50%);
			width: 60rpx;
			height: 6rpx;
			background-color: #3f8cff;
			border-radius: 3rpx;
		}
	}
}

/* 空状态样式 */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 100rpx 0;
}

.empty-image {
	width: 200rpx;
	height: 200rpx;
	margin-bottom: 30rpx;
}

.empty-text {
	font-size: 28rpx;
	color: #999;
}

/* 场地预约列表样式 */
.order-list {
	padding: 20rpx;
}

.order-item {
	background-color: #ffffff;
	border-radius: 16rpx;
	margin-bottom: 30rpx;
	overflow: hidden;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
	position: relative;
}

.order-status-bar {
	position: absolute;
	top: 0;
	right: 0;
	padding: 8rpx 24rpx;
	color: #fff;
	font-size: 24rpx;
	border-bottom-left-radius: 16rpx;
	z-index: 2;
}

.venue-image-wrapper {
	width: 100%;
	height: 320rpx;
	overflow: hidden;
	position: relative;
}

.venue-cover {
	width: 100%;
	height: 100%;
	object-fit: cover;
	transition: transform 0.3s ease;
}

.order-item:active .venue-cover {
	transform: scale(1.05);
}

.order-main-content {
	padding: 24rpx;
}

.venue-title-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.venue-name {
	font-size: 34rpx;
	font-weight: 600;
	color: #333;
	flex: 1;
}

.order-price {
	font-size: 34rpx;
	font-weight: 600;
	color: #ff5a5f;
}

.venue-time-info {
	margin-bottom: 16rpx;
}

.time-item {
	display: flex;
	align-items: center;
	margin-bottom: 10rpx;
}

.time-icon {
	margin-right: 10rpx;
	font-size: 28rpx;
}

.time-text {
	font-size: 28rpx;
	color: #666;
}

.order-id-row {
	margin-top: 16rpx;
	padding-top: 16rpx;
	border-top: 1rpx solid #f0f0f0;
}

.order-id {
	font-size: 26rpx;
	color: #999;
}

.order-footer {
	display: flex;
	justify-content: flex-end;
	padding: 20rpx 24rpx;
	border-top: 1rpx solid #f0f0f0;
	background-color: #fafafa;
}

.order-btn {
	font-size: 28rpx;
	padding: 12rpx 36rpx;
	margin-left: 20rpx;
	border-radius: 100rpx;
	border: none;
}

.order-btn.cancel {
	background-color: #f5f5f5;
	color: #666;
}

.order-btn.pay {
	background-color: #3f8cff;
	color: #fff;
}

/* 状态颜色 */
.status-pending, .status-waiting-payment {
	background-color: #ff9500;
}

.status-confirmed {
	background-color: #34c759;
}

.status-completed {
	background-color: #5ac8fa;
}

.status-cancelled {
	background-color: #8e8e93;
}

/* 归还确认弹窗样式 */
.return-modal {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.6);
	display: flex;
	justify-content: center;
	align-items: center;
	z-index: 100;
}

.modal-content {
	width: 80%;
	background-color: #fff;
	border-radius: 16rpx;
	overflow: hidden;
}

.modal-title {
	text-align: center;
	padding: 30rpx 0;
	font-size: 32rpx;
	font-weight: 500;
	border-bottom: 1rpx solid #f0f0f0;
}

.modal-body {
	padding: 30rpx;
	display: flex;
	flex-direction: column;
}

.modal-body text {
	margin-bottom: 20rpx;
	font-size: 28rpx;
	color: #333;
}

.price-estimate {
	color: #ff5a5f;
	font-weight: 500;
	font-size: 32rpx;
}

.modal-footer {
	display: flex;
	border-top: 1rpx solid #f0f0f0;
}

.modal-btn {
	flex: 1;
	text-align: center;
	padding: 24rpx 0;
	font-size: 30rpx;
	background-color: #fff;
	border: none;
	border-radius: 0;
}

.modal-btn.cancel {
	background-color: #f5f5f5;
	color: #666;
}

.modal-btn.confirm {
	background-color: #3f8cff;
	color: #fff;
}

/* 道具租借列表样式 */
.rental-list {
	padding: 0 20rpx;
}

.rental-item {
	background-color: #ffffff;
	border-radius: 16rpx;
	margin-bottom: 20rpx;
	overflow: hidden;
	box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
}

.rental-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 24rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.rental-id {
	font-size: 26rpx;
	color: #666;
}

.rental-status {
	font-size: 26rpx;
	font-weight: 500;
	padding: 6rpx 16rpx;
	border-radius: 100rpx;
}

.rental-content {
	display: flex;
	padding: 24rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.prop-image {
	width: 160rpx;
	height: 160rpx;
	border-radius: 8rpx;
	margin-right: 20rpx;
	background-color: #f5f5f5;
}

.prop-info {
	flex: 1;
}

.prop-name {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 12rpx;
}

.rental-time, .rental-duration {
	font-size: 26rpx;
	color: #666;
	margin-bottom: 8rpx;
	display: flex;
}

.time-label, .duration-label {
	color: #999;
	width: 150rpx;
}

.time-value, .duration-value {
	color: #333;
	flex: 1;
}

.duration-value {
	color: #3f8cff;
	font-weight: 500;
}

.rental-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 24rpx;
}

.rental-price-container {
	display: flex;
	align-items: center;
}

.price-label {
	font-size: 26rpx;
	color: #999;
}

.rental-price {
	font-size: 32rpx;
	font-weight: 600;
	color: #ff5a5f;
}

.rental-price.estimated {
	font-size: 28rpx;
	color: #ff9500;
}

.rental-btn {
	font-size: 26rpx;
	padding: 12rpx 30rpx;
	border-radius: 100rpx;
	border: none;
	background-color: #3f8cff;
	color: #ffffff;
	line-height: 1.4;
}

.rental-btn.return {
	background-color: #3f8cff;
}

/* 新增订单详情卡片样式 */
.venue-create-time {
	font-size: 24rpx;
	color: #999;
	margin-top: 6rpx;
	display: block;
}

.order-price-container {
	display: flex;
	flex-direction: column;
	align-items: flex-end;
	justify-content: center;
}

.price-label {
	font-size: 24rpx;
	color: #999;
	margin-bottom: 4rpx;
}

/* 详情弹窗样式 */
.detail-modal {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.6);
	display: flex;
	justify-content: center;
	align-items: center;
	z-index: 100;
}

.modal-content {
	width: 90%;
	max-height: 80vh;
	background-color: #fff;
	border-radius: 16rpx;
	overflow: hidden;
	display: flex;
	flex-direction: column;
}

.modal-header {
	padding: 30rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
	font-size: 34rpx;
	font-weight: 600;
	color: #333;
}

.modal-close {
	font-size: 40rpx;
	color: #999;
	line-height: 1;
}

.modal-body {
	padding: 30rpx;
	flex: 1;
	overflow-y: auto;
}

.detail-item {
	margin-bottom: 20rpx;
}

.detail-label {
	font-size: 28rpx;
	color: #999;
	margin-bottom: 8rpx;
	display: block;
}

.detail-value {
	font-size: 30rpx;
	color: #333;
}

.detail-value.status {
	display: inline-block;
	padding: 4rpx 16rpx;
	border-radius: 100rpx;
	font-size: 26rpx;
}

.detail-value.price {
	color: #ff5a5f;
	font-weight: 600;
}

.detail-value.duration {
	color: #3f8cff;
	font-weight: 500;
}

.venue-cover-image, .prop-cover-image {
	width: 100%;
	height: 300rpx;
	border-radius: 12rpx;
	margin-top: 10rpx;
}

.modal-footer {
	display: flex;
	border-top: 1rpx solid #f0f0f0;
}

.modal-btn {
	flex: 1;
	text-align: center;
	padding: 24rpx 0;
	font-size: 30rpx;
	background-color: #fff;
	border: none;
	border-radius: 0;
}

.modal-btn.close {
	background-color: #f5f5f5;
	color: #666;
}

.modal-btn.pay {
	background-color: #3f8cff;
	color: #fff;
}

.modal-btn.return {
	background-color: #3f8cff;
	color: #fff;
}
</style>
