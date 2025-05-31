"use strict";
const common_vendor = require("../../common/vendor.js");
const api_prop = require("../../api/prop.js");
const _sfc_main = {
  __name: "prop_detail",
  setup(__props) {
    const propDetail = common_vendor.ref({
      id: 0,
      name: "",
      description: "",
      price: 0,
      available: true,
      imageList: [],
      detailImageList: []
    });
    common_vendor.onLoad((option) => {
      const propId = Number(option.id || 1);
      fetchPropDetail(propId);
    });
    const fetchPropDetail = async (id) => {
      try {
        common_vendor.index.showLoading({
          title: "加载中..."
        });
        const result = await api_prop.getPropDetail(id);
        if (result.code === 1 && result.data) {
          result.data.imageList = result.data.imageList || [];
          result.data.detailImageList = result.data.detailImageList || [];
          propDetail.value = result.data;
        }
      } catch (error) {
        console.error("获取道具详情失败:", error);
        common_vendor.index.showToast({
          title: "获取道具详情失败",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
      }
    };
    const contactService = () => {
      common_vendor.index.showToast({
        title: "正在连接客服...",
        icon: "none"
      });
    };
    const rentProp = () => {
      common_vendor.index.navigateTo({
        url: `/pages/prop_rental/prop_rental?id=${propDetail.value.id}`
      });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.f(propDetail.value.imageList, (image, index, i0) => {
          return {
            a: image.imageUrl,
            b: index
          };
        }),
        b: common_vendor.t(propDetail.value.price),
        c: common_vendor.t(propDetail.value.name),
        d: common_vendor.t(propDetail.value.description),
        e: common_vendor.t(propDetail.value.available ? "可租借" : "暂不可租"),
        f: !propDetail.value.available ? 1 : "",
        g: common_vendor.f(propDetail.value.detailImageList, (detailImg, index, i0) => {
          return {
            a: index,
            b: detailImg.imageUrl
          };
        }),
        h: common_vendor.o(contactService),
        i: common_vendor.t(propDetail.value.available ? "立即租借" : "暂不可租"),
        j: !propDetail.value.available ? 1 : "",
        k: common_vendor.o(rentProp)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-5eaef006"], ["__file", "C:/Users/86182/Desktop/上班/解压小程序/前端/JieYa/pages/prop_detail/prop_detail.vue"]]);
wx.createPage(MiniProgramPage);
