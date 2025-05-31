"use strict";
const common_vendor = require("../../common/vendor.js");
const api_note = require("../../api/note.js");
const api_user = require("../../api/user.js");
if (!Array) {
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  const _easycom_item2 = common_vendor.resolveComponent("item");
  (_easycom_uni_icons2 + _easycom_item2)();
}
const _easycom_uni_icons = () => "../../uni_modules/uni-icons/components/uni-icons/uni-icons.js";
const _easycom_item = () => "../../components/item/item2.js";
if (!Math) {
  (_easycom_uni_icons + _easycom_item)();
}
const __default__ = {
  navigationBarTitleText: "搜索"
};
const _sfc_main = /* @__PURE__ */ Object.assign(__default__, {
  __name: "search",
  setup(__props) {
    const keyword = common_vendor.ref("");
    const searchHistory = common_vendor.ref([]);
    const showResults = common_vendor.ref(false);
    const activeTab = common_vendor.ref("notes");
    const noteList = common_vendor.ref([]);
    const notesCurrentPage = common_vendor.ref(1);
    const notesPageSize = common_vendor.ref(10);
    const notesLoading = common_vendor.ref(false);
    const notesHasMore = common_vendor.ref(true);
    const userList = common_vendor.ref([]);
    common_vendor.onMounted(() => {
      loadSearchHistory();
    });
    function loadSearchHistory() {
      try {
        const history = common_vendor.index.getStorageSync("searchHistory");
        if (history) {
          searchHistory.value = JSON.parse(history);
        }
      } catch (e) {
        console.error("加载搜索历史失败:", e);
        searchHistory.value = [];
      }
    }
    function saveSearchHistory(keyword2) {
      try {
        const index = searchHistory.value.indexOf(keyword2);
        if (index !== -1) {
          searchHistory.value.splice(index, 1);
        }
        searchHistory.value.unshift(keyword2);
        if (searchHistory.value.length > 10) {
          searchHistory.value = searchHistory.value.slice(0, 10);
        }
        common_vendor.index.setStorageSync("searchHistory", JSON.stringify(searchHistory.value));
      } catch (e) {
        console.error("保存搜索历史失败:", e);
      }
    }
    function clearHistory() {
      common_vendor.index.showModal({
        title: "提示",
        content: "确定要清空搜索历史吗？",
        success: function(res) {
          if (res.confirm) {
            searchHistory.value = [];
            common_vendor.index.removeStorageSync("searchHistory");
          }
        }
      });
    }
    function useHistoryKeyword(historyKeyword) {
      keyword.value = historyKeyword;
      handleSearch();
    }
    function clearKeyword() {
      keyword.value = "";
      if (showResults.value) {
        showResults.value = false;
      }
    }
    function goBack() {
      common_vendor.index.navigateBack();
    }
    function handleSearch() {
      if (!keyword.value.trim()) {
        common_vendor.index.showToast({
          title: "请输入搜索内容",
          icon: "none"
        });
        return;
      }
      saveSearchHistory(keyword.value.trim());
      showResults.value = true;
      noteList.value = [];
      userList.value = [];
      notesCurrentPage.value = 1;
      notesHasMore.value = true;
      loadSearchResults();
    }
    function loadSearchResults() {
      if (activeTab.value === "notes") {
        loadNoteResults();
      } else {
        loadUserResults();
      }
    }
    async function loadNoteResults() {
      if (notesLoading.value || !notesHasMore.value)
        return;
      try {
        notesLoading.value = true;
        const res = await api_note.searchNotes(
          keyword.value.trim(),
          notesCurrentPage.value,
          notesPageSize.value
        );
        if (res.code === 1) {
          const newNotes = res.data.records || [];
          if (notesCurrentPage.value === 1) {
            noteList.value = newNotes;
          } else {
            noteList.value = [...noteList.value, ...newNotes];
          }
          notesHasMore.value = newNotes.length === notesPageSize.value;
        } else {
          common_vendor.index.showToast({
            title: res.message || "搜索失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("搜索笔记失败:", error);
        common_vendor.index.showToast({
          title: "网络错误，请重试",
          icon: "none"
        });
      } finally {
        notesLoading.value = false;
      }
    }
    function loadMoreNotes() {
      if (notesHasMore.value && !notesLoading.value) {
        notesCurrentPage.value++;
        loadNoteResults();
      }
    }
    async function loadUserResults() {
      try {
        const res = await api_user.searchUsers(keyword.value.trim());
        if (res.code === 1) {
          userList.value = res.data || [];
        } else {
          common_vendor.index.showToast({
            title: res.message || "搜索失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("搜索用户失败:", error);
        common_vendor.index.showToast({
          title: "网络错误，请重试",
          icon: "none"
        });
      }
    }
    function switchTab(tab) {
      if (activeTab.value === tab)
        return;
      activeTab.value = tab;
      if (tab === "users" && userList.value.length === 0) {
        loadUserResults();
      }
    }
    function goToNote(noteId) {
      common_vendor.index.navigateTo({
        url: `/pages/note/note?id=${noteId}`
      });
    }
    function goToUserProfile(userId) {
      common_vendor.index.navigateTo({
        url: `/pages/user/profile?id=${userId}`
      });
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(handleSearch),
        b: keyword.value,
        c: common_vendor.o(($event) => keyword.value = $event.detail.value),
        d: keyword.value
      }, keyword.value ? {
        e: common_vendor.p({
          type: "close",
          size: "18",
          color: "#999"
        }),
        f: common_vendor.o(clearKeyword)
      } : {}, {
        g: common_vendor.o(goBack),
        h: !showResults.value
      }, !showResults.value ? {
        i: common_vendor.p({
          type: "trash",
          size: "18",
          color: "#666"
        }),
        j: common_vendor.o(clearHistory),
        k: common_vendor.f(searchHistory.value, (item, index, i0) => {
          return {
            a: common_vendor.t(item),
            b: index,
            c: common_vendor.o(($event) => useHistoryKeyword(item), index)
          };
        })
      } : {}, {
        l: showResults.value
      }, showResults.value ? common_vendor.e({
        m: activeTab.value === "notes" ? 1 : "",
        n: common_vendor.o(($event) => switchTab("notes")),
        o: activeTab.value === "users" ? 1 : "",
        p: common_vendor.o(($event) => switchTab("users")),
        q: activeTab.value === "notes"
      }, activeTab.value === "notes" ? common_vendor.e({
        r: noteList.value.length === 0
      }, noteList.value.length === 0 ? {} : {
        s: common_vendor.f(noteList.value, (note, index, i0) => {
          return {
            a: "c10c040c-2-" + i0,
            b: common_vendor.p({
              username: note.user ? note.user.nickName : "未知用户",
              title: note.title,
              image: note.coverImage,
              userimage: note.user ? note.user.avatar : "",
              likeCount: note.likeCount || 0
            }),
            c: index,
            d: common_vendor.o(($event) => goToNote(note.id), index)
          };
        })
      }, {
        t: notesLoading.value && noteList.value.length > 0
      }, notesLoading.value && noteList.value.length > 0 ? {} : {}, {
        v: !notesHasMore.value && noteList.value.length > 0
      }, !notesHasMore.value && noteList.value.length > 0 ? {} : {}, {
        w: common_vendor.o(loadMoreNotes)
      }) : {}, {
        x: activeTab.value === "users"
      }, activeTab.value === "users" ? common_vendor.e({
        y: userList.value.length === 0
      }, userList.value.length === 0 ? {} : {}, {
        z: common_vendor.f(userList.value, (user, index, i0) => {
          return {
            a: user.avatar || "/static/default-avatar.png",
            b: common_vendor.t(user.nickName),
            c: common_vendor.t(user.personIntruduction || "这个用户很懒，什么都没留下"),
            d: index,
            e: common_vendor.o(($event) => goToUserProfile(user.id), index)
          };
        })
      }) : {}) : {});
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-c10c040c"], ["__file", "C:/Users/86182/Desktop/上班/解压小程序/前端/JieYa/pages/search/search.vue"]]);
wx.createPage(MiniProgramPage);
