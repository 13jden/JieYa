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
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.f(lists.value, (list, index, i0) => {
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
        b: _ctx.noMoreData
      }, _ctx.noMoreData ? {} : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-1c765c2e"], ["__file", "C:/Users/86182/Documents/HBuilderProjects/JieYa/pages/find/find.vue"]]);
wx.createPage(MiniProgramPage);
