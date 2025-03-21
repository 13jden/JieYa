"use strict";
const common_vendor = require("../common/vendor.js");
const api_request = require("./request.js");
function getVerificationCode() {
  return api_request.request({
    url: "/checkcode",
    method: "GET"
  }).then((res) => {
    if (res.code === 1 && res.data) {
      return {
        key: res.data.checkCodeKey,
        image: res.data.checkCode
        // 这已经是完整的data:image/png;base64格式
      };
    }
    return null;
  });
}
function register(data) {
  return api_request.request({
    url: "/user/register",
    method: "POST",
    data
  });
}
function login(data) {
  return api_request.request({
    url: "/user/login",
    method: "POST",
    data
  }).then((res) => {
    if (res.code === 1 && res.data) {
      common_vendor.wx$1.setStorageSync("token", res.data.token);
      common_vendor.wx$1.setStorageSync("userInfo", res.data);
    }
    return res;
  });
}
exports.getVerificationCode = getVerificationCode;
exports.login = login;
exports.register = register;
