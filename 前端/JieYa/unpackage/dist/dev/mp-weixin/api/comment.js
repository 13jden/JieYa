"use strict";
const common_vendor = require("../common/vendor.js");
const api_request = require("./request.js");
function getCommentsByNoteId(noteId, pageNum = 1, pageSize = 10) {
  return api_request.request({
    url: "/comment/getListByNoteId",
    method: "GET",
    params: {
      noteId,
      pageNum,
      pageSize
    }
  });
}
function getChildComments(parentId, pageNum = 1, pageSize = 10) {
  return api_request.request({
    url: "/comment/getChildren",
    method: "GET",
    params: {
      commentParentId: parentId,
      pageNum,
      pageSize
    }
  });
}
function addComment(data) {
  return api_request.request({
    url: "/comment/add",
    method: "POST",
    data,
    headers: {
      Authorization: common_vendor.index.getStorageSync("token"),
      "Content-Type": "multipart/form-data"
    }
  });
}
exports.addComment = addComment;
exports.getChildComments = getChildComments;
exports.getCommentsByNoteId = getCommentsByNoteId;
