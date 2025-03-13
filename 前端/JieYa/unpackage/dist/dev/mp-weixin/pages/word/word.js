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
    common_vendor.ref([]);
    common_vendor.onMounted(() => {
    });
    return (_ctx, _cache) => {
      return {
        a: common_vendor.p({
          type: "settings",
          size: "50rpx"
        }),
        b: common_vendor.p({
          type: "right"
        }),
        c: common_vendor.p({
          type: "right"
        }),
        d: common_vendor.p({
          type: "right"
        }),
        e: common_vendor.p({
          type: "right"
        })
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-ce61a269"], ["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/pages/word/word.vue"]]);
wx.createPage(MiniProgramPage);
