"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  __name: "friendMessage",
  setup(__props) {
    const newMessage = common_vendor.ref("");
    const messageContainer = common_vendor.ref(null);
    common_vendor.ref(false);
    const myavatar = common_vendor.ref("https://jiayaya.oss-cn-hangzhou.aliyuncs.com/2.jpg");
    const favatar = common_vendor.ref("");
    const messagelist = common_vendor.ref([]);
    const userId = common_vendor.ref("");
    common_vendor.watch(messagelist, (newMessages) => {
      console.log("监听到消息列表更新：", newMessages);
      common_vendor.nextTick$1(scrollToBottom);
    }, { deep: true });
    common_vendor.onLoad((options) => {
      if (options.userId) {
        userId.value = options.userId;
        console.log(userId.value);
        loadFriendInfo();
        loadMessages();
        console.log(messagelist);
      }
    });
    common_vendor.onMounted(() => {
      loadFriendInfo();
      loadMessages();
      common_vendor.nextTick$1(() => {
        console.log("确保页面更新后的消息列表：", messagelist.value);
        scrollToBottom();
      });
    });
    const loadFriendInfo = () => {
      let friendList = common_vendor.wx$1.getStorageSync("friendList");
      try {
        friendList = JSON.parse(friendList);
      } catch (e) {
        console.error("解析 friendList 失败", e);
        friendList = [];
      }
      const friendArray = Array.isArray(friendList) ? friendList : [];
      const friend = friendArray.find((f) => f.userId === userId.value);
      if (friend) {
        favatar.value = friend.avatarUrl;
        console.log(favatar.value);
      } else {
        console.warn("未找到好友信息", userId.value);
      }
    };
    const updateFriend = (newMessage2) => {
      let friendList = common_vendor.wx$1.getStorageSync("friendList");
      try {
        friendList = JSON.parse(friendList);
      } catch (e) {
        console.error("解析 friendList 失败", e);
        friendList = [];
      }
      const friendArray = Array.isArray(friendList) ? friendList : [];
      const friend = friendArray.find((f) => f.userId === userId.value);
      if (friend) {
        friend.message = newMessage2;
        common_vendor.wx$1.setStorageSync("friendList", JSON.stringify(friendArray));
      } else {
        console.warn("未找到好友信息", userId.value);
      }
    };
    const loadMessages = () => {
      const storedMessages = common_vendor.wx$1.getStorageSync(`messageList-${userId.value}`) || [];
      messagelist.value = storedMessages;
      common_vendor.nextTick$1(scrollToBottom);
    };
    const send = () => {
      if (!newMessage.value.trim())
        return;
      messagelist.value.push({ content: newMessage.value, mymessage: 1 });
      common_vendor.wx$1.setStorageSync(`messageList-${userId.value}`, messagelist.value);
      updateFriend(newMessage.value);
      newMessage.value = "";
      scrollToBottom();
    };
    const scrollToBottom = () => {
      common_vendor.nextTick$1(() => {
        if (messageContainer.value) {
          messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
        }
      });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.f(messagelist.value, (message, index, i0) => {
          return common_vendor.e({
            a: message.mymessage === 0
          }, message.mymessage === 0 ? {
            b: favatar.value,
            c: common_vendor.t(message.content)
          } : {}, {
            d: message.mymessage === 1
          }, message.mymessage === 1 ? {
            e: common_vendor.t(message.content),
            f: myavatar.value
          } : {}, {
            g: index,
            h: message.mymessage === 1 ? 1 : "",
            i: message.mymessage === 0 ? 1 : ""
          });
        }),
        b: common_vendor.o((...args) => _ctx.handleScroll && _ctx.handleScroll(...args)),
        c: newMessage.value,
        d: common_vendor.o(($event) => newMessage.value = $event.detail.value),
        e: common_vendor.o(send)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-db650ebf"], ["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/pages/friendMessage/friendMessage.vue"]]);
wx.createPage(MiniProgramPage);
