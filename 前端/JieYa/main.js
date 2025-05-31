import App from './App'

// #ifndef VUE3
import Vue from 'vue'
import './uni.promisify.adaptor'
Vue.config.productionTip = false
App.mpType = 'app'
const app = new Vue({
  ...App
})
app.$mount()
// #endif

// #ifdef VUE3
import { createSSRApp } from 'vue'
export function createApp() {
  const app = createSSRApp(App)
  return {
    app
  }
}
// #endif

// 拦截Tab切换
uni.addInterceptor('switchTab', {
  success(e) {
    console.log('切换Tab:', e);
    
    // 获取应用实例并更新角标
    const app = getApp();
    if (app && app.updateUnreadMessageCount) {
      setTimeout(() => {
        app.updateUnreadMessageCount();
      }, 200);
    }
  }
});