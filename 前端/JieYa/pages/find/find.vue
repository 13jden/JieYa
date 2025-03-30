<template>
	<view class="title-box">
		<view class="name">
			解鸭呀
		</view>
		<image src="../../static/logo.png" class="image1" mode="aspectFill"></image>
		
		<image src="/static/search.png" mode="aspectFit"></image>
	</view>
	
	<!-- 情绪分类-滑动设计 -->
	<scroll-view class="category-scroll" scroll-x="true" show-scrollbar="false">
		<view class="category-container">
			<view class="category-item" 
						v-for="(category, index) in categories" 
						:key="index"
						:class="{ active: activeCategory === index }"
						@tap="selectCategory(index)">
				{{ category }}
			</view>
		</view>
	</scroll-view>

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
const categories = ref(['全部', '开心', '难过', '焦虑', '兴奋', '平静', '孤独', '感恩']);
const activeCategory = ref(0);

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

// 选择分类
function selectCategory(index) {
  activeCategory.value = index;
  // 这里可以添加根据分类筛选数据的逻辑
  console.log('选择分类:', categories.value[index]);
  // TODO: 根据分类加载笔记数据
}
</script>

<style lang="scss" scoped>
.title-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
  
  .name {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
    background: linear-gradient(135deg, #1a73e8 0%, #0d47a1 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  
  .image1 {
    width: 120rpx;
    height: 120rpx;
    border-radius: 60rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  }
  
  image {
    width: 45rpx;
    height: 45rpx;
    padding: 10rpx;
    background-color: #f8f9fa;
    border-radius: 50%;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  }
}

/* 新的分类滚动样式 */
.category-scroll {
  width: 100%;
  white-space: nowrap;
  padding: 20rpx 0;
  background-color: #fff;
  position: sticky;
  top: 160rpx;
  z-index: 90;
}

.category-container {
  padding: 0 20rpx;
  display: inline-flex;
}

.category-item {
  display: inline-block;
  padding: 12rpx 30rpx;
  margin: 0 10rpx;
  font-size: 28rpx;
  color: #666;
  border-radius: 30rpx;
  transition: all 0.3s ease;
  background-color: #f5f5f5;
  
  &.active {
    background-color: #e6f3ff;
    color: #1a73e8;
    font-weight: 500;
    box-shadow: 0 2rpx 8rpx rgba(26, 115, 232, 0.1);
  }
  
  &:first-child {
    margin-left: 0;
  }
  
  &:last-child {
    margin-right: 0;
  }
}

.masonry {
  column-count: 2;
  column-gap: 2rpx;
  padding: 0rpx;
  background-color: #f8f9fa;
  min-height: calc(100vh - 300rpx);
}

.box {
  break-inside: avoid;
  margin-bottom:5rpx;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.98);
  }
}

.no-more {
  height: 100rpx;
  text-align: center;
  font-size: 26rpx;
  color: #888;
  margin-top: 20rpx;
  padding: 20rpx;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12rpx;
  margin: 20rpx;
}
</style>
