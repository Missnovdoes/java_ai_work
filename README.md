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
- Node.js 18+（仅修改前端时需要）
- Docker（可选）

### IDEA 一键启动

1. 用 IDEA 打开 `zhigou-backend/pom.xml` 作为 Maven 项目
2. 运行 `ZhigouApplication.java` 的 main 方法
3. 浏览器访问 **http://localhost:5000**

前端已集成在 `resources/static/` 中，Spring Boot 直接 serve，无需额外启动前端。

### 前端开发（修改前端时）

```bash
cd frontend
npm install
npm run dev           # 开发模式 http://localhost:3000

# 修改完成后构建并同步到后端
npm run build
cp -r dist/* ../zhigou-backend/src/main/resources/static/
```

### Docker 一键部署

```bash
cd zhigou-backend
mvn package -DskipTests
docker-compose up -d
# 访问 http://localhost
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
