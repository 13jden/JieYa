import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: {
      requiresAuth: false
    }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/index.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'dashboard' }
      }
    ]
  },
  {
    path: '/content',
    component: Layout,
    meta: { title: '内容管理', icon: 'document' },
    children: [
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/content/category.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'banner',
        name: 'Banner',
        component: () => import('@/views/content/banner.vue'),
        meta: { title: '轮播图管理' }
      },
      {
        path: 'article',
        name: 'Article',
        component: () => import('@/views/content/article.vue'),
        meta: { title: '稿件管理' }
      }
    ]
  },
  {
    path: '/product',
    component: Layout,
    meta: { title: '商品管理', icon: 'shopping' },
    children: [
      {
        path: 'list',
        name: 'ProductList',
        component: () => import('@/views/product/list.vue'),
        meta: { title: '商品列表' }
      },
      {
        path: 'venue',
        name: 'Venue',
        component: () => import('@/views/product/venue.vue'),
        meta: { title: '场地管理' }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'user' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由拦截器
router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('token')
  
  if (to.path === '/login') {
    // 如果已登录，访问登录页则重定向到首页
    if (token) {
      next('/')
    } else {
      next()
    }
  } else {
    // 如果未登录，访问其他页面则重定向到登录页
    if (!token) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router 