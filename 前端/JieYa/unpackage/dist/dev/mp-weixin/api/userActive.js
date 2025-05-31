"use strict";
const api_request = require("./request.js");
function addLike(toUserId, noteId) {
  return api_request.request({
    url: "/user-active/addLike",
    method: "POST",
    params: {
      noteId,
      toUserId
    }
  });
}
function deleteLike(toUserId, noteId) {
  return api_request.request({
    url: "/user-active/deleteLike",
    method: "POST",
    params: {
      noteId,
      toUserId
    }
  });
}
function addCollect(userId, noteId) {
  return api_request.request({
    url: "/user-active/addCollect",
    method: "POST",
    params: {
      userId,
      noteId
    }
  });
}
function deleteCollect(userId, noteId) {
  return api_request.request({
    url: "/user-active/deleteCollect",
    method: "POST",
    params: {
      userId,
      noteId
    }
  });
}
function checkUserActive(noteId) {
  return api_request.request({
    url: "/user-active/checkActive",
    method: "GET",
    params: {
      noteId
    }
  });
}
exports.addCollect = addCollect;
exports.addLike = addLike;
exports.checkUserActive = checkUserActive;
exports.deleteCollect = deleteCollect;
exports.deleteLike = deleteLike;
