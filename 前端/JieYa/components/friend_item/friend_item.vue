<template>
	<view class="box">
		<image :src="userimage" mode="aspectFill" class="image"></image>
		<view class="spot" v-show="isNew"></view>
		<view class="box2">
			<view class="text">
				<view class="text1">{{username}}</view>
				<view class="text2">{{message}}</view>
			</view>
		<view class="date">
			{{date}}
		</view>
		</view>
		
	</view>
</template>

<script setup>
import { ref } from 'vue';
const props = defineProps({
	userimage:{
		type:String,
		default:"https://th.bing.com/th/id/R.5da7e5c735cc95f8f8e3d3bb7e981d7a?rik=koE8E6kqXKbipA&riu=http%3a%2f%2fwww.beihaiting.com%2fuploads%2fallimg%2f150515%2f10723-15051522234J95.jpg&ehk=CTB857uvdo4mBMipOW25WsUo7aaZd7w73ebL%2b7H71jA%3d&risl=&pid=ImgRaw&r=0",
	},
	username:{
		type:String,
		default:"育良书记"
	},
	message:{
		type:String,
		default:"你好"
	},
	date:{
		type:String,
		default:"8月9日"
	},
	isNew:{
		type:Boolean,
		default:true
	}
})
const emit = defineEmits(['click']);

function handleClick() {
  emit('click'); // 触发父组件的点击事件
}
</script>

<style lang="scss" scoped>
	
.box{
	image{
		margin-left: 30rpx;
		border-radius: 30rpx;
		height: 110rpx;
		width: 110rpx;
	}
	height: 130rpx;
	display: flex;
	align-items: center;
	background-color: rgb(255, 255, 255,0.8);
	padding-bottom: 10rpx;
	box-shadow: 0 2rpx 2rpx rgba(0, 0, 0, 0.04) ,0 -2rpx 2rpx rgba(0, 0, 0, 0.04);
}
.spot{
	left: 125rpx;
	margin-top: -80rpx;
	position: absolute;
	width: 25rpx;
	height: 25rpx;
	border-radius: 50%;
	background-color: #007aff;
}
.box2{
	display: flex;
	justify-content: space-between;
	align-items: center;
}
.text{
	margin-left: 10rpx;
	padding: 10rpx;
	.text1{
		font-size: 35rpx;
	}
	.text2{
		margin-top: 8rpx;
		font-size: 25rpx;
		color: #999999;
	}
}
.date{
	position: absolute;
	right: 20rpx;
	margin-bottom: 30rpx;
	font-size: 22rpx;
	color: #999;
}
</style>
