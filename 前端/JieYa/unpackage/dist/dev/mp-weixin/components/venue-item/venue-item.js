"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  __name: "venue-item",
  props: {
    venueData: {
      type: Object,
      required: true
    }
  },
  emits: ["itemClick"],
  setup(__props, { emit: __emit }) {
    const props = __props;
    const emit = __emit;
    function onItemClick() {
      emit("itemClick", props.venueData.id);
    }
    return (_ctx, _cache) => {
      return {
        a: __props.venueData.coverImage,
        b: common_vendor.t(__props.venueData.name),
        c: common_vendor.t(__props.venueData.location),
        d: common_vendor.t(__props.venueData.description),
        e: common_vendor.t(__props.venueData.price),
        f: common_vendor.t(__props.venueData.capacity),
        g: common_vendor.f(__props.venueData.tags, (tag, index, i0) => {
          return {
            a: common_vendor.t(tag),
            b: index
          };
        }),
        h: common_vendor.o(onItemClick)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-65a7f693"], ["__file", "C:/Users/86182/Desktop/上班/解压小程序/前端/JieYa/components/venue-item/venue-item.vue"]]);
wx.createPage(MiniProgramPage);
