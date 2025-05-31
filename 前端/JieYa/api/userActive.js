import request from './request';

// 添加点赞
export function addLike( toUserId,noteId) {
  return request({
    url: '/user-active/addLike',
    method: 'POST',
    params: {
      noteId,
      toUserId
    }
  });
}

// 取消点赞
export function deleteLike(toUserId, noteId) {
  return request({
    url: '/user-active/deleteLike',
    method: 'POST',
    params: {
      noteId,
      toUserId
    }
  });
}

// 添加收藏
export function addCollect(userId, noteId) {
  return request({
    url: '/user-active/addCollect',
    method: 'POST',
    params: {
      userId,
      noteId
    }
  });
}

// 取消收藏
export function deleteCollect(userId, noteId) {
  return request({
    url: '/user-active/deleteCollect',
    method: 'POST',
    params: {
      userId,
      noteId
    }
  });
}

// 检查用户是否已点赞笔记
export function checkLikeStatus(userId, noteId) {
  return request({
    url: '/user-active/checkLike',
    method: 'GET',
    params: {
      userId,
      noteId
    }
  });
}

// 检查用户是否已收藏笔记
export function checkCollectStatus(userId, noteId) {
  return request({
    url: '/user-active/checkCollect',
    method: 'GET',
    params: {
      userId,
      noteId
    }
  });
}

// 检查用户活动状态（点赞和收藏）
export function checkUserActive(noteId) {
  return request({
    url: '/user-active/checkActive',
    method: 'GET',
    params: {
      noteId
    }
  });
}

// 关注用户
export function followUser(userId, followId) {
  return request({
    url: '/user-active/follow',
    method: 'POST',
    params: {
      userId,
      followId
    }
  });
}

// 取消关注用户
export function unfollowUser(userId, followId) {
  return request({
    url: '/user-active/unfollow',
    method: 'POST',
    params: {
      userId,
      followId
    }
  });
}

// 检查是否已关注用户
export function checkFollowStatus(userId, followId) {
  return request({
    url: '/user-active/checkFollow',
    method: 'GET',
    params: {
      userId,
      followId
    }
  });
} 