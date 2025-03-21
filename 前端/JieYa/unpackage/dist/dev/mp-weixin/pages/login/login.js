"use strict";
const common_vendor = require("../../common/vendor.js");
const api_user = require("../../api/user.js");
const _sfc_main = {
  __name: "login",
  setup(__props) {
    const email = common_vendor.ref("");
    const password = common_vendor.ref("");
    const checkCode = common_vendor.ref("");
    const checkCodeKey = common_vendor.ref("");
    const codeImage = common_vendor.ref("");
    const getCode = async () => {
      try {
        const res = await api_user.getVerificationCode();
        if (res && res.key && res.image) {
          checkCodeKey.value = res.key;
          codeImage.value = res.image;
        } else {
          common_vendor.index.showToast({
            title: "获取验证码失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("获取验证码错误:", error);
        common_vendor.index.showToast({
          title: "获取验证码失败",
          icon: "none"
        });
      }
    };
    const refreshCode = () => {
      getCode();
    };
    const login = async () => {
      if (!email.value) {
        common_vendor.index.showToast({ title: "请输入邮箱", icon: "none" });
        return;
      }
      if (!password.value) {
        common_vendor.index.showToast({ title: "请输入密码", icon: "none" });
        return;
      }
      if (!checkCode.value) {
        common_vendor.index.showToast({ title: "请输入验证码", icon: "none" });
        return;
      }
      if (!checkCodeKey.value) {
        common_vendor.index.showToast({ title: "验证码已失效，请刷新", icon: "none" });
        getCode();
        return;
      }
      try {
        const res = await api_user.login({
          email: email.value,
          password: password.value,
          checkCodeKey: checkCodeKey.value,
          checkCode: checkCode.value
        });
        if (res.code === 1) {
          common_vendor.index.showToast({
            title: "登录成功",
            icon: "success",
            success: () => {
              setTimeout(() => {
                common_vendor.index.switchTab({ url: "/pages/index/index" });
              }, 1500);
            }
          });
        } else {
          common_vendor.index.showToast({ title: res.message || "登录失败", icon: "none" });
          getCode();
        }
      } catch (error) {
        console.error("登录错误:", error);
        common_vendor.index.showToast({ title: error.message || "登录失败", icon: "none" });
        getCode();
      }
    };
    const goRegister = () => {
      common_vendor.index.navigateTo({ url: "/pages/register/register" });
    };
    common_vendor.onMounted(() => {
      getCode();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: email.value,
        b: common_vendor.o(($event) => email.value = $event.detail.value),
        c: password.value,
        d: common_vendor.o(($event) => password.value = $event.detail.value),
        e: checkCode.value,
        f: common_vendor.o(($event) => checkCode.value = $event.detail.value),
        g: codeImage.value
      }, codeImage.value ? {
        h: codeImage.value
      } : {}, {
        i: common_vendor.o(refreshCode),
        j: common_vendor.o(login),
        k: common_vendor.o(goRegister)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-e4e4508d"], ["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/pages/login/login.vue"]]);
wx.createPage(MiniProgramPage);
