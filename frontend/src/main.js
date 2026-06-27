import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import supabase from './supabase'

const app = createApp(App)
app.use(router)

// 检查 users 表是否存在
async function checkUserTable() {
  try {
    const { error } = await supabase
      .from('users')
      .select('id')
      .limit(1)

    if (!error) return

    console.warn('users 表不存在，请在 Supabase 控制台创建。建议结构: id(UUID PK), email(TEXT UNIQUE NOT NULL), name(TEXT), created_at, updated_at')
  } catch (err) {
    console.warn('检查 users 表失败:', err)
  }
}

// 调用检查表函数
checkUserTable()

app.mount('#app')
