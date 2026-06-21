<template>
  <div class="bg-white rounded-lg shadow-md p-6">
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-xl font-bold text-gray-900">
        {{ chatContext.type === 'product' ? `AI 砍价助手 — 商品 #${chatContext.productId}` : 'AI 客服助手' }}
      </h3>
      <button @click="clearChat" class="text-sm text-gray-500 hover:text-red-500">清空记录</button>
    </div>

    <div ref="chatContainer" class="h-96 overflow-y-auto mb-4 p-4 bg-gray-50 rounded-md space-y-4">
      <div v-if="messages.length === 0" class="text-center text-gray-400 py-16">
        输入你的出价，和 AI 销售开始砍价吧
      </div>

      <div v-for="(msg, i) in messages" :key="i"
        class="flex items-end" :class="msg.type === 'user' ? 'justify-end' : 'justify-start'">
        <div class="w-8 h-8 rounded-full flex items-center justify-center text-sm mr-2"
          :class="msg.type === 'user' ? 'order-2 ml-2 bg-blue-500 text-white' : 'bg-gray-200'">
          {{ msg.type === 'user' ? '我' : 'AI' }}
        </div>
        <div class="max-w-[75%] p-3 rounded-lg"
          :class="msg.type === 'user' ? 'bg-blue-500 text-white' : 'bg-white border'">
          <div class="text-sm whitespace-pre-wrap">{{ msg.content }}</div>
          <div v-if="msg.price" class="mt-2 text-xs">
            <span class="px-2 py-1 rounded" :class="msg.type === 'user' ? 'bg-blue-600' : 'bg-green-100 text-green-800'">
              {{ msg.type === 'user' ? '我的出价' : 'AI 报价' }}: ¥{{ msg.price }}
            </span>
            <span v-if="msg.accepted" class="ml-2 px-2 py-1 bg-green-500 text-white rounded text-xs">已成交</span>
            <span v-if="msg.finalOffer" class="ml-2 px-2 py-1 bg-orange-500 text-white rounded text-xs">最终报价</span>
          </div>
          <div class="text-xs mt-1 opacity-50 text-right">{{ formatTime(msg.timestamp) }}</div>
        </div>
      </div>

      <div v-if="isTyping" class="flex items-end">
        <div class="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center text-sm mr-2">AI</div>
        <div class="bg-white border rounded-lg p-3">
          <div class="flex gap-1">
            <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0ms"></div>
            <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 150ms"></div>
            <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 300ms"></div>
          </div>
        </div>
      </div>
    </div>

    <div class="flex gap-2">
      <input v-model="inputMessage" type="text" placeholder="输入出价或消息..."
        class="flex-1 border border-gray-300 rounded-md px-4 py-3 focus:outline-none focus:ring-2 focus:ring-blue-500"
        @keyup.enter="sendMessage" :disabled="isTyping" />
      <button @click="sendMessage"
        class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-md disabled:opacity-50"
        :disabled="isTyping || !inputMessage.trim()">
        {{ isTyping ? '发送中' : '发送' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import config from '../config.js'

const props = defineProps({
  chatContext: { type: Object, default: () => ({ type: 'customer_service', productId: null }) }
})

const chatContainer = ref(null)
const messages = ref([])
const inputMessage = ref('')
const isTyping = ref(false)
const isMounted = ref(true)

const userId = 'user-' + Date.now().toString(36)

function formatTime(date) {
  return new Date(date).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function scrollToBottom() {
  nextTick(() => {
    if (isMounted.value && chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

async function sendMessage() {
  if (isTyping.value || !inputMessage.value.trim()) return
  const content = inputMessage.value.trim()

  const userMsg = { type: 'user', content, timestamp: new Date() }
  messages.value.push(userMsg)
  inputMessage.value = ''
  scrollToBottom()
  isTyping.value = true

  try {
    const priceMatch = content.match(/\d+(\.\d+)?/)
    const userOffer = priceMatch ? parseFloat(priceMatch[0]) : null

    if (userOffer && props.chatContext.type === 'product') {
      // 包含数字且在商品页 → 走价格谈判接口
      await handleNegotiation(userOffer)
    } else {
      // 普通对话 → 走通用 AI 接口
      await handleGeneralChat(content)
    }
  } catch (e) {
    messages.value.push({
      type: 'agent', content: '抱歉，暂时无法连接服务器，请稍后重试。',
      timestamp: new Date(), isError: true
    })
  } finally {
    isTyping.value = false
    scrollToBottom()
  }
}

async function handleNegotiation(userOffer) {
  const res = await fetch(`${config.apiBaseUrl}/api/negotiate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      userId: userId,
      productId: Number(props.chatContext.productId),
      userOffer: userOffer
    })
  })

  if (!res.ok) throw new Error('Negotiation failed')
  const data = await res.json()

  let content = data.response || ''
  if (data.accepted) content = '🎉 ' + content
  if (data.finalOffer) content = '📌 ' + content

  messages.value.push({
    type: 'agent', content,
    price: data.price, accepted: data.accepted,
    finalOffer: data.finalOffer, timestamp: new Date()
  })
}

async function handleGeneralChat(content) {
  // 通用对话：如果有豆包 key 就走 AI，否则走简单关键词匹配
  if (config.doubao.apiKey) {
    try {
      const res = await fetch(config.doubao.endpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${config.doubao.apiKey}`
        },
        body: JSON.stringify({
          model: config.doubao.model,
          messages: [
            { role: 'system', content: '你是二手交易平台的客服助手，回答简洁专业。' },
            { role: 'user', content }
          ],
          temperature: config.doubao.temperature
        })
      })
      const data = await res.json()
      const reply = data.choices?.[0]?.message?.content || fallbackReply(content)
      messages.value.push({ type: 'agent', content: reply, timestamp: new Date() })
      return
    } catch (e) {
      // API 失败走关键词匹配
    }
  }
  messages.value.push({ type: 'agent', content: fallbackReply(content), timestamp: new Date() })
}

function fallbackReply(msg) {
  const m = msg.toLowerCase()
  if (m.includes('价格') || m.includes('多少钱')) return '价格可以商量，直接输入你的心理价位，我们开始砍价吧！'
  if (m.includes('功能') || m.includes('特点')) return '商品详情请查看上方描述，有具体问题可以问我。'
  if (m.includes('保修') || m.includes('售后')) return '我们提供 3 个月质保和 7 天无理由退换货服务。'
  if (m.includes('发货') || m.includes('快递')) return '付款后 24 小时内发货，默认顺丰快递。'
  if (m.includes('支付')) return '支持微信支付、支付宝和银行卡支付。'
  return '感谢咨询！有任何问题都可以问我，也可以直接出价开始砍价哦。'
}

function clearChat() {
  messages.value = []
}

onMounted(() => scrollToBottom())
onUnmounted(() => { isMounted.value = false })
</script>
