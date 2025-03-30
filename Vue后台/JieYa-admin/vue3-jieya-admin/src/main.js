import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import './styles/index.scss'
import { createPinia } from 'pinia'
import websocketPlugin from './plugins/websocket'

const app = createApp(App)
const pinia = createPinia()

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)  // 先使用Pinia
app.use(router)
app.use(ElementPlus, { size: 'default' })
app.use(websocketPlugin)

app.mount('#app')
