"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  __name: "login",
  setup(__props) {
    const username = common_vendor.ref("");
    const password = common_vendor.ref("");
    const code = common_vendor.ref("");
    const codeText = common_vendor.ref("验证码");
    const getCode = () => {
      codeText.value = "已发送";
      setTimeout(() => {
        codeText.value = "验证码";
      }, 3e3);
    };
    const login = () => {
      if (!username.value || !password.value) {
        common_vendor.index.showToast({ title: "请输入用户名和密码", icon: "none" });
        return;
      }
      common_vendor.index.showToast({ title: "登录成功", icon: "success" });
      common_vendor.index.navigateTo({ url: "/pages/index/index" });
    };
    const goRegister = () => {
      common_vendor.index.navigateTo({ url: "/pages/register/register" });
    };
    return (_ctx, _cache) => {
      return {
        a: username.value,
        b: common_vendor.o(($event) => username.value = $event.detail.value),
        c: password.value,
        d: common_vendor.o(($event) => password.value = $event.detail.value),
        e: code.value,
        f: common_vendor.o(($event) => code.value = $event.detail.value),
        g: common_vendor.t(codeText.value),
        h: common_vendor.o(getCode),
        i: common_vendor.o(login),
        j: common_vendor.o(goRegister)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-e4e4508d"], ["__file", "C:/Users/86182/Documents/HBuilderProjects/JieYa/pages/login/login.vue"]]);
wx.createPage(MiniProgramPage);
