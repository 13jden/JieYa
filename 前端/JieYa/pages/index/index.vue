<template>
	
<view class="container">
	<view class="swiper-box">
		<swiper
			:indicator-dots="true"
			:autoplay="true"
			:interval="3000"
			:duration="1000"
			:indicator-color="'rgba(255, 255, 255, 0.6)'"
			:indicator-active-color="'#ffffff'" 
			class="swiper"
		  >
			<swiper-item v-for="(banner, index) in bannerList" :key="banner.bannerId" @tap="handleBannerClick(banner)">
				<view class="swiper-item">
					<image :src="banner.image" mode="aspectFill"></image>
					<view class="banner-overlay" v-if="banner.text">
						<view class="banner-text">{{ banner.text }}</view>
						<view class="banner-type">{{ banner.getTypeText() }}</view>
					</view>
				</view>
			</swiper-item>
		</swiper>
	</view>
	
	<view class="button-box">
		<view class="button-1" @tap="navigateToProp">
			<image src="../../static/prop.png" class="image1" mode=""></image>
			<view class="back-text">Prop rental</view>
			<view class="title">
				道具租借
			</view>
			
			<view class="text">
				品质保障
			</view>
		</view>
		<view class="button-2" @tap="navigateToVenue">
			<image src="../../static/venue.png" mode=""></image>
			<view class="back-text">Venue booking</view>
			<view class="title">
				场地预定
			</view>
			<view class="text">
				便捷预约
			</view>
		</view>
	</view>

	<view class="recommend">
		心理资讯
	</view>
	<information
	@tap="weChatAccount()"
	></information>
	<!-- 弹窗 -->
    <view v-if="showQRCode" class="modal">
      <view class="modal-content">
        <view class="close" @click="closeModal">×</view>
        <view class="tip">关注公众号获取更多资讯</view>
        <image
          src="https://jiayaya.oss-cn-hangzhou.aliyuncs.com/account.jpg"
          mode="widthFix"
          class="qrcode"
		  :show-menu-by-longpress="true"
        />
        <view class="tip">长按识别</view>
      </view>
    </view>
<!-- 	<view class="box2">
		<view class="button-1">
			<image src="../../static/index_1.png" mode=""></image>
			<view class="title">
				联系我们
			</view>
		</view>
		<view class="button-2">
			<image src="../../static/index_2.png" mode=""></image>
			<view class="title">
				ai心理咨询
			</view>
		</view>
		<view class="button-3">
			<image src="../../static/index_3.png" mode=""></image>
			<view class="title">
				意见反馈
			</view>
		</view>
	</view> -->
</view>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { getBannerList } from '@/api/banner';
import Banner from '@/models/banner';

// Banner数据
const bannerList = ref([]);

// 控制弹窗显示
const showQRCode = ref(false);

// 获取Banner数据
async function fetchBannerData() {
	try {
		const res = await getBannerList();
		if (res.code === 1 && res.data) {
			// 将API返回的数据转换为Banner模型对象
			bannerList.value = res.data
				.map(item => new Banner(item))
				.sort((a, b) => a.sort - b.sort); // 按sort字段排序
		}
	} catch (error) {
		console.error('获取Banner数据失败:', error);
		// 加载失败时使用默认数据
		bannerList.value = [
			new Banner({
				bannerId: 1,
				image: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/2.jpg',
				type: 3,
				on: 1,
				text: '心理健康小贴士',
				sort: 1
			}),
			new Banner({
				bannerId: 2,
				image: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/3.jpg',
				type: 2,
				on: 1,
				text: '心理减压道具',
				sort: 2
			}),
			new Banner({
				bannerId: 3,
				image: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/backgroundImage.jpg',
				type: 1,
				on: 1,
				text: '舒适咨询环境',
				sort: 3
			})
		];
	}
}

// 处理Banner点击事件
function handleBannerClick(banner) {
	const url = banner.getJumpUrl();
	if (url) {
		uni.navigateTo({
			url,
			fail: () => {
				uni.showToast({
					title: '页面跳转失败',
					icon: 'none'
				});
			}
		});
	}
}

// 页面加载时获取Banner数据
onMounted(() => {
	fetchBannerData();
});

// 显示二维码弹窗
function weChatAccount() {
	showQRCode.value = true;
}

