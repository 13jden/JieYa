"use strict";
const api_request = require("./request.js");
function getPropList() {
  return api_request.request({
    url: "/prop/list",
    method: "GET"
  });
}
function getPropDetail(id) {
  return api_request.request({
    url: `/prop/detail/${id}`,
    method: "GET"
  });
}
function getAvailableProps() {
  return api_request.request({
    url: "/prop/available",
    method: "GET"
  });
}
function rentProp(propId) {
  return api_request.request({
    url: `/prop-rental/rent?propId=${propId}`,
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded"
    }
  });
}
function returnProp(rentalId) {
  return api_request.request({
    url: `/prop-rental/return/${rentalId}`,
    method: "POST"
  });
}
function getRentalHistory() {
  return api_request.request({
    url: "/prop-rental/history",
    method: "GET"
  });
}
exports.getAvailableProps = getAvailableProps;
exports.getPropDetail = getPropDetail;
exports.getPropList = getPropList;
exports.getRentalHistory = getRentalHistory;
exports.rentProp = rentProp;
exports.returnProp = returnProp;
