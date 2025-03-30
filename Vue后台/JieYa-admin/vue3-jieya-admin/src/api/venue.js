import request from '@/utils/request'

// 获取场馆列表
export function getVenueList() {
  return request({
    url: '/venue/list',
    method: 'get'
  })
}

// 添加场馆
export function addVenue(data) {
  return request({
    url: '/venue/add',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取场馆标签列表
export function getVenueTagList() {
  return request({
    url: '/venue-tag/list',
    method: 'get'
  })
}

// 添加场馆标签
export function addVenueTag(tagName) {
  return request({
    url: '/venue-tag/add',
    method: 'post',
    params: { tagName }
  })
}

// 删除场馆标签
export function deleteVenueTag(tagId) {
  return request({
    url: '/venue-tag/delete',
    method: 'post',
    params: { tagId }
  })
}

// 更新场馆
export function updateVenue(data) {
  return request({
    url: '/venue/update',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除场馆
export function deleteVenue(params) {
  return request({
    url: '/venue/delete',
    method: 'post',
    params
  })
} 