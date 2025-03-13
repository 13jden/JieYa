<template>
<view class="container">
	<view class="user">
		<view class="header">
			<image src="https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg" mode="aspectFill"></image>
			<view class="text">
				<view class="title">登登</view>
				<view class="id">id号:12345678</view>	
			</view>
		</view>
		<view class="userbox">
			<view class="myfans">
			  <text class="number">88</text>
			  <text class="label">粉丝</text>
			</view>
			<view class="mygood">
			  <text class="number">88</text>
			  <text class="label">点赞</text>
			</view>
			<view class="myfocus">
			  <text class="number">88</text>
			  <text class="label">关注</text>
			</view>
			<uni-icons type="settings" size="50rpx"></uni-icons>
		</view>
		<view class="introduction">
			我的签名...
		</view>
	</view>
	<view class="three">
		<view class="good">	
			<image src="../../static/mygood.png" mode="aspectFill"></image>
			<view class="text">我的点赞</view>
		</view>
		<view class="collect">
			<image src="../../static/myCollection.png" mode="aspectFill"></image>
			<view class="text">我的收藏</view>
		</view>
		<view class="focus">
			<image src="../../static/myFocus.png" mode="aspectFill"></image>
			<view class="text">我的关注</view>
		</view>
		<view class="history">
			<image src="../../static/myHistory.png" mode="aspectFill"></image>
			<view class="text">历史浏览</view>
		</view>
		
	</view>
	<view class="box">
		<view class="title">
			我的账户
		</view>
		<view class="two">
			<view class="book">
				<image class="image1" src="../../static/myOrder.png" mode="aspectFill"></image>
				<view class="text">我的订单</view>
				<view class="text2">道具租借情况</view>
			</view>
			<view class="order">
				<image src="../../static/myBook.png" mode="aspectFill"></image>
				<view class="text">我的预约</view>
				<view class="text2">场地预约情况</view>
			</view>
		</view>
	</view>

	<view class="four">
		<view class="note">
			<text>我的笔记</text>
			<uni-icons type="right"></uni-icons>
		</view>
		<view class="agreement">
			<text>隐私协议</text>
			<uni-icons type="right"></uni-icons>
		</view>
		<view class="opinion">
			<text>意见反馈</text>
			<uni-icons type="right"></uni-icons>
		</view>
		<view class="we">
			<text>关于我们</text>
			<uni-icons type="right"></uni-icons>
		</view>
	</view>
	
	<view class="bottom">
		<text>解压呀 - v1.0</text>
	</view>
</view>
</template>

<script setup>
import { onMounted, ref } from 'vue';

const lists = ref([]);

onMounted(() => {
});
</script>

<style lang="scss" scoped>
	.container{
		min-height:100vh ;
		background-color: #f6f7fc;
	}
	.user{
		height: 400rpx;
		width: 100%;
		.header{
			display: flex;
			justify-content: flex-start;
			align-items: center;
			border-radius: 10rpx ;
		}
		image{
			margin: 20rpx;
			margin-right: 10rpx;
			width: 160rpx;
			height: 160rpx;
			border-radius: 50%;
		}
		.text{
			margin-top: 30rpx;
			margin-left: 30rpx;
			
			.title{
				font-weight: 520;
				font-size: 45rpx;
			}
			.id{
				margin-top: 10rpx;
				font-size: 28rpx;
				color: #666;
			}
		}
		.userbox{
			margin-top: 20rpx;
			display: flex;
			justify-content: space-between;
			align-items: center;
			.myfans,.myfocus,.mygood{
				display: flex;
				flex-direction: column;
				justify-content: center;
				align-items: center;
				width: 20%;
			}
			uni-icons{
				position: relative;
				width: 150rpx;
				left: 20rpx;
			}
		}
		.introduction{
			margin: 30rpx;
			font-size: 29rpx;
			color: #666;
		}
	}
	.box{
		display: flex;
		flex-direction: column;
		width: 96%;
		margin-left: 20rpx;
		margin-top: 20rpx;
		background-color: #fff;
		height: 280rpx;
		border-radius: 20rpx;
		.title{
			margin: 20rpx;
			font-size: 33rpx;
			font-weight: 500;
			color: #333;
		}
		.two{
			display: flex;
			justify-content: space-between;
			align-items: center;
			
			.book,.order{
				position: relative;
				z-index: 3;
				background-color: #fff;
				width: 360rpx;
				height: 220rpx;
				border-radius: 20rpx;
				display: flex;
				flex-direction: column;
				justify-content: center;
				align-items: center;
				.text{
					position: relative;
					z-index: 3;
					font-size: 44rpx;
					font-weight: 550;
				}
				.text2{
					margin-top: 20rpx;
					position: relative;
					z-index: 3;
					font-size: 25rpx;
					font-weight: 500;
					color: #999;
				}
				image{
					position: absolute;
					top:-20rpx;
					z-index: 2;
					width: 200rpx;
					height:210rpx;
				}
				.image1{
					height: 230rpx;
				}
			}
			
			.book{
				border-right: 2rpx solid #ddd;
			}
			.order{
				border-left: 2rpx solid #ddd;
			}
		}
	}
	.three{
		display: flex;
		background-color: #fff;
		height: 140rpx;
		margin-top: 0rpx;
		justify-content: space-between;
		.good,.collect,.history,.focus{
			display: flex;
			flex-direction: column;
			justify-content: center;
			align-items: center;
			border-left: 2rpx solid #eee;
			width: 25%;
			image{
				width: 50rpx;
				height: 50rpx;
			}
		}
	}
	
	.four{
		width: 96%;
		margin-left: 20rpx;
		background-color: #fff;
		margin-top: 60rpx;
		border-radius: 20rpx;
		.note,.we,.opinion,.agreement{
			display: flex;
			justify-content: space-between;
			align-items: center;
			height: 80rpx;
			margin-left: 30rpx;
			border-bottom: 1rpx solid #eee;
		}
	}
	.bottom{
		margin-top: 30rpx;
		margin-bottom: 50rpx;
		color: #666;
		font-size: 23rpx;
		display: flex;
		justify-content: center;
		align-items: center;

	}
</style>
