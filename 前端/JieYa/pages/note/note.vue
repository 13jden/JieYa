<template>
  <view class="note-container">
    <!-- 加载中提示 -->
    <view class="loading-container" v-if="loading">
      <view class="loading-spinner"></view>
      <text>加载中...</text>
    </view>
    
    <template v-else>
      <!-- 预览模式指示器 -->
      <view class="preview-mode-indicator" v-if="isPreview">预览模式</view>
      
      <!-- 作者信息 - 固定在顶部 -->
      <view class="author-section fixed-top" v-if="!isPreview && noteData.user">
        <view class="author-info" @click="goToUser(noteData.user.userId)">
          <image class="author-avatar" :src="noteData.user.avatar || defaultAvatar"></image>
          <view class="author-detail">
            <text class="author-name">{{ noteData.user.nickName || '未知用户' }}</text>
            <text class="author-school">{{ noteData.user.school || '' }}</text>
          </view>
        </view>
        <!-- 只有当不是自己的笔记时才显示关注按钮 -->
        <button class="follow-btn" 
                :class="{ following: isFollowing }" 
                @click="handleFollow" 
                v-if="currentUser && currentUser.userId !== noteData.user.userId">
          {{ isFollowing ? '已关注' : '关注' }}
        </button>
      </view>
      
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
        <swiper-item v-for="(image, index) in previewImages" :key="index" @click="previewImage(image, index)">
          <image class="swiper-image" :src="image" mode="aspectFill"></image>
        </swiper-item>
      </swiper>
      
      <!-- 笔记内容 -->
      <view class="note-content">
        <view class="note-title">{{ noteData.title || '无标题' }}</view>
        <view class="note-text">{{ noteData.content || '暂无内容' }}</view>
        
        <!-- 标签 -->
        <view class="tags-container" v-if="noteData.tags && noteData.tags.length > 0">
          <view class="tag-item" v-for="(tag, index) in parsedTags" :key="index">
            #{{ tag }}
          </view>
        </view>
        
        <!-- 互动按钮 - 仅在非预览模式显示 -->
        <view class="interaction-bar" v-if="!isPreview">
          <view class="interaction-item" @click="handleLike">
            <uni-icons :type="isLiked ? 'heart-filled' : 'heart'" size="24" :color="isLiked ? '#ff4757' : '#999'"></uni-icons>
            <text :class="{ active: isLiked }">{{ noteData.likeCount || 0 }}</text>
          </view>
          <view class="interaction-item" @click="handleCollect">
            <uni-icons :type="isCollected ? 'star-filled' : 'star'" size="24" :color="isCollected ? '#ffbc00' : '#999'"></uni-icons>
            <text :class="{ active: isCollected }">{{ noteData.collectCount || 0 }}</text>
          </view>
          <view class="interaction-item">
            <uni-icons type="chat" size="24" color="#999"></uni-icons>
            <text>{{ commentCount }}</text>
          </view>
          <view class="interaction-item">
            <uni-icons type="download" size="24" color="#999"></uni-icons>
          </view>
        </view>
      </view>
      
      <!-- 评论区域 - 仅在非预览模式显示 -->
      <view class="comments-section" v-if="!isPreview">
        <view class="section-title">评论区 ({{ commentCount }})</view>
        
        <!-- 评论列表 -->
        <view class="comments-list">
          <view v-if="comments.length === 0" class="empty-comments">
            还没有评论，快来抢沙发吧！
          </view>
          
          <!-- 父评论 -->
          <view class="comment-item" v-for="(comment, index) in comments" :key="index">
            <view class="parent-comment">
              <image class="comment-avatar" :src="comment.avatar || defaultAvatar" @click="goToUser(comment.userId)"></image>
              <view class="comment-content">
                <view class="comment-header">
                  <text class="comment-username" @click="goToUser(comment.userId)">{{ comment.userName }}</text>
                  <text class="comment-time">{{ formatTime(comment.time) }}</text>
                </view>
                <text class="comment-text">{{ comment.content }}</text>
                <view class="comment-actions">
                  <text class="action-btn" @click="replyTo(comment, {userId: comment.userId, userName: comment.userName})">回复</text>
                </view>
              </view>
            </view>
            
            <!-- 只有当有子评论时才显示查看回复按钮 -->
            <view v-if="comment.childrenCount > 0" 
                  class="view-replies" 
                  @click="handleViewReplies(comment.id)">
              <text>{{ comment.showChildren ? '收起回复' : `查看${comment.childrenCount}条回复` }}</text>
            </view>
            
            <!-- 只有当showChildren为true时才显示子评论列表 -->
            <view class="child-comments" v-if="comment.showChildren && comment.children && comment.children.length > 0">
              <view class="child-comment" v-for="(reply, replyIndex) in comment.children" :key="replyIndex">
                <image class="comment-avatar small" :src="reply.avatar || defaultAvatar" @click="goToUser(reply.userId)"></image>
                <view class="comment-content">
                  <view class="comment-header">
                    <text class="comment-username" @click="goToUser(reply.userId)">{{ reply.userName }}</text>
                    <text class="reply-to" v-if="reply.toUserId">回复</text>
                    <text class="comment-username" v-if="reply.toUserId" @click="goToUser(reply.toUserId)">{{ reply.replyName }}</text>
                    <text class="comment-time">{{ formatTime(reply.time) }}</text>
                  </view>
                  <text class="comment-text">{{ reply.content }}</text>
                  <view class="comment-actions">
                    <text class="action-btn" @click="replyTo(reply, {userId: reply.userId, userName: reply.userName})">回复</text>
                  </view>
                </view>
              </view>
              
              <!-- 加载更多子评论 -->
              <view class="load-more-replies" 
                    v-if="comment.childrenCount > comment.children.length"
                    @click="loadMoreReplies(comment.id)">
                加载更多回复
              </view>
            </view>
          </view>
          
          <!-- 加载更多评论 -->
          <view class="load-more" v-if="hasMoreComments" @click="loadMoreComments">
            加载更多评论
          </view>
        </view>
      </view>
    </template>
    
    <!-- 评论输入框 - 仅在非预览模式显示 -->
    <view class="comment-input-container" v-if="!loading && !isPreview">
      <input 
        class="comment-input" 
        type="text" 
        :placeholder="replyPlaceholder"
        v-model="commentContent"
        confirm-type="send"
        @confirm="submitComment"
      />
      <button class="send-btn" :disabled="!commentContent.trim()" @click="submitComment">发送</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';

