"use strict";
const common_vendor = require("../common/vendor.js");
const BASE_URL = "http://localhost:8081";
function request(options) {
  return new Promise((resolve, reject) => {
    const token = common_vendor.wx$1.getStorageSync("token");
    const header = {
      "Content-Type": options.method === "GET" ? "application/x-www-form-urlencoded" : "application/json",
      ...options.header
    };
    if (token) {
      header["Authorization"] = token;
    }
    common_vendor.index.request({
      url: BASE_URL + options.url,
      method: options.method || "GET",
      data: options.data,
      header,
      success: (res) => {
        if (res.statusCode === 200 && res.data.code !== 1) {
          common_vendor.index.showToast({
            title: res.data.message || "请求失败",
            icon: "none"
          });
          reject(res.data);
          return;
        }
        if (res.statusCode === 401) {
          common_vendor.wx$1.removeStorageSync("token");
          common_vendor.wx$1.removeStorageSync("userInfo");
          common_vendor.index.navigateTo({
            url: "/pages/login/login"
          });
          reject(res.data);
          return;
        }
        resolve(res.data);
      },
      fail: (err) => {
        common_vendor.index.showToast({
          title: "网络请求失败",
          icon: "none"
        });
        reject(err);
      }
    });
  });
}
exports.request = request;
