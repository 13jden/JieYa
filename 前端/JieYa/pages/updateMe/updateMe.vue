<template>
	<view class="container">
		<!-- 头像上传区域 -->
		<view class="avatar-container" @tap="chooseAvatar">
			<image 
				:src="avatarUrl || 'https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg'" 
				mode="aspectFill" 
				class="avatar-image"
			></image>
			<view class="avatar-overlay">
				<uni-icons type="camera-filled" size="40rpx" color="#fff"></uni-icons>
				<text>点击更换头像</text>
			</view>
		</view>
		
		<view class="title">个人资料修改</view>
		
		<view class="form-item">
			<text class="label">昵称</text>
			<input class="input" type="text" v-model="userForm.nickName" placeholder="请输入昵称" />
		</view>
		
		<view class="form-item">
			<text class="label">性别</text>
			<view class="radio-group">
				<view class="radio" @tap="userForm.sex = true" :class="{ active: userForm.sex === true }">
					<text>男</text>
				</view>
				<view class="radio" @tap="userForm.sex = false" :class="{ active: userForm.sex === false }">
					<text>女</text>
				</view>
			</view>
		</view>
		
		<view class="form-item">
			<text class="label">生日</text>
			<picker class="picker" mode="date" :value="userForm.birthday" @change="onBirthdayChange">
				<view class="picker-text">{{ userForm.birthday || '请选择出生日期' }}</view>
			</picker>
		</view>
		
		<view class="form-item">
			<text class="label">学校</text>
			<input class="input" type="text" v-model="userForm.school" placeholder="请输入学校" />
		</view>
		
		<view class="form-item">
			<text class="label">个人介绍</text>
			<textarea class="textarea" v-model="userForm.personIntruduction" placeholder="请输入个人介绍" maxlength="200" />
			<text class="word-count">{{ userForm.personIntruduction?.length || 0 }}/200</text>
		</view>
		
		<button class="save-btn" @tap="saveUserInfo">保存修改</button>
	</view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { updateUserInfo } from '@/api/user';

// 表单数据
const userForm = reactive({
	nickName: '',
	sex: null,
	birthday: '',
	school: '',
	personIntruduction: ''
});

// 头像相关数据
const avatarUrl = ref(''); // 头像预览URL
const avatarFile = ref(null); // 头像文件对象

// 选择头像
const chooseAvatar = () => {
	uni.chooseImage({
		count: 1, // 只选择一张图片
		sizeType: ['compressed'], // 压缩图片
		sourceType: ['album', 'camera'], // 从相册或相机选择
		success: (res) => {
			// 更新预览
			avatarUrl.value = res.tempFilePaths[0];
			// 保存文件对象供上传使用
			avatarFile.value = res.tempFiles[0];
		}
	});
};

// 生日选择器事件处理
const onBirthdayChange = (e) => {
	userForm.birthday = e.detail.value;
};

// 保存用户信息
const saveUserInfo = async () => {
	try {
		// 只提交有值的字段
		const updateData = {};
		if (userForm.nickName) updateData.nickName = userForm.nickName;
		if (userForm.sex !== null) updateData.sex = userForm.sex;
		if (userForm.birthday) updateData.birthday = userForm.birthday;
		if (userForm.school) updateData.school = userForm.school;
		if (userForm.personIntruduction) updateData.personIntruduction = userForm.personIntruduction;
		
		// 检查是否有任何修改（包括头像）
		if (Object.keys(updateData).length === 0 && !avatarFile.value) {
			uni.showToast({
				title: '请修改至少一项信息',
				icon: 'none'
			});
			return;
		}
		
		// 显示加载提示
		uni.showLoading({
			title: '正在保存...'
		});
		
		// 调用API更新用户信息
		const res = await updateUserInfo(updateData, avatarFile.value);
		
		// 隐藏加载提示
		uni.hideLoading();
		
		if (res.code === 1) {
			uni.showToast({
				title: '修改成功',
				icon: 'success',
				success: () => {
					setTimeout(() => {
						// 跳转到word页面
						uni.reLaunch({
							url: '/pages/word/word'
						});
					}, 1500);
				}
			});
		} else {
			uni.showToast({
				title: res.message || '修改失败',
				icon: 'none'
			});
		}
	} catch (error) {
		uni.hideLoading();
		console.error('更新用户信息失败:', error);
		uni.showToast({
			title: '网络错误，请重试',
			icon: 'none'
		});
	}
};

// 初始化表单数据
onMounted(() => {
	const userInfo = uni.getStorageSync('userInfo');
	if (userInfo) {
		// 初始化表单数据，使用现有值或默认值
		userForm.nickName = userInfo.nickName || '';
		userForm.sex = userInfo.sex !== undefined ? userInfo.sex : null;
		userForm.birthday = userInfo.birthday || '';
		userForm.school = userInfo.school || '';
		userForm.personIntruduction = userInfo.personIntruduction || '';
		
		// 设置当前头像URL
		if (userInfo.avatar) {
			avatarUrl.value = userInfo.avatar;
		}
	}
});
</script>

<style lang="scss" scoped>
.container {
	padding: 30rpx;
	background-color: #f6f7fc;
	min-height: 100vh;
}

/* 头像上传区域样式 */
.avatar-container {
	position: relative;
	width: 180rpx;
	height: 180rpx;
	margin: 30rpx auto 40rpx;
	border-radius: 50%;
	overflow: hidden;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
	
	.avatar-image {
		width: 100%;
		height: 100%;
	}
	
	.avatar-overlay {
		position: absolute;
		bottom: 0;
		left: 0;
		right: 0;
		background: rgba(0, 0, 0, 0.5);
		height: 70rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		
		text {
			color: #fff;
			font-size: 20rpx;
			margin-top: 4rpx;
		}
	}
}

.title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 40rpx;
	text-align: center;
}

.form-item {
	background-color: #fff;
	padding: 20rpx 30rpx;
	border-radius: 12rpx;
	margin-bottom: 20rpx;
	
	.label {
		display: block;
		font-size: 28rpx;
		color: #666;
		margin-bottom: 10rpx;
	}
	
	.input {
		height: 80rpx;
		font-size: 30rpx;
		color: #333;
		width: 100%;
	}
	
	.radio-group {
		display: flex;
		
		.radio {
			display: flex;
			align-items: center;
			justify-content: center;
			width: 120rpx;
			height: 80rpx;
			border: 1rpx solid #ddd;
			border-radius: 8rpx;
			margin-right: 30rpx;
			
			&.active {
				background-color: #007aff;
				color: #fff;
				border-color: #007aff;
			}
		}
	}
	
	.picker {
		height: 80rpx;
		display: flex;
		align-items: center;
		
		.picker-text {
			font-size: 30rpx;
			color: #333;
		}
	}
	
	.textarea {
		width: 100%;
		height: 200rpx;
		font-size: 30rpx;
		color: #333;
		line-height: 1.5;
	}
	
	.word-count {
		display: block;
		text-align: right;
		font-size: 24rpx;
		color: #999;
		margin-top: 10rpx;
	}
}

.save-btn {
	margin-top: 60rpx;
	background-color: #007aff;
	color: #fff;
	font-size: 32rpx;
	border-radius: 12rpx;
	height: 90rpx;
	line-height: 90rpx;
}
</style>
