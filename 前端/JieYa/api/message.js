import request from './request'

// 发送消息
export function sendMessage(data) {
  return request({
    url: '/message/send',
    method: 'post',
    params: {
      toUser: data.toUser,
      content: data.content,
      type: data.type,
      fileUrl: data.fileUrl
    }
  })
}

// 获取消息列表
export function getUserMessagesList(pageNum = 1, pageSize = 20) {
  return request({
    url: '/message/getList',
    method: 'get',
    params: {
      pageNum,
      pageSize
    }
  })
}

// 获取系统消息
export function getSystemMessages(pageNum = 1, pageSize = 20) {
  return request({
    url: '/message/system',
    method: 'get',
    params: {
      pageNum,
      pageSize
    }
  })
}

// 获取订单消息
export function getOrderMessages(pageNum = 1, pageSize = 20) {
  return request({
    url: '/message/order',
    method: 'get',
    params: {
      pageNum,
      pageSize
    }
  })
}

// 获取与特定用户的聊天记录
export function getChatMessages(targetId, pageNum = 1, pageSize = 20) {
  return request({
    url: '/message/chat',
    method: 'get',
    params: {
      targetId,
      pageNum,
      pageSize
    }
  })
}

// 标记消息为已读
export function markAsRead(targetId) {
  return request({
    url: '/message/read',
    method: 'post',
    params: {
      targetId
    }
  })
}

// 标记系统消息为已读
export function markSystemMessagesAsRead() {
  return request({
    url: '/message/system/read',
    method: 'post'
  })
}

// 标记订单消息为已读
export function markOrderMessagesAsRead() {
  return request({
    url: '/message/order/read',
    method: 'post'
  })
}

// 删除消息
export function deleteMessage(messageId) {
  return request({
    url: `/message/${messageId}`,
    method: 'delete'
  })
}

// 批量删除消息
export function batchDeleteMessages(messageIds) {
  return request({
    url: '/message/batch',
    method: 'delete',
    data: messageIds
  })
}


// 获取未读消息总数
export function getUnreadMessageCount() {
  return request({
    url: '/message/unread/count',
    method: 'GET'
  });
} 