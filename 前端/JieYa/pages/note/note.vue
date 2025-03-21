<template>
  <view class="note-container">
    <!-- 图片轮播图 -->
    <swiper 
      class="swiper" 
      circular 
      indicator-dots 
      autoplay
      interval="4000"
      duration="500"
      indicator-active-color="#ffffff"
      indicator-color="rgba(255, 255, 255, 0.3)">
      <swiper-item v-for="(image, index) in noteData.images" :key="index">
        <image class="swiper-image" :src="image" mode="aspectFill"></image>
      </swiper-item>
    </swiper>
    
    <!-- 笔记内容 -->
    <view class="note-content">
      <view class="note-title">{{ noteData.title || '无标题' }}</view>
      <view class="note-text">{{ noteData.content || '暂无内容' }}</view>
      
      <!-- 标签 -->
      <view class="tags-container" v-if="noteData.tags && noteData.tags.length > 0">
        <view class="tag-item" v-for="(tag, index) in noteData.tags" :key="index">
          #{{ tag }}
        </view>
      </view>
      
      <!-- 可见性指示器 -->
      <view class="visibility-indicator">
        <text class="visibility-icon">
          {{ 
            noteData.visibility === 'private' ? '🔒' : 
            noteData.visibility === 'friends' ? '👥' : '🌐'
          }}
        </text>
        <text class="visibility-text">
          {{ 
            noteData.visibility === 'private' ? '仅自己可见' : 
            noteData.visibility === 'friends' ? '好友可见' : '所有人可见'
          }}
        </text>
      </view>
    </view>
    
    <!-- 评论区域 - 仅在非预览模式显示 -->
    <view class="comments-section" v-if="!isPreviewMode">
      <view class="section-title">评论区</view>
      
      <!-- 评论列表 -->
      <view class="comments-list">
        <view class="comment-item" v-for="(comment, index) in comments" :key="index">
          <!-- 父评论 -->
          <view class="parent-comment">
            <image class="comment-avatar" :src="comment.avatar || defaultAvatar"></image>
            <view class="comment-content">
              <view class="comment-header">
                <text class="comment-username">{{ comment.username }}</text>
                <text class="comment-time">{{ formatTime(comment.time) }}</text>
              </view>
              <text class="comment-text">{{ comment.content }}</text>
              <view class="comment-actions">
                <text class="action-btn" @click="replyTo(comment.id, comment.username)">回复</text>
              </view>
            </view>
          </view>
          
          <!-- 子评论 -->
          <view class="child-comments" v-if="comment.replies && comment.replies.length > 0">
            <view class="child-comment" v-for="(reply, replyIndex) in comment.replies" :key="replyIndex">
              <image class="comment-avatar small" :src="reply.avatar || defaultAvatar"></image>
              <view class="comment-content">
                <view class="comment-header">
                  <text class="comment-username">{{ reply.username }}</text>
                  <text class="reply-to">回复</text>
                  <text class="comment-username">{{ reply.replyToUsername }}</text>
                  <text class="comment-time">{{ formatTime(reply.time) }}</text>
                </view>
                <text class="comment-text">{{ reply.content }}</text>
                <view class="comment-actions">
                  <text class="action-btn" @click="replyTo(comment.id, reply.username)">回复</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 评论输入框 -->
      <view class="comment-input-container">
        <input 
          type="text" 
          v-model="commentText" 
          :placeholder="replyInfo.isReplying ? `回复 ${replyInfo.username}：` : '说点什么...'" 
          class="comment-input"
          confirm-type="send"
          @confirm="submitComment" />
        <button class="send-btn" @click="submitComment" :disabled="!commentText.trim()">发送</button>
      </view>
    </view>
    
    <!-- 预览模式提示 -->
    <view class="preview-mode-indicator" v-if="isPreviewMode">
      <text>预览模式</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';

const isPreviewMode = ref(false);
const defaultAvatar = "https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0";
const noteData = ref({
  title: '',
  content: '',
  tags: [],
  images: [],
  visibility: 'public'
});

