<template>
  <div class="bg-white rounded-lg shadow-md p-6 mb-8">
    <div class="flex flex-col md:flex-row">
      <!-- 商品图片轮播 -->
      <div class="md:w-1/2 mb-4 md:mb-0 md:pr-6">
        <div class="relative">
          <div class="overflow-hidden rounded-md h-80">
            <img 
              v-for="(img, index) in (product.images || [])" 
              :key="index"
              :src="img"
              :alt="`商品图片 ${index + 1}`"
              class="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-full h-full object-contain transition-opacity duration-500"
              :class="{ 'opacity-100': currentImageIndex === index, 'opacity-0': currentImageIndex !== index }"
            >
          </div>
          <!-- 图片指示器 -->
          <div v-if="(product.images || []).length > 1" class="absolute bottom-4 left-1/2 transform -translate-x-1/2 flex gap-2">
            <button 
              v-for="(img, index) in (product.images || [])" 
              :key="index"
              @click="currentImageIndex = index"
              class="w-3 h-3 rounded-full transition-all duration-300"
              :class="currentImageIndex === index ? 'bg-white w-8' : 'bg-white/50'"
            ></button>
          </div>
          <!-- 左右切换按钮 -->
          <button 
            v-if="(product.images || []).length > 1"
            @click="prevImage"
            class="absolute left-2 top-1/2 transform -translate-y-1/2 bg-black/30 hover:bg-black/50 text-white w-10 h-10 rounded-full flex items-center justify-center transition-colors"
          >
            ←
          </button>
          <button 
            v-if="(product.images || []).length > 1"
            @click="nextImage"
            class="absolute right-2 top-1/2 transform -translate-y-1/2 bg-black/30 hover:bg-black/50 text-white w-10 h-10 rounded-full flex items-center justify-center transition-colors"
          >
            →
          </button>
        </div>
      </div>
      
      <!-- 商品信息 -->
      <div class="md:w-1/2">
        <h2 class="text-2xl font-bold text-gray-900 mb-2">{{ product.name }}</h2>
        <div class="text-sm text-gray-500 mb-4">{{ product.category }}</div>
        
        <!-- 评分 -->
        <div class="flex items-center gap-2 mb-4">
          <div class="flex">
            <span v-for="i in 5" :key="i" class="text-yellow-400">★</span>
          </div>
          <div class="text-sm text-gray-500">({{ (product.reviews || []).length }}条评价)</div>
        </div>
        
        <!-- 价格 -->
        <div class="mb-4">
          <div class="text-3xl font-bold text-red-600">¥{{ product.price }}</div>
          <div class="text-sm text-gray-500 line-through">¥{{ product.original_price }}</div>
          <div class="text-xs text-green-600 mt-1">已优惠 ¥{{ product.original_price - product.price }}</div>
        </div>
        
        <!-- 商品属性 -->
        <div class="mb-6">
          <h3 class="text-sm font-semibold mb-2 text-gray-700">颜色</h3>
          <div class="flex gap-3 mb-4">
            <button 
              v-for="color in (product.colors || [])" 
              :key="color"
              @click="selectedColor = color"
              class="w-10 h-10 rounded-full border-2 transition-all hover:scale-110"
              :style="{ backgroundColor: color }"
              :class="selectedColor === color ? 'border-blue-500 ring-2 ring-offset-2 ring-blue-300' : 'border-gray-300'"
            ></button>
          </div>
          
          <h3 class="text-sm font-semibold mb-2 text-gray-700">尺寸</h3>
          <div class="flex flex-wrap gap-2">
            <button 
              v-for="size in (product.sizes || [])" 
              :key="size"
              @click="selectedSize = size"
              class="px-4 py-2 border rounded-md transition-colors"
              :class="selectedSize === size ? 'border-blue-500 bg-blue-50 text-blue-700' : 'border-gray-300 hover:border-gray-400'"
            >
              {{ size }}
            </button>
          </div>
        </div>
        
        <!-- 商品描述 -->
        <div class="mb-6">
          <h3 class="text-lg font-semibold mb-2">商品描述</h3>
          <p class="text-gray-600 leading-relaxed">{{ product.description }}</p>
        </div>
        
        <!-- 库存信息 -->
        <div class="mb-6">
          <div class="text-sm text-gray-500">
            库存: <span class="text-green-600 font-medium">{{ product.stock }}件</span>
          </div>
        </div>
        
        <!-- 购买按钮 -->
        <div class="flex gap-4">
          <button 
            @click="buyNow" 
            class="flex-1 bg-red-600 hover:bg-red-700 text-white font-bold py-3 px-4 rounded-md transition-colors transform hover:scale-105 active:scale-95"
          >
            立即购买
          </button>
          <button 
            @click="addToCart" 
            class="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold py-3 px-4 rounded-md transition-colors transform hover:scale-105 active:scale-95"
          >
            加入购物车
          </button>
        </div>
        
        <!-- 价格还价输入 -->
        <div class="mt-6">
          <h3 class="text-lg font-semibold mb-2">价格协商</h3>
          <div class="flex gap-2">
            <input 
              v-model.number="userOffer" 
              type="number" 
              placeholder="输入您的出价" 
              min="1"
              step="1"
              class="flex-1 border border-gray-300 rounded-md px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
            >
            <button 
              @click="negotiate" 
              class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-md transition-colors transform hover:scale-105 active:scale-95"
            >
              出价
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 商品评价 -->
    <div class="mt-12">
      <h3 class="text-2xl font-bold text-gray-900 mb-6">商品评价</h3>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div v-for="(review, index) in (product.reviews || [])" :key="index" class="border rounded-md p-4 hover:shadow-md transition-shadow">
          <div class="flex items-center justify-between mb-2">
            <div class="font-medium">{{ review.user_name }}</div>
            <div class="text-sm text-gray-500">{{ formatDate(review.date) }}</div>
          </div>
          <div class="flex text-yellow-400 mb-2">
            <span v-for="i in review.rating" :key="i">★</span>
            <span v-for="i in (5 - review.rating)" :key="i + 5">☆</span>
          </div>
          <div class="text-gray-600 text-sm">{{ review.content }}</div>
        </div>
      </div>
      <!-- 查看更多评价按钮 -->
      <div class="text-center mt-8">
        <button class="text-blue-600 hover:text-blue-800 font-medium flex items-center gap-1 mx-auto">
          <span>查看更多评价</span>
          <span>→</span>
        </button>
      </div>
    </div>
    
    <!-- AI客服组件 -->
    <ChatComponent :chat-context="{ type: 'product', productId: product.id }" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import ChatComponent from './ChatComponent.vue'
