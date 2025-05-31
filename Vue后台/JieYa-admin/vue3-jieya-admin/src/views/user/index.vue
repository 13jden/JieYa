<template>
  <div class="user-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-input
              v-model="searchForm.username"
              placeholder="请输入用户名/昵称"
              style="width: 200px"
              clearable
              @keyup.enter="handleSearch"
            />
            <el-input
              v-model="searchForm.userId"
              placeholder="请输入用户ID"
              style="width: 200px; margin-left: 10px"
              clearable
              @keyup.enter="handleSearch"
            />
            <el-select
              v-model="searchForm.status"
              placeholder="用户状态"
              style="width: 120px; margin-left: 10px"
              clearable
              @change="handleSearch"
            >
              <el-option label="正常" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
            <el-button type="primary" @click="handleSearch" style="margin-left: 10px">
              搜索
            </el-button>
          </div>
        </div>
      </template>

      <!-- 用户列表 -->
      <el-table :data="userList" style="width: 100%" v-loading="loading">
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" :size="40">
              {{ row.nickName?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="nickName" label="昵称" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column label="性别" width="80">
          <template #default="{ row }">
            {{ row.sex === true ? '男' : row.sex === false ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="school" label="学校" width="150" show-overflow-tooltip />
        <el-table-column prop="joinTime" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.joinTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="180">
          <template #default="{ row }">
            {{ formatDate(row.lastLoginTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status ? 'success' : 'info'">
              {{ row.status ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              :type="row.status ? 'danger' : 'success'"
              link
              @click="handleToggleStatus(row)"
            >
              {{ row.status ? '禁用' : '启用' }}
            </el-button>
            <el-button type="success" link @click="handleSendMessage(row)">
              发消息
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑用户"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="昵称" prop="nickName">
          <el-input v-model="form.nickName" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-select v-model="form.sex" placeholder="请选择性别">
            <el-option :value="true" label="男" />
            <el-option :value="false" label="女" />
            <el-option :value="null" label="未知" />
          </el-select>
        </el-form-item>
        <el-form-item label="生日" prop="birthday">
          <el-date-picker v-model="form.birthday" type="date" placeholder="选择生日" />
        </el-form-item>
        <el-form-item label="学校" prop="school">
          <el-input v-model="form.school" placeholder="请输入学校" />
        </el-form-item>
        <el-form-item label="简介" prop="personIntruduction">
          <el-input v-model="form.personIntruduction" type="textarea" placeholder="请输入简介" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="true"
            :inactive-value="false"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 发送消息对话框 -->
    <el-dialog
      v-model="messageDialogVisible"
      title="发送消息"
      width="500px"
    >
      <el-form
        ref="messageFormRef"
        :model="messageForm"
        :rules="messageRules"
        label-width="80px"
      >
        <el-form-item label="用户" prop="userId">
          <el-input v-model="messageForm.userId" disabled />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="messageForm.type" placeholder="请选择消息类型" @change="handleTypeChange">
            <el-option
              v-for="item in messageTypes"
              :key="item.code"
              :label="item.description"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input 
            v-model="messageForm.content" 
            type="textarea" 
            rows="5" 
            placeholder="请输入消息内容" 
          />
          <div class="message-preview" v-if="messageForm.type && messageForm.content">
            <span class="preview-label">预览：</span>
            <div class="preview-content">{{ getPreviewContent() }}</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="messageDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSendMessageSubmit">
            发送
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getUserList, 
  forbidUser, 
  unforbidUser, 
  sendMessageToUser, 
  getMessageTypes, 
  sendMessageToAllUsers, 
  deleteUser, 
  updateUser 
} from '@/api/user'

// 搜索表单
const searchForm = ref({
  username: '',
  userId: '',
  status: ''
})

// 表格数据
const loading = ref(false)
const userList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 编辑对话框数据
const dialogVisible = ref(false)
const formRef = ref(null)
const form = ref({
  userId: '',
  nickName: '',
  email: '',
  sex: null,
  birthday: '',
  school: '',
  personIntruduction: '',
  status: true
})

// 表单验证规则
const rules = {
  nickName: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 消息对话框数据
const messageDialogVisible = ref(false)
const messageFormRef = ref(null)
const messageForm = ref({
  userId: '',
  type: '',
  content: '',
  typeDescription: ''
})
const messageTypes = ref([])

// 消息表单验证规则
const messageRules = {
  type: [{ required: true, message: '请选择消息类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入消息内容', trigger: 'blur' }]
}

// 获取用户列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getUserList(
      currentPage.value, 
      pageSize.value, 
      searchForm.value.status, 
      searchForm.value.username, 
      searchForm.value.userId
    )
    if (res.code === 1) {
      userList.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.msg || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 获取消息类型列表
const getMessageTypeList = async () => {
  try {
    const res = await getMessageTypes()
    if (res.code === 1) {
      messageTypes.value = res.data
    } else {
      ElMessage.error(res.msg || '获取消息类型失败')
    }
  } catch (error) {
    console.error('获取消息类型失败:', error)
    ElMessage.error('获取消息类型失败')
  }
}

// 获取预览内容
const getPreviewContent = () => {
  const selectedType = messageTypes.value.find(item => item.code === messageForm.value.type)
  const typeDescription = selectedType ? selectedType.description : ''
  return `${typeDescription}：${messageForm.value.content}`
}

// 消息类型变更
const handleTypeChange = (value) => {
  const selectedType = messageTypes.value.find(item => item.code === value)
  if (selectedType) {
    messageForm.value.typeDescription = selectedType.description
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  
  const d = new Date(date)
  return `${d.getFullYear()}-${padZero(d.getMonth() + 1)}-${padZero(d.getDate())} ${padZero(d.getHours())}:${padZero(d.getMinutes())}`
}

// 补零
const padZero = (num) => {
  return num < 10 ? `0${num}` : num
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  getList()
}

// 编辑用户
const handleEdit = (row) => {
  form.value = JSON.parse(JSON.stringify(row))
  dialogVisible.value = true
}

// 切换状态
const handleToggleStatus = async (row) => {
  try {
    const res = row.status
      ? await forbidUser(row.userId)
      : await unforbidUser(row.userId)
    
    if (res.code === 1) {
      ElMessage.success(row.status ? '用户已禁用' : '用户已启用')
      row.status = !row.status
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除用户"${row.nickName}"吗？此操作不可恢复！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteUser(row.userId)
      if (res.code === 1) {
        ElMessage.success('用户已删除')
        getList()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 发送消息
const handleSendMessage = (row) => {
  messageForm.value = {
    userId: row.userId,
    type: '',
    content: '',
    typeDescription: ''
  }
  messageDialogVisible.value = true
  // 获取消息类型
  getMessageTypeList()
}

// 发送消息提交
const handleSendMessageSubmit = async () => {
  if (!messageFormRef.value) return
  await messageFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 拼接类型描述和内容
        const finalContent = `${messageForm.value.typeDescription}：${messageForm.value.content}`
        
        const res = await sendMessageToUser(
          messageForm.value.userId,
          finalContent,
          messageForm.value.type
        )
        if (res.code === 1) {
          ElMessage.success('消息发送成功')
          messageDialogVisible.value = false
        } else {
          ElMessage.error(res.msg || '消息发送失败')
        }
      } catch (error) {
        console.error('消息发送失败:', error)
        ElMessage.error('消息发送失败')
      }
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await updateUser(form.value)
        if (res.code === 1) {
          ElMessage.success('编辑成功')
          dialogVisible.value = false
          getList()
        } else {
          ElMessage.error(res.msg || '编辑失败')
        }
      } catch (error) {
        console.error('编辑失败:', error)
        ElMessage.error('编辑失败')
      }
    }
  })
}

// 分页相关
const handleSizeChange = (val) => {
  pageSize.value = val
  getList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  getList()
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.user-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .left {
      display: flex;
      align-items: center;
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .message-preview {
    margin-top: 10px;
    padding: 10px;
    background-color: #f8f8f8;
    border-radius: 4px;
    
    .preview-label {
      font-weight: bold;
      color: #409EFF;
    }
    
    .preview-content {
      margin-top: 5px;
      white-space: pre-wrap;
      word-break: break-all;
    }
  }
}
</style> 