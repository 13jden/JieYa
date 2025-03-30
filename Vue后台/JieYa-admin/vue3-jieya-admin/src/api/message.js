import request from '@/utils/request'

// 获取所有类型消息的未读数量
export function getAllMessageCounts() {
  return request({
    url: '/message/getAllList',
    method: 'get'
  })
}

// 获取用户消息列表
export function getUserMessageList(pageNum, pageSize) {
  return request({
    url: '/message/getUserMessageList',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

// 获取特定用户的消息详情
export function getUserItemMessageList(userId, pageNum, pageSize) {
  return request({
    url: '/message/getUserItemMessageList',
    method: 'get',
    params: { userId, pageNum, pageSize }
  })
}

// 获取订单消息
export function getOrderMessage(pageNum, pageSize) {
  return request({
    url: '/message/getOrderMessage',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

// 获取系统消息
export function getSystemMessage(pageNum, pageSize) {
  return request({
    url: '/message/getSystemMessage',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

// 发送消息
export function sendMessage(toUser, content, type, fileUrl) {
  return request({
    url: '/message/send',
    method: 'post',
    params: {
      toUser,
      content,
      type,
      fileUrl
    }
  })
}

// 删除单条消息
export function deleteMessage(messageId) {
  return request({
    url: '/message/deleteMessage',
    method: 'get',
    params: { messageId }
  })
}

// 批量删除消息
export function deleteMessageList(messageIds) {
  return request({
    url: '/message/deleteMessageList',
    method: 'get',
    params: { messageIds }
  })
}

// 删除与用户的所有消息
export function deleteUserMessage(userId) {
  return request({
    url: '/message/deleteUserMessage',
    method: 'get',
    params: { userId }
  })
}

// 标记订单消息为已读
export function markOrderMessagesAsRead() {
  return request({
    url: '/message/read/order',
    method: 'post'
  })
}

// 标记系统消息为已读
export function markSystemMessagesAsRead() {
  return request({
    url: '/message/read/system',
    method: 'post'
  })
}

// 标记用户消息为已读
export function markUserMessagesAsRead(userId) {
  const formData = new FormData()
  formData.append('userId', userId)
  
  return request({
    url: '/message/read/user',
    method: 'post',
    data: formData
  })
}

// 标记所有消息为已读
export function markAllMessagesAsRead() {
  return request({
    url: '/message/read/all',
    method: 'post'
  })
} 