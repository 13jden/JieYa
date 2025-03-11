<template>
  <view class="container">
	<view class="title-box">
		<view class="title">欢迎回来</view>
		<view class="subtitle">登录您的账户</view>
	</view>
    

    <view class="input-box">
	  <image src="../../static/username.png" class="image" mode="aspectFit"></image>
      <input type="text" placeholder="用户名" v-model="username" />
    </view>

    <view class="input-box">
      <image src="../../static/password.png" class="image2" mode="aspectFit"></image>
      <input type="password" placeholder="密码" v-model="password" />
    </view>

    <view class="verification-box">
      <input class="verification" type="text" placeholder="请输入验证码" v-model="code" />
	  <button class="code-btn" @click="getCode">{{ codeText }}</button>
    </view>
	
    <button class="login-btn" @click="login">登录</button>

    <view class="footer">
      <text class="forgot-password">忘记密码？</text>
      <text class="register" @click="goRegister">立即注册</text>
    </view>
  </view>
</template>
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
	margin-left:20rpx ;
  }
}

.verification-box {
  display: flex;
  justify-content: space-between;
  .verification{
	  display: flex;
	  justify-content: flex-start;
	  background-color: #f5f5f5;
	  height: 90rpx;
	  border-radius: 20rpx;
	  text-align: left;
	  padding-left: 20rpx;
	  margin-left:20rpx ;
  }
  .code-btn {
    background: #007aff;
    color: white;
    border: none;
    padding: 10rpx 20rpx;
    border-radius: 5rpx;
  }
}

.login-btn {
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
  justify-content: space-between;
  margin-top: 30rpx;
  font-size: 28rpx;
  
  .forgot-password {
    color: #007aff;
  }
  
  .register {
    color: #007aff;
    font-weight: bold;
  }
}
</style>
<script setup>
import { ref } from "vue";

const username = ref("");
const password = ref("");
const code = ref("");
const codeText = ref("验证码");

const getCode = () => {
  codeText.value = "已发送";
  setTimeout(() => {
    codeText.value = "验证码";
  }, 3000);
};

const login = () => {
  if (!username.value || !password.value) {
    uni.showToast({ title: "请输入用户名和密码", icon: "none" });
    return;
  }
  uni.showToast({ title: "登录成功", icon: "success" });
  uni.navigateTo({ url: "/pages/index/index" });
};

const goRegister = () => {
  uni.navigateTo({ url: "/pages/register/register" });
};
</script>


