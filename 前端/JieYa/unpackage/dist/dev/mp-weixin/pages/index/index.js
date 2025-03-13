"use strict";
const common_vendor = require("../../common/vendor.js");
if (!Array) {
  const _easycom_information2 = common_vendor.resolveComponent("information");
  _easycom_information2();
}
const _easycom_information = () => "../../components/information/information2.js";
if (!Math) {
  _easycom_information();
}
const _sfc_main = {
  __name: "index",
  setup(__props) {
    const imageList = [
      { url: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/2.jpg" },
      { url: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/3.jpg" },
      { url: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/backgroundImage.jpg" }
    ];
    common_vendor.ref([]);
    const showQRCode = common_vendor.ref(false);
    function weChatAccount() {
      showQRCode.value = true;
    }
    function closeModal() {
      console.log("dianji");
      showQRCode.value = false;
    }
    common_vendor.onMounted(() => {
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.f(imageList, (image, index, i0) => {
          return {
            a: image.url,
            b: index
          };
        }),
        b: common_vendor.o(($event) => weChatAccount()),
        c: showQRCode.value
      }, showQRCode.value ? {
        d: common_vendor.o(closeModal)
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-1cf27b2a"], ["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/pages/index/index.vue"]]);
wx.createPage(MiniProgramPage);
