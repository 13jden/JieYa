<template>
	<view class="prop-container">
		<view class="header">
			<view class="search-box">
				<text class="search-icon">🔍</text>
				<input type="text" v-model="searchText" placeholder="搜索心理道具" class="search-input" />
			</view>
			<view class="filter-box">
				<view class="filter-item" 
					  v-for="(filter, index) in filters" 
					  :key="index"
					  :class="{ active: activeFilter === index }"
					  @tap="setFilter(index)">
					{{ filter }}
				</view>
			</view>
		</view>
		
		<scroll-view class="prop-list" scroll-y="true" @scrolltolower="loadMore">
			<prop-item 
				v-for="(item, index) in filteredProps" 
				:key="index" 
				:prop-data="item"
				@item-click="onPropClick"
			></prop-item>
			
			<view v-if="loading" class="loading">加载中...</view>
			<view v-if="noMoreData" class="no-more">没有更多道具了</view>
		</scroll-view>
		
		<!-- 悬浮按钮 -->
		<view class="float-button" hover-class="float-button-hover" @tap="navigateToMyRentals">
			<view class="float-button-inner">
				<text class="float-icon">📦</text>
				<text class="float-text">我的租借</text>
			</view>
		</view>
		

	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import PropItem from '../../components/prop-item/prop-item.vue';

// 模拟数据
const propList = ref([
	{
		id: 1,
		name: '情绪卡片',
		description: '帮助识别和表达情绪的卡片组，包含30种基本情绪',
		price: 15,
		available: true,
		image: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/2.jpg'
	},
	{
		id: 2,
		name: '心理沙盘',
		description: '用于沙盘游戏治疗的专业工具，适合儿童心理疏导',
		price: 88,
		available: true,
		image: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/3.jpg'
	},
	{
		id: 3,
		name: '减压玩具套装',
		description: '包含多种减压球、魔方等，帮助缓解焦虑和压力',
		price: 35,
		available: false,
		image: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/backgroundImage.jpg'
	},
	{
		id: 4,
		name: '心理测评工具',
		description: '专业心理测评工具，包含多种量表和评估材料',
		price: 120,
		available: true,
		image: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/2.jpg'
	},
	{
		id: 5,
		name: '绘画治疗套装',
		description: '专业绘画治疗工具，包含画笔、颜料和特殊纸张',
		price: 68,
		available: true,
		image: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/3.jpg'
	}
]);

const searchText = ref('');
const filters = ref(['全部', '可租借', '价格↑', '价格↓']);
const activeFilter = ref(0);
const loading = ref(false);
const noMoreData = ref(false);

// 根据筛选条件过滤道具
const filteredProps = computed(() => {
	let result = propList.value;
	
	// 搜索过滤
	if (searchText.value) {
		result = result.filter(item => 
			item.name.includes(searchText.value) || 
			item.description.includes(searchText.value)
		);
	}
	
	// 筛选条件过滤
	switch(activeFilter.value) {
		case 1: // 可租借
			result = result.filter(item => item.available);
			break;
		case 2: // 价格↑
			result = [...result].sort((a, b) => a.price - b.price);
			break;
		case 3: // 价格↓
			result = [...result].sort((a, b) => b.price - a.price);
			break;
	}
	
	return result;
});

// 设置筛选条件
function setFilter(index) {
	activeFilter.value = index;
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

// 点击道具
function onPropClick(id) {
	console.log('点击道具:', id);
	uni.showToast({
		title: '功能开发中',
		icon: 'none'
	});
}

// 导航到我的租借
function navigateToMyRentals() {
	uni.showToast({
		title: '我的租借功能开发中',
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
.prop-container {
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
	
	.filter-box {
		display: flex;
		justify-content: space-between;
		
		.filter-item {
			flex: 1;
			text-align: center;
			font-size: 26rpx;
			color: #666;
			padding: 10rpx 0;
			position: relative;
			
			&.active {
				color: #1a73e8;
				font-weight: 500;
				
				&::after {
					content: '';
					position: absolute;
					bottom: 0;
					left: 50%;
					transform: translateX(-50%);
					width: 40rpx;
					height: 4rpx;
					background-color: #1a73e8;
					border-radius: 2rpx;
				}
			}
		}
	}
}

.prop-list {
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
