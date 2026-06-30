# Petty - 宠物上门喂养服务平台

宠物上门喂养 O2O 平台，连接宠物主人与专业喂养师。

## 项目结构

```
Petty/
├── backend/          # Spring Boot 后端 API
├── admin-web/        # React 管理后台
├── mini-app/         # UniApp 用户端小程序
├── docs/             # 产品与业务文档
├── database/         # 数据库设计
└── openapi/          # API 规范
```

## 技术栈

| 模块 | 技术 |
|------|------|
| 后端 | Java 21 + Spring Boot 3.3 + MyBatis-Plus 3.5 |
| 数据库 | H2 (开发) / MySQL 8.0 (生产) |
| 管理后台 | React 18 + TypeScript + Vite + Tailwind CSS |
| 用户端 | UniApp + Vue3 + TypeScript + Pinia |
| 小程序目标 | 微信小程序 / H5 |

## 快速启动

### 前置条件

- Java 21 (推荐 OpenJDK)
- Node.js >= 18
- (可选) Docker + Docker Compose

### 1. 启动后端

```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw spring-boot:run
```

- 端口: `18080`
- H2 控制台: http://localhost:18080/h2-console (JDBC URL: `jdbc:h2:mem:petty`)
- API 入口: http://localhost:18080/api/v1/

> 使用 MySQL: 启动 Docker 后加参数 `--spring.profiles.active=mysql`

### 2. 启动管理后台

```bash
cd admin-web
npm install
npm run dev
```

- 端口: `3000`
- 自动代理 `/api` 到后端

### 3. 启动小程序端

**H5 调试模式:**
```bash
cd mini-app
npm install --legacy-peer-deps
npx uni
```
- 端口: `5173`

**微信小程序:**
```bash
npx uni -p mp-weixin
# 用微信开发者工具打开 dist/dev/mp-weixin
```

## 核心功能

### 已实现

- **JWT 认证**: 登录获取 token，所有接口通过 Bearer token 鉴权
- **订单全生命周期**: 下单 > 匹配 > 接单 > 打卡 > 服务 > 完成 > 确认
- **智能匹配引擎**: 距离(30%) + 评分(30%) + 完成率(20%) + 响应速度(20%)
- **GPS 打卡**: 到达 200m / 离开 500m 范围校验
- **支付模块**: 预授权冻结 > 确认扣款 > 退款（含阶梯取消费率）
- **评价模块**: 评分 / 图片 / 标签 / 匿名 / 联动惩罚
- **提现模块**: 最低 50 元 / 日限 1 次 / 佣金分级
- **取消规则**: 24h 前全额 / 2-24h 退 80% / 2h 内退 50%
- **乐观锁**: 防止订单状态并发修改
- **实体鉴权**: 宠物归属校验、喂养师身份验证

### 演示数据

启动后自动加载:
- 6 种服务类型 (上门喂养/遛狗/陪玩/洗护/喂药/寄养)
- 2 个宠物主人 (张小花 VIP / 李明)
- 3 只宠物 (小橘/豆豆/咪咪)
- 2 个喂养师 (王大勇/赵小美)

## API 示例

```bash
# 1. 登录获取 token
TOKEN=$(curl -s -X POST http://localhost:18080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800001111","role":"OWNER"}' | jq -r '.data.token')

# 2. 查看服务类型（公开接口，无需 token）
curl http://localhost:18080/api/v1/service-types

# 3. 创建订单
curl -X POST http://localhost:18080/api/v1/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"serviceTypeId":1,"petIds":[1],"scheduledDate":"2026-07-01","scheduledStartTime":"10:00","scheduledEndTime":"10:30","serviceAddress":"北京市朝阳区建国路88号","latitude":39.9087,"longitude":116.4716}'

# 4. 喂养师登录并接单
SITTER_TOKEN=$(curl -s -X POST http://localhost:18080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13900001111","role":"SITTER"}' | jq -r '.data.token')

curl -X POST http://localhost:18080/api/v1/orders/1/accept \
  -H "Authorization: Bearer $SITTER_TOKEN"

# 5. 到达打卡
curl -X POST http://localhost:18080/api/v1/orders/1/check-in \
  -H "Authorization: Bearer $SITTER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"latitude":39.9087,"longitude":116.4716,"photoUrl":"https://example.com/arrive.jpg"}'
```

## 开发规范

详见 [AGENTS.md](./AGENTS.md)

## Docker 部署

```bash
# 一键启动所有服务（后端 + 管理后台 + MySQL + Redis）
docker-compose up -d

# 后端: http://localhost:8080
# 管理后台: http://localhost:3000
# MySQL: localhost:3306 (petty/petty123)
```

生产部署注意事项：
- 修改 `PETTY_JWT_SECRET` 为强随机密钥
- 修改数据库密码
- CORS 需配置实际域名（修改 `WebConfig.java`）
- `petty.security.dev-bypass` 必须为 `false`

## 角色与认证

| 角色 | 用途 | 演示账号 |
|------|------|----------|
| OWNER | 宠物主人 | 13800001111 |
| SITTER | 喂养师 | 13900001111 |
| ADMIN | 管理后台 | 13800000000 |

```bash
# ADMIN 登录
ADMIN_TOKEN=$(curl -s -X POST http://localhost:18080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800000000","role":"ADMIN"}' | jq -r '.data.token')

# 管理端接口（需要 ADMIN token）
curl http://localhost:18080/api/v1/orders/all \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## License

MIT
