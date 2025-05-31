<template>
  <div class="message-center">
    <el-card class="message-container">
      <template #header>
        <div class="header-actions">
          <el-button @click="goBack" type="primary" plain icon="ArrowLeft">返回</el-button>
          <span class="title">消息中心</span>
        </div>
      </template>
      
      <div class="message-layout">
        <!-- 左侧消息类型选择 -->
        <div class="message-sidebar">
          <div 
            class="message-type-item" 
            :class="{ active: activeType === 'system' }" 
            @click="switchType('system')"
          >
            <el-icon class="type-icon"><Bell /></el-icon>
            <span>系统消息</span>
            <el-badge :value="messageCounts.system" :hidden="messageCounts.system === 0" />
          </div>
          <div 
            class="message-type-item" 
            :class="{ active: activeType === 'order' }" 
            @click="switchType('order')"
          >
            <el-icon class="type-icon"><ShoppingCart /></el-icon>
            <span>订单消息</span>
            <el-badge :value="messageCounts.order" :hidden="messageCounts.order === 0" />
          </div>
          <div 
            class="message-type-item" 
            :class="{ active: activeType === 'user' }" 
            @click="switchType('user')"
          >
            <el-icon class="type-icon"><ChatDotRound /></el-icon>
            <span>用户消息</span>
            <el-badge :value="messageCounts.user" :hidden="messageCounts.user === 0" />
          </div>
        </div>
        
        <!-- 用户列表区域 - 仅在用户消息类型时显示 -->
        <div class="user-list-container" v-if="activeType === 'user'">
          <div class="user-list-header">
            <span>用户列表</span>
            <el-button type="primary" size="small" plain>全部已读</el-button>
          </div>
          
          <div class="user-list">
            <div 
              v-for="user in userList" 
              :key="user.user"
              class="user-item"
              :class="{ active: user.user === activeUser }"
              @click="selectUser(user.user)"
            >
              <el-avatar :src="user.avatarUrl || defaultAvatar" :size="40"></el-avatar>
              <div class="user-info">
                <div class="user-name">
                  {{ user.userName }}
                  <span class="message-time">{{ formatDate(user.time) }}</span>
                </div>
                <div class="message-preview">{{ user.content }}</div>
              </div>
              <el-badge :value="user.notReadCount" :hidden="user.notReadCount === 0" />
            </div>
            <div class="load-more-container" v-if="userList.length < userTotal">
              <el-button type="primary" plain size="small" @click="loadMoreUsers" :loading="loadingMore">
                加载更多
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 消息内容区域 -->
        <div class="message-content">
          <!-- 系统消息和订单消息 -->
          <template v-if="activeType !== 'user'">
            <div class="message-list">
              <div 
                v-for="msg in messages" 
                :key="msg.id" 
                class="message-item"
                :class="{ unread: !msg.read }"
              >
                <div class="message-header">
                  <div class="message-title">
                    <el-tag size="small" :type="getTagType(msg.type)" class="message-tag">
                      {{ getTagText(msg.type) }}
                    </el-tag>
                    <span class="message-subject">{{ msg.subject }}</span>
                  </div>
                  <div class="message-time">{{ formatDate(msg.createTime) }}</div>
                </div>
                <div class="message-body">{{ msg.content }}</div>
                <div class="message-actions">
                  <el-button 
                    type="text" 
                    size="small" 
                    @click="markAsRead(msg)" 
                    v-if="!msg.read"
                  >
                    标为已读
                  </el-button>
                  <el-button 
                    type="text" 
                    size="small" 
                    @click="deleteMessage(msg)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </div>
            
            <!-- 分页 -->
            <div class="pagination-container">
              <el-pagination
                layout="prev, pager, next"
                :total="total"
                :page-size="pageSize"
                :current-page="currentPage"
                @current-change="handlePageChange"
              />
            </div>
            
            <!-- 空数据提示 -->
            <div v-if="messages.length === 0" class="empty-state">
              <el-empty description="暂无消息">
                <template #image>
                  <el-icon :size="60"><ChatDotRound /></el-icon>
                </template>
              </el-empty>
            </div>
          </template>
          
          <!-- 用户聊天区域 -->
          <template v-else>
            <div v-if="activeUser" class="chat-container">
              <div class="chat-header">
                {{ userList.find(u => u.user === activeUser)?.userName || '聊天' }}
              </div>
              
              <div class="chat-messages" ref="chatMessagesRef">
                <div class="load-more-container" v-if="chatMessages.length < chatTotal">
                  <el-button type="primary" plain size="small" @click="loadMoreChatMessages" :loading="loadingMoreChat">
                    加载更多
                  </el-button>
                </div>
                
                <div 
                  v-for="(msg, index) in chatMessages" 
                  :key="index"
                  class="chat-message"
                  :class="{ 'message-mine': msg.user === 'admin' }"
                >
                  <div class="message-time">{{ formatDate(msg.time) }}</div>
                  
                  <div class="message-content-wrapper">
                    <el-avatar 
                      :size="36" 
                      :src="msg.user === 'admin' ? defaultAvatar : userList.find(u => u.user === activeUser)?.avatarUrl || defaultAvatar"
                    />
                    
                    <div class="message-bubble">
                      <!-- 显示文本内容 -->
                      <div class="message-content" v-if="msg.content">{{ msg.content }}</div>
                      
                      <!-- 显示图片 -->
                      <div class="message-media" v-if="msg.fileUrl">
                        <img 
                          v-if="isImage(msg.fileUrl)" 
                          :src="msg.fileUrl" 
                          class="message-image" 
                          @click="previewImageFile(msg.fileUrl)" 
                        />
                        
                        <!-- 显示视频 -->
                        <video 
                          v-else-if="isVideo(msg.fileUrl)" 
                          class="message-video" 
                          controls
                          :src="msg.fileUrl"
                        ></video>
                        
                        <!-- 其他文件类型 -->
                        <a 
                          v-else 
                          :href="msg.fileUrl" 
                          target="_blank" 
                          class="message-file"
                        >
                          <el-icon><Document /></el-icon>
                          下载文件
                        </a>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="chat-input">
                <!-- 添加预览区域 -->
                <div class="upload-preview" v-if="previewImage || uploadedFile">
                  <div class="preview-content">
                    <img v-if="previewImage" :src="previewImage" class="preview-image" />
                    <div v-else-if="uploadedFile && uploadedFile.type === 'video'" class="video-placeholder">
                      <el-icon><VideoPlay /></el-icon>
                      <span>视频: {{ uploadedFile.name }}</span>
                    </div>
                  </div>
                  <el-button type="danger" circle size="small" @click="cancelFileUpload" class="cancel-upload">
                    <el-icon><Close /></el-icon>
                  </el-button>
                </div>

                <el-input
                  v-model="newMessage"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入消息..."
                  @keyup.enter.native="sendMessage"
                  @paste="handlePaste"
                />
                
                <div class="input-actions">
                  <!-- 文件上传按钮 -->
                  <el-button type="primary" plain @click="triggerFileUpload" :loading="uploadLoading">
                    <el-icon><PictureRounded /></el-icon>
                    图片/视频
                  </el-button>
                  
                  <!-- 隐藏的文件输入 -->
                  <input 
                    type="file" 
                    ref="fileInputRef" 
                    style="display: none;"
                    @change="handleFileChange"
                    accept="image/*,video/*"
                  />
                  
                  <el-button type="primary" @click="sendMessage">发送</el-button>
                </div>
              </div>
            </div>
            
            <!-- 未选择用户时的提示 -->
            <div v-else class="empty-chat">
              <div class="empty-placeholder">
                <el-icon :size="60"><ChatDotRound /></el-icon>
                <p>请选择用户开始聊天</p>
              </div>
            </div>
          </template>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick, onUnmounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Bell, 
  ShoppingCart, 
  ChatDotRound, 
  Message,
  ArrowLeft,
  Document,
  VideoPlay,
  PictureRounded,
  Close
} from '@element-plus/icons-vue'
import { 
  getAllMessageCounts, 
  getUserMessageList, 
  getUserItemMessageList,
  getOrderMessage,
  getSystemMessage,
  sendMessage as apiSendMessage,
  deleteMessage as apiDeleteMessage,
  deleteMessageList,
  deleteUserMessage,
  markOrderMessagesAsRead as apiMarkOrderMessagesAsRead,
  markSystemMessagesAsRead as apiMarkSystemMessagesAsRead,
  markUserMessagesAsRead as apiMarkUserMessagesAsRead,
  markAllMessagesAsRead
} from '@/api/message'
import { uploadMessageFile } from '@/api/upload'

