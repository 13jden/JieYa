"use strict";
const common_vendor = require("../../common/vendor.js");
const api_message = require("../../api/message.js");
const _sfc_main = {
  __name: "orderMessage",
  setup(__props) {
    const orderMessages = common_vendor.ref([]);
    const isLoading = common_vendor.ref(true);
    const isRefreshing = common_vendor.ref(false);
    const hasMore = common_vendor.ref(true);
    const currentPage = common_vendor.ref(1);
    const pageSize = common_vendor.ref(20);
    const loadOrderMessages = async (refresh = false) => {
      if (refresh) {
        currentPage.value = 1;
      }
      isLoading.value = true;
      try {
        const res = await api_message.getOrderMessages(currentPage.value, pageSize.value);
        if (res.code === 1) {
          if (refresh) {
            orderMessages.value = res.data.records || [];
            await api_message.markOrderMessagesAsRead();
          } else {
            orderMessages.value = [...orderMessages.value, ...res.data.records || []];
          }
          hasMore.value = currentPage.value * pageSize.value < res.data.total;
        }
      } catch (error) {
        console.error("获取订单消息失败:", error);
        common_vendor.index.showToast({
          title: "获取消息失败",
          icon: "none"
        });
      } finally {
        isLoading.value = false;
        isRefreshing.value = false;
      }
    };
    const onRefresh = () => {
      isRefreshing.value = true;
      loadOrderMessages(true);
    };
    const loadMore = () => {
      if (hasMore.value && !isLoading.value) {
        currentPage.value++;
        loadOrderMessages();
      }
    };
    const viewOrderDetail = (item) => {
      common_vendor.index.navigateTo({
        url: `/pages/order/orderDetail?id=${item.orderId}`
      });
    };
    const getStatusText = (status) => {
      const statusMap = {
        "PENDING": "待支付",
        "PAID": "已支付",
        "PROCESSING": "处理中",
        "COMPLETED": "已完成",
        "CANCELLED": "已取消",
        "REFUNDING": "退款中",
        "REFUNDED": "已退款"
      };
      return statusMap[status] || "未知状态";
    };
    const getStatusClass = (status) => {
      const classMap = {
        "PENDING": "status-pending",
        "PAID": "status-paid",
        "PROCESSING": "status-processing",
        "COMPLETED": "status-completed",
        "CANCELLED": "status-cancelled",
        "REFUNDING": "status-refunding",
        "REFUNDED": "status-refunded"
      };
      return classMap[status] || "";
    };
    const formatTime = (time) => {
      if (!time)
        return "";
      const date = new Date(time);
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, "0");
      const day = date.getDate().toString().padStart(2, "0");
      const hours = date.getHours().toString().padStart(2, "0");
      const minutes = date.getMinutes().toString().padStart(2, "0");
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    };
    common_vendor.onMounted(() => {
      loadOrderMessages(true);
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.f(orderMessages.value, (item, index, i0) => {
          return {
            a: common_vendor.t(getStatusText(item.status)),
            b: common_vendor.n(getStatusClass(item.status)),
            c: common_vendor.t(formatTime(item.time)),
            d: common_vendor.t(item.title || "订单通知"),
            e: common_vendor.t(item.content),
            f: common_vendor.t(item.orderNo || "未知"),
            g: index,
            h: common_vendor.o(($event) => viewOrderDetail(item), index)
          };
        }),
        b: hasMore.value && !isLoading.value
      }, hasMore.value && !isLoading.value ? {
        c: common_vendor.o(loadMore)
      } : {}, {
        d: isLoading.value
      }, isLoading.value ? {} : {}, {
        e: orderMessages.value.length === 0 && !isLoading.value
      }, orderMessages.value.length === 0 && !isLoading.value ? {} : {}, {
        f: common_vendor.o(onRefresh),
        g: isRefreshing.value
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-a393d756"], ["__file", "C:/Users/86182/Desktop/上班/解压小程序/前端/JieYa/pages/orderMessage/orderMessage.vue"]]);
wx.createPage(MiniProgramPage);
