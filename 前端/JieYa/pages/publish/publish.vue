<template>
  <view class="container">
    <!-- 图片选择区域 -->
    <scroll-view class="image-scroll" scroll-x="true">
      <view class="image-container">
        <view class="image-item" 
              v-for="(item, index) in filelist" 
              :key="index"
              :data-index="index"
              :class="{'dragging': isDragging && dragIndex === index}"
              :style="getItemStyle(index)"
              @touchstart="dragStart"
              @touchmove="dragMove"
              @touchend="dragEnd">
          <image :src="item" mode="aspectFill"></image>
          <view class="delete-btn" @click.stop="removeImage(index)">×</view>
        </view>
        <view class="upload-btn" v-if="filelist.length < 9" @click="chooseFile">
          <text>+</text>
        </view>
      </view>
    </scroll-view>

    <!-- 标题输入 -->
    <input type="text" v-model="title" class="title" placeholder="请输入标题" />

    <!-- 正文输入 -->
    <textarea v-model="content" 
              class="content" 
              placeholder="请输入正文"
              auto-height></textarea>

    <!-- 话题标签 -->
    <view class="tags-section" style="margin-top: 100rpx;">
      <view class="tags-container">
        <view class="tag-item" v-for="(tag, index) in selectedTags" :key="index">
          #{{tag}}
          <text class="delete-tag" @click="removeTag(index)">×</text>
        </view>
      </view>
      <view class="add-tag-section">
        <input type="text" v-model="newTag" placeholder="输入新话题" @keyup.enter="addTag" />
        <button class="add-tag-btn" 
                @tap="addTag()" 
                :disabled="!newTag" 
                :style="{ background: newTag ? '#1a73e8' : '#eee' }">添加话题</button>
      </view>
    </view>

    <!-- 权限设置 -->
    <view class="permission-section">
      <text class="section-title">可见范围</text>
      <view class="permission-options">
        <view class="permission-option" 
              :class="{ active: visibility === 'public' }"
              @click="setVisibility('public')">
          <text class="option-icon">🌐</text>
          <text class="option-text">所有人可见</text>
        </view>
        <view class="permission-option" 
              :class="{ active: visibility === 'friends' }"
              @click="setVisibility('friends')">
          <text class="option-icon">👥</text>
          <text class="option-text">好友可见</text>
        </view>
        <view class="permission-option" 
              :class="{ active: visibility === 'private' }"
              @click="setVisibility('private')">
          <text class="option-icon">🔒</text>
          <text class="option-text">仅自己可见</text>
        </view>
      </view>
    </view>

    <!-- 底部按钮区域 -->
    <view class="bottom-actions">
      <button class="action-btn draft-btn" @click="saveDraft">存草稿</button>
      <button class="action-btn preview-btn" @click="previewNote">预览</button>
      <button class="action-btn publish-btn" @click="finish" :disabled="!filelist.length">发布</button>
    </view>
  </view>
</template>

<script setup>
import { ref, getCurrentInstance, onMounted } from 'vue';
import { addNote, updateNote, getNoteDetail } from '@/api/note';

const filePath = ref("");
const title = ref("");
const content = ref("");
const filelist = ref([]); // 本地图片文件路径
const imagelist = ref([]); // 上传成功的图片链接
const selectedTags = ref([]);
const newTag = ref('');
const dragIndex = ref(-1);
const dragStartX = ref(0);
const dragCurrentX = ref(0);
const isDragging = ref(false);
const dragElement = ref(null);
const visibility = ref('public'); // 默认所有人可见
const isEdit = ref(false); // 是否为编辑模式
const postId = ref(''); // 笔记ID，编辑模式时使用
const originalImageList = ref([]); // 原始图片列表，编辑模式时使用

onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  const options = currentPage.$page?.options;
  
  if (options && options.postId) {
    // 编辑模式
    postId.value = options.postId;
    isEdit.value = true;
    loadNoteDetail();
  } else {
    // 新增模式
    // 页面加载时检查是否有草稿
    checkDraft();
  }
});

