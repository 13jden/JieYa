import request from '@/utils/request'

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/image/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function deleteTempImage(fileName) {
  return request({
    url: '/image/deleteTemp',
    method: 'delete',
    params: { fileName }
  })
}

// 删除正式图片
export function deleteBannerImage(fileName) {
  return request({
    url: '/image/deleteBanner',
    method: 'delete',
    params: { fileName }
  })
} 