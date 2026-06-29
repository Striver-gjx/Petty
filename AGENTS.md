# AGENTS

所有 AI Agent 开发前必须阅读本文件。

---

## Architecture

```
Controller → Application Service → Domain Service → Repository → Database
```

禁止：Controller 直接调用 Mapper/Repository

### 分层职责

| 层 | 职责 | 示例 |
|---|---|---|
| Controller | 参数校验、路由、权限 | `OrderController` |
| Application Service | 业务编排、事务 | `OrderAppService`（组合多个 Domain Service） |
| Domain Service | 核心业务逻辑 | `MatchingService`（匹配算法） |
| Repository/Mapper | 数据访问 | `OrderMapper`（MyBatis-Plus） |

---

## Project Structure

```
Petty/
├── backend/                              # Java 后端服务
│   ├── src/main/java/com/petty/
│   │   ├── common/                       # 公共模块
│   │   │   ├── config/                   # 配置类
│   │   │   ├── exception/                # 异常定义
│   │   │   ├── result/                   # 统一返回
│   │   │   └── util/                     # 工具类
│   │   ├── controller/                   # API 控制器
│   │   ├── dto/                          # 数据传输对象
│   │   ├── entity/                       # 数据库实体
│   │   ├── mapper/                       # MyBatis Mapper
│   │   ├── service/                      # 业务逻辑层
│   │   │   └── impl/
│   │   └── vo/                           # 视图对象
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── mapper/                       # XML Mapper
│   │   └── schema.sql                    # H2 兼容 Schema
│   └── src/test/
├── mini-app/                             # 微信小程序（宠物主人+喂养师）
│   ├── src/
│   │   ├── pages/                        # 页面
│   │   │   ├── owner/                    # 主人端页面
│   │   │   ├── sitter/                   # 喂养师端页面
│   │   │   └── common/                   # 共用页面
│   │   ├── components/                   # 组件
│   │   ├── api/                          # API 请求
│   │   ├── store/                        # 状态管理
│   │   └── utils/                        # 工具
│   └── uni.config.ts
├── admin-web/                            # 管理后台（React）
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── api/
│   │   └── test/
│   └── vite.config.ts
├── database/                             # 数据库版本管理
├── openapi/                              # API 规范文档
├── docs/                                 # 业务文档
└── scripts/                              # 构建/部署脚本
```

---

## Code Style

### Java (Backend)

- **JDK**: 21
- **框架**: Spring Boot 3.x
- **ORM**: MyBatis-Plus 3.5+
- **工具**: Lombok + MapStruct
- **日志**: SLF4J（禁止 `System.out.println`）
- **文档**: 方法注释使用 JavaDoc 格式
- **包管理**: Maven

#### 依赖选型

| 功能 | 选型 |
|------|------|
| Web | Spring Boot Starter Web |
| 数据库 | MySQL 8 + H2（测试） |
| ORM | MyBatis-Plus |
| 缓存 | Spring Cache + Redis |
| 消息 | Spring AMQP (RabbitMQ) |
| 对象存储 | 阿里云 OSS SDK |
| 地图/距离 | 高德 Web Service API |
| 支付 | 微信支付 V3 SDK |
| 认证 | JWT + 微信小程序登录 |
| 参数校验 | Jakarta Validation |
| JSON | Jackson |
| 测试 | JUnit5 + Mockito + SpringBootTest |

### TypeScript (小程序端)

- **框架**: UniApp + Vue3 Composition API
- **语言**: TypeScript（严格模式，禁止 `any`）
- **组件**: `<script setup>` 语法
- **状态管理**: Pinia
- **请求**: uni.request 封装 + 拦截器
- **样式**: SCSS + 响应式 rpx

### TypeScript (管理后台)

- **框架**: React 18 + TypeScript
- **构建**: Vite
- **样式**: Tailwind CSS
- **图表**: Recharts
- **请求**: Axios
- **测试**: Vitest + React Testing Library

