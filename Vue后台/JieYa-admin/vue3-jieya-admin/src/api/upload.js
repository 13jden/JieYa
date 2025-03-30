import request from '@/utils/request'

// 上传聊天图片/视频
export function uploadMessageFile(formData) {
  return request({
    url: '/image/message',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
} 