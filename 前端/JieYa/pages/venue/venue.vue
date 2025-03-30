<template>
	<view class="venue-container">
		<view class="header">
			<view class="search-box">
				<text class="search-icon">🔍</text>
				<input type="text" v-model="searchText" placeholder="搜索心理咨询场地" class="search-input" />
			</view>
			
			<scroll-view class="category-scroll" scroll-x="true" show-scrollbar="false">
				<view 
					v-for="(item, index) in categories" 
					:key="index" 
					class="category-item" 
					:class="{ active: activeTagId === item.id }"
					@tap="setCategory(item)"
				>
					{{ item.tagName }}
				</view>
			</scroll-view>
		</view>
		
		<scroll-view 
			class="venue-list" 
			scroll-y="true" 
			:style="{ height: scrollHeight + 'px' }"
		>
			<view 
				v-for="(item, index) in filteredVenues" 
				:key="index" 
				class="venue-item"
				@tap="onVenueClick(item)"
			>
				<image :src="item.coverImage" mode="aspectFill" class="venue-image"></image>
				<view class="venue-info">
					<view class="venue-name">{{ item.name }}</view>
					<view class="venue-location">
						<text class="location-icon">📍</text>
						<text class="location-text">{{ item.location }}</text>
					</view>
					
					<!-- 直接显示转换好的标签名称 -->
					<view v-if="item.tagNames && item.tagNames.length > 0" class="venue-tags">
						<text v-for="(tagName, tagIndex) in item.tagNames" :key="tagIndex" class="venue-tag">
							{{ tagName }}
						</text>
					</view>
					
					<view class="venue-price">¥{{ item.price }}<text class="price-unit">/小时</text></view>
				</view>
			</view>
			
			<view class="safe-bottom-area"></view>
		</scroll-view>
		
		<!-- 悬浮按钮 -->
		<view class="float-button" hover-class="float-button-hover" @tap="navigateToMyBookings">
			<view class="float-button-inner">
				<text class="float-icon">📅</text>
				<text class="float-text">我的预约</text>
			</view>
		</view>

	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import VenueItem from '../../components/venue-item/venue-item.vue';
import { getVenueList, searchVenue, getVenueTags } from '../../api/venue.js';

// 保留基本 ref 变量
const searchText = ref('');
const loading = ref(false);
const venueList = ref([]);
const scrollHeight = ref(0);

// 标签相关变量
const tagList = ref([]); // 存储标签列表
const categories = ref([{ id: 0, name: '全部' }]); // 默认分类
const activeTagId = ref(0); // 当前选中的标签ID

// 计算属性 - 根据当前选中的标签过滤场地
const filteredVenues = computed(() => {
	if (activeTagId.value === 0) {
		return venueList.value;
	} else {
		// 根据选中的标签过滤场地
		return venueList.value.filter(venue => 
			venue.tags && venue.tags.includes(activeTagId.value)
		);
	}
});

// 获取标签列表
const fetchTags = async () => {
	try {
		const result = await getVenueTags();
		
		if (result.code === 1 && result.data) {
			tagList.value = result.data;
			
			categories.value = [
				{ id: 0, tagName: '全部' },
				...result.data
			];
			
			console.log('标签列表:', tagList.value);
		}
	} catch (error) {
		console.error('获取标签列表失败:', error);
		uni.showToast({
			title: '获取标签列表失败',
			icon: 'none'
		});
	}
};

