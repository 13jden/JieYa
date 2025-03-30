<template>
  <div class="app-container">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px" class="sidebar">
        <div class="logo"><img src="@/assets/logo.png" class="logoImgage" />JieYa后台管理</div>
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical"
          :collapse="isCollapse"
          router
          background-color="#304156"
          text-color="#fff"
          active-text-color="#409EFF"
        >
          <!-- 首页菜单项 -->
          <el-menu-item index="/dashboard">
            <el-icon><data-line /></el-icon>
            <span>首页</span>
          </el-menu-item>
          
          <!-- 内容管理 -->
          <el-sub-menu index="/content">
            <template #title>
              <el-icon><management /></el-icon>
              <span>内容管理</span>
            </template>
            <el-menu-item index="/content/category">
              <template #title>分类管理</template>
            </el-menu-item>
            <el-menu-item index="/content/banner">
              <template #title>轮播图管理</template>
            </el-menu-item>
            <el-menu-item index="/content/article">
              <template #title>稿件管理</template>
            </el-menu-item>
          </el-sub-menu>
          
          <!-- 商品管理 -->
          <el-sub-menu index="/product">
            <template #title>
              <el-icon><shopping-cart /></el-icon>
              <span>商品管理</span>
            </template>
            <el-menu-item index="/product/venue">
              <template #title>场地管理</template>
            </el-menu-item>
            <el-menu-item index="/product/venue/booking">
              <template #title>场地预约订单</template>
            </el-menu-item>
            <el-menu-item index="/product/prop">
              <template #title>道具管理</template>
            </el-menu-item>
            <el-menu-item index="/product/prop/rental">
              <template #title>道具租借订单</template>
            </el-menu-item>
          </el-sub-menu>
          
          <!-- 用户管理 -->
          <el-sub-menu index="/user">
            <template #title>
              <el-icon><user /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/user/index">
              <template #title>用户管理</template>
            </el-menu-item>
          </el-sub-menu>
          
          <!-- 消息中心 -->
          <el-menu-item index="/message">
            <el-icon><message /></el-icon>
            <span>消息中心</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-container>
        <!-- 顶部导航栏 -->
        <el-header class="header">
          <div class="header-left">
            <el-icon 
              class="toggle-icon" 
              @click="isCollapse = !isCollapse"
            >
              <component :is="isCollapse ? 'Expand' : 'Fold'" />
            </el-icon>
            <breadcrumb />
          </div>
          
          <div class="header-right">
            <!-- 消息图标 -->
            <div class="message-icon-container">
              <el-badge :value="totalUnreadCount" :hidden="totalUnreadCount === 0" class="message-badge">
                <el-tooltip
                  effect="light"
                  placement="bottom"
                  :show-after="300"
                  popper-class="message-tooltip"
                  :hide-after="0"
                  :visible="messageTooltipVisible"
                  @mouseenter="showMessageTooltip"
                  @mouseleave="hideMessageTooltip"
                >
                  <template #content>
                    <div class="message-types">
                      <div class="message-type" @click="goToMessageCenter('system')">
                        <img src="@/assets/SystemMessage.png" class="type-icon" />
                        <span>系统消息</span>
                        <el-badge :value="messageCounts.system" :hidden="messageCounts.system === 0" />
                      </div>
                      <div class="message-type" @click="goToMessageCenter('order')">
                        <img src="@/assets/OrderMessage.png" class="type-icon" />
                        <span>订单消息</span>
                        <el-badge :value="messageCounts.order" :hidden="messageCounts.order === 0" />
                      </div>
                      <div class="message-type" @click="goToMessageCenter('user')">
                        <img src="@/assets/UserMessage.png" class="type-icon" />
                        <span>用户消息</span>
                        <el-badge :value="messageCounts.user" :hidden="messageCounts.user === 0" />
                      </div>
                    </div>
                  </template>
                  <el-icon @click="goToMessageCenter('system')" class="message-icon">
                    <img src="@/assets/MessageBox.png" class="message-box-icon" />
                  </el-icon>
                </el-tooltip>
              </el-badge>
            </div>

            <!-- 用户头像 -->
            <el-dropdown @command="handleCommand">
              <div class="avatar-container">
                <el-avatar :src="userInfo.avatar" :size="36" />
                <span class="username">{{ userInfo.name }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleProfile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            
            <!-- WebSocket连接状态 -->
            <div class="ws-status">
              <el-tag size="small" :type="wsConnected ? 'success' : 'danger'">
                {{ wsConnected ? '已连接' : '未连接' }}
              </el-tag>
            </div>
          </div>
        </el-header>
        
        <!-- 主内容区 -->
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DataLine, Management, ShoppingCart, User, Expand, Fold, Message } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import Breadcrumb from '@/components/Breadcrumb.vue'
import { getAllMessageCounts } from '@/api/message'

