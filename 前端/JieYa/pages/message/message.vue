<template>
  <view class="container">
    <!-- 消息列表 -->
    <view ref="messageContainer" class="message-list" @scroll="handleScroll">
      <view
        v-for="(message, index) in messagelist"
        :key="index"
        class="message-row"
        :class="{ 'mine': message.mymessage === 1, 'theirs': message.mymessage === 0 }"
      >
        <view v-if="message.mymessage === 0" class="message">
          <image class="avatar" :src="favatar" />
          <view class="message-content">{{ message.content }}</view>
        </view>
        <view v-if="message.mymessage === 1" class="message my-message">
          <view class="message-content">{{ message.content }}</view>
          <image class="avatar" :src="myavatar" />
        </view>
      </view>
    </view>
    <!-- 发送消息框-->
    <view class="send_box">
      <input type="text" v-model="newMessage" placeholder="请输入..." class="send_input" 
		bindconfirm="send" />
      <button class="send_button" @click="send">发送</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import axios from 'axios';
import mpAdapter from 'axios-miniprogram-adapter';
// 适配小程序环境
axios.defaults.adapter = mpAdapter;
const newMessage = ref("");
const session_id = ref(""); // 用于存储会话 ID
const appId = '8877b855310e403f97110950934b465e'; // 替换为实际的应用 ID
const apiKey = 'sk-c048c72e74024e53a1fe0248b8213f6a';
const messageContainer = ref(null);
const isLoadingHistory = ref(false);
const myavatar = ref("https://jiayaya.oss-cn-hangzhou.aliyuncs.com/2.jpg"); // 当前用户头像
const favatar = ref("https://jiayaya.oss-cn-hangzhou.aliyuncs.com/3.jpg"); // 对方用户头像
const messagelist = ref([
  { content: "欢迎使用心理咨询顾问 。我可以为您提供心理咨询、情感支持和心理辅导等服务。请告诉我您的问题和困惑，我会尽可能为您提供专业、温暖的建议和帮助。", mymessage: 0 }, 
]);

onMounted(async () => {
	if(wx.getStorageSync('aiMessageList')){
		messagelist.value = wx.getStorageSync('aiMessageList');
	}
	console.log("加载ok");
	session_id.value = wx.setStorageSync('sesion_id');
	scrollToBottom();
});



const send = async () => {
  if (!newMessage.value.trim()) return;

  // 添加用户输入
  messagelist.value.push({ content: newMessage.value, mymessage: 1 });

  // 记录索引
  const aiMessageIndex = messagelist.value.length;
  messagelist.value.push({ content: "思考中...", mymessage: 0 });

  // 启动 "思考中..." 变化动画
  let dots = 0;
  const thinkingInterval = setInterval(() => {
    dots = (dots + 1) % 3; // 0, 1, 2 循环
    const thinkingText = "思考中" + ".".repeat(dots + 1);
    messagelist.value[aiMessageIndex].content = thinkingText;
  }, 500); // 每 500ms 切换一次

  // API 请求参数
  const url = `https://dashscope.aliyuncs.com/api/v1/apps/${appId}/completion`;
  const requestData = {
    input: {
      prompt: newMessage.value,
      session_id: session_id.value || undefined,
    },
    parameters: {
      has_thoughts: true,
      incremental_output: true,
    },
    stream: true,
  };

  newMessage.value = "";
  let result = "";

  try {
    const response = await new Promise((resolve, reject) => {
      wx.request({
        url,
        method: "POST",
        header: {
          Authorization: `Bearer ${apiKey}`,
          "Content-Type": "application/json",
          "X-DashScope-SSE": "enable", // 启用流式支持
        },
        data: requestData,
        responseType: "text", // 设置响应类型为文本
        success(res) {
          if (res.statusCode === 200) {
            console.log("请求成功");
            let buffer = "";
            const processChunk = (chunk) => {
              buffer += chunk;
              const lines = buffer.split("\n\n"); // 按 `\n\n` 分割流数据
              buffer = lines.pop(); // 剩余部分继续缓存
              lines.forEach((line) => {
                if (line.trim()) {
                  try {
                    const match = line.match(/data:\s*(\{.*\})/); // 提取 JSON 数据
                    if (match) {
                      const jsonData = JSON.parse(match[1]); // 解析 JSON

                      if (jsonData.output && jsonData.output.text) {
                        console.log("Received text:", jsonData.output.text);
                        // 只在第一次接收到 session_id 时存储，避免重复
                        if (!session_id.value) {
                          session_id.value = jsonData.output.session_id;
                        }
                        const read = jsonData.output.text;
                        result += read;
                        messagelist.value[aiMessageIndex].content = result;
                        clearInterval(thinkingInterval); // 停止 "思考中..." 变化
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
        },
      });
    });
	nextTick();
	wx.setStorageSync('aiMessageList', messagelist.value);
	wx.setStorageSync('sesion_id', session_id.value);
  } catch (error) {
    console.error("请求失败:", error);
    clearInterval(thinkingInterval); // 确保失败时停止 "思考中..."
  }
};

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
	  console.log("滑动");
    }
  });
};

// 监听滚动事件，向上加载更多历史消息
const handleScroll = () => {
  if (!messageContainer.value || isLoadingHistory.value) return;
  if (messageContainer.value.scrollTop === 0) {
    isLoadingHistory.value = true;
    setTimeout(() => {
      messagelist.value.unshift(
        { content: "之前的消息1", mymessage: 0 },
        { content: "之前的消息2", mymessage: 1 }
      );

      nextTick(() => {
        messageContainer.value.scrollTop = 100;
      });

      isLoadingHistory.value = false;
    }, 1000);
  }
};

onMounted(scrollToBottom);
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: white;
  padding: 2rpx;
  box-sizing: border-box;
}

/* 消息列表 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20rpx;
}

/* 消息项 */
.message-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  &.mine {
    justify-content: flex-end;
  }
  &.theirs {
    justify-content: flex-start;
  }
}

.message {
  display: flex;
  align-items: center;
}

.message-content {
  background-color: #f1f1f1;
  padding: 10px;
  border-radius: 10px;
  max-width: 70%;
  word-wrap: break-word;
}

.my-message .message-content {
  background-color: #a8e6cf;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-left: 10px;
  margin-right: 10px;
}

/* 发送框 */
.send_box {
  display: flex;
  height: 6vh;
  width: 100vw;
  background-color: #fff;
  border: 1px solid #f1f1f1;
  border-radius: 30rpx;
  border-radius: 10rpx;
  justify-content: center;
  align-items: center;
  .send_input {
    width: 80vw;
    height: 60rpx;
    padding: 5rpx;
  }
  .send_button {
    height: 65rpx;
    text-align: center;
    font-size: 30rpx;
    background-color: #41b87a;
    color: white;
  }
}
</style>