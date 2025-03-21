<template>
	<view class="venue-item" @tap="onItemClick">
		<image class="venue-image" :src="venueData.image" mode="aspectFill"></image>
		<view class="venue-overlay">
			<view class="venue-name">{{ venueData.name }}</view>
			<view class="venue-location">
				<text class="location-icon">📍</text>
				<text>{{ venueData.location }}</text>
			</view>
		</view>
		<view class="venue-info">
			<view class="venue-desc">{{ venueData.description }}</view>
			<view class="venue-bottom">
				<view class="venue-price">¥{{ venueData.price }}/小时</view>
				<view class="venue-capacity">容纳{{ venueData.capacity }}人</view>
			</view>
			<view class="venue-tags">
				<view class="venue-tag" v-for="(tag, index) in venueData.tags" :key="index">
					{{ tag }}
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
	venueData: {
		type: Object,
		required: true
	}
});

const emit = defineEmits(['itemClick']);

function onItemClick() {
	emit('itemClick', props.venueData.id);
}
</script>

<style lang="scss" scoped>
.venue-item {
	position: relative;
	background-color: #ffffff;
	border-radius: 16rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
	overflow: hidden;
	
	.venue-image {
		width: 100%;
		height: 360rpx;
		object-fit: cover;
	}
	
	.venue-overlay {
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		padding: 20rpx;
		background: linear-gradient(to bottom, rgba(0,0,0,0.6) 0%, rgba(0,0,0,0) 100%);
		
		.venue-name {
			font-size: 36rpx;
			font-weight: 600;
			color: #ffffff;
			text-shadow: 0 2rpx 4rpx rgba(0,0,0,0.3);
			margin-bottom: 10rpx;
		}
		
		.venue-location {
			display: flex;
			align-items: center;
			font-size: 24rpx;
			color: #ffffff;
			
			.location-icon {
				margin-right: 6rpx;
			}
		}
	}
	
	.venue-info {
		padding: 20rpx;
		
		.venue-desc {
			font-size: 28rpx;
			color: #666;
			margin-bottom: 16rpx;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
			overflow: hidden;
			text-overflow: ellipsis;
		}
		
		.venue-bottom {
			display: flex;
			justify-content: space-between;
			align-items: center;
			margin-bottom: 16rpx;
			
			.venue-price {
				font-size: 32rpx;
				font-weight: 600;
				color: #1a73e8;
			}
			
			.venue-capacity {
				font-size: 24rpx;
				color: #666;
				background-color: #f5f5f5;
				padding: 6rpx 16rpx;
				border-radius: 20rpx;
			}
		}
		
		.venue-tags {
			display: flex;
			flex-wrap: wrap;
			
			.venue-tag {
				font-size: 22rpx;
				color: #666;
				background-color: #f0f4f7;
				padding: 4rpx 16rpx;
				border-radius: 16rpx;
				margin-right: 10rpx;
				margin-bottom: 10rpx;
			}
		}
	}
	
	&:active {
		transform: scale(0.98);
	}
}
</style>
