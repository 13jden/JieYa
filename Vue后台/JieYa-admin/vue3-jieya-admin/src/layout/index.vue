<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container" :class="{ 'is-collapse': isCollapse }">
      <div class="logo">
        <img src="../assets/logo.svg" alt="Logo" />
        <span v-show="!isCollapse">后台管理系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        unique-opened
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <template #title>首页</template>
        </el-menu-item>

        <el-sub-menu index="/content">
          <template #title>
            <el-icon><Management /></el-icon>
            <span>内容管理</span>
          </template>
          <el-menu-item index="/content/category">分类管理</el-menu-item>
          <el-menu-item index="/content/banner">轮播图管理</el-menu-item>
          <el-menu-item index="/content/article">稿件管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/product">
          <template #title>
            <el-icon><Shop /></el-icon>
            <span>商品管理</span>
          </template>
          <el-menu-item index="/product/list">商品列表</el-menu-item>
          <el-menu-item index="/product/venue">场地管理</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/user/index">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 主容器 -->
    <div class="main-container">
      <!-- 头部 -->
      <div class="navbar">
        <div class="left">
          <el-icon class="fold-btn" @click="isCollapse = !isCollapse">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
          <breadcrumb />
        </div>
        <div class="right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="avatar-wrapper">
              <el-avatar :size="32" :src="userInfo.avatar" />
              <span class="username">{{ userInfo.name }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleProfile">个人信息</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 主要内容区 -->
      <div class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  DataLine,
  Management,
  Shop,
  User,
  Expand,
  Fold
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import Breadcrumb from '@/components/Breadcrumb.vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)

const userInfo = ref({
  name: 'Admin',
  avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
})

const activeMenu = computed(() => route.path)

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
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('loginTime')
    router.push('/login')
  })
}
</script>

<style lang="scss" scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
  width: 100%;

  .sidebar-container {
    width: 210px;
    height: 100%;
    background: #304156;
    transition: width 0.3s;
    overflow: hidden;

    &.is-collapse {
      width: 64px;
    }

    .logo {
      height: 50px;
      display: flex;
      align-items: center;
      padding: 10px;
      background: #2b2f3a;
      
      img {
        width: 32px;
        height: 32px;
        margin-right: 12px;
      }

      span {
        color: #fff;
        font-size: 16px;
        font-weight: 600;
        white-space: nowrap;
      }
    }
  }

  .main-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .navbar {
      height: 50px;
      background: #fff;
      box-shadow: 0 1px 4px rgba(0,21,41,.08);
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 15px;

      .left {
        display: flex;
        align-items: center;

        .fold-btn {
          padding: 0 15px;
          cursor: pointer;
          font-size: 20px;
          &:hover {
            color: #409EFF;
          }
        }
      }

      .avatar-wrapper {
        display: flex;
        align-items: center;
        cursor: pointer;
        
        .username {
          margin-left: 8px;
          font-size: 14px;
        }
      }
    }

    .app-main {
      flex: 1;
      padding: 20px;
      overflow-y: auto;
      background: #f0f2f5;
    }
  }
}

// 路由切换动画
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style> 