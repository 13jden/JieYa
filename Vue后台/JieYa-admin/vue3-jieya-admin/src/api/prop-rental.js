import request from '@/utils/request'

// 分页查询租借记录
export function getPropRentalPage(params) {
  return request({
    url: '/prop-rental/page',
    method: 'get',
    params
  })
}

// 根据ID查询租借详情
export function getPropRentalById(id) {
  return request({
    url: `/prop-rental/${id}`,
    method: 'get'
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