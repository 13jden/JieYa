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

<script>
import { getCheckCode, login } from '@/api/admin'

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        account: '',
        password: '',
        checkCode: '',
        checkCodeKey: ''
      },
      loginRules: {
        account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        checkCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
      },
      loading: false,
      checkCodeImg: ''
    }
  },
  created() {
    this.refreshCheckCode()
  },
  methods: {
    // 刷新验证码
    async refreshCheckCode() {
      try {
        const res = await getCheckCode()
        if (res.code === 1) {
          this.checkCodeImg = res.data.checkCode
          this.loginForm.checkCodeKey = res.data.checkCodeKey
        }
      } catch (error) {
        console.error('获取验证码失败:', error)
        this.$message.error('获取验证码失败')
      }
    },
    // 处理登录
    handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (valid) {
          this.loading = true
          try {
            // 检查验证码是否已获取
            if (!this.loginForm.checkCodeKey) {
              this.$message.error('请先获取验证码')
              this.refreshCheckCode()
              return
            }
            
            const res = await login(this.loginForm)
            if (res.code === 1) {
              this.$message.success('登录成功')
              // 登录成功，将时间戳和token组合存储
              const timestamp = Date.now()
              const tokenWithTime = `${res.data.token}_${timestamp}`
              sessionStorage.setItem('token', tokenWithTime)
              this.$router.push('/dashboard')
            } else {
              this.$message.error(res.message || '登录失败')
              this.refreshCheckCode()
            }
          } catch (error) {
            console.error('登录失败:', error)
            this.refreshCheckCode()
          } finally {
            this.loading = false
          }
        }
      })
    }
  }
}
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