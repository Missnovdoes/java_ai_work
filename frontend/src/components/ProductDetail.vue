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
import supabase from '../supabase.js'

// 接收路由参数
const props = defineProps({
  id: {
    type: [Number, String], // 支持数字和字符串类型的id
    default: 1
  }
})

// 确保id始终是数字类型
const productId = computed(() => Number(props.id))



// 本地商品数据，用于在Supabase获取失败时作为备用
const localProducts = [
  {
    id: 1,
    name: "智能手表 Pro",
    category: "智能设备",
    price: 1299,
    original_price: 1599,
    description: "这款智能手表具有心率监测、睡眠追踪、防水等功能，采用高清触摸屏幕，支持多种运动模式，续航时间长达7天，是您健康生活的好帮手。",
    images: [
      "https://ts4.tc.mm.bing.net/th/id/OIP-C.VP82W90NG4jgee06tQemgQHaHa?rs=1&pid=ImgDetMain&o=7&rm=3",
      "https://ts3.tc.mm.bing.net/th/id/OIP-C.Va6Kix185-0fUdv1GErmtwAAAA?rs=1&pid=ImgDetMain&o=7&rm=3",
      "https://n.sinaimg.cn/sinacn10119/74/w2048h1226/20191118/fd91-iipztfe7943518.jpg"
    ],
    colors: ["#000000", "#FFFFFF", "#FF0000", "#00FF00", "#0000FF"],
    sizes: ["S", "M", "L", "XL"],
    stock: 150,
    reviews: []
  },
  {
    id: 2,
    name: "智能手机 X",
    category: "智能设备",
    price: 6999,
    original_price: 7999,
    description: "旗舰级智能手机，强大性能，出色拍照体验，支持5G网络，大容量电池，让您畅享智能生活。",
    images: [
      "https://picsum.photos/id/1016/600/400",
      "https://picsum.photos/id/1017/600/400",
      "https://picsum.photos/id/1019/600/400"
    ],
    colors: ["#000000", "#FFFFFF", "#FF6B6B", "#4ECDC4"],
    sizes: ["64GB", "128GB", "256GB", "512GB"],
    stock: 200,
    reviews: []
  },
  {
    id: 3,
    name: "智能音箱",
    category: "智能家居",
    price: 299,
    original_price: 399,
    description: "智能语音助手，支持多种智能家居控制，高品质音效，清晰语音识别，让您的家更智能。",
    images: [
      "https://picsum.photos/id/1018/600/400",
      "https://picsum.photos/id/1020/600/400",
      "https://picsum.photos/id/1021/600/400"
    ],
    colors: ["#000000", "#FFFFFF", "#F7DC6F", "#85C1E9"],
    sizes: ["标准版", "增强版"],
    stock: 300,
    reviews: []
  },
  {
    id: 4,
    name: "无线耳机",
    category: "智能设备",
    price: 799,
    original_price: 999,
    description: "主动降噪，高清音质，舒适佩戴，长效续航，支持无线充电，让您享受沉浸式音乐体验。",
    images: [
      "https://picsum.photos/id/1019/600/400",
      "https://picsum.photos/id/1022/600/400",
      "https://picsum.photos/id/1023/600/400"
    ],
    colors: ["#000000", "#FFFFFF", "#9B59B6", "#3498DB"],
    sizes: ["均码"],
    stock: 250,
    reviews: []
  }
]

// 商品数据
const product = ref({
  id: 1,
  name: "智能手表 Pro",
  category: "智能设备",
  price: 1299,
  original_price: 1599,
  description: "这款智能手表具有心率监测、睡眠追踪、防水等功能，采用高清触摸屏幕，支持多种运动模式，续航时间长达7天，是您健康生活的好帮手。",
  images: [
    "https://ts4.tc.mm.bing.net/th/id/OIP-C.VP82W90NG4jgee06tQemgQHaHa?rs=1&pid=ImgDetMain&o=7&rm=3",
    "https://ts3.tc.mm.bing.net/th/id/OIP-C.Va6Kix185-0fUdv1GErmtwAAAA?rs=1&pid=ImgDetMain&o=7&rm=3",
    "https://n.sinaimg.cn/sinacn10119/74/w2048h1226/20191118/fd91-iipztfe7943518.jpg"
  ],
  colors: ["#000000", "#FFFFFF", "#FF0000", "#00FF00", "#0000FF"],
  sizes: ["S", "M", "L", "XL"],
  stock: 150,
  reviews: []
})

// 图片轮播状态
const currentImageIndex = ref(0)

// 商品属性选择
const selectedColor = ref(product.value.colors.length > 0 ? product.value.colors[0] : "")
const selectedSize = ref(product.value.sizes.length > 0 ? product.value.sizes[0] : "")

// 用户出价
const userOffer = ref(0)

// 加载状态
const loading = ref(false)
const error = ref(null)

