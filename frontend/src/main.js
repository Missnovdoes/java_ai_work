import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import supabase from './supabase'
import config from './config'

const app = createApp(App)
app.use(router)

// 检查并提示创建Supabase user表
async function checkUserTable() {
  try {
    console.log('开始检查users表...')
    
    // 尝试查询users表，检查是否存在
    const { data, error } = await supabase
      .from('users')
      .select('id')
      .limit(1)
    
    console.log('查询users表结果:', { data, error })
    
    if (!error) {
      console.log('✓ users表已存在')
      return
    }
    
    // 如果表不存在，显示创建表的指导
    console.log('⚠️  检测到users表不存在或无法访问，请在Supabase控制台创建表')
    console.log('\n建议的users表结构:')
    console.log('1. id: UUID (主键, 与Auth用户ID关联)')
    console.log('   - 类型: UUID')
    console.log('   - 约束: PRIMARY KEY')
    console.log('   - 注释: 与Supabase Auth的user.id保持一致')
    console.log('2. email: Text (唯一, 必填)')
    console.log('   - 类型: Text')
    console.log('   - 约束: UNIQUE, NOT NULL')
    console.log('3. name: Text (可选)')
    console.log('   - 类型: Text')
    console.log('4. created_at: Timestamp')
    console.log('   - 类型: Timestamp with time zone')
    console.log('   - 默认值: now()')
    console.log('5. updated_at: Timestamp')
    console.log('   - 类型: Timestamp with time zone')
    console.log('   - 默认值: now()')
    console.log('\n注意事项:')
    console.log('- 确保表名为"users"（小写）')
    console.log('- 确保Supabase的anon key有INSERT和SELECT权限')
    console.log('- 在Supabase控制台创建表后，应用即可正常工作')
    console.log('\nSupabase控制台地址:', config.supabase.url)
    
  } catch (error) {
    console.error('检查users表失败:', error)
  }
}

// 调用检查表函数
checkUserTable()

app.mount('#app')
