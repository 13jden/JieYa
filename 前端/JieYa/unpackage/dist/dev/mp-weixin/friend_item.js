"use strict";
const common_vendor = require("./common/vendor.js");
const _sfc_main = {
  __name: "friend_item",
  props: {
    username: {
      type: String,
      default: "未知用户"
    },
    userimage: {
      type: String,
      default: "/static/default-avatar.png"
    },
    message: {
      type: String,
      default: ""
    },
    date: {
      type: String,
      default: ""
    },
    isNew: {
      type: Boolean,
      default: false
    },
    badgeCount: {
      type: Number,
      default: 0
    }
  },
  setup(__props) {
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: __props.userimage,
        b: __props.badgeCount > 0
      }, __props.badgeCount > 0 ? {
        c: common_vendor.t(__props.badgeCount > 99 ? "99+" : __props.badgeCount)
      } : {}, {
        d: common_vendor.t(__props.username),
        e: common_vendor.t(__props.message),
        f: __props.isNew ? 1 : "",
        g: __props.date
      }, __props.date ? {
        h: common_vendor.t(__props.date)
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-db022675"], ["__file", "C:/Users/86182/Desktop/上班/解压小程序/前端/JieYa/components/friend_item/friend_item.vue"]]);
exports.MiniProgramPage = MiniProgramPage;