const route = useRoute()
const router = useRouter()
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 用户信息
const userInfo = ref({
  name: 'Admin',
  avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
})

// 消息类型
const activeType = ref('system')
const messageCounts = ref({
  system: 0,
  order: 0,
  user: 0
})

// 用户消息相关
const userList = ref([])
const activeUser = ref(null)
const chatMessages = ref([])
const chatMessagesRef = ref(null)
const newMessage = ref('')
const selectedFile = ref(null)

// 订单消息相关
const orderMessages = ref([])
const orderPageNum = ref(1)
const orderPageSize = ref(10)
const orderTotal = ref(0)

// 系统消息相关
const systemMessages = ref([])
const systemPageNum = ref(1)
const systemPageSize = ref(10)
const systemTotal = ref(0)

// 消息列表
const messages = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)

// 用户列表分页参数
const userPageNum = ref(1)
const userPageSize = ref(20)
const userTotal = ref(0)

// 用户聊天分页参数
const chatPageNum = ref(1)
const chatPageSize = ref(20)
const chatTotal = ref(0)

const loadingMore = ref(false)
const loadingMoreChat = ref(false)

// 添加WebSocket服务
const websocket = inject('websocket')

// 文件上传相关
const fileInputRef = ref(null)
const uploadLoading = ref(false)
const uploadedFile = ref(null)
const previewImage = ref(null)

