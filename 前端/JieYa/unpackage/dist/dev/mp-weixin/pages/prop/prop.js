"use strict";
const common_vendor = require("../../common/vendor.js");
if (!Math) {
  PropItem();
}
const PropItem = () => "../../components/prop-item/prop-item2.js";
const _sfc_main = {
  __name: "prop",
  setup(__props) {
    const propList = common_vendor.ref([
      {
        id: 1,
        name: "情绪卡片",
        description: "帮助识别和表达情绪的卡片组，包含30种基本情绪",
        price: 15,
        available: true,
        image: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/2.jpg"
      },
      {
        id: 2,
        name: "心理沙盘",
        description: "用于沙盘游戏治疗的专业工具，适合儿童心理疏导",
        price: 88,
        available: true,
        image: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/3.jpg"
      },
      {
        id: 3,
        name: "减压玩具套装",
        description: "包含多种减压球、魔方等，帮助缓解焦虑和压力",
        price: 35,
        available: false,
        image: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/backgroundImage.jpg"
      },
      {
        id: 4,
        name: "心理测评工具",
        description: "专业心理测评工具，包含多种量表和评估材料",
        price: 120,
        available: true,
        image: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/2.jpg"
      },
      {
        id: 5,
        name: "绘画治疗套装",
        description: "专业绘画治疗工具，包含画笔、颜料和特殊纸张",
        price: 68,
        available: true,
        image: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/3.jpg"
      }
    ]);
    const searchText = common_vendor.ref("");
    const filters = common_vendor.ref(["全部", "可租借", "价格↑", "价格↓"]);
    const activeFilter = common_vendor.ref(0);
    const loading = common_vendor.ref(false);
    const noMoreData = common_vendor.ref(false);
    const filteredProps = common_vendor.computed(() => {
      let result = propList.value;
      if (searchText.value) {
        result = result.filter(
          (item) => item.name.includes(searchText.value) || item.description.includes(searchText.value)
        );
      }
      switch (activeFilter.value) {
        case 1:
          result = result.filter((item) => item.available);
          break;
        case 2:
          result = [...result].sort((a, b) => a.price - b.price);
          break;
        case 3:
          result = [...result].sort((a, b) => b.price - a.price);
          break;
      }
      return result;
    });
    function setFilter(index) {
      activeFilter.value = index;
    }
    function loadMore() {
      if (noMoreData.value)
        return;
      loading.value = true;
      setTimeout(() => {
        loading.value = false;
        noMoreData.value = true;
      }, 1e3);
    }
    function onPropClick(id) {
      console.log("点击道具:", id);
      common_vendor.index.showToast({
        title: "功能开发中",
        icon: "none"
      });
    }
    function navigateToMyRentals() {
      common_vendor.index.showToast({
        title: "我的租借功能开发中",
        icon: "none"
      });
    }
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
        g: common_vendor.o(loadMore),
        h: common_vendor.o(navigateToMyRentals)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-101b1b06"], ["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/pages/prop/prop.vue"]]);
wx.createPage(MiniProgramPage);
