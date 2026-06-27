import { createClient } from '@supabase/supabase-js'
import config from './config.js'

// 创建Supabase客户端或模拟客户端
let supabase;

if (config.useMock) {
  // 模拟Supabase客户端，用于演示和开发
  console.log('⚠️  使用模拟Supabase客户端，数据将不会持久化存储');
  
  // 模拟数据存储
  const mockProducts = [
    {
      id: 1,
      name: '智能手表 Pro',
      images: ['https://ts4.tc.mm.bing.net/th/id/OIP-C.VP82W90NG4jgee06tQemgQHaHa?rs=1&pid=ImgDetMain&o=7&rm=3', 'https://ts3.tc.mm.bing.net/th/id/OIP-C.Va6Kix185-0fUdv1GErmtwAAAA?rs=1&pid=ImgDetMain&o=7&rm=3', 'https://n.sinaimg.cn/sinacn10119/74/w2048h1226/20191118/fd91-iipztfe7943518.jpg'],
      price: 1299,
      original_price: 1599,
      cost_price: 900,
      description: '这款智能手表具有心率监测、睡眠追踪、防水等功能，采用高清触摸屏幕，支持多种运动模式，续航时间长达7天，是您健康生活的好帮手。',
      category: '智能设备',
      colors: ['#000000', '#FFFFFF', '#FF0000', '#00FF00', '#0000FF'],
      sizes: ['S', 'M', 'L', 'XL'],
      stock: 150,
      reviews: [],
      created_at: new Date().toISOString()
    },
    {
      id: 2,
      name: '智能手机 X',
      images: ['https://picsum.photos/id/1016/600/400', 'https://picsum.photos/id/1017/600/400', 'https://picsum.photos/id/1019/600/400'],
      price: 6999,
      original_price: 7999,
      cost_price: 4900,
      description: '旗舰级智能手机，强大性能，出色拍照体验，支持5G网络，大容量电池，让您畅享智能生活。',
      category: '智能设备',
      colors: ['#000000', '#FFFFFF', '#FF6B6B', '#4ECDC4'],
      sizes: ['64GB', '128GB', '256GB', '512GB'],
      stock: 200,
      reviews: [],
      created_at: new Date().toISOString()
    },
    {
      id: 3,
      name: '智能音箱',
      images: ['https://picsum.photos/id/1018/600/400', 'https://picsum.photos/id/1020/600/400', 'https://picsum.photos/id/1021/600/400'],
      price: 299,
      original_price: 399,
      cost_price: 200,
      description: '智能语音助手，支持多种智能家居控制，高品质音效，清晰语音识别，让您的家更智能。',
      category: '智能家居',
      colors: ['#000000', '#FFFFFF', '#F7DC6F', '#85C1E9'],
      sizes: ['标准版', '增强版'],
      stock: 300,
      reviews: [],
      created_at: new Date().toISOString()
    },
    {
      id: 4,
      name: '无线耳机',
      images: ['https://picsum.photos/id/1019/600/400', 'https://picsum.photos/id/1022/600/400', 'https://picsum.photos/id/1023/600/400'],
      price: 799,
      original_price: 999,
      cost_price: 550,
      description: '主动降噪，高清音质，舒适佩戴，长效续航，支持无线充电，让您享受沉浸式音乐体验。',
      category: '智能设备',
      colors: ['#000000', '#FFFFFF', '#9B59B6', '#3498DB'],
      sizes: ['均码'],
      stock: 250,
      reviews: [],
      created_at: new Date().toISOString()
    }
  ];

  const mockData = {
    products: mockProducts,
    chat_messages: [],
    users: []
  };
  
  // 模拟用户数据存储
  const mockUsers = [];
  const mockSessions = [];
  
  // 模拟Supabase客户端
  supabase = {
    from: (table) => {
      return {
        select: (columns) => {
          return {
            eq: (column, value) => {
              return {
                order: (column, options) => {
                  return new Promise((resolve) => {
                    let data = mockData[table] || [];
                    data = data.filter(item => item[column] === value);
                    if (options && options.ascending) {
                      data.sort((a, b) => new Date(a[column]) - new Date(b[column]));
                    }
                    resolve({ data, error: null });
                  });
                },
                single: () => {
                  return new Promise((resolve) => {
                    let data = mockData[table] || [];
                    data = data.filter(item => item[column] === value);
                    const singleData = data.length > 0 ? data[0] : null;
                    resolve({ data: singleData, error: singleData ? null : 'No data found' });
                  });
                }
              };
            }
          };
        },
        insert: (data) => {
          return new Promise((resolve) => {
            if (!mockData[table]) {
              mockData[table] = [];
            }
            mockData[table].push(data);
            resolve({ data, error: null });
          });
        },
        delete: () => {
          return {
            eq: (column, value) => {
              return new Promise((resolve) => {
                if (mockData[table]) {
                  mockData[table] = mockData[table].filter(item => item[column] !== value);
                }
                resolve({ data: null, error: null });
              });
            }
          };
        }
      };
    },
    // 模拟认证功能
    auth: {
      // 注册用户
      signUp: (credentials, options) => {
        return new Promise((resolve) => {
          const { email, password } = credentials;
          
          // 检查用户是否已存在
          const existingUser = mockUsers.find(user => user.email === email);
          if (existingUser) {
            resolve({ 
              user: null, 
              session: null, 
              error: { message: '用户已存在' } 
            });
            return;
          }
          
          // 创建新用户
          const newUser = {
            id: `user_${Date.now()}`,
            email,
            password, // 实际应用中应该加密存储
            created_at: new Date().toISOString(),
            data: options?.data || {}
          };
          
          mockUsers.push(newUser);
          
          // 创建会话
          const newSession = {
            user: newUser,
            access_token: `token_${Date.now()}`,
            refresh_token: `refresh_${Date.now()}`
          };
          
          mockSessions.push(newSession);
          
          resolve({ 
            user: newUser, 
            session: newSession, 
            error: null 
          });
        });
      },
      
      // 用户登录
      signInWithPassword: (credentials) => {
        return new Promise((resolve) => {
          const { email, password } = credentials;
          
          // 查找用户
          const existingUser = mockUsers.find(user => user.email === email && user.password === password);
          if (!existingUser) {
            resolve({ 
              user: null, 
              session: null, 
              error: { message: '邮箱或密码不正确' } 
            });
            return;
          }
          
          // 创建会话
          const newSession = {
            user: existingUser,
            access_token: `token_${Date.now()}`,
            refresh_token: `refresh_${Date.now()}`
          };
          
          mockSessions.push(newSession);
          
          resolve({ 
            user: existingUser, 
            session: newSession, 
            error: null 
          });
        });
      },
      
      // 用户登出
      signOut: () => {
        return new Promise((resolve) => {
          mockSessions.length = 0;
          resolve({ error: null });
        });
      },
      
      // 获取当前用户
      getUser: () => {
        return new Promise((resolve) => {
          const currentSession = mockSessions[mockSessions.length - 1];
          if (currentSession) {
            resolve({ user: currentSession.user, error: null });
          } else {
            resolve({ user: null, error: { message: '未登录' } });
          }
        });
      },
      
      // 监听认证状态变化
      onAuthStateChange: (callback) => {
        // 模拟状态变化
        setTimeout(() => {
          const currentSession = mockSessions[mockSessions.length - 1];
          callback('INITIAL_SESSION', currentSession);
        }, 0);
        
        return {
          data: { subscription: { unsubscribe: () => {} } }
        };
      }
    }
  };
} else {
  // 真实Supabase客户端
  supabase = createClient(config.supabase.url, config.supabase.key);
}