// 监听路由参数变化
watch(() => route.query.type, (newType) => {
  if (newType && ['system', 'user', 'order'].includes(newType)) {
    activeType.value = newType
  }
}, { immediate: true })

// 初始化
onMounted(() => {
  console.log('消息页面 - 准备添加WebSocket监听器')
  websocket.addListener('message', handleWebSocketMessage)
  console.log('消息页面 - WebSocket监听器已添加')
  
  // 检查listeners数组
  console.log('当前注册的监听器数量:', 
    websocket.listeners && websocket.listeners.message ? 
    websocket.listeners.message.length : 0)
  
  // 获取初始消息数量
  fetchMessageCounts()
  
  // 根据URL参数设置初始消息类型和用户
  if (route.query.type && ['system', 'user', 'order'].includes(route.query.type)) {
    activeType.value = route.query.type
    if (route.query.type === 'user' && route.query.userId) {
      activeUser.value = route.query.userId
      loadUserMessages(route.query.userId)
    } else {
      loadMessages()
    }
  } else {
    loadMessages()
  }
  
  // 请求通知权限
  requestNotificationPermission()
})

// 在组件卸载时移除监听器
onUnmounted(() => {
  console.log('消息页面 - 准备移除WebSocket监听器')
  websocket.removeListener('message', handleWebSocketMessage)
  console.log('消息页面 - WebSocket监听器已移除')
})


// 监听WebSocket消息的处理函数
const handleWebSocketMessage = (data) => {
  try {
    console.log('消息中心收到:', data)
    
    // 忽略心跳和连接消息
    if (data.type === 'HEARTBEAT' || data.type === 'CONNECT') {
      return
    }
    
    // 对所有有效消息进行通知提醒
    showNotification(data)
    
    // 消息类型处理
    switch(data.type) {
      case 'ADMIN-USER':
        // 用户聊天消息
        handleUserChatMessage(data)
        break
      case 'SYSTEM-ADMIN':
        // 系统消息
        handleSystemMessage(data)
        break
      case 'ORDER-ADMIN':
        // 订单消息
        handleOrderMessage(data)
        break
      default:
        console.log('收到其他类型消息:', data.type)
    }
    
    // 更新未读消息计数
    fetchMessageCounts()
  } catch (error) {
    console.error('处理WebSocket消息出错:', error)
  }
}

