import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import { useAuth } from './composables/useAuth'
import router from './router'

void useAuth().restore()

createApp(App).use(router).mount('#app')
