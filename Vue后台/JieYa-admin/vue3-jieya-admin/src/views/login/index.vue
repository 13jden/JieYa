<template>
  <div class="login-container">
    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">后台管理系统</h3>
      <el-form-item prop="account">
        <el-input
          v-model="loginForm.account"
          placeholder="账号"
          type="text"
          auto-complete="off"
        />
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          placeholder="密码"
          type="password"
          auto-complete="off"
          @keyup.enter.native="handleLogin"
        />
      </el-form-item>
      <el-form-item prop="checkCode">
        <div class="check-code-container">
          <el-input
            v-model="loginForm.checkCode"
            placeholder="验证码"
            type="text"
            auto-complete="off"
            style="width: 60%"
          />
          <img
            :src="checkCodeImg"
            class="check-code-img"
            @click="refreshCheckCode"
            alt="验证码"
          />
        </div>
      </el-form-item>
      <el-button
        :loading="loading"
        type="primary"
        style="width: 100%"
        @click.native.prevent="handleLogin"
      >
        登录
      </el-button>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElForm } from 'element-plus'
import { getCheckCode, login } from '@/api/admin'
import { inject } from 'vue'

// 获取依赖
const router = useRouter()
const websocket = inject('websocket')

// 表单引用
const loginFormRef = ref(null)

// 响应式数据
const loading = ref(false)
const checkCodeImg = ref('')

const loginForm = reactive({
  account: '',
  password: '',
  checkCode: '',
  checkCodeKey: ''
})

const loginRules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  checkCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// 获取Cookie工具函数
function getCookie(name) {
  const value = `; ${document.cookie}`
  const parts = value.split(`; ${name}=`)
  if (parts.length === 2) return parts.pop().split(';').shift()
  return null
}

// 刷新验证码
async function refreshCheckCode() {
  try {
    const res = await getCheckCode()
    if (res.code === 1) {
      checkCodeImg.value = res.data.checkCode
      loginForm.checkCodeKey = res.data.checkCodeKey
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败')
  }
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 检查验证码是否已获取
        if (!loginForm.checkCodeKey) {
          ElMessage.error('请先获取验证码')
          refreshCheckCode()
          return
        }
        
        const res = await login(loginForm)
        if (res.code === 1) {
          ElMessage.success('登录成功')
          
          // 从cookie获取token并保存到sessionStorage
          const token = getCookie('adminToken')
          console.log('保存token:', token) // 调试用
          
          if (token) {
            sessionStorage.setItem('token', token)
            
            // 连接WebSocket
            if (websocket) {
              websocket.connect()
            }
            
            router.push('/dashboard')
          } else {
            ElMessage.error('获取token失败')
            refreshCheckCode()
          }
        } else {
          ElMessage.error(res.message || '登录失败')
          refreshCheckCode()
        }
      } catch (error) {
        console.error('登录失败:', error)
        refreshCheckCode()
      } finally {
        loading.value = false
      }
    }
  })
}

// 组件挂载时获取验证码
onMounted(() => {
  refreshCheckCode()
})
</script>


<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: url('../../assets/cover.jpg') no-repeat center center;
  background-size: cover;

  .login-form {
    width: 400px;
    padding: 30px;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

    .title {
      text-align: center;
      margin-bottom: 30px;
      color: #333;
    }

    .check-code-container {
      display: flex;
      align-items: center;
      gap: 10px;

      .check-code-img {
        height: 40px;
        cursor: pointer;
      }
    }
  }
}
</style> 