// 加载场地列表并处理标签
const fetchVenueList = async () => {
	if (loading.value) return;
	
	loading.value = true;
	venueList.value = []; // 清空现有数据
	
	try {
		const result = await getVenueList();
		
		console.log('API返回数据:', result);
		
		// 检查返回的数据是否有效
		if (result.code === 1 && result.data && result.data.length > 0) {
			// 处理每个场地的标签，将ID转换为名称
			const processedVenues = result.data.map(venue => {
				const processedVenue = { ...venue };
				
				// 添加一个新的字段tagNames存储标签名称
				if (venue.tags && Array.isArray(venue.tags)) {
					processedVenue.tagNames = venue.tags.map(tagId => {
						const tag = tagList.value.find(t => t.id === tagId);
						return tag ? tag.tagName : ''; // 使用tagName字段
					}).filter(name => name !== '');
				} else {
					processedVenue.tagNames = [];
				}
				
				return processedVenue;
			});
			
			venueList.value = processedVenues;
			console.log('处理后的场地列表:', venueList.value);
		} else {
			// 没有数据时显示空列表
			venueList.value = [];
		}
	} catch (error) {
		console.error('获取场地列表失败:', error);
		uni.showToast({
			title: '获取场地列表失败',
			icon: 'none'
		});
	} finally {
		loading.value = false;
	}
};

