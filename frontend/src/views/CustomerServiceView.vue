<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-3xl font-bold mb-8">客服中心</h1>
    
    <div class="bg-white rounded-lg shadow-md p-6">
      <h2 class="text-xl font-semibold mb-6">在线客服</h2>
      <p class="text-gray-600 mb-6">
        您好！我是智能客服助手，有什么可以帮助您的吗？
        <br>
        您可以咨询订单问题、商品信息、退换货政策等。
      </p>
      
      <!-- 引入现有的AI聊天组件 -->
      <ChatComponent :chat-context="{ type: 'customer_service', productId: null }" />
    </div>
    
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mt-8">
      <div class="bg-white rounded-lg shadow-md p-6">
        <div class="text-2xl mb-3">📞</div>
        <h3 class="text-lg font-semibold mb-2">电话客服</h3>
        <p class="text-gray-600">400-123-4567</p>
        <p class="text-sm text-gray-500 mt-1">周一至周日 9:00-21:00</p>
      </div>
      
      <div class="bg-white rounded-lg shadow-md p-6">
        <div class="text-2xl mb-3">📧</div>
        <h3 class="text-lg font-semibold mb-2">邮箱客服</h3>
        <p class="text-gray-600">service@example.com</p>
        <p class="text-sm text-gray-500 mt-1">24小时内回复</p>
      </div>
      
      <div class="bg-white rounded-lg shadow-md p-6">
        <div class="text-2xl mb-3">🕒</div>
        <h3 class="text-lg font-semibold mb-2">工作时间</h3>
        <p class="text-gray-600">在线客服：全天24小时</p>
        <p class="text-sm text-gray-500 mt-1">人工客服：9:00-21:00</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import ChatComponent from '../components/ChatComponent.vue'
</script>

<style scoped>
/* 可以根据需要添加额外样式 */
</style>