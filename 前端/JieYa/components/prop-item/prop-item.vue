<template>
	<view class="prop-item" @tap="onItemClick">
		<image :src="propData.coverImage" mode="aspectFill" class="prop-image"></image>
		<view class="prop-info">
			<view class="prop-name">{{ propData.name }}</view>
			<view class="prop-desc">{{ propData.description }}</view>
			<view class="prop-bottom">
				<view class="prop-price">¥{{ propData.price }}/天</view>
				<view class="prop-status" :class="propData.available ? 'available' : 'unavailable'">
					{{ propData.available ? '可租借' : '已租出' }}
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
	propData: {
		type: Object,
		required: true
	}
});

const emit = defineEmits(['itemClick']);

function onItemClick() {
	emit('itemClick', props.propData.id);
}
</script>

<style lang="scss" scoped>
.prop-item {
	display: flex;
	background-color: #ffffff;
	border-radius: 16rpx;
	padding: 20rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	
	.prop-image {
		width: 180rpx;
		height: 180rpx;
		border-radius: 12rpx;
		background-color: #f5f5f5;
		object-fit: cover;
	}
	
	.prop-info {
		flex: 1;
		margin-left: 20rpx;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		
		.prop-name {
			font-size: 32rpx;
			font-weight: 600;
			color: #333;
			margin-bottom: 10rpx;
		}
		
		.prop-desc {
			font-size: 26rpx;
			color: #666;
			margin-bottom: 20rpx;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
			overflow: hidden;
			text-overflow: ellipsis;
		}
		
		.prop-bottom {
			display: flex;
			justify-content: space-between;
			align-items: center;
			
			.prop-price {
				font-size: 30rpx;
				font-weight: 600;
				color: #ff6b6b;
			}
			
			.prop-status {
				font-size: 24rpx;
				padding: 6rpx 16rpx;
				border-radius: 20rpx;
				
				&.available {
					background-color: #e6f7ee;
					color: #00b578;
				}
				
				&.unavailable {
					background-color: #f5f5f5;
					color: #999;
				}
			}
		}
	}
	
	&:active {
		transform: scale(0.98);
	}
}
</style>
