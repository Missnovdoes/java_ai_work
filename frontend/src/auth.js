import supabase from './supabase.js'

const authService = {
  async register(email, password, userData = {}) {
    try {
      const { data, error } = await supabase.auth.signUp(
        { email, password },
        { data: userData }
      )

      if (error) {
        console.error('注册失败:', error)
        throw error
      }

      if (data && data.user) {
        const user = data.user
        const { error: dbError } = await supabase
          .from('users')
          .insert({
            id: user.id,
            email: user.email,
            name: userData.name || user.email.split('@')[0],
            created_at: new Date().toISOString(),
            ...userData
          })

        if (dbError) {
          console.warn('用户信息写入users表失败:', dbError)
        }
      }

      return data
    } catch (error) {
      console.error('注册失败:', error)
      throw error
    }
  },

  async login(email, password) {
    try {
      const { data, error } = await supabase.auth.signInWithPassword({
        email,
        password
      })

      if (error) {
        console.error('登录失败:', error)
        throw error
      }

      if (data && data.user) {
        const { data: userData, error: dbError } = await supabase
          .from('users')
          .select('*')
          .eq('id', data.user.id)
          .single()

        if (userData) {
          data.user.name = userData.name
          data.user.userData = userData
        } else if (dbError) {
          console.warn('获取用户信息失败:', dbError)
        }
      }

      return data
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  },

  async logout() {
    const { error } = await supabase.auth.signOut()
    if (error) {
      console.error('登出失败:', error)
      throw error
    }
    return true
  },

  async getCurrentUser() {
    try {
      const { data, error } = await supabase.auth.getUser()

      if (error) {
        return null
      }

      if (data && data.user) {
        const { data: userData, error: dbError } = await supabase
          .from('users')
          .select('*')
          .eq('id', data.user.id)
          .single()

        if (userData) {
          data.user.name = userData.name
          data.user.userData = userData
        } else if (dbError) {
          console.warn('获取用户信息失败:', dbError)
        }
      }

      return data ? data.user : null
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return null
    }
  },

  onAuthStateChange(callback) {
    return supabase.auth.onAuthStateChange(callback)
  }
}

export default authService
