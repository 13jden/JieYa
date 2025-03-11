<template>
  <view class="container">
    

    <view class="box2">
		<view class="box">
		  <view class="text" style="font-weight: 600; font-size: 38rpx;">发布笔记</view>
		  <view class="publish" @click="finish" :disabled="!imagelist.length">发布</view>
		</view>
		
      <input type="text" v-model="title" class="title" placeholder="请输入标题" style="font-size: 35rpx; padding-left: 20rpx; padding-top: 10rpx;" />
      <view style="margin-left: 15rpx; color: rgb(0, 0, 0, 0.1);">—————————————————————</view>
	  <view class="sort-text">请选择情绪分类</view>
	  <view class="sort">
	  	<view class="happy">
	  		<image src="../../static/happy.png" mode=""></image>开心
	  	</view>
		<view class="sad">
			<image src="../../static/sad.png" mode=""></image>难过
		</view>
		<view class="anxiety">
			<image src="../../static/anxiety.png" mode=""></image>焦虑
		</view>
		<view class="excited">
			<image src="../../static/excited.png" mode=""></image>兴奋
		</view>
	  </view>
      <view class="imagebox">
        <view class="noteimage">
          <view class="image" v-for="(item, index) in filelist" :key="index" style="position: relative;">
            <image v-if="item" :src="item" mode="aspectFill" ></image>
            <image src="../../static/delete.png" @click="removeImage(index)" style="position: absolute; top: 0rpx; right: -5rpx; width: 30rpx; height: 30rpx; cursor: pointer;"></image>
          </view>
          <view class="upload" v-if="filelist.length < 9" @click="chooseFile">
            +
          </view>

        </view>
      </view>
      <textarea v-model="content" class="content" placeholder="请输入正文"></textarea>
    </view>
  </view>
</template>

<script setup>
import { ref, getCurrentInstance } from 'vue';
const filePath = ref("");
const title = ref("");
const content = ref("");
const filelist = ref([]); // 本地图片文件路径
const imagelist = ref([]); // 上传成功的图片链接



function chooseFile() {

  uni.chooseImage({
    count: 9, // 允许选择最多9张
    success: (res) => {
      res.tempFilePaths.forEach(filePath => {
        filelist.value.push(filePath);
		
      });
    },
    fail: (err) => {
      console.log("选择文件失败: ", err);
    },
  });
}

function removeImage(index) {
  filelist.value.splice(index, 1);
  imagelist.value.splice(index, 1);
}



function goBack() {
  uni.navigateBack(); // 返回上一页
}
</script>

<style lang="scss" scoped>

.container {
  background-image: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  height: 100vh;
}

.publish{
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 30rpx;
	margin-right: 30rpx;
	background-color: #1a73e8;
	color: white;
	font-weight: 520;
	height: 60rpx;
	width:160rpx;
}
.box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: white;
  margin-bottom: 30rpx;
  .text {
    padding-left: 20rpx;
  }
  button {
    border-radius: 5%;
    background-color: rgb(55, 149, 255);
    width: 120rpx;
    height: 60rpx;
    margin-left: 400rpx;
    font-size: 20rpx;
    color: white;
  }
}
.imagebox {
  margin-top: 30rpx;
  width: 100%;
  height: auto;
}

.noteimage {
  display: flex;
  flex-wrap: wrap; // 允许换行
  gap: 10rpx; // 元素之间的间距
}

.image, .upload {
  width: calc(50% - 30rpx); // 每行显示三个，减去间距
  margin-left: 10rpx;
  height: 350rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f0f0; // 图片和 + 的背景色
  border-radius: 10rpx; // 圆角
  overflow: hidden; // 防止图片溢出
  position: relative;
}
.sort-text{
	color: #666;
	
}
.sort{
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin: 10rpx;
	.happy,.anxiety,.sad,.excited{
		display: flex;
		justify-content: center;
		align-items: center;
		border-radius: 30rpx;
		background-color: #eff6ff;
		width: 160rpx;
		margin:10rpx;
		height: 80rpx;
		image{
			width: 40rpx;
			height: 40rpx;
			margin: 10rpx;
		}
	}
	.anxiety{
		background-color: #fefce8;
	}
	.excited{
		background-color: #f0fdf4;
	}
	.sad{
		background-color: #fdf2f8;
	}
}
.upload {
  font-size: 60rpx;
  color: #fff;
  cursor: pointer;
  background-color:rgba(0, 0, 0, 0.1); // + 的背景色
}

image {
  width: 100%;
  height: 100%;
  object-fit: cover; // 图片填充模式
}

.box2 {
  background-color: white;
  padding: 10rpx;
  border-radius: 5%;
  min-height: 100vh;
  width: 100%;
}
.content {
  font-size: 30rpx;
  padding: 10rpx 20rpx;
  width: 94%; // 让输入框宽度占满
  min-height: 100rpx; // 设置最小高度

  resize: none;
  border-radius: 10rpx;
  line-height: 1.5; // 适当增加行高
}

</style>
