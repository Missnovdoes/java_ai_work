<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-3xl font-bold mb-8">购物车</h1>
    
    <div v-if="cartItems.length === 0" class="bg-white rounded-lg shadow-md p-12 text-center">
      <div class="text-6xl mb-4">🛒</div>
      <h2 class="text-xl font-semibold mb-2">购物车是空的</h2>
      <p class="text-gray-500 mb-6">快去添加一些商品吧！</p>
      <router-link to="/" class="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg text-sm font-medium transition-colors">
        去购物
      </router-link>
    </div>
    
    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- 购物车商品列表 -->
      <div class="lg:col-span-2">
        <div class="bg-white rounded-lg shadow-md overflow-hidden">
          <table class="w-full">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">商品</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">价格</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">数量</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">小计</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200">
              <tr v-for="item in cartItems" :key="item.id">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="flex-shrink-0 h-16 w-16">
                      <img :src="item.image" :alt="item.name" class="h-16 w-16 object-cover">
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900">{{ item.name }}</div>
                      <div class="text-sm text-gray-500">{{ item.attributes }}</div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-900">¥{{ item.price }}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <button @click="decreaseQuantity(item.id)" class="bg-gray-100 hover:bg-gray-200 text-gray-800 w-8 h-8 rounded-full flex items-center justify-center transition-colors">
                      -
                    </button>
                    <span class="mx-3 text-sm font-medium">{{ item.quantity }}</span>
                    <button @click="increaseQuantity(item.id)" class="bg-gray-100 hover:bg-gray-200 text-gray-800 w-8 h-8 rounded-full flex items-center justify-center transition-colors">
                      +
                    </button>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm font-medium text-gray-900">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <button @click="removeFromCart(item.id)" class="text-red-600 hover:text-red-800 text-sm">
                    删除
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      
      <!-- 订单摘要 -->
      <div class="lg:col-span-1">
        <div class="bg-white rounded-lg shadow-md p-6">
          <h2 class="text-xl font-semibold mb-6">订单摘要</h2>
          
          <div class="space-y-4 mb-6">
            <div class="flex justify-between">
              <span class="text-gray-600">商品总额</span>
              <span class="text-gray-900">¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">运费</span>
              <span class="text-gray-900">¥{{ shippingFee.toFixed(2) }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">优惠</span>
              <span class="text-green-600">-¥{{ discount.toFixed(2) }}</span>
            </div>
          </div>
          
          <div class="border-t border-gray-200 pt-4 mb-6">
            <div class="flex justify-between font-semibold text-lg">
              <span>应付总额</span>
              <span>¥{{ finalPrice.toFixed(2) }}</span>
            </div>
          </div>
          
          <router-link to="/checkout" class="block w-full bg-blue-600 hover:bg-blue-700 text-white py-3 px-4 rounded-lg text-center font-medium transition-colors">
            去结账
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 购物车商品数据
const cartItems = ref([])

// 计算属性
const totalPrice = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const shippingFee = computed(() => {
  return totalPrice.value >= 100 ? 0 : 10
})

const discount = computed(() => {
  return totalPrice.value >= 500 ? 50 : 0
})

const finalPrice = computed(() => {
  return totalPrice.value + shippingFee.value - discount.value
})

// 方法
const increaseQuantity = (id) => {
  const item = cartItems.value.find(item => item.id === id)
  if (item) {
    item.quantity++
    saveToLocalStorage()
  }
}

const decreaseQuantity = (id) => {
  const item = cartItems.value.find(item => item.id === id)
  if (item && item.quantity > 1) {
    item.quantity--
    saveToLocalStorage()
  }
}

const removeFromCart = (id) => {
  cartItems.value = cartItems.value.filter(item => item.id !== id)
  saveToLocalStorage()
}

const saveToLocalStorage = () => {
  localStorage.setItem('cartItems', JSON.stringify(cartItems.value))
}

// 从本地存储加载购物车数据
onMounted(() => {
  const savedCart = localStorage.getItem('cartItems')
  if (savedCart) {
    cartItems.value = JSON.parse(savedCart)
  } else {
    // 模拟数据
    cartItems.value = [
      {
        id: 1,
        name: '智能手表 Pro',
        image: 'https://via.placeholder.com/100x100?text=智能手表',
        price: 1999,
        quantity: 1,
        attributes: '颜色：黑色，尺寸：42mm'
      },
      {
        id: 2,
        name: '无线耳机',
        image: 'https://via.placeholder.com/100x100?text=无线耳机',
        price: 799,
        quantity: 2,
        attributes: '颜色：白色'
      }
    ]
    saveToLocalStorage()
  }
})
</script>