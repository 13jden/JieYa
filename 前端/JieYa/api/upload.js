import request from './request'

// 上传消息图片
export function uploadMessageImage(file) {
  return request({
    url: '/image/message',
    method: 'POST',
    filePath: file,  
    name: 'file',   
    header: {
      'Authorization': uni.getStorageSync('token')
    }
  });
} 