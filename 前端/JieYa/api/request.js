/**
 * 请求工具类
 */

// 基础URL，根据实际环境配置
const BASE_URL = 'http://localhost:8081';

/**
 * 请求封装
 * @param {Object} options 请求选项
 * @returns {Promise} 请求结果
 */
export default function request(options) {
  return new Promise((resolve, reject) => {
    // 获取token
    const token = wx.getStorageSync('token');
    
    // 设置请求头
    const header = {
      'Content-Type': options.method === 'GET' ? 'application/x-www-form-urlencoded' : 'application/json',
      ...options.header
    };
    
    // 如果有token，添加到请求头
    if (token) {
      header['Authorization'] = token;
    }
    
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: header,
      success: (res) => {
        // 请求成功但业务失败
        if (res.statusCode === 200 && res.data.code !== 1) {
          uni.showToast({
            title: res.data.message || '请求失败',
            icon: 'none'
          });
          reject(res.data);
          return;
        }
        
        // 未授权，清除token并跳转到登录页
        if (res.statusCode === 401) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          uni.navigateTo({
            url: '/pages/login/login'
          });
          reject(res.data);
          return;
        }
        
        // 请求成功且业务成功
        resolve(res.data);
      },
      fail: (err) => {
        uni.showToast({
          title: '网络请求失败',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
}