"use strict";
const common_vendor = require("../../common/vendor.js");
const api_message = require("../../api/message.js");
const utils_websocket = require("../../utils/websocket.js");
if (!Math) {
  friendItem();
}
const friendItem = () => "../../components/friend_item/friend_item2.js";
const __default__ = {
  onShow() {
    console.log("Friend页面显示 (Options API)");
    this.loadMessages && this.loadMessages();
  },
  onHide() {
    console.log("Friend页面隐藏 (Options API)");
  },
  onTabItemTap() {
    console.log("Tab点击刷新");
    this.loadMessages && this.loadMessages();
  }
};
const _sfc_main = /* @__PURE__ */ Object.assign(__default__, {
  __name: "friend",
  setup(__props, { expose: __expose }) {
    const messageList = common_vendor.ref([]);
    const isLoading = common_vendor.ref(true);
    const isRefreshing = common_vendor.ref(false);
    common_vendor.computed(() => {
      return messageList.value.filter(
        (msg) => msg.type === "SYSTEM-USER" || msg.type === "ADMIN-USER" || msg.type === "ORDER-USER"
      );
    });
    common_vendor.computed(() => {
      return messageList.value.filter((msg) => msg.type === "USER-USER");
    });
    const lastLoadTime = common_vendor.ref(0);
    const loadMessages = async (forceRefresh = false) => {
      const now = Date.now();
      if (!forceRefresh && now - lastLoadTime.value < 2e3) {
        console.log("加载过于频繁，跳过");
        return;
      }
      lastLoadTime.value = now;
      console.log("正在刷新消息列表...");
      isLoading.value = true;
      try {
        const res = await api_message.getUserMessagesList(1, 50);
        console.log("获取到的消息数据:", res);
        if (res.code === 1) {
          messageList.value = res.data.records || [];
          let totalUnread = 0;
          messageList.value.forEach((msg) => {
            totalUnread += msg.notReadCount || 0;
          });
          const app = getApp();
          if (app) {
            app.globalData.unreadMessageCount = totalUnread;
            if (totalUnread > 0) {
              common_vendor.index.setTabBarBadge({
                index: 3,
                text: totalUnread.toString()
              });
            } else {
              try {
                common_vendor.index.removeTabBarBadge({
                  index: 3
                });
              } catch (e) {
                console.error("移除TabBar角标失败:", e);
              }
            }
          }
        }
      } catch (error) {
        console.error("获取消息列表失败:", error);
      } finally {
        isLoading.value = false;
        isRefreshing.value = false;
      }
    };
    const onRefresh = () => {
      isRefreshing.value = true;
      loadMessages();
    };
    const getAvatarByType = (type) => {
      switch (type) {
        case "SYSTEM-USER":
          return "/static/system-message.png";
        case "ADMIN-USER":
          return "/static/admin_avatar.png";
        case "ORDER-USER":
          return "/static/order-message.png";
        default:
          return "/static/default-avatar.png";
      }
    };
    const getNameByType = (type) => {
      switch (type) {
        case "SYSTEM-USER":
          return "系统消息";
        case "ADMIN-USER":
          return "管理员消息";
        case "ORDER-USER":
          return "订单消息";
        default:
          return "未知消息";
      }
    };
    const formatTime = (time) => {
      if (!time)
        return "";
      const msgDate = new Date(time);
      const now = /* @__PURE__ */ new Date();
      const diff = now - msgDate;
      if (diff < 24 * 60 * 60 * 1e3 && msgDate.getDate() === now.getDate()) {
        const hours = msgDate.getHours().toString().padStart(2, "0");
        const mins = msgDate.getMinutes().toString().padStart(2, "0");
        return `${hours}:${mins}`;
      }
      if (diff < 7 * 24 * 60 * 60 * 1e3) {
        const weekdays = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
        return weekdays[msgDate.getDay()];
      }
      return `${msgDate.getMonth() + 1}月${msgDate.getDate()}日`;
    };
    const navigateToAiChat = () => {
      common_vendor.index.navigateTo({
        url: "/pages/message/message"
      });
    };
    const handleItemTap = (item) => {
      console.log("点击消息项:", item);
      const index = messageList.value.findIndex(
        (msg) => msg.type === item.type && (item.type === "ADMIN-USER" && msg.type === "ADMIN-USER" || item.type === "USER-USER" && msg.userId === item.userId || item.type === "SYSTEM-USER" && msg.type === "SYSTEM-USER" || item.type === "ORDER-USER" && msg.type === "ORDER-USER")
      );
      if (index !== -1) {
        const oldNotReadCount = messageList.value[index].notReadCount || 0;
        messageList.value[index].notReadCount = 0;
        messageList.value = [...messageList.value];
        const app = getApp();
        if (app && app.globalData) {
          if (app.globalData.unreadMessageCount >= oldNotReadCount) {
            app.globalData.unreadMessageCount -= oldNotReadCount;
          } else {
            app.globalData.unreadMessageCount = 0;
          }
          if (app.globalData.unreadMessageCount > 0) {
            common_vendor.index.setTabBarBadge({
              index: 3,
              // 消息Tab的索引
              text: app.globalData.unreadMessageCount.toString()
            });
          } else {
            try {
              common_vendor.index.removeTabBarBadge({
                index: 3
              });
            } catch (e) {
              console.error("移除TabBar角标失败:", e);
            }
          }
        }
      }
      if (item.type === "ADMIN-USER") {
        api_message.markAsRead("admin").catch((err) => console.error("标记管理员消息已读失败:", err));
      } else if (item.type === "USER-USER" && item.userId) {
        api_message.markAsRead(item.userId).catch((err) => console.error("标记用户消息已读失败:", err));
      } else if (item.type === "SYSTEM-USER") {
        api_message.markAsRead("system").catch((err) => console.error("标记系统消息已读失败:", err));
      } else if (item.type === "ORDER-USER") {
        api_message.markAsRead("order").catch((err) => console.error("标记订单消息已读失败:", err));
      }
      if (item.type === "USER-USER") {
        common_vendor.index.navigateTo({
          url: `/pages/friendMessage/friendMessage?userId=${item.userId}`
        });
      } else if (item.type === "ADMIN-USER") {
        common_vendor.index.navigateTo({
          url: "/pages/friendMessage/friendMessage?userId=admin"
        });
      } else if (item.type === "SYSTEM-USER") {
        common_vendor.index.navigateTo({
          url: "/pages/systemMessage/systemMessage"
        });
      } else if (item.type === "ORDER-USER") {
        common_vendor.index.navigateTo({
          url: "/pages/orderMessage/orderMessage"
        });
      }
    };
    common_vendor.onMounted(() => {
      console.log("Friend页面挂载");
      loadMessages();
      setupWebSocketListener();
    });
    common_vendor.onBeforeUnmount(() => {
      console.log("Friend页面卸载前");
      removeWebSocketListener();
    });
    const setupWebSocketListener = () => {
      utils_websocket.webSocketService.on("chat", (message) => {
        console.log("Friend页面收到消息:", message);
        loadMessages();
      });
    };
    const removeWebSocketListener = () => {
      utils_websocket.webSocketService.off("chat");
    };
    const onTabActivated = () => {
      console.log("Tab页面被激活");
      loadMessages(true);
    };
    __expose({
      loadMessages,
      onTabActivated
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(navigateToAiChat),
        b: common_vendor.p({
          username: "心理咨询",
          userimage: "/static/aiChat.png",
          message: "点击开始与心理顾问对话",
          isNew: false
        }),
        c: messageList.value.length > 0
      }, messageList.value.length > 0 ? {
        d: common_vendor.f(messageList.value, (item, index, i0) => {
          return {
            a: item.id || index,
            b: common_vendor.o(($event) => handleItemTap(item), item.id || index),
            c: "b7e6a9a8-1-" + i0,
            d: common_vendor.p({
              username: item.type === "USER-USER" ? item.userName : getNameByType(item.type),
              userimage: item.type === "USER-USER" ? item.avatarUrl || "/static/default-avatar.png" : getAvatarByType(item.type),
              message: item.lastContent,
              date: formatTime(item.time),
              badgeCount: item.notReadCount
            })
          };
        })
      } : {}, {
        e: messageList.value.length === 0 && !isLoading.value
      }, messageList.value.length === 0 && !isLoading.value ? {} : {}, {
        f: common_vendor.o(onRefresh),
        g: isRefreshing.value,
        h: isLoading.value
      }, isLoading.value ? {} : {});
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-b7e6a9a8"], ["__file", "C:/Users/86182/Desktop/上班/解压小程序/前端/JieYa/pages/friend/friend.vue"]]);
wx.createPage(MiniProgramPage);
