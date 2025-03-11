<template>
	<view class="title-box">
		<view class="name">
			解鸭吖
		</view>
		<image src="/static/search.png" mode="aspectFit"></image>
	</view>
	
	<view class="sort">
		<view class="happy">
			<image src="../../static/happy.png" mode=""></image>开心
		</view>
			<view class="sad">
				<image src="../../static/sad.png" mode=""></image>难过
			</view>
			<view class="anxiety">
				<image src="../../static/anxiety.png" mode=""></image>焦虑
			</view>
			<view class="excited">
				<image src="../../static/excited.png" mode=""></image>兴奋
			</view>
	</view>

    <view class="masonry">
      <view class="box" v-for="(list, index) in lists" :key="index" @click="goToNote(list.nid)">
        <item :username="list.username" :title="list.title" :image="list.coverImage" :userimage="list.userimage"></item>
      </view>
    </view>
    <view v-if="noMoreData" class="no-more" >没有更多了</view> 
</template>

<script setup>
import { onMounted, ref } from 'vue';

const lists = ref([]);
function loadListsFromSession() {
const storedLists = wx.getStorageSync('lists');
  if (storedLists) {
    lists.value = JSON.parse(storedLists); // 解析存储的数据
  }
}

// 页面加载时读取数据
onMounted(() => {
  loadListsFromSession();
});

// 跳转到笔记详情
function goToNote(noteId) {
  uni.navigateTo({
    url: `/pages/note/note?id=${noteId}`,
  });
}
</script>

<style lang="scss" scoped>
.title-box{
	display: flex;
	justify-content: space-between;
	padding-left: 20rpx;
	padding-right: 20rpx;
	padding: 10rpx;
	margin: 10rpx;
	image{
		width: 45rpx;
		height: 45rpx;
	}
}
.sort{
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin: 10rpx;
	.happy,.anxiety,.sad,.excited{
		display: flex;
		justify-content: center;
		align-items: center;
		border-radius: 30rpx;
		background-color: #eff6ff;
		width: 160rpx;
		margin:10rpx;
		height: 80rpx;
		image{
			width: 40rpx;
			height: 40rpx;
			margin: 10rpx;
		}
	}
	.anxiety{
		background-color: #fefce8;
	}
	.excited{
		background-color: #f0fdf4;
	}
	.sad{
		background-color: #fdf2f8;
	}
}
.masonry {
  column-count: 2;
  column-gap: 5rpx;
}
.box {
  break-inside: avoid;
  margin-bottom: 0rpx;
}
.no-more {
  height: 100rpx;
  text-align: center;
  font-size: 26rpx;
  color: #888;
  margin-top: 0rpx;
}
</style>
