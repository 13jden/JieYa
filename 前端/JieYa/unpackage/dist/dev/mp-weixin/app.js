"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const common_vendor = require("./common/vendor.js");
const utils_websocket = require("./utils/websocket.js");
const api_message = require("./api/message.js");
if (!Math) {
  "./pages/index/index.js";
  "./pages/login/login.js";
  "./pages/register/register.js";
  "./components/friend_item/friend_item.js";
  "./components/item/item.js";
  "./pages/publish/publish.js";
  "./pages/note/note.js";
  "./pages/word/word.js";
  "./pages/friend/friend.js";
  "./pages/find/find.js";
  "./pages/message/message.js";
  "./components/information/information.js";
  "./pages/friendMessage/friendMessage.js";
  "./pages/prop/prop.js";
  "./pages/venue/venue.js";
  "./components/prop-item/prop-item.js";
  "./components/venue-item/venue-item.js";
  "./pages/updateMe/updateMe.js";
  "./pages/prop_detail/prop_detail.js";
  "./pages/venue_detail/venue_detail.js";
  "./pages/detail_comment/detail_comment.js";
  "./pages/order/order.js";
  "./pages/prop_rental/prop_rental.js";
  "./pages/venue_rental/venue_rental.js";
  "./pages/user/user.js";
  "./pages/systemMessage/systemMessage.js";
  "./pages/orderMessage/orderMessage.js";
  "./components/message-notification/message-notification.js";
  "./pages/search/search.js";
}
const _sfc_main = {
  globalData: {
    unreadMessageCount: 0,
    shouldRefreshFriendPage: false,
    pendingBadgeCount: 0,
    hasNewMessage: false
  },
  onLaunch() {
    console.log("App Launch");
    const token = common_vendor.index.getStorageSync("token");
    if (token) {
      const wsUrl = "ws://localhost:8082/websocket";
      setTimeout(() => {
        utils_websocket.webSocketService.connect(wsUrl, token);
        utils_websocket.webSocketService.off("chat");
        utils_websocket.webSocketService.on("chat", (message) => {
          console.log("App收到WebSocket消息:", message);
          this.handleNewMessage(message);
          this.refreshCurrentPage(message);
        });
        utils_websocket.webSocketService.on("message", (message) => {
          console.log("收到WebSocket消息:", message);
          if (message.type !== "heartbeat" && message.type !== "heartbeat_response" && message.type !== "HEARTBEAT" && message.type !== "pong") {
            this.handleNewMessage(message);
          }
        });
        this.updateUnreadMessageCount();
      }, 1e3);
    }
    if (typeof utils_websocket.webSocketService.setApp === "function") {
      utils_websocket.webSocketService.setApp(this);
    }
    common_vendor.index.onTabBarMidButtonTap(() => {
      console.log("点击了TabBar");
      this.onTabChange();
    });
  },
  methods: {
    // 处理新消息
    handleNewMessage(message) {
      console.log("处理新消息:", message);
      let title = "";
      let content = message.content || "";
      if (message.type === "USER-USER") {
        title = message.userName || "好友消息";
      } else if (message.type === "SYSTEM-USER") {
        title = "系统消息";
      } else if (message.type === "ORDER-USER") {
        title = "订单消息";
      } else if (message.type === "ADMIN-USER") {
        title = "管理员消息";
      } else {
        title = "新消息";
      }
      this.showMessageNotification(title, content);
      this.updateUnreadMessageCount();
      const pages = getCurrentPages();
      if (pages.length > 0) {
        const currentPage = pages[pages.length - 1];
        if (currentPage.route === "pages/friend/friend") {
          if (typeof currentPage.$vm.loadMessages === "function") {
            currentPage.$vm.loadMessages();
          }
        }
      }
      this.globalData.shouldRefreshFriendPage = true;
      this.globalData.hasNewMessage = true;
    },
    // 显示消息通知
    showMessageNotification(title, content) {
      console.log("显示消息通知:", title, content);
      common_vendor.index.showToast({
        title: `${title}: ${content}`,
        icon: "none",
        duration: 3e3,
        position: "top"
      });
    },
    // 如果friend页面可见，刷新它
    refreshFriendPageIfVisible() {
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      if (currentPage && currentPage.route === "pages/friend/friend") {
        console.log("当前在消息页面，刷新列表");
        if (currentPage.$vm && typeof currentPage.$vm.loadMessages === "function") {
          currentPage.$vm.loadMessages();
          this.globalData.shouldRefreshFriendPage = false;
        }
      }
    },
    // 更新未读消息计数
    updateUnreadMessageCount() {
      api_message.getUnreadMessageCount().then((res) => {
        if (res.code === 1) {
          const count = res.data || 0;
          this.globalData.unreadMessageCount = count;
          this.globalData.pendingBadgeCount = count;
          this.safeSetTabBarBadge();
        }
      }).catch((err) => {
        console.error("获取未读消息数失败:", err);
      });
    },
    // 安全地设置TabBar角标的方法
    safeSetTabBarBadge() {
      const pages = getCurrentPages();
      if (pages.length === 0)
        return;
      const currentPage = pages[pages.length - 1];
      const tabBarPages = [
        "pages/index/index",
        "pages/find/find",
        "pages/publish/publish",
        "pages/friend/friend",
        "pages/word/word"
      ];
      const isTabBarPage = tabBarPages.some((path) => currentPage.route === path);
      if (isTabBarPage) {
        const count = this.globalData.pendingBadgeCount || 0;
        if (count > 0) {
          common_vendor.index.setTabBarBadge({
            index: 3,
            // 消息选项卡索引
            text: count.toString(),
            fail: (err) => {
              console.log("设置角标失败:", err);
            }
          });
          this.globalData.hasNewMessage = false;
        } else {
          try {
            common_vendor.index.hideTabBarRedDot({
              index: 3
            });
            common_vendor.index.removeTabBarBadge({
              index: 3
            });
          } catch (e) {
            console.log("移除角标时出错:", e);
          }
        }
      } else if (this.globalData.hasNewMessage) {
        console.log("当前不在TabBar页面，但有新消息，延迟设置角标");
      }
    },
    // 刷新当前页面
    refreshCurrentPage(message) {
      const pages = getCurrentPages();
      if (pages.length === 0)
        return;
      const currentPage = pages[pages.length - 1];
      if (currentPage.route === "pages/friend/friend") {
        console.log("当前在消息页面，刷新消息列表");
        if (currentPage.$vm && typeof currentPage.$vm.loadMessages === "function") {
          currentPage.$vm.loadMessages();
        }
      }
      if (currentPage.route === "pages/friendMessage/friendMessage") {
        const targetUserId = currentPage.options && currentPage.options.userId;
        if (targetUserId && (message.user === targetUserId || message.userId === targetUserId || targetUserId === "admin" && message.type === "ADMIN-USER")) {
          console.log("当前在聊天页面，收到来自当前对象的消息");
        }
      }
    },
    // 处理TabBar切换
    onTabChange() {
      setTimeout(() => {
        const pages = getCurrentPages();
        if (pages.length > 0) {
          const currentPage = pages[pages.length - 1];
          if (currentPage.route === "pages/friend/friend") {
            console.log("检测到切换到消息页面");
            if (currentPage.$vm && typeof currentPage.$vm.loadMessages === "function") {
              currentPage.$vm.loadMessages(true);
            }
          }
        }
      }, 100);
    }
  },
  onShow: function() {
    console.log("App Show");
    const token = common_vendor.index.getStorageSync("token");
    if (token) {
      if (!utils_websocket.webSocketService.isConnected()) {
        utils_websocket.webSocketService.connect("ws://localhost:8082/websocket", token);
      }
      this.updateUnreadMessageCount();
      setTimeout(() => {
        this.safeSetTabBarBadge();
      }, 300);
    }
  },
  onHide: function() {
    console.log("App Hide");
  }
};
const App = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__file", "C:/Users/86182/Desktop/上班/解压小程序/前端/JieYa/App.vue"]]);
function createApp() {
  const app = common_vendor.createSSRApp(App);
  return {
    app
  };
}
common_vendor.index.addInterceptor("switchTab", {
  success(e) {
    console.log("切换Tab:", e);
    const app = getApp();
    if (app && app.updateUnreadMessageCount) {
      setTimeout(() => {
        app.updateUnreadMessageCount();
      }, 200);
    }
  }
});
createApp().app.mount("#app");
exports.createApp = createApp;