---

## API Convention

### 统一返回格式

```java
Result<T> {
    int code;       // 200=成功, 400=业务异常, 401=未认证, 403=无权限, 500=系统错误
    String message;
    T data;
}
```

### 统一异常

```java
BusinessException(String message)         // 默认 code=400
BusinessException(int code, String message)
```

### 统一分页

```java
PageResult<T> {
    List<T> list;
    long total;
    int page;
    int size;
}
```

### 命名规范

| 后缀 | 用途 | 示例 |
|------|------|------|
| DTO | 请求参数对象 | `OrderCreateDTO` |
| VO | 返回前端对象 | `OrderVO` |
| Entity | 数据库实体 | `ServiceOrder` |
| Query | 复杂查询参数 | `OrderQuery` |

### 接口路径规范

```
/api/v1/{模块}/{资源}
```

- 列表：`GET /api/v1/orders`
- 详情：`GET /api/v1/orders/{id}`
- 创建：`POST /api/v1/orders`
- 更新：`PUT /api/v1/orders/{id}`
- 删除：`DELETE /api/v1/orders/{id}`
- 行为：`POST /api/v1/orders/{id}/accept`

### 错误码定义

| 范围 | 模块 |
|------|------|
| 1xxx | 通用（认证、参数） |
| 2xxx | 订单模块 |
| 3xxx | 匹配模块 |
| 4xxx | 支付模块 |
| 5xxx | 评价模块 |
| 6xxx | 喂养师模块 |

---

## Authentication

### 认证流程

```
小程序 wx.login() → code → 后端 /auth/wechat-login → 返回 JWT
后续请求携带 Header: Authorization: Bearer {token}
```

### 角色权限

| 角色 | 标识 | 可访问接口前缀 |
|------|------|---------------|
| 宠物主人 | OWNER | `/api/v1/owners/**`, `/api/v1/orders/**`, `/api/v1/pets/**` |
| 喂养师 | SITTER | `/api/v1/sitters/**`, `/api/v1/orders/**`（接单视角） |
| 管理员 | ADMIN | `/api/v1/admin/**` |

---

## Data Storage

### 数据库

- **主库**: MySQL 8.0 (utf8mb4)
- **测试**: H2 内存数据库
- **连接池**: HikariCP

### 缓存（Redis）

| Key 模式 | 用途 | TTL |
|----------|------|-----|
| `sitter:nearby:{geohash}` | 附近喂养师缓存 | 5 min |
| `order:lock:{id}` | 订单操作分布式锁 | 30 sec |
| `user:token:{userId}` | 用户 Session | 7 days |
| `sitter:schedule:{id}:{date}` | 排班快照 | 1 hour |

### 文件存储（OSS）

| 路径模式 | 内容 |
|----------|------|
| `petty/avatar/{userId}/{filename}` | 用户/宠物头像 |
| `petty/service/{orderId}/{timestamp}_{type}.jpg` | 服务打卡照片 |
| `petty/cert/{sitterId}/{certName}.jpg` | 资质证书 |

---

## Error Handling

每个模块必须具备：
- 完整的异常捕捉机制（`@ControllerAdvice`）
- 统一错误码定义（见 API Convention）
- 错误日志记录（ERROR 级别 + 堆栈）
- 单元测试覆盖异常分支

### 全局异常处理器

```java
@ExceptionHandler(BusinessException.class)    → Result(code, message)
@ExceptionHandler(MethodArgumentNotValidException.class) → Result(400, "参数校验失败: {details}")
@ExceptionHandler(Exception.class)            → Result(500, "系统异常") + log.error(...)
```

---

## Testing

### 测试分层

| 层级 | 工具 | 覆盖目标 |
|------|------|----------|
| Service 集成测试 | @SpringBootTest + H2 | 业务逻辑正确性 |
| Controller 单元测试 | @WebMvcTest + MockBean | 接口参数校验、路由 |
| 安全测试 | @SpringBootTest + MockMvc | SQL 注入、XSS、越权 |
| 前端 API 测试 | Vitest + Mock | API 调用正确性 |
| 前端组件测试 | Vitest + Testing Library | 渲染、交互 |

