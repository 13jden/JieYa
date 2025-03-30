"use strict";
const api_request = require("./request.js");
function getVenueList() {
  return api_request.request({
    url: "/venue/list",
    method: "GET"
  });
}
function getVenueDetail(id) {
  return api_request.request({
    url: `/venue/detail/${id}`,
    method: "GET"
  });
}
function getAvailableTimeSlots(params) {
  console.log("API调用参数:", params);
  return api_request.request({
    url: "/venue-booking/available-time",
    method: "GET",
    params: {
      venueId: params.venueId,
      date: params.date
    }
  });
}
function getVenueTags() {
  return api_request.request({
    url: "/venue-tag/list",
    method: "GET"
  });
}
function getBookingHistory() {
  return api_request.request({
    url: "/venue-booking/history",
    method: "GET"
  });
}
function cancelBooking(bookingId) {
  return api_request.request({
    url: `/venue-booking/cancel/${bookingId}`,
    method: "POST"
  });
}
function createBooking(venueId, startTime, endTime) {
  return api_request.request({
    url: "/venue-booking/create",
    method: "POST",
    params: { venueId, startTime, endTime }
  });
}
exports.cancelBooking = cancelBooking;
exports.createBooking = createBooking;
exports.getAvailableTimeSlots = getAvailableTimeSlots;
exports.getBookingHistory = getBookingHistory;
exports.getVenueDetail = getVenueDetail;
exports.getVenueList = getVenueList;
exports.getVenueTags = getVenueTags;
