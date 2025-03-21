import request from './request';

/**
 * 用户API服务
 */

/**
 * 获取验证码
 * @returns {Promise} 返回checkCodeKey用于后续验证
 */
export function getVerificationCode() {
  return request({
    url: '/checkcode',
    method: 'GET'
  }).then(res => {
    if (res.code === 1 && res.data) {
      // 正确处理返回的数据
      return {
        key: res.data.checkCodeKey,
        image: res.data.checkCode // 这已经是完整的data:image/png;base64格式
      };
    }
    return null;
  });
}

/**
 * 用户注册
 * @param {Object} data 注册信息
 * @returns {Promise} 注册结果
 */
export function register(data) {
  return request({
    url: '/user/register',
    method: 'POST',
    data
  });
}

/**
 * 用户登录
 * @param {Object} data 登录信息
 * @returns {Promise} 登录结果，包含token和用户信息
 */
export function login(data) {
  return request({
    url: '/user/login',
    method: 'POST',
    data
  }).then(res => {
    if (res.code === 1 && res.data) {
      // 登录成功，保存token到storage
      wx.setStorageSync('token', res.data.token);
      wx.setStorageSync('userInfo', res.data);
    }
    return res;
  });
}

/**
 * 自动登录
 * @returns {Promise} 登录结果
 */
export function autoLogin() {
  const token = wx.getStorageSync('token');
  if (!token) {
    return Promise.reject('未找到有效token');
  }
  
  return request({
    url: '/user/autologin',
    method: 'GET'
  }).then(res => {
    if (res.code === 1 && res.data) {
      // 更新用户信息
      wx.setStorageSync('userInfo', res.data);
    }
    return res;
  });
}

/**
 * 退出登录
 * @returns {Promise} 登出结果
 */
export function logout() {
  return request({
    url: '/user/loginout',
    method: 'POST'
  }).then(res => {
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
    return res;
  });
} 