import { onLoad } from '@dcloudio/uni-app';
import { getNoteDetail } from '@/api/note';
import { addLike, deleteLike, addCollect, deleteCollect, checkLikeStatus, checkCollectStatus, checkUserActive } from '@/api/userActive';
import { getCommentsByNoteId, getChildComments, addComment } from '@/api/comment';
import { addFocus, deleteFocus, checkFocusStatus } from '@/api/focus';

// 页面数据
const noteId = ref('');
const noteData = ref({});
const comments = ref([]);
const loading = ref(true);
const commentContent = ref('');
const isLiked = ref(false);
const isCollected = ref(false);
const isFollowing = ref(false);
const currentUser = ref(null);
const isPreview = ref(false);

// 回复相关
const replyingTo = ref(null);
const replyParentId = ref(null);
const replyToUsername = ref('');
const replyToUserId = ref('');

// 分页相关
const currentCommentPage = ref(1);
const commentPageSize = ref(10);
const hasMoreComments = ref(false);
const commentCount = ref(0);

// 默认头像
const defaultAvatar = 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg';

// 计算属性
const replyPlaceholder = computed(() => {
  if (replyingTo.value) {
    return `回复 @${replyToUsername.value}`;
  }
  return '写下你的评论...';
});

const isOwnNote = computed(() => {
  if (!currentUser.value || !noteData.value.user) return false;
  
  return currentUser.value.userId === noteData.value.user.userId;
});

// 计算图片数组
const noteImages = computed(() => {
  if (noteData.value && noteData.value.noteImages && noteData.value.noteImages.length > 0) {
    return noteData.value.noteImages;
  }
  return [];
});

// 解析标签，处理不同格式的标签数据
const parsedTags = computed(() => {
  if (!noteData.value.tags) return [];
  
  // 如果标签是字符串，尝试解析JSON
  if (typeof noteData.value.tags === 'string') {
    try {
      const parsed = JSON.parse(noteData.value.tags);
      // 处理可能的嵌套数组 ['["tag1"]']
      if (Array.isArray(parsed) && parsed.length > 0) {
        if (typeof parsed[0] === 'string' && parsed[0].startsWith('[')) {
          try {
            return JSON.parse(parsed[0]);
          } catch {
            return parsed;
          }
        }
        return parsed;
      }
      return [];
    } catch {
      return [noteData.value.tags];
    }
  }
  
  // 如果标签已经是数组
  return noteData.value.tags;
});

