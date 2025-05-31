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
      console.log("点击轮播图:", banner);
      if (!banner) {
        console.error("无效的轮播图数据");
        return;
      }
      try {
        if (banner.contentId) {
          switch (banner.type) {
            case 1:
              common_vendor.index.navigateTo({
                url: `/pages/venue_detail/venue_detail?id=${banner.contentId}`
              });
              break;
            case 2:
              common_vendor.index.navigateTo({
                url: `/pages/prop_detail/prop_detail?id=${banner.contentId}`
              });
              break;
            case 3:
              common_vendor.index.navigateTo({
                url: `/pages/note/note?id=${banner.contentId}`
              });
              break;
            default:
              console.warn("未知的轮播图类型:", banner.type);
          }
        } else {
          switch (banner.type) {
            case 1:
              common_vendor.index.navigateTo({
                url: "/pages/venue/venue"
              });
              break;
            case 2:
              common_vendor.index.navigateTo({
                url: "/pages/prop/prop"
              });
              break;
            case 3:
              common_vendor.index.navigateTo({
                url: "/pages/find/find"
              });
              break;
            default:
              console.warn("未知的轮播图类型:", banner.type);
          }
        }
      } catch (error) {
        console.error("跳转失败:", error);
        common_vendor.index.showToast({
          title: "页面跳转失败",
          icon: "none"
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
    function navigateToProp(id) {
      console.log(id);
      common_vendor.index.navigateTo({
        url: `/pages/prop/prop`
      });
    }
    function navigateToVenue(id) {
      console.log(id);
      common_vendor.index.navigateTo({
        url: `/pages/venue/venue`
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
            d: common_vendor.t(banner.type === 1 ? "场地" : banner.type === 2 ? "道具" : "笔记")
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
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-1cf27b2a"], ["__file", "C:/Users/86182/Desktop/上班/解压小程序/前端/JieYa/pages/index/index.vue"]]);
wx.createPage(MiniProgramPage);
