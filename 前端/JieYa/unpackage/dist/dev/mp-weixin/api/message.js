"use strict";
const api_request = require("./request.js");
function sendMessage(data) {
  return api_request.request({
    url: "/message/send",
    method: "post",
    params: {
      toUser: data.toUser,
      content: data.content,
      type: data.type,
      fileUrl: data.fileUrl
    }
  });
}
function getUserMessagesList(pageNum = 1, pageSize = 20) {
  return api_request.request({
    url: "/message/getList",
    method: "get",
    params: {
      pageNum,
      pageSize
    }
  });
}
function getSystemMessages(pageNum = 1, pageSize = 20) {
  return api_request.request({
    url: "/message/system",
    method: "get",
    params: {
      pageNum,
      pageSize
    }
  });
}
function getOrderMessages(pageNum = 1, pageSize = 20) {
  return api_request.request({
    url: "/message/order",
    method: "get",
    params: {
      pageNum,
      pageSize
    }
  });
}
function getChatMessages(targetId, pageNum = 1, pageSize = 20) {
  return api_request.request({
    url: "/message/chat",
    method: "get",
    params: {
      targetId,
      pageNum,
      pageSize
    }
  });
}
function markAsRead(targetId) {
  return api_request.request({
    url: "/message/read",
    method: "post",
    params: {
      targetId
    }
  });
}
function markSystemMessagesAsRead() {
  return api_request.request({
    url: "/message/system/read",
    method: "post"
  });
}
function markOrderMessagesAsRead() {
  return api_request.request({
    url: "/message/order/read",
    method: "post"
  });
}
function getUnreadMessageCount() {
  return api_request.request({
    url: "/message/unread/count",
    method: "GET"
  });
}
exports.getChatMessages = getChatMessages;
exports.getOrderMessages = getOrderMessages;
exports.getSystemMessages = getSystemMessages;
exports.getUnreadMessageCount = getUnreadMessageCount;
exports.getUserMessagesList = getUserMessagesList;
exports.markAsRead = markAsRead;
exports.markOrderMessagesAsRead = markOrderMessagesAsRead;
exports.markSystemMessagesAsRead = markSystemMessagesAsRead;
exports.sendMessage = sendMessage;
