<template>
  <view>
    <view v-if="isLoading">加载中...</view>
	
    <view v-else class="container">
		<friend_item
		@tap="goSystemMsg"
		  username="系统消息" 
		  userimage="/static/system.png" 
		  message= "您的账户安全设置需要更新">
		</friend_item>
		<friend_item 
		  @tap="goMessage(friend.userId)" 
		  v-for="friend in friendList" 
		  :key="friend.userId" 
		  :username="friend.username" 
		  :userimage="friend.avatarUrl" 
		  :message="friend.message"
		  :isNew="friend.isNew">
		</friend_item>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
const friendList = ref([]);
function loadListsFromSession() {
  const storedLists = wx.getStorageSync('friendList');
  if (storedLists) {
    friendList.value = JSON.parse(storedLists); // 解析存储的数据
  }
}
function goMessage(userId) {
  console.log('跳转到消息页面，userId:', userId); // 添加日志
  if(userId==0){
	  uni.navigateTo({
		url: `/pages/message/message?userId=${userId}`
	  });
  }
  else{
	  uni.navigateTo({
	  		url: `/pages/friendMessage/friendMessage?userId=${userId}`
	  });
  }
  
}
function goSystemMsg() {
	console.log("系统");
  // uni.navigateTo({
  //   url: `/pages/message/message?userId=${userId}`
  // });
}
onMounted(() => {
  loadListsFromSession();
});

</script>

<style lang="scss" scoped>



</style>
