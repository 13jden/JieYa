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
    header: {
      'content-type': 'application/x-www-form-urlencoded'
    },
    data: data
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
    header: {
      'content-type': 'application/x-www-form-urlencoded'
    },
    data: data
  }).then(res => {
    if (res.code === 1 && res.data) {
      // 登录成功，保存token到storage
      uni.setStorageSync('token', res.data.token);
      uni.setStorageSync('userInfo', res.data);
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

export function getDetail(userId) {
  return request({
    url: '/user/detail',
    method: 'GET',
    params: {
      userId: userId
    }
  }).then(res => {
    return res;
  });
}
/**
 * 更新用户信息
 * @param {Object} data 用户信息
 * @param {Object} avatarFile 头像文件对象（可选）
 * @returns {Promise} 更新结果
 */
export function updateUserInfo(data, avatarFile) {
  // 准备数据，确保Boolean值被转换为字符串
  const formData = {...data};
  if (formData.sex !== undefined) {
    formData.sex = String(formData.sex); // 将Boolean转换为'true'或'false'字符串
  }

  // 如果有头像文件
  if (avatarFile && avatarFile.path) {
    return request({
      url: '/user/update',
      method: 'POST',
      filePath: avatarFile.path,
      name: 'avatar',
      formData: formData // 使用处理后的formData
    }).then(res => {
      if (res.code === 1) {
        // 更新成功后，更新本地存储的用户信息
        updateLocalUserInfo(data, res.data);
      }
      return res;
    });
  } 
  // 没有头像文件，走普通请求
  else {
    return request({
      url: '/user/update',
      method: 'POST',
      header: {
        'content-type': 'application/x-www-form-urlencoded'
      },
      data: formData // 使用处理后的formData
    }).then(res => {
      if (res.code === 1) {
        // 更新成功后，更新本地存储的用户信息
        updateLocalUserInfo(data, res.data);
      }
      return res;
    });
  }
}

/**
 * 更新本地存储的用户信息
 * @param {Object} updatedData 更新的数据
 * @param {Object} responseData 服务器返回的数据
 */
function updateLocalUserInfo(updatedData, responseData) {
  try {
    // 获取当前存储的用户信息
    const userInfo = uni.getStorageSync('userInfo') || {};
    
    // 创建更新后的用户信息对象
    const updatedUserInfo = {
      ...userInfo, // 保留原有信息
      ...updatedData // 添加更新的字段
    };
    
    // 如果服务器返回了新数据，优先使用服务器数据
    if (responseData) {
      // 如果返回了新的头像地址
      if (responseData.avatar) {
        updatedUserInfo.avatar = responseData.avatar;
      }
      
      // 如果有其他字段，也一并更新
      if (responseData.nickName) updatedUserInfo.nickName = responseData.nickName;
      if (responseData.sex !== undefined) updatedUserInfo.sex = responseData.sex;
      if (responseData.birthday) updatedUserInfo.birthday = responseData.birthday;
      if (responseData.school) updatedUserInfo.school = responseData.school;
      if (responseData.personIntruduction) updatedUserInfo.personIntruduction = responseData.personIntruduction;
    }
    
    // 将更新后的用户信息保存回本地存储
    uni.setStorageSync('userInfo', updatedUserInfo);
    
    console.log('本地用户信息已更新:', updatedUserInfo);
  } catch (error) {
    console.error('更新本地用户信息失败:', error);
  }
}

/**
 * 搜索用户
 * @param {String} keyword 搜索关键词
 * @returns {Promise} 搜索结果
 */
export function searchUsers(keyword) {
  return request({
    url: '/user/search',
    method: 'GET',
    params: {
      keyword
    }
  });
} 