"use strict";
const common_vendor = require("./common/vendor.js");
const _sfc_main = {
  __name: "prop-item",
  props: {
    propData: {
      type: Object,
      required: true
    }
  },
  emits: ["itemClick"],
  setup(__props, { emit: __emit }) {
    const props = __props;
    const emit = __emit;
    function onItemClick() {
      emit("itemClick", props.propData.id);
    }
    return (_ctx, _cache) => {
      return {
        a: __props.propData.image,
        b: common_vendor.t(__props.propData.name),
        c: common_vendor.t(__props.propData.description),
        d: common_vendor.t(__props.propData.price),
        e: common_vendor.t(__props.propData.available ? "可租借" : "已租出"),
        f: common_vendor.n(__props.propData.available ? "available" : "unavailable"),
        g: common_vendor.o(onItemClick)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-eeb9ab45"], ["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/components/prop-item/prop-item.vue"]]);
exports.MiniProgramPage = MiniProgramPage;