// 显示消息通知
const showNotification = (message) => {
  // 播放提示音
  playNotificationSound()
  
  // 根据消息类型处理
  let msgType = 'system'
  if (message.type === 'ADMIN-USER') {
    msgType = 'user'
  } else if (message.type === 'ORDER') {
    msgType = 'order'
  }
  
  // 如果浏览器支持通知且用户已授权，显示通知
  if (Notification && Notification.permission === 'granted') {
    const notificationTitle = `新${getMessageTypeName(msgType)}消息`
    const notificationOptions = {
      body: getNotificationBody(msgType, message),
      icon: '/favicon.ico'
    }
    
    const notification = new Notification(notificationTitle, notificationOptions)
    
    // 点击通知时跳转到对应的消息页面
    notification.onclick = () => {
      window.focus()
      if (msgType === 'user') {
        // 跳转到用户聊天页面，如果消息是从用户发来的，选择该用户
        const targetUser = message.user === 'admin' ? message.toUser : message.user
        router.push({ 
          path: '/message', 
          query: { type: 'user', userId: targetUser } 
        })
        
        // 如果当前已在消息页面，手动更新选中用户
        if (route.path === '/message') {
          activeType.value = 'user'
          activeUser.value = targetUser
          loadUserMessages(targetUser)
        }
      } else {
        // 跳转到系统或订单消息页面
        router.push({ path: '/message', query: { type: msgType } })
        
        // 如果当前已在消息页面，手动更新选中类型
        if (route.path === '/message') {
          activeType.value = msgType
          loadMessages()
        }
      }
      notification.close()
    }
    
    // 5秒后自动关闭通知
    setTimeout(() => {
      notification.close()
    }, 5000)
  }
}

// 播放提示音 - 为确保声音播放，使用更可靠的方法
const playNotificationSound = () => {
  try {
    const audio = new Audio()
    audio.src = '/notification.mp3'
    audio.volume = 0.5
    
    // 预加载音频
    audio.load()
    
    // 当可以播放时立即播放
    audio.oncanplaythrough = () => {
      const playPromise = audio.play()
      if (playPromise) {
        playPromise.catch(err => {
          console.log('无法播放提示音:', err)
        })
      }
    }
  } catch (err) {
    console.log('创建音频对象失败:', err)
  }
}

// 处理用户聊天消息
const handleUserChatMessage = (message) => {
  // 检查消息发送者或接收者是否为当前活动用户
  const isCurrentUserChat = activeType.value === 'user' && 
                           activeUser.value && 
                           (message.user === activeUser.value || message.toUser === activeUser.value);
  
  if (isCurrentUserChat) {
    // 只通过ID判断消息是否存在，避免内容相同的消息被过滤
    const messageExists = chatMessages.value.some(msg => msg.id === message.id);
    
    if (!messageExists) {
      console.log("消息内容:"+message.content);
      // 将新消息添加到列表
      chatMessages.value.push(message);
      // 重新排序
      chatMessages.value.sort((a, b) => new Date(a.time) - new Date(b.time));
      // 滚动到底部
      nextTick(() => {
        scrollToBottom();
      });
      // 标记为已读
      apiMarkUserMessagesAsRead(activeUser.value).then(() => {
        // 确保用户列表未读计数更新
        const userIndex = userList.value.findIndex(u => u.user === activeUser.value)
        if (userIndex !== -1) {
          userList.value[userIndex].unreadCount = 0
        }
      });
    }
  } else if (activeType.value === 'user') {
    // 在用户聊天页面，但不是当前聊天对象
    // 刷新用户列表
    loadUserList();
  }
}

// 处理系统消息
const handleSystemMessage = (message) => {
  if (activeType.value === 'system') {
    // 在系统消息页面，刷新消息列表
    loadSystemMessages();
    fetchMessageCounts();
  }
}

// 处理订单消息
const handleOrderMessage = (message) => {
  if (activeType.value === 'order') {
    // 在订单消息页面，刷新消息列表
    loadOrderMessages();
    fetchMessageCounts();
  }
}

