"use strict";
const api_request = require("./request.js");
function getNoteDetail(id) {
  return api_request.request({
    url: "/note/detail",
    method: "GET",
    params: { id }
  });
}
function addNote(data, fileList) {
  if (!fileList || fileList.length === 0) {
    return createNoteWithoutImages(data);
  }
  return uploadFirstImage(fileList[0]).then((firstImageRes) => {
    const noteId = firstImageRes.data.nodeId;
    console.log("获取到noteId:", noteId);
    if (fileList.length > 1) {
      const uploadPromises = fileList.slice(1).map((file, index) => {
        return uploadImage(file, index + 2, noteId);
      });
      return Promise.all(uploadPromises).then(() => {
        return createNote(data, noteId, fileList.length);
      });
    } else {
      return createNote(data, noteId, 1);
    }
  });
}
function uploadFirstImage(filePath) {
  return uploadImage(filePath, 1, null);
}
function uploadImage(filePath, sort, noteId) {
  const formData = {
    sort
  };
  if (noteId) {
    formData.noteId = noteId;
  }
  return api_request.request({
    url: "/node-image/upload",
    method: "post",
    formData,
    filePath,
    name: "file"
  });
}
function createNote(data, noteId, imageCount) {
  const params = {
    title: data.title,
    noteId,
    content: data.content || "",
    categoryId: data.categoryId || 1,
    imageCount
  };
  if (data.tags && data.tags.length) {
    params.tags = JSON.stringify(data.tags);
  }
  return api_request.request({
    url: "/node-post/postNode",
    method: "post",
    params
    // 使用params而不是data
  });
}
function updateNote(id, data, newFileList, originalImageList) {
  const formData = {
    postId: id,
    title: data.title,
    content: data.content,
    categoryId: data.categoryId || 1
  };
  if (data.tags && data.tags.length) {
    formData.tags = JSON.stringify(data.tags);
  }
  if (originalImageList && originalImageList.length) {
    formData.imageList = JSON.stringify(originalImageList);
  }
  const coverFile = newFileList.length > 0 ? newFileList[0] : null;
  return api_request.request({
    url: "/node-post/updateNode",
    method: "post",
    formData,
    filePath: coverFile,
    name: "imageFile",
    header: {
      "Content-Type": "application/x-www-form-urlencoded"
    }
  }).then((res) => {
    if (res.code === 200 && newFileList.length > 1) {
      const nodeId = id;
      const startSort = originalImageList ? originalImageList.length + 1 : 2;
      const uploadPromises = newFileList.slice(1).map((file, index) => {
        return uploadImage(file, startSort + index, nodeId);
      });
      return Promise.all(uploadPromises).then(() => res).catch(() => res);
    }
    return res;
  });
}
function getNoteList(params) {
  return api_request.request({
    url: "/note/list",
    method: "GET",
    params
  });
}
function createNoteWithoutImages(data) {
  return uploadImage("/static/images/placeholder.png", 1, null).then((res) => {
    const noteId = res.data.nodeId;
    return createNote(data, noteId, 0);
  });
}
function getUserNotes(userId, pageNum = 1, pageSize = 10) {
  return api_request.request({
    url: "/note/getUserNotes",
    method: "GET",
    params: {
      userId,
      pageNum,
      pageSize
    }
  });
}
function getCollectList(userId) {
  return api_request.request({
    url: "/user-active/getCollectList",
    method: "get",
    params: { userId }
  });
}
function searchNotes(keyword, pageNum = 1, pageSize = 10) {
  return api_request.request({
    url: "/note/search",
    method: "GET",
    params: {
      keyword,
      pageNum,
      pageSize
    }
  });
}
exports.addNote = addNote;
exports.getCollectList = getCollectList;
exports.getNoteDetail = getNoteDetail;
exports.getNoteList = getNoteList;
exports.getUserNotes = getUserNotes;
exports.searchNotes = searchNotes;
exports.updateNote = updateNote;
