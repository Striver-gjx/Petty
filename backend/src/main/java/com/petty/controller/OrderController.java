package com.petty.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petty.common.result.Result;
import com.petty.entity.ServiceOrder;
import com.petty.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Result<List<ServiceOrder>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long sitterId) {

        LambdaQueryWrapper<ServiceOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(ServiceOrder::getStatus, status);
        if (ownerId != null) wrapper.eq(ServiceOrder::getOwnerId, ownerId);
        if (sitterId != null) wrapper.eq(ServiceOrder::getSitterId, sitterId);
        wrapper.orderByDesc(ServiceOrder::getCreatedAt);

        return Result.success(orderService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<ServiceOrder> get(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @PostMapping
    public Result<ServiceOrder> create(@RequestBody ServiceOrder order) {
        if (order.getOrderNo() == null) {
            order.setOrderNo("ORD" + System.currentTimeMillis());
        }
        if (order.getStatus() == null) {
            order.setStatus("PENDING_MATCH");
        }
        if (order.getPaymentStatus() == null) {
            order.setPaymentStatus("UNPAID");
        }
        orderService.save(order);
        return Result.success(order);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody StatusDTO dto) {
        ServiceOrder order = orderService.getById(id);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        order.setStatus(dto.status());
        orderService.updateById(order);
        return Result.success();
    }

    public record StatusDTO(String status) {}
}
