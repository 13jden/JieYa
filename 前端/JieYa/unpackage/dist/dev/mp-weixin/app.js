"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const common_vendor = require("./common/vendor.js");
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
}
const _sfc_main = {
  onLaunch() {
    console.log("App Launch");
    const defaultLists = [
      { nid: "N101", username: "Alice", title: "探索大自然", coverImage: "https://picsum.photos/300/400?random=1", userimage: "https://picsum.photos/50/50?random=1" },
      { nid: "N102", username: "Bob", title: "美食日记", coverImage: "https://picsum.photos/300/500?random=2", userimage: "https://picsum.photos/50/50?random=2" },
      { nid: "N103", username: "Charlie", title: "夜晚的城市", coverImage: "https://picsum.photos/300/450?random=3", userimage: "https://picsum.photos/50/50?random=3" },
      { nid: "N104", username: "David", title: "沙漠之旅", coverImage: "https://picsum.photos/300/350?random=4", userimage: "https://picsum.photos/50/50?random=4" },
      { nid: "N105", username: "Emma", title: "咖啡时光", coverImage: "https://picsum.photos/300/380?random=5", userimage: "https://picsum.photos/50/50?random=5" },
      { nid: "N106", username: "Frank", title: "登山记", coverImage: "https://picsum.photos/300/420?random=6", userimage: "https://picsum.photos/50/50?random=6" },
      { nid: "N107", username: "Grace", title: "海边日落", coverImage: "https://picsum.photos/300/370?random=7", userimage: "https://picsum.photos/50/50?random=7" },
      { nid: "N108", username: "Hank", title: "雨后漫步", coverImage: "https://picsum.photos/300/430?random=8", userimage: "https://picsum.photos/50/50?random=8" },
      { nid: "N109", username: "Ivy", title: "露营体验", coverImage: "https://picsum.photos/300/390?random=9", userimage: "https://picsum.photos/50/50?random=9" },
      { nid: "N110", username: "Jack", title: "艺术涂鸦", coverImage: "https://picsum.photos/300/410?random=10", userimage: "https://picsum.photos/50/50?random=10" }
    ];
    const friendList = [
      {
        userId: "0",
        username: "ai心理咨询",
        avatarUrl: "/static/aiChat.png",
        message: "你好",
        isNew: false
      },
      {
        userId: "1",
        username: "广志",
        avatarUrl: "https://tse4-mm.cn.bing.net/th/id/OIP-C.9NfK_6Ffv4FK0cWcX4fQoAHaHa?pid=ImgDet&w=474&h=474&rs=1",
        message: "你好",
        isNew: false
      },
      {
        userId: "2",
        username: "爷爷",
        avatarUrl: "https://tse2-mm.cn.bing.net/th/id/OIP-C.eg929lbj8_ayl3sPv3mJnQHaHa?pid=ImgDet&w=474&h=474&rs=1",
        message: "周末出去玩"
      }
    ];
    const messageList_1 = [
      { content: "你好", mymessage: 0 },
      { content: "广志你好", mymessage: 1 }
    ];
    const messageList_2 = [
      { content: "周末出去玩", mymessage: 0 }
    ];
    common_vendor.wx$1.setStorageSync("lists", JSON.stringify(defaultLists));
    common_vendor.wx$1.setStorageSync("friendList", JSON.stringify(friendList));
    common_vendor.wx$1.setStorageSync("messageList-1", messageList_1);
    common_vendor.wx$1.setStorageSync("messageList-2", messageList_2);
  },
  onShow: function() {
    console.log("App Show");
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
