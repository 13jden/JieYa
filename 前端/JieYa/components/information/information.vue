<template>
	<view class="container">
		<image :src="props.image" mode="aspectFill" class="news-image"></image>
		<view class="box">
			<view class="title">
				{{ props.title }}
			</view>
			<view class="text">
				{{ props.summary }}
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';


const props = defineProps({
	image: {
		type: String,
		default: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/4.jpg",
	},
	
	title: {
		type: String,
		default: "开学季————开学心理调适指南",
	},
	summary: {
		type: String,
		default: "正文内容...",
	},
});
const emit = defineEmits(['click']);

function handleClick() {
  emit('click'); // 触发父组件的点击事件
}
</script>

<style lang="scss" scoped>
.container {
	display: flex;
	flex-direction: column;
	background-color: #fff;
	border-radius: 8px;
	overflow: hidden;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.news-image {
	width: 100%;
	height: 200px;
}

.box {
	padding: 16px;
}

.title {
	font-size: 18px;
	font-weight: bold;
	margin-bottom: 8px;
	color: #333;
}

.text {
	font-size: 14px;
	color: #666;
	line-height: 1.5;
}
</style>