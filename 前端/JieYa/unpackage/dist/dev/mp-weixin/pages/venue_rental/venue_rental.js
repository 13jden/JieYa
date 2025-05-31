"use strict";
const common_vendor = require("../../common/vendor.js");
const api_venue = require("../../api/venue.js");
const _sfc_main = {
  __name: "venue_rental",
  setup(__props) {
    const venueId = common_vendor.ref(null);
    const venueName = common_vendor.ref("");
    const startTime = common_vendor.ref("");
    const endTime = common_vendor.ref("");
    const price = common_vendor.ref(0);
    const submitting = common_vendor.ref(false);
    common_vendor.onLoad((options) => {
      venueId.value = options.venueId;
      venueName.value = decodeURIComponent(options.venueName || "");
      startTime.value = decodeURIComponent(options.startTime || "");
      endTime.value = decodeURIComponent(options.endTime || "");
      price.value = options.price || 0;
      console.log("预约信息:", {
        venueId: venueId.value,
        venueName: venueName.value,
        startTime: startTime.value,
        endTime: endTime.value,
        price: price.value
      });
    });
    const formatDateTime = (dateTimeStr) => {
      if (!dateTimeStr)
        return "";
      const parts = dateTimeStr.split(" ");
      if (parts.length !== 2)
        return dateTimeStr;
      const dateParts = parts[0].split("-");
      if (dateParts.length !== 3)
        return dateTimeStr;
      return `${dateParts[1]}-${dateParts[2]} ${parts[1]}`;
    };
    const calculateDuration = () => {
      if (!startTime.value || !endTime.value)
        return 0;
      try {
        const start = new Date(startTime.value);
        const end = new Date(endTime.value);
        const diffMs = end - start;
        const diffHours = diffMs / (1e3 * 60 * 60);
        return diffHours.toFixed(1);
      } catch (e) {
        console.error("计算时长错误:", e);
        return 0;
      }
    };
    const submitBooking = async () => {
      if (submitting.value)
        return;
      if (!venueId.value || !startTime.value || !endTime.value) {
        common_vendor.index.showToast({
          title: "预约信息不完整",
          icon: "none"
        });
        return;
      }
      submitting.value = true;
      try {
        const result = await api_venue.createBooking(
          venueId.value,
          startTime.value,
          endTime.value
        );
        console.log("预约结果:", result);
        if (result.code === 1) {
          common_vendor.index.showToast({
            title: "预约成功",
            icon: "success"
          });
          setTimeout(() => {
            common_vendor.index.redirectTo({
              url: "/pages/order/order?activeTab=venue"
            });
          }, 1500);
        } else {
          common_vendor.index.showToast({
            title: result.msg || "预约失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("预约错误:", error);
        common_vendor.index.showToast({
          title: "预约失败，请重试",
          icon: "none"
        });
      } finally {
        submitting.value = false;
      }
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.t(venueName.value),
        b: common_vendor.t(formatDateTime(startTime.value)),
        c: common_vendor.t(formatDateTime(endTime.value)),
        d: common_vendor.t(calculateDuration()),
        e: common_vendor.t(price.value),
        f: common_vendor.t(price.value),
        g: common_vendor.t(submitting.value ? "提交中..." : "提交订单"),
        h: common_vendor.o(submitBooking),
        i: submitting.value
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-cd4f3854"], ["__file", "C:/Users/86182/Desktop/上班/解压小程序/前端/JieYa/pages/venue_rental/venue_rental.vue"]]);
wx.createPage(MiniProgramPage);
