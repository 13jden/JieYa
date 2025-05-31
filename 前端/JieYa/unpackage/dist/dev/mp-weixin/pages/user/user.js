"use strict";
const common_vendor = require("../../common/vendor.js");
const api_user = require("../../api/user.js");
const api_note = require("../../api/note.js");
const api_focus = require("../../api/focus.js");
if (!Array) {
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  _easycom_uni_icons2();
}
const _easycom_uni_icons = () => "../../uni_modules/uni-icons/components/uni-icons/uni-icons.js";
if (!Math) {
  _easycom_uni_icons();
}
const _sfc_main = {
  __name: "user",
  setup(__props) {
    const userInfo = common_vendor.ref({});
    const isFollowing = common_vendor.ref(false);
    const activeTab = common_vendor.ref("notes");
    const noteList = common_vendor.ref([]);
    const likedList = common_vendor.ref([]);
    const collectionList = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    const userId = common_vendor.ref("OsPcvnikSK");
    const isCurrentUser = common_vendor.computed(() => {
      const currentUser = common_vendor.index.getStorageSync("userInfo");
      return currentUser && currentUser.userId === userInfo.value.userId;
    });
    const currentUserId = common_vendor.computed(() => {
      const userInfo2 = common_vendor.index.getStorageSync("userInfo");
      return userInfo2 ? userInfo2.userId : "";
    });
    common_vendor.onLoad((options) => {
      if (options && options.id) {
        userId.value = options.id;
      }
      fetchUserDetail();
      checkUserFollowStatus();
    });
    async function fetchUserDetail() {
      try {
        loading.value = true;
        common_vendor.index.showLoading({
          title: "加载中..."
        });
        console.log("获取用户详情:", userId.value);
        const res = await api_user.getDetail(userId.value);
        if (res.code === 1 && res.data) {
          userInfo.value = res.data;
          console.log("用户详情:", userInfo.value);
          fetchUserNotes();
          fetchUserFocusList();
          fetchUserFansList();
        } else {
          common_vendor.index.showToast({
            title: "获取用户信息失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("获取用户详情失败:", error);
        common_vendor.index.showToast({
          title: "网络错误，请重试",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
        loading.value = false;
      }
    }
    async function fetchUserNotes() {
      try {
        loading.value = true;
        const res = await api_note.getUserNotes(userId.value);
        if (res.code === 1) {
          noteList.value = res.data || [];
        } else {
          noteList.value = [
            {
              id: 1,
              title: "笔记1",
              coverImage: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg"
            },
            {
              id: 2,
              title: "笔记2",
              coverImage: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg"
            }
          ];
        }
      } catch (error) {
        console.error("获取用户笔记错误:", error);
        noteList.value = [
          {
            id: 1,
            title: "示例笔记1",
            coverImage: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg"
          },
          {
            id: 2,
            title: "示例笔记2",
            coverImage: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg"
          }
        ];
      } finally {
        loading.value = false;
      }
    }
    async function fetchUserLikes() {
      try {
        loading.value = true;
        likedList.value = [
          {
            id: 3,
            title: "喜欢的笔记1",
            coverImage: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg"
          },
          {
            id: 4,
            title: "喜欢的笔记2",
            coverImage: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg"
          }
        ];
        await new Promise((resolve) => setTimeout(resolve, 500));
      } catch (error) {
        console.error("获取喜欢笔记错误:", error);
        likedList.value = [];
      } finally {
        loading.value = false;
      }
    }
    async function fetchUserCollections() {
      try {
        loading.value = true;
        const res = await api_note.getCollectList(userId.value);
        if (res.code === 1) {
          collectionList.value = res.data || [];
        } else {
          collectionList.value = [
            {
              id: 5,
              title: "收藏的笔记1",
              coverImage: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg"
            },
            {
              id: 6,
              title: "收藏的笔记2",
              coverImage: "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg"
            }
          ];
        }
      } catch (error) {
        console.error("获取收藏笔记错误:", error);
        collectionList.value = [];
      } finally {
        loading.value = false;
      }
    }
    async function fetchUserFocusList() {
      try {
        loading.value = true;
        const res = await api_focus.getFocusList(userId.value);
        if (res.code === 1) {
          userInfo.value.followCount = res.data.length || 0;
        }
      } catch (error) {
        console.error("获取关注列表错误:", error);
      } finally {
        loading.value = false;
      }
    }
    async function fetchUserFansList() {
      try {
        loading.value = true;
        const res = await api_focus.getFansList(userId.value);
        if (res.code === 1) {
          userInfo.value.fansCount = res.data.length || 0;
        }
      } catch (error) {
        console.error("获取粉丝列表错误:", error);
      } finally {
        loading.value = false;
      }
    }
    async function checkUserFollowStatus() {
      if (isCurrentUser.value)
        return;
      try {
        if (currentUserId.value) {
          const res = await api_focus.checkFocusStatus(currentUserId.value, userId.value);
          if (res.code === 1) {
            isFollowing.value = res.data;
          }
        }
      } catch (error) {
        console.error("检查关注状态错误:", error);
      }
    }
    async function handleFollow() {
      if (!currentUserId.value) {
        common_vendor.index.showToast({
          title: "请先登录",
          icon: "none"
        });
        return;
      }
      if (currentUserId.value === userId.value) {
        common_vendor.index.showToast({
          title: "不能关注自己",
          icon: "none"
        });
        return;
      }
      try {
        common_vendor.index.showLoading({
          title: isFollowing.value ? "取消关注中..." : "关注中..."
        });
        let res;
        if (isFollowing.value) {
          res = await api_focus.deleteFocus(currentUserId.value, userId.value);
        } else {
          res = await api_focus.addFocus(currentUserId.value, userId.value);
        }
        if (res.code === 1) {
          isFollowing.value = !isFollowing.value;
          common_vendor.index.showToast({
            title: isFollowing.value ? "已关注" : "已取消关注",
            icon: "none"
          });
          if (isFollowing.value) {
            userInfo.value.fansCount = (userInfo.value.fansCount || 0) + 1;
          } else if (userInfo.value.fansCount > 0) {
            userInfo.value.fansCount -= 1;
          }
        } else {
          common_vendor.index.showToast({
            title: res.message || "操作失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("关注操作错误:", error);
        common_vendor.index.showToast({
          title: "网络错误，请重试",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
      }
    }
    async function switchTab(tab) {
      if (activeTab.value === tab)
        return;
      activeTab.value = tab;
      if (tab === "notes") {
        await fetchUserNotes();
      } else if (tab === "likes") {
        await fetchUserLikes();
      } else if (tab === "collections") {
        await fetchUserCollections();
      }
    }
    function goToUpdateMe() {
      common_vendor.index.navigateTo({
        url: "/pages/updateMe/updateMe"
      });
    }
    function goToNote(noteId) {
      common_vendor.index.navigateTo({
        url: `/pages/note/note?id=${noteId}`
      });
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: userInfo.value.avatar ? `http://localhost:8081/images/avatar/${userInfo.value.avatar}` : "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg",
        b: common_vendor.t(userInfo.value.nickName || "未设置昵称"),
        c: common_vendor.t(userInfo.value.userId || "未知"),
        d: common_vendor.t(userInfo.value.fansCount || 0),
        e: common_vendor.t(userInfo.value.likeCount || 0),
        f: common_vendor.t(userInfo.value.followCount || 0),
        g: isCurrentUser.value
      }, isCurrentUser.value ? {
        h: common_vendor.o(goToUpdateMe),
        i: common_vendor.p({
          type: "settings",
          size: "50rpx"
        })
      } : {
        j: common_vendor.t(isFollowing.value ? "已关注" : "关注"),
        k: common_vendor.o(handleFollow)
      }, {
        l: common_vendor.t(userInfo.value.personIntruduction || "这个人很懒，什么都没留下..."),
        m: userInfo.value.sex !== void 0 || userInfo.value.school
      }, userInfo.value.sex !== void 0 || userInfo.value.school ? common_vendor.e({
        n: userInfo.value.sex !== void 0
      }, userInfo.value.sex !== void 0 ? {
        o: common_vendor.p({
          type: "person",
          size: "30rpx",
          color: "#666"
        }),
        p: common_vendor.t(userInfo.value.sex ? "男" : "女")
      } : {}, {
        q: userInfo.value.school
      }, userInfo.value.school ? {
        r: common_vendor.p({
          type: "education",
          size: "30rpx",
          color: "#666"
        }),
        s: common_vendor.t(userInfo.value.school)
      } : {}) : {}, {
        t: activeTab.value === "notes" ? 1 : "",
        v: common_vendor.o(($event) => switchTab("notes")),
        w: activeTab.value === "likes" ? 1 : "",
        x: common_vendor.o(($event) => switchTab("likes")),
        y: activeTab.value === "collections" ? 1 : "",
        z: common_vendor.o(($event) => switchTab("collections")),
        A: activeTab.value === "notes"
      }, activeTab.value === "notes" ? common_vendor.e({
        B: loading.value
      }, loading.value ? {
        C: common_vendor.p({
          type: "spinner-cycle",
          size: "30",
          color: "#007AFF"
        })
      } : noteList.value.length === 0 ? {} : {
        E: common_vendor.f(noteList.value, (note, index, i0) => {
          return {
            a: note.coverImage,
            b: common_vendor.t(note.title),
            c: index,
            d: common_vendor.o(($event) => goToNote(note.id), index)
          };
        })
      }, {
        D: noteList.value.length === 0
      }) : activeTab.value === "likes" ? common_vendor.e({
        G: loading.value
      }, loading.value ? {
        H: common_vendor.p({
          type: "spinner-cycle",
          size: "30",
          color: "#007AFF"
        })
      } : likedList.value.length === 0 ? {} : {
        J: common_vendor.f(likedList.value, (note, index, i0) => {
          return {
            a: note.coverImage,
            b: common_vendor.t(note.title),
            c: index,
            d: common_vendor.o(($event) => goToNote(note.id), index)
          };
        })
      }, {
        I: likedList.value.length === 0
      }) : common_vendor.e({
        K: loading.value
      }, loading.value ? {
        L: common_vendor.p({
          type: "spinner-cycle",
          size: "30",
          color: "#007AFF"
        })
      } : collectionList.value.length === 0 ? {} : {
        N: common_vendor.f(collectionList.value, (note, index, i0) => {
          return {
            a: note.coverImage,
            b: common_vendor.t(note.title),
            c: index,
            d: common_vendor.o(($event) => goToNote(note.id), index)
          };
        })
      }, {
        M: collectionList.value.length === 0
      }), {
        F: activeTab.value === "likes"
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-0f7520f0"], ["__file", "C:/Users/86182/Desktop/上班/解压小程序/前端/JieYa/pages/user/user.vue"]]);
wx.createPage(MiniProgramPage);
