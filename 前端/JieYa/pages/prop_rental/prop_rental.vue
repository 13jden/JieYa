<template>
	<view class="rental-container">
		<!-- 道具信息 -->
		<view class="prop-card">
			<image v-if="propDetail.coverImage" :src="propDetail.coverImage" mode="aspectFill" class="prop-image"></image>
			<view class="prop-info">
				<view class="prop-name">{{ propDetail.name }}</view>
				<view class="prop-desc">{{ propDetail.description }}</view>
				<view class="prop-price">¥{{ propDetail.price }}<text class="price-unit">/天</text></view>
			</view>
		</view>
		
		<!-- 租借政策说明 -->
		<view class="policy-card">
			<view class="card-title">租借政策</view>
			<view class="policy-item">
				<view class="policy-icon">⏱️</view>
				<view class="policy-text">
					<text class="policy-title">租借时长计算</text>
					<text class="policy-desc">10分钟内归还不收取费用，超过10分钟按天计费</text>
				</view>
			</view>
			<view class="policy-item">
				<view class="policy-icon">💰</view>
				<view class="policy-text">
					<text class="policy-title">费用计算</text>
					<text class="policy-desc">不足24小时按1天计算，超过24小时按实际天数计算</text>
				</view>
			</view>
			<view class="policy-item">
				<view class="policy-icon">⚠️</view>
				<view class="policy-text">
					<text class="policy-title">物品损坏</text>
					<text class="policy-desc">归还时如有损坏，需按照物品价值赔偿</text>
				</view>
			</view>
		</view>
		
		<!-- 费用估算 -->
		<view class="fee-card">
			<view class="card-title">费用估算</view>
			<view class="fee-item">
				<text>租借押金</text>
				<text>免费</text>
			</view>
			<view class="fee-item">
				<text>日租金</text>
				<text>¥{{ propDetail.price?.toFixed(2) }}</text>
			</view>
			<view class="fee-note">注：实际费用根据归还时间计算</view>
		</view>
		
		<!-- 租借按钮 -->
		<view class="button-area">
			<button class="rent-button" @tap="confirmRental">确认租借</button>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getPropDetail } from '../../api/prop.js';
import { rentProp } from '../../api/prop.js';

const propDetail = ref({});
const loading = ref(false);

// 加载道具详情
onLoad((options) => {
	const propId = options.id;
	if (propId) {
		fetchPropDetail(propId);
	} else {
		uni.showToast({
			title: '未找到道具ID',
			icon: 'none'
		});
		setTimeout(() => {
			uni.navigateBack();
		}, 1500);
	}
});

// 获取道具详情
const fetchPropDetail = async (id) => {
	loading.value = true;
	try {
		uni.showLoading({ title: '加载中...' });
		
		const result = await getPropDetail(id);
		if (result.code === 1 && result.data) {
			propDetail.value = result.data;
		} else {
			uni.showToast({
				title: result.msg || '获取道具详情失败',
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('获取道具详情失败:', error);
		uni.showToast({
			title: '获取道具详情失败',
			icon: 'none'
		});
	} finally {
		uni.hideLoading();
		loading.value = false;
	}
};

// 确认租借
const confirmRental = async () => {
	if (!propDetail.value.id) {
		uni.showToast({
			title: '道具信息无效',
			icon: 'none'
		});
		return;
	}
	
	try {
		uni.showLoading({ title: '处理中...' });
		
		const result = await rentProp(propDetail.value.id);
		
		if (result.code === 1) {
			uni.showToast({
				title: '租借成功',
				icon: 'success'
			});
			
			// 跳转到订单页面
			setTimeout(() => {
				uni.redirectTo({
					url: '/pages/order/order?activeTab=prop'
				});
			}, 1500);
		} else {
			uni.showToast({
				title: result.msg || '租借失败',
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('租借道具失败:', error);
		uni.showToast({
			title: '租借道具失败',
			icon: 'none'
		});
	} finally {
		uni.hideLoading();
	}
};
</script>

<style lang="scss" scoped>
.rental-container {
	padding: 30rpx;
	background-color: #f5f7fa;
	min-height: 100vh;
}

.prop-card {
	background-color: #ffffff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	display: flex;
	align-items: center;
}

.prop-image {
	width: 160rpx;
	height: 160rpx;
	border-radius: 12rpx;
	margin-right: 30rpx;
	background-color: #f0f0f0;
}

.prop-info {
	flex: 1;
}

.prop-name {
	font-size: 34rpx;
	font-weight: 600;
	color: #333333;
	margin-bottom: 10rpx;
}

.prop-desc {
	font-size: 26rpx;
	color: #666666;
	margin-bottom: 20rpx;
	line-height: 1.4;
}

.prop-price {
	font-size: 36rpx;
	font-weight: 600;
	color: #ff5a5f;
	
	.price-unit {
		font-size: 24rpx;
		font-weight: normal;
		color: #999999;
	}
}

.policy-card, .fee-card {
	background-color: #ffffff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.card-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #333333;
	margin-bottom: 20rpx;
	padding-bottom: 20rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.policy-item {
	display: flex;
	margin-bottom: 20rpx;
	
	&:last-child {
		margin-bottom: 0;
	}
}

.policy-icon {
	font-size: 36rpx;
	margin-right: 20rpx;
}

.policy-text {
	flex: 1;
}

.policy-title {
	font-size: 28rpx;
	font-weight: 500;
	color: #333333;
	display: block;
	margin-bottom: 6rpx;
}

.policy-desc {
	font-size: 24rpx;
	color: #666666;
	line-height: 1.4;
}

.fee-item {
	display: flex;
	justify-content: space-between;
	margin-bottom: 16rpx;
	font-size: 28rpx;
	color: #333333;
}

.fee-note {
	font-size: 24rpx;
	color: #999999;
	margin-top: 16rpx;
}

.button-area {
	margin-top: 60rpx;
}

.rent-button {
	width: 100%;
	background-color: #3f8cff;
	color: #ffffff;
	font-size: 32rpx;
	font-weight: 500;
	border-radius: 100rpx;
	padding: 24rpx 0;
	text-align: center;
	border: none;
}
</style>
