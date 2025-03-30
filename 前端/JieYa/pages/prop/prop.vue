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
import { ref, computed, onMounted, watch } from 'vue';
import PropItem from '../../components/prop-item/prop-item.vue';
import { getPropList, searchProp, getAvailableProps } from '../../api/prop.js';

// 保留基本 ref 变量
const searchText = ref('');
const filters = ref(['全部', '可租借', '不可租借']);
const activeFilter = ref(0);
const loading = ref(false);
const noMoreData = ref(false);
const propList = ref([]);
const showOnlyAvailable = ref(false);

// 这个计算属性用于显示道具列表
const filteredProps = computed(() => {
	return propList.value;
});

// 监听搜索文本变化
watch(searchText, (newVal) => {
	if (newVal.trim() === '') {
		// 如果搜索文本为空，重新加载所有道具
		resetSearch();
	}
});

// 重置搜索并加载所有道具
const resetSearch = () => {
	propList.value = []; // 清空现有数据
	noMoreData.value = false;
	fetchPropList();
};

// 加载道具列表 - 根据可租借状态过滤
const fetchPropList = async () => {
	if (loading.value) return;
	
	loading.value = true;
	propList.value = []; // 清空现有数据
	
	try {
		let result;
		
		if (activeFilter.value === 1) {
			// 获取可租借道具
			result = await getAvailableProps();
		} else if (activeFilter.value === 2) {
			// 获取不可租借道具
			result = await getPropList({
				available: false
			});
		} else {
			// 获取所有道具
			result = await getPropList();
		}
		
		console.log('API返回道具数据:', result);
		
		// 检查返回的数据是否有效
		if (result.code === 1 && result.data && result.data.length > 0) {
			// 直接使用返回的数据
			propList.value = result.data;
			console.log('道具列表:', propList.value);
		} else {
			// 没有数据时显示空列表
			propList.value = [];
		}
	} catch (error) {
		console.error('获取道具列表失败:', error);
		uni.showToast({
			title: '获取道具列表失败',
			icon: 'none'
		});
	} finally {
		loading.value = false;
	}
};

// 搜索道具 - 简化搜索逻辑
const handleSearch = async () => {
	if (!searchText.value.trim()) {
		// 如果搜索框为空，则显示所有道具
		fetchPropList();
		return;
	}
	
	loading.value = true;
	propList.value = []; // 清空现有数据
	
	try {
		const result = await searchProp(searchText.value);
		
		if (result.code === 1 && result.data && result.data.length > 0) {
			propList.value = result.data;
		} else {
			propList.value = []; // 搜索无结果时显示空列表
			uni.showToast({
				title: '没有找到相关道具',
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('搜索道具失败:', error);
		uni.showToast({
			title: '搜索道具失败',
			icon: 'none'
		});
	} finally {
		loading.value = false;
	}
};

// 切换只显示可用道具
const toggleAvailableOnly = () => {
	showOnlyAvailable.value = !showOnlyAvailable.value;
	fetchPropList(); // 重新加载数据
};

// 设置筛选条件
function setFilter(index) {
	if (activeFilter.value === index) return;
	
	activeFilter.value = index;
	fetchPropList(); // 重新加载数据
}

// 点击道具
function onPropClick(id) {
	console.log('点击道具:', id);
	uni.navigateTo({
		url: `/pages/prop_detail/prop_detail?id=${id}`
	});
}

// 清除搜索内容
function clearSearch() {
	searchText.value = '';
	fetchPropList();
}

// 导航到我的租借
function navigateToMyRentals() {
	uni.navigateTo({
		url: '/pages/order/order?activeTab=prop'
	});
}

// 初始加载数据
onMounted(() => {
	fetchPropList();
});

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
