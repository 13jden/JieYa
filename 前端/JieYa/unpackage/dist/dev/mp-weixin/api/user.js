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
    header: {
      "content-type": "application/x-www-form-urlencoded"
    },
    data
  });
}
function login(data) {
  return api_request.request({
    url: "/user/login",
    method: "POST",
    header: {
      "content-type": "application/x-www-form-urlencoded"
    },
    data
  }).then((res) => {
    if (res.code === 1 && res.data) {
      common_vendor.index.setStorageSync("token", res.data.token);
      common_vendor.index.setStorageSync("userInfo", res.data);
    }
    return res;
  });
}
function updateUserInfo(data, avatarFile) {
  if (avatarFile && avatarFile.path) {
    return api_request.request({
      url: "/user/update",
      method: "POST",
      filePath: avatarFile.path,
      name: "avatar",
      formData: data
      // 其他数据作为formData发送
    }).then((res) => {
      if (res.code === 1) {
        updateLocalUserInfo(data, res.data);
      }
      return res;
    });
  } else {
    return api_request.request({
      url: "/user/update",
      method: "POST",
      header: {
        "content-type": "application/x-www-form-urlencoded"
      },
      data
    }).then((res) => {
      if (res.code === 1) {
        updateLocalUserInfo(data, res.data);
      }
      return res;
    });
  }
}
function updateLocalUserInfo(updatedData, responseData) {
  try {
    const userInfo = common_vendor.index.getStorageSync("userInfo") || {};
    const updatedUserInfo = {
      ...userInfo,
      // 保留原有信息
      ...updatedData
      // 添加更新的字段
    };
    if (responseData) {
      if (responseData.avatar) {
        updatedUserInfo.avatar = responseData.avatar;
      }
      if (responseData.nickName)
        updatedUserInfo.nickName = responseData.nickName;
      if (responseData.sex !== void 0)
        updatedUserInfo.sex = responseData.sex;
      if (responseData.birthday)
        updatedUserInfo.birthday = responseData.birthday;
      if (responseData.school)
        updatedUserInfo.school = responseData.school;
      if (responseData.personIntruduction)
        updatedUserInfo.personIntruduction = responseData.personIntruduction;
    }
    common_vendor.index.setStorageSync("userInfo", updatedUserInfo);
    console.log("本地用户信息已更新:", updatedUserInfo);
  } catch (error) {
    console.error("更新本地用户信息失败:", error);
  }
}
exports.getVerificationCode = getVerificationCode;
exports.login = login;
exports.register = register;
exports.updateUserInfo = updateUserInfo;
