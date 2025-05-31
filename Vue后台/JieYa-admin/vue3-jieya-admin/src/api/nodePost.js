import request from '@/utils/request'

// 获取待审核笔记列表
export function getPendingList(page, size) {
  return request({
    url: '/node-post/pendingList',
    method: 'get',
    params: { page, size }
  })
}

// 获取所有笔记列表
export function getPostList(page, size, categoryId, keyword) {
  return request({
    url: '/node-post/list',
    method: 'get',
    params: { page, size, categoryId, keyword }
  })
}

// 按标题搜索笔记
export function searchByTitle(title, page, size) {
  return request({
    url: '/node-post/searchByTitle',
    method: 'get',
    params: { title, page, size }
  })
}

// 获取笔记详情
export function getPostDetail(postId) {
  return request({
    url: '/node-post/detail',
    method: 'get',
    params: { postId }
  })
}

// 审核通过笔记
export function approvePost(postId) {
  return request({
    url: '/node-post/approve',
    method: 'post',
    params: { postId }
  })
}

// 驳回笔记
export function rejectPost(postId, reason) {
  return request({
    url: '/node-post/reject',
    method: 'post',
    params: { postId, reason }
  })
}

// 删除笔记
export function deletePost(postId) {
  return request({
    url: '/node-post/delete',
    method: 'delete',
    params: { postId }
  })
}

// 按用户ID查询笔记
export function getPostsByUser(userId, page, size) {
  return request({
    url: '/node-post/listByUser',
    method: 'get',
    params: { userId, page, size }
  })
}

// 修改笔记分类
export function updateCategory(postId, categoryId) {
  return request({
    url: '/node-post/updateCategory',
    method: 'put',
    params: { postId, categoryId }
  })
} 