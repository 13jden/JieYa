"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  __name: "publish",
  setup(__props) {
    common_vendor.ref("");
    const title = common_vendor.ref("");
    const content = common_vendor.ref("");
    const filelist = common_vendor.ref([]);
    const imagelist = common_vendor.ref([]);
    function chooseFile() {
      common_vendor.index.chooseImage({
        count: 9,
        // 允许选择最多9张
        success: (res) => {
          res.tempFilePaths.forEach((filePath) => {
            filelist.value.push(filePath);
          });
        },
        fail: (err) => {
          console.log("选择文件失败: ", err);
        }
      });
    }
    function removeImage(index) {
      filelist.value.splice(index, 1);
      imagelist.value.splice(index, 1);
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o((...args) => _ctx.finish && _ctx.finish(...args)),
        b: !imagelist.value.length,
        c: title.value,
        d: common_vendor.o(($event) => title.value = $event.detail.value),
        e: common_vendor.f(filelist.value, (item, index, i0) => {
          return common_vendor.e({
            a: item
          }, item ? {
            b: item
          } : {}, {
            c: common_vendor.o(($event) => removeImage(index), index),
            d: index
          });
        }),
        f: filelist.value.length < 9
      }, filelist.value.length < 9 ? {
        g: common_vendor.o(chooseFile)
      } : {}, {
        h: content.value,
        i: common_vendor.o(($event) => content.value = $event.detail.value)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-bfce3555"], ["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/pages/publish/publish.vue"]]);
wx.createPage(MiniProgramPage);
