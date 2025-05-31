"use strict";
const api_request = require("./request.js");
function addFocus(userId, focusUserId) {
  return api_request.request({
    url: "/focus/add",
    method: "get",
    params: {
      userId,
      focusUserId
    }
  });
}
function deleteFocus(userId, focusUserId) {
  return api_request.request({
    url: "/focus/delete",
    method: "get",
    params: {
      userId,
      focusUserId
    }
  });
}
function getFocusList(userId) {
  return api_request.request({
    url: "/focus/getList",
    method: "get",
    params: { userId }
  });
}
function getFansList(userId) {
  return api_request.request({
    url: "/focus/getFansList",
    method: "get",
    params: { userId }
  });
}
function checkFocusStatus(userId, focusUserId) {
  return api_request.request({
    url: "/focus/check",
    method: "get",
    params: {
      userId,
      focusUserId
    }
  });
}
exports.addFocus = addFocus;
exports.checkFocusStatus = checkFocusStatus;
exports.deleteFocus = deleteFocus;
exports.getFansList = getFansList;
exports.getFocusList = getFocusList;
