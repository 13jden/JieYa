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
import { ref, onMounted, nextTick } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { watch } from "vue";
const newMessage = ref("");
const messageContainer = ref(null);
const isLoadingHistory = ref(false);
const myavatar = ref("https://jiayaya.oss-cn-hangzhou.aliyuncs.com/2.jpg"); // 当前用户头像
const favatar = ref(""); // 对方头像
const messagelist = ref([]);
const userId = ref(""); // 当前聊天对象的 userId


watch(messagelist, (newMessages) => {
  console.log("监听到消息列表更新：", newMessages);
  nextTick(scrollToBottom);
}, { deep: true });

onLoad((options) => {
  if (options.userId) {
    userId.value = options.userId;
	console.log(userId.value);
    loadFriendInfo();
    loadMessages();
	console.log(messagelist);
  }
});
onMounted(() => {
  loadFriendInfo();
  loadMessages();
  nextTick(() => {
    console.log("确保页面更新后的消息列表：", messagelist.value);
    scrollToBottom();
  });
});

// **获取好友信息**
const loadFriendInfo = () => {
  let friendList = wx.getStorageSync("friendList");
  try {
    friendList = JSON.parse(friendList); // 解析 JSON
  } catch (e) {
    console.error("解析 friendList 失败", e);
    friendList = []; // 解析失败时，设置为空数组，避免 .find() 报错
  }
  const friendArray = Array.isArray(friendList) ? friendList : [];
  const friend = friendArray.find((f) => f.userId === userId.value);

  if (friend) {
    favatar.value = friend.avatarUrl;
	console.log(favatar.value);
  } else {
    console.warn("未找到好友信息", userId.value);
  }
};
const updateFriend = (newMessage) => {
  let friendList = wx.getStorageSync("friendList");
  try {
    friendList = JSON.parse(friendList); // 解析 JSON
  } catch (e) {
    console.error("解析 friendList 失败", e);
    friendList = [];
  }
  const friendArray = Array.isArray(friendList) ? friendList : [];
  const friend = friendArray.find((f) => f.userId === userId.value);
  if (friend) {
    friend.message = newMessage; // 更新消息
    // 保存更新后的 friendList 回 localStorage
    wx.setStorageSync("friendList", JSON.stringify(friendArray)); 
  } else {
    console.warn("未找到好友信息", userId.value);
  }
};


const loadMessages = () => {
  const storedMessages = wx.getStorageSync(`messageList-${userId.value}`) || [];
  messagelist.value = storedMessages;
  nextTick(scrollToBottom);
};

// **发送消息**
const send = () => {
  if (!newMessage.value.trim()) return;
  messagelist.value.push({ content: newMessage.value, mymessage: 1 });
  // 更新本地存储
  wx.setStorageSync(`messageList-${userId.value}`, messagelist.value);
  updateFriend(newMessage.value);
  newMessage.value = "";
  scrollToBottom();
};

// **滚动到底部**
const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
    }
  });
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

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20rpx;
}

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
