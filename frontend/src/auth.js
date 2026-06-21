import supabase from './supabase.js'
import config from './config.js'

// 认证服务
const authService = {
  // 用户注册
  async register(email, password, userData = {}) {
    try {
      console.log('开始注册流程:', { email, password: '******', userData })
      console.log('当前使用的Supabase客户端:', config.useMock ? '模拟客户端' : '真实客户端')
      
      // 先检查是否能获取到当前用户（用于调试）
      const { data: currentUser, error: currentUserError } = await supabase.auth.getUser()
      console.log('注册前获取当前用户结果:', { currentUser, currentUserError })
      
      const { data, error } = await supabase.auth.signUp(
        { email, password },
        { data: userData }
      )
      
      console.log('Auth注册结果:', { data, error })
      console.log('注册成功后的用户信息:', data ? data.user : null)
      console.log('注册成功后的会话信息:', data ? data.session : null)
      
      if (error) {
        console.error('Auth注册失败:', error)
        throw error
      }
      
      // 注册成功后，验证用户是否真的存在于Supabase Auth中
      if (data && data.user) {
        const { data: verifyUser, error: verifyError } = await supabase.auth.getUser()
        console.log('验证注册用户结果:', { verifyUser, verifyError })
        
        // 注意：listUsers是管理API，只能在服务器端使用，客户端无法直接调用
        console.log('用户已成功在Auth中注册:', data.user.email)
        
        const user = data.user
        console.log('准备插入users表的用户数据:', {
          id: user.id,
          email: user.email,
          name: userData.name || user.email.split('@')[0],
          created_at: new Date().toISOString(),
          ...userData
        })
        
        const { data: insertData, error: dbError } = await supabase
          .from('users')
          .insert({
            id: user.id,
            email: user.email,
            name: userData.name || user.email.split('@')[0],
            created_at: new Date().toISOString(),
            ...userData
          })
          .select()
        
        console.log('插入users表结果:', { insertData, dbError })
        
        if (dbError) {
          console.warn('用户已在Auth中创建，但未能将信息存入users表:', dbError)
          // 不抛出错误，因为Auth注册已经成功
        } else {
          console.log('用户信息已成功存入users表:', insertData)
        }
      }
      
      return data
    } catch (error) {
      console.error('注册失败:', error)
      throw error
    }
  },
  
  // 用户登录
  async login(email, password) {
    try {
      console.log('开始登录流程:', { email })
      console.log('当前使用的Supabase客户端:', config.useMock ? '模拟客户端' : '真实客户端')
      
      // 先检查是否能获取到当前用户（用于调试）
      const { data: currentUser, error: currentUserError } = await supabase.auth.getUser()
      console.log('登录前获取当前用户结果:', { currentUser, currentUserError })
      
      const { data, error } = await supabase.auth.signInWithPassword({
        email,
        password
      })
      
      console.log('Auth登录结果:', { data, error })
      console.log('登录成功后的用户信息:', data ? data.user : null)
      console.log('登录成功后的会话信息:', data ? data.session : null)
      
      if (error) {
        console.error('Auth登录失败:', error)
        // 注意：listUsers是管理API，只能在服务器端使用，客户端无法直接调用
        // 登录失败可能是因为用户不存在或密码不正确
        console.log('登录失败原因可能是：用户不存在或密码不正确')
        throw error
      }
      
      // 登录成功后，从users表获取完整用户信息
      if (data && data.user) {
        const user = data.user
        const { data: userData, error: dbError } = await supabase
          .from('users')
          .select('*')
          .eq('id', user.id)
          .single()
        
        if (userData) {
          // 将用户表中的信息合并到用户对象中
          user.name = userData.name
          user.created_at = userData.created_at
          user.userData = userData
        } else if (dbError) {
          console.warn('登录成功，但未能从users表获取用户信息:', dbError)
        }
      }
      
      return data
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  },
  
  // 用户登出
  async logout() {
    try {
      const { error } = await supabase.auth.signOut()
      
      if (error) {
        console.error('登出失败:', error)
        throw error
      }
      
      return true
    } catch (error) {
      console.error('登出失败:', error)
      throw error
    }
  },
  
  // 获取当前用户信息
  async getCurrentUser() {
    try {
      const { data, error } = await supabase.auth.getUser()
      
      console.log('获取当前用户结果:', { data, error })
      
      if (error) {
        console.error('获取当前用户失败:', error)
        return null
      }
      
      if (data && data.user) {
        const user = data.user
        // 从users表获取完整用户信息
        const { data: userData, error: dbError } = await supabase
          .from('users')
          .select('*')
          .eq('id', user.id)
          .single()
        
        if (userData) {
          // 将用户表中的信息合并到用户对象中
          user.name = userData.name
          user.created_at = userData.created_at
          user.userData = userData
        } else if (dbError) {
          console.warn('未能从users表获取用户信息:', dbError)
        }
      }
      
      return data ? data.user : null
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return null
    }
  },
  
  // 监听用户认证状态变化
  onAuthStateChange(callback) {
    return supabase.auth.onAuthStateChange(callback)
  }
}

export default authService
