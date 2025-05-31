import request from './request'

// 获取笔记详情
export function getNoteDetail(id) {
  return request({
    url: '/note/detail',
    method: 'GET',
    params: { id }
  })
}

// 添加笔记
export function addNote(data, fileList) {
  // 如果没有图片，直接创建笔记
  if (!fileList || fileList.length === 0) {
    return createNoteWithoutImages(data);
  }
  
  // 有图片，先上传第一张获取noteId
  return uploadFirstImage(fileList[0])
    .then(firstImageRes => {
      const noteId = firstImageRes.data.nodeId;
      console.log('获取到noteId:', noteId);
      
      // 如果有多张图片，上传剩余图片
      if (fileList.length > 1) {
        const uploadPromises = fileList.slice(1).map((file, index) => {
          return uploadImage(file, index + 2, noteId);
        });
        
        return Promise.all(uploadPromises)
          .then(() => {
            // 所有图片上传完成，创建笔记
            return createNote(data, noteId, fileList.length);
          });
      } else {
        // 只有一张图片，直接创建笔记
        return createNote(data, noteId, 1);
      }
    });
}

// 上传第一张图片获取noteId
function uploadFirstImage(filePath) {
  return uploadImage(filePath, 1, null);
}

// 上传单张图片
function uploadImage(filePath, sort, noteId) {
  const formData = {
    sort: sort
  };
  
  if (noteId) {
    formData.noteId = noteId;
  }
  
  return request({
    url: '/node-image/upload',
    method: 'post',
    formData: formData,
    filePath: filePath,
    name: 'file'
  });
}

// 创建笔记
function createNote(data, noteId, imageCount) {
  // 准备参数数据
  const params = {
    title: data.title,
    noteId: noteId,
    content: data.content || '',
    categoryId: data.categoryId || 1,
    imageCount: imageCount
  };
  
  // 处理标签数组
  if (data.tags && data.tags.length) {
    // 如果后端能正确解析JSON字符串
    params.tags = JSON.stringify(data.tags);
  }
  
  return request({
    url: '/node-post/postNode',
    method: 'post',
    params: params  // 使用params而不是data
  });
}

// 更新笔记
export function updateNote(id, data, newFileList, originalImageList) {
  // 准备表单数据
  const formData = {
    postId: id,
    title: data.title,
    content: data.content,
    categoryId: data.categoryId || 1
  };
  
  // 处理标签数组
  if (data.tags && data.tags.length) {
    // 使用JSON.stringify处理标签数组，与createNote保持一致
    formData.tags = JSON.stringify(data.tags);
  }
  
  // 添加原始图片列表
  if (originalImageList && originalImageList.length) {
    formData.imageList = JSON.stringify(originalImageList);
  }
  
  // 如果有新文件，上传第一张作为封面
  const coverFile = newFileList.length > 0 ? newFileList[0] : null;
  
  // 先更新笔记和上传第一张新图片
  return request({
    url: '/node-post/updateNode',
    method: 'post',
    formData: formData,
    filePath: coverFile,
    name: 'imageFile',
    header: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  }).then(res => {
    // 如果笔记更新成功且有多张新图片，上传额外的图片
    if (res.code === 200 && newFileList.length > 1) {
      const nodeId = id;
      
      // 计算起始排序值
      const startSort = originalImageList ? originalImageList.length + 1 : 2;
      
      // 逐个上传剩余图片
      const uploadPromises = newFileList.slice(1).map((file, index) => {
        return uploadImage(file, startSort + index, nodeId);
      });
      
      return Promise.all(uploadPromises)
        .then(() => res)
        .catch(() => res);
    }
    
    return res;
  });
}

// 删除笔记
export function deleteNote(postId) {
  return request({
    url: '/node-post/deleteNode',
    method: 'delete',
    params: { postId }
  })
}

