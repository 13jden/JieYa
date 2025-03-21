"use strict";
const common_vendor = require("../../common/vendor.js");
if (!Array) {
  const _easycom_item2 = common_vendor.resolveComponent("item");
  _easycom_item2();
}
const _easycom_item = () => "../../components/item/item2.js";
if (!Math) {
  _easycom_item();
}
const _sfc_main = {
  __name: "find",
  setup(__props) {
    const lists = common_vendor.ref([]);
    const categories = common_vendor.ref(["全部", "开心", "难过", "焦虑", "兴奋", "平静", "孤独", "感恩"]);
    const activeCategory = common_vendor.ref(0);
    function loadListsFromSession() {
      const storedLists = common_vendor.wx$1.getStorageSync("lists");
      if (storedLists) {
        lists.value = JSON.parse(storedLists);
      }
    }
    common_vendor.onMounted(() => {
      loadListsFromSession();
    });
    function goToNote(noteId) {
      common_vendor.index.navigateTo({
        url: `/pages/note/note?id=${noteId}`
      });
    }
    function selectCategory(index) {
      activeCategory.value = index;
      console.log("选择分类:", categories.value[index]);
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.f(categories.value, (category, index, i0) => {
          return {
            a: common_vendor.t(category),
            b: index,
            c: activeCategory.value === index ? 1 : "",
            d: common_vendor.o(($event) => selectCategory(index), index)
          };
        }),
        b: common_vendor.f(lists.value, (list, index, i0) => {
          return {
            a: "1c765c2e-0-" + i0,
            b: common_vendor.p({
              username: list.username,
              title: list.title,
              image: list.coverImage,
              userimage: list.userimage
            }),
            c: index,
            d: common_vendor.o(($event) => goToNote(list.nid), index)
          };
        }),
        c: _ctx.noMoreData
      }, _ctx.noMoreData ? {} : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-1c765c2e"], ["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/pages/find/find.vue"]]);
wx.createPage(MiniProgramPage);
