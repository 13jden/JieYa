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
    name: 'product',
    meta: { title: '商品管理', icon: 'ShoppingCart' },
    redirect: '/product/venue',
    children: [
      {
        path: 'venue',
        name: 'Venue',
        component: () => import('@/views/product/venue.vue'),
        meta: { title: '场地管理' }
      },
      {
        path: 'venue/booking',
        name: 'VenueBooking',
        component: () => import('@/views/product/venue/booking.vue'),
        meta: { title: '场地预约订单' }
      },
      {
        path: 'prop',
        name: 'Prop',
        component: () => import('@/views/product/prop.vue'),
        meta: { title: '道具管理' }
      },
      {
        path: 'prop/rental',
        name: 'PropRental',
        component: () => import('@/views/product/prop/rental.vue'),
        meta: { title: '道具租借订单' }
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
  },
  {
    path: '/message',
    component: Layout,
    name: 'Message',
    meta: { title: '消息中心', icon: 'Message' },
    children: [
      {
        path: '',
        name: 'MessageCenter',
        component: () => import('@/views/message/index.vue'),
        meta: { title: '消息中心', keepAlive: true }
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
    const token = getCookie('adminToken');
    if (!token) {
      next('/login')
    } else {
      sessionStorage.setItem('token', token);
      next()
    }
  }
})
function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop().split(';').shift();
  return null;
}
export default router 