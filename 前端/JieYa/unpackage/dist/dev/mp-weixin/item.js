"use strict";
const common_vendor = require("./common/vendor.js");
if (!Array) {
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  _easycom_uni_icons2();
}
const _easycom_uni_icons = () => "./uni_modules/uni-icons/components/uni-icons/uni-icons.js";
if (!Math) {
  _easycom_uni_icons();
}
const _sfc_main = {
  __name: "item",
  props: {
    title: {
      type: String,
      default: "志强同志视察计算机学院"
    },
    good: {
      type: Number,
      default: 20
    },
    image: {
      type: String,
      default: "https://yuebaimage.oss-rg-china-mainland.aliyuncs.com/6f4f334d69d9482e835157f3674b62b2.jpg"
    },
    userimage: {
      type: String,
      default: "https://p1.ssl.qhimg.com/t01202765ddf451918a.png"
    },
    username: {
      type: String,
      default: "丁志强"
    }
  },
  emits: ["click"],
  setup(__props, { emit: __emit }) {
    const handupType = common_vendor.ref("hand-up");
    const good = common_vendor.ref(20);
    const toggleHandup = () => {
      if (handupType.value === "hand-up") {
        handupType.value = "hand-up-filled";
        good.value++;
      } else {
        handupType.value = "hand-up";
        good.value--;
      }
    };
    return (_ctx, _cache) => {
      return {
        a: __props.image,
        b: common_vendor.t(__props.title),
        c: __props.userimage,
        d: common_vendor.t(__props.username),
        e: common_vendor.o(toggleHandup),
        f: common_vendor.p({
          type: handupType.value
        }),
        g: common_vendor.t(good.value)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-d25a932c"], ["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/components/item/item.vue"]]);
exports.MiniProgramPage = MiniProgramPage;
