<template>
	<view class="container">
		<!-- 用户头部信息 -->
		<view class="user">
			<view class="header">
				<image :src="userInfo.avatar ? `http://localhost:8081/images/avatar/${userInfo.avatar}` : 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg'" mode="aspectFill"></image>
				<view class="text">
					<view class="title">{{ userInfo.nickName || '未设置昵称' }}</view>
					<view class="id">ID: {{ userInfo.userId || '未知' }}</view>
				</view>
			</view>
			
			<!-- 用户数据统计 -->
			<view class="userbox">
				<view class="myfans">
					<text class="number">{{ userInfo.fansCount || 0 }}</text>
					<text class="label">粉丝</text>
				</view>
				<view class="mygood">
					<text class="number">{{ userInfo.likeCount || 0 }}</text>
					<text class="label">点赞</text>
				</view>
				<view class="myfocus">
					<text class="number">{{ userInfo.followCount || 0 }}</text>
					<text class="label">关注</text>
				</view>
				<!-- 如果是当前用户，显示设置按钮，否则显示关注按钮 -->
				<view v-if="isCurrentUser">
					<uni-icons type="settings" size="50rpx" @click="goToUpdateMe"></uni-icons>
				</view>
				<view v-else>
					<button class="follow-btn" @click="handleFollow">{{ isFollowing ? '已关注' : '关注' }}</button>
				</view>
			</view>
			
			<!-- 个人简介 -->
			<view class="introduction">
				{{ userInfo.personIntruduction || '这个人很懒，什么都没留下...' }}
			</view>
			
			<!-- 性别和学校信息 -->
			<view class="user-info" v-if="userInfo.sex !== undefined || userInfo.school">
				<view class="info-item" v-if="userInfo.sex !== undefined">
					<uni-icons type="person" size="30rpx" color="#666"></uni-icons>
					<text>{{ userInfo.sex ? '男' : '女' }}</text>
				</view>
				<view class="info-item" v-if="userInfo.school">
					<uni-icons type="education" size="30rpx" color="#666"></uni-icons>
					<text>{{ userInfo.school }}</text>
				</view>
			</view>
		</view>
		
		<!-- 用户内容分类 -->
		<view class="content-tabs">
			<view class="tab" :class="{ active: activeTab === 'notes' }" @click="switchTab('notes')">
				笔记
			</view>
			<view class="tab" :class="{ active: activeTab === 'likes' }" @click="switchTab('likes')">
				喜欢
			</view>
			<view class="tab" :class="{ active: activeTab === 'collections' }" @click="switchTab('collections')">
				收藏
			</view>
		</view>
		
		<!-- 内容列表 -->
		<view class="content-list">
			<template v-if="activeTab === 'notes'">
				<view v-if="loading" class="loading-tip">
					<uni-icons type="spinner-cycle" size="30" color="#007AFF"></uni-icons>
					<text>加载中...</text>
				</view>
				<view v-else-if="noteList.length === 0" class="empty-tip">
					暂无笔记内容
				</view>
				<view v-else class="note-grid">
					<view v-for="(note, index) in noteList" :key="index" class="note-item" @click="goToNote(note.id)">
						<image :src="note.coverImage" mode="aspectFill"></image>
						<view class="note-title">{{ note.title }}</view>
					</view>
				</view>
			</template>
			
			<template v-else-if="activeTab === 'likes'">
				<view v-if="loading" class="loading-tip">
					<uni-icons type="spinner-cycle" size="30" color="#007AFF"></uni-icons>
					<text>加载中...</text>
				</view>
				<view v-else-if="likedList.length === 0" class="empty-tip">
					暂无喜欢的内容
				</view>
				<view v-else class="note-grid">
					<view v-for="(note, index) in likedList" :key="index" class="note-item" @click="goToNote(note.id)">
						<image :src="note.coverImage" mode="aspectFill"></image>
						<view class="note-title">{{ note.title }}</view>
					</view>
				</view>
			</template>
			
			<template v-else>
				<view v-if="loading" class="loading-tip">
					<uni-icons type="spinner-cycle" size="30" color="#007AFF"></uni-icons>
					<text>加载中...</text>
				</view>
				<view v-else-if="collectionList.length === 0" class="empty-tip">
					暂无收藏内容
				</view>
				<view v-else class="note-grid">
					<view v-for="(note, index) in collectionList" :key="index" class="note-item" @click="goToNote(note.id)">
						<image :src="note.coverImage" mode="aspectFill"></image>
						<view class="note-title">{{ note.title }}</view>
					</view>
				</view>
			</template>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getDetail } from '@/api/user';
