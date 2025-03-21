<template>
	<view class="container">
		<view class="title-box">
			<view class="title">用户注册</view>
			<view class="subtitle">创建您的心理咨询账户</view>
		</view>

		<view class="input-box">
			<image src="../../static/email.png" class="image" mode="aspectFit"></image>
			<input type="text" placeholder="邮箱" v-model="email" />
		</view>

		<view class="input-box">
			<image src="../../static/username.png" class="image" mode="aspectFit"></image>
			<input type="text" placeholder="昵称" v-model="nickName" />
		</view>

		<view class="input-box">
			<image src="../../static/password.png" class="image2" mode="aspectFit"></image>
			<input type="password" placeholder="密码" v-model="password" />
		</view>
		
		<view class="input-box">
			<image src="../../static/password.png" class="image2" mode="aspectFit"></image>
			<input type="password" placeholder="确认密码" v-model="confirmPassword" />
		</view>

		<view class="verification-box">
			<input class="verification" type="text" placeholder="请输入验证码" v-model="checkCode" />
			<view class="code-image" @tap="refreshCode">
				<image v-if="codeImage" :src="codeImage" mode="aspectFit"></image>
				<text v-else>加载中...</text>
			</view>
		</view>
		
		<button class="register-btn" @click="register">注册</button>

		<view class="footer">
			<text>已有账号？</text>
			<text class="login-link" @click="goLogin">立即登录</text>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getVerificationCode, register as userRegister } from '@/api/user';

// 表单数据
const email = ref('');
const nickName = ref('');
const password = ref('');
const confirmPassword = ref('');
const checkCode = ref('');
const checkCodeKey = ref('');
const codeImage = ref('');

// 获取验证码
const getCode = async () => {
	try {
		const res = await getVerificationCode();
		if (res && res.key && res.image) {
			checkCodeKey.value = res.key;
			codeImage.value = res.image;
		} else {
			uni.showToast({
				title: '获取验证码失败',
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('获取验证码错误:', error);
		uni.showToast({
			title: '获取验证码失败',
			icon: 'none'
		});
	}
};

// 刷新验证码
const refreshCode = () => {
	getCode();
};

// 注册
const register = async () => {
	// 表单验证
	if (!email.value) {
		uni.showToast({ title: '请输入邮箱', icon: 'none' });
		return;
	}
	
	// 邮箱格式验证
	const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
	if (!emailRegex.test(email.value)) {
		uni.showToast({ title: '邮箱格式不正确', icon: 'none' });
		return;
	}
	
	if (!nickName.value) {
		uni.showToast({ title: '请输入昵称', icon: 'none' });
		return;
	}
	
	if (nickName.value.length > 20) {
		uni.showToast({ title: '昵称不能超过20个字符', icon: 'none' });
		return;
	}
	
	if (!password.value) {
		uni.showToast({ title: '请输入密码', icon: 'none' });
		return;
	}
	
	// 密码强度验证（8-16位，包含字母和数字）
	const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,16}$/;
	if (!passwordRegex.test(password.value)) {
		uni.showToast({ title: '密码必须是8-16位，且包含字母和数字', icon: 'none' });
		return;
	}
	
	if (password.value !== confirmPassword.value) {
		uni.showToast({ title: '两次密码输入不一致', icon: 'none' });
		return;
	}
	
	if (!checkCode.value) {
		uni.showToast({ title: '请输入验证码', icon: 'none' });
		return;
	}
	
	if (!checkCodeKey.value) {
		uni.showToast({ title: '验证码已失效，请刷新', icon: 'none' });
		getCode();
		return;
	}
	
	// 提交注册
	try {
		const res = await userRegister({
			email: email.value,
			nickName: nickName.value,
			password: password.value,
			checkCodeKey: checkCodeKey.value,
			checkCode: checkCode.value
		});
		
		if (res.code === 1) {
			uni.showToast({ 
				title: '注册成功', 
				icon: 'success',
				success: () => {
					// 延迟跳转到登录页
					setTimeout(() => {
						uni.navigateTo({ url: '/pages/login/login' });
					}, 1500);
				}
			});
		} else {
			uni.showToast({ title: res.message || '注册失败', icon: 'none' });
			// 刷新验证码
			getCode();
		}
	} catch (error) {
		console.error('注册错误:', error);
		uni.showToast({ title: error.message || '注册失败', icon: 'none' });
		// 刷新验证码
		getCode();
	}
};

// 跳转到登录页
const goLogin = () => {
	uni.navigateTo({ url: '/pages/login/login' });
};

// 组件挂载时获取验证码
onMounted(() => {
	getCode();
});
</script>

<style lang="scss" scoped>
.container {
	padding: 50rpx;
	text-align: center;
}

.title-box{
	display: flex;
	flex-direction: column;
	justify-self: center;
	align-items: flex-start;
}

.title {
	font-size: 50rpx;
	font-weight: 500;
}

.subtitle {
	margin-top: 20rpx;
	font-size: 28rpx;
	color: #666;
	margin-bottom: 40rpx;
}

.input-box {
	display: flex;
	align-items: center;
	justify-content: flex-start;
	background: #f5f5f5;
	padding: 20rpx;
	border-radius: 10rpx;
	margin-bottom: 20rpx;
	
	image{
		width: 50rpx;
		height: 50rpx;
	}
	
	.image2{
		width: 40rpx;
		padding-left: 5rpx;
	}
	
	input {
		display: flex;
		align-items: flex-start;
		text-align: left;
		font-size: 28rpx;
		margin-left:20rpx;
		width: 100%;
	}
}

.verification-box {
	display: flex;
	justify-content: space-between;
	margin-bottom: 30rpx;
	
	.verification{
		display: flex;
		justify-content: flex-start;
		background-color: #f5f5f5;
		height: 90rpx;
		width: 55%;
		border-radius: 10rpx;
		text-align: left;
		padding-left: 20rpx;
	}
	
	.code-image {
		width: 40%;
		height: 90rpx;
		border-radius: 10rpx;
		overflow: hidden;
		background-color: #f5f5f5;
		display: flex;
		align-items: center;
		justify-content: center;
		
		image {
			width: 100%;
			height: 100%;
		}
	}
}

.register-btn {
	width: 100%;
	background: #007aff;
	color: white;
	font-size: 32rpx;
	padding: 20rpx;
	border-radius: 10rpx;
	margin-top: 20rpx;
}

.footer {
	display: flex;
	justify-content: center;
	margin-top: 30rpx;
	font-size: 28rpx;
	color: #666;
	
	.login-link {
		color: #007aff;
		margin-left: 10rpx;
	}
}
</style>
