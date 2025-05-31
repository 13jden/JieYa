<template>
	<view class="friend-item">
		<view class="avatar-container">
			<image :src="userimage" class="avatar"></image>
			<view v-if="badgeCount > 0" class="badge">
				<text>{{ badgeCount > 99 ? '99+' : badgeCount }}</text>
			</view>
		</view>
		<view class="info">
			<view class="username">{{ username }}</view>
			<view class="message" :class="{ 'unread-message': isNew }">{{ message }}</view>
		</view>
		<view class="date" v-if="date">
			{{ date }}
		</view>
	</view>
</template>

<script setup>
// 不需要导入defineProps和defineEmits
const props = defineProps({
	username: {
		type: String,
		default: '未知用户'
	},
	userimage: {
		type: String,
		default: '/static/default-avatar.png'
	},
	message: {
		type: String,
		default: ''
	},
	date: {
		type: String,
		default: ''
	},
	isNew: {
		type: Boolean,
		default: false
	},
	badgeCount: {
		type: Number,
		default: 0
	}
});
</script>

<style lang="scss" scoped>
.friend-item {
	display: flex;
	align-items: center;
	padding: 30rpx;
	background-color: #fff;
	border-bottom: 1rpx solid #eaeaea;
	position: relative;
}

.avatar-container {
	position: relative;
	margin-right: 30rpx;
}

.avatar {
	width: 100rpx;
	height: 100rpx;
	border-radius: 50%;
}

.badge {
	position: absolute;
	top: -10rpx;
	right: -10rpx;
	min-width: 40rpx;
	height: 40rpx;
	line-height: 40rpx;
	border-radius: 20rpx;
	background-color: #ff4d4f;
	color: #fff;
	font-size: 24rpx;
	text-align: center;
	padding: 0 10rpx;
}

.info {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: center;
	overflow: hidden;
}

.username {
	font-size: 32rpx;
	font-weight: 500;
	color: #333;
	margin-bottom: 10rpx;
}

.message {
	font-size: 28rpx;
	color: #999;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}

.unread-message {
	color: #333;
	font-weight: 500;
}

.date {
	font-size: 24rpx;
	color: #999;
	position: absolute;
	right: 30rpx;
	top: 30rpx;
}
</style>