import { getUserNotes, getCollectList } from '@/api/note';
import { addFocus, deleteFocus, checkFocusStatus, getFocusList, getFansList } from '@/api/focus';

// 页面数据
const userInfo = ref({}); // 用户信息
const isFollowing = ref(false); // 是否已关注
const activeTab = ref('notes'); // 激活的标签
const noteList = ref([]); // 笔记列表
const likedList = ref([]); // 喜欢的笔记列表
const collectionList = ref([]); // 收藏的笔记列表
const loading = ref(false); // 加载状态

// 从URL获取用户ID，如果没有则使用默认测试ID
const userId = ref('OsPcvnikSK'); // 默认测试ID

// 判断是否是当前登录用户
const isCurrentUser = computed(() => {
	const currentUser = uni.getStorageSync('userInfo');
	return currentUser && currentUser.userId === userInfo.value.userId;
});

// 获取当前登录用户ID
const currentUserId = computed(() => {
	const userInfo = uni.getStorageSync('userInfo');
	return userInfo ? userInfo.userId : '';
});

// 获取URL参数
function getUrlParams() {
	const pages = getCurrentPages();
	const currentPage = pages[pages.length - 1];
	const options = currentPage.options || {};
	return options;
}

// 页面加载时执行
onLoad((options) => {
	if (options && options.id) {
		userId.value = options.id;
	}
	
	fetchUserDetail();
	checkUserFollowStatus();
});

