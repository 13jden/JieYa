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
    <!-- 发送消息框 -->
    <view class="send_box">
      <input
        type="text"
        v-model="newMessage"
        placeholder="请输入..."
        class="send_input"
        bindconfirm="send"
      />
      <button class="send_button" @click="send">发送</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import axios from 'axios';
import mpAdapter from 'axios-miniprogram-adapter';
import cloudbase from "@cloudbase/js-sdk";

// 适配小程序环境
axios.defaults.adapter = mpAdapter;

const newMessage = ref("");
const session_id = ref(""); // 存储会话 ID
const messageContainer = ref(null);
const isLoadingHistory = ref(false);
const myavatar = ref("https://jiayaya.oss-cn-hangzhou.aliyuncs.com/2.jpg"); // 当前用户头像
const favatar = ref("https://jiayaya.oss-cn-hangzhou.aliyuncs.com/3.jpg"); // 对方用户头像
const messagelist = ref([
  { content: "欢迎使用心理咨询顾问。我可以为您提供心理咨询、情感支持和心理辅导等服务。请告诉我您的问题和困惑，我会尽可能为您提供专业、温暖的建议和帮助。", mymessage: 0 }, 
]);

// 初始化 CloudBase
let ai = null;

onMounted(async () => {
  const app = cloudbase.init({ env: "cloud1-3g929mfy63013808" });
  const auth = app.auth();
  
  try {
    await auth.signInAnonymously();
    ai = await app.ai();
    console.log("CloudBase 初始化成功");
  } catch (error) {
    console.error("CloudBase 初始化失败:", error);
  }

  // 读取本地存储的消息记录
  const storedMessages = wx.getStorageSync('aiMessageList');
  if (storedMessages) {
    messagelist.value = storedMessages;
  }

  // 读取 session_id
  session_id.value = wx.getStorageSync('session_id') || "";

  // 如果 session_id 为空，生成一个随机的 5 位字符串并保存
  if (!session_id.value) {
    session_id.value = generateRandomString(5);
    wx.setStorageSync('session_id', session_id.value);
  }

  // 滚动到底部
  nextTick(scrollToBottom);
});

// 生成随机字符串的函数
function generateRandomString(length) {
  const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += characters.charAt(Math.floor(Math.random() * characters.length));
  }
  return result;
}

// 发送消息
const send = async () => {
  if (!newMessage.value.trim()) return;

  // 添加用户消息到列表
  messagelist.value.push({ content: newMessage.value, mymessage: 1 });
  const userInput = newMessage.value;
  newMessage.value = "";
  scrollToBottom();

  // 显示“思考中”动画
  const aiMessageIndex = messagelist.value.length;
  messagelist.value.push({ content: "思考中...", mymessage: 0 });

  let dots = 0;
  const thinkingInterval = setInterval(() => {
    dots = (dots + 1) % 3;
    messagelist.value[aiMessageIndex].content = "思考中" + ".".repeat(dots + 1);
  }, 1000);

  // 调用 AI API
  try {
    const res = await ai.bot.sendMessage({
      botId: "bot-68f2d97a",
      msg: userInput,
	  sender:"user-a"
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
    wx.setStorageSync('aiMessageList', messagelist.value);
    wx.setStorageSync('session_id', session_id.value);
    scrollToBottom();
	console.log(res);
  } catch (error) {
    clearInterval(thinkingInterval);
    console.error("AI API 调用失败:", error);
    messagelist.value[aiMessageIndex] = { content: "AI 发生错误，请稍后再试。", mymessage: 0 };
  }
};

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
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
  align-items: flex-start;
  
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
    font-size: 30rpx;
    background-color: #41b87a;
    color: white;
  }
}
</style>
