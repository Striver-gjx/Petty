# Changelog

All notable changes to this project will be documented in this file.

## [0.6.0] - 2026-06-29

### Added
- GitHub Actions CI: backend `mvn verify` + admin-web typecheck/build + mini-app typecheck
- Backend Dockerfile: multi-stage build with JDK 21, non-root user, healthcheck
- Admin-web Dockerfile + nginx reverse proxy for `/api/`
- docker-compose: backend + admin-web + MySQL + Redis full orchestration
- Spring Boot Actuator health endpoint (`/actuator/health`)
- `@RequireRole` annotation + `RoleInterceptor` for ADMIN role isolation
- Controller integration tests: Auth, Owner, Pet, Sitter, Withdrawal (30 new tests)
- ADMIN login support in AuthController
- `.dockerignore` for backend and admin-web

### Fixed
- `OrderScheduler.handleAutoConfirm()` now calls `orderService.confirmOrder()` for full settlement
- Admin-web Login page uses ADMIN role instead of OWNER
- Sensitive field exposure: Sitter/Owner entities now use `@JsonIgnore` on PII fields

### Security
- `/orders/all` requires ADMIN role
- Sitter approve/reject/create/update requires ADMIN role
- Actuator endpoints excluded from JWT filter

## [0.5.0] - 2026-06-29

### Added
- Admin-web: order detail page with service logs timeline
- Admin-web: sitter operations (approve/reject/suspend/activate)
- Admin-web: Owner/Pet/Sitter CRUD forms with modal dialogs
- Admin-web: search/filter on all list pages + status tabs on orders
- Admin-web: settings page with commission display and service type CRUD
- Mini-app: pet add/edit form with species/gender pickers
- Mini-app: multi-dimensional review page (5 dimensions, tags, anonymous)
- Mini-app: enhanced order detail with pet list, service logs, action buttons

### Fixed
- Dashboard `orderApi.list()` → `orderApi.listAll()` bug

## [0.4.0] - 2026-06-29

### Added
- Order lifecycle: `SITTER_EN_ROUTE` status + `/orders/{id}/en-route` endpoint
- Scheduled tasks: accept timeout (30min re-match), auto-confirm (24h), auto-review (72h)
- Service log validation: require at least one log before check-out
- Member discount + auto-upgrade (NORMAL→VIP→SVIP)
- Sitter wallet balance + withdrawal balance check
- Multi-dimensional reviews (5 rating dimensions) with 72h time window
- Sitter schedule filtering in matching engine
- Sitter onboarding flow (apply/approve/reject)
- Mini-app: JWT login page with role selection
- Mini-app: sitter order management + active work pages
- Admin-web: JWT authentication with login page
- P0 complete: payment after order creation, reject triggers re-match

## [0.3.0] - 2026-06-29

### Added
- JWT 认证框架（JwtUtil + JwtAuthFilter + UserContext + AuthController）
- 乐观锁防止订单状态并发修改（version 字段 + OptimisticLockerInterceptor）
- order_pet 关联表写入与查询
- ServiceTypeService 服务层（修复分层违反）
- ServiceTypeVO 视图对象

### Fixed
- 取消订单现在正确触发退款（按阶梯费率）
- 退款接口验证支付归属（ownerId 校验）
- capturePayment 拒绝已退款订单
- 喂养师身份验证（check-in/check-out/addServiceLog/reject）
- 评价身份验证（必须是订单参与方）
- SLF4J 格式 `{:.2f}` 修正为 `{}`
- CheckIn/CheckOut DTO 纬度/经度校验消息互换修正
- 150 个测试全部通过

### Security
- Controllers 不再接受 query param 传递用户身份
- 宠物归属校验阻止跨用户操作
- 开发模式下无 token 默认用户（生产模式返回 401）

## [0.2.0] - 2026-06-28

### Added
- 评价模块（ReviewService + ReviewController）
- 支付模块（PaymentService + PaymentController）
- 提现模块（WithdrawalService + WithdrawalController）
- 32 个边界条件和并发安全测试
- JaCoCo 覆盖率插件

## [0.1.0] - 2026-06-28

### Added
- Spring Boot 3.3 后端骨架（H2 + MyBatis-Plus 3.5.9）
- 订单状态机（7 种状态 + 状态转换规则）
- 智能匹配引擎（Haversine 距离 + 多因子排名）
- GPS 到达/离开打卡校验
- 取消规则（阶梯退款费率）
- 佣金分级计算
- React 管理后台（6 个页面）
- UniApp 小程序端（7 个页面）
- 完整项目文档（PROJECT_CONTEXT / AGENTS / DB Schema / OpenAPI / Business Rules）
