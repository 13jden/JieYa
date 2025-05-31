"use strict";
const common_vendor = require("../../common/vendor.js");
const api_message = require("../../api/message.js");
const utils_websocket = require("../../utils/websocket.js");
const api_upload = require("../../api/upload.js");
const _sfc_main = {
  __name: "friendMessage",
  setup(__props) {
    const messageList = common_vendor.ref([]);
    const inputMessage = common_vendor.ref("");
    const loading = common_vendor.ref(false);
    const isRefreshing = common_vendor.ref(false);
    const currentPage = common_vendor.ref(1);
    const pageSize = common_vendor.ref(20);
    const hasMore = common_vendor.ref(true);
    const targetUserId = common_vendor.ref("");
    const currentUserId = common_vendor.ref("");
    const targetUserInfo = common_vendor.ref({});
    const scrollToMessageId = common_vendor.ref("");
    const userAvatar = common_vendor.ref("");
    const showEmojiPanel = common_vendor.ref(false);
    const emojiList = common_vendor.ref(["😀", "😂", "😊", "😍", "🤔", "😒", "😭", "😡", "👍", "👋", "❤️", "🎉", "🔥", "✅", "⭐", "🎁", "🙏", "🌹"]);
    const needRefreshFriendPage = common_vendor.ref(false);
    common_vendor.onMounted(() => {
      const userInfoStorage = common_vendor.index.getStorageSync("userInfo");
      console.log("Retrieved userInfo from storage:", userInfoStorage);
      if (userInfoStorage) {
        try {
          const parsedUserInfo = typeof userInfoStorage === "string" ? JSON.parse(userInfoStorage) : userInfoStorage;
          if (parsedUserInfo && typeof parsedUserInfo === "object") {
            currentUserId.value = parsedUserInfo.id || "";
            userAvatar.value = parsedUserInfo.avatar || "/static/default-avatar.png";
          } else {
            console.warn("Parsed userInfo is not a valid object:", parsedUserInfo);
            currentUserId.value = "";
            userAvatar.value = "/static/default-avatar.png";
          }
        } catch (e) {
          console.error("Failed to parse userInfo from storage:", e, "Raw value:", userInfoStorage);
          currentUserId.value = "";
          userAvatar.value = "/static/default-avatar.png";
        }
      } else {
        console.warn("未在 Storage 中找到 userInfo");
        currentUserId.value = "";
        userAvatar.value = "/static/default-avatar.png";
      }
      const pages = getCurrentPages();
      const page = pages[pages.length - 1];
      if (page && page.options && page.options.userId) {
        targetUserId.value = page.options.userId;
        const friendInfoStorage = common_vendor.index.getStorageSync("friendInfo_" + targetUserId.value);
        console.log(`Retrieved friendInfo for ${targetUserId.value}:`, friendInfoStorage);
        if (friendInfoStorage) {
          try {
            const parsedFriendInfo = typeof friendInfoStorage === "string" ? JSON.parse(friendInfoStorage) : friendInfoStorage;
            if (parsedFriendInfo && typeof parsedFriendInfo === "object") {
              targetUserInfo.value = parsedFriendInfo;
            } else {
              console.warn(`Parsed friendInfo for ${targetUserId.value} is not a valid object:`, parsedFriendInfo);
              targetUserInfo.value = {};
            }
          } catch (e) {
            console.error(`Failed to parse friendInfo for ${targetUserId.value}:`, e, "Raw value:", friendInfoStorage);
            targetUserInfo.value = {};
          }
        } else if (targetUserId.value === "admin") {
          console.log("adminadmin");
          targetUserInfo.value = {
            userId: "admin",
            username: "管理员",
            avatarUrl: "/static/admin_avatar.png"
          };
        } else {
          console.warn(`未在 Storage 中找到 friendInfo_${targetUserId.value}`);
          targetUserInfo.value = {};
        }
        loadMessages();
        markMessagesAsReadAndUpdateBadge();
        setupWebSocket();
      } else {
        console.error("未能获取到目标用户ID");
      }
      console.log("Current User ID:", currentUserId.value);
      console.log("Target User ID:", targetUserId.value);
      console.log("Current User Avatar:", userAvatar.value);
      console.log("Target User Info:", targetUserInfo.value);
    });
    const setupWebSocket = () => {
      utils_websocket.webSocketService.on("chat", (data) => {
        console.log("聊天页面收到消息:", data, "当前聊天对象:", targetUserId.value);
        if (data.user === targetUserId.value || data.userId === targetUserId.value) {
          messageList.value.push({
            id: data.id || Date.now(),
            user: data.user || data.userId,
            content: data.content,
            fileUrl: data.fileUrl,
            type: data.type || "text",
            time: data.time || /* @__PURE__ */ new Date(),
            status: data.status || 1
          });
          common_vendor.nextTick$1(() => {
            scrollToBottom();
          });
          markMessagesAsReadAndUpdateBadge();
        } else {
          console.log("收到非当前聊天对象的消息，不处理");
          needRefreshFriendPage.value = true;
        }
      });
    };
    common_vendor.onBeforeUnmount(() => {
      utils_websocket.webSocketService.off("chat");
    });
    const loadMessages = async () => {
      if (loading.value || !hasMore.value)
        return;
      loading.value = true;
      try {
        const res = await api_message.getChatMessages(targetUserId.value, currentPage.value, pageSize.value);
        if (res && res.code === 1) {
          const records = res.data.records || [];
          if (records.length === 0) {
            hasMore.value = false;
          } else {
            const sortedMessages = records.sort((a, b) => new Date(a.time) - new Date(b.time));
            messageList.value = [...sortedMessages, ...messageList.value];
            currentPage.value++;
          }
        }
      } catch (error) {
        console.error("加载消息失败:", error);
      } finally {
        loading.value = false;
        isRefreshing.value = false;
      }
    };
    const loadMoreMessages = () => {
      if (hasMore.value && !loading.value) {
        loadMessages();
      }
    };
    const onRefresh = () => {
      isRefreshing.value = true;
      loadMoreMessages();
    };
    const scrollToBottom = () => {
      if (messageList.value.length > 0) {
        const lastMsg = messageList.value[messageList.value.length - 1];
        scrollToMessageId.value = lastMsg.id;
      }
    };
    const handleSendMessage = async () => {
      if (!inputMessage.value.trim())
        return;
      const messageContent = inputMessage.value.trim();
      inputMessage.value = "";
      const messageData = {
        toUser: targetUserId.value,
        content: messageContent,
        type: targetUserId.value === "admin" ? "ADMIN-USER" : "USER-USER"
      };
      const tempId = "temp-" + Date.now();
      const newMessage = {
        id: tempId,
        user: currentUserId.value,
        toUser: targetUserId.value,
        content: messageContent,
        type: messageData.type,
        time: /* @__PURE__ */ new Date(),
        status: 0
        // 发送中状态
      };
      messageList.value.push(newMessage);
      common_vendor.nextTick$1(() => {
        scrollToBottom();
      });
      try {
        const res = await api_message.sendMessage(messageData);
        if (res.code === 1) {
          const index = messageList.value.findIndex((msg) => msg.id === tempId);
          if (index !== -1) {
            messageList.value[index].id = res.data.id || tempId;
            messageList.value[index].status = 1;
          }
        } else {
          const index = messageList.value.findIndex((msg) => msg.id === tempId);
          if (index !== -1) {
            messageList.value[index].status = 2;
          }
          common_vendor.index.showToast({
            title: res.message || "发送失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("发送消息失败:", error);
        const index = messageList.value.findIndex((msg) => msg.id === tempId);
        if (index !== -1) {
          messageList.value[index].status = 2;
        }
        common_vendor.index.showToast({
          title: "发送失败",
          icon: "none"
        });
      }
    };
    const handleAttachment = () => {
      common_vendor.index.chooseImage({
        count: 1,
        success: (res) => {
          const filePath = res.tempFilePaths[0];
          common_vendor.index.showLoading({
            title: "上传中..."
          });
          api_upload.uploadMessageImage(filePath).then((uploadRes) => {
            if (uploadRes.code === 1 && uploadRes.data) {
              const fileUrl = uploadRes.data;
              const messageData = {
                toUser: targetUserId.value,
                content: "[图片]",
                fileUrl,
                type: targetUserId.value === "admin" ? "ADMIN-USER" : "USER-USER"
              };
              const tempId = "temp-" + Date.now();
              messageList.value.push({
                id: tempId,
                user: currentUserId.value,
                toUser: targetUserId.value,
                content: "[图片]",
                fileUrl,
                type: messageData.type,
                time: /* @__PURE__ */ new Date()
              });
              common_vendor.nextTick$1(() => {
                messageList.value = [...messageList.value];
                scrollToBottom();
              });
              api_message.sendMessage(messageData).then((res2) => {
                if (res2.code === 1) {
                  const index = messageList.value.findIndex((msg) => msg.id === tempId);
                  if (index !== -1) {
                    messageList.value[index].id = res2.data.id || tempId;
                    messageList.value[index].status = 1;
                  }
                } else {
                  const index = messageList.value.findIndex((msg) => msg.id === tempId);
                  if (index !== -1) {
                    messageList.value[index].status = 2;
                  }
                  common_vendor.index.showToast({
                    title: res2.message || "发送失败",
                    icon: "none"
                  });
                }
              }).catch((error) => {
                console.error("发送消息失败:", error);
                const index = messageList.value.findIndex((msg) => msg.id === tempId);
                if (index !== -1) {
                  messageList.value[index].status = 2;
                }
                common_vendor.index.showToast({
                  title: "发送失败",
                  icon: "none"
                });
              });
            } else {
              common_vendor.index.showToast({
                title: uploadRes.message || "上传失败",
                icon: "none"
              });
            }
          }).catch((error) => {
            console.error("上传失败:", error);
            common_vendor.index.showToast({
              title: error.message || "上传失败",
              icon: "none"
            });
          }).finally(() => {
            common_vendor.index.hideLoading();
          });
        }
      });
    };
    const previewImage = (url) => {
      const imageUrls = messageList.value.filter((msg) => msg.fileUrl).map((msg) => msg.fileUrl);
      if (imageUrls.length === 0) {
        imageUrls.push(url);
      }
      common_vendor.index.previewImage({
        current: url,
        // 当前显示图片的URL
        urls: imageUrls,
        // 所有图片URL的数组
        longPressActions: {
          itemList: ["保存图片"],
          success: function(data) {
            if (data.tapIndex === 0) {
              common_vendor.index.saveImageToPhotosAlbum({
                filePath: imageUrls[data.index],
                success: function() {
                  common_vendor.index.showToast({
                    title: "保存成功",
                    icon: "success"
                  });
                },
                fail: function() {
                  common_vendor.index.showToast({
                    title: "保存失败",
                    icon: "none"
                  });
                }
              });
            }
          }
        }
      });
    };
    const markMessagesAsReadAndUpdateBadge = async () => {
      try {
        await api_message.markAsRead(targetUserId.value);
        const pages = getCurrentPages();
        for (let i = 0; i < pages.length; i++) {
          if (pages[i].route === "pages/friend/friend") {
            const friendPage = pages[i];
            if (friendPage.$vm && typeof friendPage.$vm.updateMessageBadge === "function") {
              friendPage.$vm.updateMessageBadge(targetUserId.value);
            }
            break;
          }
        }
        const app = getApp();
        if (app && app.updateUnreadMessageCount) {
          app.updateUnreadMessageCount();
        }
      } catch (error) {
        console.error("标记消息已读失败:", error);
      }
    };
    const formatDateDivider = (time) => {
      if (!time)
        return "";
      const date = new Date(time);
      const hours = date.getHours().toString().padStart(2, "0");
      const minutes = date.getMinutes().toString().padStart(2, "0");
      return `${hours}:${minutes}`;
    };
    const shouldShowTimestamp = (msg, index) => {
      if (index === 0)
        return true;
      const prevMsg = messageList.value[index - 1];
      const msgDate = new Date(msg.time);
      const prevMsgDate = new Date(prevMsg.time);
      return msgDate.getTime() - prevMsgDate.getTime() > 5 * 60 * 1e3;
    };
    const toggleEmojiPanel = () => {
      showEmojiPanel.value = !showEmojiPanel.value;
    };
    const insertEmoji = (emoji) => {
      inputMessage.value += emoji;
    };
    const isSelfMessage = (msg) => {
      if (msg.user === currentUserId.value) {
        return true;
      }
      if (msg.toUser === targetUserId.value) {
        return true;
      }
      return false;
    };
    common_vendor.onUnmounted(() => {
      if (needRefreshFriendPage.value) {
        const pages = getCurrentPages();
        for (let i = 0; i < pages.length; i++) {
          if (pages[i].route === "pages/friend/friend") {
            const friendPage = pages[i];
            if (friendPage.$vm && typeof friendPage.$vm.loadMessages === "function") {
              friendPage.$vm.loadMessages();
            }
            break;
          }
        }
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: loading.value
      }, loading.value ? {} : {}, {
        b: common_vendor.f(messageList.value, (msg, index, i0) => {
          return common_vendor.e({
            a: shouldShowTimestamp(msg, index)
          }, shouldShowTimestamp(msg, index) ? {
            b: common_vendor.t(formatDateDivider(msg.time))
          } : {}, {
            c: isSelfMessage(msg) ? userAvatar.value : targetUserInfo.value.avatarUrl || "/static/default-avatar.png",
            d: msg.fileUrl
          }, msg.fileUrl ? {
            e: msg.fileUrl,
            f: common_vendor.o(($event) => previewImage(msg.fileUrl), msg.id)
          } : {
            g: common_vendor.t(msg.content)
          }, {
            h: "msg-" + msg.id,
            i: isSelfMessage(msg) ? 1 : "",
            j: msg.id
          });
        }),
        c: messageList.value.length === 0 && !loading.value
      }, messageList.value.length === 0 && !loading.value ? {} : {}, {
        d: common_vendor.o(loadMoreMessages),
        e: "msg-" + scrollToMessageId.value,
        f: !loading.value,
        g: isRefreshing.value,
        h: common_vendor.o(onRefresh),
        i: common_vendor.o(handleSendMessage),
        j: inputMessage.value,
        k: common_vendor.o(($event) => inputMessage.value = $event.detail.value),
        l: common_vendor.o(toggleEmojiPanel),
        m: !inputMessage.value.trim()
      }, !inputMessage.value.trim() ? {} : {}, {
        n: common_vendor.o(($event) => inputMessage.value.trim() ? handleSendMessage() : handleAttachment()),
        o: showEmojiPanel.value
      }, showEmojiPanel.value ? {
        p: common_vendor.f(emojiList.value, (emoji, index, i0) => {
          return {
            a: common_vendor.t(emoji),
            b: index,
            c: common_vendor.o(($event) => insertEmoji(emoji), index)
          };
        })
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-db650ebf"], ["__file", "C:/Users/86182/Desktop/上班/解压小程序/前端/JieYa/pages/friendMessage/friendMessage.vue"]]);
wx.createPage(MiniProgramPage);