// 返回上一页
const goBack = () => {
  router.go(-1)
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

// 根据当前选择的类型加载消息
const loadMessages = () => {
  if (activeType.value === 'user') {
    loadUserList()
  } else {
    currentPage.value = 1
    if (activeType.value === 'order') {
      loadOrderMessages()
    } else {
      loadSystemMessages()
    }
  }
}

// 切换消息类型
const switchType = (type) => {
  activeType.value = type
  activeUser.value = null
  
  // 加载对应类型的消息
  loadMessages()
  
  // 标记相应类型的消息为已读
  if (type === 'SYSTEM') {
    apiMarkSystemMessagesAsRead().then(() => {
      fetchMessageCounts()
    })
  } else if (type === 'ORDER') {
    apiMarkOrderMessagesAsRead().then(() => {
      fetchMessageCounts()
    })
  }
}

// 加载用户列表
const loadUserList = async (loadMore = false) => {
  try {
    const res = await getUserMessageList(userPageNum.value, userPageSize.value)
    if (res.code === 1) {
      if (loadMore) {
        // 加载更多时追加数据
        userList.value = [...userList.value, ...(res.data.records || [])]
      } else {
        // 首次加载或切换页码时替换数据
        userList.value = res.data.records || []
        // 自动选择第一个用户
        if (userList.value.length > 0 && !activeUser.value) {
          activeUser.value = userList.value[0].user
          loadUserMessages(userList.value[0].user)
          userList.value[0].notReadCount = 0;
        }

   

      }
      userTotal.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  }
}

// 选择用户
const selectUser = (userId) => {
  activeUser.value = userId
  chatPageNum.value = 1 // 重置聊天分页
  
  // 立即更新用户列表中的未读标记
  const userIndex = userList.value.findIndex(u => u.user === userId);
  if (userIndex !== -1) {
    userList.value[userIndex].notReadCount = 0;
    console.log("用户列表:"+userList.value[userIndex].notReadCount);
  }
  
  // 加载用户消息
  loadUserMessages(userId)
  
  // 标记该用户的消息为已读
  apiMarkUserMessagesAsRead(userId).then(() => {
    fetchMessageCounts()
    // 触发全局刷新消息计数事件
    websocket.emit('refreshMessageCounts')
  })
}

// 加载用户消息
const loadUserMessages = async (userId) => {
  if (!userId) return
  
  try {
    const res = await getUserItemMessageList(userId, chatPageNum.value, chatPageSize.value)
    if (res.code === 1) {
      // 获取消息记录并按时间排序（旧消息在上，新消息在下）
      const messages = res.data.records || []
      chatMessages.value = messages.sort((a, b) => {
        return new Date(a.time) - new Date(b.time)
      })
      chatTotal.value = res.data.total || 0
      
      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })
      
      // 标记消息为已读
      try {
        await apiMarkUserMessagesAsRead(userId)
        // 更新未读消息数
        fetchMessageCounts()
      } catch (error) {
        console.error('标记已读失败:', error)
      }
    }
  } catch (error) {
    console.error('获取用户消息失败:', error)
    ElMessage.error('获取用户消息失败')
  }
}

// 滚动聊天窗口到底部
const scrollToBottom = () => {
  if (chatMessagesRef.value) {
    chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
  }
}

// 触发文件选择
const triggerFileUpload = () => {
  fileInputRef.value.click()
}

// 处理文件选择
const handleFileChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  const isImage = file.type.startsWith('image/')
  const isVideo = file.type.startsWith('video/')
  
  if (!isImage && !isVideo) {
    ElMessage.warning('只能上传图片或视频文件')
    return
  }
  
  try {
    uploadLoading.value = true
    
    // 创建FormData对象
    const formData = new FormData()
    formData.append('file', file)
    
    // 调用上传API
    const res = await uploadMessageFile(formData)
    
    if (res.code === 1) {
      uploadedFile.value = {
        url: res.data,
        type: isImage ? 'image' : 'video',
        name: file.name
      }
      
      // 如果是图片，设置预览
      if (isImage) {
        previewImage.value = res.data
      } else {
        previewImage.value = null
      }
      
      ElMessage.success('文件上传成功')
    } else {
      ElMessage.error(res.msg || '上传失败')
    }
  } catch (error) {
    console.error('文件上传失败:', error)
    ElMessage.error('文件上传失败')
  } finally {
    uploadLoading.value = false
    // 清空input以允许重复选择同一文件
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
  }
}