// 评论相关
const comments = ref([]);
const commentText = ref('');
const replyInfo = ref({
  isReplying: false,
  commentId: null,
  username: ''
});

// 获取页面参数
onMounted(() => {
  // 微信小程序获取页面参数的修复方法
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  
  // 获取URL查询参数
  let options = {};
  if (currentPage && currentPage.options) {
    options = currentPage.options;
  } else {
    // 尝试从路由中获取参数
    const query = uni.getSystemInfoSync().platform === 'devtools' 
      ? uni.$route && uni.$route.query
      : null;
    options = query || {};
  }
  
  console.log('页面参数:', options);
  
  if (options.id) {
    console.log('从发现页进入，笔记ID:', options.id);
    isPreviewMode.value = false;
    loadNoteData(options.id);
    loadComments();
  } else {
    try {
      const previewData = wx.getStorageSync('notePreview');
      if (previewData && previewData.isPreview) {
        console.log('预览模式已启用');
        isPreviewMode.value = true;
        noteData.value = previewData;
      } else {
        // 如果不是预览数据，则以普通模式加载
        isPreviewMode.value = false;
        loadNoteData();
        loadComments();
      }
    } catch (e) {
      console.error('获取预览数据失败:', e);
      isPreviewMode.value = false;
      loadNoteData();
      loadComments();
    }
  }
});

function loadPreviewData() {
  try {
    const previewData = wx.getStorageSync('notePreview');
    if (previewData) {
      noteData.value = previewData;
    }
  } catch (e) {
    console.error('加载预览数据失败:', e);
    uni.showToast({
      title: '加载预览数据失败',
      icon: 'none'
    });
  }
}

function loadNoteData(id) {
  // 实际项目中，这里应该是从API获取笔记数据
  // 这里仅做示例
  noteData.value = {
    title: '示例笔记标题',
    content: '这是笔记的内容，实际项目中应该从后端API获取。',
    tags: ['示例标签1', '示例标签2'],
    images: [
      'https://picsum.photos/500/300?random=1',
      'https://picsum.photos/500/300?random=2',
      'https://picsum.photos/500/300?random=3'
    ],
    visibility: 'public'
  };
}

function loadComments() {
  // 模拟评论数据
  comments.value = [
    {
      id: 1,
      username: '用户A',
      avatar: 'https://picsum.photos/100/100?random=1',
      content: '这是一条父评论',
      time: Date.now() - 3600000,
      replies: [
        {
          id: 11,
          username: '用户B',
          replyToUsername: '用户A',
          avatar: 'https://picsum.photos/100/100?random=2',
          content: '这是对父评论的回复',
          time: Date.now() - 1800000
        }
      ]
    },
    {
      id: 2,
      username: '用户C',
      avatar: 'https://picsum.photos/100/100?random=3',
      content: '这是另一条父评论',
      time: Date.now() - 7200000,
      replies: []
    }
  ];
}

