"use strict";
const common_vendor = require("../../common/vendor.js");
const api_user = require("../../api/user.js");
if (!Array) {
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  _easycom_uni_icons2();
}
const _easycom_uni_icons = () => "../../uni_modules/uni-icons/components/uni-icons/uni-icons.js";
if (!Math) {
  _easycom_uni_icons();
}
const _sfc_main = {
  __name: "updateMe",
  setup(__props) {
    const userForm = common_vendor.reactive({
      nickName: "",
      sex: null,
      birthday: "",
      school: "",
      personIntruduction: ""
    });
    const avatarUrl = common_vendor.ref("");
    const avatarFile = common_vendor.ref(null);
    const chooseAvatar = () => {
      common_vendor.index.chooseImage({
        count: 1,
        // 只选择一张图片
        sizeType: ["compressed"],
        // 压缩图片
        sourceType: ["album", "camera"],
        // 从相册或相机选择
        success: (res) => {
          avatarUrl.value = res.tempFilePaths[0];
          avatarFile.value = res.tempFiles[0];
        }
      });
    };
    const onBirthdayChange = (e) => {
      userForm.birthday = e.detail.value;
    };
    const saveUserInfo = async () => {
      try {
        const updateData = {};
        if (userForm.nickName)
          updateData.nickName = userForm.nickName;
        if (userForm.sex !== null)
          updateData.sex = userForm.sex;
        if (userForm.birthday)
          updateData.birthday = userForm.birthday;
        if (userForm.school)
          updateData.school = userForm.school;
        if (userForm.personIntruduction)
          updateData.personIntruduction = userForm.personIntruduction;
        if (Object.keys(updateData).length === 0 && !avatarFile.value) {
          common_vendor.index.showToast({
            title: "请修改至少一项信息",
            icon: "none"
          });
          return;
        }
        common_vendor.index.showLoading({
          title: "正在保存..."
        });
        const res = await api_user.updateUserInfo(updateData, avatarFile.value);
        common_vendor.index.hideLoading();
        if (res.code === 1) {
          common_vendor.index.showToast({
            title: "修改成功",
            icon: "success",
            success: () => {
              setTimeout(() => {
                common_vendor.index.reLaunch({
                  url: "/pages/word/word"
                });
              }, 1500);
            }
          });
        } else {
          common_vendor.index.showToast({
            title: res.message || "修改失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.hideLoading();
        console.error("更新用户信息失败:", error);
        common_vendor.index.showToast({
          title: "网络错误，请重试",
          icon: "none"
        });
      }
    };
    common_vendor.onMounted(() => {
      const userInfo = common_vendor.index.getStorageSync("userInfo");
      if (userInfo) {
        userForm.nickName = userInfo.nickName || "";
        userForm.sex = userInfo.sex !== void 0 ? userInfo.sex : null;
        userForm.birthday = userInfo.birthday || "";
        userForm.school = userInfo.school || "";
        userForm.personIntruduction = userInfo.personIntruduction || "";
        if (userInfo.avatar) {
          avatarUrl.value = userInfo.avatar;
        }
      }
    });
    return (_ctx, _cache) => {
      var _a;
      return {
        a: avatarUrl.value || "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg",
        b: common_vendor.p({
          type: "camera-filled",
          size: "40rpx",
          color: "#fff"
        }),
        c: common_vendor.o(chooseAvatar),
        d: userForm.nickName,
        e: common_vendor.o(($event) => userForm.nickName = $event.detail.value),
        f: common_vendor.o(($event) => userForm.sex = true),
        g: userForm.sex === true ? 1 : "",
        h: common_vendor.o(($event) => userForm.sex = false),
        i: userForm.sex === false ? 1 : "",
        j: common_vendor.t(userForm.birthday || "请选择出生日期"),
        k: userForm.birthday,
        l: common_vendor.o(onBirthdayChange),
        m: userForm.school,
        n: common_vendor.o(($event) => userForm.school = $event.detail.value),
        o: userForm.personIntruduction,
        p: common_vendor.o(($event) => userForm.personIntruduction = $event.detail.value),
        q: common_vendor.t(((_a = userForm.personIntruduction) == null ? void 0 : _a.length) || 0),
        r: common_vendor.o(saveUserInfo)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-b4db514c"], ["__file", "C:/Users/86182/Desktop/上班/解压小程序/前端/JieYa/pages/updateMe/updateMe.vue"]]);
wx.createPage(MiniProgramPage);
