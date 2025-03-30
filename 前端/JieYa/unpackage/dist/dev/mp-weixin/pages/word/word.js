"use strict";
const common_vendor = require("../../common/vendor.js");
if (!Array) {
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  _easycom_uni_icons2();
}
const _easycom_uni_icons = () => "../../uni_modules/uni-icons/components/uni-icons/uni-icons.js";
if (!Math) {
  _easycom_uni_icons();
}
const _sfc_main = {
  __name: "word",
  setup(__props) {
    const userInfo = common_vendor.ref({});
    common_vendor.onMounted(() => {
      const storedUserInfo = common_vendor.index.getStorageSync("userInfo");
      if (storedUserInfo) {
        userInfo.value = storedUserInfo;
      }
    });
    const goToUpdateMe = () => {
      common_vendor.index.navigateTo({
        url: "/pages/updateMe/updateMe"
      });
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: userInfo.value.avatar || "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg",
        b: common_vendor.t(userInfo.value.nickName || "未设置昵称"),
        c: common_vendor.t(userInfo.value.userId || "未登录"),
        d: common_vendor.o(goToUpdateMe),
        e: common_vendor.p({
          type: "settings",
          size: "50rpx"
        }),
        f: common_vendor.t(userInfo.value.personIntruduction || "我的签名..."),
        g: userInfo.value.sex !== void 0 || userInfo.value.school
      }, userInfo.value.sex !== void 0 || userInfo.value.school ? common_vendor.e({
        h: userInfo.value.sex !== void 0
      }, userInfo.value.sex !== void 0 ? {
        i: common_vendor.p({
          type: "person",
          size: "30rpx",
          color: "#666"
        }),
        j: common_vendor.t(userInfo.value.sex ? "男" : "女")
      } : {}, {
        k: userInfo.value.school
      }, userInfo.value.school ? {
        l: common_vendor.p({
          type: "school",
          size: "30rpx",
          color: "#666"
        }),
        m: common_vendor.t(userInfo.value.school)
      } : {}) : {}, {
        n: common_vendor.p({
          type: "right"
        }),
        o: common_vendor.p({
          type: "right"
        }),
        p: common_vendor.p({
          type: "right"
        }),
        q: common_vendor.p({
          type: "right"
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-ce61a269"], ["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/pages/word/word.vue"]]);
wx.createPage(MiniProgramPage);