// 获取预览图片
const previewImages = computed(() => {
  if (isPreview.value && noteData.value.images) {
    return noteData.value.images;
  } else if (noteData.value.noteImages && noteData.value.noteImages.length > 0) {
    return noteData.value.noteImages.map(img => img.imagePath);
  } else if (noteData.value.coverImage) {
    return [formatImageUrl(noteData.value.coverImage)];
  }
  return [];
});

// 页面加载
onLoad((options) => {
  if (options.id) {
    // 正常模式
    noteId.value = options.id;
    isPreview.value = false;
    
    // 获取当前登录用户信息
    const userInfo = uni.getStorageSync('userInfo');
    if (userInfo) {
      currentUser.value = typeof userInfo === 'string' ? JSON.parse(userInfo) : userInfo;
    }
    
    // 加载笔记详情和评论
    fetchNoteDetail();
    fetchComments();
  } else {
    // 预览模式 - 从本地存储获取预览数据
    isPreview.value = true;
    loadPreviewData();
  }
});

// 加载预览数据
function loadPreviewData() {
  try {
    loading.value = true;
    const previewData = uni.getStorageSync('notePreview');
    
    if (previewData) {
      noteData.value = previewData;
      console.log('预览数据:', previewData);
    } else {
      uni.showToast({
        title: '没有预览数据',
        icon: 'none'
      });
      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
    }
  } catch (error) {
    console.error('加载预览数据失败:', error);
    uni.showToast({
      title: '加载预览失败',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
}

// 获取笔记详情
async function fetchNoteDetail() {
  // 如果是预览模式，则不从服务器获取数据
  if (isPreview.value) return;
  
  try {
    loading.value = true;
    
    const res = await getNoteDetail(noteId.value);
    
    if (res.code === 1 && res.data) {
      noteData.value = res.data;
      
      // 检查当前用户的点赞和收藏状态
      if (currentUser.value) {
        checkUserInteractions();
      }
    } else {
      uni.showToast({
        title: '获取笔记详情失败',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('获取笔记详情失败:', error);
    uni.showToast({
      title: '网络错误，请重试',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
}

// 检查用户与笔记的交互状态
async function checkUserInteractions() {
  if (!currentUser.value) return;
  
  try {
    // 使用新API检查点赞和收藏状态
    const activeRes = await checkUserActive(noteId.value);
    if (activeRes.code === 1) {
      isLiked.value = activeRes.data.like;
      isCollected.value = activeRes.data.collect;
    }
    
    // 根据noteData中的isFocus字段设置关注状态
    if (noteData.value && noteData.value.isFocus !== undefined) {
      // isFocus: 0-自己 1-已关注 -1-未关注
      if (noteData.value.isFocus === 0) {
        // 是自己的笔记，设置isOwnNote为true (实际上这由计算属性处理)
      } else if (noteData.value.isFocus === 1) {
        // 已关注
        isFollowing.value = true;
      } else if (noteData.value.isFocus === -1) {
        // 未关注
        isFollowing.value = false;
      }
    }
  } catch (error) {
    console.error('检查交互状态失败:', error);
  }
}

// 获取评论列表
async function fetchComments(append = false) {
  try {
    const res = await getCommentsByNoteId(noteId.value, currentCommentPage.value, commentPageSize.value);
    
    if (res.code === 1) {
      const formattedComments = res.data.records.map(comment => ({
        ...comment,
        showChildren: false  // 添加showChildren属性，默认不显示子评论
      }));
      
      if (append) {
        comments.value = [...comments.value, ...formattedComments];
      } else {
        comments.value = formattedComments;
      }
      
      commentCount.value = res.data.total;
      hasMoreComments.value = comments.value.length < res.data.total;
    }
  } catch (error) {
    console.error('获取评论失败:', error);
  }
}

// 加载子评论
async function loadChildComments(parentId, pageNum = 1) {
  try {
    const res = await getChildComments(parentId, pageNum, 5);
    
    if (res.code === 1) {
      const parentIndex = comments.value.findIndex(c => c.id === parentId);
      if (parentIndex !== -1) {
        if (!comments.value[parentIndex].children) {
          comments.value[parentIndex].children = [];
        }
        
        if (pageNum === 1) {
          comments.value[parentIndex].children = res.data.records;
        } else {
          comments.value[parentIndex].children = [
            ...comments.value[parentIndex].children,
            ...res.data.records
          ];
        }
        
        comments.value[parentIndex].childrenCount = res.data.total;
      }
    }
  } catch (error) {
    console.error('获取子评论失败:', error);
    uni.showToast({
      title: '加载回复失败',
      icon: 'none'
    });
  }
}

// 加载更多评论
function loadMoreComments() {
  currentCommentPage.value++;
  fetchComments(true);
}

// 加载更多回复
function loadMoreReplies(parentId) {
  const parentComment = comments.value.find(c => c.id === parentId);
  if (parentComment && parentComment.children) {
    const currentPage = Math.ceil(parentComment.children.length / 5);
    loadChildComments(parentId, currentPage + 1);
  }
}

// 处理点赞
async function handleLike() {
  if (!currentUser.value) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    });
    return;
  }
  
  try {
    if (isLiked.value) {
      // 取消点赞
      await deleteLike(noteData.value.user.userId, noteId.value);
      noteData.value.likeCount = Math.max(0, (noteData.value.likeCount || 1) - 1);
    } else {
      // 添加点赞
      await addLike(noteData.value.user.userId, noteId.value);
      noteData.value.likeCount = (noteData.value.likeCount || 0) + 1;
    }
    
    // 更新点赞状态
    isLiked.value = !isLiked.value;
    
    uni.showToast({
      title: isLiked.value ? '已点赞' : '已取消点赞',
      icon: 'none'
    });
  } catch (error) {
    console.error('点赞操作失败:', error);
    uni.showToast({
      title: '操作失败，请重试',
      icon: 'none'
    });
  }
}

// 处理收藏
async function handleCollect() {
  if (!currentUser.value) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    });
    return;
  }
  
  try {
    if (isCollected.value) {
      // 取消收藏
      await deleteCollect(currentUser.value.userId, noteId.value);
      noteData.value.collectCount = Math.max(0, (noteData.value.collectCount || 1) - 1);
    } else {
      // 添加收藏
      await addCollect(currentUser.value.userId, noteId.value);
      noteData.value.collectCount = (noteData.value.collectCount || 0) + 1;
    }
    
    // 更新收藏状态
    isCollected.value = !isCollected.value;
    
    uni.showToast({
      title: isCollected.value ? '已收藏' : '已取消收藏',
      icon: 'none'
    });
  } catch (error) {
    console.error('收藏操作失败:', error);
    uni.showToast({
      title: '操作失败，请重试',
      icon: 'none'
    });
  }
}

// 处理关注
async function handleFollow() {
  if (!currentUser.value) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    });
    return;
  }
  
  if (!noteData.value.user || !noteData.value.user.userId) {
    uni.showToast({
      title: '作者信息缺失',
      icon: 'none'
    });
    return;
  }
  
  try {
    if (isFollowing.value) {
      // 取消关注
      await deleteFocus(currentUser.value.userId, noteData.value.user.userId);
      // 更新状态
      noteData.value.isFocus = -1;
    } else {
      // 添加关注
      await addFocus(currentUser.value.userId, noteData.value.user.userId);
      // 更新状态
      noteData.value.isFocus = 1;
    }
    
    // 更新关注状态
    isFollowing.value = !isFollowing.value;
    
    uni.showToast({
      title: isFollowing.value ? '已关注' : '已取消关注',
      icon: 'none'
    });
  } catch (error) {
    console.error('关注操作失败:', error);
    uni.showToast({
      title: '操作失败，请重试',
      icon: 'none'
    });
  }
}

