package com.petty.controller;

import com.petty.common.result.Result;
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
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long sitterId) {
        return Result.success(orderService.listOrders(status, ownerId, sitterId));
    }

    @GetMapping("/{id}")
    public Result<OrderDetailVO> get(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }

    @PostMapping
    public Result<OrderVO> create(
            @RequestParam(defaultValue = "1") Long ownerId,
            @Valid @RequestBody OrderCreateDTO dto) {
        return Result.success(orderService.createOrder(ownerId, dto));
    }

    @PostMapping("/{id}/accept")
    public Result<Void> accept(@PathVariable Long id,
                               @RequestParam(defaultValue = "1") Long sitterId) {
        orderService.acceptOrder(id, sitterId);
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id,
                               @RequestParam(defaultValue = "1") Long sitterId,
                               @RequestBody(required = false) CancelDTO dto) {
        orderService.rejectOrder(id, sitterId, dto != null ? dto.getReason() : null);
        return Result.success();
    }

    @PostMapping("/{id}/check-in")
    public Result<Void> checkIn(@PathVariable Long id,
                                @RequestParam(defaultValue = "1") Long sitterId,
                                @Valid @RequestBody CheckInDTO dto) {
        orderService.checkIn(id, sitterId, dto);
        return Result.success();
    }

    @PostMapping("/{id}/check-out")
    public Result<Void> checkOut(@PathVariable Long id,
                                 @RequestParam(defaultValue = "1") Long sitterId,
                                 @Valid @RequestBody CheckOutDTO dto) {
        orderService.checkOut(id, sitterId, dto);
        return Result.success();
    }

    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id,
                                @RequestParam(defaultValue = "1") Long ownerId) {
        orderService.confirmOrder(id, ownerId);
        return Result.success();
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                               @RequestParam(defaultValue = "1") Long userId,
                               @RequestBody(required = false) CancelDTO dto) {
        orderService.cancelOrder(id, userId, dto != null ? dto.getReason() : "用户取消");
        return Result.success();
    }

    @GetMapping("/{id}/logs")
    public Result<List<ServiceLogVO>> getLogs(@PathVariable Long id) {
        return Result.success(orderService.getServiceLogs(id));
    }

    @PostMapping("/{id}/logs")
    public Result<Void> addLog(@PathVariable Long id,
                               @RequestParam(defaultValue = "1") Long sitterId,
                               @Valid @RequestBody ServiceLogCreateDTO dto) {
        orderService.addServiceLog(id, sitterId, dto);
        return Result.success();
    }
}
