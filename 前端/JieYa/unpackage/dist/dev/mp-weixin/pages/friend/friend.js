"use strict";
const common_vendor = require("../../common/vendor.js");
if (!Array) {
  const _easycom_friend_item2 = common_vendor.resolveComponent("friend_item");
  _easycom_friend_item2();
}
const _easycom_friend_item = () => "../../components/friend_item/friend_item2.js";
if (!Math) {
  _easycom_friend_item();
}
const _sfc_main = {
  __name: "friend",
  setup(__props) {
    const friendList = common_vendor.ref([]);
    function loadListsFromSession() {
      const storedLists = common_vendor.wx$1.getStorageSync("friendList");
      if (storedLists) {
        friendList.value = JSON.parse(storedLists);
      }
    }
    function goMessage(userId) {
      console.log("跳转到消息页面，userId:", userId);
      common_vendor.index.navigateTo({
        url: `/pages/message/message?userId=${userId}`
      });
    }
    function goSystemMsg() {
      console.log("系统");
    }
    common_vendor.onMounted(() => {
      loadListsFromSession();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: _ctx.isLoading
      }, _ctx.isLoading ? {} : {
        b: common_vendor.o(goSystemMsg),
        c: common_vendor.p({
          username: "系统消息",
          userimage: "/static/system.png",
          message: "您的账户安全设置需要更新"
        }),
        d: common_vendor.f(friendList.value, (friend, k0, i0) => {
          return {
            a: common_vendor.o(($event) => goMessage(friend.userId), friend.userId),
            b: friend.userId,
            c: "5a48eb7b-1-" + i0,
            d: common_vendor.p({
              username: friend.username,
              userimage: friend.avatarUrl,
              message: friend.message,
              isNew: friend.isNew
            })
          };
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__file", "C:/Users/86182/Documents/HBuilderProjects/JieYa/pages/friend/friend.vue"]]);
wx.createPage(MiniProgramPage);