function formatTime(timestamp) {
  const now = Date.now();
  const diff = now - timestamp;
  
  // 小于1分钟
  if (diff < 60000) {
    return '刚刚';
  }
  // 小于1小时
  if (diff < 3600000) {
    return Math.floor(diff / 60000) + '分钟前';
  }
  // 小于1天
  if (diff < 86400000) {
    return Math.floor(diff / 3600000) + '小时前';
  }
  // 小于1周
  if (diff < 604800000) {
    return Math.floor(diff / 86400000) + '天前';
  }
  
  // 其他情况显示具体日期
  const date = new Date(timestamp);
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`;
}

function replyTo(commentId, username) {
  replyInfo.value = {
    isReplying: true,
    commentId: commentId,
    username: username
  };
  // 聚焦输入框
  uni.$nextTick(() => {
    const query = uni.createSelectorQuery();
    query.select('.comment-input').boundingClientRect();
    query.exec((res) => {
      // 滚动到输入框
      uni.pageScrollTo({
        selector: '.comment-input',
        duration: 300
      });
    });
  });
}

function submitComment() {
  if (!commentText.value.trim()) return;
  
  if (replyInfo.value.isReplying) {
    // 添加子评论
    const parentComment = comments.value.find(c => c.id === replyInfo.value.commentId);
    if (parentComment) {
      parentComment.replies.push({
        id: Date.now(),
        username: '当前用户', // 实际项目中应该是登录用户名
        replyToUsername: replyInfo.value.username,
        avatar: defaultAvatar, // 实际项目中应该是登录用户头像
        content: commentText.value,
        time: Date.now()
      });
    }
  } else {
    // 添加父评论
    comments.value.unshift({
      id: Date.now(),
      username: '当前用户', // 实际项目中应该是登录用户名
      avatar: defaultAvatar, // 实际项目中应该是登录用户头像
      content: commentText.value,
      time: Date.now(),
      replies: []
    });
  }
  
  // 重置状态
  commentText.value = '';
  replyInfo.value = {
    isReplying: false,
    commentId: null,
    username: ''
  };
}
</script>

<style lang="scss" scoped>
.note-container {
  min-height: 100vh;
  background-color: #fff;
  display: flex;
  flex-direction: column;
}

.swiper {
  height: 500rpx;
  width: 100%;
}

.swiper-image {
  width: 100%;
  height: 100%;
}

.note-content {
  padding: 30rpx;
}

.note-title {
  font-size: 40rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
  color: #333;
}

.note-text {
  font-size: 28rpx;
  line-height: 1.6;
  color: #666;
  margin-bottom: 30rpx;
  white-space: pre-wrap;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  margin: 20rpx 0;
}

.tag-item {
  background-color: #e3f2fd;
  color: #1a73e8;
  padding: 6rpx 20rpx;
  border-radius: 30rpx;
  font-size: 24rpx;
  margin-right: 16rpx;
  margin-bottom: 16rpx;
}

.visibility-indicator {
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: #999;
  margin-top: 20rpx;
  
  .visibility-icon {
    margin-right: 10rpx;
  }
}

.comments-section {
  margin-top: 40rpx;
  padding: 0 30rpx 120rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 30rpx;
  color: #333;
  position: relative;
  padding-left: 20rpx;
  
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 6rpx;
    height: 30rpx;
    width: 6rpx;
    background-color: #1a73e8;
    border-radius: 3rpx;
  }
}

.comment-item {
  margin-bottom: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  padding-bottom: 20rpx;
}

.parent-comment, .child-comment {
  display: flex;
  margin-bottom: 20rpx;
}

.child-comments {
  margin-left: 80rpx;
  background-color: #f9f9f9;
  border-radius: 12rpx;
  padding: 20rpx;
}

.comment-avatar {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  
  &.small {
    width: 50rpx;
    height: 50rpx;
  }
}

.comment-content {
  flex: 1;
}

.comment-header {
  margin-bottom: 10rpx;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.comment-username {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
  margin-right: 10rpx;
}

.reply-to {
  font-size: 24rpx;
  color: #999;
  margin: 0 10rpx;
}

.comment-time {
  font-size: 22rpx;
  color: #999;
  margin-left: auto;
}

.comment-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
  word-wrap: break-word;
}

.comment-actions {
  margin-top: 10rpx;
  display: flex;
  justify-content: flex-end;
}

.action-btn {
  font-size: 24rpx;
  color: #1a73e8;
  margin-left: 20rpx;
}

.comment-input-container {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background-color: #fff;
  display: flex;
  align-items: center;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.comment-input {
  flex: 1;
  height: 70rpx;
  background-color: #f5f5f5;
  border-radius: 35rpx;
  padding: 0 30rpx;
  font-size: 26rpx;
}

.send-btn {
  margin-left: 20rpx;
  background-color: #1a73e8;
  color: white;
  font-size: 26rpx;
  padding: 10rpx 30rpx;
  border-radius: 35rpx;
  height: 70rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &:disabled {
    background-color: #ccc;
  }
}

.preview-mode-indicator {
  position: fixed;
  right: 30rpx;
  top: 30rpx;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 10rpx 20rpx;
  border-radius: 30rpx;
  font-size: 24rpx;
  z-index: 100;
}
</style> 