// 关闭弹窗
function closeModal() {
	console.log("dianji");
	showQRCode.value = false;
}

function loadListsFromSession() {
	// const storedLists = wx.getStorageSync('lists');
	// if (storedLists) {
	//   lists.value = JSON.parse(storedLists); // 解析存储的数据
	// }
}

// 跳转到笔记详情
function goToNote(noteId) {
	uni.navigateTo({
		url: `/pages/note/note?id=${noteId}`,
	});
}

function navigateToProp() {
	uni.navigateTo({
		url: '/pages/prop/prop'
	});
}

function navigateToVenue() {
	uni.navigateTo({
		url: '/pages/venue/venue'
	});
}
</script>

<style lang="scss" scoped>
.container{
	width: 100%;
	min-height: 100vh;
	background-color: #f0f4f7;
}
.swiper-box{
	display: flex;
	justify-content: center;
	align-items: center;
	width: 100%;
	height: 550rpx;
}
.swiper{
	display: flex;
	justify-content: center;
	align-items: center;
	width: 100%;
	height: 100%;
	border-radius: 0 0 10rpx;
	
	swiper-item{
		position: relative;
		z-index: 1;
		width: 100%;
		height: 100%;
		border-radius: 16rpx;
		.swiper-item{
			position: relative;
			z-index: 1;
			width: 100%;
			height: 100%;
			border-radius: 16rpx;
		}
		image{
			width: 100%;
			height: 100%;
			border-radius: 16rpx;
		}
		.banner-overlay {
			position: absolute;
			bottom: 0;
			left: 0;
			right: 0;
			padding: 20rpx;
			background: linear-gradient(to top, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0) 100%);
			border-bottom-left-radius: 16rpx;
			border-bottom-right-radius: 16rpx;
			
			.banner-text {
				font-size: 32rpx;
				font-weight: 600;
				color: #ffffff;
				text-shadow: 0 2rpx 4rpx rgba(0,0,0,0.5);
				margin-bottom: 8rpx;
			}
			
			.banner-type {
				display: inline-block;
				font-size: 22rpx;
				color: #ffffff;
				background-color: rgba(26, 115, 232, 0.8);
				padding: 4rpx 16rpx;
				border-radius: 20rpx;
			}
		}
	}
}

.button-box{
	position: relative;
	display: flex;
	justify-content: space-between;
	width: 98%;
	margin-left: 6rpx;
	border-radius: 30rpx;
	background-color: #fff;
	z-index: 0;
	margin-top: -50rpx;
	.button-1,.button-2{
		width: 350rpx;
		height: 200rpx;
		border-radius: 20rpx;
		margin: 10rpx;
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		image{
			position:absolute;
			z-index: 1;
			top: 30rpx;
			width: 200rpx;
			height: 200rpx;
		}
		.image1{
			top: 5rpx;
			width: 250rpx;
			height: 250rpx;
		}
		padding: 10rpx;
		.title{
			position: relative;
			z-index: 3;
			font-weight: bolder;
			font-size: 45rpx;
		}
		.text{
			position: relative;
			z-index: 3;
			margin-top: 10rpx;
			font-size: 23rpx;
			color: #666;
		}
		.back-text{
			position: relative;
			z-index: 3;
			font-size: 24rpx;
			font-weight: 400;
			color: #000;
		}
	}
	.button-1{
		background-color: #fff;
		border-right: 2rpx solid #ddd ;
		margin-right: 0rpx;
	}
	.button-2{
		background-color: #fff;
		border-left: 2rpx solid #ddd;
		margin-left: 0rpx;
	}

}


.recommend{
	font-weight: bolder;
	font-size: 40rpx;
	margin:20rpx
}

.masonry {
  column-count: 2;
  column-gap: 5rpx;
}
.box {
  break-inside: avoid;
  margin-bottom: 0rpx;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-content {
  background-color: #fff;
  padding: 20px;
  border-radius: 10px;
  text-align: center;
  width: 80%;
  position: relative;
}

.close {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 24px;
  color: #999;
  cursor: pointer;
}

.tip {
  font-size: 16px;
  color: #333;
  margin: 10px 0;
}

.qrcode {
  width: 200px;
  height: 200px;
  margin: 10px auto;
}
</style>
