import sockJSService from '../utils/sockjs';

// 退出登录函数
const logout = () => {
  // 关闭SockJS连接
  sockJSService.close();
  
  // 清除登录状态和token
  uni.removeStorageSync('token');
  
  // 跳转到登录页
  uni.reLaunch({ url: '/pages/login/login' });
};

export default logout; 