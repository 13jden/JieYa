"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  __name: "message-notification",
  props: {
    title: {
      type: String,
      default: "新消息"
    },
    message: {
      type: String,
      default: ""
    },
    duration: {
      type: Number,
      default: 3e3
    },
    messageData: {
      type: Object,
      default: () => ({})
    }
  },
  emits: ["tap", "close"],
  setup(__props, { expose: __expose, emit: __emit }) {
    const props = __props;
    const emit = __emit;
    const visible = common_vendor.ref(false);
    const show = () => {
      visible.value = true;
      if (props.duration > 0) {
        setTimeout(() => {
          hide();
        }, props.duration);
      }
    };
    const hide = () => {
      visible.value = false;
      emit("close");
    };
    const handleTap = () => {
      emit("tap", props.messageData);
      hide();
    };
    __expose({
      show,
      hide
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: visible.value
      }, visible.value ? {
        b: common_vendor.t(__props.title),
        c: common_vendor.t(__props.message),
        d: common_vendor.o(handleTap)
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-33995313"], ["__file", "C:/Users/86182/Desktop/上班/解压小程序/前端/JieYa/components/message-notification/message-notification.vue"]]);
wx.createPage(MiniProgramPage);
