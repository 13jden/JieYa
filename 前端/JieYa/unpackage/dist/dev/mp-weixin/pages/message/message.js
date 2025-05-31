"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  __name: "message",
  setup(__props) {
    common_vendor.axios.defaults.adapter = common_vendor.mpAdapter;
    const newMessage = common_vendor.ref("");
    const session_id = common_vendor.ref("");
    const messageContainer = common_vendor.ref(null);
    const isLoadingHistory = common_vendor.ref(false);
    const myavatar = common_vendor.ref("https://jiayaya.oss-cn-hangzhou.aliyuncs.com/2.jpg");
    const favatar = common_vendor.ref("https://jiayaya.oss-cn-hangzhou.aliyuncs.com/3.jpg");
    const messagelist = common_vendor.ref([
      { content: "欢迎使用心理咨询顾问。我可以为您提供心理咨询、情感支持和心理辅导等服务。请告诉我您的问题和困惑，我会尽可能为您提供专业、温暖的建议和帮助。", mymessage: 0 }
    ]);
    let ai = null;
    common_vendor.onMounted(async () => {
      const app = common_vendor.cloudbase.init({ env: "cloud1-3g929mfy63013808" });
      const auth = app.auth();
      try {
        await auth.signInAnonymously();
        ai = await app.ai();
        console.log("CloudBase 初始化成功");
      } catch (error) {
        console.error("CloudBase 初始化失败:", error);
      }
      const storedMessages = common_vendor.wx$1.getStorageSync("aiMessageList");
      if (storedMessages) {
        messagelist.value = storedMessages;
      }
      session_id.value = common_vendor.wx$1.getStorageSync("session_id") || "";
      if (!session_id.value) {
        session_id.value = generateRandomString(5);
        common_vendor.wx$1.setStorageSync("session_id", session_id.value);
      }
      common_vendor.nextTick$1(scrollToBottom);
    });
    function generateRandomString(length) {
      const characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
      let result = "";
      for (let i = 0; i < length; i++) {
        result += characters.charAt(Math.floor(Math.random() * characters.length));
      }
      return result;
    }
    const send = async () => {
      if (!newMessage.value.trim())
        return;
      messagelist.value.push({ content: newMessage.value, mymessage: 1 });
      const userInput = newMessage.value;
      newMessage.value = "";
      scrollToBottom();
      const aiMessageIndex = messagelist.value.length;
      messagelist.value.push({ content: "思考中...", mymessage: 0 });
      let dots = 0;
      const thinkingInterval = setInterval(() => {
        dots = (dots + 1) % 3;
        messagelist.value[aiMessageIndex].content = "思考中" + ".".repeat(dots + 1);
      }, 1e3);
      try {
        const res = await ai.bot.sendMessage({
          botId: "bot-68f2d97a",
          msg: userInput,
          sender: session_id
        });
        let aiResponse = "";
        for await (let data of res.dataStream) {
          if (data.content) {
            aiResponse += data.content;
            messagelist.value[aiMessageIndex].content = aiResponse;
          }
        }
        clearInterval(thinkingInterval);
        messagelist.value[aiMessageIndex] = { content: aiResponse, mymessage: 0 };
        common_vendor.wx$1.setStorageSync("aiMessageList", messagelist.value);
        common_vendor.wx$1.setStorageSync("session_id", session_id.value);
        scrollToBottom();
        console.log(res);
      } catch (error) {
        clearInterval(thinkingInterval);
        console.error("AI API 调用失败:", error);
        messagelist.value[aiMessageIndex] = { content: "AI 发生错误，请稍后再试。", mymessage: 0 };
      }
    };
    const scrollToBottom = () => {
      common_vendor.nextTick$1(() => {
        if (messageContainer.value) {
          messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
        }
      });
    };
    const handleScroll = () => {
      if (!messageContainer.value || isLoadingHistory.value)
        return;
      if (messageContainer.value.scrollTop === 0) {
        isLoadingHistory.value = true;
        setTimeout(() => {
          messagelist.value.unshift(
            { content: "之前的消息1", mymessage: 0 },
            { content: "之前的消息2", mymessage: 1 }
          );
          common_vendor.nextTick$1(() => {
            messageContainer.value.scrollTop = 100;
          });
          isLoadingHistory.value = false;
        }, 1e3);
      }
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.f(messagelist.value, (message, index, i0) => {
          return common_vendor.e({
            a: message.mymessage === 0
          }, message.mymessage === 0 ? {
            b: favatar.value,
            c: common_vendor.t(message.content)
          } : {}, {
            d: message.mymessage === 1
          }, message.mymessage === 1 ? {
            e: common_vendor.t(message.content),
            f: myavatar.value
          } : {}, {
            g: index,
            h: message.mymessage === 1 ? 1 : "",
            i: message.mymessage === 0 ? 1 : ""
          });
        }),
        b: common_vendor.o(handleScroll),
        c: newMessage.value,
        d: common_vendor.o(($event) => newMessage.value = $event.detail.value),
        e: common_vendor.o(send)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-4c1b26cf"], ["__file", "C:/Users/86182/Desktop/上班/解压小程序/前端/JieYa/pages/message/message.vue"]]);
wx.createPage(MiniProgramPage);
