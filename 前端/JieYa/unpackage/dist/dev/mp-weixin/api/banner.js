"use strict";
const api_request = require("./request.js");
function getBannerList() {
  return api_request.request({
    url: "/banner/list",
    method: "GET"
  });
}
exports.getBannerList = getBannerList;
