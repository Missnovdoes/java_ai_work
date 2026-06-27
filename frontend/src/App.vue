<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 导航栏 -->
    <header class="bg-white shadow-sm sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-16">
          <!-- 网站标志 -->
          <div class="flex items-center">
            <span class="text-2xl font-bold text-blue-600">智购</span>
            <span class="text-lg font-medium text-gray-700 ml-1">二手交易平台</span>
          </div>
          
          <!-- 导航菜单 -->
          <nav class="hidden md:flex items-center space-x-8">
            <a href="#" class="text-gray-600 hover:text-blue-600 font-medium">首页</a>
            <a href="#" class="text-gray-600 hover:text-blue-600 font-medium">商品分类</a>
            <a href="#" class="text-gray-600 hover:text-blue-600 font-medium">热门商品</a>
            <a href="#" class="text-gray-600 hover:text-blue-600 font-medium">优惠活动</a>
            <a href="#" class="text-gray-600 hover:text-blue-600 font-medium">关于我们</a>
          </nav>
          
          <!-- 用户操作 -->
          <div class="flex items-center space-x-4">
            <router-link to="/cart" class="text-gray-600 hover:text-blue-600 relative" title="购物车">
              <span class="text-xl">🛒</span>
              <span v-if="cartCount > 0" class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">{{ cartCount }}</span>
            </router-link>
            <button class="text-gray-600 hover:text-blue-600" title="收藏">
              <span class="text-xl">❤️</span>
            </button>
            <div class="relative">
              <router-link to="/profile" class="text-gray-600 hover:text-blue-600" title="用户中心">
                <span class="text-xl">👤</span>
              </router-link>
            </div>
            <div>
              <!-- 根据用户登录状态显示不同按钮 -->
              <template v-if="currentUser">
                <!-- 登录后显示个人中心和退出登录 -->
                <router-link to="/profile" class="inline-block px-4 py-2 text-sm font-medium text-gray-600 hover:text-blue-600 mr-2">
                  个人中心
                </router-link>
                <button @click="handleLogout" class="inline-block px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg">
                  退出登录
                </button>
              </template>
              <template v-else>
                <!-- 未登录显示登录和注册 -->
                <router-link to="/login" class="inline-block px-4 py-2 text-sm font-medium text-gray-600 hover:text-blue-600">
                  登录
                </router-link>
                <router-link to="/register" class="inline-block px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg">
                  注册
                </router-link>
              </template>
            </div>
          </div>
        </div>
      </div>
    </header>
    
    <!-- 主要内容 -->
    <main class="flex-1">
      <router-view />
    </main>
    
    <!-- 页脚 -->
    <footer class="bg-gray-800 text-white mt-12">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
          <!-- 关于我们 -->
          <div>
            <h3 class="text-lg font-semibold mb-4">关于我们</h3>
            <p class="text-gray-400 text-sm">二手交易平台，为您提供优质的二手商品和服务。</p>
          </div>
          
          <!-- 快速链接 -->
          <div>
            <h3 class="text-lg font-semibold mb-4">快速链接</h3>
            <ul class="space-y-2 text-gray-400 text-sm">
              <li><a href="#" class="hover:text-white">首页</a></li>
              <li><a href="#" class="hover:text-white">商品分类</a></li>
              <li><a href="#" class="hover:text-white">热门商品</a></li>
              <li><a href="#" class="hover:text-white">优惠活动</a></li>
            </ul>
          </div>
          
          <!-- 联系方式 -->
          <div>
            <h3 class="text-lg font-semibold mb-4">联系方式</h3>
            <ul class="space-y-2 text-gray-400 text-sm">
              <li>📧 email@example.com</li>
              <li>📞 400-123-4567</li>
              <li>📍 北京市朝阳区科技园区</li>
            </ul>
          </div>
          
          <!-- 关注我们 -->
          <div>
            <h3 class="text-lg font-semibold mb-4">关注我们</h3>
            <div class="flex space-x-4 text-gray-400">
              <a href="#" class="hover:text-white text-xl">📱</a>
              <a href="#" class="hover:text-white text-xl">🐦</a>
              <a href="#" class="hover:text-white text-xl">📘</a>
              <a href="#" class="hover:text-white text-xl">📷</a>
            </div>
          </div>
        </div>
        
        <!-- 版权信息 -->
        <div class="mt-12 pt-8 border-t border-gray-700 text-center text-gray-500 text-sm">
          <p>© {{ currentYear }} 智购二手交易平台. 保留所有权利.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import authService from './auth.js'

const currentYear = new Date().getFullYear()

// 用户状态
const currentUser = ref(null)

// 购物车数量
const cartCount = ref(0)

function updateCartCount() {
  const saved = localStorage.getItem('cartItems')
  if (saved) {
    const items = JSON.parse(saved)
    cartCount.value = items.reduce((sum, item) => sum + (item.quantity || 1), 0)
  } else {
    cartCount.value = 0
  }
}

// 获取当前用户
async function fetchCurrentUser() {
  const user = await authService.getCurrentUser()
  currentUser.value = user
}

// 监听用户认证状态变化
let authUnsubscribe = null

// 组件挂载时获取用户信息
onMounted(() => {
  fetchCurrentUser()
  updateCartCount()
  window.addEventListener('cart-updated', updateCartCount)

  const { data } = authService.onAuthStateChange((_event, _session) => {
    fetchCurrentUser()
  })
  authUnsubscribe = data?.subscription?.unsubscribe
})

onUnmounted(() => {
  window.removeEventListener('cart-updated', updateCartCount)
  if (authUnsubscribe) authUnsubscribe()
})

// 用户登出
async function handleLogout() {
  await authService.logout()
  currentUser.value = null
}
</script>

<style>
/* 全局样式 */
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