// 加载笔记详情
async function loadNoteDetail() {
  try {
    uni.showLoading({
      title: '加载中...'
    });
    
    const result = await getNoteDetail(postId.value);
    if (result.code === 200) {
      const noteData = result.data;
      title.value = noteData.title;
      content.value = noteData.content;
      selectedTags.value = noteData.tags || [];
      filelist.value = noteData.imageUrls || [];
      originalImageList.value = noteData.images || [];
      visibility.value = noteData.visibility || 'public';
    } else {
      uni.showToast({
        title: '获取笔记失败',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('获取笔记详情失败:', error);
    uni.showToast({
      title: '获取笔记失败',
      icon: 'none'
    });
  } finally {
    uni.hideLoading();
  }
}

function checkDraft() {
  try {
    const draftData = uni.getStorageSync('noteDraft');
    if (draftData) {
      // 显示提示框
      uni.showModal({
        title: '发现草稿',
        content: '是否继续编辑上次的草稿？',
        confirmText: '继续编辑',
        cancelText: '新建笔记',
        success: (res) => {
          if (res.confirm) {
            // 用户点击继续编辑，加载草稿数据
            loadDraft();
            uni.showToast({
              title: '已加载草稿',
              icon: 'success',
              duration: 2000
            });
          } else {
            // 用户点击新建笔记，清除草稿数据
            uni.removeStorageSync('noteDraft');
            // 重置表单
            resetForm();
            uni.showToast({
              title: '已创建新笔记',
              icon: 'success',
              duration: 2000
            });
          }
        }
      });
    }
  } catch (e) {
    console.error('检查草稿失败:', e);
  }
}

function resetForm() {
  title.value = "";
  content.value = "";
  selectedTags.value = [];
  filelist.value = [];
  imagelist.value = [];
  visibility.value = 'public';
}

function loadDraft() {
  try {
    const draftData = uni.getStorageSync('noteDraft');
    if (draftData) {
      title.value = draftData.title || '';
      content.value = draftData.content || '';
      selectedTags.value = draftData.tags || [];
      filelist.value = draftData.images || [];
      visibility.value = draftData.visibility || 'public';
    }
  } catch (e) {
    console.error('加载草稿失败:', e);
  }
}

function saveDraft() {
  try {
    const draftData = {
      title: title.value,
      content: content.value,
      tags: selectedTags.value,
      images: filelist.value,
      visibility: visibility.value,
      timestamp: Date.now()
    };
    
    uni.setStorageSync('noteDraft', draftData);
    
    uni.showToast({
      title: '草稿保存成功',
      icon: 'success',
      duration: 2000
    });
  } catch (e) {
    console.error('保存草稿失败:', e);
    uni.showToast({
      title: '草稿保存失败',
      icon: 'none',
      duration: 2000
    });
  }
}

function previewNote() {
  // 保存当前内容到临时预览数据
  try {
    const previewData = {
      title: title.value,
      content: content.value,
      tags: selectedTags.value,
      images: filelist.value,
      visibility: visibility.value,
      isPreview: true // 标记为预览模式
    };
    
    uni.setStorageSync('notePreview', previewData);
    
    // 跳转到note页面
    uni.navigateTo({
      url: '/pages/note/note'
    });
  } catch (e) {
    console.error('预览失败:', e);
    uni.showToast({
      title: '预览失败',
      icon: 'none',
      duration: 2000
    });
  }
}

function chooseFile() {
  uni.chooseImage({
    count: 9 - filelist.value.length,
    success: (res) => {
      res.tempFilePaths.forEach(filePath => {
        filelist.value.push(filePath);
      });
    },
    fail: (err) => {
      console.log("选择文件失败: ", err);
    },
  });
}

function removeImage(index) {
  filelist.value.splice(index, 1);
  if (imagelist.value.length > index) {
    imagelist.value.splice(index, 1);
  }
}

function addTag() {
  if (newTag.value) {
    if (!selectedTags.value.includes(newTag.value)) {
      selectedTags.value.push(newTag.value);
      newTag.value = '';
    } else {
      uni.showToast({
        title: '该话题已添加',
        icon: 'none'
      });
    }
  }
}

function removeTag(index) {
  selectedTags.value.splice(index, 1);
}

function getItemStyle(index) {
  if (!isDragging.value || dragIndex.value !== index) {
    return {};
  }
  
  const moveX = dragCurrentX.value - dragStartX.value;
  return {
    transform: `translateX(${moveX}px) scale(1.05)`,
    zIndex: '10',
    transition: 'transform 0.05s ease'
  };
}

function dragStart(e) {
  dragIndex.value = e.currentTarget.dataset.index;
  dragStartX.value = e.touches[0].clientX;
  dragCurrentX.value = dragStartX.value;
  isDragging.value = true;
  
  dragElement.value = e.currentTarget;
}

function dragMove(e) {
  if (dragIndex.value < 0 || !isDragging.value) return;
  
  dragCurrentX.value = e.touches[0].clientX;
  const moveDistance = dragCurrentX.value - dragStartX.value;
  
  const itemWidth = 220;
  const moveItems = Math.round(moveDistance / itemWidth);
  const targetIndex = Math.max(0, Math.min(filelist.value.length - 1, dragIndex.value + moveItems));
  
  if (targetIndex !== dragIndex.value) {
    const temp = filelist.value[dragIndex.value];
    filelist.value.splice(dragIndex.value, 1);
    filelist.value.splice(targetIndex, 0, temp);
    
    dragIndex.value = targetIndex;
    dragStartX.value = dragCurrentX.value;
  }
}

function dragEnd(e) {
  if (!isDragging.value) return;
  
  isDragging.value = false;
  dragIndex.value = -1;
  dragElement.value = null;
}

function goBack() {
  uni.navigateBack(); // 返回上一页
}

function setVisibility(type) {
  visibility.value = type;
}

// 完成发布
async function finish() {
  if (!title.value) {
    uni.showToast({
      title: '请输入标题',
      icon: 'none'
    });
    return;
  }
  
  if (!content.value) {
    uni.showToast({
      title: '请输入内容',
      icon: 'none'
    });
    return;
  }
  
  if (filelist.value.length === 0) {
    uni.showToast({
      title: '请至少添加一张图片',
      icon: 'none'
    });
    return;
  }
  
  // 显示加载提示
  uni.showLoading({
    title: isEdit.value ? '更新中...' : '发布中...'
  });
  
  try {
    const noteData = {
      title: title.value,
      content: content.value,
      tags: selectedTags.value,
      visibility: visibility.value
    };
    
    let result;
    if (isEdit.value) {
      // 编辑模式，找出新上传的本地图片和保留的原始图片
      const newLocalImages = filelist.value.filter(item => item.startsWith('file://') || item.startsWith('/'));
      result = await updateNote(postId.value, noteData, newLocalImages, originalImageList.value);
    } else {
      // 新增模式
      result = await addNote(noteData, filelist.value);
    }
    
    uni.hideLoading();
    if (result.code === 200) {
      uni.showToast({
        title: isEdit.value ? '更新成功' : '发布成功',
        icon: 'success'
      });
      
      // 清空本地草稿
      uni.removeStorageSync('noteDraft');
      
      // 延迟跳转，让用户看到成功提示
      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
    } else {
      uni.showToast({
        title: result.msg || '操作失败',
        icon: 'none'
      });
    }
  } catch (error) {
    uni.hideLoading();
    uni.showToast({
      title: '操作失败: ' + error.message,
      icon: 'none'
    });
    console.error(isEdit.value ? '更新笔记失败:' : '发布笔记失败:', error);
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.image-scroll {
  width: 100%;
  margin-bottom: 20rpx;
}

.image-container {
  display: flex;
  padding: 10rpx;
  gap: 20rpx;
}

.image-item {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  flex-shrink: 0;
  transition: all 0.3s ease;
  touch-action: none;
  
  &.dragging {
    opacity: 0.8;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.2);
  }
  
  image {
    width: 100%;
    height: 100%;
    border-radius: 12rpx;
  }
  
  .delete-btn {
    position: absolute;
    top: -10rpx;
    right: -10rpx;
    width: 40rpx;
    height: 40rpx;
    background: rgba(0,0,0,0.5);
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.upload-btn {
  width: 200rpx;
  height: 200rpx;
  border: 2rpx dashed #ddd;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60rpx;
  color: #999;
  flex-shrink: 0;
}

.title, .content {
  width: 90%;
  margin: 10rpx 0;
  border: none;
}
.title{
  font-size: 36rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #ccc;
}
.content{
  font-size: 26rpx;
  min-height: 100rpx;
  max-height: 800rpx;
  overflow-y: auto;
  resize: none;
}

.tags-section {
  width: 100%;
  margin-top: 100rpx;
  
  .tags-container {
    display: flex;
    flex-wrap: wrap;
    gap: 10rpx;
    margin-bottom: 10rpx;
  }
  
  .tag-item {
    background: #e0f7fa;
    padding: 8rpx 20rpx;
    border-radius: 30rpx;
    font-size: 26rpx;
    color: #ccc;
    display: flex;
    align-items: center;
    margin-bottom: 10rpx;
    
    .delete-tag {
      margin-left: 10rpx;
      color: #999;
    }
  }
  
  .add-tag-section{
    width: 100%;
    display: flex;
    align-items: center;
    
    input {
      flex: 1;
      height: 70rpx;
      padding: 0 20rpx;
      border: none;
      background-color: #f5f5f5;
      border-radius: 8rpx;
      margin-right: 20rpx;
    }
  }
  
  .add-tag-btn {
    background: #eee;
    color: white;
    padding: 8rpx 20rpx;
    border-radius: 30rpx;
    font-size: 26rpx;
    border: none;
    cursor: pointer;
    min-width: 160rpx;
    text-align: center;
    
    &:disabled {
      background: #ccc;
    }
  }
}

.permission-section {
  width: 100%;
  margin-top: 40rpx;
  
  .section-title {
    font-size: 28rpx;
    color: #333;
    margin-bottom: 20rpx;
    display: block;
  }
  
  .permission-options {
    display: flex;
    justify-content: space-between;
    gap: 20rpx;
  }
  
  .permission-option {
    flex: 1;
    padding: 20rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    
    &.active {
      background: #e3f2fd;
      border: 1px solid #1a73e8;
    }
    
    .option-icon {
      font-size: 40rpx;
      margin-bottom: 10rpx;
    }
    
    .option-text {
      font-size: 24rpx;
      color: #333;
    }
  }
}

.bottom-actions {
  width: 100%;
  display: flex;
  justify-content: space-between;
  margin-top: 40rpx;
  padding: 20rpx 0;
  position: sticky;
  bottom: 0;
  background-color: white;
}

.action-btn {
  flex: 1;
  margin: 0 10rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 40rpx;
  font-size: 28rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.draft-btn {
  background-color: #f5f5f5;
  color: #666;
}

.preview-btn {
  background-color: #e3f2fd;
  color: #1a73e8;
}

.publish-btn {
  background: linear-gradient(135deg, #1a73e8 0%, #0d47a1 100%);
  color: white;
  font-weight: 500;
  
  &:disabled {
    background: #ccc;
    color: #fff;
  }
}

.publish {
  display: none;
}
</style>
