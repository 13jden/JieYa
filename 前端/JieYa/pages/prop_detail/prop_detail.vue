<template>
	<view class="prop-detail-container">
		<!-- 轮播图 -->
		<swiper class="swiper" circular indicator-dots autoplay interval="3000" duration="500">
			<swiper-item v-for="(image, index) in propDetail.imageList" :key="index">
				<image :src="image.imageUrl" mode="aspectFill" class="swiper-image"></image>
			</swiper-item>
		</swiper>
		
		<!-- 基本信息 -->
		<view class="info-section">
			<view class="price">¥{{ propDetail.price }}<text class="price-unit">/天</text></view>
			<view class="title">{{ propDetail.name }}</view>
			<view class="description">{{ propDetail.description }}</view>
			<view class="status" :class="{ unavailable: !propDetail.available }">
				{{ propDetail.available ? '可租借' : '暂不可租' }}
			</view>
		</view>
		
		<!-- 分割线 -->
		<view class="divider"></view>
		
		<!-- 详细介绍 -->
		<view class="detail-section">
			<view class="section-title">详细介绍</view>
			<view class="detail-content">
				<image 
					v-for="(detailImg, index) in propDetail.detailImageList" 
					:key="index" 
					:src="detailImg.imageUrl" 
					mode="widthFix" 
					class="detail-image"
				></image>
			</view>
		</view>
		
		<!-- 底部租借按钮 -->
		<view class="bottom-bar">
			<view class="contact-btn" @tap="contactService">联系客服</view>
			<view 
				class="rent-btn" 
				:class="{ disabled: !propDetail.available }"
				@tap="rentProp"
			>
				{{ propDetail.available ? '立即租借' : '暂不可租' }}
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getPropDetail } from '../../api/prop.js';

// 道具详情数据
const propDetail = ref({
	id: 0,
	name: '',
	description: '',
	price: 0,
	available: true,
	imageList: [],
	detailImageList: []
});

// 获取页面参数
onLoad((option) => {
	const propId = Number(option.id || 1);
	fetchPropDetail(propId);
});

// 获取道具详情
const fetchPropDetail = async (id) => {
	try {
		uni.showLoading({
			title: '加载中...'
		});
		
		const result = await getPropDetail(id);
		if (result.code === 1 && result.data) {
			// 为null的字段赋予默认值
			result.data.imageList = result.data.imageList || [];
			result.data.detailImageList = result.data.detailImageList || [];
			propDetail.value = result.data;
		}
	} catch (error) {
		console.error('获取道具详情失败:', error);
		uni.showToast({
			title: '获取道具详情失败',
			icon: 'none'
		});
	} finally {
		uni.hideLoading();
	}
};

// 联系客服
const contactService = () => {
	uni.showToast({
		title: '正在连接客服...',
		icon: 'none'
	});
	// 实际项目中可以调用联系客服的API或跳转到客服页面
};

// 租借道具
const rentProp = () => {
	// 跳转到租借确认页面，传递道具ID
	uni.navigateTo({
		url: `/pages/prop_rental/prop_rental?id=${propDetail.value.id}`
	});
};
</script>

<style lang="scss" scoped>
.prop-detail-container {
	padding-bottom: 140rpx;
}

.swiper {
	width: 100%;
	height: 500rpx;
	
	.swiper-image {
		width: 100%;
		height: 100%;
	}
}

.info-section {
	padding: 30rpx;
	background-color: #fff;
	position: relative;
	
	.price {
		font-size: 48rpx;
		color: #ff6b81;
		font-weight: bold;
		margin-bottom: 20rpx;
		
		.price-unit {
			font-size: 24rpx;
			color: #999;
			font-weight: normal;
		}
	}
	
	.title {
		font-size: 36rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 20rpx;
	}
	
	.description {
		font-size: 28rpx;
		color: #666;
		line-height: 1.6;
		margin-bottom: 20rpx;
	}
	
	.status {
		position: absolute;
		top: 30rpx;
		right: 30rpx;
		padding: 10rpx 20rpx;
		border-radius: 30rpx;
		font-size: 24rpx;
		color: #fff;
		background-color: #67c23a;
		
		&.unavailable {
			background-color: #909399;
		}
	}
}

.divider {
	height: 20rpx;
	background-color: #f5f5f5;
}

.detail-section {
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
	
	.detail-content {
		.detail-image {
			width: 100%;
			margin-bottom: 20rpx;
			border-radius: 10rpx;
			overflow: hidden;
		}
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
	
	.contact-btn, .rent-btn {
		flex: 1;
		display: flex;
		justify-content: center;
		align-items: center;
		height: 100%;
		font-size: 32rpx;
	}
	
	.contact-btn {
		color: #666;
		background-color: #fff;
	}
	
	.rent-btn {
		color: #fff;
		background: linear-gradient(135deg, #1a73e8, #6c5ce7);
		
		&.disabled {
			background: linear-gradient(135deg, #b8b8b8, #999);
		}
	}
}
</style>
