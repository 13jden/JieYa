"use strict";
const common_vendor = require("../../common/vendor.js");
const api_user = require("../../api/user.js");
const _sfc_main = {
  __name: "register",
  setup(__props) {
    const email = common_vendor.ref("");
    const nickName = common_vendor.ref("");
    const password = common_vendor.ref("");
    const confirmPassword = common_vendor.ref("");
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
    const register = async () => {
      if (!email.value) {
        common_vendor.index.showToast({ title: "请输入邮箱", icon: "none" });
        return;
      }
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(email.value)) {
        common_vendor.index.showToast({ title: "邮箱格式不正确", icon: "none" });
        return;
      }
      if (!nickName.value) {
        common_vendor.index.showToast({ title: "请输入昵称", icon: "none" });
        return;
      }
      if (nickName.value.length > 20) {
        common_vendor.index.showToast({ title: "昵称不能超过20个字符", icon: "none" });
        return;
      }
      if (!password.value) {
        common_vendor.index.showToast({ title: "请输入密码", icon: "none" });
        return;
      }
      const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,16}$/;
      if (!passwordRegex.test(password.value)) {
        common_vendor.index.showToast({ title: "密码必须是8-16位，且包含字母和数字", icon: "none" });
        return;
      }
      if (password.value !== confirmPassword.value) {
        common_vendor.index.showToast({ title: "两次密码输入不一致", icon: "none" });
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
        const res = await api_user.register({
          email: email.value,
          nickName: nickName.value,
          password: password.value,
          checkCodeKey: checkCodeKey.value,
          checkCode: checkCode.value
        });
        if (res.code === 1) {
          common_vendor.index.showToast({
            title: "注册成功",
            icon: "success",
            success: () => {
              setTimeout(() => {
                common_vendor.index.navigateTo({ url: "/pages/login/login" });
              }, 1500);
            }
          });
        } else {
          common_vendor.index.showToast({ title: res.message || "注册失败", icon: "none" });
          getCode();
        }
      } catch (error) {
        console.error("注册错误:", error);
        common_vendor.index.showToast({ title: error.message || "注册失败", icon: "none" });
        getCode();
      }
    };
    const goLogin = () => {
      common_vendor.index.navigateTo({ url: "/pages/login/login" });
    };
    common_vendor.onMounted(() => {
      getCode();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: email.value,
        b: common_vendor.o(($event) => email.value = $event.detail.value),
        c: nickName.value,
        d: common_vendor.o(($event) => nickName.value = $event.detail.value),
        e: password.value,
        f: common_vendor.o(($event) => password.value = $event.detail.value),
        g: confirmPassword.value,
        h: common_vendor.o(($event) => confirmPassword.value = $event.detail.value),
        i: checkCode.value,
        j: common_vendor.o(($event) => checkCode.value = $event.detail.value),
        k: codeImage.value
      }, codeImage.value ? {
        l: codeImage.value
      } : {}, {
        m: common_vendor.o(refreshCode),
        n: common_vendor.o(register),
        o: common_vendor.o(goLogin)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-bac4a35d"], ["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/pages/register/register.vue"]]);
wx.createPage(MiniProgramPage);
