import request from './request';

// 获取笔记的评论列表
export function getCommentsByNoteId(noteId, pageNum = 1, pageSize = 10) {
  return request({
    url: '/comment/getListByNoteId',
    method: 'GET',
    params: {
      noteId,
      pageNum,
      pageSize
    }
  });
}

// 获取子评论
export function getChildComments(parentId, pageNum = 1, pageSize = 10) {
  return request({
    url: '/comment/getChildren',
    method: 'GET',
    params: {
      commentParentId: parentId,
      pageNum,
      pageSize
    }
  });
}

// 添加评论
export function addComment(data) {
  return request({
    url: '/comment/add',
    method: 'POST',
    data,
    headers: {
      Authorization: uni.getStorageSync('token'),
      'Content-Type': 'multipart/form-data'
    }
  });
} 