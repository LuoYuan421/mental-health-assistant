import { createApp } from 'vue'
import './style.css'
import './styles/dark.css'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { createPinia } from 'pinia'

const app = createApp(App)

const pinia = createPinia()

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// Apply dark mode from localStorage before mount
const savedDark = localStorage.getItem('darkMode')
if (savedDark === 'true') {
  document.documentElement.classList.add('dark')
  document.documentElement.setAttribute('data-theme', 'dark')
}

app.use(ElementPlus).use(router).use(pinia).mount('#app')