// 粘贴图片处理
const handlePaste = async (event) => {
  const items = (event.clipboardData || event.originalEvent.clipboardData).items
  
  for (const item of items) {
    if (item.kind === 'file') {
      const file = item.getAsFile()
      if (file && file.type.startsWith('image/')) {
        event.preventDefault()
        
        try {
          uploadLoading.value = true
          
          // 创建FormData对象
          const formData = new FormData()
          formData.append('file', file)
          
          // 调用上传API
          const res = await uploadMessageFile(formData)
          
          if (res.code === 1) {
            uploadedFile.value = {
              url: res.data,
              type: 'image',
              name: '粘贴的图片'
            }
            
            previewImage.value = res.data
            ElMessage.success('图片上传成功')
          } else {
            ElMessage.error(res.msg || '上传失败')
          }
        } catch (error) {
          console.error('图片上传失败:', error)
          ElMessage.error('图片上传失败')
        } finally {
          uploadLoading.value = false
        }
        break
      }
    }
  }
}

// 取消文件上传
const cancelFileUpload = () => {
  uploadedFile.value = null
  previewImage.value = null
}

// 发送消息
const sendMessage = async () => {
  // 检查是否有内容或上传的文件
  if (!newMessage.value.trim() && !uploadedFile.value) {
    ElMessage.warning('消息不能为空')
    return
  }
  
  try {
    // 发送API请求，传递单独的参数
    const res = await apiSendMessage(
      activeUser.value,  // toUser
      newMessage.value.trim(),  // content
      'ADMIN-USER',  // type
      uploadedFile.value ? uploadedFile.value.url : null  // fileUrl
    )
    
    if (res.code === 1) {
      // 添加新消息到列表
      chatMessages.value.push({
        id: Date.now(),
        user: 'admin',
        toUser: activeUser.value,
        content: newMessage.value.trim(),
        fileUrl: uploadedFile.value ? uploadedFile.value.url : null,
        time: new Date().toISOString(),
        status: 1
      })
      
      // 清空消息输入框和上传文件
      newMessage.value = ''
      uploadedFile.value = null
      previewImage.value = null
      
      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })
    } else {
      ElMessage.error(res.msg || '发送失败')
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  }
}

// 加载系统消息
const loadSystemMessages = async () => {
  try {
    const res = await getSystemMessage(systemPageNum.value, systemPageSize.value)
    if (res.code === 1) {
      messages.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取系统消息失败:', error)
    ElMessage.error('获取系统消息失败')
  }
}

// 加载订单消息
const loadOrderMessages = async () => {
  try {
    const res = await getOrderMessage(orderPageNum.value, orderPageSize.value)
    if (res.code === 1) {
      messages.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取订单消息失败:', error)
    ElMessage.error('获取订单消息失败')
  }
}

// 处理分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  if (activeType.value === 'system') {
    systemPageNum.value = page
    loadSystemMessages()
  } else if (activeType.value === 'order') {
    orderPageNum.value = page
    loadOrderMessages()
  }
}

// 标记消息为已读
const markAsRead = async (message) => {
  try {
    let res
    if (activeType.value === 'system') {
      res = await apiMarkSystemMessagesAsRead([message.id])
    } else if (activeType.value === 'order') {
      res = await apiMarkOrderMessagesAsRead([message.id])
    }
    
    if (res && res.code === 1) {
      // 更新本地消息状态
      message.read = true
      // 更新未读消息数量
      fetchMessageCounts()
      
      // 触发全局刷新消息计数事件
      websocket.emit('refreshMessageCounts')
      
      ElMessage.success('标记成功')
    }
  } catch (error) {
    console.error('标记已读失败:', error)
    ElMessage.error('标记已读失败')
  }
}