// 获取笔记列表
export function getNoteList(params) {
  return request({
    url: '/note/list',
    method: 'GET',
    params
  });
}

// 获取我的笔记列表
export function getMyNoteList(params) {
  return request({
    url: '/node-post/myList',
    method: 'get',
    params
  })
}

// 创建不含图片的笔记
function createNoteWithoutImages(data) {
  // 调用上传接口获取noteId
  return uploadImage('/static/images/placeholder.png', 1, null)
    .then(res => {
      const noteId = res.data.nodeId;
      return createNote(data, noteId, 0);
    });
}

// 根据用户ID获取用户的笔记列表
export function getUserNotes(userId, pageNum = 1, pageSize = 10) {
  return request({
    url: '/note/getUserNotes',
    method: 'GET',
    params: {
      userId,
      pageNum,
      pageSize
    }
  });
}

// ------------------- 用户活跃相关接口 -------------------

// 检查用户活跃状态（是否点赞、收藏等）
export function checkUserActive(noteId) {
  return request({
    url: '/user-active/checkActive',
    method: 'get',
    params: { noteId }
  })
}

// 获取用户收藏列表
export function getCollectList(userId) {
  return request({
    url: '/user-active/getCollectList',
    method: 'get',
    params: { userId }
  })
}

// 添加点赞
export function addLike(noteId, toUserId) {
  return request({
    url: '/user-active/addLike',
    method: 'get',
    params: { 
      noteId,
      toUserId
    }
  })
}

// 取消点赞
export function deleteLike(noteId, toUserId) {
  return request({
    url: '/user-active/deleteLike',
    method: 'get',
    params: { 
      noteId,
      toUserId
    }
  })
}

// 添加收藏
export function addCollect(userId, noteId) {
  return request({
    url: '/user-active/addCollect',
    method: 'get',
    params: { 
      userId,
      noteId
    }
  })
}

// 取消收藏
export function deleteCollect(userId, noteId) {
  return request({
    url: '/user-active/deleteCollect',
    method: 'get',
    params: { 
      userId,
      noteId
    }
  })
}

// ------------------- 评论相关接口 -------------------

// 获取笔记的评论列表
export function getCommentsByNoteId(noteId, pageNum = 1, pageSize = 10) {
  return request({
    url: '/comment/getListByNoteId',
    method: 'get',
    params: {
      noteId,
      pageNum,
      pageSize
    }
  })
}

// 获取子评论
export function getCommentChildren(commentParentId, pageNum = 1, pageSize = 10) {
  return request({
    url: '/comment/getChildren',
    method: 'get',
    params: {
      commentParentId,
      pageNum,
      pageSize
    }
  })
}

// 添加评论
export function addComment(data) {
  return request({
    url: '/comment/add',
    method: 'post',
    data: {
      content: data.content,
      noteId: data.noteId,
      parentId: data.parentId || null,
      toUser: data.toUser || null
    },
    header: {
      'content-type': 'application/x-www-form-urlencoded'
    }
  })
}

// 删除评论
export function deleteComment(commentId) {
  return request({
    url: '/comment/delete',
    method: 'get',
    params: { commentId }
  })
}

// ------------------- 关注相关接口 -------------------

// 添加关注
export function addFocus(userId, focusUserId) {
  return request({
    url: '/focus/add',
    method: 'get',
    params: {
      userId,
      focusUserId
    }
  })
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
  })
}

// 获取关注列表
export function getFocusList(userId) {
  return request({
    url: '/focus/getList',
    method: 'get',
    params: { userId }
  })
}

// 获取粉丝列表
export function getFansList(userId) {
  return request({
    url: '/focus/getFansList',
    method: 'get',
    params: { userId }
  })
}

// 搜索笔记
export function searchNotes(keyword, pageNum = 1, pageSize = 10) {
  return request({
    url: '/note/search',
    method: 'GET',
    params: {
      keyword,
      pageNum,
      pageSize
    }
  })
} 