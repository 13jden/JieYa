<template>
	<view class="venue-detail-container">
		<!-- 轮播图 -->
		<swiper class="swiper" indicator-dots autoplay circular :interval="3000">
			<swiper-item v-for="(image, index) in venueDetail.imageList || []" :key="index">
				<image :src="image.imageUrl" mode="aspectFill" class="swiper-image"></image>
			</swiper-item>
		</swiper>
		
		<!-- 场地基本信息 -->
		<view class="info-section">
			<view class="title">{{ venueDetail.name }}</view>
			<view class="location">
				<text class="location-icon">📍</text>
				<text class="location-text">{{ venueDetail.address }}</text>
			</view>
			<view class="tags" v-if="venueDetail.tagNames && venueDetail.tagNames.length > 0">
				<text v-for="(tag, index) in venueDetail.tagNames" :key="index" class="tag">{{ tag }}</text>
			</view>
			<view class="price">
				¥{{ venueDetail.price || '0.00' }}<text class="price-unit">/小时</text>
			</view>
			<view class="capacity" v-if="venueDetail.capacity">容纳人数: {{ venueDetail.capacity }}人</view>
		</view>
		
		<!-- 分割线 -->
		<view class="divider"></view>
		
		<!-- 场地介绍 -->
		<view class="intro-section">
			<view class="section-title">场地介绍</view>
			<view class="intro-content">{{ venueDetail.content }}</view>
			<view class="intro-images">
				<image 
					v-for="(detailImg, index) in venueDetail.detailImageList" 
					:key="index" 
					:src="detailImg.imageUrl" 
					mode="widthFix" 
					class="intro-image"
				></image>
			</view>
		</view>
		
		<!-- 分割线 -->
		<view class="divider"></view>
		
		<!-- 可预约时间段 -->
		<view class="time-section">
			<view class="section-title">可预约时间</view>
			<view class="date-selector">
				<scroll-view scroll-x="true" class="date-scroll">
					<view 
						class="date-item" 
						v-for="(date, index) in availableDates" 
						:key="index"
						:class="{ active: selectedDate === date }"
						@tap="onDateChange(date)"
					>
						<text class="date-day">{{ date.split('-')[2] }}</text>
						<text class="date-date">{{ date.split('-')[1] }}/{{ date.split('-')[0].substr(2) }}</text>
					</view>
				</scroll-view>
			</view>
			
			<view class="time-slots-info">
				<text class="time-slots-tip">选择连续的时间段，可多选</text>
			</view>
			
			<view class="time-slots">
				<view 
					class="time-slot" 
					v-for="(slot, index) in timeSlots" 
					:key="index"
					:class="{ 
						unavailable: !slot.available, 
						selected: isSlotSelected(index)
					}"
					@tap="selectTimeSlot(index)"
				>
					{{ slot.startTime }} - {{ slot.endTime }}
				</view>
			</view>
		</view>
		
		<!-- 底部预约按钮 -->
		<view class="bottom-bar">
			<view class="price-summary">
				<text class="total-price">¥{{ calculatedPrice }}</text>
				<text class="price-note" v-if="selectedTimeIndices.length > 0">
					{{ getSelectedTimeRange() }}
				</text>
			</view>
			<view class="book-btn" :class="{ disabled: selectedTimeIndices.length === 0 }" @tap="goToBooking">立即预约</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getVenueDetail, getAvailableTimeSlots, createBooking } from '../../api/venue.js';

// 场地详情数据
const venueDetail = ref({});
const loading = ref(false);

// 预定相关
const selectedDate = ref('');
const availableDates = ref([]);
const timeSlots = ref([]);
const selectedTimeIndices = ref([]);

// 计算总价
const calculatedPrice = computed(() => {
	if (!venueDetail.value.price || selectedTimeIndices.value.length === 0) {
		return '0.00';
	}
	// 半小时一个时间段，所以乘以0.5得到小时数
	return (venueDetail.value.price * selectedTimeIndices.value.length * 0.5).toFixed(2);
});

// 判断时间段是否被选中
const isSlotSelected = (index) => {
	return selectedTimeIndices.value.includes(index);
};

// 获取选中的时间范围描述
const getSelectedTimeRange = () => {
	if (selectedTimeIndices.value.length === 0 || !timeSlots.value.length) {
		return '';
	}
	
	const startIndex = Math.min(...selectedTimeIndices.value);
	const endIndex = Math.max(...selectedTimeIndices.value);
	
	return `${timeSlots.value[startIndex].startTime} - ${timeSlots.value[endIndex].endTime}`;
};

