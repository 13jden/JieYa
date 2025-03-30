// 道具相关API接口

import request from './request';
// 获取所有道具列表
export function getPropList() {
  return request({
    url: '/prop/list',
    method: 'GET'
  });
}

// 获取道具详情
export function getPropDetail(id) {
  return request({
    url: `/prop/detail/${id}`,
    method: 'GET'
  });
}

// 搜索道具
export function searchProp(keyword) {
  return request({
    url: '/prop/search',
    method: 'GET',
    data: { keyword }
  });
}

// 获取可用道具列表
export function getAvailableProps() {
  return request({
    url: '/prop/available',
    method: 'GET'
  });
}

// 检查道具在指定日期是否可租借
export function checkPropAvailability(propId, startDate, endDate) {
  return request({
    url: '/prop/check-availability',
    method: 'GET',
    data: {
      propId,
      startDate,
      endDate
    }
  });
}

// 租借道具（使用URL请求参数）
export function rentProp(propId) {
  return request({
    url: `/prop-rental/rent?propId=${propId}`,
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  });
}

// 归还道具
export function returnProp(rentalId) {
  return request({
    url: `/prop-rental/return/${rentalId}`,
    method: 'POST'
  });
}

// 获取租借历史
export function getRentalHistory() {
  return request({
    url: '/prop-rental/history',
    method: 'GET'
  });
} 