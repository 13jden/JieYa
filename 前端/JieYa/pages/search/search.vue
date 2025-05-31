<template>
  <view class="search-container">
    <!-- 搜索栏 -->
    <view class="search-header">
      <view class="search-box">
        <image src="/static/search.png" class="search-icon" mode="aspectFit"></image>
        <input 
          class="search-input" 
          type="text" 
          v-model="keyword" 
          placeholder="搜索笔记和用户" 
          focus
          confirm-type="search"
          @confirm="handleSearch"
        />
        <view class="clear-icon" v-if="keyword" @tap="clearKeyword">
          <uni-icons type="close" size="18" color="#999"></uni-icons>
        </view>
      </view>
      <view class="cancel-btn" @tap="goBack">取消</view>
    </view>

    <!-- 搜索前显示搜索历史 -->
    <view class="search-history" v-if="!showResults">
      <view class="history-header">
        <text class="history-title">搜索历史</text>
        <view class="clear-history" @tap="clearHistory">
          <uni-icons type="trash" size="18" color="#666"></uni-icons>
        </view>
      </view>
      <view class="history-list">
        <view 
          class="history-item" 
          v-for="(item, index) in searchHistory" 
          :key="index"
          @tap="useHistoryKeyword(item)"
        >
          <text>{{ item }}</text>
        </view>
      </view>
    </view>

    <!-- 搜索后显示结果 -->
    <view class="search-results" v-if="showResults">
      <!-- 标签页 -->
      <view class="tabs">
        <view 
          class="tab-item" 
          :class="{ active: activeTab === 'notes' }" 
          @tap="switchTab('notes')"
        >
          笔记
        </view>
        <view 
          class="tab-item" 
          :class="{ active: activeTab === 'users' }" 
          @tap="switchTab('users')"
        >
          用户
        </view>
      </view>

      <!-- 笔记结果 - 使用与find页面相同的瀑布流布局 -->
      <scroll-view 
        class="results-list" 
        scroll-y="true" 
        v-if="activeTab === 'notes'"
        @scrolltolower="loadMoreNotes"
      >
        <view class="no-results" v-if="noteList.length === 0">
          <text>没有找到相关笔记</text>
        </view>
        <view class="masonry" v-else>
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
        <view class="loading" v-if="notesLoading && noteList.length > 0">加载中...</view>
        <view class="no-more" v-if="!notesHasMore && noteList.length > 0">没有更多了</view>
      </scroll-view>

      <!-- 用户结果 -->
      <scroll-view 
        class="results-list" 
        scroll-y="true" 
        v-if="activeTab === 'users'"
      >
        <view class="no-results" v-if="userList.length === 0">
          <text>没有找到相关用户</text>
        </view>
        <view class="user-list">
          <view class="user-item" v-for="(user, index) in userList" :key="index" @tap="goToUserProfile(user.id)">
            <image :src="user.avatar || '/static/default-avatar.png'" class="user-avatar" mode="aspectFill"></image>
            <view class="user-info">
              <view class="user-name">{{ user.nickName }}</view>
              <view class="user-intro">{{ user.personIntruduction || '这个用户很懒，什么都没留下' }}</view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { searchNotes } from '@/api/note';
import { searchUsers } from '@/api/user';

// 搜索相关数据
const keyword = ref('');
const searchHistory = ref([]);
const showResults = ref(false);
const activeTab = ref('notes');

// 笔记列表数据
const noteList = ref([]);
const notesCurrentPage = ref(1);
const notesPageSize = ref(10);
const notesLoading = ref(false);
const notesHasMore = ref(true);

// 用户列表数据
const userList = ref([]);

// 初始化时加载搜索历史
onMounted(() => {
  loadSearchHistory();
});

// 加载搜索历史
function loadSearchHistory() {
  try {
    const history = uni.getStorageSync('searchHistory');
    if (history) {
      searchHistory.value = JSON.parse(history);
    }
  } catch (e) {
    console.error('加载搜索历史失败:', e);
    searchHistory.value = [];
  }
}

// 保存搜索历史
function saveSearchHistory(keyword) {
  try {
    // 先从数组中移除相同的关键词（如果存在）
    const index = searchHistory.value.indexOf(keyword);
    if (index !== -1) {
      searchHistory.value.splice(index, 1);
    }
    
    // 将新关键词添加到数组开头
    searchHistory.value.unshift(keyword);
    
    // 只保留最近的10条搜索记录
    if (searchHistory.value.length > 10) {
      searchHistory.value = searchHistory.value.slice(0, 10);
    }
    
    // 保存到本地存储
    uni.setStorageSync('searchHistory', JSON.stringify(searchHistory.value));
  } catch (e) {
    console.error('保存搜索历史失败:', e);
  }
}

// 清空搜索历史
function clearHistory() {
  uni.showModal({
    title: '提示',
    content: '确定要清空搜索历史吗？',
    success: function(res) {
      if (res.confirm) {
        searchHistory.value = [];
        uni.removeStorageSync('searchHistory');
      }
    }
  });
}

// 使用历史关键词
function useHistoryKeyword(historyKeyword) {
  keyword.value = historyKeyword;
  handleSearch();
}

// 清除关键词
function clearKeyword() {
  keyword.value = '';
  if (showResults.value) {
    showResults.value = false;
  }
}

// 返回上一页
function goBack() {
  uni.navigateBack();
}

