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
    if(token==null){
		setTimeout(() => {
		  uni.reLaunch({
		    url: '/pages/login/login'
		  });
		}, 1000);
		return reject({ code: 401, message: '未登录，请先登录' });
	}
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
          if (res.statusCode === 50) {
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
      // 修改这里：检查是否已设置content-type，如果未设置，则使用默认值
      if (!header['content-type'] && !header['Content-Type']) {
        // 默认值：GET请求使用表单格式，POST请求也默认使用表单格式
        header['content-type'] = 'application/x-www-form-urlencoded';
      }
      
      // 处理数据
      let data = options.data;
      
      // 增强表单数据处理，支持数组
      let contentType = header['content-type'] || header['Content-Type'] || '';
      contentType = contentType.toLowerCase(); // 转小写以便比较
      
      // 如果是表单格式且data是对象，转换为表单字符串
      if ((contentType.includes('application/x-www-form-urlencoded')) && typeof data === 'object' && data !== null) {
        // 预处理数组参数
        const processedData = {};
        
        for (const key in data) {
          if (data.hasOwnProperty(key)) {
            if (Array.isArray(data[key])) {
              // 将数组参数转为JSON字符串
              processedData[key] = JSON.stringify(data[key]);
            } else if (data[key] !== null && data[key] !== undefined) {
              processedData[key] = data[key];
            }
          }
        }
        
        // 转换为URL编码的表单字符串
        let formData = '';
        for (let key in processedData) {
          if (processedData.hasOwnProperty(key)) {
            if (formData !== '') {
              formData += '&';
            }
            formData += encodeURIComponent(key) + '=' + encodeURIComponent(processedData[key] === null ? '' : processedData[key]);
          }
        }
        data = formData;
        
        // 调试信息
        console.log('表单数据:', data);
      } else if (contentType.includes('application/json') && typeof data === 'object') {
        // JSON格式，无需特殊处理
        console.log('JSON数据:', data);
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