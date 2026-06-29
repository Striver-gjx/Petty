# AGENTS

所有 AI Agent 开发前必须阅读本文件。

---

## Architecture

```
Controller → Application Service → Domain Service → Repository → Database
```

禁止：Controller 直接调用 Mapper/Repository

---

## Code Style

### Java (Backend)

- Java 21 + SpringBoot 3
- Lombok + MapStruct
- 禁止 `System.out.println`，必须使用 SLF4J
- 方法注释使用 JavaDoc 格式

### TypeScript (Frontend - 小程序端)

- UniApp + Vue3 + TypeScript
- 严格模式，禁止 `any` 类型
- 组件使用 `<script setup>` 语法

### TypeScript (管理后台)

- React + TypeScript + Tailwind CSS
- 严格模式，禁止 `any` 类型

---

## API Convention

统一返回格式：

```java
Result<T> {
  int code;
  String message;
  T data;
}
```

统一异常：`BusinessException`

统一分页：`PageResult<T>`

命名规范：
- DTO：数据传输对象（跨层传递）
- VO：视图对象（返回前端）
- Entity：数据库实体

---

## Data Storage

- 默认数据保存到 MySQL 数据库
- 媒体文件（照片/视频）存储到 OSS 对象存储
- 缓存使用 Redis（会话、匹配结果、热门数据）
- 消息队列用于异步通知（订单状态变更、推送消息）

---

## Error Handling

每个模块必须具备：
- 完整的异常捕捉机制
- 统一错误码定义
- 错误日志记录
- 单元测试覆盖异常分支

---

## Commit Convention

```
feat: 新功能
fix: 修复 bug
docs: 文档变更
test: 测试相关
refactor: 重构
chore: 构建/工具变更
```

---

## Module Checklist

开发任何模块前，确认以下清单：

- [ ] 是否有对应的 API 文档（openapi/）
- [ ] 是否有数据库设计（database/）
- [ ] 是否有业务规则说明（docs/02-Business/）
- [ ] 是否编写了单元测试
- [ ] 是否有异常处理
- [ ] 是否更新了 CHANGELOG

---

## Domain-Specific Rules

### 订单安全

- 资金托管模式：下单时预授权冻结，服务完成后释放
- 取消规则：服务开始前 2 小时可免费取消，之后收取服务费 50%
- 纠纷处理：双方均可发起，平台 48 小时内介入裁决

### 隐私保护

- 宠物主人地址在接单前脱敏展示（仅显示小区/距离）
- 接单后才开放完整地址 + 门锁密码（一次性授权）
- 服务照片/视频仅主人和喂养师双方可见

### 服务标准

- 每次服务必须包含：到达打卡照 + 离开打卡照 + 服务报告
- 喂养时长不低于预约时长的 80%（GPS 在场校验）
- 异常情况（宠物状态异常、无法进门等）必须实时上报
