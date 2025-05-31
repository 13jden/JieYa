"use strict";
const common_vendor = require("../../common/vendor.js");
const api_message = require("../../api/message.js");
const _sfc_main = {
  __name: "systemMessage",
  setup(__props) {
    const messageList = common_vendor.ref([]);
    const isLoading = common_vendor.ref(true);
    const isRefreshing = common_vendor.ref(false);
    const hasMore = common_vendor.ref(true);
    const currentPage = common_vendor.ref(1);
    const pageSize = common_vendor.ref(20);
    const messageType = common_vendor.ref("SYSTEM");
    const pageTitle = common_vendor.computed(() => {
      switch (messageType.value) {
        case "SYSTEM":
          return "系统消息";
        case "ADMIN":
          return "管理员消息";
        case "ORDER":
          return "订单消息";
        default:
          return "系统消息";
      }
    });
    const loadMessages = async (refresh = false) => {
      if (refresh) {
        currentPage.value = 1;
      }
      isLoading.value = true;
      try {
        const res = await api_message.getSystemMessages(currentPage.value, pageSize.value);
        if (res.code === 1) {
          if (refresh) {
            messageList.value = res.data.records || [];
            await api_message.markSystemMessagesAsRead();
          } else {
            messageList.value = [...messageList.value, ...res.data.records || []];
          }
          hasMore.value = currentPage.value * pageSize.value < res.data.total;
        }
      } catch (error) {
        console.error("获取系统消息失败:", error);
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
      loadMessages(true);
    };
    const loadMore = () => {
      if (hasMore.value && !isLoading.value) {
        currentPage.value++;
        loadMessages();
      }
    };
    const handleAction = (item) => {
      console.log("处理操作:", item);
      switch (item.actionType) {
        case "VIEW_NOTE":
          common_vendor.index.navigateTo({
            url: `/pages/note/note?id=${item.targetId}`
          });
          break;
        case "VIEW_ORDER":
          common_vendor.index.navigateTo({
            url: `/pages/order/orderDetail?id=${item.targetId}`
          });
          break;
        case "VIEW_USER":
          common_vendor.index.navigateTo({
            url: `/pages/user/user?id=${item.targetId}`
          });
          break;
      }
    };
    const getActionText = (actionType) => {
      switch (actionType) {
        case "VIEW_NOTE":
          return "查看笔记";
        case "VIEW_ORDER":
          return "查看订单";
        case "VIEW_USER":
          return "查看用户";
        default:
          return "查看详情";
      }
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
      const pages = getCurrentPages();
      const page = pages[pages.length - 1];
      if (page && page.options && page.options.type) {
        messageType.value = page.options.type.toUpperCase();
      }
      loadMessages(true);
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.f(messageList.value, (item, index, i0) => {
          return common_vendor.e({
            a: common_vendor.t(item.title || "系统通知"),
            b: common_vendor.t(formatTime(item.time)),
            c: common_vendor.t(item.content),
            d: item.actionType
          }, item.actionType ? {
            e: common_vendor.t(getActionText(item.actionType)),
            f: common_vendor.o(($event) => handleAction(item), index)
          } : {}, {
            g: index
          });
        }),
        b: hasMore.value && !isLoading.value
      }, hasMore.value && !isLoading.value ? {
        c: common_vendor.o(loadMore)
      } : {}, {
        d: isLoading.value
      }, isLoading.value ? {} : {}, {
        e: messageList.value.length === 0 && !isLoading.value
      }, messageList.value.length === 0 && !isLoading.value ? {
        f: common_vendor.t(pageTitle.value)
      } : {}, {
        g: common_vendor.o(onRefresh),
        h: isRefreshing.value
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-c119f252"], ["__file", "C:/Users/86182/Desktop/上班/解压小程序/前端/JieYa/pages/systemMessage/systemMessage.vue"]]);
wx.createPage(MiniProgramPage);
