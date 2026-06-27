// 应用配置 —— 所有敏感信息通过环境变量注入，详见 .env.example
const config = {
  doubao: {
    apiKey: import.meta.env.VITE_DOUBAO_API_KEY || '',
    endpoint: import.meta.env.VITE_DOUBAO_ENDPOINT || 'https://ark.cn-beijing.volces.com/api/v3/chat/completions',
    model: import.meta.env.VITE_DOUBAO_MODEL || 'doubao-seed-code-preview-251028',
    temperature: 0.7
  },

  supabase: {
    url: import.meta.env.VITE_SUPABASE_URL || 'https://your-project.supabase.co',
    key: import.meta.env.VITE_SUPABASE_KEY || ''
  },

  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || '',

  useMock: import.meta.env.VITE_USE_MOCK === 'true'
}

export default config
