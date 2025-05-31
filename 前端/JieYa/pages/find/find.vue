<template>
	<view class="title-box">
    <image src="../../static/logo.png" class="image1" mode="aspectFill"></image>
		<view class="name">
			轻屿jieya
		</view>
		
		<image src="/static/search.png" mode="aspectFit" @click="goToSearch"></image>
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

    <!-- 使用scroll-view包裹瀑布流区域，支持下拉刷新 -->
    <scroll-view 
      class="content-scroll" 
      scroll-y="true"
      refresher-enabled="true"
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <view class="masonry">
        <view class="box" v-for="(note, index) in noteList" :key="index" @click="goToNote(note.id)">
          <item 
            :username="note.user ? note.user.nickName : '未知用户'" 
            :title="note.title" 
            :image="note.coverImage" 
            :userimage="note.user ? note.user.avatar : ''" 
            :likeCount="note.likeCount || 0"
          ></item>
        </view>
      </view>
      <view v-if="loading" class="loading">加载中...</view>
      <view v-if="noMoreData" class="no-more">没有更多了</view>
    </scroll-view>
    
    <!-- 回到顶部按钮 -->
    <view class="back-to-top" v-if="showBackToTop" @click="scrollToTop">
      <uni-icons type="top" size="30" color="#1a73e8"></uni-icons>
    </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { onPullDownRefresh, onReachBottom, onPageScroll } from '@dcloudio/uni-app';
import { getNoteList } from '@/api/note';

// 页面数据
const noteList = ref([]);
const categories = ref(['全部', '开心', '难过', '焦虑', '兴奋', '平静', '孤独', '感恩']);
const activeCategory = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const noMoreData = ref(false);
const showBackToTop = ref(false);
const scrollTop = ref(0);
// 新增刷新状态控制
const isRefreshing = ref(false);

// 页面加载时获取笔记列表
onMounted(() => {
  fetchNoteList();
});

// 监听页面滚动
onPageScroll((e) => {
  scrollTop.value = e.scrollTop;
  showBackToTop.value = scrollTop.value > 300;
});

// 获取笔记列表
async function fetchNoteList(append = false) {
  if (loading.value) return;
  
  try {
    loading.value = true;
    
    // 获取选中的情绪类型（如果不是"全部"）
    const emotion = activeCategory.value === 0 ? undefined : categories.value[activeCategory.value];
    
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      emotion: emotion
    };
    
    const res = await getNoteList(params);
    
    if (res.code === 1) {
      if (append) {
        noteList.value = [...noteList.value, ...res.data.records];
      } else {
        noteList.value = res.data.records;
      }
      
      noMoreData.value = res.data.records.length < pageSize.value;
      
      // 如果是第一页，而且没有数据，显示无数据提示
      if (currentPage.value === 1 && noteList.value.length === 0) {
        noMoreData.value = true;
      }
    } else {
      uni.showToast({
        title: '获取笔记失败',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('获取笔记列表失败:', error);
    uni.showToast({
      title: '网络错误，请重试',
      icon: 'none'
    });
  } finally {
    loading.value = false;
    isRefreshing.value = false; // 结束刷新状态
  }
}

// scroll-view的下拉刷新处理
function onRefresh() {
  console.log('scroll-view 下拉刷新触发');
  isRefreshing.value = true; // 设置刷新状态为true
  currentPage.value = 1;
  noMoreData.value = false;
  fetchNoteList(false);
}

// scroll-view的加载更多处理
function onLoadMore() {
  console.log('滚动到底部，加载更多');
  if (!loading.value && !noMoreData.value) {
    currentPage.value++;
    fetchNoteList(true);
  }
}

// 下拉刷新 (页面级别的下拉刷新，如果使用scroll-view可以忽略)
onPullDownRefresh(() => {
  console.log('页面下拉刷新触发');
  currentPage.value = 1;
  noMoreData.value = false;
  fetchNoteList(false);
});

// 上拉加载更多 (页面级别的加载更多，如果使用scroll-view可以忽略)
onReachBottom(() => {
  if (!noMoreData.value) {
    currentPage.value++;
    fetchNoteList(true);
  }
});

// 选择分类
function selectCategory(index) {
  if (activeCategory.value === index) return;
  
  activeCategory.value = index;
  currentPage.value = 1;
  noMoreData.value = false;
  fetchNoteList(false);
}

// 跳转到笔记详情
function goToNote(noteId) {
  uni.navigateTo({
    url: `/pages/note/note?id=${noteId}`,
  });
}

// 回到顶部
function scrollToTop() {
  uni.pageScrollTo({
    scrollTop: 0,
    duration: 300
  });
}

// 跳转到搜索页面
function goToSearch() {
  uni.navigateTo({
    url: '/pages/search/search'
  });
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
  height: 60rpx;
  
  .name {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
    background: linear-gradient(135deg, #1a73e8 0%, #0d47a1 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  
  .image1 {
    position: relative;
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    object-fit: cover;
    z-index: -1;
    padding: 0;  /* 移除内边距让图像填满容器 */
    transform: scale(1.15);  /* 放大图像但保持容器尺寸 */
    transform-origin: center;
  }
  
  image {
    width: 50rpx;
    height: 50rpx;
    padding: 10rpx;
    background-color: #f8f9fa;
    border-radius: 50%;
  }
}

/* 新的分类滚动样式 */
.category-scroll {
  width: 100%;
  white-space: nowrap;
  padding: 20rpx 0;
  background-color: #fff;
  position: sticky;
  top: 130rpx;
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

.loading {
  text-align: center;
  padding: 20rpx;
  color: #666;
  font-size: 26rpx;
}

.back-to-top {
  position: fixed;
  bottom: 100rpx;
  right: 40rpx;
  width: 80rpx;
  height: 80rpx;
  background-color: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.2);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
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

/* 新增 scroll-view 相关样式 */
.content-scroll {
  height: calc(100vh - 220rpx); /* 减去顶部组件的高度 */
  width: 100%;
  background-color: #f8f9fa;
}
</style>

<script>
export default {
  // 正确的配置方式
  options: {
    enablePullDownRefresh: true,
    backgroundTextStyle: 'dark'
  },
  // 设置导航栏样式
  navigationStyle: 'custom'
}
</script>
