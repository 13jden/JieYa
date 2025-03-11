<template>
	<view class="box">
		<image :src="image" mode="widthFix"></image>
		<view class="title">{{ title }}</view>
		<view class="user">
			<view class="image">
				<image :src="userimage" mode="aspectFill" class="user_image"></image>
			</view>
			<view class="name">{{ username }}</view>
			<uni-icons
				:type="handupType"
				style="margin-left: 100rpx;"
				@click="toggleHandup"
			></uni-icons>
			<view class="good" style="font-size: 20rpx;">{{ good }}</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
	title: {
		type: String,
		default: "志强同志视察计算机学院",
	},
	good: {
		type: Number,
		default: 20,
	},
	image: {
		type: String,
		default:
			"https://yuebaimage.oss-rg-china-mainland.aliyuncs.com/6f4f334d69d9482e835157f3674b62b2.jpg",
	},
	userimage: {
		type: String,
		default: "https://p1.ssl.qhimg.com/t01202765ddf451918a.png",
	},
	username: {
		type: String,
		default: "丁志强",
	},
});
const emit = defineEmits(['click']);

function handleClick() {
  emit('click'); // 触发父组件的点击事件
}
const handupType = ref("hand-up");

const good = ref(20);


const toggleHandup = () => {
	if (handupType.value === "hand-up") {
		handupType.value = "hand-up-filled";
		good.value++;
	} else {
		handupType.value = "hand-up";
		good.value--;
	}
};
</script>

<style lang="scss" scoped>
.box {
	width: 369rpx;
	border-radius: 25rpx;
	box-shadow: 0 0 5rpx rgba(0, 0, 0, 0.2);
	background-color: rgba(255, 255, 255, 0.6);
	height: auto;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	padding: 2rpx;
}

.box image {
	width: 98%;
	border-radius: 10rpx;
	margin-left: 2rpx;
}

.title {
	margin-top:10rpx;
	margin-left: 20rpx;
	font-size: 32rpx;
	white-space: nowrap;           // 强制文本不换行
	overflow: hidden;              // 隐藏溢出部分
	text-overflow: ellipsis;       // 超出部分显示省略号
}

.user {
	height: 80rpx;
	width: 90%;
	padding: 10rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}
.image{
	margin-left: -20rpx;
	width: 70rpx;
	height: 70rpx;
	.user_image {
	width: 70rpx;
	height: 70rpx;
	background-color: black;
	border-radius: 50%;
}
}


.name {
	width: auto;
	font-size: 15rpx;
	margin-left: 20rpx;
}
.good{
	position: relative;
	right: -20rpx;
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
