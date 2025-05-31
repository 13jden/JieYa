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
    if (token == null) {
      setTimeout(() => {
        common_vendor.index.reLaunch({
          url: "/pages/login/login"
        });
      }, 1e3);
      return reject({ code: 401, message: "未登录，请先登录" });
    }
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
          if (res.statusCode === 50) {
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
      if (!header["content-type"] && !header["Content-Type"]) {
        header["content-type"] = "application/x-www-form-urlencoded";
      }
      let data = options.data;
      let contentType = header["content-type"] || header["Content-Type"] || "";
      contentType = contentType.toLowerCase();
      if (contentType.includes("application/x-www-form-urlencoded") && typeof data === "object" && data !== null) {
        const processedData = {};
        for (const key in data) {
          if (data.hasOwnProperty(key)) {
            if (Array.isArray(data[key])) {
              processedData[key] = JSON.stringify(data[key]);
            } else if (data[key] !== null && data[key] !== void 0) {
              processedData[key] = data[key];
            }
          }
        }
        let formData = "";
        for (let key in processedData) {
          if (processedData.hasOwnProperty(key)) {
            if (formData !== "") {
              formData += "&";
            }
            formData += encodeURIComponent(key) + "=" + encodeURIComponent(processedData[key] === null ? "" : processedData[key]);
          }
        }
        data = formData;
        console.log("表单数据:", data);
      } else if (contentType.includes("application/json") && typeof data === "object") {
        console.log("JSON数据:", data);
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