// 处理搜索
function handleSearch() {
  if (!keyword.value.trim()) {
    uni.showToast({
      title: '请输入搜索内容',
      icon: 'none'
    });
    return;
  }
  
  // 保存搜索历史
  saveSearchHistory(keyword.value.trim());
  
  // 重置搜索状态
  showResults.value = true;
  noteList.value = [];
  userList.value = [];
  notesCurrentPage.value = 1;
  notesHasMore.value = true;
  
  // 加载搜索结果
  loadSearchResults();
}

// 加载搜索结果
function loadSearchResults() {
  if (activeTab.value === 'notes') {
    loadNoteResults();
  } else {
    loadUserResults();
  }
}

// 加载笔记搜索结果
async function loadNoteResults() {
  if (notesLoading.value || !notesHasMore.value) return;
  
  try {
    notesLoading.value = true;
    
    const res = await searchNotes(
      keyword.value.trim(),
      notesCurrentPage.value,
      notesPageSize.value
    );
    
    if (res.code === 1) {
      const newNotes = res.data.records || [];
      
      if (notesCurrentPage.value === 1) {
        noteList.value = newNotes;
      } else {
        noteList.value = [...noteList.value, ...newNotes];
      }
      
      // 判断是否还有更多数据
      notesHasMore.value = newNotes.length === notesPageSize.value;
    } else {
      uni.showToast({
        title: res.message || '搜索失败',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('搜索笔记失败:', error);
    uni.showToast({
      title: '网络错误，请重试',
      icon: 'none'
    });
  } finally {
    notesLoading.value = false;
  }
}

// 加载更多笔记
function loadMoreNotes() {
  if (notesHasMore.value && !notesLoading.value) {
    notesCurrentPage.value++;
    loadNoteResults();
  }
}

// 加载用户搜索结果
async function loadUserResults() {
  try {
    const res = await searchUsers(keyword.value.trim());
    
    if (res.code === 1) {
      userList.value = res.data || [];
    } else {
      uni.showToast({
        title: res.message || '搜索失败',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('搜索用户失败:', error);
    uni.showToast({
      title: '网络错误，请重试',
      icon: 'none'
    });
  }
}

// 切换标签页
function switchTab(tab) {
  if (activeTab.value === tab) return;
  
  activeTab.value = tab;
  
  if (tab === 'users' && userList.value.length === 0) {
    loadUserResults();
  }
}

// 跳转到笔记详情
function goToNote(noteId) {
  uni.navigateTo({
    url: `/pages/note/note?id=${noteId}`
  });
}

// 跳转到用户主页
function goToUserProfile(userId) {
  uni.navigateTo({
    url: `/pages/user/profile?id=${userId}`
  });
}
</script>

<style lang="scss" scoped>
.search-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f8f9fa;
}

.search-header {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background-color: #fff;
  border-bottom: 2rpx solid #eee;
  position: sticky;
  top: 0;
  z-index: 100;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  background-color: #f2f2f2;
  border-radius: 36rpx;
  padding: 0 20rpx;
  height: 72rpx;
}

.search-icon {
  width: 40rpx;
  height: 40rpx;
  margin-right: 10rpx;
}

.search-input {
  flex: 1;
  height: 72rpx;
  font-size: 28rpx;
}

.clear-icon {
  padding: 10rpx;
}

.cancel-btn {
  font-size: 28rpx;
  color: #1a73e8;
  margin-left: 20rpx;
  padding: 10rpx;
}

/* 搜索历史 */
.search-history {
  background-color: #fff;
  padding: 30rpx;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.history-title {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.clear-history {
  padding: 10rpx;
}

.history-list {
  display: flex;
  flex-wrap: wrap;
}

.history-item {
  background-color: #f5f5f5;
  padding: 10rpx 30rpx;
  border-radius: 30rpx;
  margin-right: 20rpx;
  margin-bottom: 20rpx;
  font-size: 24rpx;
  color: #666;
}

/* 搜索结果 */
.search-results {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.tabs {
  display: flex;
  background-color: #fff;
  border-bottom: 2rpx solid #eee;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  font-size: 28rpx;
  color: #666;
  position: relative;
}

.tab-item.active {
  color: #1a73e8;
  font-weight: 500;
}

.tab-item.active:after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 6rpx;
  background-color: #1a73e8;
  border-radius: 3rpx;
}

.results-list {
  flex: 1;
  background-color: #f8f9fa;
}

.no-results {
  padding: 100rpx 0;
  text-align: center;
  color: #999;
  font-size: 28rpx;
}

/* 笔记瀑布流样式 - 与find页面保持一致 */
.masonry {
  column-count: 2;
  column-gap: 2rpx;
  padding: 0rpx;
  background-color: #f8f9fa;
  min-height: calc(100vh - 300rpx);
}

.box {
  break-inside: avoid;
  margin-bottom: 5rpx;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.98);
  }
}

/* 用户列表 */
.user-list {
  padding: 20rpx 30rpx;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 2rpx solid #eee;
}

.user-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  margin-right: 20rpx;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 10rpx;
}

.user-intro {
  font-size: 26rpx;
  color: #999;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
}

/* 加载状态 */
.loading, .no-more {
  text-align: center;
  padding: 20rpx;
  font-size: 26rpx;
  color: #999;
}
</style>

<script>
export default {
  navigationBarTitleText: '搜索'
}
</script> 