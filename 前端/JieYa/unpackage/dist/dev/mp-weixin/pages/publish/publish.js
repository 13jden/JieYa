"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  __name: "publish",
  setup(__props) {
    common_vendor.ref("");
    const title = common_vendor.ref("");
    const content = common_vendor.ref("");
    const filelist = common_vendor.ref([]);
    const imagelist = common_vendor.ref([]);
    const selectedTags = common_vendor.ref([]);
    const newTag = common_vendor.ref("");
    const dragIndex = common_vendor.ref(-1);
    const dragStartX = common_vendor.ref(0);
    const dragCurrentX = common_vendor.ref(0);
    const isDragging = common_vendor.ref(false);
    const dragElement = common_vendor.ref(null);
    const visibility = common_vendor.ref("public");
    common_vendor.onMounted(() => {
      checkDraft();
    });
    function checkDraft() {
      try {
        const draftData = common_vendor.wx$1.getStorageSync("noteDraft");
        if (draftData) {
          common_vendor.index.showModal({
            title: "发现草稿",
            content: "是否继续编辑上次的草稿？",
            confirmText: "继续编辑",
            cancelText: "新建笔记",
            success: (res) => {
              if (res.confirm) {
                loadDraft();
                common_vendor.index.showToast({
                  title: "已加载草稿",
                  icon: "success",
                  duration: 2e3
                });
              } else {
                common_vendor.wx$1.removeStorageSync("noteDraft");
                resetForm();
                common_vendor.index.showToast({
                  title: "已创建新笔记",
                  icon: "success",
                  duration: 2e3
                });
              }
            }
          });
        }
      } catch (e) {
        console.error("检查草稿失败:", e);
      }
    }
    function resetForm() {
      title.value = "";
      content.value = "";
      selectedTags.value = [];
      filelist.value = [];
      imagelist.value = [];
      visibility.value = "public";
    }
    function loadDraft() {
      try {
        const draftData = common_vendor.wx$1.getStorageSync("noteDraft");
        if (draftData) {
          title.value = draftData.title || "";
          content.value = draftData.content || "";
          selectedTags.value = draftData.tags || [];
          filelist.value = draftData.images || [];
          visibility.value = draftData.visibility || "public";
        }
      } catch (e) {
        console.error("加载草稿失败:", e);
      }
    }
    function saveDraft() {
      try {
        const draftData = {
          title: title.value,
          content: content.value,
          tags: selectedTags.value,
          images: filelist.value,
          visibility: visibility.value,
          timestamp: Date.now()
        };
        common_vendor.wx$1.setStorageSync("noteDraft", draftData);
        common_vendor.index.showToast({
          title: "草稿保存成功",
          icon: "success",
          duration: 2e3
        });
      } catch (e) {
        console.error("保存草稿失败:", e);
        common_vendor.index.showToast({
          title: "草稿保存失败",
          icon: "none",
          duration: 2e3
        });
      }
    }
    function previewNote() {
      try {
        const previewData = {
          title: title.value,
          content: content.value,
          tags: selectedTags.value,
          images: filelist.value,
          visibility: visibility.value,
          isPreview: true
          // 标记为预览模式
        };
        common_vendor.wx$1.setStorageSync("notePreview", previewData);
        common_vendor.index.navigateTo({
          url: "/pages/note/note"
        });
      } catch (e) {
        console.error("预览失败:", e);
        common_vendor.index.showToast({
          title: "预览失败",
          icon: "none",
          duration: 2e3
        });
      }
    }
    function chooseFile() {
      common_vendor.index.chooseImage({
        count: 9,
        success: (res) => {
          res.tempFilePaths.forEach((filePath) => {
            filelist.value.push(filePath);
          });
        },
        fail: (err) => {
          console.log("选择文件失败: ", err);
        }
      });
    }
    function removeImage(index) {
      filelist.value.splice(index, 1);
      imagelist.value.splice(index, 1);
    }
    function addTag() {
      if (newTag.value) {
        if (!selectedTags.value.includes(newTag.value)) {
          selectedTags.value.push(`${newTag.value}`);
          newTag.value = "";
        } else {
          common_vendor.index.showToast({
            title: "该话题已添加",
            icon: "none"
          });
        }
      }
    }
    function removeTag(index) {
      selectedTags.value.splice(index, 1);
    }
    function getItemStyle(index) {
      if (!isDragging.value || dragIndex.value !== index) {
        return {};
      }
      const moveX = dragCurrentX.value - dragStartX.value;
      return {
        transform: `translateX(${moveX}px) scale(1.05)`,
        zIndex: "10",
        transition: "transform 0.05s ease"
      };
    }
    function dragStart(e) {
      dragIndex.value = e.currentTarget.dataset.index;
      dragStartX.value = e.touches[0].clientX;
      dragCurrentX.value = dragStartX.value;
      isDragging.value = true;
      dragElement.value = e.currentTarget;
    }
    function dragMove(e) {
      if (dragIndex.value < 0 || !isDragging.value)
        return;
      dragCurrentX.value = e.touches[0].clientX;
      const moveDistance = dragCurrentX.value - dragStartX.value;
      const itemWidth = 220;
      const moveItems = Math.round(moveDistance / itemWidth);
      const targetIndex = Math.max(0, Math.min(filelist.value.length - 1, dragIndex.value + moveItems));
      if (targetIndex !== dragIndex.value) {
        const temp = filelist.value[dragIndex.value];
        filelist.value.splice(dragIndex.value, 1);
        filelist.value.splice(targetIndex, 0, temp);
        dragIndex.value = targetIndex;
        dragStartX.value = dragCurrentX.value;
      }
    }
    function dragEnd(e) {
      if (!isDragging.value)
        return;
      isDragging.value = false;
      dragIndex.value = -1;
      dragElement.value = null;
    }
    function setVisibility(type) {
      visibility.value = type;
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.f(filelist.value, (item, index, i0) => {
          return {
            a: item,
            b: common_vendor.o(($event) => removeImage(index), index),
            c: index,
            d: index,
            e: isDragging.value && dragIndex.value.value === index ? 1 : "",
            f: common_vendor.s(getItemStyle(index)),
            g: common_vendor.o(dragStart, index),
            h: common_vendor.o(dragMove, index),
            i: common_vendor.o(dragEnd, index)
          };
        }),
        b: filelist.value.length < 9
      }, filelist.value.length < 9 ? {
        c: common_vendor.o(chooseFile)
      } : {}, {
        d: title.value,
        e: common_vendor.o(($event) => title.value = $event.detail.value),
        f: content.value,
        g: common_vendor.o(($event) => content.value = $event.detail.value),
        h: common_vendor.f(selectedTags.value, (tag, index, i0) => {
          return {
            a: common_vendor.t(tag),
            b: common_vendor.o(($event) => removeTag(index), index),
            c: index
          };
        }),
        i: common_vendor.o(addTag),
        j: newTag.value,
        k: common_vendor.o(($event) => newTag.value = $event.detail.value),
        l: common_vendor.o(($event) => addTag()),
        m: !newTag.value,
        n: newTag.value ? "#1a73e8" : "#eee",
        o: visibility.value === "public" ? 1 : "",
        p: common_vendor.o(($event) => setVisibility("public")),
        q: visibility.value === "friends" ? 1 : "",
        r: common_vendor.o(($event) => setVisibility("friends")),
        s: visibility.value === "private" ? 1 : "",
        t: common_vendor.o(($event) => setVisibility("private")),
        v: common_vendor.o(saveDraft),
        w: common_vendor.o(previewNote),
        x: common_vendor.o((...args) => _ctx.finish && _ctx.finish(...args)),
        y: !filelist.value.length
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-bfce3555"], ["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/pages/publish/publish.vue"]]);
wx.createPage(MiniProgramPage);
