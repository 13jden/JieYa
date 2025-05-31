"use strict";
const common_vendor = require("../common/vendor.js");
const api_request = require("./request.js");
function uploadMessageImage(file) {
  return api_request.request({
    url: "/image/message",
    method: "POST",
    filePath: file,
    name: "file",
    header: {
      "Authorization": common_vendor.index.getStorageSync("token")
    }
  });
}
exports.uploadMessageImage = uploadMessageImage;
