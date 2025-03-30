import request from '@/utils/request'

// 获取道具列表
export function getPropList(params) {
  return request({
    url: '/prop/list',
    method: 'get',
    params
  })
}

// 获取道具详情
export function getPropDetail(id) {
  return request({
    url: `/prop/detail/${id}`,
    method: 'get'
  })
}

// 添加道具
export function addProp(data) {
  return request({
    url: '/prop/add',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 更新道具
export function updateProp(data) {
  return request({
    url: '/prop/update',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除道具
export function deleteProp(params) {
  return request({
    url: '/prop/delete',
    method: 'post',
    params
  })
}

// 获取道具租借列表
export function getPropRentalList(params) {
  return request({
    url: '/prop-rental/page',
    method: 'get',
    params
  })
}

// 更新租借状态
export function updatePropRentalStatus(id, status) {
  return request({
    url: '/prop-rental/update-status',
    method: 'post',
    params: { id, status }
  })
}

// 删除租借记录
export function deletePropRental(id) {
  return request({
    url: `/prop-rental/delete/${id}`,
    method: 'post'
  })
}

// 批量删除租借记录
export function batchDeletePropRentals(ids) {
  return request({
    url: '/prop-rental/batch-delete',
    method: 'post',
    data: ids
  })
} 