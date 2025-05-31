"use strict";
const common_vendor = require("../../common/vendor.js");
const api_prop = require("../../api/prop.js");
if (!Math) {
  PropItem();
}
const PropItem = () => "../../components/prop-item/prop-item2.js";
const _sfc_main = {
  __name: "prop",
  setup(__props) {
    const searchText = common_vendor.ref("");
    const filters = common_vendor.ref(["全部", "可租借", "不可租借"]);
    const activeFilter = common_vendor.ref(0);
    const loading = common_vendor.ref(false);
    const noMoreData = common_vendor.ref(false);
    const propList = common_vendor.ref([]);
    common_vendor.ref(false);
    const filteredProps = common_vendor.computed(() => {
      return propList.value;
    });
    common_vendor.watch(searchText, (newVal) => {
      if (newVal.trim() === "") {
        resetSearch();
      }
    });
    const resetSearch = () => {
      propList.value = [];
      noMoreData.value = false;
      fetchPropList();
    };
    const fetchPropList = async () => {
      if (loading.value)
        return;
      loading.value = true;
      propList.value = [];
      try {
        let result;
        if (activeFilter.value === 1) {
          result = await api_prop.getAvailableProps();
        } else if (activeFilter.value === 2) {
          result = await api_prop.getPropList({
            available: false
          });
        } else {
          result = await api_prop.getPropList();
        }
        console.log("API返回道具数据:", result);
        if (result.code === 1 && result.data && result.data.length > 0) {
          propList.value = result.data;
          console.log("道具列表:", propList.value);
        } else {
          propList.value = [];
        }
      } catch (error) {
        console.error("获取道具列表失败:", error);
        common_vendor.index.showToast({
          title: "获取道具列表失败",
          icon: "none"
        });
      } finally {
        loading.value = false;
      }
    };
    function setFilter(index) {
      if (activeFilter.value === index)
        return;
      activeFilter.value = index;
      fetchPropList();
    }
    function onPropClick(id) {
      console.log("点击道具:", id);
      common_vendor.index.navigateTo({
        url: `/pages/prop_detail/prop_detail?id=${id}`
      });
    }
    function navigateToMyRentals() {
      common_vendor.index.navigateTo({
        url: "/pages/order/order?activeTab=prop"
      });
    }
    common_vendor.onMounted(() => {
      fetchPropList();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: searchText.value,
        b: common_vendor.o(($event) => searchText.value = $event.detail.value),
        c: common_vendor.f(filters.value, (filter, index, i0) => {
          return {
            a: common_vendor.t(filter),
            b: index,
            c: activeFilter.value === index ? 1 : "",
            d: common_vendor.o(($event) => setFilter(index), index)
          };
        }),
        d: common_vendor.f(filteredProps.value, (item, index, i0) => {
          return {
            a: index,
            b: common_vendor.o(onPropClick, index),
            c: "101b1b06-0-" + i0,
            d: common_vendor.p({
              ["prop-data"]: item
            })
          };
        }),
        e: loading.value
      }, loading.value ? {} : {}, {
        f: noMoreData.value
      }, noMoreData.value ? {} : {}, {
        g: common_vendor.o((...args) => _ctx.loadMore && _ctx.loadMore(...args)),
        h: common_vendor.o(navigateToMyRentals)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-101b1b06"], ["__file", "C:/Users/86182/Desktop/上班/解压小程序/前端/JieYa/pages/prop/prop.vue"]]);
wx.createPage(MiniProgramPage);
