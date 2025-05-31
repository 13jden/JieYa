"use strict";
const common_vendor = require("../../common/vendor.js");
const api_note = require("../../api/note.js");
if (!Array) {
  const _easycom_item2 = common_vendor.resolveComponent("item");
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  (_easycom_item2 + _easycom_uni_icons2)();
}
const _easycom_item = () => "../../components/item/item2.js";
const _easycom_uni_icons = () => "../../uni_modules/uni-icons/components/uni-icons/uni-icons.js";
if (!Math) {
  (_easycom_item + _easycom_uni_icons)();
}
const __default__ = {
  // 正确的配置方式
  options: {
    enablePullDownRefresh: true,
    backgroundTextStyle: "dark"
  },
  // 设置导航栏样式
  navigationStyle: "custom"
};
const _sfc_main = /* @__PURE__ */ Object.assign(__default__, {
  __name: "find",
  setup(__props) {
    const noteList = common_vendor.ref([]);
    const categories = common_vendor.ref(["全部", "开心", "难过", "焦虑", "兴奋", "平静", "孤独", "感恩"]);
    const activeCategory = common_vendor.ref(0);
    const currentPage = common_vendor.ref(1);
    const pageSize = common_vendor.ref(10);
    const loading = common_vendor.ref(false);
    const noMoreData = common_vendor.ref(false);
    const showBackToTop = common_vendor.ref(false);
    const scrollTop = common_vendor.ref(0);
    const isRefreshing = common_vendor.ref(false);
    common_vendor.onMounted(() => {
      fetchNoteList();
    });
    common_vendor.onPageScroll((e) => {
      scrollTop.value = e.scrollTop;
      showBackToTop.value = scrollTop.value > 300;
    });
    async function fetchNoteList(append = false) {
      if (loading.value)
        return;
      try {
        loading.value = true;
        const emotion = activeCategory.value === 0 ? void 0 : categories.value[activeCategory.value];
        const params = {
          pageNum: currentPage.value,
          pageSize: pageSize.value,
          emotion
        };
        const res = await api_note.getNoteList(params);
        if (res.code === 1) {
          if (append) {
            noteList.value = [...noteList.value, ...res.data.records];
          } else {
            noteList.value = res.data.records;
          }
          noMoreData.value = res.data.records.length < pageSize.value;
          if (currentPage.value === 1 && noteList.value.length === 0) {
            noMoreData.value = true;
          }
        } else {
          common_vendor.index.showToast({
            title: "获取笔记失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("获取笔记列表失败:", error);
        common_vendor.index.showToast({
          title: "网络错误，请重试",
          icon: "none"
        });
      } finally {
        loading.value = false;
        isRefreshing.value = false;
      }
    }
    function onRefresh() {
      console.log("scroll-view 下拉刷新触发");
      isRefreshing.value = true;
      currentPage.value = 1;
      noMoreData.value = false;
      fetchNoteList(false);
    }
    function onLoadMore() {
      console.log("滚动到底部，加载更多");
      if (!loading.value && !noMoreData.value) {
        currentPage.value++;
        fetchNoteList(true);
      }
    }
    common_vendor.onPullDownRefresh(() => {
      console.log("页面下拉刷新触发");
      currentPage.value = 1;
      noMoreData.value = false;
      fetchNoteList(false);
    });
    common_vendor.onReachBottom(() => {
      if (!noMoreData.value) {
        currentPage.value++;
        fetchNoteList(true);
      }
    });
    function selectCategory(index) {
      if (activeCategory.value === index)
        return;
      activeCategory.value = index;
      currentPage.value = 1;
      noMoreData.value = false;
      fetchNoteList(false);
    }
    function goToNote(noteId) {
      common_vendor.index.navigateTo({
        url: `/pages/note/note?id=${noteId}`
      });
    }
    function scrollToTop() {
      common_vendor.index.pageScrollTo({
        scrollTop: 0,
        duration: 300
      });
    }
    function goToSearch() {
      common_vendor.index.navigateTo({
        url: "/pages/search/search"
      });
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goToSearch),
        b: common_vendor.f(categories.value, (category, index, i0) => {
          return {
            a: common_vendor.t(category),
            b: index,
            c: activeCategory.value === index ? 1 : "",
            d: common_vendor.o(($event) => selectCategory(index), index)
          };
        }),
        c: common_vendor.f(noteList.value, (note, index, i0) => {
          return {
            a: "1c765c2e-0-" + i0,
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
        }),
        d: loading.value
      }, loading.value ? {} : {}, {
        e: noMoreData.value
      }, noMoreData.value ? {} : {}, {
        f: isRefreshing.value,
        g: common_vendor.o(onRefresh),
        h: common_vendor.o(onLoadMore),
        i: showBackToTop.value
      }, showBackToTop.value ? {
        j: common_vendor.p({
          type: "top",
          size: "30",
          color: "#1a73e8"
        }),
        k: common_vendor.o(scrollToTop)
      } : {});
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-1c765c2e"], ["__file", "C:/Users/86182/Desktop/上班/解压小程序/前端/JieYa/pages/find/find.vue"]]);
_sfc_main.__runtimeHooks = 1;
wx.createPage(MiniProgramPage);
