"use strict";
const common_vendor = require("../common/vendor.js");
const BASE_URL = "http://localhost:8081";
const objectToQueryString = (params) => {
  if (!params)
    return "";
  return "?" + Object.keys(params).map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`).join("&");
};
function request(options) {
  const queryString = options.params ? objectToQueryString(options.params) : "";
  return new Promise((resolve, reject) => {
    const token = common_vendor.index.getStorageSync("token");
    const header = {
      ...options.header,
      "Authorization": token ? `${token}` : ""
    };
    const isFileUpload = options.filePath && options.name;
    if (isFileUpload) {
      common_vendor.index.uploadFile({
        url: BASE_URL + options.url + queryString,
        filePath: options.filePath,
        name: options.name,
        formData: options.formData || {},
        header,
        success: (res) => {
          if (res.statusCode === 500) {
            handleUnauthorized();
            reject({ code: 500, message: "登录已过期，请重新登录" });
            return;
          }
          if (typeof res.data === "string") {
            try {
              const data = JSON.parse(res.data);
              if (data.code === 1) {
                resolve(data);
              } else {
                common_vendor.index.showToast({
                  title: data.message || "请求失败",
                  icon: "none"
                });
                reject(data);
              }
            } catch (e) {
              reject({ message: "返回数据格式错误" });
            }
          } else {
            resolve(res.data);
          }
        },
        fail: (err) => {
          console.error("上传失败:", err);
          common_vendor.index.showToast({
            title: "上传失败",
            icon: "none"
          });
          reject(err);
        }
      });
    } else {
      if (!header["content-type"]) {
        header["content-type"] = options.method === "GET" ? "application/x-www-form-urlencoded" : "application/json";
      }
      let data = options.data;
      if (header["content-type"] === "application/x-www-form-urlencoded" && typeof data === "object") {
        let formData = "";
        for (let key in data) {
          if (data.hasOwnProperty(key)) {
            if (formData !== "") {
              formData += "&";
            }
            formData += encodeURIComponent(key) + "=" + encodeURIComponent(data[key]);
          }
        }
        data = formData;
      }
      common_vendor.index.request({
        url: BASE_URL + options.url + queryString,
        method: options.method || "GET",
        data,
        header,
        success: (res) => {
          if (res.statusCode === 500) {
            handleUnauthorized();
            reject({ code: 500, message: "登录已过期，请重新登录" });
            return;
          }
          if (res.statusCode === 200) {
            if (res.data.code === 1) {
              resolve(res.data);
            } else {
              common_vendor.index.showToast({
                title: res.data.message || "请求失败",
                icon: "none"
              });
              reject(res.data);
            }
          } else {
            common_vendor.index.showToast({
              title: "请求失败: " + res.statusCode,
              icon: "none"
            });
            reject(res);
          }
        },
        fail: (err) => {
          console.error("请求失败:", err);
          common_vendor.index.showToast({
            title: "网络错误，请重试",
            icon: "none"
          });
          reject(err);
        }
      });
    }
  });
}
function handleUnauthorized() {
  common_vendor.index.removeStorageSync("token");
  common_vendor.index.removeStorageSync("userInfo");
  common_vendor.index.showToast({
    title: "登录已过期，请重新登录",
    icon: "none",
    duration: 2e3
  });
  setTimeout(() => {
    common_vendor.index.reLaunch({
      url: "/pages/login/login"
    });
  }, 1e3);
}
exports.request = request;