// 获取用户详细信息
async function fetchUserDetail() {
	try {
		loading.value = true;
		uni.showLoading({
			title: '加载中...'
		});
		
		console.log('获取用户详情:', userId.value);
		const res = await getDetail(userId.value);
		
		if (res.code === 1 && res.data) {
			userInfo.value = res.data;
			console.log('用户详情:', userInfo.value);
			
			// 加载用户的笔记
			fetchUserNotes();
			// 加载用户的关注和粉丝数据
			fetchUserFocusList();
			fetchUserFansList();
		} else {
			uni.showToast({
				title: '获取用户信息失败',
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('获取用户详情失败:', error);
		uni.showToast({
			title: '网络错误，请重试',
			icon: 'none'
		});
	} finally {
		uni.hideLoading();
		loading.value = false;
	}
}

// 获取用户笔记
async function fetchUserNotes() {
	try {
		loading.value = true;
		
		// 实际API调用
		const res = await getUserNotes(userId.value);
		if (res.code === 1) {
			noteList.value = res.data || [];
		} else {
			// 临时使用示例数据
			noteList.value = [
				{
					id: 1,
					title: '笔记1',
					coverImage: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg'
				},
				{
					id: 2,
					title: '笔记2',
					coverImage: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg'
				}
			];
		}
	} catch (error) {
		console.error('获取用户笔记错误:', error);
		// 使用示例数据作为后备
		noteList.value = [
			{
				id: 1,
				title: '示例笔记1',
				coverImage: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg'
			},
			{
				id: 2,
				title: '示例笔记2',
				coverImage: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg'
			}
		];
	} finally {
		loading.value = false;
	}
}

// 获取用户喜欢的笔记
async function fetchUserLikes() {
	try {
		loading.value = true;
		
		// 这里应该调用获取用户喜欢笔记的API
		// 暂时使用示例数据
		likedList.value = [
			{
				id: 3,
				title: '喜欢的笔记1',
				coverImage: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg'
			},
			{
				id: 4,
				title: '喜欢的笔记2',
				coverImage: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg'
			}
		];
		
		// 延迟模拟网络请求
		await new Promise(resolve => setTimeout(resolve, 500));
	} catch (error) {
		console.error('获取喜欢笔记错误:', error);
		likedList.value = [];
	} finally {
		loading.value = false;
	}
}

// 获取用户收藏的笔记
async function fetchUserCollections() {
	try {
		loading.value = true;
		
		// 获取用户收藏列表
		const res = await getCollectList(userId.value);
		
		if (res.code === 1) {
			collectionList.value = res.data || [];
		} else {
			// 临时使用示例数据
			collectionList.value = [
				{
					id: 5,
					title: '收藏的笔记1',
					coverImage: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg'
				},
				{
					id: 6,
					title: '收藏的笔记2',
					coverImage: 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg'
				}
			];
		}
	} catch (error) {
		console.error('获取收藏笔记错误:', error);
		collectionList.value = [];
	} finally {
		loading.value = false;
	}
}

// 获取用户关注列表
async function fetchUserFocusList() {
	try {
		loading.value = true;
		const res = await getFocusList(userId.value);
		
		if (res.code === 1) {
			userInfo.value.followCount = res.data.length || 0;
		}
	} catch (error) {
		console.error('获取关注列表错误:', error);
	} finally {
		loading.value = false;
	}
}

// 获取用户粉丝列表
async function fetchUserFansList() {
	try {
		loading.value = true;
		const res = await getFansList(userId.value);
		
		if (res.code === 1) {
			userInfo.value.fansCount = res.data.length || 0;
		}
	} catch (error) {
		console.error('获取粉丝列表错误:', error);
	} finally {
		loading.value = false;
	}
}

// 检查是否已关注该用户
async function checkUserFollowStatus() {
	if (isCurrentUser.value) return;
	
	try {
		// 如果已登录，检查关注状态
		if (currentUserId.value) {
			const res = await checkFocusStatus(currentUserId.value, userId.value);
			if (res.code === 1) {
				isFollowing.value = res.data;
			}
		}
	} catch (error) {
		console.error('检查关注状态错误:', error);
	}
}

// 关注/取消关注用户
async function handleFollow() {
	// 检查登录状态
	if (!currentUserId.value) {
		uni.showToast({
			title: '请先登录',
			icon: 'none'
		});
		return;
	}
	
	// 验证是否是用户自己
	if (currentUserId.value === userId.value) {
		uni.showToast({
			title: '不能关注自己',
			icon: 'none'
		});
		return;
	}
	
	try {
		uni.showLoading({
			title: isFollowing.value ? '取消关注中...' : '关注中...'
		});
		
		// 调用关注/取消关注API
		let res;
		if (isFollowing.value) {
			res = await deleteFocus(currentUserId.value, userId.value);
		} else {
			res = await addFocus(currentUserId.value, userId.value);
		}
		
		if (res.code === 1) {
			isFollowing.value = !isFollowing.value;
			uni.showToast({
				title: isFollowing.value ? '已关注' : '已取消关注',
				icon: 'none'
			});
			
			// 更新用户信息中的粉丝数
			if (isFollowing.value) {
				userInfo.value.fansCount = (userInfo.value.fansCount || 0) + 1;
			} else if (userInfo.value.fansCount > 0) {
				userInfo.value.fansCount -= 1;
			}
		} else {
			uni.showToast({
				title: res.message || '操作失败',
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('关注操作错误:', error);
		uni.showToast({
			title: '网络错误，请重试',
			icon: 'none'
		});
	} finally {
		uni.hideLoading();
	}
}

// 切换标签
async function switchTab(tab) {
	if (activeTab.value === tab) return;
	
	activeTab.value = tab;
	
	if (tab === 'notes') {
		await fetchUserNotes();
	} else if (tab === 'likes') {
		await fetchUserLikes();
	} else if (tab === 'collections') {
		await fetchUserCollections();
	}
}

// 跳转到编辑个人资料
function goToUpdateMe() {
	uni.navigateTo({
		url: '/pages/updateMe/updateMe'
	});
}

// 跳转到笔记详情
function goToNote(noteId) {
	uni.navigateTo({
		url: `/pages/note/note?id=${noteId}`
	});
}
</script>

<style lang="scss" scoped>
.container {
	background-color: #f6f7f9;
	min-height: 100vh;
}

.user {
	background-color: #fff;
	padding: 30rpx;
	margin-bottom: 20rpx;
	
	.header {
		display: flex;
		align-items: center;
		
		image {
			width: 120rpx;
			height: 120rpx;
			border-radius: 50%;
			margin-right: 20rpx;
			border: 2rpx solid #eee;
		}
		
		.text {
			flex: 1;
			
			.title {
				font-size: 36rpx;
				font-weight: bold;
				color: #333;
				margin-bottom: 10rpx;
			}
			
			.id {
				font-size: 24rpx;
				color: #999;
			}
		}
	}
	
	.userbox {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-top: 30rpx;
		padding: 20rpx 0;
		border-bottom: 1rpx solid #f1f1f1;
		
		.myfans, .mygood, .myfocus {
			display: flex;
			flex-direction: column;
			align-items: center;
			
			.number {
				font-size: 32rpx;
				font-weight: bold;
				color: #333;
			}
			
			.label {
				font-size: 24rpx;
				color: #999;
				margin-top: 6rpx;
			}
		}
		
		.follow-btn {
			background-color: #1a73e8;
			color: #fff;
			font-size: 26rpx;
			padding: 10rpx 30rpx;
			border-radius: 30rpx;
			border: none;
			line-height: 1.5;
			
			&::after {
				border: none;
			}
			
			&.following {
				background-color: #f1f1f1;
				color: #666;
			}
		}
	}
	
	.introduction {
		padding: 20rpx 0;
		font-size: 28rpx;
		color: #666;
		line-height: 1.5;
	}
	
	.user-info {
		display: flex;
		flex-wrap: wrap;
		
		.info-item {
			display: flex;
			align-items: center;
			margin-right: 30rpx;
			margin-top: 10rpx;
			
			text {
				font-size: 26rpx;
				color: #666;
				margin-left: 8rpx;
			}
		}
	}
}

.content-tabs {
	display: flex;
	background-color: #fff;
	border-bottom: 1rpx solid #f1f1f1;
	
	.tab {
		flex: 1;
		text-align: center;
		padding: 20rpx 0;
		font-size: 30rpx;
		color: #666;
		position: relative;
		
		&.active {
			color: #1a73e8;
			font-weight: bold;
			
			&::after {
				content: '';
				position: absolute;
				bottom: 0;
				left: 50%;
				transform: translateX(-50%);
				width: 60rpx;
				height: 6rpx;
				background-color: #1a73e8;
				border-radius: 3rpx;
			}
		}
	}
}

.content-list {
	padding: 20rpx;
	min-height: 300rpx;
	
	.loading-tip {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 60rpx 0;
		color: #999;
		font-size: 28rpx;
		
		text {
			margin-top: 20rpx;
		}
	}
	
	.empty-tip {
		text-align: center;
		color: #999;
		font-size: 28rpx;
		padding: 100rpx 0;
	}
	
	.note-grid {
		display: flex;
		flex-wrap: wrap;
		
		.note-item {
			width: calc(50% - 20rpx);
			margin: 10rpx;
			background-color: #fff;
			border-radius: 10rpx;
			overflow: hidden;
			box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
			
			image {
				width: 100%;
				height: 300rpx;
			}
			
			.note-title {
				padding: 15rpx;
				font-size: 26rpx;
				color: #333;
				overflow: hidden;
				text-overflow: ellipsis;
				white-space: nowrap;
			}
		}
	}
}
</style>
