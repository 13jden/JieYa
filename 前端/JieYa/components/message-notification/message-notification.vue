<template>
  <view class="notification" v-if="visible" @tap="handleTap">
    <view class="notification-content">
      <text class="notification-title">{{ title }}</text>
      <text class="notification-message">{{ message }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  title: {
    type: String,
    default: '新消息'
  },
  message: {
    type: String,
    default: ''
  },
  duration: {
    type: Number,
    default: 3000
  },
  messageData: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['tap', 'close']);

const visible = ref(false);

// 显示通知
const show = () => {
  visible.value = true;
  
  // 自动关闭
  if (props.duration > 0) {
    setTimeout(() => {
      hide();
    }, props.duration);
  }
};

// 隐藏通知
const hide = () => {
  visible.value = false;
  emit('close');
};

// 处理点击事件
const handleTap = () => {
  emit('tap', props.messageData);
  hide();
};

// 暴露方法
defineExpose({
  show,
  hide
});
</script>

<style scoped>
.notification {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 9999;
  background-color: rgba(0, 0, 0, 0.7);
  padding: 10px 15px;
  animation: slideDown 0.3s ease-out;
}

.notification-content {
  display: flex;
  flex-direction: column;
}

.notification-title {
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
}

.notification-message {
  color: #eee;
  font-size: 14px;
}

@keyframes slideDown {
  from {
    transform: translateY(-100%);
  }
  to {
    transform: translateY(0);
  }
}
</style>