export const mockProducts = [
  {
    id: 1, name: '智能手表 Pro',
    images: ['https://ts4.tc.mm.bing.net/th/id/OIP-C.VP82W90NG4jgee06tQemgQHaHa?rs=1&pid=ImgDetMain&o=7&rm=3', 'https://ts3.tc.mm.bing.net/th/id/OIP-C.Va6Kix185-0fUdv1GErmtwAAAA?rs=1&pid=ImgDetMain&o=7&rm=3', 'https://n.sinaimg.cn/sinacn10119/74/w2048h1226/20191118/fd91-iipztfe7943518.jpg'],
    price: 1299, original_price: 1599, cost_price: 900,
    description: '这款智能手表具有心率监测、睡眠追踪、防水等功能，采用高清触摸屏幕，支持多种运动模式，续航时间长达7天，是您健康生活的好帮手。',
    category: '智能设备', colors: ['#000000', '#FFFFFF', '#FF0000', '#00FF00', '#0000FF'],
    sizes: ['S', 'M', 'L', 'XL'], stock: 150, reviews: []
  },
  {
    id: 2, name: '智能手机 X',
    images: ['https://picsum.photos/id/1016/600/400', 'https://picsum.photos/id/1017/600/400', 'https://picsum.photos/id/1019/600/400'],
    price: 6999, original_price: 7999, cost_price: 4900,
    description: '旗舰级智能手机，强大性能，出色拍照体验，支持5G网络，大容量电池，让您畅享智能生活。',
    category: '智能设备', colors: ['#000000', '#FFFFFF', '#FF6B6B', '#4ECDC4'],
    sizes: ['64GB', '128GB', '256GB', '512GB'], stock: 200, reviews: []
  },
  {
    id: 3, name: '智能音箱',
    images: ['https://picsum.photos/id/1018/600/400', 'https://picsum.photos/id/1020/600/400', 'https://picsum.photos/id/1021/600/400'],
    price: 299, original_price: 399, cost_price: 200,
    description: '智能语音助手，支持多种智能家居控制，高品质音效，清晰语音识别，让您的家更智能。',
    category: '智能家居', colors: ['#000000', '#FFFFFF', '#F7DC6F', '#85C1E9'],
    sizes: ['标准版', '增强版'], stock: 300, reviews: []
  },
  {
    id: 4, name: '无线耳机',
    images: ['https://picsum.photos/id/1019/600/400', 'https://picsum.photos/id/1022/600/400', 'https://picsum.photos/id/1023/600/400'],
    price: 799, original_price: 999, cost_price: 550,
    description: '主动降噪，高清音质，舒适佩戴，长效续航，支持无线充电，让您享受沉浸式音乐体验。',
    category: '智能设备', colors: ['#000000', '#FFFFFF', '#9B59B6', '#3498DB'],
    sizes: ['均码'], stock: 250, reviews: []
  }
];

export default supabase