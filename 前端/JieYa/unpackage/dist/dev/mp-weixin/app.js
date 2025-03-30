"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const common_vendor = require("./common/vendor.js");
const utils_websocket = require("./utils/websocket.js");
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
}
const _sfc_main = {
  onLaunch() {
    console.log("App Launch");
    const token = common_vendor.index.getStorageSync("token");
    if (token) {
      const wsUrl = "ws://localhost:8082/websocket";
      setTimeout(() => {
        utils_websocket.webSocketService.connect(wsUrl, token);
        utils_websocket.webSocketService.on("message", (message) => {
          console.log("收到WebSocket消息:", message);
          if (message.type === "notification") {
            common_vendor.index.showToast({
              title: message.content,
              icon: "none"
            });
          } else if (message.type === "chat") {
            console.log("收到聊天消息:", message.content);
          }
        });
      }, 1e3);
    }
  },
  onShow: function() {
    console.log("App Show");
    const token = common_vendor.index.getStorageSync("token");
    if (token && !utils_websocket.webSocketService.isConnected()) {
      utils_websocket.webSocketService.connect("ws://localhost:8082/ws", token);
    }
  },
  onHide: function() {
    console.log("App Hide");
  }
};
const App = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/App.vue"]]);
function createApp() {
  const app = common_vendor.createSSRApp(App);
  return {
    app
  };
}
createApp().app.mount("#app");
exports.createApp = createApp;
