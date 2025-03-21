<template>
	<view class="venue-container">
		<view class="header">
			<view class="search-box">
				<text class="search-icon">🔍</text>
				<input type="text" v-model="searchText" placeholder="搜索心理咨询场地" class="search-input" />
			</view>
			
			<scroll-view class="category-scroll" scroll-x="true" show-scrollbar="false">
				<view class="category-container">
					<view class="category-item" 
							v-for="(category, index) in categories" 
							:key="index"
							:class="{ active: activeCategory === index }"
							@tap="setCategory(index)">
						{{ category }}
					</view>
				</view>
			</scroll-view>
		</view>
		
		<scroll-view class="venue-list" scroll-y="true" @scrolltolower="loadMore">
			<venue-item 
				v-for="(item, index) in filteredVenues" 
				:key="index" 
				:venue-data="item"
				@item-click="onVenueClick"
			></venue-item>
			
			<view v-if="loading" class="loading">加载中...</view>
			<view v-if="noMoreData" class="no-more">没有更多场地了</view>
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
import { ref, computed } from 'vue';
import VenueItem from '../../components/venue-item/venue-item.vue';

// 模拟数据
const venueList = ref([
	{
		id: 1,
		name: '阳光心理咨询室',
		description: '安静舒适的咨询环境，专业设备齐全，适合个人咨询和小组活动',
		location: '杭州市西湖区文三路',
		price: 120,
		capacity: 8,
		tags: ['安静', '舒适', '专业设备'],
		image: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/2.jpg'
	},
	{
		id: 2,
		name: '心灵花园工作室',
		description: '温馨自然的环境，有独立休息区和茶水间，适合长时间心理工作坊',
		location: '杭州市拱墅区莫干山路',
		price: 200,
		capacity: 15,
		tags: ['自然', '宽敞', '休息区'],
		image: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/3.jpg'
	},
	{
		id: 3,
		name: '静心沙龙',
		description: '位于市中心的高端咨询场所，隔音效果好，私密性强，适合VIP客户',
		location: '杭州市上城区平海路',
		price: 280,
		capacity: 4,
		tags: ['高端', '私密', '市中心'],
		image: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/backgroundImage.jpg'
	},
	{
		id: 4,
		name: '青少年心理活动中心',
		description: '专为青少年设计的活动空间，配备互动游戏设施，适合团体心理辅导',
		location: '杭州市滨江区江南大道',
		price: 150,
		capacity: 20,
		tags: ['青少年', '互动', '团体活动'],
		image: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/2.jpg'
	}
]);

const searchText = ref('');
const categories = ref(['全部', '个人咨询', '团体活动', '工作坊', '亲子空间']);
const activeCategory = ref(0);
const loading = ref(false);
const noMoreData = ref(false);

// 根据筛选条件过滤场地
const filteredVenues = computed(() => {
	let result = venueList.value;
	
	// 搜索过滤
	if (searchText.value) {
		result = result.filter(item => 
			item.name.includes(searchText.value) || 
			item.description.includes(searchText.value) ||
			item.location.includes(searchText.value)
		);
	}
	
	// 分类过滤 (简化示例，实际应该根据场地类型属性过滤)
	if (activeCategory.value !== 0) {
		// 这里只是示例，实际应该根据场地的类型属性进行过滤
		const categoryMap = {
			1: [1, 3], // 个人咨询场地ID
			2: [2, 4], // 团体活动场地ID
			3: [2],    // 工作坊场地ID
			4: [4]     // 亲子空间场地ID
		};
		
		const allowedIds = categoryMap[activeCategory.value] || [];
		result = result.filter(item => allowedIds.includes(item.id));
	}
	
	return result;
});

// 设置分类
function setCategory(index) {
	activeCategory.value = index;
}

// 加载更多
function loadMore() {
	if (noMoreData.value) return;
	
	loading.value = true;
	
	// 模拟加载更多数据
	setTimeout(() => {
		loading.value = false;
		noMoreData.value = true; // 示例中设置没有更多数据
	}, 1000);
}

// 点击场地
function onVenueClick(id) {
	console.log('点击场地:', id);
	uni.showToast({
		title: '功能开发中',
		icon: 'none'
	});
}

// 导航到我的预约
function navigateToMyBookings() {
	uni.showToast({
		title: '我的预约功能开发中',
		icon: 'none'
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
	padding: 20rpx;
	padding-bottom: 120rpx; /* 为悬浮按钮留出空间 */
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


</style>
