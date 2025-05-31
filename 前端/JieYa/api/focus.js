import request from './request';

// 添加关注
export function addFocus(userId, focusUserId) {
  return request({
    url: '/focus/add',
    method: 'get',
    params: {
      userId,
      focusUserId
    }
  });
}

// 取消关注
export function deleteFocus(userId, focusUserId) {
  return request({
    url: '/focus/delete',
    method: 'get',
    params: {
      userId,
      focusUserId
    }
  });
}

// 获取关注列表
export function getFocusList(userId) {
  return request({
    url: '/focus/getList',
    method: 'get',
    params: { userId }
  });
}

// 获取粉丝列表
export function getFansList(userId) {
  return request({
    url: '/focus/getFansList',
    method: 'get',
    params: { userId }
  });
}

// 检查是否已关注用户
export function checkFocusStatus(userId, focusUserId) {
  return request({
    url: '/focus/check',
    method: 'get',
    params: {
      userId,
      focusUserId
    }
  });
} 