import supabase, { mockProducts } from '../supabase.js'

const props = defineProps({
  id: {
    type: [Number, String],
    default: 1
  }
})

const productId = computed(() => Number(props.id))

const product = ref({ ...mockProducts[0] })

const currentImageIndex = ref(0)
const selectedColor = ref('')
const selectedSize = ref('')
const userOffer = ref(0)
const loading = ref(false)
const error = ref(null)

function applyProduct(p) {
  product.value = p
  selectedColor.value = p.colors?.length > 0 ? p.colors[0] : ''
  selectedSize.value = p.sizes?.length > 0 ? p.sizes[0] : ''
}

async function fetchProductData() {
  loading.value = true
  error.value = null

  try {
    const { data, error: supabaseError } = await supabase
      .from('products')
      .select('*')
      .eq('id', productId.value)
      .single()

    if (data && !supabaseError) {
      applyProduct(data)
      return
    }
  } catch (err) {
    error.value = err.message
  }

  // Supabase 失败 → 使用 mock 数据兜底
  const fallback = mockProducts.find(p => p.id === productId.value)
  if (fallback) applyProduct(fallback)

  loading.value = false
}

function initProduct() {
  const local = mockProducts.find(p => p.id === productId.value)
  if (local) applyProduct(local)
}

onMounted(() => {
  initProduct()
  fetchProductData()
})

function prevImage() {
  const images = product.value.images
  if (!images || images.length === 0) return
  currentImageIndex.value = (currentImageIndex.value - 1 + images.length) % images.length
}

function nextImage() {
  const images = product.value.images
  if (!images || images.length === 0) return
  currentImageIndex.value = (currentImageIndex.value + 1) % images.length
}

function formatDate(dateString) {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

function buyNow() {
  if (!selectedColor.value || !selectedSize.value) {
    alert('请选择商品属性')
    return
  }
  alert(`立即购买：${product.value.name}，颜色：${selectedColor.value}，尺寸：${selectedSize.value}`)
}

function addToCart() {
  if (!selectedColor.value || !selectedSize.value) {
    alert('请选择商品属性')
    return
  }

  const cartItem = {
    id: product.value.id,
    name: product.value.name,
    image: product.value.images?.[0] || '',
    price: product.value.price,
    quantity: 1,
    attributes: `颜色：${selectedColor.value}，尺寸：${selectedSize.value}`
  }

  const savedCart = localStorage.getItem('cartItems')
  let cartItems = savedCart ? JSON.parse(savedCart) : []

  const existing = cartItems.findIndex(item =>
    item.id === cartItem.id && item.attributes === cartItem.attributes
  )

  if (existing !== -1) {
    cartItems[existing].quantity += 1
  } else {
    cartItems.push(cartItem)
  }

  localStorage.setItem('cartItems', JSON.stringify(cartItems))
  window.dispatchEvent(new Event('cart-updated'))
  alert(`已加入购物车：${product.value.name}`)
}

async function negotiate() {
  if (!userOffer.value || userOffer.value <= 0) {
    alert('请输入有效的出价')
    return
  }

  try {
    const response = await fetch('/api/negotiate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: 'test-user-1',
        userOffer: userOffer.value,
        productId: product.value.id
      })
    })

    const data = await response.json()
    window.dispatchEvent(new CustomEvent('negotiation-response', { detail: data }))
    userOffer.value = 0
  } catch (err) {
    console.error('价格协商失败:', err)
    alert('价格协商失败，请稍后重试')
  }
}
</script>