// 设置回复对象
function replyTo(comment, replyToUser) {
  // 判断是否是父评论（没有parentId的就是父评论）
  const isParentComment = !comment.parentId;
  
  // 确保parentId是字符串类型
  replyParentId.value = isParentComment ? String(comment.id) : String(comment.parentId);
  
  // 只有回复子评论时才需要设置toUserId
  replyToUserId.value = isParentComment ? null : replyToUser.userId;
  replyToUsername.value = replyToUser.userName;
  replyingTo.value = true;
  
  // 聚焦输入框
  setTimeout(() => {
    uni.createSelectorQuery()
      .select('.comment-input')
      .boundingClientRect(data => {
        console.log('input rect:', data);
      })
      .exec();
  }, 100);
}

// 提交评论
async function submitComment() {
  if (!currentUser.value) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    });
    return;
  }
  
  if (!commentContent.value.trim()) {
    uni.showToast({
      title: '评论内容不能为空',
      icon: 'none'
    });
    return;
  }
  
  try {
    uni.showLoading({
      title: '发送中...'
    });
    
    // 构建评论数据
    const commentData = {
      content: commentContent.value,
      noteId: noteId.value  // 始终传递noteId
    };
    
    // 如果是回复评论
    if (replyingTo.value && replyParentId.value) {
      // 确保parentId是字符串
      commentData.parentId = String(replyParentId.value);
      
      console.log('回复评论，parentId:', commentData.parentId);
      
      // 只有回复子评论时才传toUser
      if (replyToUserId.value) {
        commentData.toUser = String(replyToUserId.value);
      }
    }
    
    console.log('发送评论数据:', commentData);
    
    const res = await addComment(commentData);
    
    if (res.code === 1) {
      uni.showToast({
        title: '评论成功',
        icon: 'success'
      });
      
      if (!replyingTo.value) {
        // 直接评论笔记，添加到顶部
        const newComment = {
          id: res.data ? res.data : new Date().getTime().toString(),
          content: commentContent.value,
          userId: currentUser.value.userId,
          time: new Date().toISOString(),
          userName: currentUser.value.nickName,
          avatar: currentUser.value.avatar || defaultAvatar,
          childrenCount: 0,
          showChildren: false
        };
        
        comments.value.unshift(newComment);
        commentCount.value += 1;
      } else {
        // 回复评论，找到对应的父评论
        const parentId = String(replyParentId.value);
        const parentComment = comments.value.find(c => String(c.id) === parentId);
        
        if (parentComment) {
          // 确保显示子评论
          parentComment.showChildren = true;
          
          // 构建新的回复
          const newReply = {
            id: res.data ? res.data : new Date().getTime().toString(),
            content: commentContent.value,
            parentId: parentId,
            userId: currentUser.value.userId,
            time: new Date().toISOString(),
            userName: currentUser.value.nickName,
            avatar: currentUser.value.avatar || defaultAvatar,
            toUserId: replyToUserId.value,
            replyName: replyToUsername.value
          };
          
          // 如果还没有children数组，创建一个
          if (!parentComment.children) {
            parentComment.children = [];
          }
          
          // 添加新回复到子评论列表
          parentComment.children.unshift(newReply);
          
          // 更新子评论数量
          parentComment.childrenCount = (parentComment.childrenCount || 0) + 1;
        }
      }
      
      // 清空输入框和回复状态
      commentContent.value = '';
      replyingTo.value = null;
      replyParentId.value = null;
      replyToUsername.value = '';
      replyToUserId.value = '';
    } else {
      uni.showToast({
        title: res.message || '评论失败',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('发送评论失败:', error);
    uni.showToast({
      title: '网络错误，请重试',
      icon: 'none'
    });
  } finally {
    uni.hideLoading();
  }
}

// 跳转到用户页面
function goToUser(userId) {
  if (!userId) return;
  
  uni.navigateTo({
    url: `/pages/user/user?id=${userId}`
  });
}

// 格式化时间
function formatTime(timestamp) {
  if (!timestamp) return '';
  
  const date = new Date(timestamp);
  const now = new Date();
  
  const diffMs = now - date;
  const diffSec = Math.floor(diffMs / 1000);
  const diffMin = Math.floor(diffSec / 60);
  const diffHour = Math.floor(diffMin / 60);
  const diffDay = Math.floor(diffHour / 24);
  
  if (diffSec < 60) {
    return '刚刚';
  } else if (diffMin < 60) {
    return `${diffMin}分钟前`;
  } else if (diffHour < 24) {
    return `${diffHour}小时前`;
  } else if (diffDay < 30) {
    return `${diffDay}天前`;
  } else {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }
}

// 格式化图片URL，确保是完整的URL
function formatImageUrl(url) {
  if (!url) return '';
  
  // 如果已经是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }
  
  // 如果是相对路径，添加基础URL
  // 根据环境配置基础URL
  const baseUrl = 'http://localhost:8081'; // 开发环境
  // const baseUrl = 'https://your-production-domain.com'; // 生产环境
  
  // 如果路径已经包含 /image，则直接拼接
  if (url.startsWith('/image')) {
    return baseUrl + url;
  }
  
  // 否则假设它是相对于笔记图片目录的路径
  return `${baseUrl}/image/note/${url}`;
}

// 修改预览图片函数
function previewImage(currentUrl, index) {
  if (isPreview.value && noteData.value.images) {
    uni.previewImage({
      current: currentUrl,
      urls: noteData.value.images
    });
  } else if (noteData.value.noteImages && noteData.value.noteImages.length > 0) {
    const urls = noteData.value.noteImages.map(img => img.imagePath);
    
    uni.previewImage({
      current: currentUrl,
      urls: urls
    });
  } else if (noteData.value.coverImage) {
    const coverImageUrl = formatImageUrl(noteData.value.coverImage);
    uni.previewImage({
      current: coverImageUrl,
      urls: [coverImageUrl]
    });
  }
}

// 处理查看回复
async function handleViewReplies(parentId) {
  const comment = comments.value.find(c => c.id === parentId);
  if (!comment) return;
  
  if (comment.showChildren) {
    // 如果已经显示子评论，则收起
    comment.showChildren = false;
  } else {
    // 显示子评论并加载
    comment.showChildren = true;
    if (!comment.children || comment.children.length === 0) {
      await loadChildComments(parentId);
    }
  }
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
  margin-top: 10rpx;
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

.loading-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
  
  .loading-spinner {
    width: 60rpx;
    height: 60rpx;
    border: 6rpx solid #f3f3f3;
    border-top: 6rpx solid #1a73e8;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 20rpx;
  }
  
  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
}

.author-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background-color: #fff;
  
  &.fixed-top {
    position: sticky;
    top: 0;
    z-index: 100;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  }
  
  .author-info {
    display: flex;
    align-items: center;
    flex: 1;
    
    .author-avatar {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      margin-right: 20rpx;
    }
    
    .author-detail {
      display: flex;
      flex-direction: column;
      
      .author-name {
        font-size: 28rpx;
        font-weight: 500;
        color: #333;
      }
      
      .author-school {
        font-size: 22rpx;
        color: #999;
        margin-top: 6rpx;
      }
    }
  }
  
  .follow-btn {
    min-width: 140rpx;
    height: 60rpx;
    padding: 0 30rpx;
    background: linear-gradient(135deg, #1a73e8 0%, #0d47a1 100%);
    color: white;
    border-radius: 30rpx;
    font-size: 26rpx;
    line-height: 60rpx;
    text-align: center;
    box-shadow: 0 2rpx 8rpx rgba(26, 115, 232, 0.3);
    
    &.following {
      background: #f5f5f5;
      color: #666;
      border: 1rpx solid #ddd;
      box-shadow: none;
    }
  }
}

.interaction-bar {
  display: flex;
  justify-content: space-around;
  padding: 20rpx 0;
  border-top: 1rpx solid #f0f0f0;
  margin-top: 30rpx;
  
  .interaction-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    text {
      font-size: 24rpx;
      color: #999;
      margin-top: 10rpx;
      
      &.active {
        color: #1a73e8;
      }
    }
  }
}

.empty-comments {
  text-align: center;
  padding: 50rpx 0;
  color: #999;
  font-size: 28rpx;
}

.view-more-replies {
  font-size: 24rpx;
  color: #1a73e8;
  padding: 10rpx 0 10rpx 100rpx;
  
  &:active {
    opacity: 0.7;
  }
}

.load-more {
  text-align: center;
  padding: 20rpx 0;
  color: #1a73e8;
  font-size: 26rpx;
  
  &:active {
    opacity: 0.7;
  }
}

.view-replies {
  padding: 10rpx 0 10rpx 90rpx;
  font-size: 24rpx;
  color: #1a73e8;
  
  &:active {
    opacity: 0.7;
  }
}

.load-more-replies {
  text-align: center;
  padding: 10rpx 0;
  font-size: 24rpx;
  color: #1a73e8;
  
  &:active {
    opacity: 0.7;
  }
}

.child-comments {
  margin-left: 90rpx;
  background-color: #f9f9f9;
  border-radius: 12rpx;
  padding: 20rpx;
}
</style> 