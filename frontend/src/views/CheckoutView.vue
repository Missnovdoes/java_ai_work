<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-3xl font-bold mb-8">结账</h1>
    
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- 结账表单 -->
      <div class="lg:col-span-2">
        <div class="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 class="text-xl font-semibold mb-4">收货信息</h2>
          <form @submit.prevent="submitOrder">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
              <div>
                <label for="name" class="block text-sm font-medium text-gray-700 mb-1">姓名</label>
                <input type="text" id="name" v-model="shippingInfo.name" required class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500">
              </div>
              <div>
                <label for="phone" class="block text-sm font-medium text-gray-700 mb-1">手机号码</label>
                <input type="tel" id="phone" v-model="shippingInfo.phone" required class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500">
              </div>
            </div>
            
            <div class="mb-6">
              <label for="address" class="block text-sm font-medium text-gray-700 mb-1">详细地址</label>
              <input type="text" id="address" v-model="shippingInfo.address" required class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500">
            </div>
            
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
              <div>
                <label for="province" class="block text-sm font-medium text-gray-700 mb-1">省份</label>
                <select id="province" v-model="shippingInfo.province" required class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500">
                  <option value="">请选择</option>
                  <option value="北京">北京</option>
                  <option value="上海">上海</option>
                  <option value="广东">广东</option>
                  <option value="浙江">浙江</option>
                </select>
              </div>
              <div>
                <label for="city" class="block text-sm font-medium text-gray-700 mb-1">城市</label>
                <input type="text" id="city" v-model="shippingInfo.city" required class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500">
              </div>
              <div>
                <label for="zipCode" class="block text-sm font-medium text-gray-700 mb-1">邮政编码</label>
                <input type="text" id="zipCode" v-model="shippingInfo.zipCode" required class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500">
              </div>
            </div>
            
            <div class="mb-6">
              <label for="notes" class="block text-sm font-medium text-gray-700 mb-1">备注</label>
              <textarea id="notes" v-model="shippingInfo.notes" rows="3" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"></textarea>
            </div>
          </form>
        </div>
        
        <div class="bg-white rounded-lg shadow-md p-6">
          <h2 class="text-xl font-semibold mb-4">支付方式</h2>
          <div class="space-y-4">
            <label class="flex items-center p-4 border border-gray-300 rounded-lg cursor-pointer hover:border-blue-500 transition-colors">
              <input type="radio" name="payment" value="alipay" v-model="paymentMethod" class="mr-4">
              <div class="flex items-center">
                <span class="text-2xl mr-3">🛒</span>
                <span class="font-medium">支付宝</span>
              </div>
            </label>
            <label class="flex items-center p-4 border border-gray-300 rounded-lg cursor-pointer hover:border-blue-500 transition-colors">
              <input type="radio" name="payment" value="wechat" v-model="paymentMethod" class="mr-4">
              <div class="flex items-center">
                <span class="text-2xl mr-3">💳</span>
                <span class="font-medium">微信支付</span>
              </div>
            </label>
            <label class="flex items-center p-4 border border-gray-300 rounded-lg cursor-pointer hover:border-blue-500 transition-colors">
              <input type="radio" name="payment" value="card" v-model="paymentMethod" class="mr-4">
              <div class="flex items-center">
                <span class="text-2xl mr-3">💎</span>
                <span class="font-medium">银行卡支付</span>
              </div>
            </label>
          </div>
        </div>
      </div>
      
      <!-- 订单信息 -->
      <div class="lg:col-span-1">
        <div class="bg-white rounded-lg shadow-md p-6">
          <h2 class="text-xl font-semibold mb-6">订单信息</h2>
          
          <div class="space-y-4 mb-6">
            <div v-for="item in orderItems" :key="item.id" class="flex justify-between">
              <div class="flex items-center">
                <img :src="item.image" :alt="item.name" class="w-12 h-12 object-cover mr-3">
                <div>
                  <div class="text-sm font-medium text-gray-900">{{ item.name }}</div>
                  <div class="text-xs text-gray-500">{{ item.attributes }}</div>
                </div>
              </div>
              <div class="text-sm font-medium text-gray-900">¥{{ item.price }} × {{ item.quantity }}</div>
            </div>
          </div>
          
          <div class="border-t border-gray-200 pt-4 mb-4">
            <div class="flex justify-between mb-2">
              <span class="text-gray-600">商品总额</span>
              <span class="text-gray-900">¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            <div class="flex justify-between mb-2">
              <span class="text-gray-600">运费</span>
              <span class="text-gray-900">¥{{ shippingFee.toFixed(2) }}</span>
            </div>
            <div class="flex justify-between mb-2">
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
          
          <button @click="submitOrder" class="block w-full bg-blue-600 hover:bg-blue-700 text-white py-3 px-4 rounded-lg text-center font-medium transition-colors" :disabled="isSubmitting">
            {{ isSubmitting ? '提交中...' : '提交订单' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 收货信息
const shippingInfo = ref({
  name: '',
  phone: '',
  address: '',
  province: '',
  city: '',
  zipCode: '',
  notes: ''
})

// 支付方式
const paymentMethod = ref('alipay')

// 订单商品
const orderItems = ref([
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
])

// 计算属性
const totalPrice = computed(() => {
  return orderItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
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

// 提交状态
const isSubmitting = ref(false)

// 提交订单
const submitOrder = () => {
  isSubmitting.value = true
  
  // 模拟提交订单
  setTimeout(() => {
    isSubmitting.value = false
    alert('订单提交成功！')
    router.push('/profile')
  }, 1500)
}
</script>