<template>
  <div class="min-h-screen bg-gray-100 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          创建账号
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          已有账号？
          <router-link to="/login" class="font-medium text-blue-600 hover:text-blue-500">
            立即登录
          </router-link>
        </p>
      </div>
      <form class="mt-8 space-y-6" @submit.prevent="handleRegister">
        <div class="rounded-md shadow-sm -space-y-px">
          <div>
            <label for="username" class="sr-only">用户名</label>
            <input
              id="username"
              name="username"
              type="text"
              autocomplete="username"
              required
              v-model="form.username"
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
              placeholder="用户名"
            />
          </div>
          <div>
            <label for="email" class="sr-only">邮箱地址</label>
            <input
              id="email"
              name="email"
              type="email"
              autocomplete="email"
              required
              v-model="form.email"
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
              placeholder="邮箱地址"
            />
          </div>
          <div>
            <label for="password" class="sr-only">密码</label>
            <input
              id="password"
              name="password"
              type="password"
              autocomplete="new-password"
              required
              v-model="form.password"
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
              placeholder="密码（至少6位）"
            />
          </div>
          <div>
            <label for="password_confirmation" class="sr-only">确认密码</label>
            <input
              id="password_confirmation"
              name="password_confirmation"
              type="password"
              autocomplete="new-password"
              required
              v-model="form.password_confirmation"
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
              placeholder="确认密码"
            />
          </div>
        </div>

        <div>
          <button
            type="submit"
            :disabled="loading"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="loading" class="inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></span>
            {{ loading ? '注册中...' : '注册账号' }}
          </button>
        </div>

        <!-- 错误信息 -->
        <div v-if="error" class="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded">
          {{ error }}
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import authService from '../auth.js'

const router = useRouter()
const loading = ref(false)
const error = ref('')
const form = ref({
  username: '',
  email: '',
  password: '',
  password_confirmation: ''
})

async function handleRegister() {
  // 重置错误信息
  error.value = ''
  
  // 表单验证
  if (!form.value.username || !form.value.email || !form.value.password || !form.value.password_confirmation) {
    error.value = '请填写所有必填字段'
    return
  }
  
  if (form.value.password !== form.value.password_confirmation) {
    error.value = '密码和确认密码不匹配'
    return
  }
  
  if (form.value.password.length < 6) {
    error.value = '密码长度至少为6位'
    return
  }
  
  try {
    loading.value = true
    // 准备用户数据
    const userData = {
      name: form.value.username
    }
    
    // 调用注册服务
    await authService.register(form.value.email, form.value.password, userData)
    
    // 注册成功，跳转到首页
    router.push('/')
  } catch (err) {
    error.value = err.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>