// 页面加载时获取场地详情
onLoad(async (options) => {
	console.log('接收到的参数:', options);

	const venueId = Number(options.id);
	if (!venueId) {
		uni.showToast({
			title: '场地ID不存在',
			icon: 'none'
		});
		return;
	}

	try {
		uni.showLoading({
			title: '加载中...'
		});

		const result = await getVenueDetail(venueId);
		console.log('场地详情:', result);

		if (result.code === 1) {
			venueDetail.value = result.data;
			// 生成可选日期并获取第一天的时间段
			generateAvailableDates();
			if (availableDates.value.length > 0) {
				await fetchTimeSlots(venueId, availableDates.value[0]);
			}
		} else {
			uni.showToast({
				title: result.msg || '获取场地详情失败',
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('获取场地详情失败:', error);
		uni.showToast({
			title: '获取场地详情失败',
			icon: 'none'
		});
	} finally {
		uni.hideLoading();
	}
});

// 生成未来7天的日期
const generateAvailableDates = () => {
	const dates = [];
	const today = new Date();
	
	for (let i = 0; i < 7; i++) {
		const date = new Date();
		date.setDate(today.getDate() + i);
		const formattedDate = formatDate(date);
		dates.push(formattedDate);
	}
	
	availableDates.value = dates;
	selectedDate.value = dates[0]; // 默认选择第一天
};

// 格式化日期为YYYY-MM-DD
const formatDate = (date) => {
	const year = date.getFullYear();
	const month = String(date.getMonth() + 1).padStart(2, '0');
	const day = String(date.getDate()).padStart(2, '0');
	return `${year}-${month}-${day}`; // 确保格式为 yyyy-MM-dd
};

// 获取可用时间段
const fetchTimeSlots = async (venueId, date) => {
	if (!venueId || !date) return;
	
	try {
		uni.showLoading({ title: '加载时间段...' });
		
		console.log('请求参数:', { venueId, date }); // 调试用
		const result = await getAvailableTimeSlots({
			venueId: Number(venueId), // 确保 venueId 是数字类型
			date: formatDate(new Date(date)) // 确保日期格式正确
		});
		console.log('时间段数据:', result);
		
		if (result.code === 1 && result.data) {
			timeSlots.value = result.data;
		} else {
			timeSlots.value = [];
			uni.showToast({
				title: result.msg || '获取时间段失败',
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('获取时间段失败:', error);
		uni.showToast({
			title: '获取时间段失败',
			icon: 'none'
		});
		timeSlots.value = [];
	} finally {
		uni.hideLoading();
	}
};

// 当日期选择改变时
const onDateChange = async (date) => {
	selectedDate.value = date;
	selectedTimeIndices.value = []; // 清空已选时间段
	
	// 获取该日期的时间段
	await fetchTimeSlots(venueDetail.value.id, date);
};

// 选择时间段
const selectTimeSlot = (index) => {
	// 检查是否可用
	if (!timeSlots.value[index].available) {
		return;
	}
	
	// 如果当前没有选择，直接添加
	if (selectedTimeIndices.value.length === 0) {
		selectedTimeIndices.value.push(index);
		return;
	}
	
	// 找出当前选择的最小和最大索引
	const minIndex = Math.min(...selectedTimeIndices.value);
	const maxIndex = Math.max(...selectedTimeIndices.value);
	
	// 检查是否是连续的选择
	if (index === minIndex - 1) {
		// 向前扩展选择
		selectedTimeIndices.value.push(index);
	} else if (index === maxIndex + 1) {
		// 向后扩展选择
		selectedTimeIndices.value.push(index);
	} else if (index >= minIndex && index <= maxIndex) {
		// 取消选择已选中的时间段
		selectedTimeIndices.value = selectedTimeIndices.value.filter(i => i !== index);
	} else {
		// 不连续的选择，给出提示但不清空
		uni.showToast({
			title: '请选择连续的时间段',
			icon: 'none'
		});
	}
	
	// 检查所有连续时间段是否可用
	validateSelectedSlots();
};

// 验证选中的时间段是否都可用且连续
const validateSelectedSlots = () => {
	if (selectedTimeIndices.value.length === 0) return;
	
	// 排序选中的索引
	selectedTimeIndices.value.sort((a, b) => a - b);
	
	// 检查是否连续
	let isValid = true;
	const first = selectedTimeIndices.value[0];
	
	for (let i = 0; i < selectedTimeIndices.value.length; i++) {
		// 检查是否连续
		if (selectedTimeIndices.value[i] !== first + i) {
			isValid = false;
			break;
		}
		
		// 检查是否可用
		if (!timeSlots.value[selectedTimeIndices.value[i]].available) {
			isValid = false;
			break;
		}
	}
	
	// 如果不有效，重置选择
	if (!isValid) {
		selectedTimeIndices.value = [];
		uni.showToast({
			title: '请选择连续且可用的时间段',
			icon: 'none'
		});
	}
};

// 前往预约页面
const goToBooking = () => {
	if (selectedTimeIndices.value.length === 0) {
		uni.showToast({
			title: '请选择时间段',
			icon: 'none'
		});
		return;
	}
	
	// 排序并获取起始和结束时间
	const sortedIndices = [...selectedTimeIndices.value].sort((a, b) => a - b);
	const startSlot = timeSlots.value[sortedIndices[0]];
	const endSlot = timeSlots.value[sortedIndices[sortedIndices.length - 1]];
	
	// 构建完整的日期时间字符串 (YYYY-MM-DD HH:mm)
	const fullStartTime = `${selectedDate.value} ${startSlot.startTime}`;
	const fullEndTime = `${selectedDate.value} ${endSlot.endTime}`;
	
	// 跳转到预约页面
	uni.navigateTo({
		url: `/pages/venue_rental/venue_rental?venueId=${venueDetail.value.id}&venueName=${encodeURIComponent(venueDetail.value.name)}&startTime=${encodeURIComponent(fullStartTime)}&endTime=${encodeURIComponent(fullEndTime)}&price=${calculatedPrice.value}`
	});
};
</script>

<style lang="scss" scoped>
.venue-detail-container {
	padding-bottom: 120rpx; // 为底部预约栏留出空间
}

.swiper {
	height: 500rpx;
	
	.swiper-image {
		width: 100%;
		height: 100%;
	}
}

.info-section {
	padding: 30rpx;
	background-color: #fff;
	
	.title {
		font-size: 38rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 20rpx;
	}
	
	.location {
		display: flex;
		align-items: center;
		margin-bottom: 20rpx;
		
		.location-icon {
			font-size: 28rpx;
			margin-right: 10rpx;
		}
		
		.location-text {
			font-size: 28rpx;
			color: #666;
		}
	}
	
	.tags {
		display: flex;
		flex-wrap: wrap;
		margin-bottom: 20rpx;
		
		.tag {
			font-size: 24rpx;
			color: #1a73e8;
			background-color: rgba(26, 115, 232, 0.1);
			padding: 6rpx 16rpx;
			border-radius: 6rpx;
			margin-right: 10rpx;
			margin-bottom: 10rpx;
		}
	}
	
	.price {
		font-size: 38rpx;
		font-weight: bold;
		color: #ff6b81;
		margin-bottom: 10rpx;
		
		.price-unit {
			font-size: 24rpx;
			font-weight: normal;
			color: #999;
		}
	}
	
	.capacity {
		font-size: 28rpx;
		color: #666;
	}
}

.divider {
	height: 20rpx;
	background-color: #f5f5f5;
}

.intro-section {
	padding: 30rpx;
	background-color: #fff;
	
	.section-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 30rpx;
		position: relative;
		padding-left: 20rpx;
		
		&::before {
			content: '';
			position: absolute;
			left: 0;
			top: 10rpx;
			height: 32rpx;
			width: 6rpx;
			background-color: #1a73e8;
			border-radius: 3rpx;
		}
	}
	
	.intro-content {
		font-size: 28rpx;
		color: #666;
		line-height: 1.6;
		margin-bottom: 30rpx;
	}
	
	.intro-images {
		.intro-image {
			width: 100%;
			margin-bottom: 20rpx;
			border-radius: 10rpx;
			overflow: hidden;
		}
	}
}

.time-section {
	padding: 30rpx;
	background-color: #fff;
	
	.section-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 30rpx;
		position: relative;
		padding-left: 20rpx;
		
		&::before {
			content: '';
			position: absolute;
			left: 0;
			top: 10rpx;
			height: 32rpx;
			width: 6rpx;
			background-color: #1a73e8;
			border-radius: 3rpx;
		}
	}
}

.date-selector {
	margin-bottom: 30rpx;
	
	.date-scroll {
		white-space: nowrap;
		height: 120rpx;
	}
	
	.date-item {
		display: inline-flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		width: 120rpx;
		height: 120rpx;
		margin-right: 20rpx;
		border-radius: 10rpx;
		background-color: #f5f5f5;
		transition: all 0.3s ease;
		
		.date-day {
			font-size: 28rpx;
			color: #666;
		}
		
		.date-date {
			font-size: 24rpx;
			color: #999;
			margin-top: 10rpx;
		}
		
		&.active {
			background-color: #1a73e8;
			
			.date-day, .date-date {
				color: #fff;
			}
		}
	}
}

.time-slots-info {
	padding: 0 20rpx 10rpx;
}

.time-slots-tip {
	font-size: 24rpx;
	color: #666;
}

.time-slots {
	display: grid;
	grid-template-columns: repeat(2, 1fr); // 每行显示2个时间段
	gap: 15rpx;
	padding: 20rpx;
}

.time-slot {
	text-align: center;
	padding: 20rpx 10rpx;
	background-color: #f5f5f5;
	border-radius: 10rpx;
	font-size: 26rpx;
	color: #333;
	
	&.unavailable {
		color: #999;
		background-color: #f0f0f0;
		text-decoration: line-through;
	}
	
	&.selected {
		background-color: #1a73e8;
		color: #fff;
	}
}

.bottom-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	display: flex;
	height: 120rpx;
	background-color: #fff;
	box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
	z-index: 100;
	
	.price-summary {
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: center;
		padding: 0 30rpx;
		
		.total-price {
			font-size: 40rpx;
			color: #ff6b81;
			font-weight: bold;
		}
		
		.price-note {
			font-size: 24rpx;
			color: #999;
		}
	}
	
	.book-btn {
		width: 300rpx;
		display: flex;
		justify-content: center;
		align-items: center;
		height: 100%;
		font-size: 32rpx;
		color: #fff;
		background: linear-gradient(135deg, #1a73e8, #6c5ce7);
		
		&.disabled {
			background: linear-gradient(135deg, #b8b8b8, #999);
		}
	}
}
</style>
