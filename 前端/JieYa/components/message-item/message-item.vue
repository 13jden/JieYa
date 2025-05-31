<template>
	<view class="message-item" :class="{ 'fixed': fixed }" @tap="onClick">
	  <view class="avatar-container">
		<image class="avatar" :src="avatar"></image>
		<view class="status-dot" v-if="online"></view>
	  </view>
	  <view class="content">
		<view class="top-row">
		  <text class="name">{{ name }}</text>
		  <text class="time">{{ time }}</text>
		</view>
		<view class="bottom-row">
		  <text class="message" :class="{ 'highlight': highlight }">{{ message }}</text>
		  <view class="badge" v-if="badge > 0">
			<text>{{ badge > 99 ? '99+' : badge }}</text>
		  </view>
		</view>
	  </view>
	</view>
  </template>
  
  <script setup>
  import { defineProps, defineEmits } from 'vue';
  
  const props = defineProps({
	avatar: {
	  type: String,
	  default: '/static/default-avatar.png'
	},
	name: {
	  type: String,
	  required: true
	},
	message: {
	  type: String,
	  default: ''
	},
	time: {
	  type: String,
	  default: ''
	},
	badge: {
	  type: Number,
	  default: 0
	},
	highlight: {
	  type: Boolean,
	  default: false
	},
	fixed: {
	  type: Boolean,
	  default: false
	},
	online: {
	  type: Boolean,
	  default: false
	}
  });
  
  const emit = defineEmits(['click']);
  
  const onClick = () => {
	emit('click');
  };
  </script>
  
  <style lang="scss" scoped>
  .message-item {
	display: flex;
	padding: 16px;
	background-color: #ffffff;
	position: relative;
	
	&.fixed {
	  background-color: #f8f8ff;
	  border-left: 3px solid #1e90ff;
	}
	
	&:active {
	  background-color: #f5f5f5;
	}
	
	&::after {
	  content: '';
	  position: absolute;
	  bottom: 0;
	  left: 68px;
	  right: 0;
	  height: 1px;
	  background-color: #f0f0f0;
	}
	
	&:last-child::after {
	  display: none;
	}
  }
  
  .avatar-container {
	position: relative;
	margin-right: 12px;
  }
  
  .avatar {
	width: 52px;
	height: 52px;
	border-radius: 6px;
	background-color: #f0f0f0;
  }
  
  .status-dot {
	position: absolute;
	right: 0;
	bottom: 0;
	width: 12px;
	height: 12px;
	border-radius: 50%;
	background-color: #52c41a;
	border: 2px solid #ffffff;
  }
  
  .content {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
  }
  
  .top-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
  }
  
  .name {
	font-size: 16px;
	font-weight: 500;
	color: #333;
  }
  
  .time {
	font-size: 12px;
	color: #999;
  }
  
  .bottom-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-top: 6px;
  }
  
  .message {
	font-size: 14px;
	color: #666;
	max-width: 85%;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
	
	&.highlight {
	  color: #1890ff;
	}
  }
  
  .badge {
	min-width: 16px;
	height: 16px;
	border-radius: 8px;
	background-color: #ff4d4f;
	color: #fff;
	font-size: 12px;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 0 5px;
  }
  </style>