import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import './styles/theme.css'

const app = createApp(App)

// 全局注册所有 Element Plus 图标，模板中可直接使用 <Search/> <Bell/> 等
for (const [name, comp] of Object.entries(ElementPlusIconsVue)) {
  app.component(name, comp as never)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
