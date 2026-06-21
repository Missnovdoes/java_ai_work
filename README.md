# 智购 — AI 驱动的二手交易平台

> **智购**是一个基于 Vue 3 + Spring Boot 的二手商品交易平台，核心亮点是集成**豆包 AI 大模型**实现智能价格谈判与客服对话。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vue Router + Tailwind CSS + Vite |
| 后端 | Spring Boot 3.2 + Java 17 + WebFlux |
| 数据库 | Supabase (PostgreSQL) |
| AI | 豆包大模型 (Doubao API) |
| 部署 | Docker + GitHub Actions |

## 核心功能

- **AI 砍价助手** — 用户在商品详情页输入出价，AI 销售根据成本底线、利润目标和谈判轮数进行多轮智能议价，支持成交判定与最终报价
- **AI 客服对话** — 基于豆包 API 的智能客服，支持商品咨询、售后政策等常见问题解答（含关键词降级回复）
- **商品浏览** — 商品列表、详情展示
- **用户系统** — 注册 / 登录（Supabase Auth）
- **购物流程** — 购物车、结算
- **用户中心** — 个人信息管理

## 项目结构

```
├── frontend/                # Vue 3 前端
│   ├── src/
│   │   ├── views/           # 页面：首页、登录、注册、购物车、结算、客服、个人中心
│   │   ├── components/      # 组件：AI 砍价聊天、商品详情
│   │   ├── router/          # 路由配置
│   │   ├── auth.js          # 认证模块 (Supabase)
│   │   └── config.js        # 环境配置
│   ├── Dockerfile
│   └── nginx.conf
│
├── zhigou-backend/          # Spring Boot 后端
│   ├── src/main/java/com/zhigou/
│   │   ├── controller/      # NegotiateController、ProductController
│   │   ├── service/         # AiService（豆包调用）、NegotiationService（砍价逻辑）、SupabaseService
│   │   ├── model/           # DTO + Entity
│   │   ├── config/          # AI 配置、WebClient 配置
│   │   └── resources/
│   │       ├── prompts/     # AI Prompt 模板
│   │       └── application.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── .github/workflows/       # CI/CD 流水线
├── docker-compose.yml       # 一键部署
└── README.md
```

## 快速启动

### 环境要求

- JDK 17+
- Node.js 18+
- Docker（可选）

### 后端启动

```bash
cd zhigou-backend

# 设置环境变量
export SUPABASE_URL=https://xxx.supabase.co
export SUPABASE_KEY=your_service_role_key
export DOUBAO_API_KEY=your_doubao_api_key

# 启动
./mvnw spring-boot:run
# 后端运行在 http://localhost:5000
```

### 前端启动

```bash
cd frontend
npm install
npm run dev
# 前端运行在 http://localhost:5173
```

### Docker 一键部署

```bash
docker-compose up -d
# 前端 → 80 端口，后端 → 5000 端口
```

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/negotiate` | AI 价格谈判 |
| GET | `/api/products` | 商品列表 |
| GET | `/api/products/{id}` | 商品详情 |

### 谈判接口示例

```json
POST /api/negotiate
{
  "userId": "user-abc",
  "productId": 1,
  "userOffer": 85.00
}

Response:
{
  "response": "您的出价 85 元我收到了...",
  "price": 92.00,
  "accepted": false,
  "round": 2,
  "finalOffer": false
}
```

## 谈判策略设计

系统根据成本价、目标利润价和谈判轮数动态调整报价：

- **用户出价 ≥ 目标利润价** → 立即成交
- **用户出价 < 成本价** → 拒绝，坚守底线
- **达到最大轮数** → 给出折中一口价
- **正常让步** → 每轮按步长递减，营造"赢家感"

> 详细逻辑见 `NegotiationService.java`。
