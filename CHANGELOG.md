# Changelog

All notable changes to this project will be documented in this file.

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