// 删除消息
const deleteMessage = async (message) => {
  try {
    const res = await apiDeleteMessage(message.id)
    if (res.code === 1) {
      // 从列表中移除消息
      messages.value = messages.value.filter(m => m.id !== message.id)
      // 更新未读消息数量
      fetchMessageCounts()
      ElMessage.success('删除成功')
    }
  } catch (error) {
    console.error('删除消息失败:', error)
    ElMessage.error('删除消息失败')
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  
  const d = new Date(date)
  const now = new Date()
  const diff = now - d
  
  // 如果是今天，只显示时间
  if (diff < 24 * 60 * 60 * 1000 && 
      d.getDate() === now.getDate() && 
      d.getMonth() === now.getMonth() && 
      d.getFullYear() === now.getFullYear()) {
    return `${padZero(d.getHours())}:${padZero(d.getMinutes())}`
  }
  
  // 如果是昨天，显示"昨天 时间"
  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)
  if (d.getDate() === yesterday.getDate() && 
      d.getMonth() === yesterday.getMonth() && 
      d.getFullYear() === yesterday.getFullYear()) {
    return `昨天 ${padZero(d.getHours())}:${padZero(d.getMinutes())}`
  }
  
  // 其他情况显示完整日期
  return `${d.getFullYear()}-${padZero(d.getMonth() + 1)}-${padZero(d.getDate())} ${padZero(d.getHours())}:${padZero(d.getMinutes())}`
}

// 补零
const padZero = (num) => {
  return num < 10 ? `0${num}` : num
}

// 获取标签类型
const getTagType = (type) => {
  switch (type) {
    case 'system':
      return 'info'
    case 'order':
      return 'success'
    case 'user':
      return 'warning'
    default:
      return 'info'
  }
}

// 获取标签文本
const getTagText = (type) => {
  switch (type) {
    case 'system':
      return '系统'
    case 'order':
      return '订单'
    case 'user':
      return '用户'
    default:
      return '系统'
  }
}

const loadMoreUsers = async () => {
  if (userList.length >= userTotal.value) return
  
  loadingMore.value = true
  userPageNum.value++
  
  try {
    await loadUserList(true)
  } finally {
    loadingMore.value = false
  }
}

const loadMoreChatMessages = async () => {
  if (!activeUser.value || chatMessages.length >= chatTotal.value) return
  
  loadingMoreChat.value = true
  chatPageNum.value++
  
  try {
    const res = await getUserItemMessageList(activeUser.value, chatPageNum.value, chatPageSize.value)
    if (res.code === 1) {
      // 获取新消息并按时间排序
      const newMessages = res.data.records || []
      // 合并新旧消息并重新排序
      const allMessages = [...chatMessages.value, ...newMessages]
      chatMessages.value = allMessages.sort((a, b) => {
        return new Date(a.time) - new Date(b.time)
      })
    }
  } catch (error) {
    console.error('获取更多聊天记录失败:', error)
    ElMessage.error('获取更多聊天记录失败')
  } finally {
    loadingMoreChat.value = false
  }
}

// 请求通知权限
const requestNotificationPermission = () => {
  if (!('Notification' in window)) {
    console.log('当前浏览器不支持桌面通知!')
    return
  }
  
  if (Notification.permission !== 'granted' && Notification.permission !== 'denied') {
    // 尝试请求权限
    try {
      Notification.requestPermission().then(permission => {
        if (permission === 'granted') {
          console.log('通知权限已获得!')
        }
      })
    } catch (error) {
      // 处理旧版浏览器的API
      Notification.requestPermission(permission => {
        if (permission === 'granted') {
          console.log('通知权限已获得!')
        }
      })
    }
  }
}

// 文件类型判断
const isImage = (url) => {
  if (!url) return false
  return /\.(jpg|jpeg|png|gif|webp)$/i.test(url)
}

const isVideo = (url) => {
  if (!url) return false
  return /\.(mp4|webm|ogg|mov)$/i.test(url)
}

// 图片预览
const previewImageFile = (url) => {
  if (isImage(url)) {
    // 可以使用Element Plus的图片预览组件
    // 或者简单实现如下:
    window.open(url, '_blank')
  }
}
</script>

