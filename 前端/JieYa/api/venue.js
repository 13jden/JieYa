// 场地相关API接口

import request from './request';

// 获取所有场地列表
export function getVenueList() {
  return request({
    url: '/venue/list',
    method: 'GET'
  });
}

// 获取场地详情
export function getVenueDetail(id) {
  return request({
    url: `/venue/detail/${id}`,
    method: 'GET'
  });
}

// 按标签筛选场地
export function getVenueByTag(tagId) {
  return request({
    url: `/venue/tag/${tagId}`,
    method: 'GET'
  });
}

// 搜索场地
export function searchVenue(keyword) {
  return request({
    url: '/venue/search',
    method: 'GET',
    data: { keyword }
  });
}

// 获取场地可用时间段
export function getAvailableTimeSlots(params) {
  console.log('API调用参数:', params); // 调试用
  return request({
    url: '/venue-booking/available-time',
    method: 'GET',
    params: {
      venueId: params.venueId,
      date: params.date
    }
  });
}

// 获取场地标签列表
export function getVenueTags() {
  return request({
    url: '/venue-tag/list',
    method: 'GET'
  });
}

// 获取场地预约记录
export function getBookingHistory() {
  return request({
    url: '/venue-booking/history',
    method: 'GET'
  });
}

// 取消场地预约
export function cancelBooking(bookingId) {
  return request({
    url: `/venue-booking/cancel/${bookingId}`,
    method: 'POST'
  });
}

// 支付场地预约
export function payForBooking(bookingId) {
  return request({
    url: `/venue-booking/pay/${bookingId}`,
    method: 'POST'
  });
}

/**
 * 创建场地预约
 * @param {Number} venueId 场地ID
 * @param {String} startTime 开始时间，格式：YYYY-MM-DD HH:mm
 * @param {String} endTime 结束时间，格式：YYYY-MM-DD HH:mm
 */
export function createBooking(venueId, startTime, endTime) {
  return request({
    url: '/venue-booking/create',
    method: 'POST',
    params: { venueId, startTime, endTime }
  });
}

/**
 * 获取预约详情
 * @param {String} bookingId 预约ID
 */
export function getBookingDetail(bookingId) {
  return request({
    url: `/venue-booking/detail/${bookingId}`,
    method: 'GET'
  });
} 