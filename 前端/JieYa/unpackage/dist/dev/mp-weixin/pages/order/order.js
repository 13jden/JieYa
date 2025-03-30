"use strict";
const common_vendor = require("../../common/vendor.js");
const api_venue = require("../../api/venue.js");
const api_prop = require("../../api/prop.js");
const _sfc_main = {
  __name: "order",
  setup(__props) {
    const activeTab = common_vendor.ref("venue");
    const venueOrders = common_vendor.ref([]);
    const propRentals = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    const showReturnModal = common_vendor.ref(false);
    const currentRental = common_vendor.ref({});
    const showVenueDetailModal = common_vendor.ref(false);
    const currentVenue = common_vendor.ref({});
    const showPropDetailModal = common_vendor.ref(false);
    const currentProp = common_vendor.ref({});
    const switchTab = (tab) => {
      activeTab.value = tab;
      if (tab === "venue") {
        fetchVenueOrders();
      } else if (tab === "prop") {
        fetchPropRentals();
      }
    };
    common_vendor.onLoad((options) => {
      console.log("订单页面参数:", options);
      if (options.activeTab) {
        activeTab.value = options.activeTab;
      }
      if (activeTab.value === "venue") {
        fetchVenueOrders();
      } else {
        fetchPropRentals();
      }
    });
    common_vendor.onShow(() => {
      if (activeTab.value === "venue") {
        fetchVenueOrders();
      } else {
        fetchPropRentals();
      }
    });
    const fetchVenueOrders = async () => {
      loading.value = true;
      try {
        const result = await api_venue.getBookingHistory();
        if (result.code === 1 && result.data) {
          venueOrders.value = result.data;
        } else {
          venueOrders.value = [];
        }
      } catch (error) {
        console.error("获取场地预约历史失败:", error);
        common_vendor.index.showToast({
          title: "获取预约历史失败",
          icon: "none"
        });
      } finally {
        loading.value = false;
      }
    };
    const fetchPropRentals = async () => {
      loading.value = true;
      try {
        const result = await api_prop.getRentalHistory();
        if (result.code === 1 && result.data) {
          propRentals.value = result.data;
        } else {
          propRentals.value = [];
        }
      } catch (error) {
        console.error("获取道具租借历史失败:", error);
        common_vendor.index.showToast({
          title: "获取租借历史失败",
          icon: "none"
        });
      } finally {
        loading.value = false;
      }
    };
    const getStatusText = (status) => {
      const statusMap = {
        "PENDING": "待支付",
        "PAID": "已支付",
        "COMPLETED": "已完成",
        "CANCELLED": "已取消"
      };
      return statusMap[status] || status;
    };
    const getStatusClass = (status) => {
      if (!status)
        return "";
      switch (status) {
        case "PENDING":
        case "待支付":
          return "status-waiting-payment";
        case "CONFIRMED":
        case "已确认":
          return "status-confirmed";
        case "COMPLETED":
        case "已完成":
          return "status-completed";
        case "CANCELLED":
        case "已取消":
          return "status-cancelled";
        default:
          return "";
      }
    };
    const getRentalStatusClass = (status) => {
      const classMap = {
        "ACTIVE": "status-active",
        "RETURNED": "status-returned",
        "OVERDUE": "status-overdue"
      };
      return classMap[status] || "";
    };
    const formatTime = (timeStr) => {
      if (!timeStr)
        return "未知时间";
      const date = new Date(timeStr);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      const hours = String(date.getHours()).padStart(2, "0");
      const minutes = String(date.getMinutes()).padStart(2, "0");
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    };
    const calculateDuration = (startTimeStr) => {
      if (!startTimeStr)
        return "未知";
      const startTime = new Date(startTimeStr);
      const now = /* @__PURE__ */ new Date();
      const diffMs = now - startTime;
      if (diffMs < 10 * 60 * 1e3) {
        return "不足10分钟";
      }
      const diffMinutes = Math.floor(diffMs / (60 * 1e3));
      const diffHours = Math.floor(diffMs / (60 * 60 * 1e3));
      const diffDays = Math.floor(diffMs / (24 * 60 * 60 * 1e3));
      if (diffDays > 0) {
        return `${diffDays}天${diffHours % 24}小时`;
      } else if (diffHours > 0) {
        return `${diffHours}小时${diffMinutes % 60}分钟`;
      } else {
        return `${diffMinutes}分钟`;
      }
    };
    const estimatePrice = (rental) => {
      if (!rental || !rental.startDate || !rental.propPrice) {
        return "¥0.00";
      }
      const startTime = new Date(rental.startDate);
      const now = /* @__PURE__ */ new Date();
      const diffMs = now - startTime;
      if (diffMs < 10 * 60 * 1e3) {
        return "¥0.00";
      }
      const diffDays = Math.ceil(diffMs / (24 * 60 * 60 * 1e3));
      const price = diffDays * rental.propPrice;
      return `¥${price.toFixed(2)}`;
    };
    const returnRental = (rentalId) => {
      const rental = propRentals.value.find((item) => item.id === rentalId);
      if (!rental)
        return;
      currentRental.value = rental;
      showReturnModal.value = true;
    };
    const confirmReturn = async () => {
      try {
        common_vendor.index.showLoading({ title: "处理中..." });
        const result = await api_prop.returnProp(currentRental.value.id);
        if (result.code === 1 && result.data) {
          common_vendor.index.showToast({
            title: "归还成功",
            icon: "success"
          });
          showReturnModal.value = false;
          fetchPropRentals();
        } else {
          common_vendor.index.showToast({
            title: result.msg || "归还失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("归还道具失败:", error);
        common_vendor.index.showToast({
          title: "归还道具失败",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
      }
    };
    const cancelOrder = async (orderId) => {
      try {
        common_vendor.index.showLoading({ title: "处理中..." });
        const result = await api_venue.cancelBooking(orderId);
        if (result.code === 1) {
          common_vendor.index.showToast({
            title: "取消成功",
            icon: "success"
          });
          fetchVenueOrders();
        } else {
          common_vendor.index.showToast({
            title: result.msg || "取消失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("取消预约失败:", error);
        common_vendor.index.showToast({
          title: "取消预约失败",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
      }
    };
    const payOrder = (orderId) => {
      common_vendor.index.navigateTo({
        url: `/pages/payment/payment?orderId=${orderId}`
      });
    };
    const showVenueDetail = (order) => {
      currentVenue.value = order;
      showVenueDetailModal.value = true;
    };
    const showPropDetail = (rental) => {
      currentProp.value = rental;
      showPropDetailModal.value = true;
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: activeTab.value === "venue" ? 1 : "",
        b: common_vendor.o(($event) => switchTab("venue")),
        c: activeTab.value === "prop" ? 1 : "",
        d: common_vendor.o(($event) => switchTab("prop")),
        e: activeTab.value === "venue"
      }, activeTab.value === "venue" ? common_vendor.e({
        f: venueOrders.value.length === 0
      }, venueOrders.value.length === 0 ? {} : {
        g: common_vendor.f(venueOrders.value, (order, index, i0) => {
          return common_vendor.e({
            a: common_vendor.t(getStatusText(order.status)),
            b: common_vendor.n(getStatusClass(order.status)),
            c: order.venueCoverImage
          }, order.venueCoverImage ? {
            d: order.venueCoverImage
          } : {}, {
            e: common_vendor.t(order.venueName),
            f: common_vendor.t(order.totalPrice.toFixed(2)),
            g: common_vendor.t(order.startTimeStr || ""),
            h: common_vendor.t(order.endTimeStr || ""),
            i: common_vendor.t(order.id),
            j: order.status === "PENDING" || order.status === "待支付"
          }, order.status === "PENDING" || order.status === "待支付" ? {
            k: common_vendor.o(($event) => cancelOrder(order.id), index)
          } : {}, {
            l: order.status === "PENDING" || order.status === "待支付"
          }, order.status === "PENDING" || order.status === "待支付" ? {
            m: common_vendor.o(($event) => payOrder(order.id), index)
          } : {}, {
            n: index,
            o: common_vendor.o(($event) => showVenueDetail(order), index)
          });
        })
      }) : {}, {
        h: activeTab.value === "prop"
      }, activeTab.value === "prop" ? common_vendor.e({
        i: propRentals.value.length === 0
      }, propRentals.value.length === 0 ? {} : {
        j: common_vendor.f(propRentals.value, (rental, index, i0) => {
          return common_vendor.e({
            a: common_vendor.t(rental.id),
            b: common_vendor.t(rental.status),
            c: common_vendor.n(getRentalStatusClass(rental.status)),
            d: rental.propCoverImage
          }, rental.propCoverImage ? {
            e: rental.propCoverImage
          } : {}, {
            f: common_vendor.t(rental.propName),
            g: common_vendor.t(formatTime(rental.startDate)),
            h: rental.endDate
          }, rental.endDate ? {
            i: common_vendor.t(formatTime(rental.endDate))
          } : {}, {
            j: rental.status === "租借中"
          }, rental.status === "租借中" ? {
            k: common_vendor.t(calculateDuration(rental.startDate))
          } : {}, {
            l: rental.totalPrice
          }, rental.totalPrice ? {
            m: common_vendor.t(rental.totalPrice.toFixed(2))
          } : {
            n: common_vendor.t(estimatePrice(rental))
          }, {
            o: rental.status === "租借中"
          }, rental.status === "租借中" ? {
            p: common_vendor.o(($event) => returnRental(rental.id), index)
          } : {}, {
            q: index,
            r: common_vendor.o(($event) => showPropDetail(rental), index)
          });
        })
      }) : {}, {
        k: showReturnModal.value
      }, showReturnModal.value ? {
        l: common_vendor.t(currentRental.value.propName),
        m: common_vendor.t(formatTime(currentRental.value.startDate)),
        n: common_vendor.t(calculateDuration(currentRental.value.startDate)),
        o: common_vendor.t(estimatePrice(currentRental.value)),
        p: common_vendor.o(($event) => showReturnModal.value = false),
        q: common_vendor.o(confirmReturn)
      } : {}, {
        r: showVenueDetailModal.value
      }, showVenueDetailModal.value ? common_vendor.e({
        s: common_vendor.o(($event) => showVenueDetailModal.value = false),
        t: currentVenue.value.venueCoverImage
      }, currentVenue.value.venueCoverImage ? {
        v: currentVenue.value.venueCoverImage
      } : {}, {
        w: common_vendor.t(currentVenue.value.venueName),
        x: common_vendor.t(currentVenue.value.venueLocation || currentVenue.value.location),
        y: common_vendor.t(currentVenue.value.startTimeStr || ""),
        z: common_vendor.t(currentVenue.value.endTimeStr || ""),
        A: common_vendor.t(getStatusText(currentVenue.value.status)),
        B: common_vendor.n(getStatusClass(currentVenue.value.status)),
        C: common_vendor.t(currentVenue.value.totalPrice ? currentVenue.value.totalPrice.toFixed(2) : "0.00"),
        D: common_vendor.t(currentVenue.value.id),
        E: common_vendor.t(currentVenue.value.createdAtStr || ""),
        F: currentVenue.value.venueContent
      }, currentVenue.value.venueContent ? {
        G: common_vendor.t(currentVenue.value.venueContent)
      } : {}, {
        H: common_vendor.o(($event) => showVenueDetailModal.value = false),
        I: currentVenue.value.status === "PENDING" || currentVenue.value.status === "待支付"
      }, currentVenue.value.status === "PENDING" || currentVenue.value.status === "待支付" ? {
        J: common_vendor.o(($event) => payOrder(currentVenue.value.id))
      } : {}) : {}, {
        K: showPropDetailModal.value
      }, showPropDetailModal.value ? common_vendor.e({
        L: common_vendor.o(($event) => showPropDetailModal.value = false),
        M: currentProp.value.propCoverImage
      }, currentProp.value.propCoverImage ? {
        N: currentProp.value.propCoverImage
      } : {}, {
        O: common_vendor.t(currentProp.value.propName),
        P: common_vendor.t(currentProp.value.status),
        Q: common_vendor.n(getRentalStatusClass(currentProp.value.status)),
        R: common_vendor.t(formatTime(currentProp.value.startDate)),
        S: currentProp.value.endDate
      }, currentProp.value.endDate ? {
        T: common_vendor.t(formatTime(currentProp.value.endDate))
      } : {}, {
        U: currentProp.value.status === "租借中"
      }, currentProp.value.status === "租借中" ? {
        V: common_vendor.t(calculateDuration(currentProp.value.startDate))
      } : {}, {
        W: currentProp.value.totalPrice
      }, currentProp.value.totalPrice ? {
        X: common_vendor.t(currentProp.value.totalPrice.toFixed(2))
      } : {
        Y: common_vendor.t(estimatePrice(currentProp.value))
      }, {
        Z: common_vendor.t(currentProp.value.id),
        aa: common_vendor.o(($event) => showPropDetailModal.value = false),
        ab: currentProp.value.status === "租借中"
      }, currentProp.value.status === "租借中" ? {
        ac: common_vendor.o(($event) => returnRental(currentProp.value.id))
      } : {}) : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/pages/order/order.vue"]]);
wx.createPage(MiniProgramPage);
