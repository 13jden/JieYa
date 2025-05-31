import request from '@/utils/request'

// 获取用户列表
export function getUserList(pageNum, pageSize, status, username, userId) {
  return request({
    url: '/user/list',
    method: 'get',
    params: { pageNum, pageSize, status, username, userId }
  })
}

// 禁用用户
export function forbidUser(userId) {
  return request({
    url: '/user/forbidUser',
    method: 'get',
    params: { userId }
  })
}

// 启用用户
export function unforbidUser(userId) {
  return request({
    url: '/user/unforbidUser',
    method: 'get',
    params: { userId }
  })
}

// 发送消息给用户
export function sendMessageToUser(userId, content, type) {
  return request({
    url: '/user/sendMessage2User',
    method: 'get',
    params: { userId, content, type }
  })
}

// 获取消息类型
export function getMessageTypes() {
  return request({
    url: '/user/getMessage2UserType',
    method: 'get'
  })
}

// 发送消息给所有用户
export function sendMessageToAllUsers(content) {
  return request({
    url: '/user/sendMessage2AllUser',
    method: 'get',
    params: { content }
  })
}

// 删除用户
export function deleteUser(userId) {
  return request({
    url: '/user/deleteUser',
    method: 'get',
    params: { userId }
  })
}

// 添加用户
export function addUser(data) {
  return request({
    url: '/user/addUser',
    method: 'post',
    data
  })
}

// 更新用户
export function updateUser(data) {
  return request({
    url: '/user/updateUser',
    method: 'post',
    data
  })
} 