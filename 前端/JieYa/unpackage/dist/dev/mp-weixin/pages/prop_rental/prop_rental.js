"use strict";
const common_vendor = require("../../common/vendor.js");
const api_prop = require("../../api/prop.js");
const _sfc_main = {
  __name: "prop_rental",
  setup(__props) {
    const propDetail = common_vendor.ref({});
    const loading = common_vendor.ref(false);
    common_vendor.onLoad((options) => {
      const propId = options.id;
      if (propId) {
        fetchPropDetail(propId);
      } else {
        common_vendor.index.showToast({
          title: "未找到道具ID",
          icon: "none"
        });
        setTimeout(() => {
          common_vendor.index.navigateBack();
        }, 1500);
      }
    });
    const fetchPropDetail = async (id) => {
      loading.value = true;
      try {
        common_vendor.index.showLoading({ title: "加载中..." });
        const result = await api_prop.getPropDetail(id);
        if (result.code === 1 && result.data) {
          propDetail.value = result.data;
        } else {
          common_vendor.index.showToast({
            title: result.msg || "获取道具详情失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("获取道具详情失败:", error);
        common_vendor.index.showToast({
          title: "获取道具详情失败",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
        loading.value = false;
      }
    };
    const confirmRental = async () => {
      if (!propDetail.value.id) {
        common_vendor.index.showToast({
          title: "道具信息无效",
          icon: "none"
        });
        return;
      }
      try {
        common_vendor.index.showLoading({ title: "处理中..." });
        const result = await api_prop.rentProp(propDetail.value.id);
        if (result.code === 1) {
          common_vendor.index.showToast({
            title: "租借成功",
            icon: "success"
          });
          setTimeout(() => {
            common_vendor.index.redirectTo({
              url: "/pages/order/order?activeTab=prop"
            });
          }, 1500);
        } else {
          common_vendor.index.showToast({
            title: result.msg || "租借失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("租借道具失败:", error);
        common_vendor.index.showToast({
          title: "租借道具失败",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
      }
    };
    return (_ctx, _cache) => {
      var _a;
      return common_vendor.e({
        a: propDetail.value.coverImage
      }, propDetail.value.coverImage ? {
        b: propDetail.value.coverImage
      } : {}, {
        c: common_vendor.t(propDetail.value.name),
        d: common_vendor.t(propDetail.value.description),
        e: common_vendor.t(propDetail.value.price),
        f: common_vendor.t((_a = propDetail.value.price) == null ? void 0 : _a.toFixed(2)),
        g: common_vendor.o(confirmRental)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-2016118d"], ["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/pages/prop_rental/prop_rental.vue"]]);
wx.createPage(MiniProgramPage);
