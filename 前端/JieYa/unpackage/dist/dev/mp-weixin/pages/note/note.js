"use strict";
const common_vendor = require("../../common/vendor.js");
const api_note = require("../../api/note.js");
const api_userActive = require("../../api/userActive.js");
const api_comment = require("../../api/comment.js");
const api_focus = require("../../api/focus.js");
if (!Array) {
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  _easycom_uni_icons2();
}
const _easycom_uni_icons = () => "../../uni_modules/uni-icons/components/uni-icons/uni-icons.js";
if (!Math) {
  _easycom_uni_icons();
}
const defaultAvatar = "https://jiayaya.oss-cn-hangzhou.aliyuncs.com/avatar.jpg";
const _sfc_main = {
  __name: "note",
  setup(__props) {
    const noteId = common_vendor.ref("");
    const noteData = common_vendor.ref({});
    const comments = common_vendor.ref([]);
    const loading = common_vendor.ref(true);
    const commentContent = common_vendor.ref("");
    const isLiked = common_vendor.ref(false);
    const isCollected = common_vendor.ref(false);
    const isFollowing = common_vendor.ref(false);
    const currentUser = common_vendor.ref(null);
    const isPreview = common_vendor.ref(false);
    const replyingTo = common_vendor.ref(null);
    const replyParentId = common_vendor.ref(null);
    const replyToUsername = common_vendor.ref("");
    const replyToUserId = common_vendor.ref("");
    const currentCommentPage = common_vendor.ref(1);
    const commentPageSize = common_vendor.ref(10);
    const hasMoreComments = common_vendor.ref(false);
    const commentCount = common_vendor.ref(0);
    const replyPlaceholder = common_vendor.computed(() => {
      if (replyingTo.value) {
        return `回复 @${replyToUsername.value}`;
      }
      return "写下你的评论...";
    });
    common_vendor.computed(() => {
      if (!currentUser.value || !noteData.value.user)
        return false;
      return currentUser.value.userId === noteData.value.user.userId;
    });
    common_vendor.computed(() => {
      if (noteData.value && noteData.value.noteImages && noteData.value.noteImages.length > 0) {
        return noteData.value.noteImages;
      }
      return [];
    });
    const parsedTags = common_vendor.computed(() => {
      if (!noteData.value.tags)
        return [];
      if (typeof noteData.value.tags === "string") {
        try {
          const parsed = JSON.parse(noteData.value.tags);
          if (Array.isArray(parsed) && parsed.length > 0) {
            if (typeof parsed[0] === "string" && parsed[0].startsWith("[")) {
              try {
                return JSON.parse(parsed[0]);
              } catch {
                return parsed;
              }
            }
            return parsed;
          }
          return [];
        } catch {
          return [noteData.value.tags];
        }
      }
      return noteData.value.tags;
    });
    const previewImages = common_vendor.computed(() => {
      if (isPreview.value && noteData.value.images) {
        return noteData.value.images;
      } else if (noteData.value.noteImages && noteData.value.noteImages.length > 0) {
        return noteData.value.noteImages.map((img) => img.imagePath);
      } else if (noteData.value.coverImage) {
        return [formatImageUrl(noteData.value.coverImage)];
      }
      return [];
    });
    common_vendor.onLoad((options) => {
      if (options.id) {
        noteId.value = options.id;
        isPreview.value = false;
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        if (userInfo) {
          currentUser.value = typeof userInfo === "string" ? JSON.parse(userInfo) : userInfo;
        }
        fetchNoteDetail();
        fetchComments();
      } else {
        isPreview.value = true;
        loadPreviewData();
      }
    });
    function loadPreviewData() {
      try {
        loading.value = true;
        const previewData = common_vendor.index.getStorageSync("notePreview");
        if (previewData) {
          noteData.value = previewData;
          console.log("预览数据:", previewData);
        } else {
          common_vendor.index.showToast({
            title: "没有预览数据",
            icon: "none"
          });
          setTimeout(() => {
            common_vendor.index.navigateBack();
          }, 1500);
        }
      } catch (error) {
        console.error("加载预览数据失败:", error);
        common_vendor.index.showToast({
          title: "加载预览失败",
          icon: "none"
        });
      } finally {
        loading.value = false;
      }
    }
    async function fetchNoteDetail() {
      if (isPreview.value)
        return;
      try {
        loading.value = true;
        const res = await api_note.getNoteDetail(noteId.value);
        if (res.code === 1 && res.data) {
          noteData.value = res.data;
          if (currentUser.value) {
            checkUserInteractions();
          }
        } else {
          common_vendor.index.showToast({
            title: "获取笔记详情失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("获取笔记详情失败:", error);
        common_vendor.index.showToast({
          title: "网络错误，请重试",
          icon: "none"
        });
      } finally {
        loading.value = false;
      }
    }
    async function checkUserInteractions() {
      if (!currentUser.value)
        return;
      try {
        const activeRes = await api_userActive.checkUserActive(noteId.value);
        if (activeRes.code === 1) {
          isLiked.value = activeRes.data.like;
          isCollected.value = activeRes.data.collect;
        }
        if (noteData.value && noteData.value.isFocus !== void 0) {
          if (noteData.value.isFocus === 0) {
          } else if (noteData.value.isFocus === 1) {
            isFollowing.value = true;
          } else if (noteData.value.isFocus === -1) {
            isFollowing.value = false;
          }
        }
      } catch (error) {
        console.error("检查交互状态失败:", error);
      }
    }
    async function fetchComments(append = false) {
      try {
        const res = await api_comment.getCommentsByNoteId(noteId.value, currentCommentPage.value, commentPageSize.value);
        if (res.code === 1) {
          const formattedComments = res.data.records.map((comment) => ({
            ...comment,
            showChildren: false
            // 添加showChildren属性，默认不显示子评论
          }));
          if (append) {
            comments.value = [...comments.value, ...formattedComments];
          } else {
            comments.value = formattedComments;
          }
          commentCount.value = res.data.total;
          hasMoreComments.value = comments.value.length < res.data.total;
        }
      } catch (error) {
        console.error("获取评论失败:", error);
      }
    }
    async function loadChildComments(parentId, pageNum = 1) {
      try {
        const res = await api_comment.getChildComments(parentId, pageNum, 5);
        if (res.code === 1) {
          const parentIndex = comments.value.findIndex((c) => c.id === parentId);
          if (parentIndex !== -1) {
            if (!comments.value[parentIndex].children) {
              comments.value[parentIndex].children = [];
            }
            if (pageNum === 1) {
              comments.value[parentIndex].children = res.data.records;
            } else {
              comments.value[parentIndex].children = [
                ...comments.value[parentIndex].children,
                ...res.data.records
              ];
            }
            comments.value[parentIndex].childrenCount = res.data.total;
          }
        }
      } catch (error) {
        console.error("获取子评论失败:", error);
        common_vendor.index.showToast({
          title: "加载回复失败",
          icon: "none"
        });
      }
    }
    function loadMoreComments() {
      currentCommentPage.value++;
      fetchComments(true);
    }
    function loadMoreReplies(parentId) {
      const parentComment = comments.value.find((c) => c.id === parentId);
      if (parentComment && parentComment.children) {
        const currentPage = Math.ceil(parentComment.children.length / 5);
        loadChildComments(parentId, currentPage + 1);
      }
    }
    async function handleLike() {
      if (!currentUser.value) {
        common_vendor.index.showToast({
          title: "请先登录",
          icon: "none"
        });
        return;
      }
      try {
        if (isLiked.value) {
          await api_userActive.deleteLike(noteData.value.user.userId, noteId.value);
          noteData.value.likeCount = Math.max(0, (noteData.value.likeCount || 1) - 1);
        } else {
          await api_userActive.addLike(noteData.value.user.userId, noteId.value);
          noteData.value.likeCount = (noteData.value.likeCount || 0) + 1;
        }
        isLiked.value = !isLiked.value;
        common_vendor.index.showToast({
          title: isLiked.value ? "已点赞" : "已取消点赞",
          icon: "none"
        });
      } catch (error) {
        console.error("点赞操作失败:", error);
        common_vendor.index.showToast({
          title: "操作失败，请重试",
          icon: "none"
        });
      }
    }
    async function handleCollect() {
      if (!currentUser.value) {
        common_vendor.index.showToast({
          title: "请先登录",
          icon: "none"
        });
        return;
      }
      try {
        if (isCollected.value) {
          await api_userActive.deleteCollect(currentUser.value.userId, noteId.value);
          noteData.value.collectCount = Math.max(0, (noteData.value.collectCount || 1) - 1);
        } else {
          await api_userActive.addCollect(currentUser.value.userId, noteId.value);
          noteData.value.collectCount = (noteData.value.collectCount || 0) + 1;
        }
        isCollected.value = !isCollected.value;
        common_vendor.index.showToast({
          title: isCollected.value ? "已收藏" : "已取消收藏",
          icon: "none"
        });
      } catch (error) {
        console.error("收藏操作失败:", error);
        common_vendor.index.showToast({
          title: "操作失败，请重试",
          icon: "none"
        });
      }
    }
    async function handleFollow() {
      if (!currentUser.value) {
        common_vendor.index.showToast({
          title: "请先登录",
          icon: "none"
        });
        return;
      }
      if (!noteData.value.user || !noteData.value.user.userId) {
        common_vendor.index.showToast({
          title: "作者信息缺失",
          icon: "none"
        });
        return;
      }
      try {
        if (isFollowing.value) {
          await api_focus.deleteFocus(currentUser.value.userId, noteData.value.user.userId);
          noteData.value.isFocus = -1;
        } else {
          await api_focus.addFocus(currentUser.value.userId, noteData.value.user.userId);
          noteData.value.isFocus = 1;
        }
        isFollowing.value = !isFollowing.value;
        common_vendor.index.showToast({
          title: isFollowing.value ? "已关注" : "已取消关注",
          icon: "none"
        });
      } catch (error) {
        console.error("关注操作失败:", error);
        common_vendor.index.showToast({
          title: "操作失败，请重试",
          icon: "none"
        });
      }
    }
    function replyTo(comment, replyToUser) {
      const isParentComment = !comment.parentId;
      replyParentId.value = isParentComment ? String(comment.id) : String(comment.parentId);
      replyToUserId.value = isParentComment ? null : replyToUser.userId;
      replyToUsername.value = replyToUser.userName;
      replyingTo.value = true;
      setTimeout(() => {
        common_vendor.index.createSelectorQuery().select(".comment-input").boundingClientRect((data) => {
          console.log("input rect:", data);
        }).exec();
      }, 100);
    }
    async function submitComment() {
      if (!currentUser.value) {
        common_vendor.index.showToast({
          title: "请先登录",
          icon: "none"
        });
        return;
      }
      if (!commentContent.value.trim()) {
        common_vendor.index.showToast({
          title: "评论内容不能为空",
          icon: "none"
        });
        return;
      }
      try {
        common_vendor.index.showLoading({
          title: "发送中..."
        });
        const commentData = {
          content: commentContent.value,
          noteId: noteId.value
          // 始终传递noteId
        };
        if (replyingTo.value && replyParentId.value) {
          commentData.parentId = String(replyParentId.value);
          console.log("回复评论，parentId:", commentData.parentId);
          if (replyToUserId.value) {
            commentData.toUser = String(replyToUserId.value);
          }
        }
        console.log("发送评论数据:", commentData);
        const res = await api_comment.addComment(commentData);
        if (res.code === 1) {
          common_vendor.index.showToast({
            title: "评论成功",
            icon: "success"
          });
          if (!replyingTo.value) {
            const newComment = {
              id: res.data ? res.data : (/* @__PURE__ */ new Date()).getTime().toString(),
              content: commentContent.value,
              userId: currentUser.value.userId,
              time: (/* @__PURE__ */ new Date()).toISOString(),
              userName: currentUser.value.nickName,
              avatar: currentUser.value.avatar || defaultAvatar,
              childrenCount: 0,
              showChildren: false
            };
            comments.value.unshift(newComment);
            commentCount.value += 1;
          } else {
            const parentId = String(replyParentId.value);
            const parentComment = comments.value.find((c) => String(c.id) === parentId);
            if (parentComment) {
              parentComment.showChildren = true;
              const newReply = {
                id: res.data ? res.data : (/* @__PURE__ */ new Date()).getTime().toString(),
                content: commentContent.value,
                parentId,
                userId: currentUser.value.userId,
                time: (/* @__PURE__ */ new Date()).toISOString(),
                userName: currentUser.value.nickName,
                avatar: currentUser.value.avatar || defaultAvatar,
                toUserId: replyToUserId.value,
                replyName: replyToUsername.value
              };
              if (!parentComment.children) {
                parentComment.children = [];
              }
              parentComment.children.unshift(newReply);
              parentComment.childrenCount = (parentComment.childrenCount || 0) + 1;
            }
          }
          commentContent.value = "";
          replyingTo.value = null;
          replyParentId.value = null;
          replyToUsername.value = "";
          replyToUserId.value = "";
        } else {
          common_vendor.index.showToast({
            title: res.message || "评论失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("发送评论失败:", error);
        common_vendor.index.showToast({
          title: "网络错误，请重试",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
      }
    }
    function goToUser(userId) {
      if (!userId)
        return;
      common_vendor.index.navigateTo({
        url: `/pages/user/user?id=${userId}`
      });
    }
    function formatTime(timestamp) {
      if (!timestamp)
        return "";
      const date = new Date(timestamp);
      const now = /* @__PURE__ */ new Date();
      const diffMs = now - date;
      const diffSec = Math.floor(diffMs / 1e3);
      const diffMin = Math.floor(diffSec / 60);
      const diffHour = Math.floor(diffMin / 60);
      const diffDay = Math.floor(diffHour / 24);
      if (diffSec < 60) {
        return "刚刚";
      } else if (diffMin < 60) {
        return `${diffMin}分钟前`;
      } else if (diffHour < 24) {
        return `${diffHour}小时前`;
      } else if (diffDay < 30) {
        return `${diffDay}天前`;
      } else {
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
      }
    }
    function formatImageUrl(url) {
      if (!url)
        return "";
      if (url.startsWith("http://") || url.startsWith("https://")) {
        return url;
      }
      const baseUrl = "http://localhost:8081";
      if (url.startsWith("/image")) {
        return baseUrl + url;
      }
      return `${baseUrl}/image/note/${url}`;
    }
    function previewImage(currentUrl, index) {
      if (isPreview.value && noteData.value.images) {
        common_vendor.index.previewImage({
          current: currentUrl,
          urls: noteData.value.images
        });
      } else if (noteData.value.noteImages && noteData.value.noteImages.length > 0) {
        const urls = noteData.value.noteImages.map((img) => img.imagePath);
        common_vendor.index.previewImage({
          current: currentUrl,
          urls
        });
      } else if (noteData.value.coverImage) {
        const coverImageUrl = formatImageUrl(noteData.value.coverImage);
        common_vendor.index.previewImage({
          current: coverImageUrl,
          urls: [coverImageUrl]
        });
      }
    }
    async function handleViewReplies(parentId) {
      const comment = comments.value.find((c) => c.id === parentId);
      if (!comment)
        return;
      if (comment.showChildren) {
        comment.showChildren = false;
      } else {
        comment.showChildren = true;
        if (!comment.children || comment.children.length === 0) {
          await loadChildComments(parentId);
        }
      }
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: loading.value
      }, loading.value ? {} : common_vendor.e({
        b: isPreview.value
      }, isPreview.value ? {} : {}, {
        c: !isPreview.value && noteData.value.user
      }, !isPreview.value && noteData.value.user ? common_vendor.e({
        d: noteData.value.user.avatar || _ctx.defaultAvatar,
        e: common_vendor.t(noteData.value.user.nickName || "未知用户"),
        f: common_vendor.t(noteData.value.user.school || ""),
        g: common_vendor.o(($event) => goToUser(noteData.value.user.userId)),
        h: currentUser.value && currentUser.value.userId !== noteData.value.user.userId
      }, currentUser.value && currentUser.value.userId !== noteData.value.user.userId ? {
        i: common_vendor.t(isFollowing.value ? "已关注" : "关注"),
        j: isFollowing.value ? 1 : "",
        k: common_vendor.o(handleFollow)
      } : {}) : {}, {
        l: common_vendor.f(previewImages.value, (image, index, i0) => {
          return {
            a: image,
            b: index,
            c: common_vendor.o(($event) => previewImage(image), index)
          };
        }),
        m: common_vendor.t(noteData.value.title || "无标题"),
        n: common_vendor.t(noteData.value.content || "暂无内容"),
        o: noteData.value.tags && noteData.value.tags.length > 0
      }, noteData.value.tags && noteData.value.tags.length > 0 ? {
        p: common_vendor.f(parsedTags.value, (tag, index, i0) => {
          return {
            a: common_vendor.t(tag),
            b: index
          };
        })
      } : {}, {
        q: !isPreview.value
      }, !isPreview.value ? {
        r: common_vendor.p({
          type: isLiked.value ? "heart-filled" : "heart",
          size: "24",
          color: isLiked.value ? "#ff4757" : "#999"
        }),
        s: common_vendor.t(noteData.value.likeCount || 0),
        t: isLiked.value ? 1 : "",
        v: common_vendor.o(handleLike),
        w: common_vendor.p({
          type: isCollected.value ? "star-filled" : "star",
          size: "24",
          color: isCollected.value ? "#ffbc00" : "#999"
        }),
        x: common_vendor.t(noteData.value.collectCount || 0),
        y: isCollected.value ? 1 : "",
        z: common_vendor.o(handleCollect),
        A: common_vendor.p({
          type: "chat",
          size: "24",
          color: "#999"
        }),
        B: common_vendor.t(commentCount.value),
        C: common_vendor.p({
          type: "download",
          size: "24",
          color: "#999"
        })
      } : {}, {
        D: !isPreview.value
      }, !isPreview.value ? common_vendor.e({
        E: common_vendor.t(commentCount.value),
        F: comments.value.length === 0
      }, comments.value.length === 0 ? {} : {}, {
        G: common_vendor.f(comments.value, (comment, index, i0) => {
          return common_vendor.e({
            a: comment.avatar || _ctx.defaultAvatar,
            b: common_vendor.o(($event) => goToUser(comment.userId), index),
            c: common_vendor.t(comment.userName),
            d: common_vendor.o(($event) => goToUser(comment.userId), index),
            e: common_vendor.t(formatTime(comment.time)),
            f: common_vendor.t(comment.content),
            g: common_vendor.o(($event) => replyTo(comment, {
              userId: comment.userId,
              userName: comment.userName
            }), index),
            h: comment.childrenCount > 0
          }, comment.childrenCount > 0 ? {
            i: common_vendor.t(comment.showChildren ? "收起回复" : `查看${comment.childrenCount}条回复`),
            j: common_vendor.o(($event) => handleViewReplies(comment.id), index)
          } : {}, {
            k: comment.showChildren && comment.children && comment.children.length > 0
          }, comment.showChildren && comment.children && comment.children.length > 0 ? common_vendor.e({
            l: common_vendor.f(comment.children, (reply, replyIndex, i1) => {
              return common_vendor.e({
                a: reply.avatar || _ctx.defaultAvatar,
                b: common_vendor.o(($event) => goToUser(reply.userId), replyIndex),
                c: common_vendor.t(reply.userName),
                d: common_vendor.o(($event) => goToUser(reply.userId), replyIndex),
                e: reply.toUserId
              }, reply.toUserId ? {} : {}, {
                f: reply.toUserId
              }, reply.toUserId ? {
                g: common_vendor.t(reply.replyName),
                h: common_vendor.o(($event) => goToUser(reply.toUserId), replyIndex)
              } : {}, {
                i: common_vendor.t(formatTime(reply.time)),
                j: common_vendor.t(reply.content),
                k: common_vendor.o(($event) => replyTo(reply, {
                  userId: reply.userId,
                  userName: reply.userName
                }), replyIndex),
                l: replyIndex
              });
            }),
            m: comment.childrenCount > comment.children.length
          }, comment.childrenCount > comment.children.length ? {
            n: common_vendor.o(($event) => loadMoreReplies(comment.id), index)
          } : {}) : {}, {
            o: index
          });
        }),
        H: hasMoreComments.value
      }, hasMoreComments.value ? {
        I: common_vendor.o(loadMoreComments)
      } : {}) : {}), {
        J: !loading.value && !isPreview.value
      }, !loading.value && !isPreview.value ? {
        K: replyPlaceholder.value,
        L: common_vendor.o(submitComment),
        M: commentContent.value,
        N: common_vendor.o(($event) => commentContent.value = $event.detail.value),
        O: !commentContent.value.trim(),
        P: common_vendor.o(submitComment)
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-f9d84dbe"], ["__file", "C:/Users/86182/Desktop/上班/解压小程序/前端/JieYa/pages/note/note.vue"]]);
wx.createPage(MiniProgramPage);
