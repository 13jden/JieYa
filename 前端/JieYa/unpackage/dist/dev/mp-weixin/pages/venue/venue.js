"use strict";
const common_vendor = require("../../common/vendor.js");
if (!Math) {
  VenueItem();
}
const VenueItem = () => "../../components/venue-item/venue-item2.js";
const _sfc_main = {
  __name: "venue",
  setup(__props) {
    const venueList = common_vendor.ref([
      {
        id: 1,
        name: "阳光心理咨询室",
        description: "安静舒适的咨询环境，专业设备齐全，适合个人咨询和小组活动",
        location: "杭州市西湖区文三路",
        price: 120,
        capacity: 8,
        tags: ["安静", "舒适", "专业设备"],
        image: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/2.jpg"
      },
      {
        id: 2,
        name: "心灵花园工作室",
        description: "温馨自然的环境，有独立休息区和茶水间，适合长时间心理工作坊",
        location: "杭州市拱墅区莫干山路",
        price: 200,
        capacity: 15,
        tags: ["自然", "宽敞", "休息区"],
        image: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/3.jpg"
      },
      {
        id: 3,
        name: "静心沙龙",
        description: "位于市中心的高端咨询场所，隔音效果好，私密性强，适合VIP客户",
        location: "杭州市上城区平海路",
        price: 280,
        capacity: 4,
        tags: ["高端", "私密", "市中心"],
        image: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/backgroundImage.jpg"
      },
      {
        id: 4,
        name: "青少年心理活动中心",
        description: "专为青少年设计的活动空间，配备互动游戏设施，适合团体心理辅导",
        location: "杭州市滨江区江南大道",
        price: 150,
        capacity: 20,
        tags: ["青少年", "互动", "团体活动"],
        image: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/2.jpg"
      }
    ]);
    const searchText = common_vendor.ref("");
    const categories = common_vendor.ref(["全部", "个人咨询", "团体活动", "工作坊", "亲子空间"]);
    const activeCategory = common_vendor.ref(0);
    const loading = common_vendor.ref(false);
    const noMoreData = common_vendor.ref(false);
    const filteredVenues = common_vendor.computed(() => {
      let result = venueList.value;
      if (searchText.value) {
        result = result.filter(
          (item) => item.name.includes(searchText.value) || item.description.includes(searchText.value) || item.location.includes(searchText.value)
        );
      }
      if (activeCategory.value !== 0) {
        const categoryMap = {
          1: [1, 3],
          // 个人咨询场地ID
          2: [2, 4],
          // 团体活动场地ID
          3: [2],
          // 工作坊场地ID
          4: [4]
          // 亲子空间场地ID
        };
        const allowedIds = categoryMap[activeCategory.value] || [];
        result = result.filter((item) => allowedIds.includes(item.id));
      }
      return result;
    });
    function setCategory(index) {
      activeCategory.value = index;
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
    function onVenueClick(id) {
      console.log("点击场地:", id);
      common_vendor.index.showToast({
        title: "功能开发中",
        icon: "none"
      });
    }
    function navigateToMyBookings() {
      common_vendor.index.showToast({
        title: "我的预约功能开发中",
        icon: "none"
      });
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: searchText.value,
        b: common_vendor.o(($event) => searchText.value = $event.detail.value),
        c: common_vendor.f(categories.value, (category, index, i0) => {
          return {
            a: common_vendor.t(category),
            b: index,
            c: activeCategory.value === index ? 1 : "",
            d: common_vendor.o(($event) => setCategory(index), index)
          };
        }),
        d: common_vendor.f(filteredVenues.value, (item, index, i0) => {
          return {
            a: index,
            b: common_vendor.o(onVenueClick, index),
            c: "c1e1bb09-0-" + i0,
            d: common_vendor.p({
              ["venue-data"]: item
            })
          };
        }),
        e: loading.value
      }, loading.value ? {} : {}, {
        f: noMoreData.value
      }, noMoreData.value ? {} : {}, {
        g: common_vendor.o(loadMore),
        h: common_vendor.o(navigateToMyBookings)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-c1e1bb09"], ["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/pages/venue/venue.vue"]]);
wx.createPage(MiniProgramPage);