<style scoped>
.message-center {
  height: 100%;
}

.message-container {
  height: 100%;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.header-actions {
  display: flex;
  align-items: center;
}

.title {
  margin-left: 20px;
  font-size: 18px;
  font-weight: bold;
}

.message-layout {
  display: flex;
  height: calc(100vh - 200px);
  overflow: hidden;
}

.message-sidebar {
  width: 160px;
  border-right: 1px solid #ebeef5;
  background-color: #f9f9f9;
}

.message-type-item {
  display: flex;
  align-items: center;
  padding: 15px;
  cursor: pointer;
  position: relative;
}

.message-type-item:hover {
  background-color: #f5f7fa;
}

.message-type-item.active {
  background-color: #ecf5ff;
  color: #409EFF;
}

.type-icon {
  width: 20px;
  height: 20px;
  margin-right: 8px;
}

.user-list-container {
  width: 240px;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
}

.user-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
}

.user-list {
  flex: 1;
  overflow-y: auto;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  cursor: pointer;
  border-bottom: 1px solid #f5f7fa;
  position: relative;
}

.user-item:hover {
  background-color: #f5f7fa;
}

.user-item.active {
  background-color: #ecf5ff;
}

.user-info {
  margin-left: 10px;
  flex: 1;
  overflow: hidden;
}

.user-name {
  font-weight: 500;
  margin-bottom: 5px;
  display: flex;
  justify-content: space-between;
}

.message-preview {
  color: #909399;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.message-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.message-list {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
}

.message-item {
  margin-bottom: 20px;
  padding: 15px;
  border-radius: 4px;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.message-item.unread {
  background-color: #f0f9ff;
  border-left: 3px solid #409EFF;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.message-title {
  display: flex;
  align-items: center;
}

.message-tag {
  margin-right: 10px;
}

.message-subject {
  font-weight: 500;
}

.message-time {
  color: #909399;
  font-size: 12px;
}

.message-body {
  padding: 10px 0;
  line-height: 1.5;
}

.message-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.pagination-container {
  padding: 15px 0;
  display: flex;
  justify-content: center;
}

/* 聊天区域样式 */
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
  font-weight: bold;
}

.chat-messages {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
}

.chat-message {
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
}

.message-content-wrapper {
  display: flex;
  align-items: flex-start;
}

.chat-message.message-mine .message-content-wrapper {
  flex-direction: row-reverse;
}

.message-bubble {
  max-width: 70%;
  margin: 0 10px;
  padding: 10px;
  border-radius: 4px;
  background-color: #f2f6fc;
  position: relative;
}

.message-mine .message-bubble {
  background-color: #ecf5ff;
}

.message-content {
  word-break: break-all;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
  text-align: center;
}

.chat-input {
  padding: 15px;
  border-top: 1px solid #ebeef5;
  display: flex;
  align-items: flex-end;
}

.chat-input .el-textarea {
  margin-right: 10px;
  flex: 1;
}

.empty-chat {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.empty-placeholder {
  text-align: center;
  color: #909399;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.load-more-container {
  padding: 10px 0;
  text-align: center;
}

/* 媒体消息样式 */
.message-media {
  margin-top: 5px;
}

.message-image {
  max-width: 240px;
  max-height: 240px;
  border-radius: 8px;
  cursor: pointer;
}

.message-video {
  max-width: 240px;
  max-height: 240px;
  border-radius: 8px;
}

.message-file {
  display: flex;
  align-items: center;
  color: #409EFF;
  text-decoration: none;
}

.message-file .el-icon {
  margin-right: 5px;
}

/* 上传预览区域 */
.upload-preview {
  position: relative;
  margin-bottom: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 8px;
  background-color: #f8f8f8;
}

.preview-content {
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-image {
  max-width: 150px;
  max-height: 150px;
  border-radius: 4px;
}

.video-placeholder {
  display: flex;
  align-items: center;
  color: #606266;
}

.video-placeholder .el-icon {
  margin-right: 5px;
}

.cancel-upload {
  position: absolute;
  top: 5px;
  right: 5px;
}

.input-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}
</style> 