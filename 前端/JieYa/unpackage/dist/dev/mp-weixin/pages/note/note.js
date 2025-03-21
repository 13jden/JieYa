"use strict";
const common_vendor = require("../../common/vendor.js");
const defaultAvatar = "https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0";
const _sfc_main = {
  __name: "note",
  setup(__props) {
    const isPreviewMode = common_vendor.ref(false);
    const noteData = common_vendor.ref({
      title: "",
      content: "",
      tags: [],
      images: [],
      visibility: "public"
    });
    const comments = common_vendor.ref([]);
    const commentText = common_vendor.ref("");
    const replyInfo = common_vendor.ref({
      isReplying: false,
      commentId: null,
      username: ""
    });
    common_vendor.onMounted(() => {
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      let options = {};
      if (currentPage && currentPage.options) {
        options = currentPage.options;
      } else {
        const query = common_vendor.index.getSystemInfoSync().platform === "devtools" ? common_vendor.index.$route && common_vendor.index.$route.query : null;
        options = query || {};
      }
      console.log("页面参数:", options);
      if (options.id) {
        console.log("从发现页进入，笔记ID:", options.id);
        isPreviewMode.value = false;
        loadNoteData(options.id);
        loadComments();
      } else {
        try {
          const previewData = common_vendor.wx$1.getStorageSync("notePreview");
          if (previewData && previewData.isPreview) {
            console.log("预览模式已启用");
            isPreviewMode.value = true;
            noteData.value = previewData;
          } else {
            isPreviewMode.value = false;
            loadNoteData();
            loadComments();
          }
        } catch (e) {
          console.error("获取预览数据失败:", e);
          isPreviewMode.value = false;
          loadNoteData();
          loadComments();
        }
      }
    });
    function loadNoteData(id) {
      noteData.value = {
        title: "示例笔记标题",
        content: "这是笔记的内容，实际项目中应该从后端API获取。",
        tags: ["示例标签1", "示例标签2"],
        images: [
          "https://picsum.photos/500/300?random=1",
          "https://picsum.photos/500/300?random=2",
          "https://picsum.photos/500/300?random=3"
        ],
        visibility: "public"
      };
    }
    function loadComments() {
      comments.value = [
        {
          id: 1,
          username: "用户A",
          avatar: "https://picsum.photos/100/100?random=1",
          content: "这是一条父评论",
          time: Date.now() - 36e5,
          replies: [
            {
              id: 11,
              username: "用户B",
              replyToUsername: "用户A",
              avatar: "https://picsum.photos/100/100?random=2",
              content: "这是对父评论的回复",
              time: Date.now() - 18e5
            }
          ]
        },
        {
          id: 2,
          username: "用户C",
          avatar: "https://picsum.photos/100/100?random=3",
          content: "这是另一条父评论",
          time: Date.now() - 72e5,
          replies: []
        }
      ];
    }
    function formatTime(timestamp) {
      const now = Date.now();
      const diff = now - timestamp;
      if (diff < 6e4) {
        return "刚刚";
      }
      if (diff < 36e5) {
        return Math.floor(diff / 6e4) + "分钟前";
      }
      if (diff < 864e5) {
        return Math.floor(diff / 36e5) + "小时前";
      }
      if (diff < 6048e5) {
        return Math.floor(diff / 864e5) + "天前";
      }
      const date = new Date(timestamp);
      return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`;
    }
    function replyTo(commentId, username) {
      replyInfo.value = {
        isReplying: true,
        commentId,
        username
      };
      common_vendor.index.$nextTick(() => {
        const query = common_vendor.index.createSelectorQuery();
        query.select(".comment-input").boundingClientRect();
        query.exec((res) => {
          common_vendor.index.pageScrollTo({
            selector: ".comment-input",
            duration: 300
          });
        });
      });
    }
    function submitComment() {
      if (!commentText.value.trim())
        return;
      if (replyInfo.value.isReplying) {
        const parentComment = comments.value.find((c) => c.id === replyInfo.value.commentId);
        if (parentComment) {
          parentComment.replies.push({
            id: Date.now(),
            username: "当前用户",
            // 实际项目中应该是登录用户名
            replyToUsername: replyInfo.value.username,
            avatar: defaultAvatar,
            // 实际项目中应该是登录用户头像
            content: commentText.value,
            time: Date.now()
          });
        }
      } else {
        comments.value.unshift({
          id: Date.now(),
          username: "当前用户",
          // 实际项目中应该是登录用户名
          avatar: defaultAvatar,
          // 实际项目中应该是登录用户头像
          content: commentText.value,
          time: Date.now(),
          replies: []
        });
      }
      commentText.value = "";
      replyInfo.value = {
        isReplying: false,
        commentId: null,
        username: ""
      };
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.f(noteData.value.images, (image, index, i0) => {
          return {
            a: image,
            b: index
          };
        }),
        b: common_vendor.t(noteData.value.title || "无标题"),
        c: common_vendor.t(noteData.value.content || "暂无内容"),
        d: noteData.value.tags && noteData.value.tags.length > 0
      }, noteData.value.tags && noteData.value.tags.length > 0 ? {
        e: common_vendor.f(noteData.value.tags, (tag, index, i0) => {
          return {
            a: common_vendor.t(tag),
            b: index
          };
        })
      } : {}, {
        f: common_vendor.t(noteData.value.visibility === "private" ? "🔒" : noteData.value.visibility === "friends" ? "👥" : "🌐"),
        g: common_vendor.t(noteData.value.visibility === "private" ? "仅自己可见" : noteData.value.visibility === "friends" ? "好友可见" : "所有人可见"),
        h: !isPreviewMode.value
      }, !isPreviewMode.value ? {
        i: common_vendor.f(comments.value, (comment, index, i0) => {
          return common_vendor.e({
            a: comment.avatar || _ctx.defaultAvatar,
            b: common_vendor.t(comment.username),
            c: common_vendor.t(formatTime(comment.time)),
            d: common_vendor.t(comment.content),
            e: common_vendor.o(($event) => replyTo(comment.id, comment.username), index),
            f: comment.replies && comment.replies.length > 0
          }, comment.replies && comment.replies.length > 0 ? {
            g: common_vendor.f(comment.replies, (reply, replyIndex, i1) => {
              return {
                a: reply.avatar || _ctx.defaultAvatar,
                b: common_vendor.t(reply.username),
                c: common_vendor.t(reply.replyToUsername),
                d: common_vendor.t(formatTime(reply.time)),
                e: common_vendor.t(reply.content),
                f: common_vendor.o(($event) => replyTo(comment.id, reply.username), replyIndex),
                g: replyIndex
              };
            })
          } : {}, {
            h: index
          });
        }),
        j: replyInfo.value.isReplying ? `回复 ${replyInfo.value.username}：` : "说点什么...",
        k: common_vendor.o(submitComment),
        l: commentText.value,
        m: common_vendor.o(($event) => commentText.value = $event.detail.value),
        n: common_vendor.o(submitComment),
        o: !commentText.value.trim()
      } : {}, {
        p: isPreviewMode.value
      }, isPreviewMode.value ? {} : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-f9d84dbe"], ["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/pages/note/note.vue"]]);
wx.createPage(MiniProgramPage);