// 从Supabase获取商品数据
async function fetchProductData() {
  loading.value = true
  error.value = null
  
  try {
    // 尝试从Supabase获取商品数据
    const { data, error: supabaseError } = await supabase
      .from('products')
      .select('*')
      .eq('id', productId.value)
      .single()
    
    if (supabaseError) {
      console.warn('从Supabase获取数据失败，使用本地商品数据:', supabaseError)
      // 如果获取失败，从本地商品数据中查找
      const localProduct = localProducts.find(p => p.id === productId.value)
      if (localProduct) {
        product.value = localProduct
        // 设置默认选中的属性
        if (product.value.colors && product.value.colors.length > 0) {
          selectedColor.value = product.value.colors[0]
        }
        if (product.value.sizes && product.value.sizes.length > 0) {
          selectedSize.value = product.value.sizes[0]
        }
      } else {
        console.warn('本地商品数据中未找到ID为', props.id, '的商品')
      }
      return
    }
    
    if (data) {
      // 使用从Supabase获取的数据
      product.value = data
      // 设置默认选中的属性
      if (product.value.colors && product.value.colors.length > 0) {
        selectedColor.value = product.value.colors[0]
      }
      if (product.value.sizes && product.value.sizes.length > 0) {
        selectedSize.value = product.value.sizes[0]
      }
      
      // reviews已经包含在products表中，无需单独查询
    }
  } catch (err) {
    console.error('获取商品数据时发生错误:', err)
    error.value = err.message
    
    // 发生异常时，从本地商品数据中查找
    const localProduct = localProducts.find(p => p.id === productId.value)
    if (localProduct) {
      product.value = localProduct
      // 设置默认选中的属性
      if (product.value.colors && product.value.colors.length > 0) {
        selectedColor.value = product.value.colors[0]
      }
      if (product.value.sizes && product.value.sizes.length > 0) {
        selectedSize.value = product.value.sizes[0]
      }
    }
  } finally {
    loading.value = false
  }
}

// 初始化商品数据（立即显示本地数据）
function initializeProductData() {
  const localProduct = localProducts.find(p => p.id === productId.value)
  if (localProduct) {
    product.value = localProduct
    // 确保在访问数组元素之前进行空值检查
    selectedColor.value = Array.isArray(product.value.colors) && product.value.colors.length > 0 ? product.value.colors[0] : ""
    selectedSize.value = Array.isArray(product.value.sizes) && product.value.sizes.length > 0 ? product.value.sizes[0] : ""
  }
}

// 组件挂载时获取商品数据
onMounted(() => {
  initializeProductData() // 立即显示本地数据
  fetchProductData() // 尝试从Supabase获取最新数据
})

// 图片切换功能
function prevImage() {
  if (!product.value.images || product.value.images.length === 0) return
  currentImageIndex.value = (currentImageIndex.value - 1 + product.value.images.length) % product.value.images.length
}

function nextImage() {
  if (!product.value.images || product.value.images.length === 0) return
  currentImageIndex.value = (currentImageIndex.value + 1) % product.value.images.length
}

// 格式化日期
function formatDate(dateString) {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

// 购买功能
function buyNow() {
  if (!selectedColor.value || !selectedSize.value) {
    alert('请选择商品属性')
    return
  }
  alert(`立即购买：${product.value.name}，颜色：${selectedColor.value}，尺寸：${selectedSize.value}`)
}

// 加入购物车
function addToCart() {
  if (!selectedColor.value || !selectedSize.value) {
    alert('请选择商品属性')
    return
  }
  
  // 创建商品对象
  const cartItem = {
    id: product.value.id,
    name: product.value.name,
    image: product.value.images ? product.value.images[0] : '',
    price: product.value.price,
    quantity: 1,
    attributes: `颜色：${selectedColor.value}，尺寸：${selectedSize.value}`
  }
  
  // 从localStorage获取现有购物车数据
  const savedCart = localStorage.getItem('cartItems')
  let cartItems = savedCart ? JSON.parse(savedCart) : []
  
  // 检查商品是否已存在于购物车中
  const existingItemIndex = cartItems.findIndex(item => 
    item.id === cartItem.id && item.attributes === cartItem.attributes
  )
  
  if (existingItemIndex !== -1) {
    // 如果商品已存在，增加数量
    cartItems[existingItemIndex].quantity += 1
  } else {
    // 如果商品不存在，添加到购物车
    cartItems.push(cartItem)
  }
  
  // 将更新后的购物车数据保存回localStorage
  localStorage.setItem('cartItems', JSON.stringify(cartItems))
  
  // 显示成功提示
  alert(`已加入购物车：${product.value.name}，颜色：${selectedColor.value}，尺寸：${selectedSize.value}`)
}

// 价格协商
async function negotiate() {
  if (!userOffer.value || userOffer.value <= 0) {
    alert('请输入有效的出价')
    return
  }
  
  try {
    const response = await fetch('/api/negotiate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        user_id: 'test-user-1', // 实际应用中应从认证获取
        user_offer: userOffer.value,
        product_id: product.value.id
      })
    })
    
    const data = await response.json()
    // 将还价结果发送到聊天组件
    window.dispatchEvent(new CustomEvent('negotiation-response', { detail: data }))
    
    userOffer.value = 0 // 清空输入框
  } catch (error) {
    console.error('价格协商失败:', error)
    alert('价格协商失败，请稍后重试')
  }
}
</script>
