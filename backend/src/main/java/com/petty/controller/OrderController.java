package com.petty.controller;

import com.petty.common.result.Result;
import com.petty.common.security.RequireRole;
import com.petty.common.security.UserContext;
import com.petty.dto.*;
import com.petty.service.OrderService;
import com.petty.vo.OrderDetailVO;
import com.petty.vo.OrderVO;
import com.petty.vo.ServiceLogVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Result<List<OrderVO>> list(
            @RequestParam(required = false) String status) {
        Long userId = UserContext.getUserId();
        String role = UserContext.getRole();
        if ("SITTER".equals(role)) {
            return Result.success(orderService.listOrders(status, null, userId));
        }
        return Result.success(orderService.listOrders(status, userId, null));
    }

    @GetMapping("/all")
    @RequireRole("ADMIN")
    public Result<List<OrderVO>> listAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long sitterId) {
        return Result.success(orderService.listOrders(status, ownerId, sitterId));
    }

    @GetMapping("/{id}")
    public Result<OrderDetailVO> get(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        String role = UserContext.getRole();
        OrderDetailVO detail = orderService.getOrderDetail(id);
        if (!"ADMIN".equals(role) && !isOrderParticipant(detail, userId, role)) {
            return Result.error(403, "无权查看该订单");
        }
        return Result.success(detail);
    }

    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody OrderCreateDTO dto) {
        Long ownerId = UserContext.getUserId();
        return Result.success(orderService.createOrder(ownerId, dto));
    }

    @PostMapping("/{id}/accept")
    public Result<Void> accept(@PathVariable Long id) {
        Long sitterId = UserContext.getUserId();
        orderService.acceptOrder(id, sitterId);
        return Result.success();
    }

    @PostMapping("/{id}/en-route")
    public Result<Void> enRoute(@PathVariable Long id) {
        Long sitterId = UserContext.getUserId();
        orderService.startEnRoute(id, sitterId);
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id,
                               @RequestBody(required = false) CancelDTO dto) {
        Long sitterId = UserContext.getUserId();
        orderService.rejectOrder(id, sitterId, dto != null ? dto.getReason() : null);
        return Result.success();
    }

    @PostMapping("/{id}/check-in")
    public Result<Void> checkIn(@PathVariable Long id,
                                @Valid @RequestBody CheckInDTO dto) {
        Long sitterId = UserContext.getUserId();
        orderService.checkIn(id, sitterId, dto);
        return Result.success();
    }

    @PostMapping("/{id}/check-out")
    public Result<Void> checkOut(@PathVariable Long id,
                                 @Valid @RequestBody CheckOutDTO dto) {
        Long sitterId = UserContext.getUserId();
        orderService.checkOut(id, sitterId, dto);
        return Result.success();
    }

    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        Long ownerId = UserContext.getUserId();
        orderService.confirmOrder(id, ownerId);
        return Result.success();
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                               @RequestBody(required = false) CancelDTO dto) {
        Long userId = UserContext.getUserId();
        orderService.cancelOrder(id, userId, dto != null ? dto.getReason() : "用户取消");
        return Result.success();
    }

    @GetMapping("/{id}/logs")
    public Result<List<ServiceLogVO>> getLogs(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        String role = UserContext.getRole();
        OrderDetailVO detail = orderService.getOrderDetail(id);
        if (!"ADMIN".equals(role) && !isOrderParticipant(detail, userId, role)) {
            return Result.error(403, "无权查看该订单日志");
        }
        return Result.success(orderService.getServiceLogs(id));
    }

    @PostMapping("/{id}/logs")
    public Result<Void> addLog(@PathVariable Long id,
                               @Valid @RequestBody ServiceLogCreateDTO dto) {
        Long sitterId = UserContext.getUserId();
        orderService.addServiceLog(id, sitterId, dto);
        return Result.success();
    }

    private boolean isOrderParticipant(OrderVO order, Long userId, String role) {
        if ("OWNER".equals(role)) {
            return userId.equals(order.getOwnerId());
        } else if ("SITTER".equals(role)) {
            return userId.equals(order.getSitterId());
        }
        return false;
    }
}
