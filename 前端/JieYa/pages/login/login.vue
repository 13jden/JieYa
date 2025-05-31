<template>
  <view class="container">
	<view class="title-box">
		<view class="title">欢迎回来</view>
		<view class="subtitle">登录您的账户</view>
	</view>
    

    <view class="input-box">
      <image src="../../static/email.png" class="image" mode="aspectFit"></image>
      <input type="text" placeholder="邮箱" v-model="email" />
    </view>

    <view class="input-box">
      <image src="../../static/password.png" class="image2" mode="aspectFit"></image>
      <input type="password" placeholder="密码" v-model="password" />
    </view>

    <view class="verification-box">
      <input class="verification" type="text" placeholder="请输入验证码" v-model="checkCode" />
      <view class="code-image" @tap="refreshCode">
        <image v-if="codeImage" :src="codeImage" mode="aspectFit"></image>
        <text v-else>加载中...</text>
      </view>
    </view>
	
    <button class="login-btn" @click="login">登录</button>

    <view class="footer">
      <text class="forgot-password">忘记密码？</text>
      <text class="register" @click="goRegister">立即注册</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { getVerificationCode, login as userLogin } from '../../api/user';
import webSocketService from '../../utils/websocket';

// 表单数据
const email = ref('');
const password = ref('');
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

// 登录
const login = async () => {
  // 表单验证
  if (!email.value) {
    uni.showToast({ title: '请输入邮箱', icon: 'none' });
    return;
  }
  
  if (!password.value) {
    uni.showToast({ title: '请输入密码', icon: 'none' });
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
  
  // 提交登录
  try {
    const res = await userLogin({
      email: email.value,
      password: password.value,
      checkCodeKey: checkCodeKey.value,
      checkCode: checkCode.value
    });
    
    if (res.code === 1) {
      const token = res.data.token;
      
      // 保存token到本地存储
      uni.setStorageSync('token', token);
      
      // 使用WebSocket连接
      const wsUrl = 'ws://localhost:8082/websocket';
      webSocketService.connect(wsUrl, token);
      
      // 监听WebSocket消息
      webSocketService.on('message', (data) => {
        console.log('接收到WebSocket消息:', data);
        // 处理消息
      });
      
      // 登录成功提示并跳转
      uni.showToast({ 
        title: '登录成功', 
        icon: 'success',
        success: () => {
          setTimeout(() => {
            uni.switchTab({ url: '/pages/index/index' });
          }, 1500);
        }
      });
    } else {
      uni.showToast({ title: res.message || '登录失败', icon: 'none' });
      // 刷新验证码
      getCode();
    }
  } catch (error) {
    console.error('登录错误:', error);
    uni.showToast({ title: error.message || '登录失败', icon: 'none' });
    // 刷新验证码
    getCode();
  }
};

// 跳转到注册页
const goRegister = () => {
  uni.navigateTo({ url: '/pages/register/register' });
};

// 组件挂载时获取验证码
onMounted(() => {
  getCode();
});

// 组件卸载前关闭WebSocket连接
onBeforeUnmount(() => {
  webSocketService.close();
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
	margin-left:20rpx ;
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

