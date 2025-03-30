import request from '@/utils/request'

// 分页查询预约记录
export function getVenueBookingPage(params) {
  return request({
    url: '/venue-booking/page',
    method: 'get',
    params
  })
}

// 根据ID查询预约详情
export function getVenueBookingById(id) {
  return request({
    url: `/venue-booking/${id}`,
    method: 'get'
  })
}

// 根据用户ID查询预约记录
export function getBookingsByUserId(userId) {
  return request({
    url: `/venue-booking/user/${userId}`,
    method: 'get'
  })
}

// 根据场地ID查询预约记录
export function getBookingsByVenueId(venueId) {
  return request({
    url: `/venue-booking/venue/${venueId}`,
    method: 'get'
  })
}

// 根据日期查询预约记录
export function getBookingsByDate(date) {
  return request({
    url: '/venue-booking/date',
    method: 'get',
    params: { date }
  })
}

// 更新预约状态
export function updateVenueBookingStatus(id, status) {
  return request({
    url: '/venue-booking/update-status',
    method: 'post',
    params: { id, status }
  })
}

// 删除预约记录
export function deleteVenueBooking(id) {
  return request({
    url: `/venue-booking/delete/${id}`,
    method: 'post'
  })
}

// 批量删除预约记录
export function batchDeleteVenueBookings(ids) {
  return request({
    url: '/venue-booking/batch-delete',
    method: 'post',
    data: ids
  })
}

// 查询某场地某天的所有预约时段
export function getBookingsByVenueAndDate(venueId, date) {
  return request({
    url: '/venue-booking/venue-date',
    method: 'get',
    params: { venueId, date }
  })
} 