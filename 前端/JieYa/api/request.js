/**
 * 请求封装
 * @param {Object} options 请求选项
 * @returns {Promise} 请求结果
 */

const BASE_URL = 'http://localhost:8081';

// 将对象转换为查询字符串
const objectToQueryString = (params) => {
  if (!params) return '';
  return '?' + Object.keys(params)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&');
};

export default function request(options) {
  // 处理查询参数
  const queryString = options.params ? objectToQueryString(options.params) : '';
  
  return new Promise((resolve, reject) => {
    // 获取token
    const token = uni.getStorageSync('token');
    
    // 设置请求头
    const header = {
      ...options.header,
      'Authorization': token ? `${token}` : ''
    };
    
    // 检查是否包含文件上传
    const isFileUpload = options.filePath && options.name;
    
    // 文件上传请求
    if (isFileUpload) {
      uni.uploadFile({
        url: BASE_URL + options.url + queryString,
        filePath: options.filePath,
        name: options.name,
        formData: options.formData || {},
        header: header,
        success: (res) => {
          if (res.statusCode === 500) {
            handleUnauthorized();
            reject({ code: 500, message: '登录已过期，请重新登录' });
            return;
          }
          
          // 上传文件接口返回的是字符串，需要转成对象
          if (typeof res.data === 'string') {
            try {
              const data = JSON.parse(res.data);
              if (data.code === 1) {
                resolve(data);
              } else {
                uni.showToast({
                  title: data.message || '请求失败',
                  icon: 'none'
                });
                reject(data);
              }
            } catch (e) {
              reject({ message: '返回数据格式错误' });
            }
          } else {
            resolve(res.data);
          }
        },
        fail: (err) => {
          console.error('上传失败:', err);
          uni.showToast({
            title: '上传失败',
            icon: 'none'
          });
          reject(err);
        }
      });
    } 
    // 普通请求
    else {
      // 如果没有指定content-type，设置默认值
      if (!header['content-type']) {
        header['content-type'] = options.method === 'GET' 
          ? 'application/x-www-form-urlencoded' 
          : 'application/json';
      }
      
      // 处理数据
      let data = options.data;
      if (header['content-type'] === 'application/x-www-form-urlencoded' && typeof data === 'object') {
        let formData = '';
        for (let key in data) {
          if (data.hasOwnProperty(key)) {
            if (formData !== '') {
              formData += '&';
            }
            formData += encodeURIComponent(key) + '=' + encodeURIComponent(data[key]);
          }
        }
        data = formData;
      }
      
      uni.request({
        url: BASE_URL + options.url + queryString,
        method: options.method || 'GET',
        data: data,
        header: header,
        success: (res) => {
          // 处理401未授权的情况
          if (res.statusCode === 500) {
            handleUnauthorized();
            reject({ code: 500, message: '登录已过期，请重新登录' });
            return;
          }
          
          // 请求成功处理
          if (res.statusCode === 200) {
            if (res.data.code === 1) {
              resolve(res.data);
            } else {
              uni.showToast({
                title: res.data.message || '请求失败',
                icon: 'none'
              });
              reject(res.data);
            }
          } else {
            uni.showToast({
              title: '请求失败: ' + res.statusCode,
              icon: 'none'
            });
            reject(res);
          }
        },
        fail: (err) => {
          console.error('请求失败:', err);
          uni.showToast({
            title: '网络错误，请重试',
            icon: 'none'
          });
          reject(err);
        }
      });
    }
  });
}

// 处理401未授权情况
function handleUnauthorized() {
  // 清除已过期的token和用户信息
  uni.removeStorageSync('token');
  uni.removeStorageSync('userInfo');
  
  // 显示提示
  uni.showToast({
    title: '登录已过期，请重新登录',
    icon: 'none',
    duration: 2000
  });
  
  // 延迟跳转到登录页面
  setTimeout(() => {
    uni.reLaunch({
      url: '/pages/login/login'
    });
  }, 1000);
}