<template>
  <view class="item">
    <!-- 图片区 -->
    <image class="item-image" :src="image" mode="aspectFill"></image>
    
    <!-- 内容区 -->
    <view class="item-content">
      <view class="item-title">{{ title }}</view>
      
      <!-- 用户信息和互动区 -->
      <view class="item-footer">
        <view class="user-info">
          <image class="avatar" :src="userimage" mode="aspectFill"></image>
          <text class="username">{{ username }}</text>
        </view>
        
        <!-- 点赞数展示 -->
        <view class="like-count">
          <uni-icons type="heart" size="18" color="#999"></uni-icons>
          <text>{{ likeCount }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
// 定义属性
defineProps({
  username: {
    type: String,
    default: '未知用户'
  },
  title: {
    type: String,
    default: ''
  },
  image: {
    type: String,
    default: ''
  },
  userimage: {
    type: String,
    default: ''
  },
  likeCount: {
    type: Number,
    default: 0
  }
});
</script>

<style lang="scss" scoped>
.item {
  border-radius: 16rpx;
  overflow: hidden;
  background-color: #fff;
  margin-bottom: 10rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  
  .item-image {
    width: 100%;
    height: 300rpx;
  }
  
  .item-content {
    padding: 20rpx;
  }
}
</style> 