// 注入websocket服务
const websocket = inject('websocket')

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const messageTooltipVisible = ref(false)
const messageCounts = ref({
  system: 0,
  order: 0,
  user: 0
})

// 计算总未读消息数
const totalUnreadCount = computed(() => {
  return messageCounts.value.system + messageCounts.value.order + messageCounts.value.user
})

const userInfo = ref({
  name: 'Admin',
  avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
})

const activeMenu = computed(() => route.path)

// 显示消息类型提示
const showMessageTooltip = () => {
  messageTooltipVisible.value = true
}

// 隐藏消息类型提示
const hideMessageTooltip = () => {
  messageTooltipVisible.value = false
}

// 跳转到消息中心
const goToMessageCenter = (type) => {
  router.push({
    path: '/message',
    query: { type }
  })
  messageTooltipVisible.value = false
}

// 获取消息数量
const fetchMessageCounts = async () => {
  try {
    const res = await getAllMessageCounts()
    if (res.code === 1) {
      messageCounts.value = res.data
    }
  } catch (error) {
    console.error('获取消息数量失败:', error)
  }
}

const handleProfile = () => {
  router.push('/profile')
}

const handleCommand = (command) => {
  if (command === 'logout') {
    logout()
  }
}

const logout = () => {
  ElMessageBox.confirm('确认退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 断开WebSocket连接
    if (websocket) {
      websocket.disconnect()
    }
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('loginTime')
    router.push('/login')
  })
}

const wsConnected = ref(false)

const handleWsConnect = () => {
  wsConnected.value = true
}

// WebSocket消息处理
const handleWsMessage = (data) => {
  // 如果是新消息通知，刷新消息数量
  console.log("收到消息:"+data.type);
  if (data.type != 'CONNECT' && data.type != 'HEARTBEAT') {
    fetchMessageCounts()
  }
}

onMounted(() => {
  // 获取初始消息数量
  fetchMessageCounts()
  
  // 添加WebSocket连接状态监听器
  if (websocket) {
    websocket.addListener('connect', handleWsConnect)
    websocket.addListener('message', handleWsMessage)
    // 添加消息计数刷新监听器
    websocket.addListener('refreshMessageCounts', fetchMessageCounts)
    
    // 如果已经连接，则更新状态
    if (websocket.connected) {
      wsConnected.value = true
    } else {
      // 尝试连接WebSocket
      websocket.connect()
    }
  } else {
    console.warn('无法获取WebSocket服务')
  }
  
  onBeforeUnmount(() => {
    // 移除WebSocket监听器
    if (websocket) {
      websocket.removeListener('connect', handleWsConnect)
      websocket.removeListener('message', handleWsMessage)
      websocket.removeListener('refreshMessageCounts', fetchMessageCounts)
    }
  })
})
</script>

<style scoped>
.app-container {
  height: 100vh;
  overflow: hidden;
}

.el-container {
  height: 100%;
}

.sidebar {
  background-color: #304156;
  color: #fff;
  overflow-y: auto;
  transition: width 0.3s;
}

.logo {
  height: 80px;
  line-height: 80px;
  display: flex;
  align-items: center;
  text-align: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  background-color: #263445;
  .logoImgage{
    width: 50px;
    height: 50px;
    margin-right: 10px;
    border-radius: 50%;
  }
}

.header {
  background-color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.toggle-icon {
  margin-right: 15px;
  font-size: 20px;
  cursor: pointer;
}

.header-right {
  display: flex;
  align-items: center;
}

.message-icon-container {
  margin-right: 20px;
  position: relative;
}

.message-icon {
  cursor: pointer;
  font-size: 24px;
}

.message-box-icon {
  width: 24px;
  height: 24px;
}

.message-tooltip {
  padding: 10px;
  min-width: 200px;
}

.message-types {
  display: flex;
  flex-direction: column;
}

.message-type {
  display: flex;
  align-items: center;
  padding: 8px 0;
  cursor: pointer;
  border-bottom: 1px solid #eee;
}

.message-type:last-child {
  border-bottom: none;
}

.message-type:hover {
  background-color: #f5f7fa;
}

.type-icon {
  width: 20px;
  height: 20px;
  margin-right: 8px;
}

.avatar-container {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-right: 20px;
}

.username {
  margin-left: 8px;
  font-size: 14px;
}

.ws-status {
  font-size: 12px;
}

.el-main {
  padding: 20px;
  overflow-y: auto;
  background-color: #f0f2f5;
}

.el-menu-vertical:not(.el-menu--collapse) {
  width: 200px;
}
</style> 