"use strict";
const common_vendor = require("./common/vendor.js");
const _sfc_main = {
  __name: "friend_item",
  props: {
    userimage: {
      type: String,
      default: "https://th.bing.com/th/id/R.5da7e5c735cc95f8f8e3d3bb7e981d7a?rik=koE8E6kqXKbipA&riu=http%3a%2f%2fwww.beihaiting.com%2fuploads%2fallimg%2f150515%2f10723-15051522234J95.jpg&ehk=CTB857uvdo4mBMipOW25WsUo7aaZd7w73ebL%2b7H71jA%3d&risl=&pid=ImgRaw&r=0"
    },
    username: {
      type: String,
      default: "育良书记"
    },
    message: {
      type: String,
      default: "你好"
    },
    date: {
      type: String,
      default: "8月9日"
    },
    isNew: {
      type: Boolean,
      default: true
    }
  },
  emits: ["click"],
  setup(__props, { emit: __emit }) {
    return (_ctx, _cache) => {
      return {
        a: __props.userimage,
        b: __props.isNew,
        c: common_vendor.t(__props.username),
        d: common_vendor.t(__props.message),
        e: common_vendor.t(__props.date)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-db022675"], ["__file", "C:/Users/86182/Documents/HBuilderProjects/JieYa/components/friend_item/friend_item.vue"]]);
exports.MiniProgramPage = MiniProgramPage;
