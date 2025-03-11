"use strict";
const common_vendor = require("../../common/vendor.js");
const appId = "8877b855310e403f97110950934b465e";
const apiKey = "sk-c048c72e74024e53a1fe0248b8213f6a";
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
      { content: "欢迎使用心理咨询顾问 。我可以为您提供心理咨询、情感支持和心理辅导等服务。请告诉我您的问题和困惑，我会尽可能为您提供专业、温暖的建议和帮助。", mymessage: 0 }
    ]);
    common_vendor.onMounted(async () => {
      if (common_vendor.wx$1.getStorageSync("aiMessageList")) {
        messagelist.value = common_vendor.wx$1.getStorageSync("aiMessageList");
      }
      console.log("加载ok");
      session_id.value = common_vendor.wx$1.setStorageSync("sesion_id");
      scrollToBottom();
    });
    const send = async () => {
      if (!newMessage.value.trim())
        return;
      messagelist.value.push({ content: newMessage.value, mymessage: 1 });
      const aiMessageIndex = messagelist.value.length;
      messagelist.value.push({ content: "思考中...", mymessage: 0 });
      let dots = 0;
      const thinkingInterval = setInterval(() => {
        dots = (dots + 1) % 3;
        const thinkingText = "思考中" + ".".repeat(dots + 1);
        messagelist.value[aiMessageIndex].content = thinkingText;
      }, 500);
      const url = `https://dashscope.aliyuncs.com/api/v1/apps/${appId}/completion`;
      const requestData = {
        input: {
          prompt: newMessage.value,
          session_id: session_id.value || void 0
        },
        parameters: {
          has_thoughts: true,
          incremental_output: true
        },
        stream: true
      };
      newMessage.value = "";
      let result = "";
      try {
        const response = await new Promise((resolve, reject) => {
          common_vendor.wx$1.request({
            url,
            method: "POST",
            header: {
              Authorization: `Bearer ${apiKey}`,
              "Content-Type": "application/json",
              "X-DashScope-SSE": "enable"
              // 启用流式支持
            },
            data: requestData,
            responseType: "text",
            // 设置响应类型为文本
            success(res) {
              if (res.statusCode === 200) {
                console.log("请求成功");
                let buffer = "";
                const processChunk = (chunk) => {
                  buffer += chunk;
                  const lines = buffer.split("\n\n");
                  buffer = lines.pop();
                  lines.forEach((line) => {
                    if (line.trim()) {
                      try {
                        const match = line.match(/data:\s*(\{.*\})/);
                        if (match) {
                          const jsonData = JSON.parse(match[1]);
                          if (jsonData.output && jsonData.output.text) {
                            console.log("Received text:", jsonData.output.text);
                            if (!session_id.value) {
                              session_id.value = jsonData.output.session_id;
                            }
                            const read = jsonData.output.text;
                            result += read;
                            messagelist.value[aiMessageIndex].content = result;
                            clearInterval(thinkingInterval);
                          }
                        }
                      } catch (error) {
                        console.error("解析分块数据失败:", error);
                      }
                    }
                  });
                };
                processChunk(res.data);
                resolve(res);
              } else {
                reject(new Error(`请求失败，状态码: ${res.statusCode}`));
              }
            },
            fail(err) {
              reject(err);
            }
          });
        });
        common_vendor.nextTick$1();
        common_vendor.wx$1.setStorageSync("aiMessageList", messagelist.value);
        common_vendor.wx$1.setStorageSync("sesion_id", session_id.value);
      } catch (error) {
        console.error("请求失败:", error);
        clearInterval(thinkingInterval);
      }
    };
    const scrollToBottom = () => {
      common_vendor.nextTick$1(() => {
        if (messageContainer.value) {
          messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
          console.log("滑动");
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
    common_vendor.onMounted(scrollToBottom);
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
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-4c1b26cf"], ["__file", "C:/Users/86182/Documents/HBuilderProjects/JieYa/pages/message/message.vue"]]);
wx.createPage(MiniProgramPage);
