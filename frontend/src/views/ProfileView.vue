<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-3xl font-bold mb-8">用户中心</h1>
    
    <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
      <!-- 侧边导航 -->
      <div class="lg:col-span-1">
        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="flex items-center mb-6">
            <div class="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center text-blue-600 text-2xl font-bold">
              {{ userInfo.name.charAt(0) }}
            </div>
            <div class="ml-4">
              <div class="text-lg font-semibold">{{ userInfo.name }}</div>
              <div class="text-sm text-gray-500">{{ userInfo.email }}</div>
            </div>
          </div>
          
          <nav>
            <ul class="space-y-2">
              <li>
                <a href="#" class="flex items-center p-3 bg-blue-50 text-blue-600 rounded-lg font-medium">
                  <span class="mr-3">👤</span>
                  个人信息
                </a>
              </li>
              <li>
                <router-link to="/cart" class="flex items-center p-3 hover:bg-gray-50 text-gray-700 rounded-lg transition-colors">
                  <span class="mr-3">📦</span>
                  我的订单
                </router-link>
              </li>
              <li>
                <a href="#" class="flex items-center p-3 hover:bg-gray-50 text-gray-700 rounded-lg transition-colors">
                  <span class="mr-3">❤️</span>
                  我的收藏
                </a>
              </li>
              <li>
                <a href="#" class="flex items-center p-3 hover:bg-gray-50 text-gray-700 rounded-lg transition-colors">
                  <span class="mr-3">⚙️</span>
                  账户设置
                </a>
              </li>
              <li>
                <router-link to="/customer-service" class="flex items-center p-3 hover:bg-gray-50 text-gray-700 rounded-lg transition-colors">
                  <span class="mr-3">💬</span>
                  客服中心
                </router-link>
              </li>
            </ul>
          </nav>
        </div>
      </div>
      
      <!-- 主要内容 -->
      <div class="lg:col-span-3">
        <div class="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 class="text-xl font-semibold mb-4">个人信息</h2>
          <form @submit.prevent="updateProfile">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
              <div>
                <label for="username" class="block text-sm font-medium text-gray-700 mb-1">用户名</label>
                <input type="text" id="username" v-model="userInfo.name" required class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500">
              </div>
              <div>
                <label for="email" class="block text-sm font-medium text-gray-700 mb-1">电子邮箱</label>
                <input type="email" id="email" v-model="userInfo.email" disabled class="w-full px-4 py-2 border border-gray-300 rounded-lg bg-gray-50">
              </div>
            </div>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
              <div>
                <label for="phone" class="block text-sm font-medium text-gray-700 mb-1">手机号码</label>
                <input type="tel" id="phone" v-model="userInfo.phone" required class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500">
              </div>
              <div>
                <label for="gender" class="block text-sm font-medium text-gray-700 mb-1">性别</label>
                <select id="gender" v-model="userInfo.gender" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500">
                  <option value="">请选择</option>
                  <option value="male">男</option>
                  <option value="female">女</option>
                  <option value="other">其他</option>
                </select>
              </div>
            </div>
            
            <div class="mb-6">
              <label for="address" class="block text-sm font-medium text-gray-700 mb-1">常用地址</label>
              <textarea id="address" v-model="userInfo.address" rows="3" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"></textarea>
            </div>
            
            <div class="flex justify-end">
              <button type="button" class="mr-4 px-6 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors">
                取消
              </button>
              <button type="submit" class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
                保存修改
              </button>
            </div>
          </form>
        </div>
        
        <div class="bg-white rounded-lg shadow-md p-6">
          <h2 class="text-xl font-semibold mb-4">最近订单</h2>
          
          <div v-if="recentOrders.length === 0" class="text-center py-8">
            <div class="text-4xl mb-3">📭</div>
            <div class="text-gray-500">暂无订单记录</div>
          </div>
          
          <div v-else class="space-y-4">
            <div v-for="order in recentOrders" :key="order.id" class="border border-gray-200 rounded-lg p-4">
              <div class="flex justify-between items-center mb-3">
                <div class="text-sm font-medium">订单号：{{ order.id }}</div>
                <div class="text-sm text-green-600">{{ order.status }}</div>
              </div>
              <div class="flex items-center mb-3">
                <div class="flex-shrink-0 h-16 w-16">
                  <img :src="order.items[0].image" :alt="order.items[0].name" class="h-16 w-16 object-cover">
                </div>
                <div class="ml-4">
                  <div class="text-sm font-medium">{{ order.items[0].name }}</div>
                  <div class="text-xs text-gray-500">{{ order.items.length }} 件商品</div>
                </div>
                <div class="ml-auto">
                  <div class="text-sm font-medium">¥{{ order.totalPrice.toFixed(2) }}</div>
                </div>
              </div>
              <div class="flex justify-end space-x-2">
                <button class="text-sm px-3 py-1 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
                  查看详情
                </button>
                <button class="text-sm px-3 py-1 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
                  再次购买
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import authService from '../auth.js'

// 用户信息
const userInfo = ref({
  name: '游客',
  email: '',
  phone: '',
  gender: '',
  address: ''
})

// 最近订单
const recentOrders = ref([])

// 加载用户信息
onMounted(async () => {
  try {
    const currentUser = await authService.getCurrentUser()
    if (currentUser) {
      // 更新用户基本信息
      userInfo.value.name = currentUser.data?.name || currentUser.email.split('@')[0]
      userInfo.value.email = currentUser.email
      
      // 加载用户扩展信息（如果有）
      userInfo.value.phone = currentUser.data?.phone || ''
      userInfo.value.gender = currentUser.data?.gender || ''
      userInfo.value.address = currentUser.data?.address || ''
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
})

// 更新个人信息
const updateProfile = () => {
  // 在实际应用中，这里应该调用API更新用户信息
  alert('个人信息更新成功！')
}
</script>