### 测试要求

- 每个 Service 方法至少覆盖：正常路径 + 异常路径
- Controller 测试必须验证参数校验生效
- 新增业务异常必须有对应测试用例
- 测试方法命名：`{方法名}_{场景}_{预期结果}`

---

## Commit Convention

```
feat: 新功能
fix: 修复 bug
docs: 文档变更
test: 测试相关
refactor: 重构
chore: 构建/工具变更
perf: 性能优化
style: 代码格式（不影响功能）
```

### Commit Scope

```
feat(order): 实现订单创建与状态流转
fix(matching): 修复距离计算精度问题
test(sitter): 补充喂养师服务层测试
```

---

## Module Checklist

开发任何模块前，确认以下清单：

- [ ] 是否有对应的 API 文档（openapi/）
- [ ] 是否有数据库设计（database/）
- [ ] 是否有业务规则说明（docs/02-Business/）
- [ ] 是否编写了单元测试
- [ ] 是否有异常处理
- [ ] 是否有参数校验（@Valid）
- [ ] 是否有权限控制（角色注解）
- [ ] 是否更新了 CHANGELOG

---

## Domain-Specific Rules

### 订单安全

- 资金托管模式：下单时预授权冻结，服务完成后释放
- 取消规则：服务开始前 2 小时可免费取消，之后收取服务费 50%
- 纠纷处理：双方均可发起，平台 48 小时内介入裁决
- 并发控制：接单操作使用 Redis 分布式锁，防止重复接单

### 隐私保护

- 宠物主人地址在接单前脱敏展示（仅显示小区名/距离）
- 接单后才开放完整地址 + 门锁密码（一次性授权）
- 服务照片/视频仅主人和喂养师双方可见
- 手机号通过虚拟号码中转（可选功能）

### 服务标准

- 每次服务必须包含：到达打卡照 + 离开打卡照 + 服务报告
- 喂养时长不低于预约时长的 80%（GPS 在场校验）
- 异常情况（宠物状态异常、无法进门等）必须实时上报
- GPS 打卡距离校验：到达 ≤ 200m，离开 ≤ 500m

### 匹配引擎

- 触发：订单进入 PENDING_MATCH 状态时
- 筛选：可用时段 + 地理范围 + 物种 + 接单余量
- 排名：distance(30%) + rating(30%) + completion_rate(20%) + response_time(20%)
- 超时：推送后 15 分钟未接 → 自动分配下一位

---

## Environment Configuration

### 配置文件

| 环境 | 文件 | 用途 |
|------|------|------|
| 开发 | `application-dev.yml` | 本地开发（H2 + 内存缓存） |
| 测试 | `application-test.yml` | 自动化测试 |
| 生产 | `application-prod.yml` | 线上部署（MySQL + Redis + OSS） |

### 环境变量

```yaml
PETTY_DB_URL: jdbc:mysql://localhost:3306/petty
PETTY_DB_USER: petty
PETTY_DB_PASS: {encrypted}
PETTY_REDIS_HOST: localhost
PETTY_OSS_ENDPOINT: oss-cn-beijing.aliyuncs.com
PETTY_OSS_BUCKET: petty-media
PETTY_WECHAT_APPID: {appid}
PETTY_WECHAT_SECRET: {encrypted}
PETTY_JWT_SECRET: {encrypted}
```

---

## Deployment

### 容器化

```
backend/Dockerfile → Java 21 slim
admin-web/Dockerfile → Nginx + static files
```

### 启动顺序

```
MySQL → Redis → RabbitMQ → Backend → Admin-Web
```

### 健康检查

- Backend: `GET /actuator/health`
- Admin-Web: `GET /index.html`
