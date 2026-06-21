<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <!-- 欢迎横幅 -->
    <div class="bg-gradient-to-r from-blue-600 to-purple-600 rounded-lg shadow-md p-8 text-white mb-8">
      <h1 class="text-3xl font-bold mb-2">欢迎来到二手交易平台</h1>
      <p class="text-lg opacity-90">发现优质二手商品，享受便捷交易体验</p>
    </div>

    <!-- 商品分类 -->
    <div class="mb-12">
      <h2 class="text-2xl font-bold mb-6">商品分类</h2>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-6">
        <div v-for="category in categories" :key="category.id" class="bg-white rounded-lg shadow-md p-6 text-center hover:shadow-lg transition-shadow">
          <div class="text-4xl mb-3">{{ category.icon }}</div>
          <h3 class="text-lg font-semibold mb-2">{{ category.name }}</h3>
          <p class="text-gray-500 text-sm">{{ category.description }}</p>
        </div>
      </div>
    </div>

    <!-- 热门商品 -->
    <div class="mb-12">
      <h2 class="text-2xl font-bold mb-6">热门商品</h2>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <div v-for="product in popularProducts" :key="product.id" class="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow">
          <div class="h-48 overflow-hidden">
            <img :src="product.image" :alt="product.name" class="w-full h-full object-cover hover:scale-105 transition-transform">
          </div>
          <div class="p-4">
            <h3 class="text-lg font-semibold mb-2">{{ product.name }}</h3>
            <p class="text-gray-600 text-sm mb-3 line-clamp-2">{{ product.description }}</p>
            <div class="flex items-center justify-between">
              <span class="text-lg font-bold text-red-500">¥{{ product.price }}</span>
              <router-link :to="`/product/${product.id}`" class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm transition-colors">
                查看详情
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 优惠活动 -->
    <div>
      <h2 class="text-2xl font-bold mb-6">优惠活动</h2>
      <div class="bg-white rounded-lg shadow-md p-6">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div v-for="promotion in promotions" :key="promotion.id" class="border border-gray-200 rounded-lg p-4 hover:border-blue-500 transition-colors">
            <div class="flex items-start">
              <div class="text-3xl mr-4">{{ promotion.icon }}</div>
              <div>
                <h3 class="text-lg font-semibold mb-2">{{ promotion.title }}</h3>
                <p class="text-gray-600 text-sm mb-3">{{ promotion.description }}</p>
                <span class="text-blue-600 font-medium">{{ promotion.validity }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// 模拟数据
const categories = [
  { id: 1, name: '智能手表', icon: '⌚', description: '最新款智能手表' },
  { id: 2, name: '智能手机', icon: '📱', description: '高端智能手机' },
  { id: 3, name: '智能家居', icon: '🏠', description: '智能生活设备' },
  { id: 4, name: '智能耳机', icon: '🎧', description: '无线蓝牙耳机' }
]

const popularProducts = [
  { id: 1, name: '智能手表 Pro', image: 'https://picsum.photos/id/1015/400/300', price: 1999, description: '全新一代智能手表，支持健康监测、运动追踪等多种功能' },
  { id: 2, name: '智能手机 X', image: 'https://picsum.photos/id/1016/400/300', price: 6999, description: '旗舰级智能手机，强大性能，出色拍照体验' },
  { id: 3, name: '智能音箱', image: 'https://picsum.photos/id/1018/400/300', price: 299, description: '智能语音助手，支持多种智能家居控制' },
  { id: 4, name: '无线耳机', image: 'https://picsum.photos/id/1019/400/300', price: 799, description: '主动降噪，高清音质，舒适佩戴' }
]

const promotions = [
  { id: 1, title: '新用户优惠', icon: '🎁', description: '注册即送50元优惠券', validity: '有效期至2023-12-31' },
  { id: 2, title: '满减活动', icon: '💰', description: '满1000减100，满2000减250', validity: '长期有效' }
]
</script>