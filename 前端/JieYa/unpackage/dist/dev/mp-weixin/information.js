"use strict";
const common_vendor = require("./common/vendor.js");
const _sfc_main = {
  __name: "information",
  props: {
    image: {
      type: String,
      default: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/4.jpg"
    },
    title: {
      type: String,
      default: "开学季————开学心理调适指南"
    },
    summary: {
      type: String,
      default: "正文内容..."
    }
  },
  emits: ["click"],
  setup(__props, { emit: __emit }) {
    const props = __props;
    return (_ctx, _cache) => {
      return {
        a: props.image,
        b: common_vendor.t(props.title),
        c: common_vendor.t(props.summary)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-78f7e7a5"], ["__file", "C:/Users/86182/Desktop/上班/解压小程序/前端/JieYa/components/information/information.vue"]]);
exports.MiniProgramPage = MiniProgramPage;