// 搜索场地 - 简化搜索逻辑
const handleSearch = async () => {
	if (!searchText.value.trim()) {
		// 如果搜索框为空，则显示所有场地
		fetchVenueList();
		return;
	}
	
	loading.value = true;
	venueList.value = []; // 清空现有数据
	
	try {
		const result = await searchVenue(searchText.value);
		
		if (result.code === 1 && result.data && result.data.length > 0) {
			venueList.value = result.data;
		} else {
			venueList.value = []; // 搜索无结果时显示空列表
			uni.showToast({
				title: '没有找到相关场地',
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('搜索场地失败:', error);
		uni.showToast({
			title: '搜索场地失败',
			icon: 'none'
		});
	} finally {
		loading.value = false;
	}
};

// 设置分类
function setCategory(item) {
	if (activeTagId.value === item.id) return;
	
	activeTagId.value = item.id;
	// 不需要重新请求，使用计算属性过滤结果
}

// 清除搜索内容
function clearSearch() {
	searchText.value = '';
	fetchVenueList();
}

// 点击场地 - 传递场地对象
function onVenueClick(item) {
	console.log('点击场地:', item);
	
	// 准备要传递的数据
	const venueData = {
		id: item.id,
		tagNames: item.tagNames || [] // 确保tagNames存在
	};
	

	uni.navigateTo({
		url: `/pages/venue_detail/venue_detail?id=${item.id}`
	});
}

// 计算滚动区域高度，确保适应各种屏幕
function calculateScrollHeight() {
	const info = uni.getSystemInfoSync();
	// 假设头部筛选区域高度为180rpx，底部安全区域为30rpx
	// 转换rpx为px: rpx / 750 * 屏幕宽度
	const headerHeight = 180 * info.windowWidth / 750;
	scrollHeight.value = info.windowHeight - headerHeight;
}

// 页面加载时计算高度
onMounted(() => {
	calculateScrollHeight();
	
	// 先获取标签列表，再获取场地列表
	fetchTags().then(() => {
		fetchVenueList();
	});
	
	// 监听窗口大小变化
	uni.onWindowResize(() => {
		calculateScrollHeight();
	});
});

// 导航到我的预约
function navigateToMyBookings() {
	uni.navigateTo({
		url: '/pages/order/order?activeTab=venue'
	});
}

// 导航到首页
function navigateToHome() {
	uni.switchTab({
		url: '/pages/index/index'
	});
}
</script>

<style lang="scss" scoped>
.venue-container {
	display: flex;
	flex-direction: column;
	height: 100vh;
	background-color: #f8f9fa;
	position: relative;
}

.header {
	padding: 30rpx 20rpx;
	background-color: #fff;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	position: sticky;
	top: 0;
	z-index: 10;
	
	.search-box {
		display: flex;
		align-items: center;
		background-color: #f5f5f5;
		border-radius: 40rpx;
		padding: 15rpx 20rpx;
		margin-bottom: 20rpx;
		
		.search-icon {
			margin-right: 10rpx;
			font-size: 32rpx;
			color: #999;
		}
		
		.search-input {
			flex: 1;
			height: 60rpx;
			font-size: 28rpx;
		}
	}
	
	.category-scroll {
		width: 100%;
		white-space: nowrap;
	}
	
	.category-container {
		display: inline-flex;
		padding: 10rpx 0;
	}
	
	.category-item {
		display: inline-block;
		padding: 10rpx 30rpx;
		margin-right: 20rpx;
		font-size: 26rpx;
		color: #666;
		background-color: #f5f5f5;
		border-radius: 30rpx;
		transition: all 0.3s ease;
		
		&.active {
			background-color: #e6f3ff;
			color: #1a73e8;
			font-weight: 500;
		}
		
		&:last-child {
			margin-right: 0;
		}
	}
}

.venue-list {
	flex: 1;
	box-sizing: border-box;
	padding-bottom: 30rpx;
	padding: 20rpx;
}

.loading, .no-more {
	text-align: center;
	padding: 20rpx;
	font-size: 26rpx;
	color: #999;
}

/* 悬浮按钮样式 */
.float-button {
	position: fixed;
	right: 30rpx;
	bottom: 60rpx;
	width: 200rpx;
	height: 80rpx;
	background: linear-gradient(135deg, #1a73e8, #6c5ce7);
	border-radius: 40rpx;
	box-shadow: 0 6rpx 20rpx rgba(26, 115, 232, 0.3);
	display: flex;
	justify-content: center;
	align-items: center;
	z-index: 100;
	transition: all 0.3s ease;
	
	.float-button-inner {
		display: flex;
		align-items: center;
		justify-content: center;
	}
	
	.float-icon {
		font-size: 36rpx;
		margin-right: 10rpx;
	}
	
	.float-text {
		color: #fff;
		font-size: 28rpx;
		font-weight: 500;
	}
}

.float-button-hover {
	transform: scale(1.05) translateY(-5rpx);
	box-shadow: 0 10rpx 25rpx rgba(26, 115, 232, 0.4);
}

/* 安全区域，确保最后一个项目可以完全显示 */
.safe-bottom-area {
	height: 100rpx; /* 设置足够的底部空间 */
	width: 100%;
}

/* 调整场地项间距 */
.venue-item {
	background-color: #ffffff;
	border-radius: 16rpx;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
	margin-bottom: 30rpx;
	overflow: hidden;
	transition: transform 0.2s, box-shadow 0.2s;
	
	&:active {
		transform: scale(0.98);
		box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	}
}

.venue-image {
	width: 100%;
	height: 360rpx;
	object-fit: cover;
}

.venue-info {
	padding: 24rpx;
}

.venue-name {
	font-size: 34rpx;
	font-weight: 600;
	color: #333333;
	margin-bottom: 16rpx;
	line-height: 1.3;
}

.venue-location {
	display: flex;
	align-items: center;
	margin-bottom: 16rpx;
	
	.location-icon {
		font-size: 28rpx;
		margin-right: 6rpx;
		color: #ff5a5f;
	}
	
	.location-text {
		font-size: 26rpx;
		color: #666666;
		flex: 1;
	}
}

.venue-tags {
	display: flex;
	flex-wrap: wrap;
	margin-bottom: 16rpx;
	
	.venue-tag {
		font-size: 22rpx;
		padding: 6rpx 16rpx;
		margin-right: 12rpx;
		margin-bottom: 10rpx;
		border-radius: 100rpx;
		background-color: #f0f7ff;
		color: #3f8cff;
		font-weight: 500;
	}
}

.venue-price {
	font-size: 32rpx;
	font-weight: 600;
	color: #ff5a5f;
	
	.price-unit {
		font-size: 24rpx;
		font-weight: 400;
		color: #999999;
		margin-left: 4rpx;
	}
}

/* 分类标签样式美化 */
.category-scroll {
	white-space: nowrap;
	padding: 20rpx 20rpx 10rpx;
	background-color: #fff;
	border-bottom: 1rpx solid #f0f0f0;
}

.category-item {
	display: inline-block;
	padding: 12rpx 30rpx;
	margin-right: 20rpx;
	font-size: 28rpx;
	color: #666666;
	background-color: #f5f5f5;
	border-radius: 100rpx;
	transition: all 0.2s;
	
	&.active {
		background-color: #3f8cff;
		color: #ffffff;
		font-weight: 500;
	}
}

</style>
