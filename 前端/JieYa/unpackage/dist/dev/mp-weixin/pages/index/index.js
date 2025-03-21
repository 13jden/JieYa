"use strict";
const common_vendor = require("../../common/vendor.js");
const api_banner = require("../../api/banner.js");
const models_banner = require("../../models/banner.js");
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
    const bannerList = common_vendor.ref([]);
    const showQRCode = common_vendor.ref(false);
    async function fetchBannerData() {
      try {
        const res = await api_banner.getBannerList();
        if (res.code === 1 && res.data) {
          bannerList.value = res.data.map((item) => new models_banner.Banner(item)).sort((a, b) => a.sort - b.sort);
        }
      } catch (error) {
        console.error("获取Banner数据失败:", error);
        bannerList.value = [
          new models_banner.Banner({
            bannerId: 1,
            image: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/2.jpg",
            type: 3,
            on: 1,
            text: "心理健康小贴士",
            sort: 1
          }),
          new models_banner.Banner({
            bannerId: 2,
            image: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/3.jpg",
            type: 2,
            on: 1,
            text: "心理减压道具",
            sort: 2
          }),
          new models_banner.Banner({
            bannerId: 3,
            image: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/backgroundImage.jpg",
            type: 1,
            on: 1,
            text: "舒适咨询环境",
            sort: 3
          })
        ];
      }
    }
    function handleBannerClick(banner) {
      const url = banner.getJumpUrl();
      if (url) {
        common_vendor.index.navigateTo({
          url,
          fail: () => {
            common_vendor.index.showToast({
              title: "页面跳转失败",
              icon: "none"
            });
          }
        });
      }
    }
    common_vendor.onMounted(() => {
      fetchBannerData();
    });
    function weChatAccount() {
      showQRCode.value = true;
    }
    function closeModal() {
      console.log("dianji");
      showQRCode.value = false;
    }
    function navigateToProp() {
      common_vendor.index.navigateTo({
        url: "/pages/prop/prop"
      });
    }
    function navigateToVenue() {
      common_vendor.index.navigateTo({
        url: "/pages/venue/venue"
      });
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.f(bannerList.value, (banner, index, i0) => {
          return common_vendor.e({
            a: banner.image,
            b: banner.text
          }, banner.text ? {
            c: common_vendor.t(banner.text),
            d: common_vendor.t(banner.getTypeText())
          } : {}, {
            e: banner.bannerId,
            f: common_vendor.o(($event) => handleBannerClick(banner), banner.bannerId)
          });
        }),
        b: common_vendor.o(navigateToProp),
        c: common_vendor.o(navigateToVenue),
        d: common_vendor.o(($event) => weChatAccount()),
        e: showQRCode.value
      }, showQRCode.value ? {
        f: common_vendor.o(closeModal)
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-1cf27b2a"], ["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/pages/index/index.vue"]]);
wx.createPage(MiniProgramPage);
